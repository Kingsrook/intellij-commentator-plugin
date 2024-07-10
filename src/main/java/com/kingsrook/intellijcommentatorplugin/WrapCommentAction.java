/*
 * Kingsrook IntelliJ Commentator Plugin
 * Copyright (C) 2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.intellijcommentatorplugin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.kingsrook.intellijcommentatorplugin.settings.CommentatorSettingsState;
import com.kingsrook.intellijcommentatorplugin.utils.CommentUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WrapCommentAction extends AbstractKRCommentatorEditorAction
{
   private int maxLength = 94;
   private int minLength = 54;

   private static long lastCallTime           = -1L;
   private static int  lastTransformIndexUsed = 0;



   /*******************************************************************************
    **
    *******************************************************************************/
   enum Transform
   {
      DO_NOT_REWRAP,
      DO_REWRAP
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public WrapCommentAction()
   {
      super("Wrap Comment");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public WrapCommentAction(String text)
   {
      super(text);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void readSettings()
   {
      try
      {
         CommentatorSettingsState settings = CommentatorSettingsState.getInstance();
         maxLength = settings.wrapCommentMaxWidth;
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void actionPerformed(AnActionEvent event)
   {
      readSettings();

      //////////////////////////////////////////////
      // Get all the required data from data keys //
      //////////////////////////////////////////////
      Editor  editor  = event.getRequiredData(CommonDataKeys.EDITOR);
      Project project = event.getProject();

      ///////////////////////////////////////////
      // Access document, caret, and selection //
      ///////////////////////////////////////////
      Document       document       = editor.getDocument();
      SelectionModel selectionModel = editor.getSelectionModel();

      String commentChar = CommentUtils.getCommentChar(document);

      ////////////////////////////
      // do rotating transforms //
      ////////////////////////////
      int transformIndexToUse = 0;
      if(System.currentTimeMillis() < lastCallTime + 1000)
      {
         transformIndexToUse = lastTransformIndexUsed + 1;
      }
      if(transformIndexToUse >= Transform.values().length)
      {
         transformIndexToUse = 0;
      }
      lastTransformIndexUsed = transformIndexToUse;
      Transform transform = Transform.values()[transformIndexToUse];
      lastCallTime = System.currentTimeMillis();

      ////////////////////////////////////////////////////////////////////////
      // find the start & end lines, based on selection start & end         //
      // expanded upward & downward as long as more comment lines are found //
      ////////////////////////////////////////////////////////////////////////
      int selectionStartLine = CommentUtils.getSelectionStartLine(selectionModel, document, commentChar);
      int selectionEndLine   = CommentUtils.getSelectionEndLine(selectionModel, document, commentChar);

      //////////////////////////////////////////
      // get the range of text being replaced //
      //////////////////////////////////////////
      int       replacementStartOffset = document.getLineStartOffset(selectionStartLine);
      int       replacementEndOffset   = document.getLineEndOffset(selectionEndLine);
      TextRange textRange              = new TextRange(replacementStartOffset, replacementEndOffset);

      ///////////////////////////////////////////////////////////////////////
      // build the replacement text, feeding it the comment-lines as input //
      ///////////////////////////////////////////////////////////////////////
      String        commentLinesText = document.getText(textRange);
      StringBuilder replacementText  = getReplacementText(commentLinesText, commentChar, transform, maxLength);

      //////////////////////////
      // make the replacement //
      //////////////////////////
      WriteCommandAction.runWriteCommandAction(project, () ->
         document.replaceString(replacementStartOffset, replacementEndOffset, replacementText)
      );

      /////////////////////////////////
      // un-select what was selected //
      /////////////////////////////////
      selectionModel.removeSelection();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   StringBuilder getReplacementText(String commentLinesText, String commentChar, Transform transform, int length)
   {
      if(transform == Transform.DO_REWRAP)
      {
         return getReplacementDoingRewrap(commentLinesText, commentChar);
      }

      String[] lines       = commentLinesText.split("\n");
      Integer  leastIndent = null;

      ///////////////////////////////////////////////////////////////////////////////////////////
      // iterate over the input lines, building a list of lines to be in the replacement text. //
      // note that the header & footer will not be in the replacementLines list                //
      ///////////////////////////////////////////////////////////////////////////////////////////
      List<String> replacementLines = new LinkedList<>();
      for(int i = 0; i < lines.length; i++)
      {
         String line = lines[i];

         //////////////////////////////////////////////////////////////////////
         // find the indent of this line - see if it's the least we've found //
         //////////////////////////////////////////////////////////////////////
         int lineIndent = 0;
         for(int c = 0; c < line.length(); c++)
         {
            if(line.charAt(c) != ' ')
            {
               lineIndent = c;
               break;
            }
         }

         if(leastIndent == null || lineIndent < leastIndent)
         {
            leastIndent = lineIndent;
         }

         ////////////////////////////////////////////
         // strip away any leading or trailing /'s //
         ////////////////////////////////////////////
         line = line.replaceAll("^( *)" + commentChar + "+ ?", ""); // used to be "$1" for RHS
         line = line.replaceAll(" *" + commentChar + "+ *$", "");

         ////////////////////////////////
         // strip away trailing spaces //
         ////////////////////////////////
         line = line.replaceAll(" *$", "");

         ////////////////////////////////////////////////////////////////////////////////////////////////////
         // if the line is empty, and it's the first or last, then assume it was a previous header/footer, //
         // and leave it out of the replacementLines list                                                  //
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         boolean lineIsEmpty = line.trim().length() == 0;

         if(lineIsEmpty)
         {
            if(i == 0 || i == lines.length - 1)
            {
               continue;
            }
         }
         replacementLines.add(line);

         /////////////////////////////////////////////////////////////////////////////////////////
         // else, if not first or last line, skip empty lines when computing indent and lengths //
         /////////////////////////////////////////////////////////////////////////////////////////
         if(lineIsEmpty)
         {
            continue;
         }
      }

      ///////////////////////////////////////////
      // ensure a least-indent value was found //
      ///////////////////////////////////////////
      if(leastIndent == null)
      {
         leastIndent = 0;
      }

      ////////////////////////////////////////////////////////////////////
      // build the string to be inserted before all lines as the indent //
      ////////////////////////////////////////////////////////////////////
      StringBuilder indentString = new StringBuilder();
      for(int i = 0; i < leastIndent; i++)
      {
         indentString.append(' ');
      }

      // System.out.println(replacementLines);
      replacementLines = manipulateLines(replacementLines, length);

      ///////////////////////////
      // find the longest line //
      ///////////////////////////
      Integer longestLength = 0;
      for(String line : replacementLines)
      {
         if(line.length() > longestLength)
         {
            longestLength = line.length();
         }
      }

      ///////////////////////////////////////////////////////////////////
      // build the top & bottom line for the boxes and do other things //
      ///////////////////////////////////////////////////////////////////
      StringBuilder topAndBottom = new StringBuilder();
      for(int i = 0; i < longestLength + 6; i++)
      {
         topAndBottom.append(commentChar);
      }

      ////////////////////////////////////////////////////////////////////////
      // start building our replacement text with an (indented) header line //
      ////////////////////////////////////////////////////////////////////////
      StringBuilder replacementText = new StringBuilder(indentString);
      replacementText.append(topAndBottom).append("\n");

      ////////////////////////////////////////////////////////////
      // loop over the replacement lines, building final output //
      ////////////////////////////////////////////////////////////
      for(String line : replacementLines)
      {
         ///////////////////////////////////////////////////////////////////////////////////////////
         // start with an indent, then comment chars, then the text (minus the leading indention) //
         ///////////////////////////////////////////////////////////////////////////////////////////
         StringBuilder replacementLine = new StringBuilder(indentString).append(commentChar).append(commentChar).append(" ").append(line);

         //////////////////////////////////////
         // if padding is needed, add it now //
         //////////////////////////////////////
         while(replacementLine.length() < indentString.length() + longestLength + 3)
         {
            replacementLine.append(' ');
         }

         /////////////////////////////////////////////
         // finalize with comment chars and newline //
         /////////////////////////////////////////////
         replacementLine.append(" ").append(commentChar).append(commentChar).append("\n");

         replacementText.append(replacementLine);
      }

      //////////////////////////////////////////////////
      // add a final bottom line of all comment chars //
      //////////////////////////////////////////////////
      replacementText.append(indentString).append(topAndBottom);

      return replacementText;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private StringBuilder getReplacementDoingRewrap(String commentLinesText, String commentChar)
   {
      StringBuilder best          = null;
      int           bestLineCount = Integer.MAX_VALUE;

      for(int length = maxLength; length >= minLength; length--)
      {
         StringBuilder candidate          = getReplacementText(commentLinesText, commentChar, Transform.DO_NOT_REWRAP, length);
         int           candidateLineCount = candidate.toString().split("\n").length;
         if(candidateLineCount <= bestLineCount)
         {
            best = candidate;
            bestLineCount = candidateLineCount;
         }
      }

      return (best);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected List<String> manipulateLines(List<String> inputLines, int length)
   {
      List<String>  words              = new ArrayList<>();
      List<String>  spaceAfterWords    = new ArrayList<>();
      List<Boolean> newlinesAfterWords = new ArrayList<>();

      StringBuilder currentWord     = null;
      int           spacesAfterWord = 0;
      for(String inputLine : inputLines)
      {
         for(int i = 0; i < inputLine.length(); i++)
         {
            char c = inputLine.charAt(i);
            if(currentWord == null)
            {
               currentWord = new StringBuilder().append(c);

               /////////////////////////////////////////////////////////
               // deal with multiple spaces together at start of line //
               /////////////////////////////////////////////////////////
               if(c == ' ')
               {
                  while(i < inputLine.length() - 1 && inputLine.charAt(i + 1) == ' ')
                  {
                     currentWord.append(' ');
                     i++;
                  }
                  if(currentWord.toString().matches("^ +$"))
                  {
                     words.add(currentWord.toString());
                     spaceAfterWords.add(String.join("", Collections.nCopies(spacesAfterWord, " ")));
                     newlinesAfterWords.add(false);
                     currentWord = null;
                  }
               }
            }
            else
            {
               if(spacesAfterWord > 0)
               {
                  if(c == ' ')
                  {
                     ////////////////////////////////
                     // multiple spaces after word //
                     ////////////////////////////////
                     spacesAfterWord++;
                  }
                  else
                  {
                     ////////////////////////
                     // start of next word //
                     ////////////////////////
                     words.add(currentWord.toString());
                     spaceAfterWords.add(String.join("", Collections.nCopies(spacesAfterWord, " ")));
                     newlinesAfterWords.add(false);
                     currentWord = new StringBuilder().append(c);
                     spacesAfterWord = 0;
                  }
               }
               else
               {
                  if(c == ' ')
                  {
                     spacesAfterWord = 1;
                  }
                  else
                  {
                     currentWord.append(c);
                  }
               }
            }
         }

         if(currentWord != null)
         {
            words.add(currentWord.toString());
            spaceAfterWords.add(String.join("", Collections.nCopies(spacesAfterWord, " ")));

            char lastChar = currentWord.charAt(currentWord.length() - 1);
            if(lastChar == '.' || lastChar == '?' || lastChar == '!')
            {
               newlinesAfterWords.add(true);
            }
            else
            {
               newlinesAfterWords.add(false);
            }

            currentWord = null;
            spacesAfterWord = 0;
         }
      }

      List<String>  outputLines = new ArrayList<>();
      StringBuilder outputLine  = null;

      ////////////////////////////////////////////
      // loop over words, building output lines //
      ////////////////////////////////////////////
      for(int i = 0; i < words.size(); i++)
      {
         // System.out.format("[%s][%s]\n", words.get(i), spaceAfterWords.get(i));

         String  word             = words.get(i);
         String  spaceAfterWord   = spaceAfterWords.get(i);
         Boolean newlineAfterWord = newlinesAfterWords.get(i);

         //////////////////////////////////////////////////////////////////////////
         // if there's no space after the word, replace that with a single-space //
         // this happens if a word ended at the end of an input line             //
         //////////////////////////////////////////////////////////////////////////
         if("".equals(spaceAfterWord) && !word.matches("^ +$"))
         {
            spaceAfterWord = " ";
         }

         ////////////////////////////////////////////
         // start new output lines with first word //
         ////////////////////////////////////////////
         if(outputLine == null)
         {
            outputLine = new StringBuilder().append(word);
         }
         else
         {
            boolean forcedNextLine = false;

            //////////////////////////////////////////////////////////////////////////////
            // force the word onto the next line if we captured a leading-space with it //
            // this would come from an indented line.                                   //
            //////////////////////////////////////////////////////////////////////////////
            if(word.startsWith(" "))
            {
               forcedNextLine = true;
            }

            /////////////////////////////////////////////
            // add word to the output line, if it fits //
            /////////////////////////////////////////////
            if(outputLine.length() + word.length() <= length && !forcedNextLine)
            {
               outputLine.append(word);
            }
            else
            {
               //////////////////////////////////////////////////////////////////////////////
               // if the word doesn't fit, then start a new output line with the next word //
               //////////////////////////////////////////////////////////////////////////////
               outputLines.add(outputLine.toString());
               outputLine = new StringBuilder().append(word);
            }

            /////////////////////////////////////////////////////////////////////
            // start a new line, if there's supposed to be one after this word //
            /////////////////////////////////////////////////////////////////////
            if(newlineAfterWord)
            {
               outputLines.add(outputLine.toString());
               outputLine = null;
            }
         }

         //////////////////////////////////////////////////////////////////////
         // if there's room for the space(s) after this word, then add them  //
         // else start a new line.  this would be to prevent a word that had //
         // multiple spaces after it from losing them to a shorter next-word //
         //////////////////////////////////////////////////////////////////////
         if(outputLine != null)
         {
            if(outputLine.length() + spaceAfterWord.length() <= length)
            {
               outputLine.append(spaceAfterWord);
            }
            else
            {
               outputLines.add(outputLine.toString());
               outputLine = null;
            }
         }
      }

      if(outputLine != null)
      {
         outputLines.add(outputLine.toString());
      }

      ///////////////////////////////////////////////////
      // trim any spaces away from end of output lines //
      ///////////////////////////////////////////////////
      outputLines.replaceAll(s -> s.replaceFirst(" +$", ""));

      return outputLines;
   }



   /*******************************************************************************
    ** Setter for maxLength
    **
    *******************************************************************************/
   public void setMaxLength(int maxLength)
   {
      this.maxLength = maxLength;
   }



   /*******************************************************************************
    ** Setter for minLength
    **
    *******************************************************************************/
   public void setMinLength(int minLength)
   {
      this.minLength = minLength;
   }
}

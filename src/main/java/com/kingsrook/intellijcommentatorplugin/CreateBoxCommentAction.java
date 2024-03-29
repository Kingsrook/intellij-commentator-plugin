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


import java.util.LinkedList;
import java.util.List;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;


/*******************************************************************************
 **
 *******************************************************************************/
public class CreateBoxCommentAction extends AnAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public CreateBoxCommentAction()
   {
      super("Create Box Comment");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CreateBoxCommentAction(String text)
   {
      super(text);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void update(AnActionEvent event)
   {
      ////////////////////////////
      // Get required data keys //
      ////////////////////////////
      final Project project = event.getProject();
      final Editor  editor  = event.getData(CommonDataKeys.EDITOR);

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // Set visibility only in case of existing project and editor and if some text in the editor is selected //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      event.getPresentation().setVisible(project != null && editor != null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void actionPerformed(AnActionEvent event)
   {
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

      String commentChar = "/";
      try
      {
         VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
         if(currentFile != null)
         {
            String fileName = currentFile.getPath();
            if(fileName.endsWith(".sh") || fileName.endsWith(".vtl") || fileName.endsWith(".vm") || fileName.endsWith(".pl"))
            {
               commentChar = "#";
            }
         }
      }
      catch(Exception e)
      {
         // leave default
      }

      ////////////////////////////////////////////////////////////////
      // find the start & end lines, based on selection start & end //
      ////////////////////////////////////////////////////////////////
      int selectionStartOffset = selectionModel.getSelectionStart();
      int selectionEndOffset   = selectionModel.getSelectionEnd();

      int selectionStartLine = document.getLineNumber(selectionStartOffset);
      int selectionEndLine   = document.getLineNumber(selectionEndOffset);

      /////////////////////////////////////////////////////////////////////////
      // expand the selection upward as long as more comment lines are found //
      /////////////////////////////////////////////////////////////////////////
      while(selectionStartLine > 1)
      {
         TextRange lineAboveSelectionTextRange = new TextRange(document.getLineStartOffset(selectionStartLine - 1), document.getLineEndOffset(selectionStartLine - 1));
         String    lineAbove                   = document.getText(lineAboveSelectionTextRange);

         if(lineAbove.matches("^ *" + commentChar + commentChar + ".*"))
         {
            selectionStartLine--;
         }
         else
         {
            break;
         }
      }

      ///////////////////////////////////////////////////////////////////////////
      // expand the selection downward as long as more comment lines are found //
      ///////////////////////////////////////////////////////////////////////////
      while(selectionEndLine < document.getLineNumber(document.getTextLength()) - 1)
      {
         TextRange lineBelowSelectionTextRange = new TextRange(document.getLineStartOffset(selectionEndLine + 1), document.getLineEndOffset(selectionEndLine + 1));
         String    lineBelow                   = document.getText(lineBelowSelectionTextRange);

         if(lineBelow.matches("^ *" + commentChar + commentChar + ".*"))
         {
            selectionEndLine++;
         }
         else
         {
            break;
         }
      }

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
      StringBuilder replacementText  = getReplacementText(commentLinesText, commentChar);

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
   protected StringBuilder getReplacementText(String commentLinesText, String commentChar)
   {
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

         ////////////////////////////////////////////
         // strip away any leading or trailing /'s //
         ////////////////////////////////////////////
         line = line.replaceAll("^( *)" + commentChar + "+ *", "$1");
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

      replacementLines = manipulateLines(indentString, replacementLines);

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
      for(int i = 0; i < longestLength + 6 - leastIndent; i++)
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
         StringBuilder replacementLine = new StringBuilder(indentString).append(commentChar).append(commentChar).append(" ").append(line.substring(Math.min(line.length(), leastIndent)));

         //////////////////////////////////////
         // if padding is needed, add it now //
         //////////////////////////////////////
         while(replacementLine.length() < longestLength + 3)
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
   protected List<String> manipulateLines(StringBuilder indentString, List<String> replacementLines)
   {
      return (replacementLines);
   }
}

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
import java.util.List;
import java.util.function.Function;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;


/*******************************************************************************
 **
 *******************************************************************************/
public class CaseChangerAction extends AbstractKRCommentatorEditorAction
{
   private static long lastCallTime           = -1L;
   private static int  lastTransformIndexUsed = 0;



   /*******************************************************************************
    ** one
    ** One
    ** one
    ** ONE
    **
    ** twoThree
    ** TwoThree
    ** two_three
    ** TWO_THREE
    ** // two-three
    ** // TWO-THREE
    *******************************************************************************/
   enum Transform
   {
      CAMEL_CASE,
      PASCAL_CASE,
      LOWER_CASE_UNDERSCORES,
      UPPER_CASE_UNDERSCORES,
      // LOWER_CASE_DASHES,
      // UPPER_CASE_DASHES
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CaseChangerAction()
   {
      super("Case Changer");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CaseChangerAction(String text)
   {
      super(text);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void actionPerformed(AnActionEvent event)
   {
      try
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
         boolean        reSelectAtEnd  = (selectionModel.getSelectionStart() != selectionModel.getSelectionEnd());

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

         CaretModel  caretModel = editor.getCaretModel();
         List<Caret> allCarets  = caretModel.getAllCarets();
         System.out.println("there are [" + allCarets.size() + "] carets");
         if(allCarets.size() > 1)
         {
            for(Caret caret : allCarets)
            {
               //////////////////////////////////////////////
               // expand selection to cover complete words //
               //////////////////////////////////////////////
               TextRange textRange   = getTextRange(document, caret);
               int       startOffset = textRange.getStartOffset();
               int       endOffset   = textRange.getEndOffset();

               ////////////////////////////////
               // build the replacement text //
               ////////////////////////////////
               String inputText       = document.getText(textRange);
               String replacementText = getReplacementText(inputText, transform);

               //////////////////////////
               // make the replacement //
               //////////////////////////
               WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(startOffset, endOffset, replacementText));
            }
         }
         else
         {
            //////////////////////////////////////////////
            // expand selection to cover complete words //
            //////////////////////////////////////////////
            TextRange textRange   = getTextRange(document, selectionModel);
            int       startOffset = textRange.getStartOffset();
            int       endOffset   = textRange.getEndOffset();

            ////////////////////////////////
            // build the replacement text //
            ////////////////////////////////
            String inputText       = document.getText(textRange);
            String replacementText = getReplacementText(inputText, transform);

            //////////////////////////
            // make the replacement //
            //////////////////////////
            WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(startOffset, endOffset, replacementText));

            /////////////////////////////////
            // re-select what was selected //
            /////////////////////////////////
            if(reSelectAtEnd)
            {
               int diff = replacementText.length() - inputText.length();
               selectionModel.setSelection(startOffset, endOffset + diff);
            }
         }

         lastCallTime = System.currentTimeMillis();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static String getReplacementText(String inputText, Transform transform)
   {
      StringBuilder rs = new StringBuilder();
      for(String word : toWords(inputText))
      {
         if(!word.isEmpty())
         {
            try
            {
               rs.append(switch(transform)
               {
                  case CAMEL_CASE -> toCamelCase(word);
                  case PASCAL_CASE -> toPascalCase(word);
                  case LOWER_CASE_UNDERSCORES -> toCase(word, "_", String::toLowerCase);
                  case UPPER_CASE_UNDERSCORES -> toCase(word, "_", String::toUpperCase);
                  // case LOWER_CASE_DASHES -> toCase(inputText, "-", String::toLowerCase);
                  // case UPPER_CASE_DASHES -> toCase(inputText, "-", String::toUpperCase);
               });
            }
            catch(Exception e)
            {
               rs.append(word);
            }
         }
      }
      return (rs.toString());
   }



   /*******************************************************************************
    * split a single "word" into its sub-words.
    * word being, separated by spaces.
    * subWord being, camelCase or under_score separated.
    *******************************************************************************/
   static List<String> toWords(String input)
   {
      List<String> rs = new ArrayList<>();
      StringBuilder currentWord = new StringBuilder();
      boolean inWord = false;
      boolean inNonWord = false;
      for(int i = 0; i < input.length(); i++)
      {
         char c = input.charAt(i);
         boolean isWordChar = isWordPart(c);
         if((isWordChar && inNonWord) || (!isWordChar && inWord))
         {
            rs.add(currentWord.toString());
            currentWord = new StringBuilder();
         }

         currentWord.append(c);
         inWord = isWordChar;
         inNonWord = !inWord;
      }

      rs.add(currentWord.toString());

      return (rs);
   }



   /*******************************************************************************
    * split a single "word" into its sub-words.
    * word being, separated by spaces.
    * subWord being, camelCase or under_score separated.
    *******************************************************************************/
   static List<String> toSubWords(String input)
   {
      List<String> rs    = new ArrayList<>();
      String[]     words = input.split("[_]");

      for(String word : words)
      {
         StringBuilder subWord = new StringBuilder();

         boolean lastCharLower = false;
         boolean lastCharUpper = true;
         for(int j = 0; j < word.length(); j++)
         {
            char c = word.charAt(j);
            boolean thisCharLower = Character.isLowerCase(c);
            boolean thisCharUpper = Character.isUpperCase(c);

            if(lastCharLower && thisCharUpper)
            {
               rs.add(subWord.toString());
               subWord = new StringBuilder();
            }

            subWord.append(c);

            lastCharLower = thisCharLower;
            lastCharUpper = thisCharUpper;
         }

         if(!subWord.isEmpty())
         {
            rs.add(subWord.toString());
         }
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String toCamelCase(String input)
   {
      List<String>  words = toSubWords(input);
      StringBuilder rs    = new StringBuilder(words.get(0).toLowerCase());
      for(int i = 1; i < words.size(); i++)
      {
         rs.append(ucFirst(words.get(i).toLowerCase()));
      }
      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String ucFirst(String input)
   {
      return input.substring(0, 1).toUpperCase() + (input.length() > 1 ? input.substring(1) : "");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String toPascalCase(String input)
   {
      List<String>  words = toSubWords(input);
      StringBuilder rs    = new StringBuilder(ucFirst(words.get(0).toLowerCase()));
      for(int i = 1; i < words.size(); i++)
      {
         rs.append(ucFirst(words.get(i).toLowerCase()));
      }
      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String toCase(String input, String separator, Function<String, String> s)
   {
      List<String>  words = toSubWords(input);
      StringBuilder rs    = new StringBuilder(s.apply(words.get(0)));
      for(int i = 1; i < words.size(); i++)
      {
         rs.append(separator);
         rs.append(s.apply(words.get(i)));
      }
      return (rs.toString());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static TextRange getTextRange(Document document, Caret caret)
   {
      int start = caret.getSelectionStart();
      int end   = caret.getSelectionEnd();

      if(start == end)
      {
         if(Character.isWhitespace(getChar(document, end)))
         {
            start--;
         }
         else
         {
            end++;
         }
      }

      while(isWordPart(getChar(document, start)))
      {
         start--;
      }
      start++;

      while(isWordPart(getChar(document, end)))
      {
         end++;
      }

      TextRange textRange = new TextRange(start, end);
      System.out.println(start + "-" + end + ": [" + document.getText(textRange) + "]");
      return textRange;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static TextRange getTextRange(Document document, SelectionModel selectionModel)
   {
      int start = selectionModel.getSelectionStart();
      int end   = selectionModel.getSelectionEnd();

      if(start == end)
      {
         if(Character.isWhitespace(getChar(document, end)))
         {
            start--;
         }
         else
         {
            end++;
         }
      }

      while(isWordPart(getChar(document, start)))
      {
         start--;
      }
      start++;

      while(isWordPart(getChar(document, end)))
      {
         end++;
      }

      TextRange textRange = new TextRange(start, end);
      System.out.println(start + "-" + end + ": [" + document.getText(textRange) + "]");
      return textRange;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isWordPart(char c)
   {
      // return (Character.isLetterOrDigit(c) || c == '_' || c == '-');
      return (Character.isLetterOrDigit(c) || c == '_');
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static char getChar(Document document, int position)
   {
      TextRange textRange = new TextRange(position, position + 1);
      return (document.getText(textRange).charAt(0));
   }

}

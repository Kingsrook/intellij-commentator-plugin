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


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;


/*******************************************************************************
 **
 *******************************************************************************/
public class WordToSnakeCaseAction extends AbstractKRCommentatorEditorAction
{
   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WordToSnakeCaseAction()
   {
      super("WordToSnakeCase");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
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

         ////////////////////////////////////////////////////////////////
         // find the start & end lines, based on selection start & end //
         ////////////////////////////////////////////////////////////////
         int selectionStartOffset = selectionModel.getSelectionStart();
         int selectionEndOffset   = selectionModel.getSelectionEnd();

         int selectionStartLine = document.getLineNumber(selectionStartOffset);
         int selectionEndLine   = document.getLineNumber(selectionEndOffset);

         //////////////////////////////////////////
         // get the range of text being replaced //
         // todo - if a selection range, that whole range, but if just 1 char, then that word
         //////////////////////////////////////////
         int       startOffset = document.getLineStartOffset(selectionStartLine);
         int       endOffset   = document.getLineEndOffset(selectionEndLine);
         TextRange textRange   = new TextRange(startOffset, endOffset);

         ///////////////////////////////////////////////////////////////////////
         // build the replacement text, feeding it the comment-lines as input //
         ///////////////////////////////////////////////////////////////////////
         String        selectedLinesText = document.getText(textRange);
         StringBuilder replacementText   = getReplacementText(selectedLinesText);

         //////////////////////////
         // make the replacement //
         //////////////////////////
         WriteCommandAction.runWriteCommandAction(project, () ->
            document.replaceString(startOffset, endOffset, replacementText)
         );

         /////////////////////////////////
         // un-select what was selected //
         /////////////////////////////////
         selectionModel.removeSelection();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public StringBuilder getReplacementText(String selectedLinesText)
   {
      StringBuilder rs = new StringBuilder();
      for(int i = 0; i < selectedLinesText.length(); i++)
      {
         // todo - camelCase to camel_case, etc...
         char c = selectedLinesText.charAt(i);
         if(c == '%' && i < selectedLinesText.length() - 2)
         {
            String hex         = selectedLinesText.substring(i + 1, i + 3);
            char   replacement = (char) Integer.parseInt(hex, 16);
            rs.append(replacement);
            i += 2;
         }
         else
         {
            rs.append(c);
         }
      }
      return (rs);
   }
}

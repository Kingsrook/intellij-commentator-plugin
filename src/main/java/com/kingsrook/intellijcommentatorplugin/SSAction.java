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
public class SSAction extends AnAction
{
   private String lastFind = null;
   private String lastReplace = null;

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SSAction()
   {
      super("SmartSubstitute");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void actionPerformed(AnActionEvent event)
   {
      SSDialogWrapper dialog = new SSDialogWrapper(lastFind, lastReplace);
      dialog.show();

      if(dialog.isOK() && dialog.getFind() != null && dialog.getReplace() != null)
      {
         updateDocument(event, dialog.getFind(), dialog.getReplace());
         lastFind = dialog.getFind();
         lastReplace = dialog.getReplace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void updateDocument(AnActionEvent event, String find, String replace)
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
      //////////////////////////////////////////
      int       replacementStartOffset = document.getLineStartOffset(selectionStartLine);
      int       replacementEndOffset   = document.getLineEndOffset(selectionEndLine);
      TextRange textRange              = new TextRange(replacementStartOffset, replacementEndOffset);

      ///////////////////////////////////////////////////////////////////////
      // build the replacement text, feeding it the comment-lines as input //
      ///////////////////////////////////////////////////////////////////////
      String        selectedLinesText = document.getText(textRange);
      StringBuilder replacementText   = getReplacementText(selectedLinesText, find, replace);

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
   private StringBuilder getReplacementText(String text, String find, String replace)
   {
      if(text == null || text.trim().length() == 0)
      {
         return new StringBuilder(text);
      }

      find = find.trim();
      String findRest = find.length() > 1 ? find.substring(1) : "";
      String lc1Find  = find.substring(0, 1).toLowerCase() + findRest;
      String uc1Find  = find.substring(0, 1).toUpperCase() + findRest;
      String ucFind   = find.toUpperCase(); // todo - innerCammels to _'s

      replace = replace == null ? "" : replace.trim();
      String replaceRest = replace.length() > 1 ? replace.substring(1) : "";
      String lc1Replace  = replace.length() == 0 ? "" : (replace.substring(0, 1).toLowerCase() + replaceRest);
      String uc1Replace  = replace.length() == 0 ? "" : (replace.substring(0, 1).toUpperCase() + replaceRest);
      String ucReplace   = replace.toUpperCase();

      return new StringBuilder(text
         .replaceAll(lc1Find, lc1Replace)
         .replaceAll(uc1Find, uc1Replace)
         .replaceAll(ucFind, ucReplace)
      );
   }
}

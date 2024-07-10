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


import java.util.List;
import java.util.Objects;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretState;
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
public class HeaderCommentAction extends AbstractKRCommentatorEditorAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public HeaderCommentAction()
   {
      super("Write Header Comment");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public HeaderCommentAction(String text)
   {
      super(text);
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

      String commentChar          = "*";
      String prefix               = "/";
      String suffix               = "/";
      String subsequentLineIndent = " ";
      try
      {
         VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
         if(currentFile != null)
         {
            String fileName = currentFile.getPath();
            if(fileName.endsWith(".sh") || fileName.endsWith(".vtl") || fileName.endsWith(".vm") || fileName.endsWith(".pl") || fileName.endsWith(".properties"))
            {
               commentChar = "#";
               prefix = "";
               suffix = "";
               subsequentLineIndent = "";
            }
            else if(fileName.endsWith(".xml") || fileName.endsWith(".html"))
            {
               commentChar = "=";
               prefix = "<!-- ";
               suffix = " -->";
               subsequentLineIndent = "";
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
      int selectionStartLine   = document.getLineNumber(selectionStartOffset);

      //////////////////////////////////////////
      // get the range of text being replaced //
      //////////////////////////////////////////
      int    replacementPoint = document.getLineStartOffset(selectionStartLine);
      String selectionLine    = document.getText(new TextRange(document.getLineStartOffset(selectionStartLine), document.getLineEndOffset(selectionStartLine)));
      String indent           = selectionLine.replaceAll("^( *).*", "$1");

      ///////////////////////////////////////////////////////////////////////
      // build the replacement text, feeding it the comment-lines as input //
      ///////////////////////////////////////////////////////////////////////
      StringBuilder replacementText = getReplacementText(indent, commentChar, prefix, suffix, subsequentLineIndent);

      //////////////////////////
      // make the replacement //
      //////////////////////////
      WriteCommandAction.runWriteCommandAction(project, () ->
         document.replaceString(replacementPoint, replacementPoint, replacementText)
      );

      /////////////////////////////////
      // un-select what was selected //
      /////////////////////////////////
      // selectionModel.removeSelection();
      // selectionModel.setSelection(replacementPoint, replacementPoint + 1);
      int    caretPosition = document.getLineStartOffset(selectionStartLine + 1) + indent.length() + subsequentLineIndent.length() + 2;
      editor.getCaretModel().moveToOffset(caretPosition);

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected StringBuilder getReplacementText(String indent, String commentChar, String prefix, String suffix, String subsequentLineIndent)
   {
      int prefixLength = 0;

      StringBuilder rs = new StringBuilder(indent);
      if(prefix != null)
      {
         rs.append(prefix);
         prefixLength = prefix.length();
      }
      rs.append(commentChar.repeat(76 - prefixLength));
      rs.append("\n");

      rs.append(indent);
      rs.append(Objects.requireNonNullElse(subsequentLineIndent, ""));
      rs.append(commentChar);
      rs.append(" ");
      rs.append("\n");

      int suffixLength = suffix == null ? 0 : suffix.length();
      rs.append(indent);
      rs.append(Objects.requireNonNullElse(subsequentLineIndent, ""));
      rs.append(commentChar.repeat(76 - suffixLength));

      if(suffix != null)
      {
         rs.append(suffix);
      }

      rs.append("\n");

      return (rs);
   }

}

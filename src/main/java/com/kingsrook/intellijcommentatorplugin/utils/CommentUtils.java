/*
 * Kingsrook IntelliJ Commentator Plugin
 * Copyright (C) 2024.  Kingsrook, LLC
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

package com.kingsrook.intellijcommentatorplugin.utils;


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class CommentUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getCommentChar(Document document)
   {
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
      return commentChar;
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static int getSelectionEndLine(SelectionModel selectionModel, Document document, String commentChar)
   {
      int selectionEndOffset   = selectionModel.getSelectionEnd();
      int selectionEndLine   = document.getLineNumber(selectionEndOffset);
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
      return selectionEndLine;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static int getSelectionStartLine(SelectionModel selectionModel, Document document, String commentChar)
   {
      int selectionStartOffset = selectionModel.getSelectionStart();
      int selectionStartLine = document.getLineNumber(selectionStartOffset);

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
      return selectionStartLine;
   }


}

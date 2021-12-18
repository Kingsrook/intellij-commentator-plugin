package com.kingsrook.commentator;


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
 ** todo - other languages / comment chars
 ** todo - handle annotatios, if we search whole file
 **
 *******************************************************************************/
public class CreateHeaderCommentAction extends AnAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public CreateHeaderCommentAction()
   {
      super("Create Header Comment");
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
      final Editor editor = event.getData(CommonDataKeys.EDITOR);

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
      Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
      Project project = event.getProject();

      ///////////////////////////////////////////
      // Access document, caret, and selection //
      ///////////////////////////////////////////
      Document document = editor.getDocument();
      SelectionModel selectionModel = editor.getSelectionModel();

      ////////////////////////////////////////////////////////////////////////
      // find the line where user has cursor (or, start of their selection) //
      ////////////////////////////////////////////////////////////////////////
      int selectionStartOffset = selectionModel.getSelectionStart();
      int selectionStartLine = document.getLineNumber(selectionStartOffset);

      //////////////////////////////////////////
      // get the range of text being replaced //
      //////////////////////////////////////////
      int lineStartOffset = document.getLineStartOffset(selectionStartLine);
      int lineEndOffset = document.getLineEndOffset(selectionStartLine);
      TextRange textRange = new TextRange(lineStartOffset, lineEndOffset);
      String selectedLine = document.getText(textRange);

      boolean needTrailingNewline = true;
      if(selectedLine.trim().equals(""))
      {
         needTrailingNewline = false;
      }

      // todo, make smart
      String indentString = "   ";

      String replacement = indentString + "/***************************************************************************\n"
         + indentString + " ** \n"
         + indentString + " ** \n"
         + indentString + " **************************************************************************/" + (needTrailingNewline ? "\n" : "");

      //////////////////////////
      // make the replacement //
      //////////////////////////
      WriteCommandAction.runWriteCommandAction(project, () ->
         {
            document.insertString(lineStartOffset, replacement);
         }
      );

      /////////////////////////////////
      // un-select what was selected //
      /////////////////////////////////
      selectionModel.removeSelection();
   }

}

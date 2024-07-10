package com.kingsrook.intellijcommentatorplugin;


import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractKRCommentatorEditorAction extends AnAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractKRCommentatorEditorAction(String title)
   {
      super(title);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public @NotNull ActionUpdateThread getActionUpdateThread()
   {
      return ActionUpdateThread.BGT;
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
      event.getPresentation().setEnabled(project != null && editor != null);
   }
}

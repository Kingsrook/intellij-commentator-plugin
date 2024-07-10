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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;


/*******************************************************************************
 **
 *******************************************************************************/
public class StubSetterCallsAction extends AbstractKRCommentatorEditorAction
{
   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public StubSetterCallsAction()
   {
      super("StubSetterCalls");
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
         //////////////////////////////////////////
         int startOffset = document.getLineStartOffset(selectionStartLine);
         int endOffset   = document.getLineEndOffset(selectionEndLine);

         String line = document.getText(new TextRange(document.getLineStartOffset(selectionEndLine), document.getLineEndOffset(selectionEndLine)));
         String leadingWhitespace = line.replaceAll("\\S.*", "");

         int     caretOffset = editor.getCaretModel().getOffset();
         PsiFile psiFile     = event.getData(CommonDataKeys.PSI_FILE);
         if(psiFile == null)
         {
            System.out.println("No PSI file...");
            return;
         }

         PsiElement psiElement = psiFile.findElementAt(caretOffset);
         if(psiElement == null)
         {
            System.out.println("No PSI element...");
            return;
         }

         if(psiElement instanceof PsiIdentifier)
         {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            String        className     = javaPsiFacade.getElementFactory().createExpressionFromText(psiElement.getText(), psiElement).getType().getCanonicalText();
            PsiClass      varClass      = javaPsiFacade.findClass(className, GlobalSearchScope.allScope(project));

            StringBuilder appendText = new StringBuilder("\n");
            for(PsiMethod method : varClass.getMethods())
            {
               if(method.getName().startsWith("set"))
               {
                  appendText.append(leadingWhitespace).append(psiElement.getText()).append(".").append(method.getName()).append("();\n");
               }
            }

            ///////////////////////////////////////////////////////////////////////
            // build the replacement text, feeding it the comment-lines as input //
            ///////////////////////////////////////////////////////////////////////
            // String selectedLinesText = document.getText(textRange);
            // String replacementText   = getReplacementText(selectedLinesText);

            //////////////////////////
            // make the replacement //
            //////////////////////////
            // WriteCommandAction.runWriteCommandAction(project, () ->
            //    document.replaceString(startOffset, endOffset, replacementText)
            // );

            WriteCommandAction.runWriteCommandAction(project, () ->
            {
               document.replaceString(endOffset, endOffset, appendText);
            });

            /////////////////////////////////
            // un-select what was selected //
            /////////////////////////////////
            selectionModel.removeSelection();
         }
         else
         {
            System.out.println("Not an identifier...");
            return;
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getReplacementText(String selectedLinesText)
   {
      System.out.println(selectedLinesText);
      return (selectedLinesText);
   }
}

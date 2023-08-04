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
import java.util.concurrent.CompletableFuture;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.tuple.Pair;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetSetWithAction extends AnAction
{
   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public GetSetWithAction()
   {
      super("GetSetWith");
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

         // editor.getDocument().get

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
         String selectedLinesText = document.getText(textRange);

         PsiElement  element      = (PsiElement) event.getDataContext().getData("psi.Element");
         PsiJavaFile parentOfType = PsiTreeUtil.getParentOfType(element, PsiJavaFile.class);

         // JavaRecursiveElementWalkingVisitor visitor = new J

         Document    currentDoc  = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
         VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
         String      className   = currentFile.getNameWithoutExtension();

         String[]      selectedLines    = selectedLinesText.split("\n");
         StringBuilder newMethodBodies  = new StringBuilder();
         StringBuilder newMethodNames   = new StringBuilder();
         int           newMethodCounter = 0;
         for(String selectedLine : selectedLines)
         {
            System.out.println(selectedLine);
            Pair<String, String> typeNamePair = parseTypeAndNameFromDeclarationLine(selectedLine);
            if(typeNamePair == null)
            {
               System.out.println("Did not find a typeNamePair in selected line");
               continue;
            }

            String  type        = typeNamePair.getKey();
            String  name        = typeNamePair.getValue();
            String  nameUcFirst = ucFirst(name);
            boolean isStatic    = isStatic(selectedLine);

            if(needMethod(document, "get" + nameUcFirst))
            {
               newMethodBodies.append(writeGetter(isStatic, type, name, className));
               newMethodNames.append("get").append(nameUcFirst).append("\n");
               newMethodCounter++;
            }

            if(!isFinal(selectedLine))
            {
               if(needMethod(document, "set" + nameUcFirst))
               {
                  newMethodBodies.append(writeSetter(isStatic, type, name, className));
                  newMethodNames.append("set").append(nameUcFirst).append("\n");
                  newMethodCounter++;
               }

               if(needMethod(document, "with" + nameUcFirst))
               {
                  newMethodBodies.append(writeWither(isStatic, type, name, className));
                  newMethodNames.append("with").append(nameUcFirst).append("\n");
                  newMethodCounter++;
               }
            }
         }

         String notificationBody = "There were no GSW's to write...";
         if(newMethodCounter > 0)
         {
            int position = getJustBeforeClassClosePosition(document);
            WriteCommandAction.runWriteCommandAction(project, () ->
               document.replaceString(position, position, newMethodBodies.toString())
            );

            /////////////////////////////////
            // un-select what was selected //
            /////////////////////////////////
            selectionModel.removeSelection();

            notificationBody = "I wrote the following " + newMethodCounter + " GSW method" + (newMethodCounter == 1 ? "" : "s") + ":<br />" + newMethodNames;
         }

         Notification notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Kingsrook Commentator Notifications")
            .createNotification(notificationBody, NotificationType.INFORMATION);
         notification.notify(project);

         CompletableFuture.supplyAsync(() ->
         {
            try
            {
               Thread.sleep(2500);
            }
            catch(InterruptedException e)
            {
               // ok...
               System.out.println("interrupted");
            }
            notification.hideBalloon();
            return (true);
         });
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean isFinal(String line)
   {
      return (tokenizeLine(line).contains("final"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean isStatic(String line)
   {
      return (tokenizeLine(line).contains("static"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int getJustBeforeClassClosePosition(Document document)
   {
      for(int lineNo = document.getLineCount() - 1; lineNo >= 1; lineNo--)
      {
         String lineText = document.getText(new TextRange(document.getLineStartOffset(lineNo), document.getLineEndOffset(lineNo)));
         if(lineText.trim().equals("}"))
         {
            return (document.getLineEndOffset(lineNo - 1));
         }
      }

      return (-1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void appendMethod(Project project, Document document, int position, String fullMethod)
   {
      WriteCommandAction.runWriteCommandAction(project, () ->
         document.replaceString(position, position, fullMethod)
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String writeGetter(boolean isStatic, String fieldType, String fieldName, String className)
   {
      return "\n"
         + "\n"
         + "   /*******************************************************************************\n"
         + "    ** Getter for " + fieldName + "\n"
         + "    *******************************************************************************/\n"
         + "   public " + fieldType + " get" + ucFirst(fieldName) + "()\n"
         + "   {\n"
         + "      return (" + (isStatic ? className : "this") + "." + fieldName + ");\n"
         + "   }\n"
         + "\n";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String writeSetter(boolean isStatic, String fieldType, String fieldName, String className)
   {
      return "\n"
         + "\n"
         + "   /*******************************************************************************\n"
         + "    ** Setter for " + fieldName + "\n"
         + "    *******************************************************************************/\n"
         + "   public void set" + ucFirst(fieldName) + "(" + fieldType + " " + fieldName + ")\n"
         + "   {\n"
         + "      " + (isStatic ? className : "this") + "." + fieldName + " = " + fieldName + ";\n"
         + "   }\n"
         + "\n";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String writeWither(boolean isStatic, String fieldType, String fieldName, String className)
   {
      return "\n"
         + "\n"
         + "   /*******************************************************************************\n"
         + "    ** Fluent setter for " + fieldName + "\n"
         + "    *******************************************************************************/\n"
         + "   public " + (isStatic ? "void" : className) + " with" + ucFirst(fieldName) + "(" + fieldType + " " + fieldName + ")\n"
         + "   {\n"
         + "      " + (isStatic ? className : "this") + "." + fieldName + " = " + fieldName + ";\n"
         + (isStatic ? ""
         : "      return (this);\n")
         + "   }\n"
         + "\n";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean needMethod(Document document, String methodName)
   {
      if(document.getText().matches("(?s).*public [\\w<, >\\[\\]]+ " + methodName + ".*"))
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String ucFirst(String name)
   {
      if(name == null)
      {
         return (null);
      }
      if(name.length() < 2)
      {
         return (name.toUpperCase());
      }
      return (name.substring(0, 1).toUpperCase() + name.substring(1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Pair<String, String> parseTypeAndNameFromDeclarationLine(String line)
   {
      List<String> tokens = tokenizeLine(line);
      if(tokens.size() > 1)
      {
         Pair<String, String> pair = Pair.of(tokens.get(tokens.size() - 2), tokens.get(tokens.size() - 1));

         /////////////////////////////////////////////////////////////////
         // try to make sure each element looks like we expect it to... //
         /////////////////////////////////////////////////////////////////
         if(!pair.getValue().matches("[a-zA-Z_$][a-zA-Z0-9_$]*"))
         {
            System.out.format("But [%s] doesn't look like a variable name, so failing.\n", pair.getValue());
            return (null);
         }

         if(!pair.getKey().matches("[a-zA-Z0-9_$, .<>\\[\\]]+"))
         {
            System.out.format("But [%s] doesn't look like a type name, so failing.\n", pair.getKey());
            return (null);
         }

         if(pair.getKey().matches("(class|enum|interface)"))
         {
            System.out.format("But [%s] is a keyword, not a type name, so failing.\n", pair.getKey());
            return (null);
         }

         return (pair);
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<String> tokenizeLine(String line)
   {
      String originalLine = line;
      line = line.replaceAll("//.*", "");
      line = line.replaceAll("\\*\\*.*", "");
      line = line.replaceAll("[=;].*", "");
      line = line.replaceAll(" +", " ");
      line = line.trim();

      List<String>  tokens              = new ArrayList<>();
      StringBuilder currentToken        = new StringBuilder();
      boolean       insideAngleBrackets = false;
      for(char c : line.toCharArray())
      {
         if(c == '<')
         {
            insideAngleBrackets = true;
            currentToken.append(c);
         }
         else if(c == '>' && insideAngleBrackets)
         {
            insideAngleBrackets = false;
            currentToken.append(c);
         }
         else if(c == ' ' && insideAngleBrackets)
         {
            currentToken.append(c);
         }
         else if(c == ' ')
         {
            tokens.add(currentToken.toString());
            currentToken = new StringBuilder();
         }
         else
         {
            currentToken.append(c);
         }
      }

      if(currentToken.length() > 0)
      {
         tokens.add(currentToken.toString());
      }

      System.out.format("Parsed [%s] into [%s]\n", originalLine, tokens);
      return tokens;
   }

}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.apache.commons.lang3.tuple.Pair;


/*******************************************************************************
 **
 *******************************************************************************/
public class MyImporterAction extends AbstractKRCommentatorEditorAction
{
   private static Map<String, String> importDefinitions = new HashMap<>();

   static
   {
      for(String junitAssertion : new String[] {
         "assertEquals",
         "assertNotEquals",
         "assertSame",
         "assertNotSame",
         "assertTrue",
         "assertFalse",
         "assertNull",
         "assertNotNull",
         "fail"
      })
      {
         importDefinitions.put(junitAssertion, "static org.junit.jupiter.api.Assertions." + junitAssertion);
      }

      importDefinitions.put("assertThat", "static org.assertj.core.api.Assertions.assertThat");
      importDefinitions.put("assertThatThrownBy", "static org.assertj.core.api.Assertions.assertThatThrownBy");
      importDefinitions.put("File", "java.io.File");
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public MyImporterAction()
   {
      super("MyImporter");
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

         //////////////////////////////////////
         // find imports already in the file //
         //////////////////////////////////////
         String      documentText    = document.getText();
         Set<String> existingImports = new HashSet<>();

         int lineNo             = 1;
         int lastImportPosition = 1;
         for(String line : documentText.split("\n"))
         {
            if(line.matches("^ *import .*"))
            {
               existingImports.add(line.replaceFirst("^ *import +", "").replaceFirst(";.*", ""));
               lastImportPosition = document.getLineEndOffset(lineNo);
            }
            lineNo++;
         }

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

         Set<String> addedImports      = new LinkedHashSet<>();
         String      selectedLinesText = document.getText(textRange);
         String[]    words             = selectedLinesText.split("\\b");
         for(String word : words)
         {
            if(importDefinitions.containsKey(word) && !existingImports.contains(importDefinitions.get(word)))
            {
               int    finalLastImportPosition = lastImportPosition;
               String newLine                 = "import " + importDefinitions.get(word) + ";\n";
               WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(finalLastImportPosition, finalLastImportPosition, newLine));
               lastImportPosition += newLine.length();
               existingImports.add(importDefinitions.get(word));
               addedImports.add(word);
            }
         }

         Notification notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Kingsrook Commentator Notifications")
            .createNotification(addedImports.isEmpty() ? "I couldn't find anything to import" : "I added imports for: " + addedImports, NotificationType.INFORMATION);
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

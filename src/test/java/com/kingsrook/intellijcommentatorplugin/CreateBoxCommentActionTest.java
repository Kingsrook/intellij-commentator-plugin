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


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for CreateBoxCommentAction.
 *******************************************************************************/
class CreateBoxCommentActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGetReplacementTextSingleLine()
   {
      String expected = "//////////\n" +
         "// Test //\n" +
         "//////////";

      assertEquals(expected, getReplacementText("Test"));
      assertEquals(expected, getReplacementText("// Test"));
      assertEquals(expected, getReplacementText("// Test //"));
      assertEquals(expected, getReplacementText("// Test   "));
      assertEquals(expected, getReplacementText("// Test   //"));
      assertEquals(expected, getReplacementText("/ Test   //"));
      assertEquals(expected, getReplacementText("///// Test"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGetReplacementTextSingleLineIndented()
   {
      String expected = "   //////////\n" +
         "   // Test //\n" +
         "   //////////";

      assertEquals(expected, getReplacementText("   Test   "));
      assertEquals(expected, getReplacementText("   // Test   "));
      assertEquals(expected, getReplacementText("   // Test    //  "));
      assertEquals(expected, getReplacementText("   / Test    /  "));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGetReplacementTextMultiLine()
   {
      String expected = "/////////////\n" +
         "// Test    //\n" +
         "// In Here //\n" +
         "/////////////";

      assertEquals(expected, getReplacementText("Test\n" +
         "In Here"));

      assertEquals(expected, getReplacementText("// Test\n" +
         "In Here"));

      assertEquals(expected, getReplacementText("// Test\n" +
         "In Here       "));

      assertEquals(expected, getReplacementText("/////////////\n" +
         "// Test    //\n" +
         "// In Here //\n" +
         "/////////////"));

      assertEquals(expected, getReplacementText("/////////////\n" +
         "// Test //\n" +
         "// In Here //\n" +
         "/////////////"));

      assertEquals(expected, getReplacementText("// Test //\n" +
         "// In Here"));

      expected = "////////////////\n" +
         "// Test       //\n" +
         "//    In Here //\n" +
         "////////////////";

      ////////////////////////////////////////////////////
      // preserve indents on lines after the first line //
      ////////////////////////////////////////////////////
      assertEquals(expected, getReplacementText("Test\n" +
         "   In Here"));
   }



   /*******************************************************************************
    ** Run the getReplacementText method on the given input
    *******************************************************************************/
   private String getReplacementText(String input)
   {
      CreateBoxCommentAction action = new CreateBoxCommentAction();
      String                 output = action.getReplacementText(input).toString();

      System.out.printf("Input:\n%s\nOutput:\n%s\n", input, output);

      return (output);
   }

}
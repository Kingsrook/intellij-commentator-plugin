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
import com.kingsrook.intellijcommentatorplugin.CaseChangerAction.Transform;
import org.junit.jupiter.api.Test;
import static com.kingsrook.intellijcommentatorplugin.CaseChangerAction.getReplacementText;
import static com.kingsrook.intellijcommentatorplugin.CaseChangerAction.toSubWords;
import static com.kingsrook.intellijcommentatorplugin.CaseChangerAction.toWords;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for CreateBoxCommentAction.
 *******************************************************************************/
class CaseChangerActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testToWords()
   {
      assertEquals(List.of("foo_bar"), toWords("foo_bar"));
      assertEquals(List.of("foo", " ", "bar"), toWords("foo bar"));
      assertEquals(List.of("foo", "\n", "bar"), toWords("foo\nbar"));
      assertEquals(List.of(" ", "foo", "\n   ", "bar", " "), toWords(" foo\n   bar "));
      assertEquals(List.of("one-two"), toWords("one-two"));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testToSubWords()
   {
      assertEquals(List.of("foo", "bar"), toSubWords("foo_bar"));
      assertEquals(List.of("foo", "Bar"), toSubWords("fooBar"));
      assertEquals(List.of("Foo", "Bar"), toSubWords("FooBar"));
      assertEquals(List.of("foo", "bar"), toSubWords("foo-bar"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReplacementText()
   {
      assertEquals("foo_bar", getReplacementText("fooBar", Transform.LOWER_CASE_UNDERSCORES));
      assertEquals("FooBar", getReplacementText("fooBar", Transform.PASCAL_CASE));
      assertEquals("fooBar", getReplacementText("foo_bar", Transform.CAMEL_CASE));
      assertEquals("FooBar FizBin", getReplacementText("fooBar fizBin", Transform.PASCAL_CASE));
      assertEquals("foo-bar", getReplacementText("foo-bar", Transform.CAMEL_CASE));
   }

}
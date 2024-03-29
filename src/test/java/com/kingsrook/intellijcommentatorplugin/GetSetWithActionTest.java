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


import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for com.kingsrook.intellijcommentatorplugin.GetSetWithAction
 *******************************************************************************/
class GetSetWithActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testParseTypeAndNameFromDeclarationLine()
   {
      assertEquals(Pair.of("String", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private String foo;"));
      assertEquals(Pair.of("String", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private String foo = \"hello\";"));
      assertEquals(Pair.of("String[]", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private String[] foo = {};"));
      assertEquals(Pair.of("List<String>", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private List<String> foo = new ArrayList<>();"));
      assertEquals(Pair.of("Map<String, Integer>", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private Map<String, Integer> foo = new HashMap<>();"));
      assertEquals(Pair.of("Map<String, List<Integer>>", "foo"), GetSetWithAction.parseTypeAndNameFromDeclarationLine("   private Map<String, List<Integer>> foo = new HashMap<>();"));
      assertNull(GetSetWithAction.parseTypeAndNameFromDeclarationLine("   // comment line"));
      assertNull(GetSetWithAction.parseTypeAndNameFromDeclarationLine("   public Constructor()"));
   }

}
/*
 * Kingsrook IntelliJ Commentator Plugin
 * Copyright (C) 2023.  Kingsrook, LLC
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
 ** Unit test for URIDecodeAction
 *******************************************************************************/
class URIDecodeActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      URIDecodeAction uriDecodeAction = new URIDecodeAction();
      StringBuilder   replacementText = uriDecodeAction.getReplacementText("%7B%22criteria%22:%5B%7B%22fieldName%22");
      assertEquals("{\"criteria\":[{\"fieldName\"", replacementText.toString());
   }

}
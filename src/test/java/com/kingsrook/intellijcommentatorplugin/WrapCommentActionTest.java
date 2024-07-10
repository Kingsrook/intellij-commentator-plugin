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
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for CreateBoxCommentAction.
 *******************************************************************************/
class WrapCommentActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test()
   {
      assertEquals("""
            ////////////////
            // one two    //
            // three four //
            // five six   //
            // seven      //
            // eight nine //
            // ten        //
            ////////////////""",
         doReplacementNoWrap("""
            /////////////////////////////////
            // one two three four five six //
            // seven eight nine ten        //
            /////////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testWordLongerThanMaxLineLength()
   {
      assertEquals("""
            /////////////////
            // one two     //
            // three       //
            // fourfivesix //
            // seven       //
            // eight nine  //
            // ten         //
            /////////////////""",
         doReplacementNoWrap("""
            ///////////////////////////////
            // one two three fourfivesix //
            // seven eight nine ten      //
            ///////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDoubleSpaceAfterPeriodIsPreserved()
   {
      assertEquals("""
            ////////////////
            // one two    //
            // three four //
            // five.  six //
            // seven      //
            // eight nine //
            // ten        //
            ////////////////""",
         doReplacementNoWrap("""
            ///////////////////////////////////
            // one two three four five.  six seven eight nine ten      //
            ///////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDoubleSpaceAfterPeriodIsLostAtEndOfWrappedLines()
   {
      assertEquals("""
            ////////////////
            // one two    //
            // three four //
            // five siix. //
            // seven      //
            // eight nine //
            // ten        //
            ////////////////""",
         doReplacementNoWrap("""
            ///////////////////////////////////
            // one two three four five siix.  seven eight nine ten      //
            ///////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testLeadingSpacesAtStartOfLineArePreserved()
   {
      assertEquals("""
            ////////////////
            // one two    //
            // three four //
            //  - five    //
            //  - six     //
            ////////////////""",
         doReplacementNoWrap("""
            ///////////////////////////////////
            // one two three four
            //  - five
            //  - six
            ///////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMultipleSpacesAfterWordNotLostToShorterNextWord()
   {
      assertEquals("""
            ///////////////
            // one.      //
            // two three //
            // four      //
            ///////////////""",
         doReplacementNoWrap("""
            ///////////////////////////////////
            // one.     two //
            // three four //
            ///////////////////////////////""", 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testWithNestedBulletListsAndDoubleSpaceAfterPeriod()
   {
      assertEquals("""
            //////////////////////////////////////////////////////////////////////////////
            // do a thing this is a comment that is much longer so long in fact i think //
            // that needs wrapped onto more lines                                       //
            //  - but what about this?                                                  //
            //    - and that?                                                           //
            //    - and other things? this is all really important so i should probably //
            // say more words keep typing.   Seriously.  I mean it.  And I like         //
            // double-spaces after periods.  for real...                                //
            //////////////////////////////////////////////////////////////////////////////""",
         doReplacementNoWrap("""
            //////////////////////////////////////////////////////////////////////////////
            // do a thing this is a comment that is much longer so long in fact i think that needs wrapped onto more lines                                       //
            //  - but what about this?
            //    - and that?
            //    - and other things? this is all really important so i should probably
            // say more words keep typing.   Seriously.  I mean it.  And I like double-spaces after periods.  for real...
            //////////////////////////////////////////////////////////////////////////////
            """, 74));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testSentenceEndingPunctuationAtEOL()
   {
      assertEquals("""
            ///////////////////////////////////////////////
            // This is a sentence that ends in a period. //
            // this should stay on a second line.        //
            ///////////////////////////////////////////////""",
         doReplacementNoWrap("""
            //////////////////////////////////////////////////////////////////////////////
            // This is a sentence that ends in a period.                                //
            // this should stay on a second line.        //
            //////////////////////////////////////////////////////////////////////////////
            """, 74));

      assertEquals("""
            ////////////////////////////////////////////////////////////////////////////////
            // This is a sentence that ends in a period.                                  //
            // here are long words that should wrap and here are long words that should   //
            // wrap and here are long words that should wrap and here are long words that //
            // should wrap.                                                               //
            ////////////////////////////////////////////////////////////////////////////////""",
         doReplacementNoWrap("""
            //////////////////////////////////////////////////////////////////////////////
            // This is a sentence that ends in a period.                                //
            // here are long words that should wrap and here are long words that should wrap and here are long words that should wrap and here are long words that should wrap.                                                        //
            //////////////////////////////////////////////////////////////////////////////
            """, 74));
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testRewrap()
   {
      assertEquals("""
            ///////////////////////////////////////////////////////////////
            // this is a comment.  it has some sentences in it.  some of //
            // those are long, and some are short, or long, who knows?   //
            ///////////////////////////////////////////////////////////////""",
         doReplacementWithWrap("""
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // this is a comment.  it has some sentences in it.  some of those are long, and some are short, //
            // or long, who knows?                                                                           //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            """, 74));
   }



   /*******************************************************************************
    ** Run the getReplacementText method on the given input, with the DO_NOT_REWRAP option
    *******************************************************************************/
   private String doReplacementNoWrap(String input, int maxLength)
   {
      return doReplacement(maxLength, input, WrapCommentAction.Transform.DO_NOT_REWRAP);
   }



   /*******************************************************************************
    ** Run the getReplacementText method on the given input, with the DO_REWRAP option
    *******************************************************************************/
   private String doReplacementWithWrap(String input, int maxLength)
   {
      return doReplacement(maxLength, input, WrapCommentAction.Transform.DO_REWRAP);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String doReplacement(int maxLength, String input, WrapCommentAction.Transform doNotRewrap)
   {
      WrapCommentAction action = new WrapCommentAction();
      String output = action.getReplacementText(input, "/", doNotRewrap, maxLength).toString();

      System.out.printf("Input:\n%s\nOutput:\n%s\n", input, output);

      return (output);
   }

}
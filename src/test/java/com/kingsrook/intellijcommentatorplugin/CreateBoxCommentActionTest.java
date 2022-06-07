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
   public void testGetReplacementText()
   {
      String expected = """
         //////////
         // Test //
         //////////""";

      assertEquals(expected, getReplacementText("Test"));
      assertEquals(expected, getReplacementText("// Test"));
      assertEquals(expected, getReplacementText("// Test //"));
      assertEquals(expected, getReplacementText("// Test   "));
      assertEquals(expected, getReplacementText("// Test   //"));
      assertEquals(expected, getReplacementText("/ Test   //"));
      assertEquals(expected, getReplacementText("///// Test"));

      expected = """
         .  //////////
            // Test //
            //////////""";

      assertEquals(expected.replace('.', ' '), getReplacementText("   Test   "));
      assertEquals(expected.replace('.', ' '), getReplacementText("   // Test   "));
      assertEquals(expected.replace('.', ' '), getReplacementText("   // Test    //  "));
      assertEquals(expected.replace('.', ' '), getReplacementText("   / Test    /  "));
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
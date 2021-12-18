package com.kingsrook.commentator;


import java.util.LinkedList;
import java.util.List;


/*******************************************************************************
 **
 *******************************************************************************/
public class CreateWrappedBoxCommentAction extends CreateBoxCommentAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public CreateWrappedBoxCommentAction()
   {
      super("Create Wrapped Box Comment");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected List<String> manipulateLines(StringBuilder indentString, List<String> replacementLines)
   {
      /////////////////////////////////////////
      // put all the lines together into one //
      /////////////////////////////////////////
      StringBuilder fullText = new StringBuilder();
      for(String line : replacementLines)
      {
         fullText.append(fullText.length() == 0 ? "" : " ").append(line.substring(indentString.length()));
      }

      ///////////////////////
      // now split them up //
      ///////////////////////
      List<String> rs = new LinkedList<>();
      String[] words = fullText.toString().split(" ");
      StringBuilder currentLine = new StringBuilder();
      for(String word : words)
      {
         //////////////////////////////////////////////////
         // this the next word fits on this line, add it //
         //////////////////////////////////////////////////
         if(currentLine.length() + word.length() < 74)
         {
            currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
         }
         else
         {
            ////////////////////////////
            // else, start a new line //
            ////////////////////////////
            if(currentLine.length() > 0)
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////
               // if there's something on the current line, add it to the rs and actually start a new line  //
               // (so, this will handle if first word we find is too long, it will just go on this line)    //
               ///////////////////////////////////////////////////////////////////////////////////////////////
               rs.add(indentString + currentLine.toString());
               currentLine = new StringBuilder();
            }

            currentLine.append(word);
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // if there are any words on the current line (won't there always be?) add them too //
      //////////////////////////////////////////////////////////////////////////////////////
      if(currentLine.length() > 0)
      {
         rs.add(indentString + currentLine.toString());
      }

      return (rs);
   }

}

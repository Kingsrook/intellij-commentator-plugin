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


import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;


/*******************************************************************************
 **
 *******************************************************************************/
public class SSDialogWrapper extends DialogWrapper
{
   private final String                         initialFindText;
   private final String                         initialReplaceText;
   private final List<SSAction.FindReplacePair> history;

   private String find;
   private String replace;

   private JTextField                         findText;
   private JTextField                         replaceText;
   private ComboBox<SSAction.FindReplacePair> historyCombo;



   /*******************************************************************************
    **
    *******************************************************************************/
   public SSDialogWrapper(String initialFindText, String initialReplaceText, List<SSAction.FindReplacePair> history)
   {
      super(true);

      this.initialFindText = initialFindText;
      this.initialReplaceText = initialReplaceText;
      this.history = history;

      setTitle("SmartSubstitute");
      setSize(350, 190); // todo - was 155
      init();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public @Nullable JComponent getPreferredFocusedComponent()
   {
      return findText;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected JComponent createCenterPanel()
   {
      JPanel dialogPanel = new JPanel();

      ////////////////////////
      // find label & field //
      ////////////////////////
      JLabel findLabel = new JLabel("Find:");
      findLabel.setPreferredSize(new Dimension(60, 30));
      dialogPanel.add(findLabel);

      findText = new JTextField();
      findText.setPreferredSize(new Dimension(255, 30));
      findText.addFocusListener(new SelectOnFocus(findText));
      if(initialFindText != null)
      {
         findText.setText(initialFindText);
      }
      dialogPanel.add(findText);

      ///////////////////////////
      // replace label & field //
      ///////////////////////////
      JLabel replaceLabel = new JLabel("Replace:");
      replaceLabel.setPreferredSize(findLabel.getPreferredSize());
      dialogPanel.add(replaceLabel);

      replaceText = new JTextField();
      replaceText.setPreferredSize(findText.getPreferredSize());
      replaceText.addFocusListener(new SelectOnFocus(replaceText));
      if(initialReplaceText != null)
      {
         replaceText.setText(initialReplaceText);
      }
      dialogPanel.add(replaceText);

      ///////////////////////////
      // history label & field //
      ///////////////////////////
      JLabel historyLabel = new JLabel("History:");
      historyLabel.setPreferredSize(findLabel.getPreferredSize());
      dialogPanel.add(historyLabel);

      historyCombo = new ComboBox<>(history.stream().toArray(SSAction.FindReplacePair[]::new));
      if(history.size() > 0)
      {
         historyCombo.setItem(history.get(history.size() - 1));
      }
      historyCombo.setPreferredSize(findText.getPreferredSize());
      historyCombo.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            SSAction.FindReplacePair item = historyCombo.getItem();
            if(item != null)
            {
               findText.setText(item.find);
               replaceText.setText(item.replace);
            }
         }
      });
      dialogPanel.add(historyCombo);

      SpringLayout layout = new SpringLayout();
      dialogPanel.setLayout(layout);
      int padding = 6;

      /////////////////////////////
      // place find label & text //
      /////////////////////////////
      layout.putConstraint(SpringLayout.WEST, findLabel, padding, SpringLayout.WEST, dialogPanel);
      layout.putConstraint(SpringLayout.NORTH, findLabel, padding, SpringLayout.NORTH, dialogPanel);

      layout.putConstraint(SpringLayout.WEST, findText, padding, SpringLayout.EAST, findLabel);
      layout.putConstraint(SpringLayout.NORTH, findText, padding, SpringLayout.NORTH, dialogPanel);

      ////////////////////////////////
      // place replace label & text //
      ////////////////////////////////
      layout.putConstraint(SpringLayout.WEST, replaceLabel, padding, SpringLayout.WEST, dialogPanel);
      layout.putConstraint(SpringLayout.NORTH, replaceLabel, padding, SpringLayout.SOUTH, findLabel);

      layout.putConstraint(SpringLayout.WEST, replaceText, padding, SpringLayout.EAST, replaceLabel);
      layout.putConstraint(SpringLayout.NORTH, replaceText, padding, SpringLayout.SOUTH, findText);

      ////////////////////////////////
      // place history label & text //
      ////////////////////////////////
      layout.putConstraint(SpringLayout.WEST, historyLabel, padding, SpringLayout.WEST, dialogPanel);
      layout.putConstraint(SpringLayout.NORTH, historyLabel, padding, SpringLayout.SOUTH, replaceLabel);

      layout.putConstraint(SpringLayout.WEST, historyCombo, padding, SpringLayout.EAST, historyLabel);
      layout.putConstraint(SpringLayout.NORTH, historyCombo, padding, SpringLayout.SOUTH, replaceText);

      return (dialogPanel);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class SelectOnFocus implements FocusListener
   {
      private final JTextField field;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public SelectOnFocus(JTextField field)
      {
         this.field = field;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void focusGained(FocusEvent e)
      {
         field.selectAll();
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void focusLost(FocusEvent e)
      {

      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void show()
   {
      super.show();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected void doOKAction()
   {
      find = findText.getText();
      replace = replaceText.getText();
      super.doOKAction();
   }



   /*******************************************************************************
    ** Getter for find
    **
    *******************************************************************************/
   public String getFind()
   {
      return find;
   }



   /*******************************************************************************
    ** Getter for replace
    **
    *******************************************************************************/
   public String getReplace()
   {
      return replace;
   }
}

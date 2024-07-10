package com.kingsrook.intellijcommentatorplugin.settings;


import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;


/*******************************************************************************
 ** UI for Commentator settings
 *******************************************************************************/
public class CommentatorSettingsComponent
{
   private final JPanel      myMainPanel;
   private final JBTextField wrapCommentMaxWidthTextField = new JBTextField();
   private final JBTextField wrapCommentMinWidthTextField = new JBTextField();



   /*******************************************************************************
    **
    *******************************************************************************/
   public CommentatorSettingsComponent()
   {
      /* todo - this didn't work
      wrapCommentMaxWidthTextField.setInputVerifier(new InputVerifier()
      {
         @Override
         public boolean verify(JComponent input)
         {
            try
            {
               int value = Integer.parseInt(((JTextField) input).getText());
               return (value > 0 && value < 500);
            }
            catch(Exception e)
            {
               return (false);
            }
         }
      });
       */

      int indentSize = 20;
      myMainPanel = FormBuilder.createFormBuilder()

         .addComponent(new TitledSeparator("Wrap Comment"))
         .addComponent(FormBuilder.createFormBuilder()
            .setFormLeftIndent(indentSize)
            .addLabeledComponent(new JBLabel("Max length: "), wrapCommentMaxWidthTextField, 1, false)
            .addLabeledComponent(new JBLabel("Min length: "), wrapCommentMinWidthTextField, 1, false)
            .getPanel())

         .addComponent(new TitledSeparator("MyImporter"))
         .addComponent(FormBuilder.createFormBuilder()
            .setFormLeftIndent(indentSize)
            .addComponentFillVertically(createComponent(), 0)
            .getPanel())

         .addComponentFillVertically(new JPanel(), 0)
         .getPanel();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public JPanel getPanel()
   {
      return myMainPanel;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public JComponent getPreferredFocusedComponent()
   {
      return wrapCommentMaxWidthTextField;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getWrapCommentMaxWidthText()
   {
      return wrapCommentMaxWidthTextField.getText();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setWrapCommentMaxWidthText(String newText)
   {
      wrapCommentMaxWidthTextField.setText(newText);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getWrapCommentMinWidthText()
   {
      return wrapCommentMinWidthTextField.getText();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setWrapCommentMinWidthText(String newText)
   {
      wrapCommentMinWidthTextField.setText(newText);
   }



   private JPanel                           myMainComponent;
   private MyImporterAddEditDeleteListPanel importerPanel;



   public JComponent createComponent()
   {
      if(myMainComponent == null)
      {
         myMainComponent = new JPanel(new BorderLayout());
         /*
         myCbUseSoftWrapsAtConsole = new JCheckBox(ApplicationBundle.message("checkbox.use.soft.wraps.at.console"), false);
         myCommandsHistoryLimitField = new JTextField(4);
         myCbOverrideConsoleCycleBufferSize = new JCheckBox(ApplicationBundle.message("checkbox.override.console.cycle.buffer.size", String.valueOf(ConsoleBuffer.getLegacyCycleBufferSize() / 1024)), false);
         myCbOverrideConsoleCycleBufferSize.addChangeListener(e -> {
            myConsoleCycleBufferSizeField.setEnabled(myCbOverrideConsoleCycleBufferSize.isSelected());
            myConsoleBufferSizeWarningLabel.setVisible(myCbOverrideConsoleCycleBufferSize.isSelected());
         });
         myConsoleCycleBufferSizeField = new JTextField(4);
         myConsoleBufferSizeWarningLabel = new JLabel();
         myConsoleBufferSizeWarningLabel.setForeground(JBColor.red);
         myConsoleCycleBufferSizeField.getDocument().addDocumentListener(new DocumentAdapter()
         {
            @Override
            protected void textChanged(@NotNull DocumentEvent e)
            {
               updateWarningLabel();
            }
         });
         myEncodingComboBox = new ConsoleEncodingComboBox();

         JPanel  northPanel = new JPanel(new GridBagLayout());
         GridBag gridBag    = new GridBag();
         gridBag.anchor(GridBagConstraints.WEST).setDefaultAnchor(GridBagConstraints.WEST);
         northPanel.add(myCbUseSoftWrapsAtConsole, gridBag.nextLine().next());
         northPanel.add(Box.createHorizontalGlue(), gridBag.next().coverLine());
         JLabel label = new JLabel(ApplicationBundle.message("editbox.console.history.limit"));
         label.setLabelFor(myCommandsHistoryLimitField);
         northPanel.add(label, gridBag.nextLine().next());
         northPanel.add(myCommandsHistoryLimitField, gridBag.next());
         if(ConsoleBuffer.useCycleBuffer())
         {
            northPanel.add(myCbOverrideConsoleCycleBufferSize, gridBag.nextLine().next());
            northPanel.add(myConsoleCycleBufferSizeField, gridBag.next());
            northPanel.add(new JLabel(ExecutionBundle.message("settings.console.kb")), gridBag.next());
            northPanel.add(Box.createHorizontalStrut(JBUIScale.scale(20)), gridBag.next());
            northPanel.add(myConsoleBufferSizeWarningLabel, gridBag.next());
         }
         northPanel.add(new JLabel(ApplicationBundle.message("combobox.console.default.encoding.label")), gridBag.nextLine().next());
         northPanel.add(myEncodingComboBox, gridBag.next().coverLine());
         if(!editFoldingsOnly())
         {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(northPanel, BorderLayout.WEST);
            myMainComponent.add(wrapper, BorderLayout.NORTH);
         }
         */

         Splitter splitter = new Splitter(true);
         myMainComponent.add(splitter, BorderLayout.CENTER);
         importerPanel = new MyImporterAddEditDeleteListPanel(ApplicationBundle.message("console.fold.console.lines"), ApplicationBundle.message("console.enter.substring.folded"));
         // myNegativePanel = new MyAddDeleteListPanel(ApplicationBundle.message("console.fold.exceptions"), ApplicationBundle.message("console.enter.substring.dont.fold"));
         splitter.setFirstComponent(importerPanel);
         // splitter.setSecondComponent(myNegativePanel);

         // myPositivePanel.getEmptyText().setText(ApplicationBundle.message("console.fold.nothing"));
         // myNegativePanel.getEmptyText().setText(ApplicationBundle.message("console.no.exceptions"));
      }

      return myMainComponent;
   }

}

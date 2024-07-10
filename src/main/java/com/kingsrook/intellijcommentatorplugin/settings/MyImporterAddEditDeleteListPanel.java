package com.kingsrook.intellijcommentatorplugin.settings;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import com.intellij.execution.ExecutionBundle;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.DialogWrapperPeer;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.MultiLineLabelUI;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import static com.intellij.openapi.ui.Messages.getCancelButton;
import static com.intellij.openapi.ui.Messages.getOkButton;
import static com.intellij.openapi.ui.Messages.showTwoStepConfirmationDialog;


/*******************************************************************************
 **
 *******************************************************************************/
class MyImporterAddEditDeleteListPanel extends AddEditDeleteListPanel<Pair<String, String>>
{
   private final String myQuery;



   /*******************************************************************************
    **
    *******************************************************************************/
   MyImporterAddEditDeleteListPanel(String title, String query)
   {
      super(title, new ArrayList<>());
      myQuery = query;
      // todo? ListSpeedSearch.installOn(myList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected @Nullable Pair<String, String> findItemToAdd()
   {
      return showEditDialog(new Pair<>("", ""));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private @Nullable Pair<String, String> showEditDialog(final Pair<String, String> initialValue)
   {
      InputValidatorEx inputValidatorEx = new InputValidatorEx()
      {
         @Override
         public @NlsContexts.DetailedDescription @Nullable String getErrorText(@NonNls String inputString)
         {
            return "Error?" ;
         }



         @Override
         public boolean checkInput(String inputString)
         {
            return !StringUtil.isEmpty(inputString);
         }



         @Override
         public boolean canClose(String inputString)
         {
            return !StringUtil.isEmpty(inputString);
         }

      };

      // String first  = Messages.showInputDialog(this, myQuery, ExecutionBundle.message("dialog.title.folding.pattern"), Messages.getQuestionIcon(), initialValue.getFirst(), inputValidatorEx);
      // String second = Messages.showInputDialog(this, myQuery, ExecutionBundle.message("dialog.title.folding.pattern"), Messages.getQuestionIcon(), initialValue.getSecond(), inputValidatorEx);

      Messages.InputDialog dialog = new MyImporterInputDialog(this, myQuery, "Import Pair", Messages.getQuestionIcon(), initialValue, inputValidatorEx);
      /*

      final JTextComponent field = dialog.getTextField();
      if (selection != null) {
         // set custom selection
         field.select(selection.getStartOffset(), selection.getEndOffset());
         field.putClientProperty(DialogWrapperPeer.HAVE_INITIAL_SELECTION, true);
      }
      */

      dialog.show();
      String first  = dialog.getInputString();
      String second = "NYditty" ;

      return new Pair<>(first, second);
   }



   public static class MyImporterInputDialog extends Messages.InputDialog
   {
      public static final int            INPUT_DIALOG_COLUMNS = 30;
      private             InputValidator myValidator; // todo!!

      // protected JTextComponent myField;
      protected JTextComponent field0;
      protected JTextComponent field1;
      private   String         myComment;



      public MyImporterInputDialog(@NotNull Component parent, @NlsContexts.DialogMessage String message, @NlsContexts.DialogTitle String title, @Nullable Icon icon, @Nullable Pair<String, String> initialValue, @Nullable InputValidator validator)
      {
         super(parent, message, title, icon, "uh", validator);
      }



      private void enableOkAction()
      {
         getOKAction().setEnabled(myValidator == null || myValidator.checkInput(field0.getText().trim()));
      }



      @Override
      protected Action @NotNull [] createActions()
      {
         final Action[] actions = new Action[myOptions.length];
         for(int i = 0; i < myOptions.length; i++)
         {
            String    option   = myOptions[i];
            final int exitCode = i;
            if(i == 0)
            { // "OK" is default button. It has index 0.
               actions[0] = getOKAction();
               actions[0].putValue(DialogWrapper.DEFAULT_ACTION, Boolean.TRUE);
               myField.getDocument().addDocumentListener(new DocumentAdapter()
               {
                  @Override
                  public void textChanged(@NotNull DocumentEvent event)
                  {
                     final String text = myField.getText().trim();
                     actions[exitCode].setEnabled(myValidator == null || myValidator.checkInput(text));
                     if(myValidator instanceof InputValidatorEx)
                     {
                        setErrorText(((InputValidatorEx) myValidator).getErrorText(text), myField);
                     }
                  }
               });
            }
            else
            {
               actions[i] = new AbstractAction(option)
               {
                  @Override
                  public void actionPerformed(ActionEvent e)
                  {
                     close(exitCode);
                  }
               };
            }
         }
         return actions;
      }



      @Override
      protected void doOKAction()
      {
         String inputString = myField.getText().trim();
         if(myValidator == null ||
            myValidator.checkInput(inputString) &&
               myValidator.canClose(inputString))
         {
            close(0);
         }
      }



      @Override
      protected JComponent createCenterPanel()
      {
         return null;
      }



      @Override
      protected JComponent createNorthPanel()
      {
         JPanel panel = createIconPanel();

         JPanel messagePanel = createMessagePanel();
         panel.add(messagePanel, BorderLayout.CENTER);

         if(myComment != null)
         {
            return UI.PanelFactory.panel(panel).withComment(myComment).createPanel();
         }
         else
         {
            return panel;
         }
      }



      @Override
      protected @NotNull JPanel createMessagePanel()
      {
         JPanel messagePanel = new JPanel(new BorderLayout());
         if(myMessage != null)
         {
            JComponent textComponent = createTextComponent();
            messagePanel.add(textComponent, BorderLayout.NORTH);
         }

         myField = createTextFieldComponent();
         messagePanel.add(createScrollableTextComponent(), BorderLayout.SOUTH);

         return messagePanel;
      }



      protected JComponent createScrollableTextComponent()
      {
         return myField;
      }



      protected JComponent createTextComponent()
      {
         JComponent textComponent;
         if(BasicHTML.isHTMLString(myMessage))
         {
            textComponent = createMessageComponent(myMessage);
         }
         else
         {
            JLabel textLabel = new JLabel(myMessage);
            textLabel.setUI(new MultiLineLabelUI());
            textComponent = textLabel;
         }
         textComponent.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 20));
         return textComponent;
      }



      public JTextComponent getTextField()
      {
         return myField;
      }



      protected JTextComponent createTextFieldComponent()
      {
         JTextField field = new JTextField(INPUT_DIALOG_COLUMNS);
         field.setMargin(JBInsets.create(0, 5));
         return field;
      }



      @Override
      public JComponent getPreferredFocusedComponent()
      {
         return myField;
      }



      public @Nullable @NlsSafe String getInputString()
      {
         return getExitCode() == 0 ? myField.getText().trim() : null;
      }
   }



   /*
   void resetFrom(List<String> patterns)
   {
      myListModel.clear();
      patterns.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(myListModel::addElement);
   }



   void applyTo(List<? super String> patterns)
   {
      patterns.clear();
      for(Object o : getListItems())
      {
         patterns.add((String) o);
      }
   }



   public void addRule(String rule)
   {
      addElement(rule);
   }
    */



   @Override
   protected Pair<String, String> editSelectedItem(Pair<String, String> item)
   {
      return showEditDialog(item);
   }
}

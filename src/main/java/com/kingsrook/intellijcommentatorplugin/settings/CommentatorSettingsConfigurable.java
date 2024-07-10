package com.kingsrook.intellijcommentatorplugin.settings;


import javax.swing.JComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;


/*******************************************************************************
 ** platform bridge for settings.
 *******************************************************************************/
public class CommentatorSettingsConfigurable implements Configurable
{
   private CommentatorSettingsComponent mySettingsComponent;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public @NlsContexts.ConfigurableName String getDisplayName()
   {
      return "Kingsrook Commentator";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Nullable
   @Override
   public JComponent createComponent()
   {
      mySettingsComponent = new CommentatorSettingsComponent();
      return mySettingsComponent.getPanel();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public JComponent getPreferredFocusedComponent()
   {
      return mySettingsComponent.getPreferredFocusedComponent();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isModified()
   {
      CommentatorSettingsState settings = CommentatorSettingsState.getInstance();

      boolean modified = false;
      modified |= !mySettingsComponent.getWrapCommentMaxWidthText().equals(String.valueOf(settings.wrapCommentMaxWidth));
      modified |= !mySettingsComponent.getWrapCommentMinWidthText().equals(String.valueOf(settings.wrapCommentMinWidth));

      return modified;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply() throws ConfigurationException
   {
      CommentatorSettingsState settings = CommentatorSettingsState.getInstance();
      settings.wrapCommentMaxWidth = Integer.parseInt(mySettingsComponent.getWrapCommentMaxWidthText());
      settings.wrapCommentMinWidth = Integer.parseInt(mySettingsComponent.getWrapCommentMinWidthText());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void reset()
   {
      CommentatorSettingsState settings = CommentatorSettingsState.getInstance();
      mySettingsComponent.setWrapCommentMaxWidthText(String.valueOf(settings.wrapCommentMaxWidth));
      mySettingsComponent.setWrapCommentMinWidthText(String.valueOf(settings.wrapCommentMinWidth));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void disposeUIResources()
   {
      mySettingsComponent = null;
   }
}

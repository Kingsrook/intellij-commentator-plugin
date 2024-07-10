package com.kingsrook.intellijcommentatorplugin.settings;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/*******************************************************************************
 ** State / model - for commentator settings
 *******************************************************************************/
@State(
   name = "com.kingsrook.intellijcommentatorplugin.settings.CommentatorSettingsState",
   storages = @Storage("KingsrookCommentatorPlugin.xml")
)
public class CommentatorSettingsState implements PersistentStateComponent<CommentatorSettingsState>
{
   public int wrapCommentMaxWidth = 96;
   public int wrapCommentMinWidth = 56;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static CommentatorSettingsState getInstance()
   {
      return ApplicationManager.getApplication().getService(CommentatorSettingsState.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public @Nullable CommentatorSettingsState getState()
   {
      return this;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void loadState(@NotNull CommentatorSettingsState state)
   {
      XmlSerializerUtil.copyBean(state, this);
   }

}

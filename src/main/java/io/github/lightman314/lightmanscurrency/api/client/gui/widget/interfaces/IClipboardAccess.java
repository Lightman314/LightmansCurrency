package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import net.minecraft.client.Minecraft;

public interface IClipboardAccess {

    default String getClipboard() { return Minecraft.getInstance().keyboardHandler.getClipboard(); }
    default void setClipboard(String text) { Minecraft.getInstance().keyboardHandler.setClipboard(text); }

}

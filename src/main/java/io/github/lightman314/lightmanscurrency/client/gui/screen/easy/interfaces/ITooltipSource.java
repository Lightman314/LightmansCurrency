package io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipSource {
    List<Component> getTooltip(int mouseX, int mouseY);
    List<Component> getTooltip();
}

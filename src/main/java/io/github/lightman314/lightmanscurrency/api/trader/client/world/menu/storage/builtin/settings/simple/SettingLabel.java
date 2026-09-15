package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextDisplayWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.network.chat.Component;

public class SettingLabel implements SimpleSettingBuilder {

    private final Component text;
    private final int textColor;
    public SettingLabel(Component text) { this(text,0xFF404040); }
    public SettingLabel(Component text, int textColor) {
        this.text = text;
        this.textColor = textColor;
    }

    @Override
    public int buildWidgets(IPositionalWidgetHolder holder, int width, int startY) {
        holder.addChild(ScreenPosition.of(0,startY),TextDisplayWidget.builder()
                .ofWidth(width)
                .withText(this.text)
                .withTextColor(this.textColor)
                .withCenteredText()
                .build());
        return 10;
    }

}

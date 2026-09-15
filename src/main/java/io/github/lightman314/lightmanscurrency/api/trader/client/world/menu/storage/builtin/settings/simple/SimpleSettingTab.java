package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.WidgetScrollingArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SimpleSettingTab extends SettingsSubTab {

    public static final int SPACING = 5;

    private final SimpleSettingCategory category;
    private final List<SimpleSettingBuilder> options;
    public SimpleSettingTab(SettingsClientTab parent,SimpleSettingCategory category, List<SimpleSettingBuilder> options) {
        super(parent);
        this.category = category;
        this.options = List.copyOf(options);
    }

    @Override
    public IconData getIcon() { return this.category.icon(); }

    @Override
    public Component getName() { return this.category.name(); }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) { }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        int width = area.width - 40;
        WidgetScrollingArea holder = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(20,20))
                .ofSize(width,115)
                .build());

        int y = 0;
        for(SimpleSettingBuilder builder : this.options) {
            int height = builder.buildWidgets(holder,width,y);
            y += height + SPACING;
        }
    }

}

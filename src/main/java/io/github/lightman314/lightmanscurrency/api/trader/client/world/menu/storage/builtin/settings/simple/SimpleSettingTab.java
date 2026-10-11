package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
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
    private final List<ScrollingWidgetBuilder> options;
    private WidgetScrollingArea holder;
    public SimpleSettingTab(SettingsClientTab parent,SimpleSettingCategory category,List<ScrollingWidgetBuilder> options) {
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
    public boolean isVisible() { return this.category.canAccess().apply(this); }

    @Override
    public boolean displayTitle() { return this.category.showTitle(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        int extraHeight = this.category.showTitle() ? 12 : 0;
        int width = area.width - 40;
        this.holder = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(20,6 + extraHeight))
                .ofSize(width,115 - extraHeight)
                .withOldWidget(this.holder)
                .build());
        this.holder.addChildren(this.options,SPACING);
    }

}

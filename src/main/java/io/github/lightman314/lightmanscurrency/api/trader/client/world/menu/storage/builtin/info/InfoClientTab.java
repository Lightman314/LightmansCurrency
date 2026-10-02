package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.IInfoTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTabWithSubTabs;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.InfoTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class InfoClientTab extends TraderStorageClientTabWithSubTabs<InfoTab,InfoClientSubTab> {

    public static final ClientMenuTab.TabBuilder<TraderStorageMenu, InfoTab, TraderStorageTab, TraderStorageScreen> BUILDER = InfoClientTab::new;

    private InfoClientTab(TraderStorageMenu menu, InfoTab commonTab, TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    protected void collectSubtabs(Consumer<InfoClientSubTab> builder) {
        InfoTabBuilder tabBuilder = new InfoTabBuilder();
        IInfoTabProvider.collectInfoTab(this,tabBuilder);
        tabBuilder.buildTabs(this,builder);
    }

    @Override
    public boolean isVisible() {
        this.assertSubtabsLoaded();
        return super.isVisible() && this.hasSubtabs();
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.WRITABLE_BOOK); }

    @Override
    public Component getName() { return InfoTab.TOOLTIP.get(); }

}


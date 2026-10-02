package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces.ISettingTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTabWithSubTabs;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SettingsTab;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SettingsClientTab extends TraderStorageClientTabWithSubTabs<SettingsTab,SettingsSubTab> implements ITickerClient {

    public static final ClientMenuTab.TabBuilder<TraderStorageMenu,SettingsTab,TraderStorageTab,TraderStorageScreen> BUILDER = SettingsClientTab::new;

    protected SettingsClientTab(TraderStorageMenu menu, SettingsTab commonTab, TraderStorageScreen screen) {
        super(menu, commonTab, screen);
    }

    @Override
    protected void collectSubtabs(Consumer<SettingsSubTab> builder) {
        SettingsTabBuilder b = new SettingsTabBuilder();
        //Collect the settings from the client nodes
        ISettingTabProvider.collectSettings(this,this,this.getCommonTab()::assembleAndHandleSettingRequst,b);
        //Build the results
        b.buildTabs(this,builder);
    }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/settings")); }

    @Override
    public boolean isVisible() {
        this.assertSubtabsLoaded();
        return super.isVisible() && this.hasSubtabs();
    }

    @Override
    public Component getName() { return SettingsTab.TOOLTIP.get(); }



    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        SettingsSubTab tab = this.getCurrentTab();
        if(tab != null && tab.displayTitle())
            gui.text(tab.getName(),8,6,0xFF404040,false);
        super.extractBackground(gui, area);
    }

    @Override
    public void clientTick() {
        SettingsSubTab tab = this.getCurrentTab();
        //Force ourselves back into the default tab slot if the current tab is no longer accessible
        if(tab != null && !tab.isVisible() && this.getCurrentTabSlot() != 0)
            this.setTab(0);
    }

    @Override
    public boolean blockInventoryButtonClosing() {
        SettingsSubTab tab = this.getCurrentTab();
        return tab != null && tab.blockInventoryButtonClosing();
    }

}

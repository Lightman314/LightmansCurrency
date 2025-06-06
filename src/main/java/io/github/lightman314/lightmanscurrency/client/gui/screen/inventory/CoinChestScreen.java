package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CoinChestScreen extends EasyMenuScreen<CoinChestMenu> {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/coin_chest.png");

    int currentTabIndex = 0;
    List<CoinChestTab> tabs = new ArrayList<>();
    LazyWidgetPositioner tabPositioner;

    public List<CoinChestTab> getTabs() { return this.tabs; }
    public CoinChestTab currentTab() { return this.tabs.get(this.currentTabIndex); }

    List<TabButton> tabButtons = new ArrayList<>();

    public final CoinChestBlockEntity be;

    public CoinChestScreen(CoinChestMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.be = this.menu.be;
        this.menu.AddExtraHandler(this::ClientMessageHandler);
        this.resize(176, 243);
    }

    private void safeAddTab(Object tab)
    {
        if(tab instanceof CoinChestTab t)
            this.tabs.add(t);
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.tabPositioner = this.addChild(LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT), ScreenPosition.of(TabButton.NEGATIVE_SIZE,0),TabButton.SIZE));

        this.refreshTabs(false);

        this.currentTab().onOpen();

    }

    private void refreshTabs(boolean clearButtons)
    {
        Class<?> oldTabType = null;
        if(clearButtons)
        {
            this.currentTab().onClose();
            for(TabButton tab : this.tabButtons)
                this.removeChild(tab);

            oldTabType = this.currentTab().getClass();
            this.tabPositioner.clear();
        }

        this.tabs = Lists.newArrayList(new DefaultTab(this));
        for(CoinChestUpgradeData data : this.menu.be.getChestUpgrades())
            data.upgrade.addClientTabs(data, this, this::safeAddTab);

        if(oldTabType != null)
        {
            for(int i = 0; i < this.tabs.size() && oldTabType != null; ++i)
            {
                if(this.tabs.get(i).getClass() == oldTabType)
                {
                    this.currentTabIndex = i;
                    oldTabType = null;
                }
            }
            //If old tab removed, reset to default tab
            if(oldTabType != null)
                this.currentTabIndex = 0;

        }



        this.tabButtons = new ArrayList<>();
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int index = i;
            CoinChestTab tab = this.tabs.get(index);
            TabButton button = this.addChild(TabButton.builder()
                    .pressAction(() -> this.changeTab(index))
                    .tab(tab)
                    .addon(EasyAddonHelper.visibleCheck(tab::isVisible))
                    .addon(EasyAddonHelper.activeCheck(() -> index != this.currentTabIndex))
                    .build());
            this.tabButtons.add(button);
            this.tabPositioner.addWidget(button);
        }

        this.validateSlotVisibility();

        this.currentTab().onOpen();

    }

    public void validateSlotVisibility()
    {
        this.menu.SetUpgradeSlotVisibility(this.currentTab().upgradeSlotsVisible());
        this.menu.SetCoinSlotVisibility(this.currentTab().coinSlotsVisible());
        this.menu.SetInventoryVisibility(this.currentTab().inventoryVisible());
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.renderNormalBackground(GUI_TEXTURE, this);

        //Render slot backgrounds for all visible slots
        for(Slot s : this.menu.slots)
        {
            if(s.isActive())
                gui.blit(GUI_TEXTURE, s.x - 1, s.y - 1, 176, 0, 18, 18);
        }

        try { this.currentTab().renderBG(gui);
        } catch(Throwable t) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", t); }

        if(this.currentTab().titleVisible())
            gui.drawString(this.title, 8, 6, 0x404040);
        if(this.currentTab().inventoryVisible())
            gui.drawString(this.playerInventoryTitle, 8, (this.getYSize() - 94), 0x404040);

    }

    @Override
    protected void renderTick() {
        if(!this.currentTab().isVisible())
            this.changeTab(0);
    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        try { this.currentTab().renderAfterWidgets(gui);
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); }
    }

    public void changeTab(int tabIndex)
    {

        tabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
        if(tabIndex == this.currentTabIndex)
            return;

        //Close the old tab
        this.currentTab().onClose();
        this.currentTabIndex = tabIndex;

        //Initialize the new tab
        this.currentTab().onOpen();
        this.validateSlotVisibility();

    }

    private void ClientMessageHandler(LazyPacketData message)
    {
        if(message.contains("RefreshTabs"))
            this.refreshTabs(true);
    }

    @Override
    public boolean blockInventoryClosing() { return this.currentTab().blockInventoryClosing(); }

}

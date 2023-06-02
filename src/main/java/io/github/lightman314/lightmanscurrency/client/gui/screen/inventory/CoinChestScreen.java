package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CoinChestScreen extends AbstractContainerScreen<CoinChestMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/coin_chest.png");

    int currentTabIndex = 0;
    List<CoinChestTab> tabs = Lists.newArrayList(new DefaultTab(this));

    public List<CoinChestTab> getTabs() { return this.tabs; }
    public CoinChestTab currentTab() { return this.tabs.get(this.currentTabIndex); }

    List<Renderable> tabWidgets = new ArrayList<>();
    List<GuiEventListener> tabListeners = new ArrayList<>();

    List<TabButton> tabButtons = new ArrayList<>();

    public final CoinChestBlockEntity be;

    public CoinChestScreen(CoinChestMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.be = this.menu.be;
        this.menu.AddExtraHandler(this::ClientMessageHandler);
        this.imageHeight = 243;
        this.imageWidth = 176;
    }

    private void safeAddTab(Object tab)
    {
        if(tab instanceof CoinChestTab t)
            this.tabs.add(t);
    }

    @Override
    protected void init() {
        super.init();

        this.tabWidgets.clear();
        this.tabListeners.clear();

        this.refreshTabs(false);

    }

    private void refreshTabs(boolean clearButtons)
    {
        Class<?> oldTabType = null;
        if(clearButtons)
        {
            this.currentTab().onClose();
            this.tabWidgets.clear();
            this.tabListeners.clear();
            for(TabButton tab : this.tabButtons)
                this.removeWidget(tab);

            oldTabType = this.currentTab().getClass();
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
            TabButton button = this.addRenderableWidget(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
            button.active = i != this.currentTabIndex;
            this.tabButtons.add(button);
        }

        this.validateTabVisiblity();
        this.validateSlotVisibility();

        this.currentTab().init();

    }

    public void validateTabVisiblity()
    {
        int y = 0;
        for(int i = 0; i < this.tabButtons.size() && i < this.tabs.size(); ++i)
        {
            TabButton button = this.tabButtons.get(i);
            CoinChestTab tab = this.tabs.get(i);
            button.visible = tab.isVisible();
            if(button.visible)
                button.reposition(this.leftPos - TabButton.SIZE, this.topPos + y++ * TabButton.SIZE, 3);
        }
    }

    public void validateSlotVisibility()
    {
        this.menu.SetUpgradeSlotVisibility(this.currentTab().upgradeSlotsVisible());
        this.menu.SetCoinSlotVisibility(this.currentTab().coinSlotsVisible());
        this.menu.SetInventoryVisibility(this.currentTab().inventoryVisible());

    }

    @Override
    protected void renderBg(@Nonnull PoseStack pose, float partialTicks, int mouseX, int mouseY) {

        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        //Render slot backgrounds for all visible slots
        for(Slot s : this.menu.slots)
        {
            if(s.isActive())
                blit(pose, this.leftPos + s.x - 1, this.topPos + s.y - 1, this.imageWidth, 0, 18, 18);
        }

        try {
            this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
            this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); }

    }

    @Override
    protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY)
    {
        if(this.currentTab().titleVisible())
            this.font.draw(pose, this.title, 8.0f, 6.0f, 0x404040);
        if(this.currentTab().inventoryVisible())
            this.font.draw(pose, this.playerInventoryTitle, 8.0f, (this.imageHeight - 94), 0x404040);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks)
    {

        //Hide any tabs that the player doesn't have permission/access to.
        this.validateTabVisiblity();
        if(!this.currentTab().isVisible())
            this.changeTab(0);

        this.renderBackground(pose);

        //Render the tab buttons & background
        super.render(pose, mouseX, mouseY, partialTicks);

        //Render the current tab
        try {
            this.currentTab().postRender(pose, mouseX, mouseY);
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); }

        this.renderTooltip(pose, mouseX,  mouseY);

        //Render the tab button tooltips
        for (TabButton tabButton : this.tabButtons) {
            if (this.tabButtons.indexOf(tabButton) > 0 && tabButton.isMouseOver(mouseX, mouseY))
                this.renderTooltip(pose, tabButton.tab.getTooltip(), mouseX, mouseY);
        }

        ITooltipSource.renderTooltips(this, pose, mouseX, mouseY);

    }

    public void changeTab(int tabIndex)
    {

        //Close the old tab
        this.currentTab().onClose();
        this.tabButtons.get(this.currentTabIndex).active = true;
        this.currentTabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
        this.tabButtons.get(this.currentTabIndex).active = false;

        //Clear the previous tabs widgets
        this.tabWidgets.clear();
        this.tabListeners.clear();

        //Initialize the new tab
        this.currentTab().init();
        this.validateSlotVisibility();

    }

    private void clickedOnTab(Button tab)
    {
        int tabIndex = -1;
        if(tab instanceof TabButton)
            tabIndex = this.tabButtons.indexOf(tab);
        if(tabIndex < 0)
            return;
        this.changeTab(tabIndex);
    }

    @Override
    public void containerTick()
    {
        this.currentTab().tick();
    }

    public <T extends Renderable> T addRenderableTabWidget(T widget)
    {
        this.tabWidgets.add(widget);
        if(widget instanceof GuiEventListener gl)
            this.addTabListener(gl);
        return widget;
    }

    public void removeRenderableTabWidget(Renderable widget)
    {
        this.tabWidgets.remove(widget);
        if(widget instanceof GuiEventListener gl)
            this.removeTabListener(gl);
    }

    public <T extends GuiEventListener> T addTabListener(T listener)
    {
        this.tabListeners.add(listener);
        return listener;
    }

    public void removeTabListener(GuiEventListener listener) { this.tabListeners.remove(listener); }

    public Font getFont() { return this.font; }

    @Override
    public @NotNull List<? extends GuiEventListener> children()
    {
        List<? extends GuiEventListener> coreListeners = super.children();
        List<GuiEventListener> listeners = Lists.newArrayList();
        listeners.addAll(coreListeners);
        listeners.addAll(this.tabListeners);
        return listeners;
    }

    private void ClientMessageHandler(LazyPacketData message)
    {
        if(message.contains("RefreshTabs"))
            this.refreshTabs(true);
    }

    @Override
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab().blockInventoryClosing()) {
            return true;
        }
        return super.keyPressed(p_97765_, p_97766_, p_97767_);
    }

}

package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings.TraderSettingsTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TraderSettingsClientTab extends TraderStorageClientTab<TraderSettingsTab> {

    private int selectedTab = 0;
    private final List<TabButton> tabButtons = new ArrayList<>();
    private final List<SettingsSubTab> tabs = new ArrayList<>();

    private SettingsSubTab getCurrentTab() {
        if(this.selectedTab < 0 || this.selectedTab >= this.tabs.size())
            this.selectedTab = 0;
        if(this.tabs.size() > 0)
            return this.tabs.get(this.selectedTab);
        return null;
    }

    private final List<Object> subtabWidgets = new ArrayList<>();

    public TraderSettingsClientTab(TraderStorageScreen screen, TraderSettingsTab commonTab) { super(screen, commonTab); }



    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_SETTINGS; }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.settings"); }

    @Override
    public boolean tabButtonVisible() { return this.menu.hasPermission(Permissions.EDIT_SETTINGS); }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public void onOpen() {

        this.subtabWidgets.clear();
        this.tabButtons.clear();
        this.refreshTabs();

        //Set up the "Current Tab"
        this.getCurrentTab().onOpen();

        this.tick();

        //Hide Coin Slots
        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void onClose() {
        //Reveal Coin Slots
        this.menu.SetCoinSlotsActive(true);
    }

    public void refreshTabs()
    {
        //Collect tabs
        this.tabs.clear();
        //Reset Selected Tabs
        this.selectedTab = 0;
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            this.tabs.addAll(trader.getSettingsTabs(this));

        //Remove Existing Tab Buttons
        for(TabButton b : this.tabButtons)
            this.screen.removeRenderableTabWidget(b);
        this.tabButtons.clear();

        //Create Tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;
            this.tabButtons.add(this.screen.addRenderableTabWidget(new TabButton(b -> this.openTab(tabIndex), this.font, this.tabs.get(tabIndex))));
        }

    }

    public void openTab(int index)
    {
        if(index == this.selectedTab || index < 0 || index >= this.tabs.size())
            return;
        this.getCurrentTab().onClose();
        while(this.subtabWidgets.size() > 0)
            this.removeWidget(this.subtabWidgets.get(0));
        this.selectedTab = index;
        this.getCurrentTab().onOpen();
    }

    public final <W> W addWidget(W widget)
    {
        this.subtabWidgets.add(widget);
        if(widget instanceof AbstractWidget aw)
            this.screen.addRenderableTabWidget(aw);
        else if(widget instanceof GuiEventListener gl)
            this.screen.addTabListener(gl);
        return widget;
    }

    public final void removeWidget(Object widget)
    {
        this.subtabWidgets.remove(widget);
        if(widget instanceof AbstractWidget aw)
            this.screen.removeRenderableTabWidget(aw);
        else if(widget instanceof GuiEventListener gl)
            this.screen.removeTabListener(gl);
    }

    @Override
    public void tick() {

        //Update Tab Button visiblity
        for(int i = 0; i < this.tabs.size() && i < this.tabButtons.size(); ++i)
        {
            this.tabButtons.get(i).visible = this.tabs.get(i).canOpen();
            this.tabButtons.get(i).active = this.selectedTab != i;
        }

        //Position buttons
        int yPos = this.screen.getGuiTop();
        for(TabButton button : this.tabButtons)
        {
            if(button.visible)
            {
                button.reposition(this.screen.getGuiLeft() + this.screen.getXSize(), yPos, 1);
                yPos += TabButton.SIZE;
            }
        }

        //Force close the tab if they don't have access to it anymore
        if(!this.getCurrentTab().canOpen() && this.selectedTab != 0)
            this.openTab(0);

        this.getCurrentTab().tick();

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.getCurrentTab().renderBG(pose, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {
        this.getCurrentTab().renderTooltips(pose, mouseX, mouseY);

        for(TabButton button : this.tabButtons)
            button.renderTooltip(pose, mouseX, mouseY, this.screen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return this.getCurrentTab().mouseClicked(mouseX, mouseY, button); }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return this.getCurrentTab().mouseReleased(mouseX, mouseY, button); }

    @Override
    public boolean shouldRenderInventoryText() { return this.getCurrentTab().shouldRenderInventoryText(); }

}
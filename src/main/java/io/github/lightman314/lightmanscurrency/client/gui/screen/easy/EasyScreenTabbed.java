package io.github.lightman314.lightmanscurrency.client.gui.screen.easy;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.options.IEasyScreenTabbedOptions;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.EasyTabRotation;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.tabs.TabOverflowHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tabs.EasyTabButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class EasyScreenTabbed<T extends EasyScreenTabbed<T,X>,X extends EasyTab<T>> extends EasyScreen {

    protected final TabOverflowHandler tabOverflowHandler;

    protected EasyScreenTabbed(IEasyScreenTabbedOptions options) {
        super(options);
        this.tabOverflowHandler = options.getTabOverflowHandler();
    }

    private final List<AbstractWidget> tabButtons = new ArrayList<>();

    @Override
    protected void initialize() {
        LightmansCurrency.LogInfo("Initializing tabbed screen.");
        //Clear lists
        this.tabButtons.clear();
        //Set up tab overflow widgets
        this.tabOverflowHandler.addWidgets(this);
        //Set up tab buttons (and open first tab)
        this.resetTabs();
        LightmansCurrency.LogInfo("Finished initializing tabbed screen.");
    }

    private List<X> tabs = new ArrayList<>();
    public final ImmutableList<X> currentTabs() { return ImmutableList.copyOf(this.tabs); }
    private int openTab = 0;
    public int getOpenTabIndex() { return this.openTab; }
    public X getOpenTab() {
        if(this.tabs.size() == 0)
            return null;
        if(this.openTab < 0 || this.openTab >= this.tabs.size())
            this.changeTab(0); //Reset to tab at index 0 in case of emergency
        return this.tabs.get(this.openTab);
    }

    private boolean resettingTabs = false;
    public final void resetTabs() {
        //Close the old tab
        X tab = this.getOpenTab();
        if(tab != null)
            tab.closeTab();
        this.resettingTabs = true;
        //Remove existing tab buttons
        for(AbstractWidget tabButton : this.tabButtons)
            this.removeChild(tabButton);
        //Collect tab list
        this.tabs = this.createTabs();
        LightmansCurrency.LogInfo("Tabbed screen generated " + this.tabs.size() + " tabs!");
        //Create new tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            final int tabIndex = i;
            AbstractWidget button = this.createTabButton(this.tabs.get(i), b -> this.changeTab(tabIndex));
            this.addChild(button);
            this.tabButtons.add(button);
        }
        this.resettingTabs = false;
        //Open the new tab
        tab = this.getOpenTab();
        if(tab != null)
            tab.openTab();
    }

    protected abstract int getTabButtonLimit();

    protected abstract @NotNull ScreenPosition getTabButtonPosition(int displayIndex);
    protected abstract @NotNull EasyTabRotation getTabButtonRotation(int displayIndex);

    public final void changeTab(int newTabIndex) {
        if(this.resettingTabs) //If in the process of resetting, assume that we know what we're doing. If wrong it'll be corrected in the getOpenTab function anyway.
            this.openTab = newTabIndex;
        if(newTabIndex == this.openTab || newTabIndex < 0 || newTabIndex >= this.tabs.size()) //Ignore if this tab is already open
            return;
        //Confirm that we can open the new tab
        X newTab = this.tabs.get(newTabIndex);
        //Always assume we can open tab at index 0 (for safety reasons)
        if(newTabIndex == 0 || newTab.canOpenTab(this.getPlayer()))
        {
            //Close the old tab
            if(this.openTab >= 0 && this.openTab < this.tabs.size())
            {
                X oldTab = this.tabs.get(this.openTab);
                oldTab.closeTab();
            }
            //Open the new tab
            this.openTab = newTabIndex;
            newTab.openTab();
        }
    }

    protected abstract @NotNull List<X> createTabs();

    protected @NotNull AbstractWidget createTabButton(@NotNull X tab, @NotNull Button.OnPress pressable) { return new EasyTabButton(tab, pressable); }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTick) {

        //Confirm that a valid tab exists
        X tab = this.getOpenTab();
        if(tab == null)
            LightmansCurrency.LogWarning("Tabbed Screen has no tabs during render phase.");

        //Handle tab buttons before rendering anything else
        this.tabOverflowHandler.handleTabs(this.tabButtons, ListUtil.convertList(this.tabs), this.minecraft.player, this.getTabButtonLimit(), this::getTabButtonPosition, this::getTabButtonRotation);

        //Draw background
        this.drawBackground(pose, mouseX, mouseY, partialTick);
        //Draw tab background
        if(tab != null)
            tab.renderTab(pose, mouseX, mouseY, partialTick);
        //Draw renderables
        this.drawRenderables(pose, mouseX, mouseY, partialTick);
        //Draw post renderables
        this.renderAfterWidgets(pose, mouseX,mouseY,partialTick);
        //Draw tab post renderables
        if(tab != null)
            tab.renderTabAfterWidgets(pose, mouseX, mouseY, partialTick);
        //Draw tooltips
        this.drawTooltips(pose, mouseX, mouseY);
        //Draw post tooltips
        this.renderAfterTooltips(pose, mouseX, mouseY, partialTick);
        //Draw tab post tooltips
        if(tab != null)
            tab.renderTabAfterTooltips(pose, mouseX, mouseY, partialTick);

    }

}
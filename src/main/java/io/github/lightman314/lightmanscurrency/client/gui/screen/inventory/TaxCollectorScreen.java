package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TaxCollectorScreen extends EasyMenuScreen<TaxCollectorMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/tax_collector.png");

    private int currentTab = 0;
    private final List<TaxCollectorClientTab<?>> tabs = new ArrayList<>();
    public TaxCollectorClientTab<?> getCurrentTab() {
        if(this.currentTab < 0 || this.currentTab >= this.tabs.size())
            return null;
        return this.tabs.get(this.currentTab);
    }

    private final List<TabButton> tabButtons = new ArrayList<>();

    public TaxCollectorScreen(TaxCollectorMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.resize(176, 176);
        this.menu.setTabChangeListener(this::ChangeTab);

        //Collect the client tabs
        for(TaxCollectorTab tab : this.menu.getAllTabs())
        {
            Object o = tab.createClientTab(this);
            if(o instanceof TaxCollectorClientTab<?> clientTab)
                this.tabs.add(clientTab);
            else
                throw new RuntimeException("Tab of type " + tab.getClass() + " did not return a Client Tab object!");
        }

    }

    private void ChangeTab(int newTabIndex)
    {
        if(newTabIndex != currentTab)
        {
            TaxCollectorClientTab<?> oldTab = this.getCurrentTab();
            oldTab.onClose();
            this.currentTab = newTabIndex;
            TaxCollectorClientTab<?> tab = this.getCurrentTab();
            if(tab != null)
                tab.onOpen();
        }
    }

    public TaxEntry getEntry() { return this.menu.getEntry(); }

    @Override
    protected void initialize(ScreenArea screenArea) {

        //Setup Tab Button
        this.tabButtons.clear();

        for(TaxCollectorClientTab<?> tab : this.tabs)
        {
            TabButton button = this.addChild(new TabButton(this::TabButtonClick, tab));
            this.tabButtons.add(button);
        }
        this.tickTabButtons();

        //Money Collection Button
        this.addChild(IconAndButtonUtil.collectCoinButtonAlt(screenArea.pos.offset(screenArea.width, 0), b -> this.menu.CollectStoredMoney(), this::storedMoneyText)
                .withAddons(EasyAddonHelper.visibleCheck(this::storedMoneyVisible), EasyAddonHelper.activeCheck(this::storedMoneyActive)));

        try{ this.getCurrentTab().onOpen(); } catch (Throwable ignored) {}



    }

    private void tickTabButtons()
    {
        int xPos = this.leftPos - TabButton.SIZE;
        int yPos = this.topPos;
        for(int i = 0; i < this.tabButtons.size(); ++i)
        {
            TabButton button = this.tabButtons.get(i);
            if(i > this.tabs.size())
                button.setVisible(false);
            else
            {
                TaxCollectorClientTab<?> tab = this.tabs.get(i);
                button.setVisible(tab.commonTab.canBeAccessed());
                if(button.isVisible())
                {
                    button.setActive(this.currentTab != i);
                    button.reposition(xPos, yPos, 3);
                    yPos += TabButton.SIZE;
                }
            }
        }
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

        this.tickTabButtons();

        gui.renderNormalBackground(GUI_TEXTURE, this);

        TaxEntry entry = this.getEntry();
        if(entry == null)
            return;

        //Render the Tab
        try { this.getCurrentTab().renderBG(gui);
        } catch (Throwable t) { LightmansCurrency.LogError("Error rendering tab BG!", t); }

    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        //Render the Tab
         try { this.getCurrentTab().renderAfterWidgets(gui);
         } catch (Throwable t) { LightmansCurrency.LogError("Error rendering tab Tooltips!", t); }
    }

    @Override
    public boolean blockInventoryClosing() {
        try { return this.getCurrentTab().blockInventoryClosing();
        } catch (Throwable ignored) { return super.blockInventoryClosing(); }
    }

    @Override
    protected void screenTick() {
        try { this.getCurrentTab().tick();
        } catch (Throwable t) { LightmansCurrency.LogError("Error ticking tab!", t); }
    }

    private boolean storedMoneyVisible() { TaxEntry entry = this.getEntry(); return entry != null && (entry.getStoredMoney().hasAny() || !entry.isLinkedToBank()); }
    private boolean storedMoneyActive() { TaxEntry entry = this.getEntry(); return entry != null && entry.getStoredMoney().hasAny(); }
    private Object storedMoneyText()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
            return entry.getStoredMoney().getString("0");
        return "NULL";
    }

    private void TabButtonClick(EasyButton button) {
        if(button instanceof TabButton)
        {
            int tabIndex = this.tabButtons.indexOf(button);
            if(tabIndex >= 0)
                this.menu.ChangeTab(tabIndex, true);
        }
    }

}

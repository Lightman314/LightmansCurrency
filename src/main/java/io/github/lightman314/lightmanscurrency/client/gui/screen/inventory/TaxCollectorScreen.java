package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.EasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.ButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.TaxCollectorMenu;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TaxCollectorScreen extends EasyTabbedMenuScreen<TaxCollectorMenu,TaxCollectorTab,TaxCollectorScreen> {

    public static final ResourceLocation GUI_TEXTURE = LightmansCurrency.id("textures/gui/container/tax_collector.png");

    public TaxCollectorScreen(TaxCollectorMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.resize(176, 176);
    }

    public TaxEntry getEntry() { return this.menu.getEntry(); }

    @Override
    protected IWidgetPositioner getTabButtonPositioner() { return LazyWidgetPositioner.create(this,LazyWidgetPositioner.createTopdown(WidgetRotation.LEFT),ScreenPosition.of(TabButton.NEGATIVE_SIZE,0),TabButton.SIZE); }

    @Override
    protected void init(ScreenArea screenArea) {
        //Money Collection Button
        this.addChild(ButtonUtil.finishCollectCoinButton(IconButton.builder()
                        .position(screenArea.pos.offset(screenArea.width, 0))
                        .pressAction(this.menu::CollectStoredMoney)
                        .addon(EasyAddonHelper.visibleCheck(this::storedMoneyVisible))
                        .addon(EasyAddonHelper.activeCheck(this::storedMoneyActive)),
                this::getMoneyStorage));
    }

    private MoneyStorage getMoneyStorage()
    {
        TaxEntry entry = this.menu.getEntry();
        if(entry != null)
            return entry.getStoredMoney();
        return null;
    }

    @Override
    protected void renderBackground(EasyGuiGraphics gui) {

        gui.renderNormalBackground(GUI_TEXTURE, this);

    }

    private boolean storedMoneyVisible() { TaxEntry entry = this.getEntry(); return entry != null && (!entry.getStoredMoney().isEmpty() || !entry.isLinkedToBank()); }
    private boolean storedMoneyActive() { TaxEntry entry = this.getEntry(); return entry != null && !entry.getStoredMoney().isEmpty(); }

}

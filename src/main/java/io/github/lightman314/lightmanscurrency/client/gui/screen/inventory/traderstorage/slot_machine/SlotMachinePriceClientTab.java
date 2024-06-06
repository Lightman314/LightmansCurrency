package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class SlotMachinePriceClientTab extends TraderStorageClientTab<SlotMachinePriceTab> {

    public SlotMachinePriceClientTab(Object screen, SlotMachinePriceTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModItems.COIN_GOLD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SLOT_MACHINE_EDIT_PRICE.get(); }

    private MoneyValueWidget priceInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        MoneyValue startingPrice = MoneyValue.empty();
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            startingPrice = trader.getPrice();
        this.priceInput = this.addChild(new MoneyValueWidget(screenArea.pos.offset((this.screen.getXSize() / 2) - (MoneyValueWidget.WIDTH / 2), 12), firstOpen ? null : this.priceInput, startingPrice, this::ChangePrice));
        this.priceInput.drawBG = false;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private void ChangePrice(MoneyValue newPrice) { this.commonTab.SetPrice(newPrice); }

}

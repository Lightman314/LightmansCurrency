package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class SlotMachinePriceClientTab extends TraderStorageClientTab<SlotMachinePriceTab> {

    public SlotMachinePriceClientTab(Object screen, SlotMachinePriceTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModItems.COIN_GOLD); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.slot_machine.edit_price"); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    private CoinValueInput priceInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        CoinValue startingPrice = CoinValue.EMPTY;
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            startingPrice = trader.getPrice();
        this.priceInput = this.addChild(new CoinValueInput(screenArea.pos.offset((this.screen.getXSize() / 2) - (CoinValueInput.DISPLAY_WIDTH / 2), 12), EasyText.empty(), startingPrice, this.getFont(), this::ChangePrice));
        this.priceInput.drawBG = false;
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    @Override
    public void tick() { this.priceInput.tick(); }

    private void ChangePrice(CoinValue newPrice) { this.commonTab.SetPrice(newPrice); }

}

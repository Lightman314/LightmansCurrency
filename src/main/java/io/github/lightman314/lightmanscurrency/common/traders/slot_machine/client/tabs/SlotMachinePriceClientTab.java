package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.client.tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.client.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.tabs.SlotMachinePriceTab;
import net.minecraft.network.chat.Component;

public class SlotMachinePriceClientTab extends TraderStorageClientTab<SlotMachinePriceTab> {

    public SlotMachinePriceClientTab(Object screen, SlotMachinePriceTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModItems.COIN_GOLD); }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SLOT_MACHINE_EDIT_PRICE.get(); }

    private MoneyValueWidget priceInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        MoneyValue startingPrice = MoneyValue.empty();
        SlotMachineNode node = this.commonTab.getNode();
        if(node != null)
            startingPrice = node.getPrice();
        this.priceInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 12))
                .oldIfNotFirst(firstOpen,this.priceInput)
                .startingValue(startingPrice)
                .valueHandler(this::ChangePrice)
                .build());
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) { }

    private void ChangePrice(MoneyValue newPrice) { this.commonTab.SetPrice(newPrice); }

}

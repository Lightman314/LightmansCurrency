package io.github.lightman314.lightmanscurrency.common.traders.gacha.client.tabs;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.client.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tabs.GachaPriceTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.TraderStorageClientTab;
import net.minecraft.network.chat.Component;

public class GachaPriceClientTab extends TraderStorageClientTab<GachaPriceTab> {

    public GachaPriceClientTab(Object screen, GachaPriceTab commonTab) { super(screen, commonTab); }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModItems.COIN_GOLD); }

    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_GACHA_EDIT_PRICE.get(); }

    private MoneyValueWidget priceInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        MoneyValue startingPrice = MoneyValue.empty();
        GachaNode node = this.commonTab.getNode();
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

    private void ChangePrice(MoneyValue newPrice) { this.commonTab.setPrice(newPrice); }

}
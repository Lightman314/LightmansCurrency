package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.gacha;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.gacha.GachaPriceTab;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class GachaPriceClientTab extends TraderStorageClientTab<GachaPriceTab> {

    public GachaPriceClientTab(Object screen, GachaPriceTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModItems.COIN_GOLD); }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_GACHA_EDIT_PRICE.get(); }

    private MoneyValueWidget priceInput;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {
        MoneyValue startingPrice = MoneyValue.empty();
        if(this.menu.getTrader() instanceof GachaTrader trader)
            startingPrice = trader.getPrice();
        this.priceInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2 - MoneyValueWidget.WIDTH / 2, 12))
                .oldIfNotFirst(firstOpen,this.priceInput)
                .startingValue(startingPrice)
                .valueHandler(this::ChangePrice)
                .build());
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private void ChangePrice(MoneyValue newPrice) { this.commonTab.setPrice(newPrice); }

}
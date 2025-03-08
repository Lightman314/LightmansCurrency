package io.github.lightman314.lightmanscurrency.common.traders.gatcha.tradedata;

import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.gatcha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gatcha.tradedata.client.GachaTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaTradeData extends TradeData {

    public final GachaTrader trader;
    public GachaTradeData(GachaTrader trader) {
        super(false);
        this.trader = trader;
    }

    @Nonnull
    @Override
    public MoneyValue getCost() { return this.trader.getPrice(); }

    @Override
    public boolean allowTradeRule(TradeRule rule) { return false; }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public int getStock(TradeContext context) { return this.trader.getStorage().getItemCount(); }

    @Override
    public TradeComparisonResult compare(TradeData expectedTrade) { return new TradeComparisonResult(); }
    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }
    @Override
    public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return List.of(); }
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRenderManager<?> getButtonRenderer() { return new GachaTradeButtonRenderer(this); }

    @Override
    public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }
    @Override
    public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { }
    @Override
    public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { }

}

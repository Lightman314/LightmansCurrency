package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.client.SlotMachineTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class SlotMachineTrade extends TradeData {

    public final SlotMachineTraderData trader;
    public SlotMachineTrade(SlotMachineTraderData trader) { super(false); this.trader = trader; }

    @Override
    public int getStock(@Nonnull TradeContext context) { return this.trader.getTradeStock(0); }

    @Override
    public MoneyValue getCost() { return this.trader.getPrice(); }

    @Override
    public boolean isValid() { return this.trader.hasValidTrade(); }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

    @Override
    public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return ImmutableList.of(); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRenderManager<?> getButtonRenderer() { return new SlotMachineTradeButtonRenderer(this); }

    //No Storage Menu Interactions for this Trade Type
    @Override
    public void OnInputDisplayInteraction(@Nonnull BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientHandler, int index, int button, @Nonnull ItemStack heldItem) { }

    @Override
    public void OnOutputDisplayInteraction(@Nonnull BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientHandler, int index, int button, @Nonnull ItemStack heldItem) { }

    @Override
    public void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nullable Consumer<LazyPacketData.Builder> clientHandler, int mouseX, int mouseY, int button, @Nonnull ItemStack heldItem) { }

    //Block Trade-Specific Trade Rules as the Slot Machine only has 1 trade, thus trader-wide rules will suffice.
    @Override
    public boolean allowTradeRule(@Nonnull TradeRule rule) { return false; }

}

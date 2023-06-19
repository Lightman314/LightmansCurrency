package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.client.SlotMachineTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.comparison.TradeComparisonResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class SlotMachineTrade extends TradeData {

    public final SlotMachineTraderData trader;
    public SlotMachineTrade(SlotMachineTraderData trader) { super(false); this.trader = trader; }

    @Override
    public CoinValue getCost() { return this.trader.getPrice(); }

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

    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRenderManager<?> getButtonRenderer() { return new SlotMachineTradeButtonRenderer(this); }

    //No Storage Menu Interactions for this Trade Type
    @Override
    public void onInputDisplayInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { }

    @Override
    public void onOutputDisplayInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { }

    @Override
    public void onInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) { }

    //Block Trade-Specific Trade Rules as the Slot Machine only has 1 trade, thus trader-wide rules will suffice.
    @Override
    public boolean allowTradeRule(@Nonnull TradeRule rule) { return false; }

}
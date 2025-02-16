package io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata;

import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.client.CommandTradeButtonRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTrade extends TradeData {

    private String command = "";
    public String getCommand() { return this.command; }
    public String getCommandDisplay() {
        if(this.command.isBlank() || this.command.startsWith("/"))
            return this.command;
        return "/" + this.command;
    }
    public void setCommand(@Nullable String command) { this.command = command == null ? "" : command; }

    public CommandTrade(boolean validateRules) { super(validateRules); }

    @Override
    public boolean isValid() { return super.isValid() && !this.command.isEmpty(); }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public int getStock(@Nonnull TradeContext context) { return 1; }

    public boolean canAfford(@Nonnull TradeContext context) { return context.hasFunds(this.getCost(context)); }

    @Nonnull
    public static List<CommandTrade> listOfSize(int size,boolean validateRules)
    {
        List<CommandTrade> list = new ArrayList<>();
        while(list.size() < size)
            list.add(new CommandTrade(validateRules));
        return list;
    }

    @Override
    public TradeComparisonResult compare(TradeData expectedTrade) { return new TradeComparisonResult(); }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

    @Override
    public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return new ArrayList<>(); }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRenderManager<?> getButtonRenderer() { return new CommandTradeButtonRenderer(this); }

    @Override
    public void OnInputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof CommandTrader trader)
        {
            int tradeIndex = trader.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED,tab.builder()
                    .setInt("TradeIndex",tradeIndex)
                    .setBoolean("CommandEdit",false));
        }
    }

    @Override
    public void OnOutputDisplayInteraction(@Nonnull BasicTradeEditTab tab, int index, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof CommandTrader trader)
        {
            int tradeIndex = trader.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED,tab.builder()
                    .setInt("TradeIndex",tradeIndex)
                    .setBoolean("CommandEdit",true));
        }
    }

    @Override
    public void OnInteraction(@Nonnull BasicTradeEditTab tab, @Nonnull TradeInteractionData data, @Nonnull ItemStack heldItem) { }

    @Override
    public CompoundTag getAsNBT() {
        CompoundTag tag = super.getAsNBT();
        tag.putString("Command",this.command);
        return tag;
    }

    @Nonnull
    public static CommandTrade loadData(CompoundTag tag, boolean validateRules)
    {
        CommandTrade trade = new CommandTrade(validateRules);
        trade.loadFromNBT(tag);
        return trade;
    }

    @Override
    protected void loadFromNBT(CompoundTag tag) {
        super.loadFromNBT(tag);
        this.command = tag.getString("Command");
    }
}
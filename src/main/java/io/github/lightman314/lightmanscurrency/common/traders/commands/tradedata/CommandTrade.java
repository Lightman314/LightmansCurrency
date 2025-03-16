package io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.client.CommandTradeButtonRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandTrade extends TradeData {

    private String command = "";
    public String getCommand() { return this.command; }
    public String formatCommand(Player player) { return this.command
            .replaceAll("%PLAYER%",player.getGameProfile().getName())
            .replaceAll("%PLAYER_NAME%",player.getName().getString());
    }
    public void setCommand(@Nullable String command) { this.command = command == null ? "" : command; }

    private String description = "";
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    private String tooltip = "";
    public String getTooltip() { return this.tooltip; }
    public void setTooltip(String tooltip) { this.tooltip = tooltip; }

    public String getCommandDisplay() {
        if(!this.description.isBlank())
            return this.description;
        if(this.command.isBlank() || this.command.startsWith("/"))
            return this.command;
        return "/" + this.command;
    }
    public List<Component> getCommandTooltip() {
        if(!this.tooltip.isBlank())
        {
            List<Component> lines = new ArrayList<>();
            for(String line : this.tooltip.split("\\\\n"))
                lines.add(EasyText.literal(line));
            return lines;
        }
        if(this.command.isBlank() || this.command.startsWith("/"))
            return Lists.newArrayList(EasyText.literal(this.command));
        return Lists.newArrayList(EasyText.literal("/" + this.command));
    }

    public CommandTrade(boolean validateRules) { super(validateRules); }

    @Override
    public boolean isValid() { return super.isValid() && !this.command.isEmpty(); }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public int getStock(TradeContext context) { return 1; }

    public boolean canAfford(TradeContext context) { return context.hasFunds(this.getCost(context)); }

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

    
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRenderManager<?> getButtonRenderer() { return new CommandTradeButtonRenderer(this); }

    @Override
    public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
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
    public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
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
    public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { }

    @Override
    public CompoundTag getAsNBT(HolderLookup.Provider lookup) {
        CompoundTag tag = super.getAsNBT(lookup);
        tag.putString("Command",this.command);
        tag.putString("Description",this.description);
        tag.putString("Tooltip",this.tooltip);
        return tag;
    }

    
    public static CommandTrade loadData(CompoundTag tag, boolean validateRules, HolderLookup.Provider lookup)
    {
        CommandTrade trade = new CommandTrade(validateRules);
        trade.loadFromNBT(tag,lookup);
        return trade;
    }

    @Override
    protected void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadFromNBT(tag, lookup);
        this.command = tag.getString("Command");
        if(tag.contains("Description"))
            this.description = tag.getString("Description");
        if(tag.contains("Tooltip"))
            this.tooltip = tag.getString("Tooltip");
    }
}

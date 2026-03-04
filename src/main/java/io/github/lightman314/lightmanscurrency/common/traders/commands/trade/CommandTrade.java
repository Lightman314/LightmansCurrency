package io.github.lightman314.lightmanscurrency.common.traders.commands.trade;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.*;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tabs.CommandTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandTrade extends RuleSupportingTradeData implements IDescriptionTrade {

    public static final Codec<CommandTrade> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(builder -> builder.group(
                    Codec.STRING.fieldOf("command").forGetter(CommandTrade::getCommand),
                    DescriptionData.CODEC.fieldOf("description").forGetter(CommandTrade::getDescriptionData)
            ).and(ruleFields(builder)).apply(builder,CommandTrade::new)),
            CodecHelper.oldValueLoader(CommandTrade::loadOldData,"Command Trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf,CommandTrade> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
            ByteBufCodecs.STRING_UTF8,CommandTrade::getCommand,
            DescriptionData.STREAM_CODEC,CommandTrade::getDescriptionData,
            CommandTrade::new);

    private String command = "";
    public String getCommand() { return this.command; }
    public String formatCommand(Player player) { return this.command
            .replaceAll("%PLAYER%",player.getGameProfile().getName())
            .replaceAll("%PLAYER_NAME%",player.getName().getString());
    }
    public void setCommand(@Nullable String command) { this.command = command == null ? "" : command; }

    private final DescriptionData description;
    @Override
    public DescriptionData getDescriptionData() { return this.description; }
    public void setDescription(String description) { this.description.description = description; }

    public void setTooltip(String tooltip) { this.description.tooltip = tooltip; }

    public String getCommandDisplay() {
        if(!this.description.description.isBlank())
            return this.description.description;
        if(this.command.isBlank() || this.command.startsWith("/"))
            return this.command;
        return "/" + this.command;
    }
    public List<Component> getCommandTooltip() {
        if(!this.description.tooltip.isBlank())
            return this.description.getTooltipLines();
        if(this.command.isBlank() || this.command.startsWith("/"))
            return Lists.newArrayList(EasyText.literal(this.command));
        return Lists.newArrayList(EasyText.literal("/" + this.command));
    }

    public CommandTrade(boolean validateRules) { super(validateRules); this.description = DescriptionData.create(); }
    private CommandTrade(String command, DescriptionData description, MoneyValue price) { this(command,description,price,new HashMap<>()); }
    private CommandTrade(String command, DescriptionData description, MoneyValue price, Map<TradeRuleType<?>,TradeRule> rules) {
        super(rules,price);
        this.command = command;
        this.description = description;
    }

    @Override
    public boolean isValid() { return super.isValid() && !this.command.isEmpty() && !this.command.equals("/"); }

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
    public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof CommandTrader trader)
        {
            int tradeIndex = trader.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            tab.sendOpenTabMessage(CommandTradeEditTab.KEY,tab.builder()
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
            tab.sendOpenTabMessage(CommandTradeEditTab.KEY,tab.builder()
                    .setInt("TradeIndex",tradeIndex)
                    .setBoolean("CommandEdit",true));
        }
    }

    @Override
    public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { }

    @Deprecated
    private static CommandTrade loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        CommandTrade trade = new CommandTrade(true);
        trade.loadFromNBT(tag,lookup);
        return trade;
    }

    @Override
    @Deprecated
    protected void loadFromNBT(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadFromNBT(tag, lookup);
        this.command = tag.getString("Command");
        if(tag.contains("Description"))
            this.description.description = tag.getString("Description");
        if(tag.contains("Tooltip"))
            this.description.tooltip = tag.getString("Tooltip");
    }

}

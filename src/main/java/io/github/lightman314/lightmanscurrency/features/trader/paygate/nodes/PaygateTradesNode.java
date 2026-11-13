package io.github.lightman314.lightmanscurrency.features.trader.paygate.nodes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.FlexibleSizedTradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.blocks.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.menu.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.notifications.PaygateTradeNotification;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.trade.PaygateTradeData;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PaygateTradesNode extends FlexibleSizedTradingNode<PaygateTradeData> implements ISettingsMessageListener {

    private static final MapCodec<PaygateTradesNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PaygateTradeData.CODEC.listOf().fieldOf("trades").forGetter(PaygateTradesNode::getMutableTrades),
            OutputConflictHandling.CODEC.fieldOf("conflict_handling").forGetter(PaygateTradesNode::getConflictHandling)
    ).apply(builder,PaygateTradesNode::new));

    public static final TraderNodeType<PaygateTradesNode> TYPE = TraderNodeType.simple(PaygateTradesNode::new,MAP_CODEC);

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry SETTINGS_LABEL = TextEntry.tooltip(LCApi.MODID,"trader.settings.paygate");
    public static final TextEntry CONFLICT_MODE = TextEntry.traderNodeValue(TYPE,"conflict_handling");

    //Trade Tooltips
    public static final TextEntry TOOLTIP_ALREADY_ACTIVE = TextEntry.tooltip(LCApi.MODID,"trader.paygate.already_active");
    public static final TextEntry TOOLTIP_TIME_REMAINING = TextEntry.tooltip(LCApi.MODID,"trader.paygate.time_remaining");

    //Trade Edit Labels
    public static final TextEntry GUI_DURATION = TextEntry.gui(LCApi.MODID,"trader.paygate.duration");
    public static final TextEntry GUI_REDSTONE_LEVEL = TextEntry.gui(LCApi.MODID,"trader.paygate.redstone_level");
    public static final TextEntry GUI_OUTPUT_SIDES = TextEntry.gui(LCApi.MODID,"trader.paygate.outputs");

    private OutputConflictHandling conflictHandling = OutputConflictHandling.DENY_SIDE_CONFLICT;
    public OutputConflictHandling getConflictHandling() { return this.conflictHandling; }
    public boolean setConflictHandling(OutputConflictHandling conflictHandling,@Nullable PlayerReference admin) {
        if(this.conflictHandling == conflictHandling)
            return false;
        this.conflictHandling = conflictHandling;
        this.setChanged(builder -> builder.setEnum("conflict_handling",this.conflictHandling));
        if(admin != null)
            this.postNotification(ChangeSettingNotification.simple(admin,CONFLICT_MODE.get(),this.conflictHandling.getName()));
        return true;
    }

    private PaygateTradesNode() { }
    private PaygateTradesNode(List<PaygateTradeData> trades,OutputConflictHandling conflictHandling) {
        super(trades);
        this.attachTrades();
        this.conflictHandling = conflictHandling;
    }

    @Nullable
    @Override
    public IconData getDefaultIcon() { return ItemIcon.of(Items.REDSTONE_BLOCK); }

    @Override
    public void updateArgument(TraderArguments arguments) {
        Optional<Number> arg = arguments.tryGet(TYPE,Number.class);
        if(arg.isPresent())
            this.forceTradeCount(Math.clamp(arg.get().intValue(),1, TraderData.GLOBAL_TRADE_LIMIT));
    }

    @Override
    protected PaygateTradeData createNewTrade() { return new PaygateTradeData(); }

    @Override
    public int getMaxTradeCount() { return 16; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        super.createSyncPacket(builder, context);
        builder.setEnum("conflict_handling",this.conflictHandling);
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        super.onDataSync(data);
        if(data.contains("conflict_handling"))
            this.conflictHandling = data.getEnum("conflict_handling",OutputConflictHandling.class,this.conflictHandling);
    }

    @Nullable
    @Override
    public Identifier advancedEditTabKey() { return PaygateTradeEditTab.KEY; }

    @Override
    public Component getSetLabel() { return NAME.get(); }

    @Override
    public void handleSettingsChange(Player player,FancyPacketMap message) {
        if(message.contains("changeConflictMode")) {
            OutputConflictHandling newMode = message.getEnum("changeConflictMode",OutputConflictHandling.class);
            if(newMode != null && this.getPermission(player,LCPermissions.EDIT_SETTINGS))
                this.setConflictHandling(newMode,PlayerReference.of(player));
        }
    }

    public boolean hasConflict(PaygateBlockEntity blockEntity, DirectionalSettings outputSides) {
        if(this.conflictHandling == OutputConflictHandling.DENY_ANY)
            return blockEntity.isActive();
        for(Direction side : blockEntity.getActiveSides()) {
            if(outputSides.allowOutputs(side))
                return true;
        }
        return false;
    }

    @Override
    public TradeResult executeTrade(TradeContext context,int tradeIndex) throws TradeFailedException {
        PaygateTradeData trade = this.getTrade(tradeIndex);
        WorldNode worldNode = this.getNode(WorldNode.TYPE);
        PaygateBlockEntity blockEntity = null;
        if(worldNode != null && worldNode.getBlockEntity(context.getLevel()) instanceof PaygateBlockEntity p)
            blockEntity = p;
        //Abort if the trade or block entity is null
        if(trade == null || blockEntity == null)
            return TradeResult.FAIL_NULL;
        TradePrice price = trade.getPrice(context);
        if(!price.isValid(context.getTrader(),trade))
            return TradeResult.FAIL_INVALID_TRADE;
        if(!trade.isCustomerReady())
            return TradeResult.FAIL_INVALID_TRADE;

        //Check for activation conflicts
        if(this.hasConflict(blockEntity,trade.getOutputSides()) && !this.conflictHandling.allowsConflicts)
            return TradeResult.FAIL_OUT_OF_STOCK;
        //Transfer the trades price
        TradePrice.TransferResult result = price.transferPrice(TradeDirection.SALE,context,this,trade);
        if(!result.isSuccess())
            return TradeResult.FAIL_INVALID_TRADE;
        //We have collected the payment, activate the paygate
        blockEntity.activate(trade.getDuration(),trade.getRedstoneLevel(),trade.getOutputSides(),this.conflictHandling,trade.getDescription());
        PaygateTradeNotification notification = new PaygateTradeNotification(result.getReceipt(),trade.getDuration(),context.getCustomer().getName(context),new TraderCategory(context.getTrader()),result.getTaxesPaid());
        this.postNotification(notification);
        return this.finishSuccessfulTrade(context,trade,price,result,notification);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void encodeSettings(ValueOutput output) {
        super.encodeSettings(output);
        output.store("conflict_handling",OutputConflictHandling.CODEC,this.conflictHandling);
    }
    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        super.decodeSettings(data,context);
        this.conflictHandling = data.read("conflict_handling",OutputConflictHandling.CODEC).orElse(OutputConflictHandling.DENY_SIDE_CONFLICT);
    }

    @Override
    public void appendDisplay(ValueInput data,SettingsDisplayOutput output) {
        super.appendDisplay(data,output);
        output.acceptEntry(CONFLICT_MODE,data.read("conflict_handling",OutputConflictHandling.CODEC).orElse(OutputConflictHandling.DENY_SIDE_CONFLICT).getName());
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        super.addTabs(builder);
        builder.addTab(PaygateTradeEditTab::new);
    }
}
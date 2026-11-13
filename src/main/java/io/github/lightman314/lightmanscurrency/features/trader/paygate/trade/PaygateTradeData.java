package io.github.lightman314.lightmanscurrency.features.trader.paygate.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.*;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.settings.SettingsIOTrade;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.world.data.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public class PaygateTradeData extends TradeDataWithRules implements IDirectionalSettingsObject, IDescriptionTrade, SettingsIOTrade {

    public static final int DURATION_MIN = 1;
    public static int getMaxDuration() {
        int value = LCConfig.SERVER.paygateMaxDuration.get();
        if(value <= 0)
            return Integer.MAX_VALUE;
        return value;
    }

    private static final MapCodec<PaygateTradeData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradePrice.CODEC.fieldOf("price").forGetter(PaygateTradeData::getInternalPrice),
            Codec.intRange(1,Integer.MAX_VALUE).fieldOf("duration").forGetter(PaygateTradeData::getDuration),
            Codec.intRange(1,Redstone.SIGNAL_MAX).fieldOf("level").forGetter(PaygateTradeData::getRedstoneLevel),
            DescriptionData.MAP_CODEC.forGetter(PaygateTradeData::getDescriptionData),
            DirectionalSettings.CODEC.fieldOf("outputs").forGetter(PaygateTradeData::getOutputSides),
            tradeRuleArg()
    ).apply(builder,PaygateTradeData::new));

    public static final Codec<PaygateTradeData> CODEC = MAP_CODEC.codec();
    public static final TradeDataType<PaygateTradeData> TYPE = new TradeDataType<>(MAP_CODEC);

    private int duration = 20;
    public int getDuration() { return this.duration; }
    public void setDuration(int duration) {
        duration = Math.max(1,duration);
        if(this.duration != duration) {
            this.duration = duration;
            this.setChanged(builder -> builder.setInt("duration",this.duration));
        }
    }

    private int level = Redstone.SIGNAL_MAX;
    public int getRedstoneLevel() { return this.level; }
    public void setRedstoneLevel(int level) {
        level = Math.clamp(level,1,Redstone.SIGNAL_MAX);
        if(this.level != level) {
            this.level = level;
            this.setChanged(builder -> builder.setInt("level",this.level));
        }
    }

    private final DescriptionData description;
    @Override
    public DescriptionData getDescriptionData() { return this.description; }
    public void setDescription(String description) {
        if(!this.description.description.equals(description)) {
            this.description.description = description;
            this.setChanged(builder -> builder.setString("description",description));
        }
    }
    public void setTooltip(String tooltip) {
        if(!this.description.tooltip.equals(tooltip)) {
            this.description.tooltip = tooltip;
            this.setChanged(builder -> builder.setString("tooltip",tooltip));
        }
    }

    private final DirectionalSettings outputs = new DirectionalSettings()
            .withParent(this,DirectionalSettingsState.OUTPUT)
            .withListener(this::setOutputsChanged);
    public DirectionalSettings getOutputSides() { return this.outputs; }
    private void setOutputsChanged(Consumer<FancyPacketMap.Mutable> change) { this.setChanged(builder -> builder.modifyMap("outputs",change)); }

    public PaygateTradeData() { super(); this.description = DescriptionData.create(); }
    public PaygateTradeData(TradePrice price,int duration,int level,DescriptionData description,DirectionalSettings outputs,Map<TradeRuleType<?>, TradeRule> rules) {
        super(price,rules);
        this.duration = duration;
        this.level = level;
        this.description = description;
        this.outputs.copyFrom(outputs);
    }

    @Override
    public TradeDataType<?> getType() { return TYPE; }

    @Override
    public TradeDirection getDirection() { return TradeDirection.SALE; }

    @Override
    public boolean isCustomerReady() { return this.getInternalPrice().isValid(this.getTrader(),this); }

    @Override
    protected void getAdditionalFullPacket(FancyPacketMap.Mutable packet,ISyncingContext context) {
        packet.setInt("duration",this.duration)
                .setInt("level",this.level)
                .write(this.description)
                .setMap("outputs",this.outputs.asPacket());
    }

    @Override
    protected void handleAdditionalPacket(FancyPacketMap message) {
        if(message.contains("duration"))
            this.duration = message.getInt("duration",1);
        if(message.contains("level"))
            this.level = message.getInt("level");
        if(message.contains("description"))
            this.description.description = message.getString("description");
        if(message.contains("tooltip"))
            this.description.tooltip = message.getString("tooltip");
        if(message.contains("outputs"))
            this.outputs.handlePacket(message.getMap("outputs"));
    }

    @Override
    public long getStock(TradeContext context) { return Long.MAX_VALUE; }

    @Override
    public boolean showOutOfSpaceWarning(TradeContext context) { return false; }

    @Override
    public boolean processTradeClick(Player player,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        if(slot.isInput())
            return this.getInternalPrice().onClickInteraction(player,this,slot,button,heldItem,context);
        ITradeInteractionHandler handler = context.handler();
        if(handler.isSimpleEdit())
            handler.openAdvancedEdit(this,TradeSlot.OUTPUT_0);
        else
            handler.changeSelection(TradeSlot.OUTPUT_0);
        return true;
    }

    @Override
    public boolean processTradeScroll(Player player,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        if(slot.isInput())
            return this.getInternalPrice().onScrollInteraction(player,this,slot,deltaY,heldItem,context);
        if(slot.isOutput()) {
            //Let the scroll modify the duration
            this.setDuration(this.duration + Math.round(deltaY * -1f));
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Block getDisplayBlock() {
        TradingNode<?> holder = this.getHolder();
        if(holder != null && holder.hasNode(WorldNode.TYPE)) {
            WorldNode node = holder.getNode(WorldNode.TYPE);
            Block b = node.getBlock();
            if(b != Blocks.AIR)
                return b;
        }
        return LCBlocks.PAYGATE.get();
    }

    @Override
    public DirectionalSettingsState getSidedState(Direction side) { return this.outputs.getState(side); }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putInt("duration",this.duration);
        output.putInt("level",this.level);
        output.putString("description",this.description.description);
        output.putString("tooltip",this.description.tooltip);
        output.store("outputs",DirectionalSettings.CODEC,this.outputs);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setDuration(data.getIntOr("duration",20));
        this.setRedstoneLevel(data.getIntOr("level",Redstone.SIGNAL_MAX));
        this.setDescription(data.getStringOr("description",""));
        this.setTooltip(data.getStringOr("tooltip",""));
        data.read("outputs",DirectionalSettings.CODEC)
                .ifPresentOrElse(this.outputs::copyFrom,
                        () -> this.outputs.setAllStates(DirectionalSettingsState.OUTPUT));
    }

}

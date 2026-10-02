package io.github.lightman314.lightmanscurrency.api.trader.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class TradeRule implements ISidedContext {

    public static final Codec<TradeRule> STANDALONE_CODEC = LCRegistries.Trader.TRADE_RULE_TYPE.byNameCodec()
            .dispatch(TradeRule::getType,TradeRuleType::codec);

    public static final Codec<Map<TradeRuleType<?>,TradeRule>> SET_CODEC = Codec.dispatchedMap(LCRegistries.Trader.TRADE_RULE_TYPE.byNameCodec(),TradeRuleType::fullCodec);

    public static String translationKeyOfType(Identifier ruleType) { return Util.makeDescriptionId("traderule",ruleType); }
    public static String translationKeyOfType(TradeRuleType<?> type) { return translationKeyOfType(type.getKey()); }
    public static Component nameOfType(Identifier ruleType) { return Component.translatable(translationKeyOfType(ruleType)); }
    public static Component nameOfType(TradeRuleType<?> ruleType) { return nameOfType(ruleType.getKey()); }

    protected static <T extends TradeRule> RecordCodecBuilder<T,Boolean> activeField() {
        return Codec.BOOL.fieldOf("active").forGetter(TradeRule::isActiveInternal);
    }

    @Override
    public final boolean isClient() { return this.holder == null || this.holder.isClient(); }
    @Override
    public final boolean isServer() { return ISidedContext.super.isServer(); }

    private TradeRuleHolder holder;
    protected TradeRuleHolder getHolder() { return this.holder; }
    public final void attach(TradeRuleHolder holder) { this.holder = holder; }
    public static void attach(Map<TradeRuleType<?>,TradeRule> data,TradeRuleHolder holder) {
        for(TradeRule rule : data.values())
            rule.attach(holder);
    }

    public final void setChanged(Consumer<FancyPacketMap.Mutable> writer) {
        if(this.holder != null)
            this.holder.setRuleChanged(this.getType(),writer);
    }
    public final void setChanged(Consumer<FancyPacketMap.Mutable> writer,UUID player) {
        if(this.holder != null)
            this.holder.setRuleChanged(this.getType(),writer,player);
    }
    public final void setChanged(Consumer<FancyPacketMap.Mutable> writer, Predicate<ISyncingContext> filter) {
        if(this.holder != null)
            this.holder.setRuleChanged(this.getType(),writer,filter);
    }

    private boolean active = false;
    protected final boolean isActiveInternal() { return this.active; }
    public final boolean isActive() { return this.holder != null && this.canActivate(this.holder) && this.active; }
    public void setActive(boolean active) {
        this.active = true;
        this.setChanged(builder -> builder.setBoolean("active",this.active));
    }

    public final boolean attachToHolder(@Nullable TradeRuleHolder holder) {
        if(holder == null)
            return false;
        if(this.requiresMultipleTrades() && !holder.isMultiTrade())
            return false;
        if(this.requiresSingleTrade() && !holder.isSingleTrade())
            return false;
        return this.allowHolder(holder);
    }

    protected boolean allowHolder(TradeRuleHolder holder) { return true; }

    public boolean canActivate() { return this.holder != null && this.canActivate(this.holder); }
    protected boolean canActivate(TradeRuleHolder holder) { return true; }
    public boolean canPlayerActivate(Player player) { return this.canActivate(); }

    protected boolean requiresMultipleTrades() { return false; }
    protected boolean requiresSingleTrade() { return false; }

    protected static <T> void postEventToRules(INodeAccess trader,TradeData trade,T event,BiConsumer<TradeRule,T> ruleHandler) {
        if(trade instanceof TradeRuleHolder holder)
            postEventToHolder(holder,event,ruleHandler);
        for(TradeRuleHolder holder : trader.getNodes(TradeRuleHolder.class))
            postEventToHolder(holder,event,ruleHandler);
    }

    protected static <T> void postEventToHolder(TradeRuleHolder holder, T event,BiConsumer<TradeRule,T> ruleHandler) {
        for(TradeRule rule : holder.getRules()) {
            if(rule.isActive())
                ruleHandler.accept(rule,event);
        }
    }

    @ApiStatus.Internal
    public static void beforeTrade(INodeAccess trader,TradeData trade,TradeEvent.Pre event) {
        postEventToRules(trader,trade,event,TradeRule::beforeTrade);
    }
    protected void beforeTrade(TradeEvent.Pre event) {}

    @ApiStatus.Internal
    public static void tradeBaseCost(INodeAccess trader,TradeData trade,TradeEvent.BaseCost event) {
        postEventToRules(trader,trade,event,TradeRule::tradeBaseCost);
    }
    protected void tradeBaseCost(TradeEvent.BaseCost event) {}

    @ApiStatus.Internal
    public static void tradeCost(INodeAccess trader,TradeData trade,TradeEvent.Cost event) {
        postEventToRules(trader,trade,event,TradeRule::tradeCost);
    }
    protected void tradeCost(TradeEvent.Cost event) {}

    @ApiStatus.Internal
    public static void afterTrade(INodeAccess trader,TradeData trade,TradeEvent.Post event) {
        postEventToRules(trader,trade,event,TradeRule::afterTrade);
    }
    protected void afterTrade(TradeEvent.Post event) {}

    protected TradeRule() {}
    protected TradeRule(boolean active) { this.active = active; }

    public abstract TradeRuleType<?> getType();

    public final Component getName() { return nameOfType(this.getType()); }

    public abstract IconData getIcon();

    public final boolean shouldWriteToFile() { return !this.isDefaultValues(); }

    public final boolean isDefaultValues() { return !this.active || this.isAdditionalDefaultValues(); }

    protected abstract boolean isAdditionalDefaultValues();

    public final void createFullSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        this.createSyncPacket(builder,context);
        builder.setBoolean("active",this.active);
    }
    protected abstract void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context);

    public final void handleSyncPacket(FancyPacketMap message) {
        if(message.contains("active"))
            this.active = message.getBoolean("active");
        this.handlePacket(message);
    }
    protected abstract void handlePacket(FancyPacketMap message);

    public final void handlePlayerRequest(Player player,FancyPacketMap request) {
        if(request.contains("setActive")) {
            boolean active = request.getBoolean("setActive");
            boolean wasActive = this.active;
            if(active)
                this.active = this.active || this.canPlayerActivate(player);
            else
                this.active = false;
            if(this.active != wasActive)
                this.setChanged(builder -> builder.setBoolean("active",this.active));
        }
        this.handlePlayerInteraction(request);
    }
    protected abstract void handlePlayerInteraction(FancyPacketMap request);

    public final void resetToDefaultValues() {
        this.active = false;
        this.resetAdditionalToDefault();
    }
    protected abstract void resetAdditionalToDefault();

    public abstract void encodeSettings(ValueOutput output);
    public abstract void decodeSettings(ValueInput data);

}
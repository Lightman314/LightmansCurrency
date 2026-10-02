package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IFlexibleTradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IUpgradeUser;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.CapacityUpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeReference;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.world.UpgradeStorage;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCUpgrades;
import io.github.lightman314.lightmanscurrency.features.trader.item.trade.ItemTradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class UpgradeableTradingNode<T extends TradeData> extends TradingNode<T> implements IUpgradeUser, IUpgradeListener, IFlexibleTradingNode, IDisplayNode {

    protected int baseCount = 1;
    public final int getBaseCount() { return this.baseCount; }
    public void safeUpdateBaseCount(int expectedCount) { if(this.baseCount < expectedCount) this.overrideBaseCount(expectedCount); }
    public void overrideBaseCount(int newCount) { this.baseCount = Math.max(1,newCount); this.refactorTrades(); }
    public void upgradeBaseCount(int amount) { if(amount > 0) this.overrideBaseCount(this.baseCount + amount); }
    protected int upgradeCount = 0;
    private final List<T> trades = new ArrayList<>();

    public static <N extends UpgradeableTradingNode<T>,T extends TradeData> Products.P2<RecordCodecBuilder.Mu<N>,Integer,Integer> baseFields(RecordCodecBuilder.Instance<N> builder) {
        return builder.group(
                Codec.INT.optionalFieldOf("baseCount",1).forGetter(n -> n.baseCount),
                Codec.INT.optionalFieldOf("upgradeCount",0).forGetter(n -> n.upgradeCount)
        );
    }
    public static <N extends UpgradeableTradingNode<T>,T extends ItemTradeData> MapCodec<N> buildCodec(Codec<T> tradeCodec, Function3<Integer,Integer,List<T>,N> factory) {
        return RecordCodecBuilder.mapCodec(builder -> baseFields(builder)
                .and(tradeCodec.listOf().fieldOf("trades").forGetter(TradingNode::getMutableTrades))
                .apply(builder,factory));
    }

    protected UpgradeableTradingNode() {}
    protected UpgradeableTradingNode(int baseCount,int upgradeCount,List<T> trades) {
        this.baseCount = baseCount;
        this.upgradeCount = upgradeCount;
        this.trades.addAll(trades);
        this.attachTrades();
    }

    @Nullable
    protected abstract Identifier getBaseCountArgument();

    @Override
    @OverridingMethodsMustInvokeSuper
    public void updateArgument(TraderArguments arguments) {
        Identifier key = this.getBaseCountArgument();
        if(key != null)
        {
            Optional<Number> arg = arguments.tryGet(key,Number.class);
            if(arg.isPresent())
                this.baseCount = Math.clamp(arg.get().intValue(),1, TraderData.GLOBAL_TRADE_LIMIT);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onAttach() {
        this.refactorTrades();
    }

    protected final void refactorTrades() {
        if(this.isClient())
            return;
        //Validate the total trade count
        int totalCount = Math.clamp(this.baseCount + this.upgradeCount,1,TraderData.GLOBAL_TRADE_LIMIT);
        //LightmansCurrency.LogDebug("Refactoring trade count. Old total: " + this.trades.size() + " New total: " + totalCount);
        this.forceTradeCount(totalCount);
    }

    @Override
    protected final List<T> getMutableTrades() { return this.trades; }

    //Upgradeable node methods
    protected final int getUnusedOfferUpgrades() {
        UpgradeStorage upgrades = this.getNodeValue(UpgradeNode.TYPE,UpgradeNode::getStorage);
        if(upgrades == null)
            return 0;
        int totalBonus = CapacityUpgradeType.getTotalCapacity(0,upgrades,LCUpgrades.TRADE_OFFER);
        if(totalBonus <= 0)
            return 0;
        int totalUsedBonus = 0;
        for(UpgradeableTradingNode<?> node : this.getNodes(UpgradeableTradingNode.class)) {
            totalUsedBonus += node.upgradeCount;
        }
        return totalBonus - totalUsedBonus;
    }

    protected final boolean isOnlyUpgradeableNode() { return this.getNodes(UpgradeableTradingNode.class).size() <= 1; }

    protected final boolean hasAnyOfferUpgrades() {
        UpgradeStorage upgrades = this.getNodeValue(UpgradeNode.TYPE,UpgradeNode::getStorage);
        if(upgrades == null)
            return false;
        for(UpgradeReference upgrade : upgrades) {
            if(upgrade.is(LCUpgrades.TRADE_OFFER) && CapacityUpgradeType.getBonusCapacity(upgrade.data()) > 0)
                return true;
        }
        return false;
    }

    @Override
    public void afterUpgradesChanged(UpgradeStorage upgrades) {
        int overflow = this.getUnusedOfferUpgrades() * -1;
        if(this.isServer() && this.upgradeCount > 0 && overflow > 0) {
            //Remove the inaccessible trade offers
            this.upgradeCount -= Math.min(this.upgradeCount,overflow);
            this.refactorTrades();
        }
    }

    @Override
    public boolean allowUpgrade(UpgradeType type) { return type.is(LCUpgrades.TRADE_OFFER); }

    @Override
    public boolean showTradeCountButtons(Player player) { return this.hasAnyOfferUpgrades() && !this.hasAnyOfferUpgrades(); }

    @Override
    public boolean canAddTrade(Player player) { return LCApi.isInAdminMode(player) || this.getUnusedOfferUpgrades() > 0; }
    @Override
    public boolean canRemoveTrade(Player player) { return (LCApi.isInAdminMode(player) && this.trades.size() > 1) || this.upgradeCount > 0; }

    @Override
    public boolean tryAddTrade(Player player) {
        if(this.isClient())
            return false;
        //Cannot add if it'll go past the global limit
        if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
            return false;
        //If the player is an admin, add to the base trade count
        if(LCApi.isInAdminMode(player))
        {
            this.baseCount += 1;
            this.refactorTrades();
            return true;
        }
        //Otherwise check if we have any available offer upgrades we can consume
        if(this.getUnusedOfferUpgrades() > 0) {
            this.upgradeCount += 1;
            this.refactorTrades();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryRemoveTrade(Player player) {
        if(this.isClient())
            return false;
        //Cannot remove all trades
        if(this.trades.size() <= 1)
            return false;
        //If the player is an admin, remove from the base trade count
        if(LCApi.isInAdminMode(player) && this.baseCount > 1)
        {
            this.baseCount -= 1;
            this.refactorTrades();
            return true;
        }
        //Otherwise remove from the upgrade usage
        if(this.upgradeCount > 0)
        {
            this.upgradeCount -= 1;
            this.refactorTrades();
            return true;
        }
        return false;
    }

    @Override
    public void appendTerminalText(@Nullable Player player, Consumer<Component> builder) { this.appendTerminalTradeStatus(builder); }

    @Override
    public Optional<Integer> getNameColor() { return this.getTerminalTradeColor(); }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putInt("trade_count",this.baseCount);
        output.putInt("upgrade_count",this.upgradeCount);
    }

    @Override
    public void decodeSettings(ValueInput data, SettingsLoadContext context) {
        boolean countChanged = false;
        if(context.getPermission(LCPermissions.EDIT_TRADES)) {
            int newUpgradeCount = data.getIntOr("upgrade_count",0);
            if(this.upgradeCount < newUpgradeCount) {
                int addAmount = Math.min(newUpgradeCount - this.upgradeCount,this.getUnusedOfferUpgrades());
                if(addAmount > 0) {
                    this.upgradeCount += addAmount;
                    countChanged = true;
                }
            }
        }
        if(context.isAdminPlayer()) {
            int newBaseCount = data.getIntOr("trade_count",0);
            if(newBaseCount > this.baseCount) {
                this.baseCount = newBaseCount;
                countChanged = true;
            }
        }
        if(countChanged)
            this.refactorTrades();
    }

    @Override
    public void appendDisplay(ValueInput data, SettingsDisplayOutput output) {
        output.acceptTitle(this.getSettingsName());
        int totalCount = data.getIntOr("trade_count",0) + data.getIntOr("upgrade_count",0);
        output.acceptEntry(VALUE_TRADE_COUNT,totalCount);
    }

}
package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DemandPricing extends PriceTweakingTradeRule implements ICopySupportingRule {

    public static final TradeRuleType<DemandPricing> TYPE = new Type();

    private static final MapCodec<DemandPricing> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MoneyValue.CODEC.fieldOf("otherPrice").forGetter(DemandPricing::getOtherPrice),
            Codec.INT.fieldOf("smallStock").forGetter(DemandPricing::getSmallStock),
            Codec.INT.fieldOf("larseStock").forGetter(DemandPricing::getLargeStock),
            baseFields()
    ).apply(builder,DemandPricing::new));

    public static final int UPPER_STOCK_LIMIT = 100000;

    private MoneyValue otherPrice = MoneyValue.empty();
    
    public MoneyValue getOtherPrice() { return this.otherPrice; }
    public void setOtherPrice(MoneyValue otherPrice) { this.otherPrice = otherPrice; }
    private int smallStock = 1;
    public int getSmallStock() { return this.smallStock; }
    public void setSmallStock(int smallStock) { this.smallStock = MathUtil.clamp(smallStock, 1, this.largeStock - 1); }
    private int largeStock = 100;
    public int getLargeStock() { return this.largeStock; }
    public void setLargeStock(int largeStock) { this.largeStock = MathUtil.clamp(largeStock, this.smallStock + 1, UPPER_STOCK_LIMIT); }

    private DemandPricing() { }
    private DemandPricing(MoneyValue otherPrice,int smallStock,int largeStock,boolean active) {
        super(active);
        this.otherPrice = otherPrice;
        this.smallStock = smallStock;
        this.largeStock = largeStock;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, ISyncingContext context) {
        builder.setMoneyValue("price",this.otherPrice)
                .setInt("smallStock",this.smallStock)
                .setInt("largeStock",this.largeStock);
    }

    @Override
    public void decodeInternal(LazyPacketData data) {
        this.otherPrice = data.getMoneyValue("price");
        this.smallStock = data.getInt("smallStock");
        this.largeStock = data.getInt("largeStock");
    }

    @Override
    public boolean allowHost(@javax.annotation.Nullable ITradeRuleHost host) { return super.allowHost(host) && host.isTrade(); }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) {
        if(host instanceof TradeData trade && trade.getTradeDirection() != TradeDirection.SALE)
            return false;
        return super.canActivate(host);
    }

    
    @Override
    public IconData getIcon() { return IconUtil.ICON_DEMAND_PRICING; }

    @Override
    protected boolean onlyAllowOnTrades() { return true; }

    public Component getInfo()
    {
        if(this.getHost() instanceof TradeData trade)
        {
            if(trade.getTradeDirection() != TradeDirection.SALE)
                return LCText.GUI_DEMAND_PRICING_INFO_INVALID_HOST.get();
            else
            {
                MoneyValue smallPrice = trade.getCost();
                MoneyValue largePrice = this.otherPrice;
                if(largePrice.getCoreValue() < smallPrice.getCoreValue())
                {
                    MoneyValue temp = largePrice;
                    largePrice = smallPrice;
                    smallPrice = temp;
                }
                if(!largePrice.sameType(smallPrice) || !smallPrice.isValidPrice() || largePrice.isEmpty() || largePrice.getCoreValue() == smallPrice.getCoreValue())
                    return LCText.GUI_DEMAND_PRICING_INFO_INVALID_PRICE.get();
                if(this.largeStock <= this.smallStock)
                    return LCText.GUI_DEMAND_PRICING_INFO_INVALID_STOCK.get();
                else
                    return LCText.GUI_DEMAND_PRICING_INFO.get(largePrice.getText().copy().withStyle(ChatFormatting.WHITE),smallPrice.getText().copy().withStyle(ChatFormatting.WHITE),this.largeStock, this.smallStock);
            }
        }
        else
            return LCText.GUI_DEMAND_PRICING_INFO_INVALID_HOST.get();
    }

    public boolean isValid(TradeData trade, @Nullable TraderData trader) { return this.isValid(trade, trade.getCost(), trader); }
    public boolean isValid(TradeData trade, MoneyValue tradeCost, @Nullable TraderData trader)
    {
        if(trader == null)
            return false;
        MoneyValue smallPrice = tradeCost;
        MoneyValue largePrice = this.otherPrice;
        if(largePrice.getCoreValue() < smallPrice.getCoreValue())
        {
            MoneyValue temp = largePrice;
            largePrice = smallPrice;
            smallPrice = temp;
        }
        return !trader.hasInfiniteStock() && trade.getTradeDirection() == TradeDirection.SALE && largePrice.sameType(smallPrice) && smallPrice.isValidPrice() && !largePrice.isEmpty() && this.largeStock > this.smallStock && smallPrice.getCoreValue() != largePrice.getCoreValue();
    }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        if(this.isValid(event.getTrade(), event.getTrader()))
        {
            event.addNeutral(LCText.TRADE_RULE_DEMAND_PRICING.get());
        }
    }

    @Override
    protected void tradeBaseCost(InternalPriceEvent event) {
        if(this.isValid(event.trade, event.getBaseCost(), event.context.getTrader()))
        {
            int stock = event.trade.getStock(event.context);
            event.setBaseCost(this.calculatePrice(stock, event.getBaseCost()));
        }
    }

    
    private MoneyValue calculatePrice(int stock, MoneyValue defaultPrice)
    {
        //Sort prices
        //One price is sourced by the trade itself, the other is from this rule
        MoneyValue smallPrice = defaultPrice;
        MoneyValue largePrice = this.otherPrice;
        if(largePrice.getCoreValue() < smallPrice.getCoreValue())
        {
            MoneyValue temp = largePrice;
            largePrice = smallPrice;
            smallPrice = temp;
        }
        if(stock <= this.smallStock)
            return largePrice;
        if(stock >= this.largeStock)
            return smallPrice;
        long maxVal = largePrice.getCoreValue();
        long minVal = smallPrice.getCoreValue();
        long deltaVal = maxVal - minVal;
        if(deltaVal <= 0)
            return smallPrice;
        int deltaStock = this.largeStock - this.smallStock;
        if(deltaStock <= 0)
            return smallPrice;
        long result = maxVal - Math.round((double)deltaVal * ((double)(stock - this.smallStock)/(double)deltaStock));
        if(result >= maxVal)
            return largePrice;
        if(result <= minVal) //If small price is free and result <= 0, this will make it return free as well :)
            return smallPrice;
        return largePrice.fromCoreValue(result);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.otherPrice = MoneyValue.load(compound.getCompound("OtherPrice"));
        this.smallStock = compound.getInt("SmallStock");
        this.largeStock = compound.getInt("LargeStock");
    }

    @Override
    public void resetToDefaultState() {
        this.otherPrice = MoneyValue.empty();
        this.smallStock = 1;
        this.largeStock = 100;
    }

    @Override
    public void writeSettings(SavedSettingData.MutableNodeAccess node) {
        node.setCompoundValue("other_price",this.otherPrice.save());
        node.setIntValue("small_stock",this.smallStock);
        node.setIntValue("large_stock",this.largeStock);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess node) {
        this.otherPrice = MoneyValue.load(node.getCompoundValue("other_price"));
        this.smallStock = node.getIntValue("small_stock");
        this.largeStock = node.getIntValue("large_stock");
    }

    @Override
    protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
        if(updateInfo.contains("ChangePrice"))
            this.otherPrice = updateInfo.getMoneyValue("ChangePrice");
        if(updateInfo.contains("ChangeSmallStock"))
            this.smallStock = updateInfo.getInt("ChangeSmallStock");
        if(updateInfo.contains("ChangeLargeStock"))
            this.largeStock = updateInfo.getInt("ChangeLargeStock");
    }

    private static class Type extends TradeRuleType<DemandPricing>
    {
        @Override
        public DemandPricing create() { return new DemandPricing(); }
        @Override
        public MapCodec<DemandPricing> mapCodec() { return MAP_CODEC; }
    }

}

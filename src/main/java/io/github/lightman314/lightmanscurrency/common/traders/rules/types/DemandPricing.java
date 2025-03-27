package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.DemandPricingTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class DemandPricing extends PriceTweakingTradeRule {

    public static final TradeRuleType<DemandPricing> TYPE = new TradeRuleType<>(new ResourceLocation(LightmansCurrency.MODID,"demand_pricing"),DemandPricing::new);

    public static final int UPPER_STOCK_LIMIT = 100000;

    private MoneyValue otherPrice = MoneyValue.empty();
    @Nonnull
    public MoneyValue getOtherPrice() { return this.otherPrice; }
    public void setOtherPrice(@Nonnull MoneyValue otherPrice) { this.otherPrice = otherPrice; }
    private int smallStock = 1;
    public int getSmallStock() { return this.smallStock; }
    public void setSmallStock(int smallStock) { this.smallStock = MathUtil.clamp(smallStock, 1, this.largeStock - 1); }
    private int largeStock = 100;
    public int getLargeStock() { return this.largeStock; }
    public void setLargeStock(int largeStock) { this.largeStock = MathUtil.clamp(largeStock, this.smallStock + 1, UPPER_STOCK_LIMIT); }

    private DemandPricing() { super(TYPE); }

    @Override
    protected boolean allowHost(@Nonnull ITradeRuleHost host) { return super.allowHost(host) && host.isTrade(); }

    @Override
    protected boolean canActivate(@Nullable ITradeRuleHost host) {
        if(host instanceof TradeData trade && trade.getTradeDirection() != TradeDirection.SALE)
            return false;
        return super.canActivate(host);
    }

    @Override
    @Nonnull
    public IconData getIcon() { return IconUtil.ICON_DEMAND_PRICING; }

    @Override
    protected boolean onlyAllowOnTrades() { return true; }

    @Nonnull
    public MutableComponent getInfo()
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
                    return LCText.GUI_DEMAND_PRICING_INFO.get(largePrice.getText().withStyle(ChatFormatting.WHITE),smallPrice.getText().withStyle(ChatFormatting.WHITE),this.largeStock, this.smallStock);
            }
        }
        else
            return LCText.GUI_DEMAND_PRICING_INFO_INVALID_HOST.get();
    }

    public boolean isValid(@Nonnull TradeData trade, @Nullable TraderData trader) { return this.isValid(trade, trade.getCost(), trader); }
    public boolean isValid(@Nonnull TradeData trade, @Nonnull MoneyValue tradeCost, @Nullable TraderData trader)
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
        return !trader.isCreative() && trade.getTradeDirection() == TradeDirection.SALE && largePrice.sameType(smallPrice) && smallPrice.isValidPrice() && !largePrice.isEmpty() && this.largeStock > this.smallStock && smallPrice.getCoreValue() != largePrice.getCoreValue();
    }

    @Override
    public void beforeTrade(@Nonnull TradeEvent.PreTradeEvent event) {
        if(this.isValid(event.getTrade(), event.getTrader()))
        {
            event.addNeutral(LCText.TRADE_RULE_DEMAND_PRICING.get());
        }
    }

    @Override
    protected void tradeBaseCost(@Nonnull InternalPriceEvent event) {
        if(this.isValid(event.trade, event.getBaseCost(), event.context.getTrader()))
        {
            int stock = event.trade.getStock(event.context);
            event.setBaseCost(this.calculatePrice(stock, event.getBaseCost()));
        }
    }

    @Nonnull
    private MoneyValue calculatePrice(int stock, @Nonnull MoneyValue defaultPrice)
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
        long result = maxVal - Math.round((double)deltaVal * ((double)(stock - this.smallStock)/ (double)deltaStock));
        if(result >= maxVal)
            return largePrice;
        if(result <= minVal) //If small price is free and result <= 0, this will make it return free as well :)
            return smallPrice;
        return largePrice.fromCoreValue(result);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound) {
        compound.put("OtherPrice", this.otherPrice.save());
        compound.putInt("SmallStock", this.smallStock);
        compound.putInt("LargeStock", this.largeStock);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound) {
        this.otherPrice = MoneyValue.load(compound.getCompound("OtherPrice"));
        this.smallStock = compound.getInt("SmallStock");
        this.largeStock = compound.getInt("LargeStock");
    }

    @Override
    public JsonObject saveToJson(@Nonnull JsonObject json) { return null; }

    @Override
    public void loadFromJson(@Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException { throw new JsonSyntaxException("DemandPricing is not supported for Persistent Traders"); }

    @Override
    public CompoundTag savePersistentData() { return null; }

    @Override
    public void loadPersistentData(@Nonnull CompoundTag data) { }

    @Override
    protected void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
        if(updateInfo.contains("ChangePrice"))
            this.otherPrice = updateInfo.getMoneyValue("ChangePrice");
        if(updateInfo.contains("ChangeSmallStock"))
            this.smallStock = updateInfo.getInt("ChangeSmallStock");
        if(updateInfo.contains("ChangeLargeStock"))
            this.largeStock = updateInfo.getInt("ChangeLargeStock");
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new DemandPricingTab(parent); }

}
package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminSettingProvider;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleHolder;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class DemandPricing extends PriceModifyingTradeRule {

    public static final int UPPER_STOCK_LIMIT = 100000;

    private static final MapCodec<DemandPricing> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            MoneyValue.CODEC.fieldOf("otherPrice").forGetter(DemandPricing::getOtherPrice),
            Codec.intRange(1,UPPER_STOCK_LIMIT - 1).fieldOf("smallStock").forGetter(DemandPricing::getSmallStock),
            Codec.intRange(2,UPPER_STOCK_LIMIT).fieldOf("largeStock").forGetter(DemandPricing::getLargeStock)
    ).apply(builder,DemandPricing::new));
    public static final TradeRuleType<DemandPricing> TYPE = new TradeRuleType<>(MAP_CODEC,DemandPricing::new);

    public static final TextEntry GUI_INFO_INVALID_HOST = TextEntry.tradeRuleGui(TYPE,"info.invalid_host");
    public static final TextEntry GUI_INFO_INVALID_PRICE = TextEntry.tradeRuleGui(TYPE,"info.invalid_price");
    public static final TextEntry GUI_INFO_INVALID_STOCK = TextEntry.tradeRuleGui(TYPE,"info.invalid_stock");
    public static final TextEntry GUI_INFO = TextEntry.tradeRuleGui(TYPE,"info");

    public static final TextEntry GUI_SMALL_STOCK = TextEntry.tradeRuleGui(TYPE,"small_stock");
    public static final TextEntry GUI_LARGE_STOCK = TextEntry.tradeRuleGui(TYPE,"large_stock");

    public static final TextEntry MESSAGE_INFO = TextEntry.tradeRuleMessage(TYPE,"info");

    private MoneyValue otherPrice = MoneyValue.empty();
    public MoneyValue getOtherPrice() { return this.otherPrice; }
    private void setOtherPrice(MoneyValue newValue) {
        if(this.otherPrice.equals(newValue))
            return;
        this.otherPrice = newValue;
        this.setChanged(p -> p.set("otherPrice",LCFancyPacketTypes.MONEY,this.otherPrice));
    }

    private int smallStock = 1;
    public int getSmallStock() { return this.smallStock; }
    private void setSmallStock(int smallStock) {
        int newValue = Math.clamp(smallStock,1,this.largeStock - 1);
        if(this.smallStock != newValue) {
            this.smallStock = newValue;
            this.setChanged(p -> p.setInt("smallStock",this.smallStock));
        }
    }
    private int largeStock = 100;
    public int getLargeStock() { return this.largeStock; }
    private void setLargeStock(int largeStock) {
        int newValue = Math.clamp(largeStock,this.smallStock + 1,UPPER_STOCK_LIMIT);
        if(this.largeStock != newValue) {
            this.largeStock = newValue;
            this.setChanged(p -> p.setInt("largeStock",this.largeStock));
        }
    }

    private DemandPricing() {}
    private DemandPricing(boolean active,MoneyValue otherPrice,int smallStock,int largeStock) {
        super(active);
        this.otherPrice = otherPrice;
        this.smallStock = smallStock;
        this.largeStock = largeStock;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/demand_pricing")); }

    @Override
    protected boolean allowHolder(TradeRuleHolder holder) { return super.allowHolder(holder) && holder.isSingleTrade(); }

    @Override
    protected boolean canActivate(TradeRuleHolder holder) {
        if(holder instanceof TradeData trade && !trade.isSale())
            return false;
        return super.canActivate(holder);
    }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.otherPrice == MoneyValue.empty() && this.smallStock == 1 && this.largeStock == 100; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.set("otherPrice", LCFancyPacketTypes.MONEY,this.otherPrice)
                .setInt("smallStock",this.smallStock)
                .setInt("largeStock",this.largeStock);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("otherPrice"))
            this.otherPrice = message.get("otherPrice",LCFancyPacketTypes.MONEY,MoneyValue.empty());
        if(message.contains("smallStock"))
            this.smallStock = message.getInt("smallStock");
        if(message.contains("largeStock"))
            this.largeStock = message.getInt("largeStock");
    }

    public Component getInfo() {
        if(this.getHolder() instanceof TradeData trade) {
            if(!trade.isSale())
                return GUI_INFO_INVALID_HOST.get();
            else {
                TradePrice price = trade.getInternalPrice();
                //Price is invalid if the trade is not using the MoneyPrice
                if(!(price instanceof MoneyPrice mp))
                    return GUI_INFO_INVALID_PRICE.get();
                MoneyValue smallPrice = mp.getPrice();
                MoneyValue largePrice = this.otherPrice;
                if(largePrice.getInternalValue() < smallPrice.getInternalValue()) {
                    MoneyValue temp = largePrice;
                    largePrice = smallPrice;
                    smallPrice = temp;
                }
                if(!largePrice.compatibleTypes(smallPrice) || !smallPrice.isValidPrice() || largePrice.isEmpty() || largePrice.getInternalValue() == smallPrice.getInternalValue())
                    return GUI_INFO_INVALID_PRICE.get();
                if(this.largeStock <= this.smallStock)
                    return GUI_INFO_INVALID_STOCK.get();
                return GUI_INFO.get(largePrice.getText().copy().withStyle(ChatFormatting.WHITE),smallPrice.getText().copy().withStyle(ChatFormatting.WHITE),this.largeStock,this.smallStock);
            }
        }
        return GUI_INFO_INVALID_HOST.get();
    }

    public boolean isValid(TradeData trade,@Nullable INodeAccess trader) { return this.isValid(trade,trade.getInternalPrice(),trader); }
    public boolean isValid(TradeData trade,TradePrice tradePrice,@Nullable INodeAccess trader) {
        if(tradePrice instanceof MoneyPrice mp)
            return this.isValid(trade,mp.getPrice(),trader);
        return false;
    }
    public boolean isValid(TradeData trade,MoneyValue tradeCost,@Nullable INodeAccess trader) {
        if(trader == null)
            return false;
        MoneyValue smallPrice;
        MoneyValue largePrice;
        if(tradeCost.getInternalValue() < this.otherPrice.getInternalValue()) {
            smallPrice = tradeCost;
            largePrice = this.otherPrice;
        }
        else {
            smallPrice = this.otherPrice;
            largePrice = tradeCost;
        }
        return !IAdminSettingProvider.hasInfiniteStock(trader) && trade.isSale() && largePrice.compatibleTypes(smallPrice) && smallPrice.isValidPrice() && this.largeStock > this.smallStock && smallPrice.getInternalValue() < largePrice.getInternalValue();
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        if(this.isValid(event.getTrade(),event.getTrader()))
            event.addNeutral(MESSAGE_INFO.get());
    }

    @Override
    protected void tradeBaseCost(TradeEvent.BaseCost event) {
        if(this.isValid(event.getTrade(),event.getBaseCost(),event.getTrader())) {
            long stock = event.getTrade().getStock(event.getContext());
            event.setBaseCost(this.calculatePrice(stock,event.getBaseCost()));
        }
    }

    private TradePrice calculatePrice(long stock,TradePrice defaultPrice) {
        if(defaultPrice instanceof MoneyPrice mp) {
            return new MoneyPrice(this.calculatePrice(stock,mp.getPrice()));
        }
        return defaultPrice;
    }
    private MoneyValue calculatePrice(long stock,MoneyValue defaultPrice) {
        MoneyValue smallPrice;
        MoneyValue largePrice;
        if(defaultPrice.getInternalValue() < this.otherPrice.getInternalValue()) {
            smallPrice = defaultPrice;
            largePrice = this.otherPrice;
        }
        else {
            smallPrice = this.otherPrice;
            largePrice = defaultPrice;
        }
        if(stock <= this.smallStock)
            return largePrice;
        if(stock >= this.largeStock)
            return smallPrice;
        long maxVal = largePrice.getInternalValue();
        long minVal = smallPrice.getInternalValue();
        long deltaVal = maxVal - minVal;
        if(deltaVal <= 0)
            return smallPrice;
        int deltaStock = this.largeStock - this.smallStock;
        if(deltaStock <= 0)
            return smallPrice;
        long result = maxVal - Math.round((double)deltaVal * ((double)(stock - this.smallStock)/(double)deltaStock));
        if(result >= maxVal)
            return largePrice;
        if(result <= minVal)
            return smallPrice;
        return largePrice.fromInternalValue(result);
    }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("changePrice"))
            this.setOtherPrice(request.get("changePrice",LCFancyPacketTypes.MONEY));
        if(request.contains("changeSmallStock"))
            this.setSmallStock(request.getInt("changeSmallStock"));
        if(request.contains("changeLargeStock"))
            this.setLargeStock(request.getInt("changeLargeStock"));
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setOtherPrice(MoneyValue.empty());
        this.setSmallStock(1);
        this.setLargeStock(100);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("otherPrice",MoneyValue.CODEC,this.otherPrice);
        output.putInt("smallStock",this.smallStock);
        output.putInt("largeStock",this.largeStock);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.otherPrice = data.read("otherPrice",MoneyValue.CODEC).orElse(MoneyValue.empty());
        this.smallStock = data.getIntOr("smallStock",1);
        this.largeStock = data.getIntOr("largeStock",100);
    }

}

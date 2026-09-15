package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public abstract class TradePrice {

    public static final Codec<TradePrice> CODEC = LCRegistries.Trader.TRADE_PRICE_TYPE.byNameCodec()
            .dispatch(TradePrice::getType,TradePriceType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,TradePrice> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.TRADE_PRICE_TYPE_KEY)
            .dispatch(TradePrice::getType,TradePriceType::streamCodec);

    public static final TextEntry TOOLTIP_TRADE_EDIT_PRICE = TextEntry.tooltip(LCApi.MODID,"trade.edit_price");

    protected static final ImmutableSet<TradeDirection> SALE_ONLY_SET = ImmutableSet.of(TradeDirection.SALE);

    public static TradePrice empty() { return new MoneyPrice(MoneyValue.empty()); }

    private Runnable listener = () -> {};
    public final TradePrice copyWithListener(Runnable listener) { return this.copy().withListener(listener); }
    public final TradePrice withListener(Runnable listener) { this.listener = listener; return this; }

    public abstract TradePriceType<?> getType();

    public abstract TradePrice copy();

    public Set<TradeDirection> supportedTradeTypes() { return TradeDirection.SET; }

    public abstract boolean maySupportTrade(TraderData trader, TradeData trade);

    public boolean currentlySupportsTrade(TraderData trader, TradeData trade) {
        return this.maySupportTrade(trader,trade) && this.supportedTradeTypes().contains(trade.getDirection());
    }

    public boolean isValid(TraderData trader,TradeData trade) {
        return this.maySupportTrade(trader,trade) && this.supportedTradeTypes().contains(trade.getDirection()) && this.isValid();
    }

    protected abstract boolean isValid();

    protected final boolean hasInfiniteStock(TradeContext context) {
        return context.getAdminSettings().hasInfiniteStock();
    }
    protected final boolean shouldStoreInTrader(TradeContext context) {
        return context.getAdminSettings().shouldStorePrice();
    }

    public final TransferResult transferPrice(TradeDirection direction,TradeContext context) throws TradeFailedException {
        TransferResult result;
        if(direction.isSale())
        {
            //Transfer the price
            result = this.transferFromCustomerToTrader(context);
            if(result.failedToGive())
                throw new TradeFailedException(TradeResult.FAIL_NO_INPUT_SPACE);
            if(result.failedToTake())
                throw new TradeFailedException(TradeResult.FAIL_CANNOT_AFFORD);
        }
        else if(direction.isPurchase())
        {
            //Transfer the price
            result = this.transferFromTraderToCustomer(context);
            if(result.failedToGive())
                throw new TradeFailedException(TradeResult.FAIL_NO_OUTPUT_SPACE);
            if(result.failedToTake())
                throw new TradeFailedException(TradeResult.FAIL_OUT_OF_STOCK);
        }
        else
            throw new TradeFailedException("Cannot transfer the price in the 'Other' direction!",TradeResult.FAIL_INVALID_TRADE);
        return result;
    }
    public abstract TransferResult transferFromCustomerToTrader(TradeContext context) throws TradeFailedException;
    public abstract TransferResult transferFromTraderToCustomer(TradeContext context) throws TradeFailedException;

    public final boolean hasStock(TradeContext context) { return this.getAvailableStock(context) > 0; }
    public abstract long getAvailableStock(TradeContext context);

    public abstract boolean showOutOfSpaceWarning(TradeContext context);

    public abstract boolean showCannotAffordWarning(TradeContext context);

    public boolean isFree() { return false; }
    public abstract TradePrice percentageOfValue(int percentage);

    public abstract boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button, ItemStack heldItem, TradeEditContext context);
    public boolean onScrollInteraction(Player player,TradeData trade,TradeSlot slot,float scroll,ItemStack heldItem,TradeEditContext context) { return false; }

    public abstract void handleCustomEditMessage(FancyPacketMap packet,TradeSlot slot);

    public abstract Component getNotificationText();

    public final void setChanged() { this.listener.run(); }

    public static final class TransferResult {

        public static final TransferResult SUCCESS = new TransferResult(Status.SUCCESS,MoneyValue.empty(),null);
        public static final TransferResult FAILED_TO_TAKE = new TransferResult(Status.FAILED_TO_TAKE,MoneyValue.empty(),null);
        public static final TransferResult FAILED_TO_GIVE = new TransferResult(Status.FAILED_TO_GIVE,MoneyValue.empty(),null);

        public static TransferResult taxedSuccess(TradePriceReceipt receipt,MoneyValue taxesPaid) { return new TransferResult(Status.SUCCESS,taxesPaid,receipt); }
        public static TransferResult success(TradePriceReceipt receipt) { return new TransferResult(Status.SUCCESS,MoneyValue.empty(),receipt); }

        private final Status status;
        public boolean isSuccess() { return this.status == Status.SUCCESS; }
        public boolean failedToGive() { return this.status == Status.FAILED_TO_GIVE; }
        public boolean failedToTake() { return this.status == Status.FAILED_TO_TAKE; }

        private final MoneyValue taxesPaid;
        public MoneyValue getTaxesPaid() { return this.taxesPaid; }

        private final TradePriceReceipt receipt;
        public TradePriceReceipt getReceipt() { return this.receipt; }

        private TransferResult(Status status,MoneyValue taxesPaid,TradePriceReceipt receipt) {
            this.status = status;
            this.taxesPaid = taxesPaid;
            this.receipt = receipt;
        }

        private enum Status { SUCCESS,FAILED_TO_GIVE,FAILED_TO_TAKE }

    }

}
package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminRuleProvider;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class TradePrice {

    public static final Codec<TradePrice> CODEC = LCRegistries.Trader.TRADE_PRICE_TYPE.byNameCodec()
            .dispatch(TradePrice::getType,TradePriceType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,TradePrice> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.TRADE_PRICE_TYPE_KEY)
            .dispatch(TradePrice::getType,TradePriceType::streamCodec);

    private Runnable listener = () -> {};
    public final TradePrice withListener(Runnable listener) { this.listener = listener; return this; }

    public abstract TradePriceType<?> getType();

    public abstract boolean supportsTrade(TraderData trader,TradeData trade);

    public boolean isValid(TraderData trader,TradeData trade) {
        return this.supportsTrade(trader,trade) && this.isValid();
    }

    protected abstract boolean isValid();

    protected final boolean hasInfiniteStock(TradeContext context) {
        return IAdminRuleProvider.hasInfiniteStock(context.getTrader());
    }
    protected final boolean shouldStoreInTrader(TradeContext context) {
        return IAdminRuleProvider.shouldStorePrice(context.getTrader());
    }

    public final TransferResult transferPrice(TradeDirection direction, TradeContext context) throws TradeFailedException {
        if(direction.isOther())
            throw new TradeFailedException("Cannot transfer the price in the 'Other' direction!",TradeResult.FAIL_INVALID_TRADE);
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
        else
        {
            //Transfer the price
            result = this.transferFromTraderToCustomer(context);
            if(result.failedToGive())
                throw new TradeFailedException(TradeResult.FAIL_NO_OUTPUT_SPACE);
            if(result.failedToTake())
                throw new TradeFailedException(TradeResult.FAIL_OUT_OF_STOCK);
        }
        return result;
    }
    public abstract TransferResult transferFromCustomerToTrader(TradeContext context);
    public abstract TransferResult transferFromTraderToCustomer(TradeContext context);

    public final boolean hasStock(TradeContext context) { return this.getAvailableStock(context) > 0; }
    public abstract long getAvailableStock(TradeContext context);

    public boolean isFree() { return false; }
    public abstract TradePrice percentageOfValue(int percentage);

    public abstract boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button, ItemStack heldItem, TradeEditContext context);
    public boolean onScrollInteraction(Player player,TradeData trade,TradeSlot slot,float scroll,ItemStack heldItem,TradeEditContext context) { return false; }

    public abstract void handleCustomEditMessage(FancyPacketMap packet);

    public final void setChanged() { this.listener.run(); }

    public static final class TransferResult {

        public static final TransferResult SUCCESS = new TransferResult(Status.SUCCESS,MoneyValue.empty(),List.of());
        public static final TransferResult FAILED_TO_TAKE = new TransferResult(Status.FAILED_TO_TAKE,MoneyValue.empty(),List.of());
        public static final TransferResult FAILED_TO_GIVE = new TransferResult(Status.FAILED_TO_GIVE,MoneyValue.empty(),List.of());

        public static TransferResult taxedSuccess(MoneyValue taxesPaid) { return new TransferResult(Status.SUCCESS,taxesPaid,List.of()); }
        public static TransferResult contextSuccess(List<?> additionalContext) { return new TransferResult(Status.SUCCESS,MoneyValue.empty(),ImmutableList.copyOf(additionalContext)); }
        public static TransferResult taxedAndContextSuccess(MoneyValue taxesPaid,List<?> additionalContext) { return new TransferResult(Status.SUCCESS,taxesPaid,ImmutableList.copyOf(additionalContext)); }

        private final Status status;
        public boolean isSuccess() { return this.status == Status.SUCCESS; }
        public boolean failedToGive() { return this.status == Status.FAILED_TO_GIVE; }
        public boolean failedToTake() { return this.status == Status.FAILED_TO_TAKE; }

        private final MoneyValue taxesPaid;
        public MoneyValue getTaxesPaid() { return this.taxesPaid; }

        private final List<?> additionalContext;
        public List<?> getAdditionalContext() { return this.additionalContext; }

        private TransferResult(Status status, MoneyValue taxesPaid,List<?> additionalContext) {
            this.status = status;
            this.taxesPaid = taxesPaid;
            this.additionalContext = additionalContext;
        }

        private enum Status { SUCCESS,FAILED_TO_GIVE,FAILED_TO_TAKE }

    }

}
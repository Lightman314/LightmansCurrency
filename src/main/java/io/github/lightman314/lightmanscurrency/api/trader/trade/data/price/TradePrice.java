package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class TradePrice {

    public static final Codec<TradePrice> CODEC = LCRegistries.Trader.TRADE_PRICE_TYPE.byNameCodec()
            .dispatch(TradePrice::getType,TradePriceType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,TradePrice> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.TRADE_PRICE_TYPE_KEY)
            .dispatch(TradePrice::getType,TradePriceType::streamCodec);

    public abstract TradePriceType<?> getType();

    public boolean supportsPurchases() { return true; }

    public abstract boolean isValid(TradeDirection direction);

    /**
     * Process the payment from the customer
     * @param context The context of the trade, including the customers available resources
     * @return Whether the payment was successful. Return {@code false} if the payment could not be taken.
     */
    public boolean takeFromCustomer(TradeContext context) { return this.takeFromSource(ResourceSource.CUSTOMER,context); }
    /**
     * Takes the payment from the trader<br>
     * Primarily used for trades of type {@link TradeDirection#PURCHASE} to take the payment from the trader to the customer
     * @param context
     * @return
     */
    public boolean takeFromTrader(TradeContext context) { return this.takeFromSource(ResourceSource.TRADER,context); }
    protected abstract boolean takeFromSource(ResourceSource source,TradeContext context);

    public boolean giveToCustomer(TradeContext context) { return this.giveToSource(ResourceSource.CUSTOMER,context); }
    public boolean giveToTrader(TradeContext context) { return this.giveToSource(ResourceSource.TRADER,context); }
    protected abstract boolean giveToSource(ResourceSource source,TradeContext context);

    public final boolean hasStock(TradeContext context) { return this.getAvailableStock(context) > 0; }
    public abstract long getAvailableStock(TradeContext context);

    public boolean isFree() { return false; }
    public abstract TradePrice percentageOfValue(int percentage);

    //TODO add button shenanigans

    public boolean onClickInteraction(int index, Player player, ItemStack heldItem, int button, TradeEditContext context) { return false; }
    public boolean onScrollInteraction(int index,Player player,ItemStack heldItem,float scroll,TradeEditContext context) { return false; }

}
package io.github.lightman314.lightmanscurrency.api.coins.atm.commands.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommand;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommandType;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ExchangeDirection;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import net.minecraft.IdentifierException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class ExchangeCommand extends ATMCommand {

    public static final ATMCommandType<ExchangeCommand> TYPE = new Type();
    private static final StreamCodec<RegistryFriendlyByteBuf,ExchangeCommand> SC = StreamCodec.composite(
            ExchangeDirection.STREAM_CODEC,c -> c.direction,
            ByteBufCodecs.holderRegistry(BuiltInRegistries.ITEM.key()),c -> c.coin,
            ExchangeCommand::new);

    private final ExchangeDirection direction;
    private final Holder<Item> coin;
    public ExchangeCommand(ExchangeDirection direction,Holder<Item> coin) { this.direction = direction; this.coin = coin; }

    @Override
    public boolean execute(ResourceHandler<ItemResource> itemResourceHandler, @Nullable Transaction transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            try {
                Item c = this.coin.value();
                if(this.direction.isUp())
                    return LCApi.getCoinAPI().exchangeCoinsUp(itemResourceHandler,c,transaction);
                else
                    return LCApi.getCoinAPI().exchangeCoinsDown(itemResourceHandler,c,transaction);
            } catch (IllegalStateException ignored) { } //Catch Illegal State Exceptions just in case the holder isn't bound/valid for some odd reason
        }
        return false;
    }

    @Override
    public ATMCommandType<?> getType() { return TYPE; }

    @Override
    protected void writeAdditional(JsonObject json, DataContext<JsonElement> context) {
        json.addProperty("direction",this.direction.toString());
        json.add("coin",context.write(this.coin,BuiltInRegistries.ITEM.holderByNameCodec()));
    }

    private static class Type implements ATMCommandType<ExchangeCommand>
    {
        @Override
        public ATMCommand parse(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
            ExchangeDirection direction = EnumHelper.getAsEnum(json,"direction",ExchangeDirection.class,"Exchange Direction");
            Holder<Item> coin = context.readOrThrow(json.get("coin"),BuiltInRegistries.ITEM.holderByNameCodec());
            return new ExchangeCommand(direction,coin);
        }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ExchangeCommand> streamCodec() { return SC; }
    }

}

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
import io.netty.buffer.ByteBuf;
import net.minecraft.IdentifierException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class ExchangeAllCommand extends ATMCommand {

    public static final ATMCommandType<?> TYPE = new Type();
    private static final StreamCodec<ByteBuf,ExchangeAllCommand> STREAM_CODEC = StreamCodec.composite(
            ExchangeDirection.STREAM_CODEC,c -> c.direction,
            ByteBufCodecs.INT,c -> c.attempts,
            ExchangeAllCommand::new);

    private final ExchangeDirection direction;
    private final int attempts;
    public ExchangeAllCommand(ExchangeDirection direction,int attempts) { this.direction = direction; this.attempts = attempts; }

    @Override
    public boolean execute(ResourceHandler<ItemResource> itemResourceHandler, @Nullable Transaction transaction) {
        try(Transaction tx = Transaction.open(transaction)) {
            if(LCApi.getCoinAPI().dataNotLoaded())
                return false;
            for(int i = 0; i < this.attempts; ++i)
            {
                if(this.direction.isUp())
                    LCApi.getCoinAPI().exchangeCoinsAllUp(itemResourceHandler,tx);
                else
                    LCApi.getCoinAPI().exchangeCoinsAllDown(itemResourceHandler,tx);
            }
            tx.commit();
            return true;
        }
    }

    @Override
    public ATMCommandType<?> getType() { return TYPE; }

    @Override
    protected void writeAdditional(JsonObject json, DataContext<JsonElement> context) {
        json.addProperty("direction",this.direction.toString());
        json.addProperty("attempts",this.attempts);
    }

    private static class Type implements ATMCommandType<ExchangeAllCommand>
    {
        @Override
        public ATMCommand parse(JsonObject json,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {
            ExchangeDirection direction = EnumHelper.getAsEnum(json,"direction",ExchangeDirection.class,"Exchange Direction");
            int attempts = GsonHelper.getAsInt(json,"attempts",1);
            return new ExchangeAllCommand(direction,attempts);
        }
        @Override
        public StreamCodec<ByteBuf,ExchangeAllCommand> streamCodec() { return STREAM_CODEC; }
    }

}

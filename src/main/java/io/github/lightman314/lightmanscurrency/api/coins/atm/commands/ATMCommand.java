package io.github.lightman314.lightmanscurrency.api.coins.atm.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import net.minecraft.IdentifierException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public abstract class ATMCommand {

    public static final StreamCodec<RegistryFriendlyByteBuf,ATMCommand> STREAM_CODEC = ByteBufCodecs
            .registry(LCRegistries.Coins.ATM_COMMAND_TYPE_KEY)
            .dispatch(ATMCommand::getType,ATMCommandType::streamCodec);

    public abstract boolean execute(ResourceHandler<ItemResource> itemResourceHandler, @Nullable Transaction transaction);

    public final JsonObject write(DataContext<JsonElement> context)
    {
        JsonObject json = new JsonObject();
        this.writeAdditional(json,context);
        json.addProperty("type", LCRegistries.Coins.ATM_COMMAND_TYPE.getKey(this.getType()).toString());
        return json;
    }

    public abstract ATMCommandType<?> getType();

    protected abstract void writeAdditional(JsonObject json,DataContext<JsonElement> context);

    public static ATMCommand parse(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException
    {
        Identifier type = Identifier.parse(GsonHelper.getAsString(json,"type"));
        ATMCommandType<?> commandType = LCRegistries.Coins.ATM_COMMAND_TYPE.getValue(type);
        if(commandType == null)
            throw new JsonSyntaxException(type + " is not a valid ATM Command type!");
        return commandType.parse(json,context);
    }

}

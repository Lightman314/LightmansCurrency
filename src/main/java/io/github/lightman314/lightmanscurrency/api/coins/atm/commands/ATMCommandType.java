package io.github.lightman314.lightmanscurrency.api.coins.atm.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import net.minecraft.IdentifierException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ATMCommandType<T extends ATMCommand> {

    ATMCommand parse(JsonObject json,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException;

    StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

}

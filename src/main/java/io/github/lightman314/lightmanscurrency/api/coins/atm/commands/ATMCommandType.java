package io.github.lightman314.lightmanscurrency.api.coins.atm.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.IdentifierException;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ATMCommandType<T extends ATMCommand> extends AbstractType<ATMCommandType<?>> {

    public abstract ATMCommand parse(JsonObject json, CodecInteractionHelper<JsonElement> context) throws JsonSyntaxException, IdentifierException;

    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    @Override
    protected final Registry<ATMCommandType<?>> getRegistry() { return LCRegistries.Coins.ATM_COMMAND_TYPE; }
    @Override
    protected final String getName() { return "ATMCommandType"; }

}

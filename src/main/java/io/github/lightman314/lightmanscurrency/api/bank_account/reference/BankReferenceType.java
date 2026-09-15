package io.github.lightman314.lightmanscurrency.api.bank_account.reference;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class BankReferenceType<T extends BankReference> extends AbstractType.Serializable<T,BankReferenceType<?>> {

    public BankReferenceType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<BankReferenceType<?>> getRegistry() { return LCRegistries.Bank.REFERENCE_TYPE; }
    @Override
    protected String getName() { return "BankReferenceType"; }

}

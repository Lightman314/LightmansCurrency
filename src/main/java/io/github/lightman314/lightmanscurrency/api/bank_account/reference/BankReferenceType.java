package io.github.lightman314.lightmanscurrency.api.bank_account.reference;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class BankReferenceType<T extends BankReference> {

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Bank.REFERENCE_TYPE,this); }
    @Override
    public String toString() { return RegistryHelper.toString("BankReferenceType",LCRegistries.Bank.REFERENCE_TYPE,this); }

}

package io.github.lightman314.lightmanscurrency.api.money.bank.reference;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class BankReferenceType<T extends BankReference> {

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    public abstract BankReference loadOldData(CompoundTag tag);

    @Override
    public int hashCode() { return LCRegistries.BANK_REFERENCE.getKey(this).hashCode(); }
    @Override
    public String toString() { return "BankReference[" + LCRegistries.BANK_REFERENCE.getKey(this) + "]"; }
}

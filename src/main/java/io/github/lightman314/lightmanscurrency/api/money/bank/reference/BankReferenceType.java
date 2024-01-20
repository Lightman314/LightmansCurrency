package io.github.lightman314.lightmanscurrency.api.money.bank.reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public abstract class BankReferenceType {

    public final ResourceLocation id;
    protected BankReferenceType(ResourceLocation id) { this.id = id; }

    public abstract BankReference load(CompoundTag tag);
    public abstract BankReference decode(FriendlyByteBuf buffer);


}

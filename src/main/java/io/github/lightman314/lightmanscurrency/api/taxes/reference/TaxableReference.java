package io.github.lightman314.lightmanscurrency.api.taxes.reference;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxable;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public abstract class TaxableReference {

    public static final Codec<TaxableReference> CODEC = Codec.withAlternative(
            //Desired Codec
            TaxReferenceType.CODEC.dispatch(t -> t.type,TaxReferenceType::codec),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(TaxableReference::loadOldData,"Taxable Reference"));
    public static final StreamCodec<ByteBuf,TaxableReference> STREAM_CODEC = TaxReferenceType.STREAM_CODEC.dispatch(TaxableReference::getType,TaxReferenceType::streamCodec);

    public final TaxReferenceType type;
    private TaxReferenceType getType() { return this.type; }
    protected TaxableReference(TaxReferenceType type) { this.type = type; }

    @Nullable
    public abstract ITaxable getTaxable(boolean isClient);

    public final boolean stillValid(boolean isClient) { return this.getTaxable(isClient) != null; }

    public final CompoundTag save() { return (CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow(); }

    public static TaxableReference load(CompoundTag tag) { return CODEC.decode(NbtOps.INSTANCE,tag).getOrThrow().getFirst(); }

    @Deprecated
    private static TaxableReference loadOldData(CompoundTag tag) {
        //Load old data
        ResourceLocation type = ResourceLocation.parse(tag.getString("Type"));
        TaxReferenceType t = TaxAPI.getApi().GetReferenceType(type);
        if(t != null)
            return t.load(tag);
        return null;
    }

    @Override
    public final boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof TaxableReference reference)
            return this.matches(reference);
        return false;
    }

    protected abstract boolean matches(TaxableReference otherReference);
}

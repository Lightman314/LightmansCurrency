package io.github.lightman314.lightmanscurrency.api.taxes.reference;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public abstract class TaxReferenceType {

    public static final Codec<TaxReferenceType> CODEC = CodecHelper.byNameCodec(id -> TaxAPI.getApi().GetReferenceType(id), t -> t.typeID,"Tax Reference Type");
    public static final StreamCodec<ByteBuf,TaxReferenceType> STREAM_CODEC = StreamHelper.byNameCodec(id -> TaxAPI.getApi().GetReferenceType(id), t -> t.typeID);

    public final ResourceLocation typeID;
    protected TaxReferenceType(ResourceLocation typeID) { this.typeID = typeID; }

    @Deprecated
    public abstract TaxableReference load(CompoundTag tag);

    public abstract MapCodec<? extends TaxableReference> codec();
    public abstract StreamCodec<ByteBuf,? extends TaxableReference> streamCodec();

}

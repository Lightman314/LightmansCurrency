package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class SecurityUpgradeData {

    public static final SecurityUpgradeData DEFAULT = new SecurityUpgradeData(false,new CompoundTag());
    public static final Codec<SecurityUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("BreakIsValid").forGetter(d -> d.breakIsValid),
                    CompoundTag.CODEC.fieldOf("Owner").forGetter(d -> d.ownerTag)
            ).apply(builder,SecurityUpgradeData::new)
    );
    public static final StreamCodec<FriendlyByteBuf,SecurityUpgradeData> STREAM_CODEC = StreamCodec.of((b,d) -> {
        b.writeBoolean(d.breakIsValid);
        b.writeNbt(d.ownerTag);
    }, b -> new SecurityUpgradeData(b.readBoolean(),b.readNbt()));

    public final boolean breakIsValid;
    private final CompoundTag ownerTag;
    private SecurityUpgradeData(boolean breakIsValid,@Nonnull CompoundTag ownerTag) { this.breakIsValid = breakIsValid; this.ownerTag = ownerTag; }

    @Nonnull
    public OwnerData parseData(@Nonnull CoinChestBlockEntity be)
    {
        OwnerData data = new OwnerData(be);
        if(this.ownerTag.isEmpty())
            return data;
        data.load(this.ownerTag,be.registryAccess());
        return data;
    }

    @Nonnull
    public SecurityUpgradeData withBreakIsValid(boolean newValue) { return new SecurityUpgradeData(newValue,this.ownerTag); }
    @Nonnull
    public SecurityUpgradeData withOwner(@Nonnull OwnerData data, @Nonnull HolderLookup.Provider lookup) { return new SecurityUpgradeData(this.breakIsValid,data.save(lookup)); }

    @Override
    public int hashCode() { return Objects.hash(this.breakIsValid,this.ownerTag); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SecurityUpgradeData other)
            return other.breakIsValid == this.breakIsValid && other.ownerTag.equals(this.ownerTag);
        return false;
    }

}

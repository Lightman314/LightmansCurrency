package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public final class SecurityUpgradeData {

    public static final SecurityUpgradeData DEFAULT = new SecurityUpgradeData(false,new OwnerData());
    public static final Codec<SecurityUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("BreakIsValid").forGetter(d -> d.breakIsValid),
                    OwnerData.CODEC.fieldOf("Owner").forGetter(d -> d.owner)
            ).apply(builder,SecurityUpgradeData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,SecurityUpgradeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,d -> d.breakIsValid,
            OwnerData.STREAM_CODEC,d -> d.owner,
            SecurityUpgradeData::new);

    public final boolean breakIsValid;
    public final OwnerData owner;
    private SecurityUpgradeData(boolean breakIsValid,OwnerData owner) { this.breakIsValid = breakIsValid; this.owner = owner; }

    public SecurityUpgradeData withBreakIsValid(boolean newValue) { return new SecurityUpgradeData(newValue,this.owner); }
    public SecurityUpgradeData withOwner(OwnerData data) { return new SecurityUpgradeData(this.breakIsValid,data); }

    @Override
    public int hashCode() { return Objects.hash(this.breakIsValid,this.owner); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SecurityUpgradeData other)
            return other.breakIsValid == this.breakIsValid && other.owner.equals(this.owner);
        return false;
    }

}

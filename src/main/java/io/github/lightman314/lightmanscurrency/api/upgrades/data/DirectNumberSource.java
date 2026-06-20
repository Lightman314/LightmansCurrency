package io.github.lightman314.lightmanscurrency.api.upgrades.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Objects;

public class DirectNumberSource extends NumberSource {

    public static final NumberSourceType<DirectNumberSource> TYPE = new NumberSourceType<>(Codec.DOUBLE.fieldOf("value").xmap(DirectNumberSource::new,DirectNumberSource::get),ByteBufCodecs.DOUBLE.map(DirectNumberSource::new,DirectNumberSource::get));

    private final double value;
    public DirectNumberSource(double value) { this.value = value; }

    @Override
    public NumberSourceType<?> getType() { return TYPE; }

    @Override
    public double get() { return this.value; }

    @Override
    protected boolean equals(NumberSource source) { return source instanceof DirectNumberSource ds && ds.value == this.value; }

    @Override
    public int hashCode() { return Objects.hash(this.value); }

}

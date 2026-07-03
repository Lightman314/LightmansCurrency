package io.github.lightman314.lightmanscurrency.api.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import javax.annotation.Nullable;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;

@Immutable
public final class WorldPosition {

    public static final WorldPosition VOID = new WorldPosition(Optional.empty(),BlockPos.ZERO);

    public static final Codec<WorldPosition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(WorldPosition::getDim),
            BlockPos.CODEC.fieldOf("pos").forGetter(WorldPosition::getPos)
    ).apply(builder,WorldPosition::parse));

    //Manual stream codec, because why encode the block position if the dimension is void anyway? :shrug:
    public static final StreamCodec<ByteBuf,WorldPosition> STREAM_CODEC = StreamCodec.of((buf,pos) -> {
        buf.writeBoolean(pos.isVoid());
        if(!pos.isVoid())
        {
            Identifier.STREAM_CODEC.encode(buf,pos.dimension.identifier());
            BlockPos.STREAM_CODEC.encode(buf,pos.pos);
        }
    },buf -> {
        if(buf.readBoolean())
            return VOID;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION,Identifier.STREAM_CODEC.decode(buf));
        return of(key,BlockPos.STREAM_CODEC.decode(buf));
    });

    @Nullable
    private final ResourceKey<Level> dimension;
    @Nullable
    public ResourceKey<Level> getDimension() { return this.dimension; }
    private Optional<ResourceKey<Level>> getDim() { return Optional.ofNullable(this.dimension); }
    public boolean sameDimension(WorldPosition other) {
        if(this.isVoid() || other.isVoid())
            return true;
        return this.dimension == other.dimension;
    }
    public boolean sameDimension(Level level) {
        if(this.isVoid())
            return true;
        return this.dimension == level.dimension();
    }
    public boolean isVoid() { return this.dimension == null; }
    private final BlockPos pos;
    public BlockPos getPos() { return this.pos; }

    private WorldPosition(Optional<ResourceKey<Level>> dimension,BlockPos pos) {
        this.dimension = dimension.orElse(null);
        this.pos = pos.immutable();
    }

    private static WorldPosition parse(Optional<ResourceKey<Level>> dimension,BlockPos pos) { return of(dimension.orElse(null),pos); }

    public static WorldPosition of(@Nullable ResourceKey<Level> dimension,BlockPos pos) {
        if(dimension == null)
            return VOID;
        return new WorldPosition(Optional.of(dimension),pos);
    }

    public static WorldPosition ofLevel(@Nullable Level level,BlockPos pos) { return of(level == null ? null : level.dimension(),pos); }

    public static WorldPosition ofBE(BlockEntity be) { return ofLevel(be.getLevel(),be.getBlockPos()); }

    @Override
    public int hashCode() { return Objects.hash(this.dimension == null ? 0 : this.dimension.identifier(),this.pos); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof WorldPosition other)
        {
            if(this.isVoid() || other.isVoid())
                return this.isVoid() == other.isVoid();
            //At this point, neither is void so a dimension is present
            return other.dimension == this.dimension && other.pos.equals(this.pos);
        }
        return false;
    }

    @Override
    public String toString() {
        if(this.dimension == null)
            return "WorldPosition[VOID]";
        return "WorldPosition[" + this.dimension.identifier() + ";" + this.pos.toShortString() + "]";
    }
}
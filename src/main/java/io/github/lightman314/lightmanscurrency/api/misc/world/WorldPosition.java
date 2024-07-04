package io.github.lightman314.lightmanscurrency.api.misc.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldPosition {

    public static final WorldPosition VOID = new WorldPosition(null, BlockPos.ZERO);

    private final ResourceKey<Level> dimension;
    @Nullable
    public final ResourceKey<Level> getDimension() { return this.dimension; }
    public final boolean sameDimension(@Nonnull WorldPosition other) { if(this.isVoid() || other.isVoid()) return true; return this.dimension.equals(other.dimension); }
    public final boolean sameDimension(@Nonnull Level level) { if(this.isVoid()) return true; return this.dimension.equals(level.dimension()); }
    public final boolean isVoid() { return this.dimension == null; }
    private final BlockPos pos;
    @Nonnull
    public BlockPos getPos() { return this.pos; }

    public final WorldArea getArea(int horizRadius, int vertSize, int vertOffset) { return WorldArea.of(this, horizRadius, vertSize, vertOffset); }

    private WorldPosition(@Nullable ResourceKey<Level> dimension, @Nonnull BlockPos pos) { this.dimension = dimension; this.pos = pos; }

    @Nonnull
    public static WorldPosition of(@Nullable ResourceKey<Level> dimension, @Nullable BlockPos pos) { if(dimension == null) return VOID; return new WorldPosition(dimension, pos != null ? pos : BlockPos.ZERO); }
    @Nonnull
    public static WorldPosition ofLevel(@Nullable Level level, @Nullable BlockPos pos) { return of(level != null ? level.dimension() : null, pos); }
    @Nonnull
    public static WorldPosition ofBE(@Nullable BlockEntity be) { if(be == null) return VOID; return ofLevel(be.getLevel(), be.getBlockPos()); }

    @Nonnull
    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        if(this.dimension == null)
            return tag;
        tag.putString("Dimension", this.dimension.location().toString());
        tag.putInt("X", this.pos.getX());
        tag.putInt("Y", this.pos.getY());
        tag.putInt("Z", this.pos.getZ());
        return tag;
    }

    @Nonnull
    public static WorldPosition load(@Nonnull CompoundTag tag)
    {
        if(tag.contains("Dimension"))
        {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dimension")));
            BlockPos pos = new BlockPos(tag.getInt("X"),tag.getInt("Y"),tag.getInt("Z"));
            return of(dimension, pos);
        }
        return WorldPosition.VOID;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof WorldPosition other)
        {
            if(other == this)
                return true;
            if(other.isVoid() != this.isVoid())
                return false;
            if(this.isVoid())
                return true;
            return other.dimension.equals(this.dimension) && other.pos.equals(this.pos);
        }
        return false;
    }
}

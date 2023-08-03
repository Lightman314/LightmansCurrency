package io.github.lightman314.lightmanscurrency.common.taxes.data;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldArea {

    public static final WorldArea VOID = new WorldArea(null, BlockPos.ZERO, 0, 0, 0);
    public static final WorldArea INFINITE_VOID = new WorldArea(null, BlockPos.ZERO, 0, 0, 0);

    private final WorldPosition center;
    public final WorldPosition getCenter() { return this.center; }
    public final boolean isVoid() { return this.center.isVoid(); }

    private boolean infiniteRange = false;
    public boolean isInfiniteRange() { return this.infiniteRange; }

    private final BlockPos corner1;
    public final BlockPos getCorner1() { return this.corner1; }
    private final BlockPos corner2;
    public final BlockPos getCorner2() { return this.corner2; }

    public final boolean isInArea(@Nonnull BlockPos pos) { return this.infiniteRange || MathUtil.WithinBounds(pos, this.corner1, this.corner2); }

    public final boolean isInArea(@Nonnull WorldPosition position)
    {
        if(this.isVoid())
            return true;
        if(position.isVoid() )
            return false;
        return this.center.getDimension().equals(position.getDimension()) && this.isInArea(position.getPos());
    }

    //Infinite range constructor
    private WorldArea(@Nullable ResourceKey<Level> dimension) { this.center = WorldPosition.of(dimension, null); this.corner1 = BlockPos.ZERO; this.corner2 = BlockPos.ZERO; this.infiniteRange = true; }
    private WorldArea(@Nullable ResourceKey<Level> dimension, @Nullable BlockPos center,  int horizRadius, int vertSize, int vertOffset) {
        this.center = WorldPosition.of(dimension, center);
        this.corner1 = this.center.getPos().west(horizRadius).south(horizRadius).above(vertOffset);
        this.corner2 = this.center.getPos().east(horizRadius).north(horizRadius).above(vertSize).above(vertOffset);
    }

    public static WorldArea ofInfiniteRange(@Nonnull WorldPosition center) { if(center.isVoid()) return INFINITE_VOID; return new WorldArea(center.getDimension()); }
    public static WorldArea of(@Nonnull WorldPosition center, int horizRadius, int vertSize, int vertOffset) { if(center.isVoid()) return VOID; return new WorldArea(center.getDimension(), center.getPos(), horizRadius, vertSize, vertOffset); }
    public static WorldArea of(@Nonnull Level level, BlockPos center, int horizRadius, int vertSize, int vertOffset) { return new WorldArea(level.dimension(), center, horizRadius, vertSize, vertOffset); }

}
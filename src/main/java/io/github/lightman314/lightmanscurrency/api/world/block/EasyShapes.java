package io.github.lightman314.lightmanscurrency.api.world.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class EasyShapes {

    private EasyShapes() {}

    public static final VoxelShape BOX = Block.box(0f,0f,0f,16f,16f,16f);
    public static final VoxelShape SLAB = Block.box(0f,0f,0f,16f,8f,16f);

    public static final VoxelShape TALL_BOX = Block.box(0f,0f,0f,16f,32f,16f);

    public static Function<Direction,VoxelShape> singleShape(VoxelShape shape) { return dir -> shape; }
    public static BiFunction<Direction,Boolean,VoxelShape> tallSingleShape(VoxelShape shape) {
        VoxelShape movedShape = moveDown(shape);
        return (facing,bottom) -> bottom ? shape : movedShape;
    }
    public static VoxelShape moveDown(VoxelShape shape) { return shape.move(0d,-1d,0d); }


}
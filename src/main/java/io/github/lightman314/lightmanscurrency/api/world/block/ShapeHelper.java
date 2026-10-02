package io.github.lightman314.lightmanscurrency.api.world.block;

import com.mojang.datafixers.util.Function3;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3fc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ShapeHelper {

    private ShapeHelper() {}

    public static final VoxelShape BOX = Block.box(0f,0f,0f,16f,16f,16f);
    public static final VoxelShape SLAB = Block.box(0f,0f,0f,16f,8f,16f);

    public static final VoxelShape TALL_BOX = Block.box(0f,0f,0f,16f,32f,16f);

    public static final BiFunction<Direction,Boolean,VoxelShape> WIDE_BOX;

    public static final Function3<Direction,Boolean,Boolean,VoxelShape> TALL_WIDE_BOX;

    static {

        VoxelShape wideNorth = Block.box(0d,0d,0d,32d,16d,16d);
        VoxelShape wideEast =  Block.box(0d,0d,0d,16d,16d,32d);
        VoxelShape wideSouth =  Block.box(-16d,0d,0d,16d,16d,16d);
        VoxelShape wideWest =  Block.box(0d,0d,-16d,16d,16d,16d);
        WIDE_BOX = wideDirectionalShape(wideNorth,wideEast,wideSouth,wideWest);

        VoxelShape tallWideNorth = Block.box(-16d,0d,0d,16d,32d,16d);
        VoxelShape tallWideEast =  Block.box(0d,0d,-16d,16d,32d,16d);
        VoxelShape tallWideSouth = Block.box(0d,0d,0d,32d,32d,16d);
        VoxelShape tallWideWest =  Block.box(0d,0d,0d,16d,32d,32d);
        TALL_WIDE_BOX = tallWideDirectionalShape(tallWideNorth,tallWideEast,tallWideSouth,tallWideWest);

    }

    //Normal Rotatable
    public static Function<Direction,VoxelShape> singleShape(VoxelShape shape) { return dir -> shape; }
    public static Function<Direction,VoxelShape> directionalShape(VoxelShape north,VoxelShape east,VoxelShape south,VoxelShape west) {
        Map<Direction,VoxelShape> map = Map.of(
                Direction.NORTH,north,
                Direction.EAST,east,
                Direction.SOUTH,south,
                Direction.WEST,west);
        return map::get;
    }
    //Tall
    public static BiFunction<Direction,Boolean,VoxelShape> tallSingleShape(VoxelShape shape) {
        VoxelShape movedShape = moveDown(shape);
        return (facing,bottom) -> bottom ? shape : movedShape;
    }
    public static BiFunction<Direction,Boolean,VoxelShape> tallDirectionalShape(VoxelShape north,VoxelShape east,VoxelShape south,VoxelShape west) {
        Map<SingleKey,VoxelShape> temp = new HashMap<>();
        addTallDirectionalToMap(temp,Direction.NORTH,north);
        addTallDirectionalToMap(temp,Direction.EAST,east);
        addTallDirectionalToMap(temp,Direction.SOUTH,south);
        addTallDirectionalToMap(temp,Direction.WEST,west);
        Map<SingleKey,VoxelShape> map = Map.copyOf(temp);
        return (facing,bottom) -> map.get(new SingleKey(facing,bottom));
    }
    private static void addTallDirectionalToMap(Map<SingleKey,VoxelShape> builder, Direction facing, VoxelShape shape) {
        builder.put(new SingleKey(facing,true),shape);
        builder.put(new SingleKey(facing,false),moveDown(shape));
    }
    public static VoxelShape moveDown(VoxelShape shape) { return shape.move(0d,-1d,0d); }

    //Wide
    public static BiFunction<Direction,Boolean,VoxelShape> wideDirectionalShape(VoxelShape north,VoxelShape east,VoxelShape south,VoxelShape west) {
        Map<SingleKey,VoxelShape> temp = new HashMap<>();
        addWideDirectionalToMap(temp,Direction.NORTH,north);
        addWideDirectionalToMap(temp,Direction.EAST,east);
        addWideDirectionalToMap(temp,Direction.SOUTH,south);
        addWideDirectionalToMap(temp,Direction.WEST,west);
        Map<SingleKey,VoxelShape> map = Map.copyOf(temp);
        return (facing,left) -> map.get(new SingleKey(facing,left));
    }
    private static void addWideDirectionalToMap(Map<SingleKey,VoxelShape> builder,Direction facing,VoxelShape shape) {
        builder.put(new SingleKey(facing,true),shape);
        Vector3fc offset = IRotatableBlock.getLeftVect(facing);
        builder.put(new SingleKey(facing,false),shape.move(offset.x(),offset.y(), offset.z()));
    }

    //Tall & Wide
    public static Function3<Direction,Boolean,Boolean,VoxelShape> tallWideDirectionalShape(VoxelShape north,VoxelShape east,VoxelShape south,VoxelShape west) {
        Map<TallWideKey,VoxelShape> temp = new HashMap<>();
        addTallWideDirectionalToMap(temp,Direction.NORTH,north);
        addTallWideDirectionalToMap(temp,Direction.EAST,east);
        addTallWideDirectionalToMap(temp,Direction.SOUTH,south);
        addTallWideDirectionalToMap(temp,Direction.WEST,west);
        Map<TallWideKey,VoxelShape> map = Map.copyOf(temp);
        return (facing,bottom,left) -> map.get(new TallWideKey(facing,bottom,left));
    }
    private static void addTallWideDirectionalToMap(Map<TallWideKey,VoxelShape> builder,Direction facing,VoxelShape shape) {
        builder.put(new TallWideKey(facing,true,true),shape);
        builder.put(new TallWideKey(facing,false,true),moveDown(shape));
        Vector3fc offset = IRotatableBlock.getLeftVect(facing);
        VoxelShape rightShape = shape.move(offset.x(),offset.y(),offset.z());
        builder.put(new TallWideKey(facing,true,false),rightShape);
        builder.put(new TallWideKey(facing,false,false),moveDown(rightShape));
    }

    private record SingleKey(Direction facing, boolean base) {}
    private record TallWideKey(Direction facing,boolean bottom,boolean left) {}

}
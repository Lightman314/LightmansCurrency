package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.util.TriFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class LazyShapes {

	//Functions for directional only
	public static Function<Direction,VoxelShape> lazySingleShape(VoxelShape shape) { return (facing) -> shape; }
	public static Function<Direction,VoxelShape> lazyDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionShapeHandler(north,east,south,west); }
	public static Function<Direction,VoxelShape> lazyDirectionalShape(VoxelShape northSouth, VoxelShape eastWest) { return new LazyDirectionShapeHandler(northSouth,eastWest,northSouth,eastWest); }
	//BiFunctions for tall only
	public static BiFunction<Direction,Boolean,VoxelShape> lazyTallSingleShape(VoxelShape shape) { return (facing,isBottom) -> { if(isBottom) return shape; return moveDown(shape);}; }
	public static BiFunction<Direction,Boolean,VoxelShape> lazyTallDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionTallShapeHandler(north,east,south,west); }
	//BiFunctions for wide only (Wide must interface with direction, so no lazySingleShape variant for it)
	public static BiFunction<Direction,Boolean,VoxelShape> lazyWideDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionWideShapeHandler(north,east,south,west); }
	//TriFunctions for tall and wide (Wide must interface with direction, so no lazySingleShape variant for it)
	public static TriFunction<Direction,Boolean,Boolean,VoxelShape> lazyTallWideDirectionalShape(BiFunction<Direction,Boolean,VoxelShape> tallShape) { return new LazyDirectionTallWideShapeHandler(tallShape); }
	public static TriFunction<Direction,Boolean,Boolean,VoxelShape> lazyTallWideDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionTallWideShapeHandler(north,east,south,west); }
	
	//Half Box
	public static final VoxelShape SHORT_BOX = Block.box(0d,0d,0d,16d,8d,16d);
	public static final Function<Direction,VoxelShape> SHORT_BOX_SHAPE = lazySingleShape(SHORT_BOX);
	//Full Box
	public static final VoxelShape BOX = Block.box(0d,0d,0d,16d,16d,16d);
	public static final VoxelShape BOX_T = Block.box(0.01d,0d,0.01d,15.99d,16d,15.99d);
	public static final Function<Direction,VoxelShape> BOX_SHAPE = lazySingleShape(BOX);
	//Tall Box
	public static final VoxelShape TALL_BOX = Block.box(0d,0d,0d,16d,32d,16d);
	public static final BiFunction<Direction,Boolean,VoxelShape> TALL_BOX_SHAPE = lazyTallSingleShape(TALL_BOX);
	//Wide Box
	public static final VoxelShape WIDE_BOX_NORTH = Block.box(0d,0d,0d,32d,16d,16d);
	public static final VoxelShape WIDE_BOX_EAST = Block.box(0d,0d,0d,16d,16d,32d);
	public static final VoxelShape WIDE_BOX_SOUTH = Block.box(-16d,0d,0d,16d,16d,16d);
	public static final VoxelShape WIDE_BOX_WEST = Block.box(0d,0d,-16d,16d,16d,16d);
	public static final BiFunction<Direction,Boolean,VoxelShape> WIDE_BOX_SHAPE = lazyWideDirectionalShape(WIDE_BOX_NORTH,WIDE_BOX_EAST,WIDE_BOX_SOUTH,WIDE_BOX_WEST);
	//Tall & Wide Box
	public static final VoxelShape TALL_WIDE_BOX_NORTH = Block.box(0d,0d,0d,32d,32d,16d);
	public static final VoxelShape TALL_WIDE_BOX_EAST = Block.box(0d,0d,0d,16d,32d,32d);
	public static final VoxelShape TALL_WIDE_BOX_SOUTH = Block.box(-16d,0d,0d,16d,32d,16d);
	public static final VoxelShape TALL_WIDE_BOX_WEST = Block.box(0d,0d,-16d,16d,32d,16d);
	public static final TriFunction<Direction,Boolean,Boolean,VoxelShape> TALL_WIDE_BOX_SHAPE = lazyTallWideDirectionalShape(TALL_WIDE_BOX_NORTH,TALL_WIDE_BOX_EAST,TALL_WIDE_BOX_SOUTH,TALL_WIDE_BOX_WEST);
	
	public static VoxelShape moveDown(VoxelShape shape) { return shape.move(0f, -1d, 0d); }
	
	protected static class LazyDirectionShapeHandler implements Function<Direction, VoxelShape>
	{
		private final VoxelShape north;
		private final VoxelShape east;
		private final VoxelShape south;
		private final VoxelShape west;
		
		public LazyDirectionShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.north = north;
			this.east = east;
			this.south = south;
			this.west = west;
		}
		
		@Override
		public VoxelShape apply(Direction facing) {
			return switch (facing) {
				case EAST -> east;
				case SOUTH -> south;
				case WEST -> west;
				default -> north;
			};
		}
	}
	
	protected static class LazyDirectionTallShapeHandler implements BiFunction<Direction,Boolean,VoxelShape>
	{
		private final Function<Direction,VoxelShape> lazyShape;
		
		public LazyDirectionTallShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.lazyShape = lazyDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isBottom)
		{
			VoxelShape shape = lazyShape.apply(facing);
			if(isBottom)
				return shape;
			else
				return moveDown(shape);
		}
	}
	
	protected static class LazyDirectionWideShapeHandler implements BiFunction<Direction,Boolean,VoxelShape>
	{
		private final Function<Direction,VoxelShape> lazyShape;
		
		public LazyDirectionWideShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.lazyShape = lazyDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isLeft)
		{
			VoxelShape shape = lazyShape.apply(facing);
			if(isLeft)
				return shape;
			else
			{
				Vector3f offset = IRotatableBlock.getLeftVect(facing);
				return shape.move(offset.x(), offset.y(), offset.z());
			}
		}
		
	}
	
	protected static class LazyDirectionTallWideShapeHandler implements TriFunction<Direction,Boolean,Boolean,VoxelShape> {
		
		private final BiFunction<Direction,Boolean,VoxelShape> lazyShape;
		
		public LazyDirectionTallWideShapeHandler(BiFunction<Direction,Boolean,VoxelShape> tallShape) {
			this.lazyShape = tallShape;
		}
		
		public LazyDirectionTallWideShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) {
			this.lazyShape = lazyTallDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isBottom, Boolean isLeft)
		{
			VoxelShape shape = lazyShape.apply(facing, isBottom);
			if(isLeft)
				return shape;
			else
			{
				Vector3f offset = IRotatableBlock.getLeftVect(facing);
				return shape.move(offset.x(), offset.y(), offset.z());
			}
		}
		
	}
	
}

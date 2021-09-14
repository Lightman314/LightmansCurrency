package io.github.lightman314.lightmanscurrency.blocks.util;

import java.util.function.BiFunction;

import com.google.common.base.Function;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LazyShapes {

	public static Function<Direction,VoxelShape> lazyDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionShapeHandler(north, east, south, west); }
	public static BiFunction<Direction, Boolean, VoxelShape> lazyTallDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionTallShapeHandler(north, east, south, west); }
	
	//Half Boxes
	public static final VoxelShape SHORT_BOX = box(0d,0d,0d,16d,8d,16d);
	public static final VoxelShape SHORT_BOX_T = box(0.01d,0d,0.01d,15.99d,8d,15.99d);
	
	//Normal Boxes
	public static final VoxelShape BOX = box(0d,0d,0d,16d,16d,16d);
	public static final VoxelShape BOX_T = box(0.01d,0d,0.01d,15.99d,16d,15.99d);
	
	//Tall Boxes
	public static final VoxelShape TALL_BOX = box(0d,0d,0d,16d,32d,16d);
	public static final VoxelShape TALL_BOX_T = box(0.01d,0d,0.01d,15.99d,32d,15.99d);
	
	public static final VoxelShape moveDown(VoxelShape shape)
	{
		return shape.move(0d, -1d, 0d);
	}
	
	private static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		return Block.box(x1, y1, z1, x2, y2, z2);
	}
	
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
			switch(facing)
			{
			case EAST:
				return east;
			case SOUTH:
				return south;
			case WEST:
				return west;
			default:
				return north;
			}
		}
		
	}
	
	protected static class LazyDirectionTallShapeHandler implements BiFunction<Direction, Boolean, VoxelShape>
	{
		
		private final Function<Direction,VoxelShape> lazyShape;
		
		public LazyDirectionTallShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.lazyShape = lazyDirectionalShape(north, east, south, west);
		}

		@Override
		public VoxelShape apply(Direction facing, Boolean isBottom) {
			VoxelShape shape = lazyShape.apply(facing);
			if(isBottom)
				return shape;
			else
				return shape.move(0d, -1d, 0d);
		}
		
	}
	
}

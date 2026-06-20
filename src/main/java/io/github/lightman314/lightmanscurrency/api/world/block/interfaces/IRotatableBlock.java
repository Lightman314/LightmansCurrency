package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nullable;

public interface IRotatableBlock {

    EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    default Direction getFacing(BlockState state) { return state.getValue(FACING); }

    static Direction getRelativeSide(Direction facing,@Nullable Direction side)
    {
        if(side == null || side.getAxis() == Direction.Axis.Y)
            return side;
        //Shouldn't need to invert since I'm going to use the correct block states this time around :)
        return Direction.from2DDataValue(facing.get2DDataValue() + side.get2DDataValue());
    }

    static Direction getActualSide(Direction facing,@Nullable Direction relativeSide)
    {
        if(relativeSide == null || relativeSide.getAxis() == Direction.Axis.Y)
            return null;
        return Direction.from2DDataValue(facing.get2DDataValue() - relativeSide.get2DDataValue() + 4);
    }

    static BlockPos getRightPos(BlockPos pos,Direction facing)
    {
        if(facing.getAxis() == Direction.Axis.Y)
            return pos;
        return pos.relative(facing.getCounterClockWise());
    }

    static BlockPos getLeftPos(BlockPos pos,Direction facing)
    {
        if(facing.getAxis() == Direction.Axis.Y)
            return pos;
        return pos.relative(facing.getClockWise());
    }

    static BlockPos getFrontPosition(BlockPos pos,Direction facing)
    {
        if(facing.getAxis() == Direction.Axis.Y)
            return pos;
        return pos.relative(facing);
    }

    static BlockPos getBackPosition(BlockPos pos,Direction facing)
    {
        if(facing.getAxis() == Direction.Axis.Y)
            return pos;
        return pos.relative(facing.getOpposite());
    }

    static Vector3fc getRightVect(Direction facing) { return facing.getClockWise().getUnitVec3f(); }
    static Vector3fc getLeftVect(Direction facing) { return facing.getCounterClockWise().getUnitVec3f(); }
    static Vector3fc getFrontVect(Direction facing) { return facing.getUnitVec3f(); }
    static Vector3fc getBackVect(Direction facing) { return facing.getOpposite().getUnitVec3f(); }

    static Vector3f getOffsetVect(Direction facing) {
        return switch (facing) {
            case SOUTH -> new Vector3f(0f,0f,1f);
            case NORTH -> new Vector3f(1f,0f,0f);
            case EAST -> new Vector3f(1f,0f,1f);
            default -> new Vector3f(0f,0f,0f);
        };
    }

    default int getRotationY(BlockState state) { return this.getRotationY(this.getFacing(state)); }
    default int getRotationY(Direction facing) {
        return switch (facing) {
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }
    default int getRotationYInv(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    static <T,BE extends BlockEntity> void registerRotatableCapability(RegisterCapabilitiesEvent event, BlockCapability<T,Direction> capability, BlockEntityType<BE> type,ICapabilityProvider<? super BE,Direction,T> getter)
    {
        event.registerBlockEntity(capability,type,(be,side) -> {
            if(be.getBlockState().getBlock() instanceof IRotatableBlock rb)
                side = IRotatableBlock.getRelativeSide(rb.getFacing(be.getBlockState()),side);
            return getter.getCapability(be,side);
        });
    }

    static <T> void registerRotatableCapability(RegisterCapabilitiesEvent event, BlockCapability<T,Direction> capability, IBlockCapabilityProvider<T,Direction> getter,Block... blocks)
    {
        event.registerBlock(capability,((level, pos, state, be, side) -> {
            if(state.getBlock() instanceof IRotatableBlock rb)
                side = IRotatableBlock.getRelativeSide(rb.getFacing(state),side);
            return getter.getCapability(level,pos,state,be,side);
        }));
    }

}
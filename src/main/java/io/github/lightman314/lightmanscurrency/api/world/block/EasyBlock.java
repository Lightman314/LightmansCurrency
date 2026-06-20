package io.github.lightman314.lightmanscurrency.api.world.block;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerCommon;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class EasyBlock extends Block {

    public EasyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.initializeDefaultState(this.defaultBlockState()));
    }

    protected BlockState initializeDefaultState(BlockState state) { return this.defaultBlockState(); }

    protected boolean isBlockOpaque(BlockState state) { return this.isBlockOpaque(); }
    protected boolean isBlockOpaque() { return true; }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        if(this.isBlockOpaque(state))
            return super.getOcclusionShape(state);
        return Shapes.empty();
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) { return createTickerFromInterfaces(level,blockState,type); }

    @Nullable
    public static <T extends BlockEntity> BlockEntityTicker<T> createTickerFromInterfaces(Level level,BlockState state,BlockEntityType<T> type)
    {
        BlockEntity dummyBE = type.create(BlockPos.ZERO,state);
        if(level.isClientSide())
        {
            //See if we should create the client or common tickers
            if(dummyBE instanceof ITickerClient)
            {
                if(dummyBE instanceof ITickerCommon)
                    return ClientAndCommonTicker.INSTANCE.cast(type);
                return ClientTicker.INSTANCE.cast(type);
            }
        }
        else
        {
            if(dummyBE instanceof ITickerServer)
            {
                if(dummyBE instanceof ITickerCommon)
                    return ServerAndCommonTicker.INSTANCE.cast(type);
                return ServerTicker.INSTANCE.cast(type);
            }
        }
        if(dummyBE instanceof ITickerCommon)
            return CommonTicker.INSTANCE.cast(type);
        return null;
    }

    private static abstract class FlexibleTicker<T extends BlockEntity> implements BlockEntityTicker<T>
    {
        public <BE extends BlockEntity> BlockEntityTicker<BE> cast(BlockEntityType<BE> type) { return (BlockEntityTicker<BE>)this; }
    }

    private static class ClientTicker<T extends BlockEntity> extends FlexibleTicker<T>
    {
        private static final ClientTicker<?> INSTANCE = new ClientTicker<>();
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
            if(entity instanceof ITickerClient t)
                t.clientTick();
        }
    }

    private static class CommonTicker<T extends BlockEntity> extends FlexibleTicker<T>
    {
        private static final CommonTicker<?> INSTANCE = new CommonTicker<>();
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
            if(entity instanceof ITickerCommon t)
                t.tick();
        }
    }

    private static class ServerTicker<T extends BlockEntity> extends FlexibleTicker<T>
    {
        private static final ServerTicker<?> INSTANCE = new ServerTicker<>();
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
            if(entity instanceof ITickerServer t)
                t.serverTick();
        }
    }

    private static class ClientAndCommonTicker<T extends BlockEntity> extends FlexibleTicker<T>
    {
        private static final ClientAndCommonTicker<?> INSTANCE = new ClientAndCommonTicker<>();
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
            if(entity instanceof ITickerCommon t)
                t.tick();
            if(entity instanceof ITickerClient t)
                t.clientTick();
        }
    }

    private static class ServerAndCommonTicker<T extends BlockEntity> extends FlexibleTicker<T>
    {
        private static final ServerAndCommonTicker<?> INSTANCE = new ServerAndCommonTicker<>();
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, BlockEntity entity) {
            if(entity instanceof ITickerCommon t)
                t.tick();
            if(entity instanceof ITickerClient t)
                t.clientTick();
        }
    }

}
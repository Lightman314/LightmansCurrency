package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.BookTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IBookTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BookTraderBlock extends TraderBlockRotatable implements IBookTraderBlock {

    public static final int BOOK_COUNT = 10;

    public BookTraderBlock(Properties properties) { super(properties); }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new BookTraderBlockEntity(pos, state, BOOK_COUNT); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.BOOK_TRADER.get(); }

    @Override
    public Vector3f GetBookRenderPos(int tradeSlot, BlockState state) {
        //Get facing
        Direction facing = this.getFacing(state);
        //Define directions for easy positional handling
        Vector3f right = IRotatableBlock.getRightVect(facing);
        Vector3f up = MathUtil.getYP();
        Vector3f forward = IRotatableBlock.getForwardVect(facing);
        Vector3f offset = IRotatableBlock.getOffsetVect(facing);

        float xPos = (tradeSlot % 5) * 3f/16f - 5f/16f;
        float yPos = tradeSlot < 5 ? 17f/16f : 9f/16f;

        return MathUtil.VectorAdd(offset, MathUtil.VectorMult(right, xPos), MathUtil.VectorMult(up, yPos), MathUtil.VectorMult(forward, 0.5f));
    }

    @Override
    public List<Quaternionf> GetBookRenderRot(int tradeSlot, BlockState state) {
        List<Quaternionf> rotation = new ArrayList<>();
        int facing = this.getFacing(state).get2DDataValue();
        rotation.add(MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing * -90f));
        return rotation;
    }

    @Override
    public int maxRenderIndex() { return BOOK_COUNT; }

    @Override
    protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER_BOOK; }

}

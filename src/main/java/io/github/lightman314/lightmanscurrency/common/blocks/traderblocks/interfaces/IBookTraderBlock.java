package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public interface IBookTraderBlock extends ITraderBlock {

    @OnlyIn(Dist.CLIENT)
    Vector3f GetBookRenderPos(int tradeSlot, BlockState state);

    @OnlyIn(Dist.CLIENT)
    List<Quaternionf> GetBookRenderRot(int tradeSlot, BlockState state);

    @OnlyIn(Dist.CLIENT)
    default float GetBookRenderScale(int tradeSlot, BlockState state) { return 1f; }

    @OnlyIn(Dist.CLIENT)
    int maxRenderIndex();

}

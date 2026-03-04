package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public interface IBookTraderBlock extends ITraderBlock {

    Vector3f GetBookRenderPos(int tradeSlot, BlockState state);

    List<Quaternionf> GetBookRenderRot(int tradeSlot, BlockState state);

    default float GetBookRenderScale(int tradeSlot, BlockState state) { return 1f; }

    int maxRenderIndex();

}

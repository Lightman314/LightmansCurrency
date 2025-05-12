package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.CommandTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class CommandTraderBlock extends TraderBlockRotatable implements IVariantBlock {

    public CommandTraderBlock(Properties properties) { super(properties); }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new CommandTraderBlockEntity(pos,state); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.COMMAND_TRADER.get(); }

    @Override
    protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_COMMAND_TRADER.asTooltip(); }

}

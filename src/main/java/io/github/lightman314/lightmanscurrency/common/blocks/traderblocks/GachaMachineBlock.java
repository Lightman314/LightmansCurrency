package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class GachaMachineBlock extends TraderBlockRotatable implements IVariantBlock {

    protected final int color;
    public int getColor() { return this.color; }
    public GachaMachineBlock(Properties properties, Color color) { this(properties,color.hexColor); }
    public GachaMachineBlock(Properties properties, int color) { super(properties, box(2d,0d,2d,14d,16d,14d)); this.color = color; }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new GachaMachineBlockEntity(pos,state,this.color); }
    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.GACHA_MACHINE.get(); }

    @Override
    protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_GACHA_MACHINE.asTooltip(); }

}
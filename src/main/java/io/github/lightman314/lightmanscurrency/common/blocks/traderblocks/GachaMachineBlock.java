package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class GachaMachineBlock extends TraderBlockRotatable implements IVariantBlock, IColoredBlock {

    public static final List<ResourceLocation> BASIC_MODELS = ImmutableList.of(LightmansCurrency.id("block/gacha_machine/basic_1"),LightmansCurrency.id("block/gacha_machine/basic_2"),LightmansCurrency.id("block/gacha_machine/basic_3"));

    protected final int color;
    public int getBlockColor() { return this.color; }
    public GachaMachineBlock(Properties properties, Color color) { this(properties,color.hexColor); }
    public GachaMachineBlock(Properties properties, int color) { super(properties, box(2d,0d,2d,14d,16d,14d)); this.color = color; }

    public List<ResourceLocation> getBasicModels() { return BASIC_MODELS; }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new GachaMachineBlockEntity(pos,state,this.color); }
    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.GACHA_MACHINE.get(); }

    @Override
    protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_GACHA_MACHINE.asTooltip(); }

}
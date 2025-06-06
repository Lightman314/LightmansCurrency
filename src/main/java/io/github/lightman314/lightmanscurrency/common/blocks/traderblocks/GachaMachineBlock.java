package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaMachineBlock extends TraderBlockRotatable implements IVariantBlock {

    public static final List<ResourceLocation> BASIC_MODELS = ImmutableList.of(VersionUtil.lcResource("block/gacha_machine/basic_1"),VersionUtil.lcResource("block/gacha_machine/basic_2"),VersionUtil.lcResource("block/gacha_machine/basic_3"));

    protected final int color;
    public int getColor() { return this.color; }
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
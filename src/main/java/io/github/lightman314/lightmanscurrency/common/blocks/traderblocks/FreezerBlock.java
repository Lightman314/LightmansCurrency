package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class FreezerBlock extends TraderBlockTallRotatable implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 8;
	
	public static final VoxelShape SHAPE_SOUTH = box(0d,0d,3d,16d,32d,16d);
	public static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,32d,13d);
	public static final VoxelShape SHAPE_EAST = box(3d,0d,0d,16d,32d,16d);
	public static final VoxelShape SHAPE_WEST = box(0d,0d,0d,13d,32d,16d);

	private final ResourceLocation doorModel;

	public FreezerBlock(Properties properties, @Nonnull ResourceLocation doorModel)
	{
		super(properties, LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST));
		this.doorModel = doorModel;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public ResourceLocation getDoorModel() { return this.doorModel; }

	public static ResourceLocation GenerateDoorModel(Color color) { return GenerateDoorModel(LightmansCurrency.MODID, color); }

	public static ResourceLocation GenerateDoorModel(String namespace, Color color) {
		return ResourceLocation.fromNamespaceAndPath(namespace, "block/freezer/doors/" + color.toString().toLowerCase());
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new FreezerTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.FREEZER_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER.asTooltip(TRADECOUNT); }
	
}

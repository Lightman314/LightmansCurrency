package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.menus.containers.RecipeContainerWrapper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class TicketStationBlockEntity extends BlockEntity{

	private final RecipeContainerWrapper recipeContainer = new RecipeContainerWrapper(() -> this.storage, new SimpleContainer(2));
	SimpleContainer storage = new SimpleContainer(2);
	public RecipeContainerWrapper getStorage() { return this.recipeContainer; }
	
	public TicketStationBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.TICKET_MACHINE.get(), pos, state);
		this.storage.addListener(c -> this.setChanged());
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
		InventoryUtil.saveAllItems("Items", tag, this.storage, lookup);
		super.saveAdditional(tag, lookup);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
		this.storage = InventoryUtil.loadAllItems("Items", tag, 2,lookup);
		this.storage.addListener(c -> this.setChanged());
		super.loadAdditional(tag, lookup);
	}
	
}

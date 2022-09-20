package io.github.lightman314.lightmanscurrency.blockentity.old.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.old.OldBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class OldItemTraderBlockEntity extends OldBlockEntity {
	
	public OldItemTraderBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.OLD_ITEM_TRADER.get(), pos, state);
	}

	@Override
	protected BlockEntity createReplacement(CompoundTag compound) {
		ItemTraderBlockEntity newBE = new ItemTraderBlockEntity(this.worldPosition, this.getBlockState(), 1, false);
		newBE.load(compound);
		LightmansCurrency.LogInfo("Successfully converted Old ItemTraderBlockEntity into an ItemTraderBlockEntity at " + this.worldPosition.toShortString());
		return newBE;
	}
	
}
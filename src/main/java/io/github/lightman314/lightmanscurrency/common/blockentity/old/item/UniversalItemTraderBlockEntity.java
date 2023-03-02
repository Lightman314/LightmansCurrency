package io.github.lightman314.lightmanscurrency.common.blockentity.old.item;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.old.OldBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.data_updating.DataConverter;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class UniversalItemTraderBlockEntity extends OldBlockEntity {
	
	public UniversalItemTraderBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.UNIVERSAL_ITEM_TRADER.get(), pos, state);
	}

	@Override
	protected BlockEntity createReplacement(CompoundTag compound) {
		UUID uuid = compound.getUUID("ID");
		long newID = DataConverter.getNewTraderID(uuid);
		TraderData trader = TraderSaveData.GetTrader(false, newID);
		ItemTraderBlockEntity newBE = new ItemTraderBlockEntity(this.worldPosition, this.getBlockState(), trader != null ? trader.getTradeCount() : 1, true);
		newBE.setTraderID(newID);
		LightmansCurrency.LogInfo("Successfully converted UniversalItemTraderBlockEntity into an ItemTraderBlockEntity at " + this.worldPosition.toShortString() + "\nOld ID: " + uuid + "\nNew ID: " + newID);
		return newBE;
	}
	
}
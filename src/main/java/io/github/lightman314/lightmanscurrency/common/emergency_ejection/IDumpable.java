package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IDumpable {

	//Ejection stuff
	public List<ItemStack> getContents(Level level, BlockPos pos, BlockState state, boolean dropBlock);
	
	public MutableComponent getName();
	
	public OwnerData getOwner();
	
}
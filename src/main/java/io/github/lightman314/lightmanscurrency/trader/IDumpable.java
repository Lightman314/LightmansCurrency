package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IDumpable {

	//Ejection stuff
	public List<ItemStack> dumpContents(BlockState state, boolean dropBlock);
	public void dumpContents(List<ItemStack> contents);
	
	public MutableComponent getName();
	
	public Team getTeam();
	public PlayerReference getOwner();
	
}

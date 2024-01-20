package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IDumpable {

	//Ejection stuff
	List<ItemStack> getContents(Level level, BlockPos pos, BlockState state, boolean dropBlock);
	
	MutableComponent getName();
	
	OwnerData getOwner();

	static IDumpable preCollected(List<ItemStack> contents, Component name, OwnerData owner) { return new LazyDumpable(contents, name, owner); }

	class LazyDumpable implements IDumpable
	{
		private final ImmutableList<ItemStack> contents;
		private final Component name;
		private final OwnerData owner = new OwnerData(() -> true, (o) -> {});

		protected LazyDumpable(List<ItemStack> contents, Component name, OwnerData owner) { this.contents = ImmutableList.copyOf(contents); this.name = name; this.owner.copyFrom(owner); }

		@Override
		public List<ItemStack> getContents(Level level, BlockPos pos, BlockState state, boolean dropBlock) { return this.contents; }
		@Override
		public MutableComponent getName() { return EasyText.makeMutable(this.name); }
		@Override
		public OwnerData getOwner() { return this.owner;}
	}
	
}

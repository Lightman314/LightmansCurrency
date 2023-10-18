package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CashRegisterBlockEntity extends BlockEntity implements ITraderSource{
	
	List<BlockPos> positions = new ArrayList<>();
	
	public CashRegisterBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.CASH_REGISTER.get(), pos, state);
	}
	
	public void loadDataFromItems(CompoundTag itemTag)
	{
		if(itemTag == null)
			return;
		readPositions(itemTag);
	}
	
	public void OpenContainer(Player player)
	{
		MenuProvider provider = TraderData.getTraderMenuProvider(this.worldPosition, BlockEntityValidator.of(this));
		if(!(player instanceof ServerPlayer sp))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
			return;
		}
		if(this.getTraders().size() > 0)
			sp.openMenu(provider, this.worldPosition);
		else
			player.sendSystemMessage(EasyText.translatable("message.lightmanscurrency.cash_register.notlinked"));
	}
	
	@Override
	public boolean isSingleTrader() { return false; }
	
	@Override
	public @NotNull List<TraderData> getTraders() {
		List<TraderData> traders = new ArrayList<>();
		for (BlockPos position : this.positions) {
			BlockEntity be = this.level.getBlockEntity(position);
			if (be instanceof TraderBlockEntity<?>) {
				TraderData trader = ((TraderBlockEntity<?>) be).getTraderData();
				if (trader != null)
					traders.add(trader);
			}
		}
		return traders;
	}
	
	@Override
	public void saveAdditional(@NotNull CompoundTag compound)
	{
		
		ListTag storageList = new ListTag();
		for (BlockPos thisPos : positions) {
			CompoundTag thisEntry = new CompoundTag();
			thisEntry.putInt("x", thisPos.getX());
			thisEntry.putInt("y", thisPos.getY());
			thisEntry.putInt("z", thisPos.getZ());
			storageList.add(thisEntry);
		}
		
		if(storageList.size() > 0)
		{
			compound.put("TraderPos", storageList);
		}
		
		super.saveAdditional(compound);
	}
	
	@Override
	public void load(@NotNull CompoundTag compound)
	{
		
		this.readPositions(compound);
		
		super.load(compound);
		
	}
	
	private void readPositions(CompoundTag compound)
	{
		if(compound.contains("TraderPos"))
		{
			this.positions = new ArrayList<>();
			ListTag storageList = compound.getList("TraderPos", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundTag thisEntry = storageList.getCompound(i);
				if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
				{
					BlockPos thisPos = new BlockPos(thisEntry.getInt("x"), thisEntry.getInt("y"), thisEntry.getInt("z"));
					this.positions.add(thisPos);
				}
			}
		}
	}
	
	@Override
	public @NotNull CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
	
}

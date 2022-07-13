package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.ITraderSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

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
		MenuProvider provider = new TraderBlockEntity.TradeMenuProvider<CashRegisterBlockEntity>(this);
		/*if(provider == null)
		{
			LightmansCurrency.LogError("No trade menu container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}*/
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
			return;
		}
		NetworkHooks.openScreen((ServerPlayer)player, provider, this.worldPosition);
	}
	
	/*public void OpenContainer(int oldIndex, int newIndex, int direction, Player player)
	{
		//Validate the direction
		if(direction == 0)
			direction = 1;
		else
			direction = MathUtil.clamp(direction, -1, 1);
		//Only open the container server-side
		if(this.level.isClientSide)
			return;
		//Confirm we have any tile entities that can be opened
		if(this.positions.size() <= 0)
		{
			LightmansCurrency.LogInfo("Cash Register has no Trader Positions stored. Unable to open container.");
			return;
		}
		//Round the newIndex value around if below 0 or greater than the position size
		if(newIndex < 0)
			newIndex = this.positions.size() - 1;
		else if(newIndex >= this.positions.size())
			newIndex = 0;
		if(newIndex == oldIndex)
		{
			LightmansCurrency.LogInfo("Trader Index is the same as the original index.");
			return;
		}
		
		TraderBlockEntity tileEntity = this.getTrader(newIndex);
		if(tileEntity != null)
		{
			//Open the container
			tileEntity.openCashRegisterTradeMenu(player, this);
			return;
		}
		else
		{
			//No tile entity found at the position. Keep moving through the loop
			if(oldIndex < 0)
				oldIndex = newIndex;
			OpenContainer(oldIndex, newIndex + direction, direction, player);
		}
		
	}
	
	public void OpenEditorScreen()
	{
		
	}
	
	public TraderBlockEntity getTrader(int index)
	{
		if(index < 0 || index >= positions.size())
			return null;
		BlockEntity tileEntity = this.level.getBlockEntity(positions.get(index));
		if(tileEntity instanceof TraderBlockEntity)
			return (TraderBlockEntity)tileEntity;
		return null;
	}
	
	public int getTraderIndex(TraderBlockEntity tileEntity)
	{
		for(int i = 0; i < positions.size(); i++)
		{
			if(positions.get(i).equals(tileEntity.getBlockPos()))
				return i;
		}
		return -1;
	}
	
	public int getPairedTraderSize()
	{
		return positions.size();
	}*/
	
	@Override
	public boolean isSingleTrader() { return false; }
	
	@Override
	public List<ITrader> getTraders() { 
		List<ITrader> traders = new ArrayList<>();
		for(int i = 0; i < this.positions.size(); ++i)
		{
			BlockEntity be = this.level.getBlockEntity(this.positions.get(i));
			if(be instanceof ITrader)
				traders.add((ITrader)be);
		}
		return traders;
	}
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		
		ListTag storageList = new ListTag();
		for(int i = 0; i < positions.size(); i++)
		{
			CompoundTag thisEntry = new CompoundTag();
			BlockPos thisPos = positions.get(i);
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
	public void load(CompoundTag compound)
	{
		
		readPositions(compound);
		
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
	public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
	
}

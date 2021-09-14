package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;

public class CashRegisterBlockEntity extends BlockEntity{
	
	List<BlockPos> positions = new ArrayList<>();
	
	public CashRegisterBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.CASH_REGISTER, pos, state);
	}
	
	public void loadDataFromItems(CompoundTag itemTag)
	{
		if(itemTag == null)
			return;
		readPositions(itemTag);
	}
	
	public void OpenContainer(Player player)
	{
		OpenContainer(-1,0,1, player);
	}
	
	public void OpenContainer(int oldIndex, int newIndex, int direction, Player player)
	{
		//Only open the container server-side
		if(this.level.isClientSide)
			return;
		
		//Validate the direction
		if(direction == 0)
			direction = 1;
		else
			direction = MathUtil.clamp(direction, -1, 1);
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
		
		TraderBlockEntity blockEntity = this.getTrader(newIndex);
		if(blockEntity != null)
		{
			//Open the container
			blockEntity.openCashRegisterTradeMenu((ServerPlayer)player, this);
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
		BlockEntity blockEntity = this.level.getBlockEntity(positions.get(index));
		if(blockEntity instanceof ItemTraderBlockEntity)
			return (ItemTraderBlockEntity)blockEntity;
		return null;
	}
	
	public int getTraderIndex(ItemTraderBlockEntity blockEntity)
	{
		for(int i = 0; i < positions.size(); i++)
		{
			if(positions.get(i).equals(blockEntity.getBlockPos()))
				return i;
		}
		return -1;
	}
	
	public int getPairedTraderSize()
	{
		return positions.size();
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
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
		
		return super.save(compound);
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
			ListTag storageList = compound.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
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
	
	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.save(new CompoundTag()));
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		CompoundTag compound = pkt.getTag();
		//CurrencyMod.LOGGER.info("Loading NBT from update packet.");
		this.load(compound);
	}
	
}

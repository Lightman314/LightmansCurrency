package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class CashRegisterTileEntity extends TileEntity{
	
	List<BlockPos> positions = new ArrayList<>();
	
	public CashRegisterTileEntity()
	{
		super(ModTileEntities.CASH_REGISTER);
	}
	
	public void loadDataFromItems(CompoundNBT itemTag)
	{
		if(itemTag == null)
			return;
		readPositions(itemTag);
	}
	
	public void OpenContainer(PlayerEntity player)
	{
		OpenContainer(-1,0,1, player);
	}
	
	public void OpenContainer(int oldIndex, int newIndex, int direction, PlayerEntity player)
	{
		//Validate the direction
		if(direction == 0)
			direction = 1;
		else
			direction = MathUtil.clamp(direction, -1, 1);
		//Only open the container server-side
		if(this.world.isRemote)
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
		
		TraderTileEntity tileEntity = this.getTrader(newIndex);
		if(tileEntity != null)
		{
			//Open the container
			tileEntity.openCashRegisterTradeMenu((ServerPlayerEntity)player, this);
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
	
	public TraderTileEntity getTrader(int index)
	{
		if(index < 0 || index >= positions.size())
			return null;
		TileEntity tileEntity = this.world.getTileEntity(positions.get(index));
		if(tileEntity instanceof TraderTileEntity)
			return (TraderTileEntity)tileEntity;
		return null;
	}
	
	public int getTraderIndex(TraderTileEntity tileEntity)
	{
		for(int i = 0; i < positions.size(); i++)
		{
			if(positions.get(i).equals(tileEntity.getPos()))
				return i;
		}
		return -1;
	}
	
	public int getPairedTraderSize()
	{
		return positions.size();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		ListNBT storageList = new ListNBT();
		for(int i = 0; i < positions.size(); i++)
		{
			CompoundNBT thisEntry = new CompoundNBT();
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
		
		return super.write(compound);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		readPositions(compound);
		
		super.read(state, compound);
		
	}
	
	private void readPositions(CompoundNBT compound)
	{
		if(compound.contains("TraderPos"))
		{
			this.positions = new ArrayList<>();
			ListNBT storageList = compound.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundNBT thisEntry = storageList.getCompound(i);
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
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 0, this.write(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		CompoundNBT compound = pkt.getNbtCompound();
		//CurrencyMod.LOGGER.info("Loading NBT from update packet.");
		this.read(this.getBlockState(), compound);
	}
	
}

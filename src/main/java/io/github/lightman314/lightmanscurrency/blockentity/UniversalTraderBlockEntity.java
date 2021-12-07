package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class UniversalTraderBlockEntity extends BlockEntity implements IOwnableBlockEntity{

	
	UUID traderID = null;
	
	protected UniversalTraderData getData()
	{
		if(this.traderID != null)
			return TradingOffice.getData(this.traderID);
		LightmansCurrency.LogError("Trader ID is null. Cannot get the data (" + (this.level.isClientSide ? "client" : "server"));
		return null;
	}
	
	public UniversalTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	public UUID getTraderID()
	{
		return this.traderID;
	}
	
	public void updateOwner(Entity player)
	{
		if(this.getData() != null && player.getUUID().equals(this.getData().getOwnerID()))
		{
			this.getData().updateOwnerName(player.getDisplayName().getString());
		}
		else if(this.getData() == null)
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.updateOwner," + (this.level.isClientSide ? "client" : "server" ) + ").");
	}
	
	public boolean isOwner(Player player)
	{
		if(this.getData() == null)
		{
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.isOwner," + (this.level.isClientSide ? "client" : "server" ) + ").");
			return true;
		}
		return this.getData().isOwner(player);
	}
	
	public boolean hasPermissions(Player player)
	{
		if(this.getData() == null)
		{
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.isOwner," + (this.level.isClientSide ? "client" : "server" ) + ").");
			return true;
		}
		return this.getData().hasPermissions(player);
	}
	
	public boolean canBreak(Player player)
	{
		return this.isOwner(player);
	}
	
	public void init(Player owner)
	{
		this.init(owner, null);
	}
	
	public void init(Player owner, String customName)
	{
		if(this.level.isClientSide)
			return;
		if(this.traderID == null)
		{
			this.traderID = UUID.randomUUID();
			UniversalTraderData traderData = createInitialData(owner);
			if(customName != null)
			{
				traderData.setName(customName);
			}
			TradingOffice.registerTrader(this.traderID, traderData, owner);
			TileEntityUtil.sendUpdatePacket(this);
		}
	}
	
	protected abstract UniversalTraderData createInitialData(Entity owner);
	
	@Override
	public void saveAdditional(CompoundTag compound)
	{
		if(this.traderID != null)
			compound.putUUID("ID", this.traderID);
		
		super.saveAdditional(compound);
		
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		if(compound.contains("ID"))
			this.traderID = compound.getUUID("ID");
		
		super.load(compound);
	}
	
	public void openStorageMenu(Player player)
	{
		if(!this.level.isClientSide && this.getData() != null)
			this.getData().openStorageMenu(player);
	}
	
	public void onDestroyed()
	{
		if(this.level.isClientSide)
			return;
		UniversalTraderData data = this.getData();
		//Remove the data from the register
		TradingOffice.removeTrader(this.traderID);
		//Dump the inventory contents
		if(data != null)
		{
			this.dumpContents(data);
		}
	}
	
	protected void dumpContents(@Nonnull UniversalTraderData data)
	{
		InventoryUtil.dumpContents(this.level, this.getBlockPos(), MoneyUtil.getCoinsOfValue(data.getStoredMoney()));
	}
	
}

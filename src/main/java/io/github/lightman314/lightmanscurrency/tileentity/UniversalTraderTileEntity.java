package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class UniversalTraderTileEntity extends TileEntity implements IOwnableTileEntity{

	
	UUID traderID = null;
	
	protected UniversalTraderData getData()
	{
		if(this.traderID != null)
			return TradingOffice.getData(this.traderID);
		LightmansCurrency.LogError("Trader ID is null. Cannot get the trader data (" + DebugUtil.getSideText(this.world) + ").");
		return null;
	}
	
	public UniversalTraderTileEntity(TileEntityType<?> type)
	{
		super(type);
	}
	
	public UUID getTraderID()
	{
		return this.traderID;
	}
	
	public void updateNames(PlayerEntity player)
	{
		if(this.getData() == null)
		{
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.updateNames," + DebugUtil.getSideText(player) + ").");
			return;
		}
		this.getData().getCoreSettings().updateNames(player);
	}
	
	public boolean hasPermission(PlayerEntity player, String permission)
	{
		if(this.getData() == null)
		{
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.hasPermission," + DebugUtil.getSideText(player) + ").");
			return true;
		}
		return this.getData().hasPermission(player, permission);
	}
	
	public int getPermissionLevel(PlayerEntity player, String permission)
	{
		if(this.getData() == null)
		{
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.getPermisisonLevel," + DebugUtil.getSideText(player) + ").");
			return Integer.MAX_VALUE;
		}
		return this.getData().getPermissionLevel(player, permission);
	}
	
	public boolean canBreak(PlayerEntity player)
	{
		return this.hasPermission(player, Permissions.BREAK_TRADER);
	}
	
	public void init(PlayerEntity owner)
	{
		this.init(owner, null);
	}
	
	public void init(PlayerEntity owner, String customName)
	{
		if(this.world.isRemote)
			return;
		if(this.traderID == null)
		{
			UniversalTraderData traderData = createInitialData(owner);
			if(customName != null)
			{
				traderData.getCoreSettings().setCustomName(null, customName);
			}
			this.traderID = TradingOffice.registerTrader(traderData, owner);
			TileEntityUtil.sendUpdatePacket(this);
		}
	}
	
	protected abstract UniversalTraderData createInitialData(PlayerEntity owner);
	
	@Override
	public void onLoad()
	{
		if(world.isRemote)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(this));
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		if(this.traderID != null)
			compound.putUniqueId("ID", this.traderID);
		
		return super.write(compound);
		
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		if(compound.contains("ID"))
			this.traderID = compound.getUniqueId("ID");
		
		super.read(state, compound);
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
		this.read(this.getBlockState(), compound);
	}
	
	public void openStorageMenu(PlayerEntity player)
	{
		if(!this.world.isRemote && this.getData() != null)
			this.getData().openStorageMenu((ServerPlayerEntity)player);
	}
	
	public void onDestroyed()
	{
		if(this.world.isRemote)
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
		InventoryUtil.dumpContents(this.world, this.pos, MoneyUtil.getCoinsOfValue(data.getInternalStoredMoney()));
	}
	
}

package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
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
			LightmansCurrency.LogError("Trader Data for trader of id '" + this.traderID + "' is null (tileEntity.isOwner," + (this.level.isClientSide  ? "client" : "server" ) + ").");
			return true;
		}
		//else
		//	LightmansCurrency.LOGGER.info("Trader Data for trader of id '" + this.traderID + "' is NOT NULL (tileEntity.updateOwner," + (this.world.isRemote ? "client" : "server" ) + ").");
		return this.getData().isOwner(player);
	}
	
	public boolean canBreak(Player player)
	{
		if(this.isOwner(player))
			return true;
		return player.hasPermissions(2) && player.isCreative();
	}
	
	public void init(Entity owner)
	{
		this.init(owner, null);
	}
	
	public void init(Entity owner, String customName)
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
			TradingOffice.registerTrader(this.traderID, traderData);
			TileEntityUtil.sendUpdatePacket(this);
		}
	}
	
	protected abstract UniversalTraderData createInitialData(Entity owner);
	
	@Override
	public void onLoad()
	{
		if(level.isClientSide)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(this));
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
	{
		if(this.traderID != null)
			compound.putUUID("ID", this.traderID);
		
		return super.save(compound);
		
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		if(compound.contains("ID"))
			this.traderID = compound.getUUID("ID");
		
		super.load(compound);
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
		this.load(compound);
	}
	
	public void openStorageMenu(Player player)
	{
		if(!this.level.isClientSide && this.getData() != null)
			this.getData().openStorageMenu((ServerPlayer)player);
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
		InventoryUtil.dumpContents(this.level, this.worldPosition, MoneyUtil.getCoinsOfValue(data.getStoredMoney()));
	}
	
}

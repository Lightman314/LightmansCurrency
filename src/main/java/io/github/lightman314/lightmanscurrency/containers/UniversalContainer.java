package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateContainerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public abstract class UniversalContainer extends AbstractContainerMenu {
	
	private static final List<UniversalContainer> activeContainers = new ArrayList<>();
	
	public final UUID traderID;
	private UniversalTraderData clientCopy;
	public final Player player;
	
	public UniversalTraderData getRawData()
	{
		if(this.isServer())
			return TradingOffice.getData(this.traderID);
		else
			return clientCopy;
	}
	
	public boolean isClient() { return this.player.level.isClientSide; }
	public boolean isServer() { return !this.player.level.isClientSide; }
	
	protected UniversalContainer(MenuType<?> type, int windowID, UUID traderID, Player player, CompoundTag traderData)
	{
		super(type, windowID);
		this.player = player;
		this.traderID = traderID;
		activeContainers.add(this);
		this.clientCopy = IUniversalDataDeserializer.Deserialize(traderData);
	}
	
	protected UniversalContainer(MenuType<?> type, int windowID, UUID traderID, Player player)
	{
		super(type, windowID);
		this.player = player;
		this.traderID = traderID;
		activeContainers.add(this);
	}
	
	public static void onDataModified(UUID traderID)
	{
		activeContainers.forEach(container ->{
			if(container.traderID.equals(traderID))
			{
				if(container.isServer())
				{
					//Send an update packet to the client
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(container.player), new MessageUpdateContainerData(container.player.getUUID(), container.getRawData().write(new CompoundTag())));
				}
				container.onDataModified();
			}
		});
	}
	
	public void onDataUpdated(CompoundTag traderData)
	{
		this.clientCopy = IUniversalDataDeserializer.Deserialize(traderData);
		onDataModified();
	}
	
	protected abstract void onDataModified();
	
	public static void onForceReopen(UUID traderID)
	{
		//Create a local copy of the active container list, as re-opening the containers will modify the active containers list
		List<UniversalContainer> oldActiveContainers = new ArrayList<>();
		activeContainers.forEach(container -> oldActiveContainers.add(container));
		oldActiveContainers.forEach(container ->
		{
			if(container.traderID.equals(traderID) && container.isServer())
			{
				container.onForceReopen();
			}
		});
	}
	
	protected abstract void onForceReopen();
	
	@Override
	public void removed(Player playerIn)
	{
		if(activeContainers.contains(this))
			activeContainers.remove(this);
	}
	
}

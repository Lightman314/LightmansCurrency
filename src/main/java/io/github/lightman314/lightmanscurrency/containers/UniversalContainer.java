package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;

@Deprecated
public abstract class UniversalContainer extends Container{
	
	private static final List<UniversalContainer> activeContainers = new ArrayList<>();
	
	public final UUID traderID;
	public final PlayerEntity player;
	
	public UniversalTraderData getRawData()
	{
		if(this.isServer())
			return TradingOffice.getData(this.traderID);
		else
			return ClientTradingOffice.getData(this.traderID);
	}
	
	public boolean isClient() { return this.player.world.isRemote; }
	public boolean isServer() { return !this.player.world.isRemote; }
	
	protected UniversalContainer(ContainerType<?> type, int windowID, UUID traderID, PlayerEntity player)
	{
		super(type, windowID);
		this.player = player;
		this.traderID = traderID;
		activeContainers.add(this);
	}
	
	@Deprecated
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
	public void onContainerClosed(PlayerEntity playerIn)
	{
		if(activeContainers.contains(this))
			activeContainers.remove(this);
	}
	
}

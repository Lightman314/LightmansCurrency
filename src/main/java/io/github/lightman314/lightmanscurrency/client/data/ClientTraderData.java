package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTraderData {

	private static final Map<Long, TraderData> loadedTraders = new HashMap<>();
	
	public static List<TraderData> GetAllTraders() { return new ArrayList<>(loadedTraders.values()); }
	
	public static TraderData GetTrader(long traderID) {
		if(loadedTraders.containsKey(traderID))
			return loadedTraders.get(traderID);
		return null;
	}
	
	public static void ClearTraders() { loadedTraders.clear(); }
	
	public static void UpdateTrader(CompoundNBT compound)
	{
		long traderID = compound.getLong("ID");
		if(loadedTraders.containsKey(traderID))
		{
			loadedTraders.get(traderID).load(compound);
		}
		else
		{
			TraderData trader = TraderData.Deserialize(true, compound);
			if(trader != null)
				loadedTraders.put(traderID, trader);
		}
	}
	
	public static void RemoveTrader(long traderID)
	{
		loadedTraders.remove(traderID);
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		//Reset loaded traders
		ClearTraders();
	}
	
}
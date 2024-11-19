package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTraderData {

	private static final Map<Long, TraderData> loadedTraders = new HashMap<>();
	
	public static List<TraderData> GetAllTraders() { return new ArrayList<>(loadedTraders.values()); }
	
	public static TraderData GetTrader(long traderID) {
		if(loadedTraders.containsKey(traderID))
			return loadedTraders.get(traderID);
		return null;
	}
	
	public static void ClearTraders() { loadedTraders.clear(); }
	
	public static void UpdateTrader(CompoundTag compound)
	{
		long traderID = compound.getLong("ID");
		if(loadedTraders.containsKey(traderID))
		{
			loadedTraders.get(traderID).load(compound, LookupHelper.getRegistryAccess());
		}
		else
		{
			TraderData trader = TraderData.Deserialize(true, compound, LookupHelper.getRegistryAccess());
			if(trader != null)
			{
				loadedTraders.put(traderID, trader);
				trader.OnRegisteredToOffice();
			}
		}
	}
	
	public static void RemoveTrader(long traderID) { loadedTraders.remove(traderID); }
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		//Reset loaded traders
		ClearTraders();
	}
	
}

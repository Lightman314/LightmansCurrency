package io.github.lightman314.lightmanscurrency.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTradingOffice {

	private static Map<UUID, UniversalTraderData> loadedTraders = new HashMap<>();
	
	public static List<UniversalTraderData> getTraderList()
	{
		return loadedTraders.values().stream().collect(Collectors.toList());
	}
	
	public static UniversalTraderData getData(UUID traderID)
	{
		if(loadedTraders.containsKey(traderID))
			return loadedTraders.get(traderID);
		return null;
	}
	
	public static void initData(List<UniversalTraderData> data)
	{
		loadedTraders.clear();
		data.forEach(trader ->{
			loadedTraders.put(trader.getTraderID(), trader.flagAsClient());
		});
	}
	
	public static void updateTrader(CompoundTag compound)
	{
		UUID traderID = compound.getUUID("ID");
		if(loadedTraders.containsKey(traderID)) //Have existing trader read the data if present
			loadedTraders.get(traderID).read(compound);
		else //New trader was added, so deserialize the data and add it to the map
			loadedTraders.put(traderID, TradingOffice.Deserialize(compound).flagAsClient());
	}
	
	public static void removeTrader(UUID traderID)
	{
		if(loadedTraders.containsKey(traderID))
			loadedTraders.remove(traderID);
	}
	
}

package io.github.lightman314.lightmanscurrency.util;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeSupplier;

public class SafeTradingOffice {

	public static UniversalTraderData getData(UUID traderID)
	{
		return DistExecutor.safeRunForDist(() -> new ClientDataGetter(traderID), () -> new ServerDataGetter(traderID));
	}
	
	public static UniversalTraderData getData(boolean isServer, UUID traderID)
	{
		if(isServer)
			return TradingOffice.getData(traderID);
		else
			return ClientTradingOffice.getData(traderID);
	}
	
	private static class ClientDataGetter implements SafeSupplier<UniversalTraderData>
	{
		private static final long serialVersionUID = -3526711238572004168L;
		private final UUID traderID;
		private ClientDataGetter(UUID traderID) { this.traderID = traderID; }
		@Override
		public UniversalTraderData get() {
			return ClientTradingOffice.getData(this.traderID);
		}
	}
	
	private static class ServerDataGetter implements SafeSupplier<UniversalTraderData>
	{
		private static final long serialVersionUID = 8603984954634374023L;
		private final UUID traderID;
		private ServerDataGetter(UUID traderID) { this.traderID = traderID; }
		@Override
		public UniversalTraderData get() {
			return TradingOffice.getData(this.traderID);
		}
	}
	
}

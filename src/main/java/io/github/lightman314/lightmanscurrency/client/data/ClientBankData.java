package io.github.lightman314.lightmanscurrency.client.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientBankData {

	private static final Map<UUID,BankAccount> loadedBankAccounts = new HashMap<>();
	private static BankReference lastSelectedAccount = null;
	
	public static BankAccount GetPlayerBankAccount(UUID playerID)
	{
		if(loadedBankAccounts.containsKey(playerID))
			return loadedBankAccounts.get(playerID);
		//Return an empty account until the server notifies us of the new accounts creation.
		LightmansCurrency.LogWarning("No bank account for player with id " + playerID.toString() + " is present on the client.");
		return new BankAccount();
	}
	
	public static void ClearBankAccounts() { loadedBankAccounts.clear(); }
	
	public static void UpdateBankAccount(UUID player, CompoundTag compound)
	{
		try {
			BankAccount account = new BankAccount(compound);
			if(player != null && account != null)
				loadedBankAccounts.put(player, account);
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public static void UpdateLastSelectedAccount(BankReference reference) {
		lastSelectedAccount = reference;
	}
	
	public static BankReference GetLastSelectedAccount() {
		return lastSelectedAccount;
	}
	
	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		loadedBankAccounts.clear();
		lastSelectedAccount = null;
	}
	
}

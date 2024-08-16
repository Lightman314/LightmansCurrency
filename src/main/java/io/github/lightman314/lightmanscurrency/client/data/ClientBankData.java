package io.github.lightman314.lightmanscurrency.client.data;

import java.util.*;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientBankData {

	private static final Map<UUID,BankAccount> loadedBankAccounts = new HashMap<>();
	private static BankReference lastSelectedAccount = null;
	
	public static BankAccount GetPlayerBankAccount(UUID playerID)
	{
		if(loadedBankAccounts.containsKey(playerID))
			return loadedBankAccounts.get(playerID);
		//Return an empty account until the server notifies us of the new accounts creation.
		LightmansCurrency.LogWarning("No bank account for player with id " + playerID.toString() + " is present on the client.");
		return new BankAccount().flagAsClient();
	}
	
	public static void ClearBankAccounts() { loadedBankAccounts.clear(); }

	public static void DeleteBankAccount(@Nonnull UUID player) { loadedBankAccounts.remove(player); }

	public static void UpdateBankAccount(UUID player, CompoundTag compound)
	{
		try {
			BankAccount account = new BankAccount(compound, LookupHelper.getRegistryAccess(true)).flagAsClient();
			if(player != null && account != null)
				loadedBankAccounts.put(player, account);
		} catch(Exception e) { LightmansCurrency.LogError("Error loading bank account on client!",e); }
	}
	
	public static void UpdateLastSelectedAccount(BankReference reference) {
		lastSelectedAccount = reference;
		lastSelectedAccount.flagAsClient();
	}
	
	public static BankReference GetLastSelectedAccount() {
		return lastSelectedAccount;
	}

	public static List<BankReference> GetPlayerBankAccounts() {
		List<BankReference> list = new ArrayList<>();
		for(UUID player : loadedBankAccounts.keySet())
			list.add(PlayerBankReference.of(player).flagAsClient());
		return list;
	}

	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		loadedBankAccounts.clear();
		lastSelectedAccount = null;
	}
	
}

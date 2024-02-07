package io.github.lightman314.lightmanscurrency.common.bank;

import java.util.*;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.data.bank.SPacketClearClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.CPacketSelectBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.data.bank.SPacketUpdateClientBank;
import io.github.lightman314.lightmanscurrency.network.message.data.bank.SPacketSyncSelectedBankAccount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class BankSaveData extends SavedData {

	
	private final Map<UUID, Pair<BankAccount,BankReference>> playerBankData = new HashMap<>();
	
	private BankSaveData() {}
	private BankSaveData(CompoundTag compound) {
		
		ListTag bankData = compound.getList("PlayerBankData", Tag.TAG_COMPOUND);
		for(int i = 0; i < bankData.size(); ++i)
		{
			CompoundTag tag = bankData.getCompound(i);
			UUID player = tag.getUUID("Player");
			BankAccount bankAccount = loadBankAccount(player, tag.getCompound("BankAccount"));
			BankReference lastSelected = BankReference.load(tag.getCompound("LastSelected"));
			playerBankData.put(player, Pair.of(bankAccount, lastSelected));
		}
	}
	
	@Nonnull
	public CompoundTag save(CompoundTag compound) {
		
		ListTag bankData = new ListTag();
		this.playerBankData.forEach((player,data) -> {
			CompoundTag tag = new CompoundTag();
			tag.putUUID("Player", player);
			tag.put("BankAccount", data.getFirst().save());
			tag.put("LastSelected", data.getSecond().save());
			bankData.add(tag);
		});
		compound.put("PlayerBankData", bankData);
		
		return compound;
	}
	
	private static BankAccount loadBankAccount(UUID player, CompoundTag compound) {
		BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(player), compound);
		try {
			bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
			bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(false));
		} catch(Throwable t) { t.printStackTrace(); }
		return bankAccount;
	}
	
	private static BankAccount generateBankAccount(UUID player) {
		BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(player));
		try {
			bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
			bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(false));
		} catch(Throwable t) { t.printStackTrace(); }
		return bankAccount;
	}
	
	private static BankSaveData get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(Level.OVERWORLD);
			if(level != null)
				return level.getDataStorage().computeIfAbsent(BankSaveData::new, BankSaveData::new, "lightmanscurrency_bank_data");
		}
		return null;
	}
	
	public static List<BankReference> GetPlayerBankAccounts() {
		List<BankReference> results = new ArrayList<>();
		BankSaveData bsd = get();
		if(bsd != null)
			bsd.playerBankData.forEach((player,data) -> results.add(PlayerBankReference.of(player)));
		return results;
	}
	
	public static BankAccount GetBankAccount(Player player) { return GetBankAccount(player.level.isClientSide, player.getUUID()); }
	
	public static BankAccount GetBankAccount(boolean isClient, UUID player) {
		if(isClient)
		{
			return ClientBankData.GetPlayerBankAccount(player);
		}
		else
		{
			BankSaveData bsd = get();
			if(bsd != null)
			{
				if(bsd.playerBankData.containsKey(player))
					return bsd.playerBankData.get(player).getFirst();
				//Create a new bank account for the player
				BankAccount newAccount = generateBankAccount(player);
				bsd.playerBankData.put(player, Pair.of(newAccount, PlayerBankReference.of(player)));
				MarkBankAccountDirty(player);
				return newAccount;
			}
			return null;
		}
	}
	
	public static void MarkBankAccountDirty(UUID player)
	{
		BankSaveData bsd = get();
		if(bsd != null)
		{
			bsd.setDirty();
			//Send update packet to all connected clients
			BankAccount bankAccount = GetBankAccount(false, player);
			new SPacketUpdateClientBank(player, bankAccount.save()).sendToAll();
		}
	}
	
	public static BankReference GetSelectedBankAccount(Player player) {
		if(player.level.isClientSide)
		{
			ClientBankData.GetLastSelectedAccount();
		}
		else
		{
			BankSaveData bsd = get();
			if(bsd != null)
			{
				if(bsd.playerBankData.containsKey(player.getUUID()))
				{
					BankReference account = bsd.playerBankData.get(player.getUUID()).getSecond();
					if(!account.allowedAccess(player))
					{
						LightmansCurrency.LogInfo(player.getName().getString() + " is no longer allowed to access their selected bank account. Switching back to their personal account.");
						account = PlayerBankReference.of(player);
						SetSelectedBankAccount(player, account);
					}
					return account;
				}
				//Generate default bank account for the player
				BankReference account = PlayerBankReference.of(player);
				SetSelectedBankAccount(player,account);
				return account;
			}
		}
		return PlayerBankReference.of(player);
	}
	
	public static void SetSelectedBankAccount(Player player, BankReference account) {
		//Ignore if the account is null or the player isn't allowed to access it.
		if(account == null)
			return;
		if(player.level.isClientSide)
		{
			new CPacketSelectBankAccount(account).send();
		}
		else
		{
			if(!account.allowedAccess(player))
			{
				LightmansCurrency.LogInfo("Player does not have access to the selected account. Canceling selection.");
				return;
			}
			BankSaveData bsd = get();
			if(bsd != null)
			{
				if(bsd.playerBankData.containsKey(player.getUUID()))
				{
					bsd.playerBankData.put(player.getUUID(), Pair.of(bsd.playerBankData.get(player.getUUID()).getFirst(), account));
				}
				else
				{
					bsd.playerBankData.put(player.getUUID(), Pair.of(generateBankAccount(player.getUUID()),account));
					MarkBankAccountDirty(player.getUUID());
				}
				
				bsd.setDirty();
				try { new SPacketSyncSelectedBankAccount(account).sendTo(player);
				} catch(Throwable ignored) {}
			}
		}
	}
	
	@SubscribeEvent
	public static void OnPlayerLogin(PlayerLoggedInEvent event)
	{
		
		PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
		
		BankSaveData bsd = get();
		
		//Confirm the presence of the loading players bank account
		GetBankAccount(event.getEntity());

		SPacketClearClientBank.INSTANCE.sendToTarget(target);
		bsd.playerBankData.forEach((id, data) -> new SPacketUpdateClientBank(id, data.getFirst().save()).sendToTarget(target));
		
		//Update to let them know their selected bank account
		BankReference selectedAccount = GetSelectedBankAccount(event.getEntity());
		new SPacketSyncSelectedBankAccount(selectedAccount).sendToTarget(target);
		
	}
	
}

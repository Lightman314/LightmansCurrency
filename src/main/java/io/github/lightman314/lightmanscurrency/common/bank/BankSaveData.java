package io.github.lightman314.lightmanscurrency.common.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageInitializeClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageSelectBankAccount;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageUpdateClientBank;
import io.github.lightman314.lightmanscurrency.network.message.bank.SPacketSyncSelectedBankAccount;
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
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID)
public class BankSaveData extends SavedData {

	
	private final Map<UUID, Pair<BankAccount,AccountReference>> playerBankData = new HashMap<>();
	
	private BankSaveData() {}
	private BankSaveData(CompoundTag compound) {
		
		ListTag bankData = compound.getList("PlayerBankData", Tag.TAG_COMPOUND);
		for(int i = 0; i < bankData.size(); ++i)
		{
			CompoundTag tag = bankData.getCompound(i);
			UUID player = tag.getUUID("Player");
			BankAccount bankAccount = loadBankAccount(player, tag.getCompound("BankAccount"));
			AccountReference lastSelected = BankAccount.LoadReference(false, tag.getCompound("LastSelected"));
			playerBankData.put(player, Pair.of(bankAccount, lastSelected));
		}
	}
	
	public @NotNull CompoundTag save(CompoundTag compound) {
		
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
	
	public static List<AccountReference> GetPlayerBankAccounts() {
		List<AccountReference> results = new ArrayList<>();
		BankSaveData bsd = get();
		if(bsd != null)
			bsd.playerBankData.forEach((player,data) -> results.add(BankAccount.GenerateReference(false, player)));
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
				bsd.playerBankData.put(player, Pair.of(newAccount, BankAccount.GenerateReference(false, player)));
				MarkBankAccountDirty(player);
				return newAccount;
			}
			return null;
		}
	}

	/** @deprecated Only use to transfer bank account data from the old Trading Office. */
	@Deprecated
	public static void GiveOldBankAccount(UUID player, BankAccount account) {
		BankSaveData bsd = get();
		if(bsd != null)
		{
			if(bsd.playerBankData.containsKey(player))
				bsd.playerBankData.put(player, Pair.of(account, bsd.playerBankData.get(player).getSecond()));
			else
				bsd.playerBankData.put(player, Pair.of(account, BankAccount.GenerateReference(false, player)));
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
			CompoundTag compound = bankAccount.save();
			compound.putUUID("Player", player);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new MessageUpdateClientBank(compound));
		}
	}
	
	public static AccountReference GetSelectedBankAccount(Player player) {
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
					AccountReference account = bsd.playerBankData.get(player.getUUID()).getSecond();
					if(!account.allowedAccess(player))
					{
						LightmansCurrency.LogInfo(player.getName().getString() + " is no longer allowed to access their selected bank account. Switching back to their personal account.");
						account = BankAccount.GenerateReference(player);
						SetSelectedBankAccount(player, account);
					}
					return account;
				}
				//Generate default bank account for the player
				AccountReference account = BankAccount.GenerateReference(player);
				SetSelectedBankAccount(player,account);
				return account;
			}
		}
		return BankAccount.GenerateReference(player);
	}

	/** @deprecated Use only to transfer selected bank account from old Trading Office. */
	@Deprecated
	public static void GiveOldSelectedBankAccount(UUID player, AccountReference account) {
		BankSaveData bsd = get();
		if(bsd != null)
		{
			if(bsd.playerBankData.containsKey(player))
				bsd.playerBankData.put(player, Pair.of(bsd.playerBankData.get(player).getFirst(), account));
			else
			{
				bsd.playerBankData.put(player, Pair.of(generateBankAccount(player), account));
				MarkBankAccountDirty(player);
			}
			
			bsd.setDirty();
		}
	}
	
	public static void SetSelectedBankAccount(Player player, AccountReference account) {
		//Ignore if the account is null or the player isn't allowed to access it.
		if(account == null)
			return;
		if(player.level.isClientSide)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSelectBankAccount(account));
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
				try {
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketSyncSelectedBankAccount(account));
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
		
		CompoundTag compound = new CompoundTag();
		ListTag bankList = new ListTag();
		bsd.playerBankData.forEach((id, data) -> {
			CompoundTag tag = data.getFirst().save();
			tag.putUUID("Player", id);
			bankList.add(tag);
		});
		compound.put("BankAccounts", bankList);
		LightmansCurrencyPacketHandler.instance.send(target, new MessageInitializeClientBank(compound));
		
		//Update to let them know their selected bank account
		AccountReference selectedAccount = GetSelectedBankAccount(event.getEntity());
		LightmansCurrencyPacketHandler.instance.send(target, new SPacketSyncSelectedBankAccount(selectedAccount));
		
	}
	
}

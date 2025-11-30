package io.github.lightman314.lightmanscurrency.api.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Event called when a player dies and the wallet drops are calculated.<br>
 * Is {@link net.neoforged.bus.api.ICancellableEvent}. Cancelling will result in the players wallet being unchanged, and nothing will be dropped.<br>
 * {@link #keepWallet} is the current value of the <code>keepWallet</code> || <code>keepInventory</code> game rules.<br>
 * {@link #coinDropPercent} is the current value of the <code>coinDropPercent</code> game rule.<br>
 * Can use {@link #getWalletInventory()} to access the wallets inventory and add or remove coins.
 * Can use {@link #setDrops(List)} to manually set the list of items to be dropped,<br>
 * or {@link #addDrop(ItemStack)} or {@link #addDrops(Collection)} to add items to be dropped.<br>
 * Use {@link #getWalletInventory()} to get a container of the wallets contents that can be modified at will (container will become invalid if {@link #setWalletStack(ItemStack)} is called while you're using this container)<br>
 * Use {@link #getWalletStack()} to get a safe copy of the wallet stack. Note, changes made to this wallet stack will not be reflected in the final results, as this is only a copy.<br>
 * Use {@link #setWalletStack(ItemStack)} to replace the players equipped wallet completely. Set to {@link ItemStack#EMPTY} to unequip it entirely.<br>
 * Note: Default behaviour is done in {@link net.neoforged.bus.api.EventPriority#LOW}
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WalletDropEvent extends PlayerEvent implements ICancellableEvent {

	private Container walletInventory;
	private ItemStack walletStack;
	public Container getWalletInventory() { return this.walletInventory; }
	public ItemStack getWalletStack()
	{
		ItemStack result = this.walletStack.copy();
		if(WalletItem.isWallet(result))
			WalletItem.getDataWrapper(result).setContents(this.walletInventory, null);
		return result;
	}
	public void setWalletStack(ItemStack wallet) {
		this.walletStack = wallet.copy();
		this.walletInventory = WalletItem.getDataWrapper(wallet).getContents();
	}
	public final DamageSource source;
	private List<ItemStack> walletDrops = new ArrayList<>();
	
	public List<ItemStack> getDrops() { return this.walletDrops; }
	public void setDrops(List<ItemStack> drops) { this.walletDrops = new ArrayList<>(drops); }
	public void addDrop(ItemStack drop) { this.walletDrops.add(drop); }
	public void addDrops(Collection<ItemStack> drop) { this.walletDrops.addAll(drop); }
	public final boolean keepWallet;
	public final boolean destroyWallet;
	public final int coinDropPercent;
	
	public WalletDropEvent(Player player, WalletHandler walletHandler, DamageSource source, boolean keepWallet, boolean destroyWallet, int coinDropPercent)
	{
		super(player);
		this.walletStack = walletHandler.getWallet().copy();
		this.walletInventory = WalletItem.getDataWrapper(this.walletStack).getContents();
		this.source = source;
		this.keepWallet = keepWallet;
		this.destroyWallet = destroyWallet;
		this.coinDropPercent = coinDropPercent;
	}
	
}

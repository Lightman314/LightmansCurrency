package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.integration.Curios;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.extendedinventory.MessageUpdateWallet;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class WalletUtil {

	/**
     * Easy public access to the equipped wallets that functions regardless of which system (stand-alone, backpacked compatibility, curios) is being used to store the slot.
     */
    public static List<ItemStack> getEquippedWallets(Player player)
    {
    	AtomicReference<List<ItemStack>> foundWallets = new AtomicReference<>(new ArrayList<>());
    	
    	if(LightmansCurrency.isCuriosLoaded())
    	{
    		foundWallets.set(Curios.getEquippedWallets(player));
    	}
    	else
    	{
    		if(player.getInventory() instanceof IWalletInventory)
        	{
    			IWalletInventory inventory = (IWalletInventory)player.getInventory();
    			for(int i = 0; i < inventory.getWalletItems().size(); i++)
    			{
    				foundWallets.get().add(inventory.getWalletItems().get(i));
    			}
        	}
    	}
    	
    	return foundWallets.get();
    	
    }
    
    public static ItemStack getEquippedWallet(Player player)
    {
    	List<ItemStack> equippedWallets = getEquippedWallets(player);
    	return equippedWallets.size() > 0 ? equippedWallets.get(0) : ItemStack.EMPTY;
    }
    
    public static List<ItemStack> extractEquippedWallets(Player player)
    {
    	AtomicReference<List<ItemStack>> equippedWallets = new AtomicReference<>(new ArrayList<>());
    	if(LightmansCurrency.isCuriosLoaded())
    	{
    		equippedWallets.set(Curios.extractEquippedWallets(player));
    	}
    	else
    	{
    		if(player.getInventory() instanceof IWalletInventory)
    		{
    			IWalletInventory inventory = (IWalletInventory)player.getInventory();
    			for(int i = 0; i < inventory.getWalletItems().size(); i++)
    			{
    				equippedWallets.get().add(inventory.getWalletItems().get(i));
    				inventory.getWalletItems().set(i, ItemStack.EMPTY);
    			}
    		}
    	}
    	return equippedWallets.get();
    }
	
    //Gets an interactable that interprets all available wallets in the players inventory (prioritizing the equipped wallet of course)
    public static PlayerWallets getWallets(Player player)
    {
    	return new PlayerWallets(player);
    }
    
    public static class PlayerWallets
    {
    	
    	private final Player player;
    	
    	public boolean hasWallet() { return queryWalletData().size() > 0; }
    	
    	public boolean hasEquippedWallet()
    	{
    		List<WalletData> data = queryWalletData();
    		if(data.size() > 0)
    			return data.get(0).isEquipped;
    		return false;
    	}
    	
    	public boolean canPickup() { for(WalletData data : this.queryWalletData()) { if(data.canPickup()) return true; } return false; }
    	
    	protected PlayerWallets(Player player) { this.player = player; }
    	
    	private List<WalletData> queryWalletData()
    	{
    		List<WalletData> foundWallets = new ArrayList<>();
    		if(this.player == null)
    			return foundWallets;
    		//Get the equipped wallet stack first so that it gets priority for taking/extracting.
    		List<ItemStack> equippedWallets = getEquippedWallets(this.player);
    		for(int i = 0; i < equippedWallets.size(); i++)
    		{
    			if(equippedWallets.get(i).getItem() instanceof WalletItem)
    				foundWallets.add(new WalletData(equippedWallets.get(i), true));
    		}
    		//Get every other wallet in the players inventory
    		for(int i = 0; i < player.getInventory().getContainerSize(); i++)
    		{
    			if(!ExtendedPlayerInventory.isWalletIndex(i)) //Ignore the wallet index as we've already gotten the wallet from there
    			{
    				if(player.getInventory().getItem(i).getItem() instanceof WalletItem)
    				{
    					foundWallets.add(new WalletData(player.getInventory().getItem(i)));
    				}
    			}
    		}
    		return foundWallets;
    	}
    	
    	/**
    	 * The total amount of stored money inside the wallets inventories.
    	 */
    	public long getStoredMoney()
    	{
    		List<WalletData> foundWallets = this.queryWalletData();
    		long value = 0;
    		for(WalletData wallet : foundWallets)
    			value += MoneyUtil.getValue(wallet.getInventory());
    		return value;
		}
    	
    	public long extractMoney(long value) { return extractMoney(value, null); }
    	
    	public long extractMoney(long value, @Nullable Container overflowSlots)
    	{
    		List<WalletData> foundWallets = queryWalletData();
    		
    		long valueToTake = value;
    		for(int i = 0; i < foundWallets.size() && valueToTake > 0; i++)
    		{
    			NonNullList<ItemStack> walletInventory = foundWallets.get(i).getInventory();
    			valueToTake = MoneyUtil.takeObjectsOfValue(valueToTake, walletInventory, true);
    			foundWallets.get(i).setInventory(walletInventory);
    		}
    		this.markPlayerDirty();
    		return valueToTake;
    	}
    	
    	/**
    	 * Picks up coins and places them inside wallets that are capable of picking up coins on the ground.
    	 * @param coinStack The stack of coins to be picked up.
    	 * @return Coins that didn't have room in any of the applicable wallets to be placed.
    	 */
    	public ItemStack PickupCoin(ItemStack coinStack)
    	{
    		List<WalletData> foundWallets = this.queryWalletData();
    		ItemStack extraCoins = coinStack;
    		for(int i = 0; i < foundWallets.size() && !extraCoins.isEmpty(); i++)
    		{
    			if(foundWallets.get(i).canPickup())
    				extraCoins = foundWallets.get(i).PlaceCoin(extraCoins);
    		}
    		this.markPlayerDirty();
    		return extraCoins;
    	}
    	
    	/**
    	 * Places the coins inside any wallets inventories.
    	 * @param coinStack The stack of coins to be placed.
    	 * @return Coins that didn't have room in any of the wallets to be placed.
    	 */
    	public ItemStack PlaceCoin(ItemStack coinStack)
    	{
    		List<WalletData> foundWallets = this.queryWalletData();
    		ItemStack extraCoins = coinStack;
    		for(int i = 0; i < foundWallets.size() && !extraCoins.isEmpty(); i++)
    		{
    			extraCoins = foundWallets.get(i).PlaceCoin(extraCoins);
    		}
    		this.markPlayerDirty();
    		return extraCoins;
		}
    	
    	private void markPlayerDirty()
    	{
    		if(this.player != null)
    		{
    			this.player.getInventory().setChanged();
    			//Send update message to the clients if one of the found wallets is the equipped wallet.
    			List<WalletData> wallets = this.queryWalletData();
    			if(wallets.size() > 0 && wallets.get(0).isEquipped && !LightmansCurrency.isCuriosLoaded())
    			{
    				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new MessageUpdateWallet(player.getId(), wallets.get(0).wallet));
    			}
    		}
    	}
    	
    	private static class WalletData
    	{
    		public final ItemStack wallet;
    		public final boolean isEquipped;
    		private boolean canPickup() { return WalletItem.CanPickup((WalletItem)wallet.getItem()); }
    		//private boolean canConvert() { return WalletItem.CanConvert((WalletItem)wallet.getItem()); }
    		//private boolean autoConvert() { return WalletItem.getAutoConvert(wallet); }
    		//Inventory accessors
    		public ItemStack PlaceCoin(ItemStack coins) { return WalletItem.PickupCoin(wallet, coins); }
    		public NonNullList<ItemStack> getInventory() { return WalletItem.getWalletInventory(wallet); }
    		public void setInventory(NonNullList<ItemStack> inventory) { WalletItem.putWalletInventory(wallet, inventory); }
    		
    		protected WalletData(ItemStack walletStack)
    		{
    			this.wallet = walletStack;
    			this.isEquipped = false;
    		}
    		
    		protected WalletData(ItemStack walletStack, boolean isEquipped)
    		{
    			this.wallet = walletStack;
    			this.isEquipped = isEquipped;
    		}
    	}
    	
    }
	
}

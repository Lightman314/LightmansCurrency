package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletBankMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.slots.BlacklistSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class WalletMenuBase extends AbstractContainerMenu {

	private static int maxWalletSlots = 0;
	public static int getMaxWalletSlots() { return maxWalletSlots; }
	public static void updateMaxWalletSlots(int slotCount) { maxWalletSlots = Math.max(maxWalletSlots, slotCount); }
	
	protected final Container dummyInventory = new SimpleContainer(1);
	
	protected final int walletStackIndex;
	public final boolean isEquippedWallet() { return this.walletStackIndex < 0; }
	public final int getWalletStackIndex() { return this.walletStackIndex; }
	
	protected final Inventory inventory;
	public final boolean hasWallet() { ItemStack wallet = this.getWallet(); return !wallet.isEmpty() && wallet.getItem() instanceof WalletItem; }
	public final ItemStack getWallet()
	{
		if(this.isEquippedWallet())
			return LightmansCurrency.getWalletStack(this.inventory.player);
		return this.inventory.getItem(this.walletStackIndex);
	}
	
	private boolean autoConvert;
	public boolean canExchange() { return WalletItem.CanConvert(this.walletItem); }
	public boolean canPickup() { return WalletItem.CanPickup(this.walletItem); }
	public boolean hasBankAccess() { return WalletItem.HasBankAccess(this.walletItem); }
	public boolean getAutoExchange() { return this.autoConvert; }
	public void ToggleAutoConvert() { this.autoConvert = !this.autoConvert; this.saveWalletContents(); }
	
	protected final Container coinInput;
	
	protected final WalletItem walletItem;
	
	public final Player player;
	public Player getPlayer() { return this.player; }
	
	protected WalletMenuBase(MenuType<?> type, int windowID, Inventory inventory, int walletStackIndex) {
		super(type, windowID);
		
		this.inventory = inventory;
		this.player = this.inventory.player;
		
		this.walletStackIndex = walletStackIndex;
		
		Item item = this.getWallet().getItem();
		if(item instanceof WalletItem)
			this.walletItem = (WalletItem)item;
		else
			this.walletItem = null;
		
		this.coinInput = new SimpleContainer(WalletItem.InventorySize(this.walletItem));
		this.reloadWalletContents();
		
		this.autoConvert = WalletItem.getAutoConvert(this.getWallet());
		
	}

	protected final void addInventorySlot(int x, int y, int index)
	{
		if(index == this.walletStackIndex)
			this.addSlot(new DisplaySlot(this.inventory, index, x, y));
		else
			this.addSlot(new BlacklistSlot(this.inventory, index, x, y, this.inventory, this.walletStackIndex));
	}
	
	protected final void addCoinSlots(int yPosition) {
		for(int y = 0; (y * 9) < this.coinInput.getContainerSize(); y++)
		{
			for(int x = 0; x < 9 && (x + y * 9) < this.coinInput.getContainerSize(); x++)
			{
				this.addSlot(new CoinSlot(this.coinInput, x + y * 9, 8 + x * 18, yPosition + y * 18).addListener(this::saveWalletContents));
			}
		}
	}
	
	protected final void addDummySlots(int slotLimit) {
		while(this.slots.size() < slotLimit) {
			this.addSlot(new DisplaySlot(this.dummyInventory, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2));
		}
	}
	
	public final void reloadWalletContents() {
		NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(getWallet());
		for(int i = 0; i < this.coinInput.getContainerSize() && i < walletInventory.size(); i++)
		{
			this.coinInput.setItem(i, walletInventory.get(i));
		}
	}
	
	public final int getRowCount() { return 1 + ((this.coinInput.getContainerSize() - 1)/9); }
	
	public final int getSlotCount() { return this.coinInput.getContainerSize(); }
	
	@Override
	public boolean stillValid(@Nonnull Player playerIn) { this.validateHasWallet(); return true; }

	public final boolean validateHasWallet() {
		if(!this.hasWallet())
		{
			if(this.walletStackIndex < 0)
				LightmansCurrency.LogWarning("Forcibly closing the wallet menu, as the player no longer has a wallet equipped!");
			else
				LightmansCurrency.LogWarning("Forcibly closing the wallet menu, as the player is no longer holding a wallet in slot " + this.walletStackIndex + "!");
			this.player.closeContainer();
			return true;
		}
		return false;
	}

	public final void saveWalletContents()
	{
		if(this.validateHasWallet())
			return;
		//Write the bag contents back into the item stack
		NonNullList<ItemStack> walletInventory = NonNullList.withSize(WalletItem.InventorySize(this.walletItem), ItemStack.EMPTY);
		for(int i = 0; i < walletInventory.size() && i < this.coinInput.getContainerSize(); i++)
		{
			walletInventory.set(i, this.coinInput.getItem(i));
		}
		WalletItem.putWalletInventory(this.getWallet(), walletInventory);
		
		if(this.autoConvert != WalletItem.getAutoConvert(this.getWallet()))
			WalletItem.toggleAutoConvert(this.getWallet());
		
	}
	
	public final void ConvertCoins()
	{
		MoneyUtil.ExchangeAllCoinsUp(this.coinInput);
		MoneyUtil.SortCoins(this.coinInput);
		this.saveWalletContents();
	}
	
	public final ItemStack PickupCoins(ItemStack stack)
	{
		
		ItemStack returnValue = stack.copy();
		
		for(int i = 0; i < this.coinInput.getContainerSize() && !returnValue.isEmpty(); i++)
		{
			ItemStack thisStack = this.coinInput.getItem(i);
			if(thisStack.isEmpty())
			{
				this.coinInput.setItem(i, returnValue.copy());
				returnValue = ItemStack.EMPTY;
			}
			else if(InventoryUtil.ItemMatches(thisStack, returnValue))
			{
				int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxStackSize() - thisStack.getCount());
				thisStack.setCount(thisStack.getCount() + amountToAdd);
				returnValue.setCount(returnValue.getCount() - amountToAdd);
			}
		}
		
		if(this.autoConvert)
			this.ConvertCoins();
		else
			this.saveWalletContents();
		
		return returnValue;
	}

	public static void OnWalletUpdated(Entity entity) {
		if(entity instanceof Player player && player.containerMenu instanceof WalletMenuBase menu)
			menu.reloadWalletContents();
	}

	public static void SafeOpenWalletMenu(@Nonnull ServerPlayer player, int walletIndex) { SafeOpenWallet(player, walletIndex, new WalletMenuProvider(walletIndex)); }

	public static void SafeOpenWalletBankMenu(@Nonnull ServerPlayer player, int walletIndex) { SafeOpenWallet(player, walletIndex, new WalletBankMenuProvider(walletIndex));}

	public static void SafeOpenWallet(@Nonnull ServerPlayer player, int walletIndex, @Nonnull MenuProvider menu) { SafeOpenWallet(player, walletIndex, menu, new WalletDataWriter(walletIndex)); }

	public static void SafeOpenWallet(@Nonnull ServerPlayer player, int walletIndex, @Nonnull MenuProvider menu, @Nonnull Consumer<FriendlyByteBuf> dataWriter) {
		if (walletIndex < 0)
		{
			if(!WalletItem.isWallet(LightmansCurrency.getWalletStack(player)))
			{
				player.sendMessage(EasyText.translatable("message.lightmanscurrency.wallet.none_equipped"), new UUID(0,0));
                return;
			}
			NetworkHooks.openGui(player, menu, dataWriter);
		}
        else
		{
			Inventory inventory = player.getInventory();
			if(walletIndex >= inventory.getContainerSize())
				return;
			if(!WalletItem.isWallet(inventory.getItem(walletIndex)))
				return;
			NetworkHooks.openGui(player, menu, dataWriter);
		}
	}

	public record WalletDataWriter(int walletIndex) implements Consumer<FriendlyByteBuf>
	{
		@Override
		public void accept(FriendlyByteBuf buffer) { buffer.writeInt(this.walletIndex); }
	}



}

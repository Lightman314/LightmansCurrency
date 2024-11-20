package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletBankMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.providers.WalletMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.slots.BlacklistSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class WalletMenuBase extends EasyMenu {

	protected final Container dummyInventory = new SimpleContainer(1);

	protected final int walletStackIndex;
	public final boolean isEquippedWallet() { return this.walletStackIndex < 0; }
	public final int getWalletStackIndex() { return this.walletStackIndex; }

	public final boolean hasWallet() { ItemStack wallet = this.getWallet(); return !wallet.isEmpty() && wallet.getItem() instanceof WalletItem; }
	public final ItemStack getWallet()
	{
		if(this.isEquippedWallet())
			return CoinAPI.API.getEquippedWallet(this.inventory.player);
		return this.inventory.getItem(this.walletStackIndex);
	}

	private boolean autoExchange;
	public boolean canExchange() { return WalletItem.CanExchange(this.walletItem); }
	public boolean canPickup() { return WalletItem.CanPickup(this.walletItem); }
	public boolean hasBankAccess() { return WalletItem.HasBankAccess(this.walletItem); }
	public boolean getAutoExchange() { return this.autoExchange; }
	public void ToggleAutoExchange() { this.autoExchange = !this.autoExchange; this.saveWalletContents(); }

	protected final SimpleContainer coinInput;
	public final int coinSlotHeight;
	public final int coinSlotWidth;
	public final int bonusWidth;
	public final int halfBonusWidth;

	protected final WalletItem walletItem;

	private final List<CoinSlot> coinSlots = new ArrayList<>();
	@Nonnull
	public List<CoinSlot> getCoinSlots() { return ImmutableList.copyOf(this.coinSlots); }

	public Player getPlayer() { return this.player; }

	protected WalletMenuBase(MenuType<?> type, int windowID, Inventory inventory, int walletStackIndex) {
		super(type, windowID, inventory);

		this.walletStackIndex = walletStackIndex;

		ItemStack wallet = this.getWallet();
		Item item = wallet.getItem();
		if(item instanceof WalletItem wi)
			this.walletItem = wi;
		else
			this.walletItem = null;

		int walletSize = WalletItem.InventorySize(wallet);
		this.coinInput = new SimpleContainer(walletSize);
		this.coinSlotHeight = Math.min(6, MathUtil.DivideByAndRoundUp(walletSize,9));
		if(walletSize > 9 * 6)
		{
			this.coinSlotWidth = MathUtil.DivideByAndRoundUp(walletSize,6);
			this.bonusWidth = 18 * (this.coinSlotWidth - 9);
			this.halfBonusWidth = this.bonusWidth / 2;
		}
		else
		{
			this.coinSlotWidth = 9;
			this.bonusWidth = this.halfBonusWidth = 0;
		}
		this.reloadWalletContents();

		this.autoExchange = WalletItem.getAutoExchange(this.getWallet());

	}

	protected final void addInventorySlot(int x, int y, int index)
	{
		if(index == this.walletStackIndex)
			this.addSlot(new DisplaySlot(this.inventory, index, x, y));
		else
			this.addSlot(new BlacklistSlot(this.inventory, index, x, y, this.inventory, this.walletStackIndex));
	}

	protected final void addCoinSlots(int yPosition) {
		if(!this.coinSlots.isEmpty())
			return;
		int dummySlots = WalletItem.MAX_WALLET_SLOTS - this.coinInput.getContainerSize();
		int index = 0;
		for(int y = 0; y < this.coinSlotHeight; y++)
		{
			int xOff;
			if(y == this.coinSlotHeight - 1)
			{
				int emptySlots = this.coinSlotWidth - (this.coinInput.getContainerSize() - index);
				xOff = Math.max(0,emptySlots * 9);
			}
			else
				xOff = 0;
			for(int x = 0; x < this.coinSlotWidth && index < this.coinInput.getContainerSize(); x++)
			{
				CoinSlot slot = new CoinSlot(this.coinInput, index++, xOff + 8 + x * 18, yPosition + y * 18);
				slot.setListener(this::saveWalletContents);
				this.addSlot(slot);
				this.coinSlots.add(slot);
			}
		}
		if(dummySlots < 0)
			LightmansCurrency.LogWarning("Coin Slot count is larger than expected limit!");
		while(dummySlots-- > 0)
		{
			DisplaySlot slot = new DisplaySlot(this.dummyInventory,0,Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
			slot.active = false;
			this.addSlot(slot);
		}
	}

	public final void reloadWalletContents() {
		Container walletInventory = WalletItem.getWalletInventory(getWallet());
		for(int i = 0; i < this.coinInput.getContainerSize() && i < walletInventory.getContainerSize(); i++)
		{
			this.coinInput.setItem(i, walletInventory.getItem(i));
		}
	}

	public final int getSlotCount() { return this.coinInput.getContainerSize(); }

	@Override
	protected void onValidationTick(@Nonnull Player player) { this.validateHasWallet(); }

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
		WalletItem.putWalletInventory(this.getWallet(), this.coinInput);

		if(this.autoExchange != WalletItem.getAutoExchange(this.getWallet()))
			WalletItem.toggleAutoExchange(this.getWallet());

	}

	public final void ExchangeCoins()
	{
		CoinAPI.API.CoinExchangeAllUp(this.coinInput);
		CoinAPI.API.SortCoinsByValue(this.coinInput);
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

		if(this.autoExchange)
			this.ExchangeCoins();
		else
			this.saveWalletContents();

		return returnValue;
	}

	public static void OnWalletUpdated(Entity entity) {
		if(entity instanceof Player player && player.containerMenu instanceof WalletMenuBase menu)
			menu.reloadWalletContents();
	}

	public static void SafeOpenWalletMenu(@Nonnull ServerPlayer player, int walletIndex) { SafeOpenWallet(player, walletIndex, new WalletMenuProvider(walletIndex)); }

	public static void SafeOpenWalletBankMenu(@Nonnull ServerPlayer player, int walletIndex) {
		if(QuarantineAPI.IsDimensionQuarantined(player))
			EasyText.sendMessage(player,LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
		else
			SafeOpenWallet(player, walletIndex, new WalletBankMenuProvider(walletIndex));
	}

	public static void SafeOpenWallet(@Nonnull ServerPlayer player, int walletIndex, @Nonnull MenuProvider menu) { SafeOpenWallet(player, walletIndex, menu, new WalletDataWriter(walletIndex)); }

	public static void SafeOpenWallet(@Nonnull ServerPlayer player, int walletIndex, @Nonnull MenuProvider menu, @Nonnull Consumer<FriendlyByteBuf> dataWriter) {
		if (walletIndex < 0)
		{
			if(!WalletItem.isWallet(CoinAPI.API.getEquippedWallet(player)))
			{
				player.sendSystemMessage(LCText.MESSAGE_WALLET_NONE_EQUIPPED.get());
                return;
			}
			NetworkHooks.openScreen(player, menu, dataWriter);
		}
        else
		{
			Inventory inventory = player.getInventory();
			if(walletIndex >= inventory.getContainerSize())
				return;
			if(!WalletItem.isWallet(inventory.getItem(walletIndex)))
				return;
			NetworkHooks.openScreen(player, menu, dataWriter);
		}
	}

	public record WalletDataWriter(int walletIndex) implements Consumer<FriendlyByteBuf>
	{
		@Override
		public void accept(FriendlyByteBuf buffer) { buffer.writeInt(this.walletIndex); }
	}



}

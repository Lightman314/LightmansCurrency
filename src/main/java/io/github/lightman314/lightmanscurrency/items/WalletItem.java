package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.menus.WalletMenu;
import io.github.lightman314.lightmanscurrency.menus.providers.WalletMenuProvider;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.SPacketSyncWallet;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class WalletItem extends Item{
	
	private static final SoundEvent emptyOpenSound = new SoundEvent(new ResourceLocation("minecraft","item.armor.equip_leather"));
	private final ResourceLocation MODEL_TEXTURE;
	
	private final int level;
	private final int storageSize;
	
	public WalletItem(int level, int storageSize, String modelName, Properties properties)
	{
		super(properties);
		this.level = level;
		this.storageSize = storageSize;
		WalletMenu.updateMaxWalletSlots(this.storageSize);
		this.MODEL_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/entity/" + modelName + ".png");
	}
	
	/**
	 * Determines if the given ItemStack can be processed as a wallet.
	 * Returns true if the stack is empty, so you will need to check for that separately.
	 */
	public static boolean validWalletStack(ItemStack walletStack)
	{
		if(walletStack.isEmpty())
			return true;
		return isWallet(walletStack.getItem());
	}
	
	/**
	 * Determines if the given Item is a WalletItem
	 */
	public static boolean isWallet(Item item)
	{
		return item instanceof WalletItem;
	}
	
	/**
	 * Whether the WalletItem is capable of converting coins to coins of higher value.
	 */
	public static boolean CanConvert(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= Config.getConvertLevel() || wallet.level >= Config.getPickupLevel();
	}
	
	/**
	 * Whether the WalletItem is capable of automatically storing coins on pickup.
	 */
	public static boolean CanPickup(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= Config.getPickupLevel();
	}
	
	/**
	 * Whether the WalletItem is capable of interfacing with the players bank account.
	 */
	public static boolean HasBankAccess(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= Config.getBankLevel();
	}
	
	/**
	 * The number of inventory slots the WalletItem has.
	 */
	public static int InventorySize(WalletItem wallet)
	{
		if(wallet == null)
			return 0;
		return wallet.storageSize;
	}
	
	/**
	 * The number of inventory slots the Wallet Stack has.
	 * Returns 0 if the item is not a valid wallet.
	 */
	public static int InventorySize(ItemStack wallet)
	{
		if(wallet.getItem() instanceof WalletItem)
			return InventorySize((WalletItem)wallet.getItem());
		return 0;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn)
	{
		
		super.appendHoverText(stack,  level,  tooltip,  flagIn);
		
		if(CanPickup(this))
		{
			tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.wallet.pickup"));
		}
		if(CanConvert(this))
		{
			if(CanPickup(this))
			{
				Component onOffText = getAutoConvert(stack) ? new TranslatableComponent("tooltip.lightmanscurrency.wallet.autoConvert.on") : new TranslatableComponent("tooltip.lightmanscurrency.wallet.autoConvert.off");
				tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.wallet.autoConvert", onOffText));
			}
			else
			{
				tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.wallet.manualConvert"));
			}
		}
		
		CoinValue contents = new CoinValue(getWalletInventory(stack));
		if(contents.getRawValue() > 0)
			tooltip.add(new TranslatableComponent("tooltip.lightmanscurrency.wallet.storedmoney", "§2" + contents.getString() ));
		
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		
		//CurrencyMod.LOGGER.info("Wallet was used.");
		
		ItemStack wallet = player.getItemInHand(hand);
		
		if(!world.isClientSide)
		{
			//CurrencyMod.LOGGER.info("Opening Wallet UI?");
			
			//Determine which slot the wallet is in.
			int walletSlot = GetWalletSlot(player.getInventory(), wallet);
			
			//Open the UI
			if(walletSlot >= 0)
			{
				
				if(player.isCrouching())
				{
					AtomicBoolean equippedWallet = new AtomicBoolean(false);
					WalletCapability.getWalletHandler(player).ifPresent(walletHandler ->{
						if(walletHandler.getWallet().isEmpty())
						{
							walletHandler.setWallet(wallet);
							player.setItemInHand(hand, ItemStack.EMPTY);
							//Manually sync the equipped wallet so that the client container will initialize with the correct number of inventory slots
							LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketSyncWallet(player.getId(), walletHandler.getWallet()));
							walletHandler.clean();
							//Flag the interaction as a success so that the wallet menu will open with the wallet in the correct slot.
							equippedWallet.set(true);
						}
					});
					if(equippedWallet.get())
						walletSlot = -1;
				}
				NetworkHooks.openGui((ServerPlayer)player, new WalletMenuProvider(walletSlot), new DataWriter(walletSlot));
			}
				
			else
				LightmansCurrency.LogError("Could not find the wallet in the players inventory!");
			
		}
		else
		{
			player.level.playSound(player, player.blockPosition(), emptyOpenSound, SoundSource.PLAYERS, 0.75f, 1.25f + player.level.random.nextFloat() * 0.5f);
			if(!isEmpty(wallet))
				player.level.playSound(player, player.blockPosition(), CurrencySoundEvents.COINS_CLINKING, SoundSource.PLAYERS, 0.4f, 1f);
		}
		
		return InteractionResultHolder.success(wallet);
		
	}
	
	/**
	 * Whether the Wallet Stacks inventory contents are empty.
	 */
	public static boolean isEmpty(ItemStack wallet)
	{
		NonNullList<ItemStack> inventory = getWalletInventory(wallet);
		for(ItemStack stack : inventory)
		{
			if(!stack.isEmpty())
				return false;
		}
		return true;
	}
	
	private static int GetWalletSlot(Inventory inventory, ItemStack wallet)
	{
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			if(inventory.getItem(i) == wallet)
				return i;
		}
		return -1;
	}
	
	/**
	 * Places the given coin stack in the given Wallet Stack.
	 * @param wallet The wallet item stack in which to place the coin
	 * @param coins The coins to place in the wallet.
	 * @return The coins that were unable to fit in the wallet.
	 */
	public static ItemStack PickupCoin(ItemStack wallet, ItemStack coins)
	{
		
		ItemStack returnValue = coins.copy();
		
		NonNullList<ItemStack> inventory = getWalletInventory(wallet);
		for(int i = 0; i < inventory.size() && !returnValue.isEmpty(); i++)
		{
			ItemStack thisStack = inventory.get(i);
			if(thisStack.isEmpty())
			{
				inventory.set(i, returnValue.copy());
				returnValue = ItemStack.EMPTY;
			}
			else if(thisStack.getItem() == returnValue.getItem())
			{
				int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxStackSize() - thisStack.getCount());
				thisStack.setCount(thisStack.getCount() + amountToAdd);
				returnValue.setCount(returnValue.getCount() - amountToAdd);
			}
		}
		
		if(WalletItem.getAutoConvert(wallet))
			inventory = WalletItem.ConvertCoins(inventory);
		else
			inventory = MoneyUtil.SortCoins(inventory);
		
		putWalletInventory(wallet, inventory);
		
		//Return the coins that could not be picked up
		return returnValue;
		
	}
	
	private static NonNullList<ItemStack> ConvertCoins(NonNullList<ItemStack> inventory)
	{
		
		inventory = MoneyUtil.ConvertAllCoinsUp(inventory);
		
		return MoneyUtil.SortCoins(inventory);
		
	}
	
	/**
	 * Writes the given wallet inventory contents to the Wallet Stacks compound tag data.
	 */
	public static void putWalletInventory(ItemStack wallet, NonNullList<ItemStack> inventory)
	{
		if(!(wallet.getItem() instanceof WalletItem))
			return;
		
		CompoundTag compound = wallet.getOrCreateTag();
		ListTag invList = new ListTag();
		for(int i = 0; i < inventory.size(); i++)
		{
			ItemStack thisStack = inventory.get(i);
			if(!thisStack.isEmpty())
			{
				CompoundTag thisItemCompound = thisStack.save(new CompoundTag());
				thisItemCompound.putByte("Slot", (byte)i);
				invList.add(thisItemCompound);
			}
		}
		compound.put("Items", invList);
		//wallet.setTag(compound);
	}
	
	/**
	 * Reads & returns the wallets inventory contents from the ItemStack's compound tag data.
	 */
	public static NonNullList<ItemStack> getWalletInventory(ItemStack wallet)
	{
		
		CompoundTag compound = wallet.getOrCreateTag();
		 if(!(wallet.getItem() instanceof WalletItem))
			 return NonNullList.withSize(6, ItemStack.EMPTY);

		NonNullList<ItemStack> value = NonNullList.withSize(WalletItem.InventorySize((WalletItem)wallet.getItem()), ItemStack.EMPTY);
		if(!compound.contains("Items"))
			return value;
		
		ListTag invList = compound.getList("Items", Tag.TAG_COMPOUND);
		for(int i = 0; i < invList.size(); i++)
		{
			CompoundTag thisCompound = invList.getCompound(i);
			ItemStack thisStack = ItemStack.of(thisCompound);
			int j = (int)thisCompound.getByte("Slot") & 255;
			if(j >= 0 && j < value.size())
				value.set(j, thisStack);
		}
		
		return value;
		
	}
	
	/**
	 * Gets the auto-convert state of the given Wallet Stack.
	 * Returns false if the wallet is not capable of both converting & collecting coins.
	 */
	public static boolean getAutoConvert(ItemStack wallet)
	{
		if(!(wallet.getItem() instanceof WalletItem))
			return false;
		
		if(!WalletItem.CanConvert((WalletItem)wallet.getItem()) || !WalletItem.CanPickup((WalletItem)wallet.getItem()))
			return false;
		
		CompoundTag tag = wallet.getOrCreateTag();
		if(!tag.contains("AutoConvert"))
		{
			tag.putBoolean("AutoConvert", true);
			return true;
		}
		
		return tag.getBoolean("AutoConvert");
		
	}
	
	/**
	 * Toggles the auto-convert state of the given Wallet Stack.
	 * Does nothing if the wallet is not capable of both converting & collecting coins.
	 */
	public static void toggleAutoConvert(ItemStack wallet)
	{
		
		if(!(wallet.getItem() instanceof WalletItem))
			return;
		
		if(!WalletItem.CanConvert((WalletItem)wallet.getItem()))
			return;
		
		CompoundTag tag = wallet.getOrCreateTag();
		boolean oldValue = WalletItem.getAutoConvert(wallet);
		tag.putBoolean("AutoConvert", !oldValue);
		
	}
	
	/**
	 * Used to copy a wallets inventory contents to a newly crafted one. Also copies over any auto-conversion settings, custom names, and enchantments.
	 * @param walletIn The wallet inventory being copied.
	 * @param walletOut The wallet whose inventory will be filled
	 */
	public static void CopyWalletContents(ItemStack walletIn, ItemStack walletOut)
	{
		if(!(walletIn.getItem() instanceof WalletItem && walletIn.getItem() instanceof WalletItem))
		{
			LightmansCurrency.LogError("WalletItem.CopyWalletContents() -> One or both of the wallet stacks are not WalletItems.");
			return;
		}
		WalletItem walletItemIn = (WalletItem)walletIn.getItem();
		WalletItem walletItemOut = (WalletItem)walletOut.getItem();
		NonNullList<ItemStack> walletInventory1 = getWalletInventory(walletIn);
		NonNullList<ItemStack> walletInventory2 = getWalletInventory(walletOut);
		if(walletInventory1.size() > walletInventory2.size())
			LightmansCurrency.LogWarning("WalletItem.CopyWalletContents() -> walletIn has a larger inventory size than walletOut. This may result in a loss of wallet contents.");
		//Copy over the wallets contents
		for(int i = 0; i < walletInventory1.size() && i < walletInventory2.size(); i++)
		{
			walletInventory2.set(i, walletInventory1.get(i).copy());
		}
		//Write walletOut's nbt data
		putWalletInventory(walletOut, walletInventory2);
		//If both wallets can convert, confirm that the auto-convert setting matches
		if(CanConvert(walletItemIn) && CanConvert(walletItemOut) && CanPickup(walletItemIn) && CanPickup(walletItemOut))
		{
			if(getAutoConvert(walletIn) != getAutoConvert(walletOut))
			{
				toggleAutoConvert(walletOut);
			}
		}
		
		//Copy custom name
		if(walletIn.hasCustomHoverName())
			walletOut.setHoverName(walletIn.getHoverName());
		
		//Copy enchantments
		EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(walletIn), walletOut);
		
	}
	
	public static class DataWriter implements Consumer<FriendlyByteBuf>
	{

		private int slotIndex;
		
		public DataWriter(int slotIndex)
		{
			this.slotIndex = slotIndex;
		}
		
		@Override
		public void accept(FriendlyByteBuf buffer) {
			
			buffer.writeInt(this.slotIndex);
			
		}
		
	}
	
	/**
	 * The wallets texture. Used to render the wallet on the players hip when equipped.
	 */
	public ResourceLocation getModelTexture()
	{
		return this.MODEL_TEXTURE;
	}
	
}

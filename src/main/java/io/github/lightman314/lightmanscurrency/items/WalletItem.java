package io.github.lightman314.lightmanscurrency.items;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.providers.WalletContainerProvider;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

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
		return wallet.level >= Config.SERVER.walletConvertLevel.get() || wallet.level >= Config.SERVER.walletPickupLevel.get();
	}
	
	/**
	 * Whether the WalletItem is capable of automatically storing coins on pickup.
	 */
	public static boolean CanPickup(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= Config.SERVER.walletPickupLevel.get();
	}
	
	/**
	 * Whether the WalletItem is capable of interfacing with the players bank account.
	 */
	public static boolean HasBankAccess(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= Config.SERVER.walletBankLevel.get();
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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		
		super.addInformation(stack,  worldIn,  tooltip,  flagIn);
		
		if(CanPickup(this))
		{
			tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.wallet.pickup"));
		}
		if(CanConvert(this))
		{
			if(CanPickup(this))
			{
				ITextComponent onOffText = getAutoConvert(stack) ? new TranslationTextComponent("tooltip.lightmanscurrency.wallet.autoConvert.on") : new TranslationTextComponent("tooltip.lightmanscurrency.wallet.autoConvert.off");
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.wallet.autoConvert", onOffText));
			}
			else
			{
				tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.wallet.manualConvert"));
			}
		}
		if(HasBankAccess(this))
		{
			tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.wallet.bankaccount"));
		}
		
		CoinValue contents = new CoinValue(getWalletInventory(stack));
		if(contents.getRawValue() > 0)
			tooltip.add(new TranslationTextComponent("tooltip.lightmanscurrency.wallet.storedmoney", "§2" + contents.getString()));
		
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		
		//CurrencyMod.LOGGER.info("Wallet was used.");
		
		ItemStack wallet = player.getHeldItem(hand);
		
		if(!world.isRemote)
		{
			//CurrencyMod.LOGGER.info("Opening Wallet UI?");
			
			//Determine which slot the wallet is in.
			int walletSlot = GetWalletSlot(player.inventory, wallet);
			
			//Open the UI
			if(walletSlot >= 0)
			{
				if(player.isCrouching())
				{
					AtomicBoolean equippedWallet = new AtomicBoolean(false);
					WalletCapability.getWalletHandler(player).ifPresent(walletHandler ->{
						//Equip the wallet
						if(walletHandler.getWallet().isEmpty())
						{
							player.setHeldItem(hand, ItemStack.EMPTY);
							walletHandler.setWallet(wallet);
							equippedWallet.set(true);
						}
					});
					if(equippedWallet.get())
						walletSlot = -1;
				}
				NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider) new WalletContainerProvider(walletSlot), new DataWriter(walletSlot));
			}
			else
				LightmansCurrency.LogError("Could not find the wallet in the players inventory!");
			
		}
		else
		{
			player.world.playSound(player, player.getPosition(), emptyOpenSound, SoundCategory.PLAYERS, 0.75f, 1.25f + player.world.rand.nextFloat() * 0.5f);
			if(!isEmpty(wallet))
				player.world.playSound(player, player.getPosition(), CurrencySoundEvents.COINS_CLINKING, SoundCategory.PLAYERS, 0.4f, 1f);
		}
		
		return ActionResult.resultSuccess(wallet);
		
	}
	
	/**
	 * Whether the Wallet's Inventory is empty.
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
	
	private static int GetWalletSlot(PlayerInventory inventory, ItemStack wallet)
	{
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			if(inventory.getStackInSlot(i) == wallet)
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
	 * Writes the given wallet inventory contents to the wallets compound tag data.
	 */
	public static void putWalletInventory(ItemStack wallet, NonNullList<ItemStack> inventory)
	{
		if(!(wallet.getItem() instanceof WalletItem))
			return;
		
		CompoundNBT compound = wallet.getOrCreateTag();
		ListNBT invList = new ListNBT();
		for(int i = 0; i < inventory.size(); i++)
		{
			ItemStack thisStack = inventory.get(i);
			if(!thisStack.isEmpty())
			{
				CompoundNBT thisItemCompound = thisStack.write(new CompoundNBT());
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
		
		CompoundNBT compound = wallet.getOrCreateTag();
		 if(!(wallet.getItem() instanceof WalletItem))
			 return NonNullList.withSize(6, ItemStack.EMPTY);

		NonNullList<ItemStack> value = NonNullList.withSize(WalletItem.InventorySize((WalletItem)wallet.getItem()), ItemStack.EMPTY);
		if(!compound.contains("Items"))
			return value;
		
		ListNBT invList = compound.getList("Items", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < invList.size(); i++)
		{
			CompoundNBT thisCompound = invList.getCompound(i);
			ItemStack thisStack = ItemStack.read(thisCompound);
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
		
		CompoundNBT tag = wallet.getOrCreateTag();
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
		
		CompoundNBT tag = wallet.getOrCreateTag();
		boolean oldValue = WalletItem.getAutoConvert(wallet);
		tag.putBoolean("AutoConvert", !oldValue);
		
	}
	
	/**
	 * Used to copy a wallets inventory contents to a newly crafted one. Also copies over any auto-conversion settings.
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
		if(walletIn.hasDisplayName())
			walletOut.setDisplayName(walletIn.getDisplayName());
		
		//Copy enchantments
		EnchantmentHelper.getEnchantments(walletIn).forEach((enchantment,level) ->{
			walletOut.addEnchantment(enchantment, level);
		});
		
	}
	
	public static class DataWriter implements Consumer<PacketBuffer>
	{

		private int slotIndex;
		
		public DataWriter(int slotIndex)
		{
			this.slotIndex = slotIndex;
		}
		
		@Override
		public void accept(PacketBuffer buffer) {
			
			buffer.writeInt(this.slotIndex);
			
		}
		
	}
	
	public ResourceLocation getModelTexture()
	{
		return this.MODEL_TEXTURE;
	}
	
}

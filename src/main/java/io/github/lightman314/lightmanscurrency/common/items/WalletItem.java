package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosInternal;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.enchantments.WalletEnchantment;
import io.github.lightman314.lightmanscurrency.LCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class WalletItem extends Item{
	
	private static final SoundEvent emptyOpenSound = SoundEvents.ARMOR_EQUIP_LEATHER.value();
	private final ResourceLocation MODEL_TEXTURE;
	
	private final int level;
	private final int storageSize;
	
	public WalletItem(int level, int storageSize, String modelName, Properties properties)
	{
		super(properties.stacksTo(1));
		this.level = level;
		this.storageSize = storageSize;
		WalletMenuBase.updateMaxWalletSlots(this.storageSize);
		this.MODEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/entity/" + modelName + ".png");
	}
	
	@Override
	public int getEnchantmentValue(@Nonnull ItemStack stack) { return 10; }
	
	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) { return true; }
	
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
	 * Determines if the given ItemStack is a WalletItem
	 */
	public static boolean isWallet(ItemStack item) { return !item.isEmpty() && isWallet(item.getItem()); }
	
	/**
	 * Determines if the given Item is a WalletItem
	 */
	public static boolean isWallet(Item item) { return item instanceof WalletItem; }
	
	/**
	 * Whether the WalletItem is capable of converting coins to coins of higher value.
	 */
	public static boolean CanExchange(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= LCConfig.SERVER.walletExchangeLevel.get();
	}
	
	/**
	 * Whether the WalletItem is capable of automatically storing coins on pickup.
	 */
	public static boolean CanPickup(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= LCConfig.SERVER.walletPickupLevel.get();
	}
	
	/**
	 * Whether the WalletItem is capable of interfacing with the players bank account.
	 */
	public static boolean HasBankAccess(WalletItem wallet)
	{
		if(wallet == null)
			return false;
		return wallet.level >= LCConfig.SERVER.walletBankLevel.get();
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
	 * The number of inventory slots the Wallet Stack has.<br>
	 * Returns 0 if the item is not a valid wallet.
	 */
	public static int InventorySize(ItemStack wallet)
	{
		if(wallet.getItem() instanceof WalletItem)
			return InventorySize((WalletItem)wallet.getItem());
		return 0;
	}

	@Nonnull
	public static WalletDataWrapper getDataWrapper(@Nonnull ItemStack stack)
	{
		if(!isWallet(stack))
			return WalletDataWrapper.EMPTY;
		return new WalletDataWrapper(stack);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		
		super.appendHoverText(stack,context,tooltip,flagIn);

		WalletDataWrapper data = getDataWrapper(stack);

		if(CanPickup(this))
		{
			tooltip.add(LCText.TOOLTIP_WALLET_PICKUP.get().withStyle(ChatFormatting.YELLOW));
		}
		if(CanExchange(this))
		{
			if(CanPickup(this))
			{
				Component onOffText = data.getAutoExchange() ? LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_ON.get().withStyle(ChatFormatting.GREEN) : LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_OFF.get().withStyle(ChatFormatting.RED);
				tooltip.add(LCText.TOOLTIP_WALLET_EXCHANGE_AUTO.get(onOffText).withStyle(ChatFormatting.YELLOW));
			}
			else
			{
				tooltip.add(LCText.TOOLTIP_WALLET_EXCHANGE_MANUAL.get().withStyle(ChatFormatting.YELLOW));
			}
		}
		if(HasBankAccess(this))
		{
			tooltip.add(LCText.TOOLTIP_WALLET_BANK_ACCOUNT.get().withStyle(ChatFormatting.YELLOW));
		}
		
		WalletEnchantment.addWalletEnchantmentTooltips(tooltip, stack, context);

		if(CoinAPI.API.NoDataAvailable())
			return;

		MoneyView contents = data.getStoredMoney();
		tooltip.add(LCText.TOOLTIP_WALLET_STORED_MONEY.get());
		for(MoneyValue val : contents.allValues())
			tooltip.add(val.getText().withStyle(ChatFormatting.DARK_GREEN));
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
	{
		
		//CurrencyMod.LOGGER.info("Wallet was used.");
		
		ItemStack wallet = player.getItemInHand(hand);
		
		if(!level.isClientSide)
		{
			//CurrencyMod.LOGGER.info("Opening Wallet UI?");
			
			//Determine which slot the wallet is in.
			int walletSlot = GetWalletSlot(player.getInventory(), wallet);
			
			//Open the UI
			if(walletSlot >= 0)
			{
				
				if(player.isCrouching() && (!LCCuriosInternal.hasCuriosWalletSlot(player)))
				{
					boolean equippedWallet = false;
					WalletHandler walletHandler = WalletHandler.get(player);
					if(walletHandler != null)
					{
						if(walletHandler.getWallet().isEmpty())
						{
							walletHandler.setWallet(wallet);
							player.setItemInHand(hand, ItemStack.EMPTY);
							//Manually sync the equipped wallet so that the client container will initialize with the correct number of inventory slots
							//This is now done automatically by the wallet handler
							//Flag the interaction as a success so that the wallet menu will open with the wallet in the correct slot.
							equippedWallet = true;
						}
					}
					if(equippedWallet)
						walletSlot = -1;
				}
				WalletMenuBase.SafeOpenWalletMenu(player, walletSlot);
			}
				
			else
				LightmansCurrency.LogError("Could not find the wallet in the players inventory!");
			
		}
		else
		{
			player.level().playSound(player, player.blockPosition(), emptyOpenSound, SoundSource.PLAYERS, 0.75f, 1.25f + player.level().random.nextFloat() * 0.5f);
			if(!isEmpty(wallet))
				player.level().playSound(player, player.blockPosition(), ModSounds.COINS_CLINKING.get(), SoundSource.PLAYERS, 0.4f, 1f);
		}
		
		return InteractionResultHolder.success(wallet);
		
	}
	
	/**
	 * Whether the Wallet Stacks inventory contents are empty.
	 */
	public static boolean isEmpty(@Nonnull ItemStack wallet)
	{
		if(!isWallet(wallet))
			return true;
		return getDataWrapper(wallet).getContents().isEmpty();
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

		if(!isWallet(wallet))
			return coins;

		WalletDataWrapper data = getDataWrapper(wallet);
		Container contents = data.getContents();
		ItemStack returnValue = InventoryUtil.TryPutItemStack(contents, coins);

		if(WalletItem.getAutoExchange(wallet))
			CoinAPI.API.CoinExchangeAllUp(contents);
		CoinAPI.API.SortCoinsByValue(contents);

		data.setContents(contents, null);
		
		//Return the coins that could not be picked up
		return returnValue;
	}
	
	/**
	 * Gets the auto-convert state of the given Wallet Stack.
	 * Returns false if the wallet is not capable of both exchanging & collecting coins.
	 */
	public static boolean getAutoExchange(ItemStack wallet)
	{
		if(!(wallet.getItem() instanceof WalletItem))
			return false;
		
		if(!WalletItem.CanExchange((WalletItem)wallet.getItem()) || !WalletItem.CanPickup((WalletItem)wallet.getItem()))
			return false;

		WalletDataWrapper data = getDataWrapper(wallet);
		return data.getAutoExchange();
		
	}
	
	/**
	 * Toggles the auto-convert state of the given Wallet Stack.
	 * Does nothing if the wallet is not capable of both converting & collecting coins.
	 */
	public static void toggleAutoExchange(ItemStack wallet)
	{
		
		if(!isWallet(wallet))
			return;
		
		if(!WalletItem.CanExchange((WalletItem)wallet.getItem()))
			return;

		WalletDataWrapper data = getDataWrapper(wallet);
		data.setAutoExchange(!data.getAutoExchange());
		
	}


	/**
	 * Automatically collects all coins from the given container into the players equipped wallet.
	 */
	public static void QuickCollect(Player player, Container container, boolean allowSideChain)
	{
		ItemStack wallet = CoinAPI.API.getEquippedWallet(player);
		if(isWallet(wallet))
		{
			for(int i = 0; i < container.getContainerSize(); ++i)
			{
				ItemStack stack = container.getItem(i);
				if(CoinAPI.API.IsCoin(stack, allowSideChain))
				{
					stack = PickupCoin(wallet, stack);
					container.setItem(i, stack);
				}
			}
		}
	}

	/**
	 * The wallets texture. Used to renderBG the wallet on the players hip when equipped.
	 */
	public ResourceLocation getModelTexture() { return this.MODEL_TEXTURE; }
	
}

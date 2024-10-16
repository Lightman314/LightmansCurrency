package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.MixedCapabilityProvider;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.SPacketSyncWallet;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.enchantments.WalletEnchantment;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class WalletItem extends Item{
	
	private static final SoundEvent emptyOpenSound = SoundEvents.ARMOR_EQUIP_LEATHER;

	/**
	 * A constant denoting the highest expected level of a wallet item<br>
	 * Used to limit the input of wallet feature config options
	 */
	public static final int LARGEST_LEVEL = 6;
	public static final int CONFIG_LIMIT = LARGEST_LEVEL + 1;

	/**
	 * The number of slots added by each upgrade material
	 */
	public static final int SLOTS_PER_UPGRADE = 6;
	/**
	 * Maximum slots that can be added by an upgrade
	 */
	public static final int SLOT_UPGRADE_LIMIT = 24;
	
	private final int level;
	private final int storageSize;

	private final int bonusMagnet;
	public final boolean indestructible;

	public final ResourceLocation model;


	@Nonnull
	public static ResourceLocation lazyModel(@Nonnull String itemID) { return lazyModel(VersionUtil.lcResource(itemID)); }
	@Nonnull
	public static ResourceLocation lazyModel(@Nonnull ResourceLocation itemID) { return itemID.withPrefix("item/wallet_hip/"); }

	/**
	 * Simplified constructor for wallets using the <code>lightmanscurrency</code> namespace for their model
	 */
	public WalletItem(int level, int storageSize, @Nonnull ResourceLocation model, @Nonnull Properties properties) { this(level,storageSize,model,false,0,properties); }
	/**
	 * Default constructor
	 * @param level The wallets numerical level. Used to allow abilities to be gained at a specific level<br>
	 *              Should not exceed {@link #LARGEST_LEVEL}
	 * @param storageSize The number of coin slots included in this wallets inventory
	 * @param model The wallets model location<br>
	 *              See {@link #lazyModel(String) or {@link #lazyModel(ResourceLocation)} for easy constructors to properly locate a model in the <code>item/wallet_hip/</code> path
	 * @param indestructible Whether the wallet is immune to all forms of damage while in item form
	 * @param bonusMagnet The level bonus given to the {@link io.github.lightman314.lightmanscurrency.common.enchantments.CoinMagnetEnchantment CoinMagnet} enchantment
	 * @param properties The items properties. Will be automatically limited to a stack size of 1.
	 */
	public WalletItem(int level, int storageSize, @Nonnull ResourceLocation model, boolean indestructible, int bonusMagnet, @Nonnull Properties properties)
	{
		super(properties.stacksTo(1));
		this.level = level;
		this.storageSize = storageSize;
		this.indestructible = indestructible;
		this.bonusMagnet = bonusMagnet;
		this.model = model;
	}
	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
	{
		List<ICapabilityProvider> providers = new ArrayList<>();
		providers.add(CapabilityMoneyViewer.createProvider(new WalletMoneyViewer(stack)));
		if(LCCurios.isLoaded())
		{
			ICapabilityProvider temp = LCCurios.createWalletProvider(stack);
			if(temp != null)
				providers.add(temp);
		}
		return new MixedCapabilityProvider(providers);
	}
	
	@Override
	public int getEnchantmentValue(ItemStack stack) { return 10; }
	
	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) { return true; }

	@Override
	public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		if(this.bonusMagnet > 0 && enchantment == ModEnchantments.COIN_MAGNET.get())
			return super.getEnchantmentLevel(stack,enchantment) + this.bonusMagnet;
		return super.getEnchantmentLevel(stack,enchantment);
	}

	@Override
	public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
		Map<Enchantment,Integer> map = super.getAllEnchantments(stack);
		if(this.bonusMagnet > 0)
			map.put(ModEnchantments.COIN_MAGNET.get(),map.getOrDefault(ModEnchantments.COIN_MAGNET.get(),0) + this.bonusMagnet);
		return map;
	}

	@Override
	public boolean canBeHurtBy(@Nonnull DamageSource source) {
		if(this.indestructible)
			return false;
		return super.canBeHurtBy(source);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		//Do nothing on the client
		if(entity.level().isClientSide)
			return false;
		//Make item not despawn if indestructable
		if(this.indestructible && entity.getAge() >= 0)
			entity.setUnlimitedLifetime();
		return false;
	}

	@Override
	public boolean overrideOtherStackedOnMe(@Nonnull ItemStack wallet, @Nonnull ItemStack item, @Nonnull Slot slot, @Nonnull ClickAction action, @Nonnull Player player, @Nonnull SlotAccess slotAccess) {
		if(action == ClickAction.SECONDARY && LCConfig.SERVER.walletCapacityUpgradeable.get() && InventoryUtil.ItemHasTag(item, LCTags.Items.WALLET_UPGRADE_MATERIAL))
		{
			CompoundTag tag = wallet.getOrCreateTag();
			int bonusSlots = tag.getInt("BonusSlots");
			//Still consume the interaction if the item was in fact an upgrade item
			if(bonusSlots >= SLOT_UPGRADE_LIMIT)
				return true;
			//Don't allow when in the wallet menu, just to be on the safe side
			if(player.containerMenu instanceof WalletMenuBase)
				return true;
			item.shrink(1);
			tag.putInt("BonusSlots", Math.min(SLOT_UPGRADE_LIMIT,bonusSlots + SLOTS_PER_UPGRADE));
			return true;
		}
		return false;
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

	public static int BonusSlots(@Nonnull ItemStack walletStack)
	{
		if(walletStack.getItem() instanceof WalletItem)
		{
			CompoundTag tag = walletStack.getOrCreateTag();
			return Math.min(SLOT_UPGRADE_LIMIT,tag.getInt("BonusSlots"));
		}
		return 0;
	}

	/**
	 * The number of inventory slots the Wallet Stack has.
	 * Returns 0 if the item is not a valid wallet.<br>
	 * Factors in the added bonus slots present {@link #BonusSlots(ItemStack)} value
	 */
	public static int InventorySize(@Nonnull ItemStack walletStack)
	{
		if(walletStack.getItem() instanceof WalletItem wallet)
			return wallet.storageSize + BonusSlots(walletStack);
		return 0;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		
		super.appendHoverText(stack,  level,  tooltip,  flagIn);

		tooltip.add(LCText.TOOLTIP_WALLET_CAPACITY.get(InventorySize(stack)).withStyle(ChatFormatting.YELLOW));

		int bonusSlots = BonusSlots(stack);
		if(bonusSlots < SLOT_UPGRADE_LIMIT)
		{
			ItemStack exampleItem = ListUtil.randomItemFromList(InventoryUtil.GetItemStacksWithTag(LCTags.Items.WALLET_UPGRADE_MATERIAL),ItemStack.EMPTY);
			if(!exampleItem.isEmpty())
			{
				tooltip.addAll(TooltipHelper.splitTooltips(LCText.TOOLTIP_WALLET_UPGRADEABLE_CAPACITY.get(
						TooltipHelper.lazyFormat(exampleItem.getHoverName(),ChatFormatting.AQUA),
						TooltipHelper.lazyFormat(String.valueOf(SLOTS_PER_UPGRADE),ChatFormatting.GOLD)
				),ChatFormatting.YELLOW));
			}
		}

		if(CanPickup(this))
		{
			tooltip.add(LCText.TOOLTIP_WALLET_PICKUP.get().withStyle(ChatFormatting.YELLOW));
		}
		if(CanExchange(this))
		{
			if(CanPickup(this))
			{
				Component onOffText = getAutoExchange(stack) ? LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_ON.get().withStyle(ChatFormatting.GREEN) : LCText.TOOLTIP_WALLET_EXCHANGE_AUTO_OFF.get().withStyle(ChatFormatting.RED);
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
		
		WalletEnchantment.addWalletEnchantmentTooltips(tooltip, stack);

		if(CoinAPI.API.NoDataAvailable())
			return;

		IMoneyViewer handler = CapabilityMoneyViewer.getCapability(stack);
		if(handler != null)
		{
			MoneyView contents = handler.getStoredMoney();
			if(!contents.isEmpty())
			{
				tooltip.add(LCText.TOOLTIP_WALLET_STORED_MONEY.get());
				for(MoneyValue val : contents.allValues())
					tooltip.add(val.getText().withStyle(ChatFormatting.DARK_GREEN));
			}
		}
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
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
				
				if(player.isCrouching() && !LCCurios.isLoaded())
				{
					IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
					if(walletHandler != null)
					{
						if(walletHandler.getWallet().isEmpty())
						{
							walletHandler.setWallet(wallet);
							player.setItemInHand(hand, ItemStack.EMPTY);
							//Manually sync the equipped wallet so that the client container will initialize with the correct number of inventory slots
							new SPacketSyncWallet(player.getId(), walletHandler.getWallet(), walletHandler.visible()).sendTo(player);
							walletHandler.clean();
							//Flag the interaction as a success so that the wallet menu will open with the wallet in the correct slot.
							walletSlot = -1;
						}
					}
				}
				WalletMenuBase.SafeOpenWalletMenu((ServerPlayer)player, walletSlot);
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
		return getWalletInventory(wallet).isEmpty();
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
		Container inventory = getWalletInventory(wallet);
		ItemStack returnValue = InventoryUtil.TryPutItemStack(inventory, coins);

		if(WalletItem.getAutoExchange(wallet))
			CoinAPI.API.CoinExchangeAllUp(inventory);
		CoinAPI.API.SortCoinsByValue(inventory);
		
		putWalletInventory(wallet, inventory);
		
		//Return the coins that could not be picked up
		return returnValue;
	}
	
	/**
	 * Writes the given wallet inventory contents to the Wallet Stacks compound tag data.
	 */
	public static void putWalletInventory(@Nonnull ItemStack wallet, @Nonnull Container inventory)
	{
		if(!(wallet.getItem() instanceof WalletItem))
			return;

		InventoryUtil.saveAllItems("Items", wallet.getOrCreateTag(), inventory);
	}
	
	/**
	 * Reads & returns the wallets inventory contents from the ItemStack's compound tag data.
	 */
	@Nonnull
	public static SimpleContainer getWalletInventory(@Nonnull ItemStack wallet)
	{

		 if(!(wallet.getItem() instanceof WalletItem))
			 return new SimpleContainer();

		CompoundTag compound = wallet.getOrCreateTag();

		int inventorySize =  WalletItem.InventorySize(wallet);
		if(!compound.contains("Items"))
			return new SimpleContainer(inventorySize);

		return InventoryUtil.loadAllItems("Items", compound, inventorySize);
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
	public static void toggleAutoExchange(ItemStack wallet)
	{
		
		if(!(wallet.getItem() instanceof WalletItem))
			return;
		
		if(!WalletItem.CanExchange((WalletItem)wallet.getItem()))
			return;
		
		CompoundTag tag = wallet.getOrCreateTag();
		boolean oldValue = WalletItem.getAutoExchange(wallet);
		tag.putBoolean("AutoConvert", !oldValue);
		
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
	
}

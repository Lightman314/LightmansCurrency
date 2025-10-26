package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.data.SoundEntry;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletData;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.enchantments.WalletEnchantment;
import io.github.lightman314.lightmanscurrency.LCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WalletItem extends Item{
	
	private static final SoundEvent emptyOpenSound = SoundEvents.ARMOR_EQUIP_LEATHER.value();

	public static final int MAX_WALLET_SLOTS = 78;

	/**
	 * The number of slots added by each upgrade material
	 */
	public static final int SLOTS_PER_UPGRADE = 6;
	public static final int DEFAULT_UPGRADE_LIMIT = 24;

	public static final ResourceLocation DEFAULT_COIN_COLLECT_SOUND = VersionUtil.lcResource("coins_clinking");

	public static ResourceLocation lazyModel(String itemID) { return lazyModel(VersionUtil.lcResource(itemID)); }
	public static ResourceLocation lazyModel(ResourceLocation itemID) { return itemID.withPrefix("item/wallet_hip/"); }

	/**
	 * Simplified constructor for wallets using the <code>lightmanscurrency</code> namespace for their model
	 */
	public WalletItem(int storageSize, ResourceLocation model, Properties properties) { this(storageSize, model, false, 0, DEFAULT_UPGRADE_LIMIT, SoundEntry.WALLET_DEFAULT, properties); }
	/**
	 * Default constructor
	 * @param storageSize The number of coin slots included in this wallets inventory
	 * @param model The wallets model location<br>
	 *              See {@link #lazyModel(ResourceLocation)} or {@link #lazyModel(String)} for easy constructors to properly locate a model in the <code>item/wallet_hip/</code> path
	 * @param properties The items properties. Will be automatically limited to a stack size of 1.
	 */
	public WalletItem(int storageSize, ResourceLocation model, boolean indestructible, int bonusMagnet, int upgradeLimit, Properties properties) { this(storageSize,model,indestructible,bonusMagnet,upgradeLimit,SoundEntry.WALLET_DEFAULT,properties); }
	/**
	 * Default constructor
	 * @param storageSize The number of coin slots included in this wallets inventory
	 * @param model The wallets model location<br>
	 *              See {@link #lazyModel(ResourceLocation)} or {@link #lazyModel(String)} for easy constructors to properly locate a model in the <code>item/wallet_hip/</code> path
	 * @param properties The items properties. Will be automatically limited to a stack size of 1.
	 */
	public WalletItem(int storageSize, ResourceLocation model, boolean indestructible, int bonusMagnet, int upgradeLimit, List<SoundEntry> coinCollectSound, Properties properties)
	{
		super(properties.stacksTo(1)
				.component(ModDataComponents.WALLET_CAPACITY,storageSize)
				.component(ModDataComponents.WALLET_INVULNERABLE,indestructible)
				.component(ModDataComponents.WALLET_BONUS_MAGNET,bonusMagnet)
				.component(ModDataComponents.WALLET_UPGRADE_LIMIT,upgradeLimit)
				.component(ModDataComponents.WALLET_MODEL,model)
				.component(ModDataComponents.WALLET_COIN_SOUND,coinCollectSound));
	}
	
	@Override
	public int getEnchantmentValue(ItemStack stack) { return 10; }
	
	@Override
	public boolean isEnchantable(ItemStack stack) { return true; }

	@Override
	public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
		int bonusMagnet = stack.getOrDefault(ModDataComponents.WALLET_BONUS_MAGNET,0);
		if(bonusMagnet > 0 && enchantment.is(ModEnchantments.COIN_MAGNET))
			return super.getEnchantmentLevel(stack,enchantment) + bonusMagnet;
		return super.getEnchantmentLevel(stack, enchantment);
	}

	
	@Override
	public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
		ItemEnchantments enchantments = super.getAllEnchantments(stack,lookup);
		int bonusMagnet = stack.getOrDefault(ModDataComponents.WALLET_BONUS_MAGNET,0);
		if(bonusMagnet > 0)
		{
			ItemEnchantments.Mutable e = new ItemEnchantments.Mutable(enchantments);
			lookup.get(ModEnchantments.COIN_MAGNET).ifPresent(cm ->
				e.set(cm,e.getLevel(cm) + bonusMagnet)
			);
			enchantments = e.toImmutable();
		}
		return enchantments;
	}

	@Override
	public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
		boolean indestructible = stack.getOrDefault(ModDataComponents.WALLET_INVULNERABLE,false);
		if(indestructible)
			return false;
		return super.canBeHurtBy(stack, source);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		//Make item not despawn if indestructable
		boolean indestructible = stack.getOrDefault(ModDataComponents.WALLET_INVULNERABLE,false);
		if(indestructible)
		{
			if(entity.getAge() >= 0 && !entity.level().isClientSide)
				entity.setUnlimitedLifetime();
			//If indestructible and y < minimum build height, set y velocity to a positive number to force it to bounce in the void
			if(entity.position().y <= entity.level().getMinBuildHeight())
			{
				Vec3 velocity = entity.getDeltaMovement();
				entity.setDeltaMovement(velocity.x,0.5d,velocity.z);
			}
		}
		return false;
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack wallet, ItemStack item, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
		if(action == ClickAction.SECONDARY && LCConfig.SERVER.walletCapacityUpgradeable.get() && InventoryUtil.ItemHasTag(item, LCTags.Items.WALLET_UPGRADE_MATERIAL))
		{
			WalletData data = wallet.getOrDefault(ModDataComponents.WALLET_DATA,WalletData.createFor(wallet));
			int upgradeLimit = wallet.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0);
			int bonusSlots = data.getBonusSlots(wallet.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0));
			//Still consume the interaction if the item was in fact an upgrade item
			if(bonusSlots >= wallet.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0))
				return true;
			//Don't allow when in the wallet menu, just to be on the safe side
			if(player.containerMenu instanceof WalletMenuBase walletMenu)
				return true;
			item.shrink(1);
			wallet.set(ModDataComponents.WALLET_DATA,data.withAddedBonusSlots(SLOTS_PER_UPGRADE));
			//Trigger set item code
			slot.set(wallet);
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
        return LCConfig.SERVER.walletCanExchange.get().contains(wallet);
	}
	
	/**
	 * Whether the WalletItem is capable of automatically storing coins on pickup.
	 */
	public static boolean CanPickup(WalletItem wallet)
	{
		if(wallet == null)
			return false;
        return LCConfig.SERVER.walletCanPickup.get().contains(wallet);
	}
	
	/**
	 * Whether the WalletItem is capable of interfacing with the players bank account.
	 */
	public static boolean HasBankAccess(WalletItem wallet)
	{
		if(wallet == null)
			return false;
        return LCConfig.SERVER.walletCanBank.get().contains(wallet);
	}

	public static int BonusSlots(ItemStack walletStack)
	{
		if(walletStack.getItem() instanceof WalletItem wallet)
			return walletStack.getOrDefault(ModDataComponents.WALLET_DATA,WalletData.EMPTY).getBonusSlots(walletStack.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0));
		return 0;
	}

	/**
	 * The number of inventory slots the Wallet Stack has.<br>
	 * Returns 0 if the item is not a valid wallet.<br>
	 * Factors in added bonus slots in the stacks present {@link io.github.lightman314.lightmanscurrency.common.items.data.WalletData#bonusSlots() WalletData#bonusSlots()} value
	 */
	public static int InventorySize(ItemStack walletStack)
	{
		if(walletStack.getItem() instanceof WalletItem wallet)
		{
			WalletData data = walletStack.getOrDefault(ModDataComponents.WALLET_DATA,WalletData.EMPTY);
			int storageSize = walletStack.getOrDefault(ModDataComponents.WALLET_CAPACITY,6);
			return MathUtil.clamp(storageSize + data.bonusSlots(),1,MAX_WALLET_SLOTS);
		}
		return 0;
	}

	
	public static WalletDataWrapper getDataWrapper(ItemStack stack)
	{
		if(!isWallet(stack))
			return WalletDataWrapper.EMPTY;
		return new WalletDataWrapper(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
	{
		
		super.appendHoverText(stack,context,tooltip,flagIn);

		WalletDataWrapper data = getDataWrapper(stack);

		tooltip.add(LCText.TOOLTIP_WALLET_CAPACITY.get(data.getContainerSize()).withStyle(ChatFormatting.YELLOW));

		if(data.getBonusSlots() < stack.getOrDefault(ModDataComponents.WALLET_UPGRADE_LIMIT,0))
		{
			ItemStack exampleItem = ListUtil.randomItemFromList(InventoryUtil.GetItemStacksWithTag(LCTags.Items.WALLET_UPGRADE_MATERIAL),ItemStack.EMPTY);
			if(!exampleItem.isEmpty())
			{
				tooltip.addAll(TooltipHelper.splitTooltips(LCText.TOOLTIP_WALLET_UPGRADEABLE_CAPACITY.get(
								TooltipHelper.lazyFormat(exampleItem.getHoverName(),ChatFormatting.AQUA),
								TooltipHelper.lazyFormat(String.valueOf(SLOTS_PER_UPGRADE),ChatFormatting.GOLD))
						,ChatFormatting.YELLOW));
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

		if(CoinAPI.getApi().NoDataAvailable())
			return;

		IMoneyHandler contentHandler = MoneyAPI.getApi().GetContainersMoneyHandler(data.getContents(),s -> {}, IClientTracker.forClient());
		MoneyView contents = contentHandler.getStoredMoney();
		if(!contents.isEmpty())
		{
			tooltip.add(LCText.TOOLTIP_WALLET_STORED_MONEY.get());
			tooltip.addAll(contents.getAllText(ChatFormatting.DARK_GREEN));
		}
	}
	
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
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
				if(player.isCrouching() && !LCCurios.isLoaded())
				{
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
							walletSlot = -1;
						}
					}
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
	public static boolean isEmpty(ItemStack wallet)
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
			CoinAPI.getApi().CoinExchangeAllUp(contents);
		CoinAPI.getApi().SortCoinsByValue(contents);

		data.setContents(contents, null);
		
		//Return the coins that could not be picked up
		return returnValue;
	}
	
	/**
	 * Gets the auto-convert state of the given Wallet Stack.
	 * Returns false if the wallet is not capable of both exchanging &amp; collecting coins.
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
	 * Does nothing if the wallet is not capable of both exchanging &amp; collecting coins.
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
		ItemStack wallet = CoinAPI.getApi().getEquippedWallet(player);
		if(isWallet(wallet))
		{
			for(int i = 0; i < container.getContainerSize(); ++i)
			{
				ItemStack stack = container.getItem(i);
				if(CoinAPI.getApi().IsAllowedInCoinContainer(stack, allowSideChain))
				{
					stack = PickupCoin(wallet, stack);
					container.setItem(i, stack);
				}
			}
		}
	}

	public static void playCollectSound(LivingEntity entity, ItemStack wallet)
	{
		Level level = entity.level();
		BuiltInRegistries.SOUND_EVENT.getOptional(getCoinCollectSound(level,wallet))
				.ifPresent(sound -> level.playSound(null, entity, sound, SoundSource.PLAYERS, 0.4f, 1f));
	}

	
	public static ResourceLocation getCoinCollectSound(Level level, ItemStack wallet)
	{
		if(!isWallet(wallet))
			return DEFAULT_COIN_COLLECT_SOUND;
		List<SoundEntry> soundEntries = wallet.getOrDefault(ModDataComponents.WALLET_COIN_SOUND,SoundEntry.WALLET_DEFAULT);
		return SoundEntry.getRandomEntry(level.getRandom(),soundEntries,DEFAULT_COIN_COLLECT_SOUND);
	}
	
}

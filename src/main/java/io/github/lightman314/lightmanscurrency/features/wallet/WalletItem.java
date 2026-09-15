package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.ItemContents;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.LCSounds;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.enchantments.CoinMagnetEnchantmentHelper;
import io.github.lightman314.lightmanscurrency.features.wallet.enchantments.WalletEnchantments;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.AbstractWalletMenu;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.WalletMenu;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import java.util.List;
import java.util.function.Consumer;

public class WalletItem extends Item {

    public static final int MAX_WALLET_SLOTS = 78;

    public static final TextEntry MESSAGE_WALLET_NONE_EQUIPPED = TextEntry.message(LCApi.MODID,"wallet.none_equipped");

    public static final TextEntry TOOLTIP_WALLET_KEY_BIND = TextEntry.tooltip(LCApi.MODID,"wallet.key_bind");
    public static final TextEntry TOOLTIP_WALLET_CAPACITY = TextEntry.tooltip(LCApi.MODID,"wallet.capacity");
    public static final MultiLineTextEntry TOOLTIP_WALLET_UPGRADEABLE = MultiLineTextEntry.tooltip(LCApi.MODID,"wallet.upgradeable");
    public static final TextEntry TOOLTIP_WALLET_FULLY_UPGRADED = TextEntry.tooltip(LCApi.MODID,"wallet.fully_upgraded");
    public static final TextEntry TOOLTIP_WALLET_STORED_MONEY = TextEntry.tooltip(LCApi.MODID,"wallet.storedmoney");

    public static final TextEntry TOOLTIP_WALLET_PICKUP = TextEntry.tooltip(LCApi.MODID,"wallet.pickup");
    public static final TextEntry TOOLTIP_WALLET_PICKUP_MAGNET = TextEntry.tooltip(LCApi.MODID,"wallet.pickup.magnet");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_MANUAL = TextEntry.tooltip(LCApi.MODID,"wallet.exchange.manual");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO = TextEntry.tooltip(LCApi.MODID,"wallet.exchange.auto");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO_ON = TextEntry.tooltip(LCApi.MODID,"wallet.exchange.auto.on");
    public static final TextEntry TOOLTIP_WALLET_EXCHANGE_AUTO_OFF = TextEntry.tooltip(LCApi.MODID,"wallet.exchange.auto.off");
    public static final TextEntry TOOLTIP_WALLET_BANK_ACCOUNT = TextEntry.tooltip(LCApi.MODID,"wallet.bank_account");

    public static final Identifier DEFAULT_SOUND = LCApi.id("coins_clinking");
    public static final WeightedList<Identifier> DEFAULT_SOUND_ENTRIES = WeightedList.of(DEFAULT_SOUND);

    public static Identifier model(String walletID) { return model(LCApi.id(walletID)); }
    public static Identifier model(Identifier walletID) { return walletID.withPrefix("wallet_hip/"); }

    public WalletItem(Properties properties) { super(properties.stacksTo(1).enchantable(10)); }
    public WalletItem(Properties properties,Identifier model,int capacity,WalletUpgradeData upgradeData) {
        this(properties
                .component(LCDataComponents.WALLET_MODEL,model)
                .component(LCDataComponents.WALLET_CAPACITY,capacity)
                .component(LCDataComponents.WALLET_UPGRADE_DATA,upgradeData)
                .component(LCDataComponents.WALLET_CONTENTS,ItemContents.EMPTY)
        );
    }

    private static boolean isNotEnchanted(DataComponentGetter item) {
        return !item.has(DataComponents.ENCHANTMENTS) || item.get(DataComponents.ENCHANTMENTS).isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> builder,TooltipFlag tooltipFlag) {

        //Slots
        builder.accept(TOOLTIP_WALLET_CAPACITY.get(getWalletSlots(stack)).withStyle(ChatFormatting.YELLOW));

        //Upgradeable Status
        if(stack.has(LCDataComponents.WALLET_UPGRADE_DATA) && LCConfig.SERVER.walletCapacityUpgradeable.get())
        {
            WalletUpgradeData upgradeData = stack.get(LCDataComponents.WALLET_UPGRADE_DATA);
            int upgradeCount = stack.getOrDefault(LCDataComponents.WALLET_UPGRADE_COUNT,0);
            if(upgradeCount < upgradeData.maxUpgrades())
            {
                List<Holder<Item>> upgradeItems = upgradeData.upgradeItems().stream().toList();
                ItemStack exampleItem = new ItemStack(ListHelper.cyclingValueFromList(upgradeItems,Items.AIR.builtInRegistryHolder()));
                if(!exampleItem.isEmpty())
                {
                    ListHelper.consumeAll(builder,TooltipHelper.splitTooltips(TOOLTIP_WALLET_UPGRADEABLE.get(
                                    TooltipHelper.lazyFormat(exampleItem.getItemName(),ChatFormatting.AQUA),
                                    TooltipHelper.lazyFormat(String.valueOf(upgradeData.bonusSlots()),ChatFormatting.GOLD),
                                    upgradeData.maxUpgrades() - upgradeCount)
                            ,ChatFormatting.YELLOW));
                }
            }
            else if(upgradeCount > 0)
                builder.accept(TOOLTIP_WALLET_FULLY_UPGRADED.getWithStyle(ChatFormatting.YELLOW));
        }

        //Abilities
        if(hasPickupAbility(stack))
            builder.accept(TOOLTIP_WALLET_PICKUP.getWithStyle(ChatFormatting.YELLOW));
        if(hasExchangeAbility(stack))
        {
            if(hasPickupAbility(stack))
            {
                Component onOffText = stack.has(LCDataComponents.WALLET_DISABLE_AUTOEXCHANGE) ? TOOLTIP_WALLET_EXCHANGE_AUTO_OFF.getWithStyle(ChatFormatting.RED) : TOOLTIP_WALLET_EXCHANGE_AUTO_ON.getWithStyle(ChatFormatting.GREEN);
                builder.accept(TOOLTIP_WALLET_EXCHANGE_AUTO.get(onOffText).withStyle(ChatFormatting.YELLOW));
            }
            else
                builder.accept(TOOLTIP_WALLET_EXCHANGE_MANUAL.getWithStyle(ChatFormatting.YELLOW));
        }
        if(hasBankAbility(stack))
            builder.accept(TOOLTIP_WALLET_BANK_ACCOUNT.getWithStyle(ChatFormatting.YELLOW));

        //Enchantment Tooltips
        CoinMagnetEnchantmentHelper.addWalletTooltips(stack,display,builder);

        //Stop here if the coin data isn't loaded yet somehow (i.e. early tooltip assessment)
        if(LCApi.getCoinAPI().dataNotLoaded())
            return;

        //Contents
        WalletStorage storage = new WalletStorage(ItemAccess.forStack(stack));
        MoneyResourceHandler contents = storage.getMoneyResourceViewer(ISidedContext.wrap(context));
        if(!contents.isEmpty())
        {
            builder.accept(TOOLTIP_WALLET_STORED_MONEY.get());
            MoneyDisplayHelper.contentsAsTooltip(contents,builder,ChatFormatting.DARK_GREEN);
        }

        //Display ItemEnchantments data component if the bonus magnet is present but no additional enchantments are present
        if(display.shows(DataComponents.ENCHANTMENTS) && isNotEnchanted(stack) && stack.has(LCDataComponents.WALLET_MAGNET_BONUS))
        {
            try {
                ItemEnchantments enchantments = this.getAllEnchantments(stack,context.registries().lookupOrThrow(Registries.ENCHANTMENT));
                if(!enchantments.isEmpty())
                    enchantments.addToTooltip(context,builder,tooltipFlag,stack);
            } catch (IllegalStateException ignored) {} //Catch the illegal state exception just in case someone tries to get the tooltips before enchantments are loaded
        }

    }

    @Override
    public InteractionResult use(Level level,Player player,InteractionHand hand) {
        ItemStack wallet = player.getItemInHand(hand);
        if(!level.isClientSide())
        {
            int walletSlot = ItemHelper.getItemIndex(player.getInventory(),wallet);
            if(walletSlot >= 0)
            {
                if(LCCuriosHelper.get().isNotLoaded() && player.isCrouching())
                {
                    //Try to equip the wallet if the player is crouching
                    //Don't attempt this if curios is loaded, as it won't sync the wallet before the menu opens
                    WalletAttachment attachment = player.getData(LCDataAttachments.WALLET);
                    if(attachment.getWallet().isEmpty())
                    {
                        attachment.setWallet(wallet);
                        player.setItemInHand(hand,ItemStack.EMPTY);
                        walletSlot = -1;
                    }
                }
                WalletMenu.openMenu(player,walletSlot);
            }
        }
        else
        {
            level.playSound(player,player.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,0.75f,1.25f + player.getRandom().nextFloat() * 0.5f);
            if(!wallet.getOrDefault(LCDataComponents.WALLET_CONTENTS,ItemContents.EMPTY).isEmpty())
                level.playSound(player,player.blockPosition(),LCSounds.COINS_CLINKING.get(),SoundSource.PLAYERS,0.4f,1f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack wallet, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if(clickAction == ClickAction.SECONDARY && LCConfig.SERVER.walletCapacityUpgradeable.get() && wallet.has(LCDataComponents.WALLET_UPGRADE_DATA))
        {
            WalletUpgradeData data = wallet.get(LCDataComponents.WALLET_UPGRADE_DATA);
            if(other.is(data.upgradeItems()))
            {
                int upgradeCount = wallet.getOrDefault(LCDataComponents.WALLET_UPGRADE_COUNT,0);
                if(upgradeCount < data.maxUpgrades())
                {
                    //Deny upgrading when the player is in the wallet menu as I don't want to need to refactor the slots
                    if(player.containerMenu instanceof AbstractWalletMenu)
                        return true;
                    //Consume the upgrade item, and increment the "upgrade count"
                    other.shrink(1);
                    wallet.set(LCDataComponents.WALLET_UPGRADE_COUNT,upgradeCount + 1);
                    //Inform the slot that the item has changed
                    slot.setChanged();
                    carriedItem.set(other);
                }
                //Still consume the interaction if the item was in fact an upgrade item
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(wallet, other, slot, clickAction, player, carriedItem);
    }

    @Override
    public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
        int level = super.getEnchantmentLevel(stack,enchantment);
        if(enchantment.is(LCApi.id("coin_magnet")))
            return Math.min(255,level + stack.getOrDefault(LCDataComponents.WALLET_MAGNET_BONUS,0));
        return level;
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        if(stack.has(LCDataComponents.WALLET_MAGNET_BONUS))
        {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(super.getAllEnchantments(stack,lookup));
            lookup.get(WalletEnchantments.COIN_MAGNET).ifPresent(holder ->
                enchantments.set(holder,Math.min(255,enchantments.getLevel(holder) + stack.get(LCDataComponents.WALLET_MAGNET_BONUS))));
            return enchantments.toImmutable();
        }
        return super.getAllEnchantments(stack,lookup);
    }

    public static int getWalletSlots(DataComponentGetter wallet) {
        int slots = wallet.getOrDefault(LCDataComponents.WALLET_CAPACITY,1);
        WalletUpgradeData upgradeData = wallet.get(LCDataComponents.WALLET_UPGRADE_DATA);
        if(upgradeData != null && LCConfig.SERVER.walletCapacityUpgradeable.get())
        {
            int upgradeCount = Math.min(wallet.getOrDefault(LCDataComponents.WALLET_UPGRADE_COUNT,0),upgradeData.maxUpgrades());
            slots += upgradeCount * upgradeData.bonusSlots();
        }
        return Math.min(slots,MAX_WALLET_SLOTS);
    }

    public static boolean isWallet(TypedInstance<Item> item) { return item.typeHolder().value() instanceof WalletItem; }

    public static boolean hasExchangeAbility(ItemStack wallet) { return LCConfig.SERVER.walletCanExchange.contains(wallet); }
    public static boolean hasPickupAbility(ItemStack wallet) { return LCConfig.SERVER.walletCanPickup.contains(wallet); }
    public static boolean hasAutoExchangeAbility(ItemStack wallet) { return hasExchangeAbility(wallet) && hasPickupAbility(wallet); }
    public static boolean hasBankAbility(ItemStack wallet) { return LCConfig.SERVER.walletCanBank.contains(wallet); }

    public static boolean shouldAutoExchange(ItemStack wallet) {
        return LCConfig.SERVER.walletCanExchange.contains(wallet) && LCConfig.SERVER.walletCanPickup.contains(wallet) && !wallet.has(LCDataComponents.WALLET_DISABLE_AUTOEXCHANGE);
    }

    public static void playPickupSound(Entity entity, ItemStack wallet) {
        Level level = entity.level();
        BuiltInRegistries.SOUND_EVENT.getOptional(getPickupSound(level,wallet))
                .ifPresent(sound -> level.playSound(null,entity,sound,SoundSource.PLAYERS,0.4f,1f));
    }

    public static Identifier getPickupSound(Level level,ItemStack wallet) {
        if(!isWallet(wallet) || wallet.has(LCDataComponents.WALLET_FORCE_DEFAULT_SOUND))
            return DEFAULT_SOUND;
        WeightedList<Identifier> sounds = wallet.getOrDefault(LCDataComponents.WALLET_PICKUP_SOUND, DEFAULT_SOUND_ENTRIES);
        return sounds.getRandom(level.getRandom()).orElse(DEFAULT_SOUND);
    }

    public static boolean hasNonDefaultSound(ItemStack wallet) {
        if(!isWallet(wallet) || !wallet.has(LCDataComponents.WALLET_PICKUP_SOUND))
            return false;
        List<Weighted<Identifier>> sounds = wallet.get(LCDataComponents.WALLET_PICKUP_SOUND).unwrap();
        for(Weighted<Identifier> s : sounds) {
            if(!s.value().equals(DEFAULT_SOUND))
                return true;
        }
        return false;
    }

}
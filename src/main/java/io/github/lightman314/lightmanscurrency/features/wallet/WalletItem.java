package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.wallet.enchantments.WalletEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.function.Consumer;

public class WalletItem extends Item {

    public static final int MAX_WALLET_SLOTS = 78;

    public static Identifier model(String walletID) { return model(LCApi.id(walletID)); }
    public static Identifier model(Identifier walletID) { return walletID.withPrefix("wallet_hip/"); }

    public WalletItem(Properties properties) { super(properties.stacksTo(1).enchantable(10)); }
    public WalletItem(Properties properties,Identifier model,int capacity,WalletUpgradeData upgradeData) {
        this(properties
                .component(LCDataComponents.WALLET_MODEL,model)
                .component(LCDataComponents.WALLET_CAPACITY,capacity)
                .component(LCDataComponents.WALLET_UPGRADE_DATA,upgradeData)
                .component(LCDataComponents.WALLET_CONTENTS,WalletStorageData.EMPTY)
        );
    }

    private static boolean isNotEnchanted(DataComponentGetter item) {
        return !item.has(DataComponents.ENCHANTMENTS) || item.get(DataComponents.ENCHANTMENTS).isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> builder,TooltipFlag tooltipFlag) {

        //Coin Magnet Enchantment if the enchantment data is missing
        if(display.shows(DataComponents.ENCHANTMENTS) && isNotEnchanted(stack) && stack.has(LCDataComponents.WALLET_MAGNET_BONUS))
        {
            ItemEnchantments enchantments = this.getAllEnchantments(stack,context.registries().lookupOrThrow(Registries.ENCHANTMENT));
            if(!enchantments.isEmpty() && display.shows(DataComponents.ENCHANTMENTS))
                enchantments.addToTooltip(context,builder,tooltipFlag,stack);
        }

        //Slots
        builder.accept(LCText.Items.TOOLTIP_WALLET_CAPACITY.get(getWalletSlots(stack)).withStyle(ChatFormatting.YELLOW));

        //Upgradeable Status
        if(stack.has(LCDataComponents.WALLET_UPGRADE_DATA))
        {
            WalletUpgradeData upgradeData = stack.get(LCDataComponents.WALLET_UPGRADE_DATA);
            int upgradeCount = stack.getOrDefault(LCDataComponents.WALLET_UPGRADE_COUNT,0);
            if(upgradeCount < upgradeData.maxUpgrades())
            {
                List<Holder<Item>> upgradeItems = upgradeData.upgradeItems().stream().toList();
                ItemStack exampleItem = new ItemStack(ListHelper.cyclingValueFromList(upgradeItems,Items.AIR.builtInRegistryHolder()));
                if(!exampleItem.isEmpty())
                {
                    TooltipHelper.splitTooltips(LCText.Items.TOOLTIP_WALLET_UPGRADEABLE.get(
                            TooltipHelper.lazyFormat(exampleItem.getHoverName(),ChatFormatting.AQUA),
                            TooltipHelper.lazyFormat(String.valueOf(upgradeData.bonusSlots()),ChatFormatting.GOLD),
                                    upgradeData.maxUpgrades() - upgradeCount)
                            ,ChatFormatting.YELLOW);
                }
            }
        }

        //Abilities


        //Enchantment Tooltips

        //Contents
        WalletStorage storage = new WalletStorage(ItemAccess.forStack(stack));
        MoneyResourceHandler contents = LCApi.getMoneyAPI().getContainersMoneyViewer(storage,ISidedContext.LOGICAL_CLIENT);
        if(!contents.isEmpty())
        {
            builder.accept(LCText.Items.TOOLTIP_WALLET_STORED_MONEY.get());
            MoneyDisplayHelper.contentsAsTooltip(contents,builder,ChatFormatting.DARK_GREEN);
        }

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
        if(upgradeData != null)
        {
            int upgradeCount = Math.min(wallet.getOrDefault(LCDataComponents.WALLET_UPGRADE_COUNT,0),upgradeData.maxUpgrades());
            slots += upgradeCount * upgradeData.bonusSlots();
        }
        return Math.min(slots,MAX_WALLET_SLOTS);
    }

    public static boolean isWallet(ItemResource resource) { return resource.getItem() instanceof WalletItem; }
    public static boolean isWallet(ItemStack stack) { return stack.getItem() instanceof WalletItem; }

}
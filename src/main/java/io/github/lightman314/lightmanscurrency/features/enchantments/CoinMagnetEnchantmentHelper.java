package io.github.lightman314.lightmanscurrency.features.enchantments;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.LCEnchantmentDataComponents;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletEventListener;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Consumer;

public final class CoinMagnetEnchantmentHelper {

    private CoinMagnetEnchantmentHelper() {}

    public static void runEntityTick(WalletAttachment attachment, LivingEntity entity) {
        if(entity.isSpectator() || entity.level().isClientSide())
            return;
        ItemStack wallet = attachment.getWallet();
        if(!WalletItem.isWallet(wallet) || !WalletItem.hasPickupAbility(wallet))
            return;
        //Get the level
        int enchantmentLevel = getCoinMagnetLevel(wallet);
        if(enchantmentLevel <= 0)
            return;
        float range = getCollectionRange(enchantmentLevel);
        WalletStorage storage = new WalletStorage(attachment);
        Level level = entity.level();

        AABB searchBox = new AABB(entity.getX() - range, entity.getY() - range,entity.getZ() - range,entity.getX() + range,entity.getY() + range,entity.getZ() + range);
        boolean success = false;
        try(Transaction transaction = Transaction.openRoot()) {
            for(Entity e : level.getEntities(entity,searchBox,e -> coinMagnetEntityFilter(e,entity))) {
                ItemEntity ie = (ItemEntity)e;
                ItemStack coinStack = ie.getItem();
                try(Transaction tx = Transaction.open(transaction)) {
                    int inserted = storage.pickup(ItemResource.of(coinStack),coinStack.getCount(),tx);
                    if(inserted > 0 && inserted <= coinStack.getCount()) {
                        tx.commit();
                        coinStack.shrink(inserted);
                        if(coinStack.isEmpty())
                            ie.discard();
                        else
                            ie.setItem(coinStack);
                        success = true;
                    }
                }
            }
            if(success) {
                //Committing the transaction now automatically updates the equipped wallet item, so we don't need to do anything fancy here anymore
                //Still moved the play sound call ouside of the loop to avoid duplicate sound effects
                transaction.commit();
                WalletItem.playPickupSound(entity,wallet);
            }
        }
    }

    public static boolean coinMagnetEntityFilter(Entity entity,LivingEntity walletHolder) {
        return entity instanceof ItemEntity item &&
                WalletEventListener.canPlayerPickup(item,walletHolder) &&
                LCApi.getCoinAPI().isAllowedInCoinContainer(item.getItem(),false);
    }

    public static int getCoinMagnetLevel(ItemStack wallet) {
        var pair = EnchantmentHelper.getHighestLevel(wallet,LCEnchantmentDataComponents.COLLECT_COINS.get());
        return pair == null ? 0 : pair.getSecond();
    }

    public static float getCollectionRange(int enchantmentLevel) {
        enchantmentLevel = Math.min(enchantmentLevel,LCConfig.SERVER.coinMagnetCalculationCap.get());
        if(enchantmentLevel < 1)
            return 0f;
        //Reduce level by one as the first level only uses the base range
        enchantmentLevel -= 1;
        return LCConfig.SERVER.coinMagnetBaseRange.get() + (LCConfig.SERVER.coinMagnetLeveledRange.get() * enchantmentLevel);
    }

    public static Component getCollectionRangeDisplay(int enchantmentLevel) {
        float range = getCollectionRange(enchantmentLevel);
        return Component.literal(String.valueOf(Math.round(range))).withStyle(ChatFormatting.GREEN);
    }

    public static void addWalletTooltips(ItemStack wallet,TooltipDisplay display,Consumer<Component> builder) {
        if(WalletItem.isWallet(wallet) && display.shows(DataComponents.ENCHANTMENTS)) {
            int enchantLevel = getCoinMagnetLevel(wallet);
            if(enchantLevel > 0 && WalletItem.hasPickupAbility(wallet))
                builder.accept(WalletItem.TOOLTIP_WALLET_PICKUP_MAGNET.get(getCollectionRangeDisplay(enchantLevel)).withStyle(ChatFormatting.YELLOW));
        }
    }

}
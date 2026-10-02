package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.TransactionCommitListener;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.ItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.core.LCGameRules;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public final class WalletEventListener {
    private WalletEventListener() {}

    @SubscribeEvent
    private static void pickUpCoinItems(ItemEntityPickupEvent.Pre event) {
        //Do nothing if another mod flagged the item as unable to be picked up
        if(event.canPickup() == TriState.FALSE)
            return;
        Player player = event.getPlayer();
        ItemEntity ie = event.getItemEntity();
        ItemStack coin = ie.getItem();
        if(canPlayerPickup(ie,player) && LCApi.getCoinAPI().isAllowedInCoinContainer(coin,false)) {
            WalletAttachment handler = player.getData(LCDataAttachments.WALLET);
            ItemStack wallet = handler.getWallet();
            if(!WalletItem.isWallet(wallet) || !WalletItem.hasPickupAbility(wallet))
                return;
            WalletStorage storage = new WalletStorage(handler);
            try(Transaction transaction = Transaction.openRoot()) {
                int inserted = storage.pickup(ItemResource.of(coin),coin.getCount(),transaction);
                if(inserted > 0) {
                    transaction.commit();
                    coin.shrink(inserted);
                    if(!coin.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(coin);
                        //Shrink the rest of the count away so that the item will appear empty to any other listeners
                        coin.shrink(coin.getCount());
                    }
                    //Discard the item entity as it's now empty
                    ie.discard();
                    event.setCanPickup(TriState.FALSE);
                }
            }
        }
    }

    @SubscribeEvent
    private static void walletDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level().isClientSide())
            return;
        if(!entity.isSpectator() && (entity.hasData(LCDataAttachments.WALLET) || LCCuriosHelper.get().hasWalletSlot(entity))) {
            boolean letCuriosHandleWallet = LCCuriosHelper.get().hasWalletSlot(entity);
            WalletAttachment holder = entity.getData(LCDataAttachments.WALLET);
            ItemStack wallet = holder.getWallet();
            if(wallet.isEmpty() || !WalletItem.isWallet(wallet))
                return;
            if(entity instanceof Player player && player.level() instanceof ServerLevel sl) { //Only worry about game rules on players. Otherwise, it always drops the wallet.
                List<ItemStack> drops = new ArrayList<>();
                GameRules rules = sl.getGameRules();
                boolean keepWallet = letCuriosHandleWallet || rules.get(LCGameRules.KEEP_WALLET.get()) || rules.get(GameRules.KEEP_INVENTORY);
                //Collect wallet content drops
                double dropRate = LCConfig.SERVER.walletDropRate.get();
                if(dropRate > 0d) {
                    WalletStorage storage = new WalletStorage(holder);
                    MoneyResourceHandler walletMoney = storage.getMoneyResourceHandler(
                            //Drop overflow items instead of giving them to the player in this scenario
                            TransactionCommitListener.wrapAction(drops::add),
                            ISidedContext.LOGICAL_SERVER);
                    try(Transaction transaction = Transaction.openRoot()) {
                        for(MoneyValue contents : walletMoney.getAllResources()) {
                            MoneyValue take = contents.multiplyValue(dropRate);
                            if(take.getInternalValue() > contents.getInternalValue())
                                take = contents;
                            if(take.isEmpty() || !(take instanceof ItemBasedValue takeValue))
                                continue;
                            try(Transaction tx = Transaction.open(transaction)) {
                                MoneyValue taken = walletMoney.extract(take,tx);
                                if(taken.equals(take)) {
                                    drops.addAll(takeValue.getAsSeperatedItemList());
                                    tx.commit();
                                }
                            }
                        }
                        //Always commit the root transaction
                        transaction.commit();
                    }
                    //Update local wallet variable now that some of the contents have been removed
                    wallet = holder.getWallet();
                }
                if(!keepWallet) {
                    drops.addFirst(wallet);
                    holder.setWallet(ItemStack.EMPTY);
                }
                List<ItemEntity> dropItems = createDropEntities(player,drops);
                if(LCConfig.SERVER.walletDropsManualSpawn.get()) {
                    Level level = entity.level();
                    for(ItemEntity e : dropItems)
                        level.addFreshEntity(e);
                }
                else
                    event.getDrops().addAll(dropItems);
            }
            else if(!letCuriosHandleWallet) {
                event.getDrops().add(createDropEntity(entity,wallet.copy()));
                holder.setWallet(ItemStack.EMPTY);
            }
        }
    }

    private static ItemEntity createDropEntity(LivingEntity entity,ItemStack item) {
        Vec3 position = entity.position();
        return new ItemEntity(entity.level(),position.x,position.y,position.z,item);
    }
    private static List<ItemEntity> createDropEntities(LivingEntity entity,List<ItemStack> items) {
        List<ItemEntity> result = new ArrayList<>();
        Vec3 position = entity.position();
        Level level = entity.level();
        for(ItemStack s : items)
            result.add(new ItemEntity(level,position.x,position.y,position.z,s));
        return result;
    }

    public static boolean canPlayerPickup(ItemEntity item,Entity walletHolder) {
        if(item.hasPickUpDelay())
            return false;
        return item.getTarget() == null || item.getTarget().equals(walletHolder.getUUID());
    }

}
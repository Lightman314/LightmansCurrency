package io.github.lightman314.lightmanscurrency.integration.curios;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public final class LCCuriosHelperImpl extends LCCuriosHelper {

    private LCCuriosHelperImpl() {}

    public static void init() { override(new LCCuriosHelperImpl()); }

    @Override
    public boolean isLoaded() { return true; }

    @Override
    public boolean hasWalletSlot(LivingEntity entity) { return this.getWalletHandler(entity) != null; }

    @Override
    public ItemStack getWallet(LivingEntity entity) {
        ICurioStacksHandler handler = getWalletHandler(entity);
        if(handler == null)
            return ItemStack.EMPTY;
        return handler.getStacks().getStackInSlot(0);
    }

    @Override
    public ItemStack getVisibleWallet(LivingEntity entity) {
        ICurioStacksHandler handler = getWalletHandler(entity);
        if(handler == null)
            return ItemStack.EMPTY;
        ItemStack wallet = handler.getCosmeticStacks().getStackInSlot(0);
        if(wallet.isEmpty())
            wallet = handler.getStacks().getStackInSlot(0);
        return wallet;
    }

    @Override
    public boolean getWalletVisibility(LivingEntity entity) {
        ICurioStacksHandler handler = getWalletHandler(entity);
        if(handler == null)
            return false;
        return handler.getRenders().getFirst();
    }

    @Override
    public void setWallet(LivingEntity entity,ItemStack wallet) {
        ICurioStacksHandler handler = getWalletHandler(entity);
        if(handler != null)
            handler.getStacks().setStackInSlot(0,wallet);
    }

    @Nullable
    private ICurioStacksHandler getWalletHandler(LivingEntity entity) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(entity);
        return handler == null ? null : getWalletHandler(handler);
    }

    @Nullable
    private ICurioStacksHandler getWalletHandler(ICuriosItemHandler handler) {
        ICurioStacksHandler result = handler.getStacksHandler(WALLET_SLOT).orElse(null);
        if(result != null && result.getSlots() > 0)
            return result;
        return null;
    }

    @Override
    public boolean hasItem(LivingEntity entity,Predicate<ItemStack> test) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(entity);
        if(handler == null)
            return false;
        for(ICurioStacksHandler stacks : List.copyOf(handler.getCurios().values())) {
            for(int i = 0; i < stacks.getSlots(); ++i) {
                ItemStack stack = stacks.getStacks().getStackInSlot(i);
                if(test.test(stack))
                    return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getRandomItem(LivingEntity entity, Predicate<ItemStack> test) {
        //Using a weighted list here for convenience
        WeightedList.Builder<ItemStack> matches = WeightedList.builder();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventoryOrNull(entity);
        if(handler == null)
            return ItemStack.EMPTY;
        for(ICurioStacksHandler stacks : List.copyOf(handler.getCurios().values())) {
            for(int i = 0; i < stacks.getSlots(); ++i) {
                ItemStack stack = stacks.getStacks().getStackInSlot(i);
                if(test.test(stack))
                    matches.add(stack);
            }
        }
        //Get random item from the list
        return matches.build().getRandom(entity.getRandom()).orElse(ItemStack.EMPTY);
    }

}
package io.github.lightman314.lightmanscurrency.integration.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Predicate;

public abstract sealed class LCCuriosHelper permits LCCuriosHelper.NotLoadedImpl, LCCuriosHelperImpl {

    public static final String WALLET_SLOT = "wallet";

    private static LCCuriosHelper INSTANCE;
    public static LCCuriosHelper get() {
        if(INSTANCE == null)
            INSTANCE = new NotLoadedImpl();
        return INSTANCE;
    }
    protected static void override(LCCuriosHelper helper) { INSTANCE = helper; }

    public abstract boolean isLoaded();
    public final boolean isNotLoaded() { return !this.isLoaded(); }

    public abstract boolean hasWalletSlot(LivingEntity entity);

    public abstract ItemStack getWallet(LivingEntity entity);
    public abstract ItemStack getVisibleWallet(LivingEntity entity);
    public abstract boolean getWalletVisibility(LivingEntity entity);

    public abstract void setWallet(LivingEntity entity,ItemStack wallet);

    public boolean hasItem(LivingEntity entity,ItemLike item) { return this.hasItem(entity,s -> s.is(item.asItem())); }
    public abstract boolean hasItem(LivingEntity entity,Predicate<ItemStack> test);

    public abstract ItemStack getRandomItem(LivingEntity entity,Predicate<ItemStack> test);

    private static final class NotLoadedImpl extends LCCuriosHelper {

        @Override
        public boolean isLoaded() { return false; }
        @Override
        public boolean hasWalletSlot(LivingEntity entity) { return false; }
        @Override
        public ItemStack getWallet(LivingEntity entity) { return ItemStack.EMPTY; }
        @Override
        public ItemStack getVisibleWallet(LivingEntity entity) { return ItemStack.EMPTY; }
        @Override
        public boolean getWalletVisibility(LivingEntity entity) { return false; }
        @Override
        public void setWallet(LivingEntity entity,ItemStack wallet) { }
        @Override
        public boolean hasItem(LivingEntity entity,Predicate<ItemStack> test) { return false; }
        @Override
        public ItemStack getRandomItem(LivingEntity entity, Predicate<ItemStack> test) { return ItemStack.EMPTY; }
    }

}
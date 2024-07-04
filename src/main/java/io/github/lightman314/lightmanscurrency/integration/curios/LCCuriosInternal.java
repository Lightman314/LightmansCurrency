package io.github.lightman314.lightmanscurrency.integration.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * Actual LC Curios Methods<br>
 * Should only be called through their {@link LCCurios} counterparts to ensure that the mod exists and to catch any API errors that may occur.
 */
@ApiStatus.Internal
public class LCCuriosInternal {

    public static final String WALLET_SLOT = "wallet";

    public static boolean isCuriosLoaded() { return false;/*ModList.get().isLoaded("curios");//*/ }

    /**
     * Whether the given entity has a valid wallet slot from the Curios inventory;
     */
    public static boolean hasCuriosWalletSlot(@Nonnull LivingEntity entity)
    {
        if(entity == null)
            return false;
        try {
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
                return stacksHandler != null && stacksHandler.getSlots() > 0;
            }*/
        } catch(Throwable t) { LightmansCurrency.LogError("Error checking curios wallet slot validity.", t); }
        return false;
    }

    @Nonnull
    public static ItemStack getCuriosWalletContents(@Nonnull LivingEntity entity) {
        try {
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
                if(stacksHandler != null && stacksHandler.getSlots() > 0)
                    return stacksHandler.getStacks().getStackInSlot(0);
            }*/
        } catch(Throwable t) { LightmansCurrency.LogError("Error getting wallet from curios wallet slot.", t); }
        return ItemStack.EMPTY;
    }

    public static boolean setCuriosWalletContents(@Nonnull LivingEntity entity, @Nonnull ItemStack wallet) {
        try {
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*
            ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
                if(stacksHandler != null && stacksHandler.getSlots() > 0)
                {
                    stacksHandler.getStacks().setStackInSlot(0, wallet);
                    return true;
                }
            }*/
        } catch(Throwable t) { LightmansCurrency.LogError("Error placing wallet into the curios wallet slot.", t); }
        return false;
    }

    public static boolean getCuriosWalletVisibility(@Nonnull LivingEntity entity) {
        try {
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
                if(stacksHandler != null && stacksHandler.getSlots() > 0)
                    return stacksHandler.getRenders().get(0);
            }*/
        } catch(Throwable t) { LightmansCurrency.LogError("Error getting wallet slot visibility from curios.", t); }
        return false;
    }

    public static boolean hasInCuriosSlot(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> predicate)
    {
        try{
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*
            ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                for(Map.Entry<String,ICurioStacksHandler> entry : curiosHelper.getCurios().entrySet())
                {
                    ICurioStacksHandler stacksHandler = entry.getValue();
                    if(stacksHandler != null)
                    {
                        IDynamicStackHandler sh = stacksHandler.getStacks();
                        for(int i = 0; i < sh.getSlots(); ++i)
                        {
                            if(predicate.test(sh.getStackInSlot(i)))
                                return true;
                        }
                    }
                }
            }*/
        } catch(Throwable t) { LightmansCurrency.LogError("Error checking for Portable Terminal from curios.", t); }
        return false;
    }

    @Nullable
    public static ItemStack getRandomFromCuriosSlot(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> predicate)
    {
        try {
            throw new IllegalStateException("Curios Integration not setup for this LC Version!");
            /*List<ItemStack> results = new ArrayList<>();
            ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
            if(curiosHelper != null)
            {
                for(Map.Entry<String,ICurioStacksHandler> entry : curiosHelper.getCurios().entrySet())
                {
                    ICurioStacksHandler stacksHandler = entry.getValue();
                    if(stacksHandler != null)
                    {
                        IDynamicStackHandler sh = stacksHandler.getStacks();
                        for(int i = 0; i < sh.getSlots(); ++i)
                        {
                            ItemStack item = sh.getStackInSlot(i);
                            if(EnchantmentHelper.has(item, ModEnchantments.REPAIR_WITH_MONEY.get()))
                                results.add(item);
                        }
                    }
                }
            }
            if(!results.isEmpty())
                return results.get(entity.getRandom().nextInt(results.size()));
            */
        } catch (Throwable t) { LightmansCurrency.LogError("Error checking for Money Mending item from curios.", t); }
        return null;
    }

    public static void registerWalletCapability(@Nonnull RegisterCapabilitiesEvent event)
    {

    }

    /*
    public static ICapabilityProvider createWalletProvider(ItemStack stack)
    {
        try{
            return CurioItemCapability.createProvider(new ICurio()
            {

                @Override
                public ItemStack getStack() { return stack; }

                @Nonnull
                @Override
                public SoundInfo getEquipSound(SlotContext context) { return new SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1f, 1f); }

                @Override
                public boolean canEquipFromUse(SlotContext context) { return false; }

                @Override
                public boolean canSync(SlotContext context) { return true; }

                @Override
                public boolean canEquip(SlotContext context) { return true; }

                @Override
                public boolean canUnequip(SlotContext context) {
                    if(context.entity() instanceof Player player && player.containerMenu instanceof WalletMenuBase menu)
                    {
                        //Prevent unequipping if the wallet is open in the menu.
                        return !menu.isEquippedWallet();
                    }
                    return true;
                }

                @Nonnull
                @Override
                public DropRule getDropRule(SlotContext context, DamageSource source, int lootingLevel, boolean recentlyHit)
                {
                    if(ModGameRules.safeGetCustomBool(context.entity().level(), ModGameRules.KEEP_WALLET, false))
                        return DropRule.ALWAYS_KEEP;
                    else
                        return DropRule.DEFAULT;
                }

            });
        } catch(Throwable t) { return null; }
    }*/

}
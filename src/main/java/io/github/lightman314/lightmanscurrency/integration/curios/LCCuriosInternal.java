package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LCCuriosInternal {

    public static final String WALLET_SLOT = "wallet";

    @SuppressWarnings("removal")
    @Nullable
    private static ICuriosItemHandler getCurios(@Nonnull LivingEntity entity) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
    }

    @Nullable
    private static ICurioStacksHandler getStacks(@Nonnull LivingEntity entity, @Nonnull String slot) {
        ICuriosItemHandler handler = getCurios(entity);
        if(handler != null)
            return handler.getStacksHandler(slot).orElse(null);
        return null;
    }

    public static boolean hasWalletSlot(@Nonnull LivingEntity entity) {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            return handler != null && handler.getSlots() > 0;
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return false;
    }

    @Nonnull
    public static ItemStack getCuriosWalletItem(@Nonnull LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getStacks().getStackInSlot(0);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static ItemStack getVisibleCuriosWalletItem(@Nonnull LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
            {
                ItemStack cosmetic = ItemStack.EMPTY;
                if(handler.getCosmeticStacks().getSlots() > 0)
                    cosmetic = handler.getCosmeticStacks().getStackInSlot(0);
                return cosmetic.isEmpty() ? handler.getStacks().getStackInSlot(0) : cosmetic;
            }
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return ItemStack.EMPTY;
    }

    public static void setCuriosWalletItem(@Nonnull LivingEntity entity, @Nonnull ItemStack item)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                handler.getStacks().setStackInSlot(0,item);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
    }

    public static boolean getCuriosWalletVisibility(@Nonnull LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getRenders().get(0);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return false;
    }

    public static boolean hasItem(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> check)
    {
        try {
            ICuriosItemHandler handler = getCurios(entity);
            if(handler != null)
            {
                for(ICurioStacksHandler stacks : handler.getCurios().values())
                {
                    IDynamicStackHandler sh = stacks.getStacks();
                    for(int i = 0; i < sh.getSlots(); ++i)
                    {
                        if(check.test(sh.getStackInSlot(i)))
                            return true;
                    }
                }
            }
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return false;
    }

    public static boolean hasPortableTerminal(@Nonnull LivingEntity entity) { return hasItem(entity, stack -> stack.getItem() instanceof PortableTerminalItem); }

    public static boolean hasPortableATM(@Nonnull LivingEntity entity) { return hasItem(entity, stack -> stack.getItem() instanceof PortableATMItem); }

    @Nullable
    public static ItemStack getRandomItem(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> check)
    {
        try {
            ICuriosItemHandler handler = getCurios(entity);
            if(handler != null)
            {
                List<ItemStack> options = new ArrayList<>();
                for(ICurioStacksHandler stacks : handler.getCurios().values())
                {
                    IDynamicStackHandler sh = stacks.getStacks();
                    for(int i = 0; i < sh.getSlots(); ++i)
                    {
                        ItemStack stack = sh.getStackInSlot(i);
                        if(check.test(stack))
                            options.add(stack);
                    }
                }
                //Return a random entry from the found options
                if(!options.isEmpty())
                    return options.get(entity.getRandom().nextInt(options.size()));
            }
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return null;
    }

    @Nullable
    public static ICapabilityProvider createWalletProvider(@Nonnull ItemStack stack)
    {
        try{
            return CurioItemCapability.createProvider(new ICurio()
            {
                @Override
                public ItemStack getStack() { return stack; }
                @Nonnull
                @Override
                public SoundInfo getEquipSound(SlotContext context) { return new SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER, 1f, 1f); }
                @Override
                public boolean canEquipFromUse(SlotContext context) { return false; }
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
    }

}
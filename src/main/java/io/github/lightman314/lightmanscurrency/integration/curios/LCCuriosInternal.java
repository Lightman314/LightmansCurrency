package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LCCuriosInternal {

    public static final String WALLET_SLOT = "wallet";

    @SuppressWarnings("removal")
    @Nullable
    private static ICuriosItemHandler getCurios(LivingEntity entity) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
    }

    @Nullable
    private static ICurioStacksHandler getStacks(LivingEntity entity, String slot) {
        ICuriosItemHandler handler = getCurios(entity);
        if(handler != null)
            return handler.getStacksHandler(slot).orElse(null);
        return null;
    }

    public static boolean hasWalletSlot(LivingEntity entity) {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            return handler != null && handler.getSlots() > 0;
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return false;
    }

    
    public static ItemStack getCuriosWalletItem(LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getStacks().getStackInSlot(0);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return ItemStack.EMPTY;
    }

    
    public static ItemStack getVisibleCuriosWalletItem(LivingEntity entity)
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

    public static void setCuriosWalletItem(LivingEntity entity, ItemStack item)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                handler.getStacks().setStackInSlot(0,item);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
    }

    public static boolean getCuriosWalletVisibility(LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getRenders().get(0);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
        return false;
    }

    public static boolean hasItem(LivingEntity entity, Predicate<ItemStack> check)
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

    @Nullable
    public static Item lookupItem(LivingEntity entity, Predicate<ItemStack> test)
    {
        AtomicReference<Item> result = new AtomicReference<>(null);
        hasItem(entity, stack -> {
            if(test.test(stack))
            {
                result.set(stack.getItem());
                return true;
            }
            return false;
        });
        return result.get();
    }

    @Nullable
    public static ItemStack getRandomItem(LivingEntity entity, Predicate<ItemStack> check)
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

    public static LCCurios.DropRule getWalletDropRules(LivingEntity entity)
    {
        ICurioStacksHandler stacks = getStacks(entity,WALLET_SLOT);
        if(stacks != null)
            return convertRule(stacks.getDropRule());
        return LCCurios.DropRule.DEFAULT;
    }

    private static LCCurios.DropRule convertRule(ICurio.DropRule rule)
    {
        return switch (rule) {
            case DESTROY ->  LCCurios.DropRule.DESTROY;
            case ALWAYS_KEEP -> LCCurios.DropRule.KEEP;
            case ALWAYS_DROP -> LCCurios.DropRule.DROP;
            default -> LCCurios.DropRule.DEFAULT;
        };
    }

    @Nullable
    public static ICapabilityProvider createWalletProvider(ItemStack stack)
    {
        try{
            return CurioItemCapability.createProvider(new ICurio()
            {
                @Override
                public ItemStack getStack() { return stack; }
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
                    return ICurio.super.canUnequip(context);
                }
                
                @Override
                public DropRule getDropRule(SlotContext context, DamageSource source, int lootingLevel, boolean recentlyHit)
                {
                    if(ModGameRules.safeGetCustomBool(context.entity().level(), ModGameRules.KEEP_WALLET, false))
                        return DropRule.ALWAYS_KEEP;
                    return ICurio.super.getDropRule(context,source,lootingLevel,recentlyHit);
                }
            });
        } catch(Throwable t) { return null; }
    }

}
package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@ApiStatus.Internal
public class LCCuriosInternal {

    public static final String WALLET_SLOT = "wallet";

    @Nullable
    private static ICuriosItemHandler getCurios(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).orElse(null);
    }

    @Nullable
    private static ICurioStacksHandler getStacks(LivingEntity entity, String slot) {
        ICuriosItemHandler handler = getCurios(entity);
        if(handler != null)
            return handler.getStacksHandler(slot).orElse(null);
        return null;
    }

    public static boolean hasWalletSlot(LivingEntity entity)
    {
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

    public static boolean getCuriosWalletVisiblity(LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getRenders().getFirst();
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

    public static void setup(IEventBus modBus)
    {
        modBus.addListener(LCCuriosInternal::registerCuriosItems);
    }

    public static LCCurios.DropRule getWalletDropRules(LivingEntity entity)
    {
        ICurioStacksHandler stacks = getStacks(entity,WALLET_SLOT);
        if(stacks != null)
            return convertRule(stacks.getDropRule());
        return LCCurios.DropRule.DEFAULT;
    }

    private static void registerCuriosItems(FMLCommonSetupEvent event)
    {
        BuiltInRegistries.ITEM.forEach(item -> {
            if(item instanceof WalletItem walletItem)
                CuriosApi.registerCurio(item,WalletCurio.INSTANCE);
        });
    }

    private static LCCurios.DropRule convertRule(ICurio.DropRule rule)
    {
        return switch (rule){
            case DESTROY -> LCCurios.DropRule.DESTROY;
            case ALWAYS_KEEP -> LCCurios.DropRule.KEEP;
            case ALWAYS_DROP -> LCCurios.DropRule.DROP;
            default -> LCCurios.DropRule.DEFAULT;
        };
    }

    private static class WalletCurio implements ICurioItem
    {
        private static final ICurioItem INSTANCE = new WalletCurio();

        @Override
        public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
            return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER.value(),1f,1f);
        }

        @Override
        public boolean canEquipFromUse(SlotContext context, ItemStack stack) { return false; }

        @Override
        public boolean canUnequip(SlotContext context, ItemStack stack) {
            if(context.entity() instanceof Player player && player.containerMenu instanceof WalletMenuBase menu)
                return !menu.isEquippedWallet();
            return ICurioItem.super.canUnequip(context,stack);
        }

        @Override
        public ICurio.DropRule getDropRule(SlotContext context, DamageSource source, boolean recentlyHit, ItemStack stack) {
            if(ModGameRules.safeGetCustomBool(context.entity().level(), ModGameRules.KEEP_WALLET, false))
                return ICurio.DropRule.ALWAYS_KEEP;
            return ICurioItem.super.getDropRule(context,source,recentlyHit,stack);
        }

    }
}

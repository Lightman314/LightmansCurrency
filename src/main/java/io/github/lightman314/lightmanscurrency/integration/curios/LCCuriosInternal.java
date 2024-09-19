package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ApiStatus.Internal
public class LCCuriosInternal {

    public static final String WALLET_SLOT = "wallet";

    @Nullable
    private static ICuriosItemHandler getCurios(@Nonnull LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).orElse(null);
    }

    @Nullable
    private static ICurioStacksHandler getStacks(@Nonnull LivingEntity entity, @Nonnull String slot) {
        ICuriosItemHandler handler = getCurios(entity);
        if(handler != null)
            return handler.getStacksHandler(slot).orElse(null);
        return null;
    }

    public static boolean hasWalletSlot(@Nonnull LivingEntity entity)
    {
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

    public static void setCuriosWalletItem(@Nonnull LivingEntity entity, @Nonnull ItemStack item)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                handler.getStacks().setStackInSlot(0,item);
        } catch (Throwable t) { LightmansCurrency.LogError("Error with Curios Integration!", t); }
    }

    public static boolean getCuriosWalletVisiblity(@Nonnull LivingEntity entity)
    {
        try {
            ICurioStacksHandler handler = getStacks(entity,WALLET_SLOT);
            if(handler != null && handler.getSlots() > 0)
                return handler.getRenders().getFirst();
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

    public static boolean hasPortableTerminal(@Nonnull LivingEntity entity) { return hasItem(entity,stack -> stack.getItem() instanceof PortableTerminalItem); }

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

    public static void setup(IEventBus modBus)
    {
        modBus.addListener(LCCuriosInternal::registerCuriosItems);
        CuriosApi.registerCurio(ModItems.WALLET_COPPER.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_IRON.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_GOLD.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_EMERALD.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_DIAMOND.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_NETHERITE.get(),WalletCurio.INSTANCE);
        CuriosApi.registerCurio(ModItems.WALLET_NETHER_STAR.get(),WalletCurio.INSTANCE);
    }

    private static void registerCuriosItems(@Nonnull FMLCommonSetupEvent event)
    {
        BuiltInRegistries.ITEM.forEach(item -> {
            if(item instanceof WalletItem walletItem)
                CuriosApi.registerCurio(item,WalletCurio.INSTANCE);
        });
    }

    private static class WalletCurio implements ICurioItem
    {
        private static final ICurioItem INSTANCE = new WalletCurio();

        @Nonnull
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
            return true;
        }

        @Nonnull
        @Override
        public ICurio.DropRule getDropRule(SlotContext context, DamageSource source, boolean recentlyHit, ItemStack stack) {
            if(ModGameRules.safeGetCustomBool(context.entity().level(), ModGameRules.KEEP_WALLET, false))
                return ICurio.DropRule.ALWAYS_KEEP;
            return ICurio.DropRule.DEFAULT;
        }
    }
}

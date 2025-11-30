package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LCCurios {

    public static final String WALLET_SLOT = "wallet";

    public static boolean isLoaded() { return ModList.get().isLoaded("curios"); }

    public static boolean hasWalletSlot(LivingEntity entity) { return isLoaded() && LCCuriosInternal.hasWalletSlot(entity); }

    public static ItemStack getCuriosWalletItem(LivingEntity entity) { return isLoaded() ? LCCuriosInternal.getCuriosWalletItem(entity) : ItemStack.EMPTY; }

    public static ItemStack getVisibleCuriosWalletItem(LivingEntity entity) { return isLoaded() ? LCCuriosInternal.getVisibleCuriosWalletItem(entity) : ItemStack.EMPTY; }

    public static void setCuriosWalletItem(LivingEntity entity, ItemStack stack) {
        if(isLoaded())
            LCCuriosInternal.setCuriosWalletItem(entity,stack);
    }

    public static boolean getCuriosWalletVisiblity(LivingEntity entity) { return isLoaded() && LCCuriosInternal.getCuriosWalletVisiblity(entity); }

    public static boolean hasItem(LivingEntity entity, Predicate<ItemStack> check) { return isLoaded() && LCCuriosInternal.hasItem(entity,check); }

    @Nullable
    public static Item lookupItem(LivingEntity entity, Predicate<ItemStack> check) { return !isLoaded() ? null : LCCuriosInternal.lookupItem(entity,check); }

    @Nullable
    public static Item lookupPortableTerminal(LivingEntity entity) { return lookupItem(entity,stack -> stack.getItem() instanceof PortableTerminalItem); }

    @Nullable
    public static Item lookupPortableATM(LivingEntity entity) { return lookupItem(entity,stack -> stack.getItem() instanceof PortableATMItem); }

    @Nullable
    public static ItemStack getRandomItem(LivingEntity entity, Predicate<ItemStack> check) { return isLoaded() ? LCCuriosInternal.getRandomItem(entity,check) : null; }

    public static DropRule getWalletDropRules(LivingEntity entity) {
        if(isLoaded())
            return LCCuriosInternal.getWalletDropRules(entity);
        return DropRule.DEFAULT;
    }

    public static void setup(IEventBus modBus)
    {
        if(isLoaded())
            LCCuriosInternal.setup(modBus);
    }

    //Non-curios enabled
    public enum DropRule {
        DEFAULT, KEEP, DROP, DESTROY;
        public boolean shouldKeep(LivingEntity entity) { return this == KEEP || (this == DEFAULT && entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)); }
        public boolean shouldDestroy() { return this == DESTROY; }
    }

}

package io.github.lightman314.lightmanscurrency.integration.curios;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;

import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LCCurios {

	public static boolean isLoaded() { return ModList.get().isLoaded("curios"); }

	public static boolean hasWalletSlot(LivingEntity entity) { return isLoaded() && LCCuriosInternal.hasWalletSlot(entity); }

	public static ItemStack getCuriosWalletItem(LivingEntity entity) { return isLoaded() ? LCCuriosInternal.getCuriosWalletItem(entity) : ItemStack.EMPTY; }

	public static ItemStack getVisibleCuriosWalletItem(LivingEntity entity) { return isLoaded() ? LCCuriosInternal.getVisibleCuriosWalletItem(entity) : ItemStack.EMPTY; }

	public static void setCuriosWalletItem(LivingEntity entity, ItemStack stack) {
		if(isLoaded())
			LCCuriosInternal.setCuriosWalletItem(entity,stack);
	}
	
	public static boolean getCuriosWalletVisibility(LivingEntity entity) { return isLoaded() && LCCuriosInternal.getCuriosWalletVisibility(entity); }

	public static boolean hasItem(LivingEntity entity, Predicate<ItemStack> check) { return isLoaded() && LCCuriosInternal.hasItem(entity,check); }

    @Nullable
    public static Item lookupItem(LivingEntity entity, Predicate<ItemStack> check) { return !isLoaded() ? null : LCCuriosInternal.lookupItem(entity,check); }

    @Nullable
    public static Item lookupPortableTerminal(LivingEntity entity) { return lookupItem(entity, stack -> stack.getItem() instanceof PortableTerminalItem); }

    @Nullable
    public static Item lookupPortableATM(LivingEntity entity) { return lookupItem(entity,stack -> stack.getItem() instanceof PortableATMItem); }
	@Nullable
	public static ItemStack getRandomItem(LivingEntity entity, Predicate<ItemStack> check) { return isLoaded() ? LCCuriosInternal.getRandomItem(entity,check) : null; }

	@Nullable
	public static ICapabilityProvider createWalletProvider(ItemStack stack)
	{
		if(isLoaded())
			return LCCuriosInternal.createWalletProvider(stack);
		return null;
	}
	
}

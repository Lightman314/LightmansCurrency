package io.github.lightman314.lightmanscurrency.integration.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;

import java.util.function.Predicate;

public class LCCurios {

	public static final String WALLET_SLOT = "wallet";

	public static boolean isLoaded() { return ModList.get().isLoaded("curios"); }

	public static boolean hasWalletSlot(@Nonnull LivingEntity entity) { return isLoaded() && LCCuriosInternal.hasWalletSlot(entity); }

	@Nonnull
	public static ItemStack getCuriosWalletItem(@Nonnull LivingEntity entity) { return isLoaded() ? LCCuriosInternal.getCuriosWalletItem(entity) : ItemStack.EMPTY; }
	
	public static void setCuriosWalletItem(@Nonnull LivingEntity entity, @Nonnull ItemStack stack) {
		if(isLoaded())
			LCCuriosInternal.setCuriosWalletItem(entity,stack);
	}
	
	public static boolean getCuriosWalletVisibility(@Nonnull LivingEntity entity) { return isLoaded() && LCCuriosInternal.getCuriosWalletVisibility(entity); }

	public static boolean hasItem(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> check) { return isLoaded() && LCCuriosInternal.hasItem(entity,check); }

	public static boolean hasPortableTerminal(@Nonnull LivingEntity entity) { return isLoaded() && LCCuriosInternal.hasPortableTerminal(entity); }

	public static boolean hasPortableATM(@Nonnull LivingEntity entity) { return isLoaded() && LCCuriosInternal.hasPortableATM(entity); }

	@Nullable
	public static ItemStack getRandomItem(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> check) { return isLoaded() ? LCCuriosInternal.getRandomItem(entity,check) : null; }

	@Nullable
	public static ICapabilityProvider createWalletProvider(ItemStack stack)
	{
		if(isLoaded())
			return LCCuriosInternal.createWalletProvider(stack);
		return null;
	}
	
}

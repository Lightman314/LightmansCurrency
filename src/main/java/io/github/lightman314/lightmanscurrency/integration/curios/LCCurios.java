package io.github.lightman314.lightmanscurrency.integration.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class LCCurios {


	public static final String WALLET_SLOT = "wallet";

    //TODO update to check modlist once Curios compat can be updated & re-enabled
    public static boolean isCuriosLoaded() { return false; }
    public static boolean hasCuriosWalletSlot(@Nonnull LivingEntity entity) { return isCuriosLoaded() && LCCuriosInternal.hasCuriosWalletSlot(entity); }

    @Nonnull
	public static ItemStack getCuriosWalletContents(@Nonnull LivingEntity entity) {
		if(hasCuriosWalletSlot(entity))
			return LCCuriosInternal.getCuriosWalletContents(entity);
		return ItemStack.EMPTY;
	}

	public static boolean setCuriosWalletContents(@Nonnull LivingEntity entity, @Nonnull ItemStack wallet) {
		if(hasCuriosWalletSlot(entity))
			return LCCuriosInternal.setCuriosWalletContents(entity,wallet);
		return false;
	}

	public static boolean getCuriosWalletVisibility(@Nonnull LivingEntity entity) {
		if(hasCuriosWalletSlot(entity))
			return LCCuriosInternal.getCuriosWalletVisibility(entity);
		return false;
	}

	public static boolean hasInCuriosSlot(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> predicate)
	{
		if(hasCuriosWalletSlot(entity))
			return LCCuriosInternal.hasInCuriosSlot(entity,predicate);
		return false;
	}

	@Nullable
	public static ItemStack getRandomFromCuriosSlot(@Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> predicate)
	{
		if(hasCuriosWalletSlot(entity))
			return LCCuriosInternal.getRandomFromCuriosSlot(entity,predicate);
		return null;
	}

	public static void registerWalletCapability(@Nonnull RegisterCapabilitiesEvent event)
	{
		if(isCuriosLoaded())
			LCCuriosInternal.registerWalletCapability(event);
	}

}
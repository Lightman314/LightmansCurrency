package io.github.lightman314.lightmanscurrency.integration.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.items.PortableTerminalItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

import java.util.Map;

public class LCCurios {

	public static final String WALLET_SLOT = "wallet";

	private static ICuriosItemHandler lazyGetCuriosHelper(LivingEntity entity) {
		LazyOptional<ICuriosItemHandler> optional = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
		return optional.isPresent() ? optional.orElseGet(() -> { throw new RuntimeException("Unexpected error occurred!"); }) : null;
	}
	public static boolean hasWalletSlot(LivingEntity entity) {
		if(entity == null)
			return false;
		try {
			ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
			if(curiosHelper != null)
			{
				ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
				return stacksHandler != null && stacksHandler.getSlots() > 0;
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error checking curios wallet slot validity.", t); }
		return false;
	}
	
	public static ItemStack getCuriosWalletContents(LivingEntity entity) {
		try {
			ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
			if(curiosHelper != null)
			{
				ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
				if(stacksHandler != null && stacksHandler.getSlots() > 0)
					return stacksHandler.getStacks().getStackInSlot(0);
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error getting wallet from curios wallet slot.", t); }
		return ItemStack.EMPTY;
	}
	
	public static void setCuriosWalletContents(LivingEntity entity, ItemStack wallet) {
		try {
			ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
			if(curiosHelper != null)
			{
				ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
				if(stacksHandler != null && stacksHandler.getSlots() > 0)
					stacksHandler.getStacks().setStackInSlot(0, wallet);
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error placing wallet into the curios wallet slot.", t); }
	}
	
	public static boolean getCuriosWalletVisibility(LivingEntity entity) {
		try {
			ICuriosItemHandler curiosHelper = lazyGetCuriosHelper(entity);
			if(curiosHelper != null)
			{
				ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
				if(stacksHandler != null && stacksHandler.getSlots() > 0)
					return stacksHandler.getRenders().get(0);
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error getting wallet slot visibility from curios.", t); }
		return false;
	}

	public static boolean hasPortableTerminal(LivingEntity entity) {
		try{
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
							if(sh.getStackInSlot(i).getItem() instanceof PortableTerminalItem)
								return true;
						}
					}
				}
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error checking for Portable Terminal from curios.", t); }
		return false;
	}

	public static boolean hasPortableATM(LivingEntity entity) {
		try{
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
							if(sh.getStackInSlot(i).getItem() instanceof PortableATMItem)
								return true;
						}
					}
				}
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error checking for Portable Terminal from curios.", t); }
		return false;
	}

	@Nullable
	public static ItemStack getMoneyMendingItem(LivingEntity entity)
	{
		try {
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
							if(EnchantmentHelper.getEnchantments(item).containsKey(ModEnchantments.MONEY_MENDING.get()))
								return item;
						}
					}
				}
			}
		} catch (Throwable t) { LightmansCurrency.LogError("Error checking for Money Mending item from curios.", t); }
		return null;
	}
	
	public static ICapabilityProvider createWalletProvider(ItemStack stack)
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
	}
	
}

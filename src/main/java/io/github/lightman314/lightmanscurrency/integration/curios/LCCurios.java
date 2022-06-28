package io.github.lightman314.lightmanscurrency.integration.curios;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.menus.WalletMenu;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class LCCurios {
	
	public static final String WALLET_SLOT = "wallet";
	
	public static boolean hasWalletSlot(LivingEntity entity) {
		if(entity == null)
			return false;
		try {
			ICuriosItemHandler curiosHelper = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
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
			ICuriosItemHandler curiosHelper = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
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
			ICuriosItemHandler curiosHelper = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
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
			ICuriosItemHandler curiosHelper = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
			if(curiosHelper != null)
			{
				ICurioStacksHandler stacksHandler = curiosHelper.getStacksHandler(WALLET_SLOT).orElse(null);
				if(stacksHandler != null && stacksHandler.getSlots() > 0)
					return stacksHandler.getRenders().get(0);
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error getting wallet slot visibility from curios.", t); }
		return false;
	}
	
	public static ICapabilityProvider createWalletProvider(ItemStack stack)
	{
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
			public boolean canEquip(SlotContext context) {
				return context.entity() instanceof Player;
			}
			
			@Override
			public boolean canUnequip(SlotContext context) {
				if(context.entity() instanceof Player)
				{
					Player player = (Player)context.entity();
					//Prevent unequipping if the wallet is open in the menu.
					if(player.containerMenu instanceof WalletMenu)
						return ((WalletMenu)player.containerMenu).getWalletIndex() >= 0;
				}
				return true;
			}
			
			@Nonnull
			@Override
			public DropRule getDropRule(SlotContext context, DamageSource source, int lootingLevel, boolean recentlyHit)
			{
				GameRules.BooleanValue keepWallet = ModGameRules.getCustomValue(context.entity().level, ModGameRules.KEEP_WALLET);
				if((keepWallet != null && keepWallet.get()))
					return DropRule.ALWAYS_KEEP;
				else
					return DropRule.DEFAULT;
			}
			
		});
	}
	
}

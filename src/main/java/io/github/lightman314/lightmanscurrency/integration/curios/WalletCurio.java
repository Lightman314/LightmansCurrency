package io.github.lightman314.lightmanscurrency.integration.curios;

import io.github.lightman314.lightmanscurrency.core.LCGameRules;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.AbstractWalletMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

public class WalletCurio implements ICurioItem {

    public static final ICurioItem INSTANCE = new WalletCurio();

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext context, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER.value(),1f,1f);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) { return false; }

    @Override
    public boolean canUnequip(SlotContext context, ItemStack stack) {
        if(context.entity() instanceof Player player && player.containerMenu instanceof AbstractWalletMenu menu)
            return !menu.isEquippedWallet();
        return ICurioItem.super.canUnequip(context,stack);
    }

    @Nonnull
    @Override
    public DropRule getDropRule(SlotContext context, DamageSource source, boolean recentlyHit, ItemStack stack) {
        //Always keep if the keepWallet game rule is enabled
        if(context.entity().level() instanceof ServerLevel sl && sl.getServer().getGameRules().get(LCGameRules.KEEP_WALLET.get()))
            return DropRule.ALWAYS_KEEP;
        return ICurioItem.super.getDropRule(context,source,recentlyHit,stack);
    }

}
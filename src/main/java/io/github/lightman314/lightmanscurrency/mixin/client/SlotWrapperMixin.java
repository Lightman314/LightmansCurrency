package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotWrapper.class)
public abstract class SlotWrapperMixin {

    @Unique
    private SlotWrapper lightmanscurrency$self() { return (SlotWrapper)(Object)this; }

    @Inject(at = @At("TAIL"),method = "<init>")
    public void init(Slot target, int index, int x, int y, CallbackInfo ci) {
        if(target instanceof WalletSlot)
        {
            ScreenPosition pos = LCConfig.CLIENT.walletSlotCreative.get().offset(1,1);
            lightmanscurrency$self().x = pos.x;
            lightmanscurrency$self().y = pos.y;
        }
    }

}
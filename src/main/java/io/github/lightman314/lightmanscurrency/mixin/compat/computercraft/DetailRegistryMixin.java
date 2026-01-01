package io.github.lightman314.lightmanscurrency.mixin.compat.computercraft;

import dan200.computercraft.impl.detail.DetailRegistryImpl;
import io.github.lightman314.lightmanscurrency.integration.computercraft.detail_providers.AncientCoinDetailProvider;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = DetailRegistryImpl.class,remap = false)
public class DetailRegistryMixin {

    @Inject(method = "getBasicDetails",at = @At("RETURN"))
    public void getBasicDetails(Object object, CallbackInfoReturnable<Map<String,Object>> cir)
    {
        if(object instanceof ItemStack stack)
        {
            //Add Ancient Coin Details to even "basic" detail data maps
            Map<String,Object> map = cir.getReturnValue();
            AncientCoinDetailProvider.INSTANCE.provideDetails(map,stack);
        }
    }

}

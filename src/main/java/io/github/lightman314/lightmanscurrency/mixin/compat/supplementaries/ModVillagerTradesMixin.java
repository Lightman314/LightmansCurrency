package io.github.lightman314.lightmanscurrency.mixin.compat.supplementaries;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import io.github.lightman314.lightmanscurrency.integration.supplementaries.LCSupplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.trades.ModVillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ModVillagerTrades.class)
public class ModVillagerTradesMixin {

    @Inject(at = @At("RETURN"), method = "getRedMerchantTrades", cancellable = true, remap = false)
    private static void getRedMerchantTrades(CallbackInfoReturnable<VillagerTrades.ItemListing[]> callbackInfo) {
        if(LCSupplementaries.triggerMixin && LCConfig.COMMON.changeModdedTrades.get())
        {
            List<VillagerTrades.ItemListing> list = new ArrayList<>(List.of(callbackInfo.getReturnValue()));
            VillagerTradeManager.replaceExistingTrades(LCSupplementaries.RED_MERCHANT_ID, list);
            callbackInfo.setReturnValue(list.toArray(VillagerTrades.ItemListing[]::new));
        }
    }

}

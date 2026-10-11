package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.features.villagers.VillagerTradeHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerTrade.class)
public class VillagerTradeMixin {

    @WrapMethod(method = "getOffer")
    private MerchantOffer modifyOffer(LootContext lootContext,Operation<MerchantOffer> operation) {
        MerchantOffer original = operation.call(lootContext);
        if(original == null)
            return null;
        try {
            return VillagerTradeHelper.modifyOffer(lootContext.getParameter(LootContextParams.THIS_ENTITY),original);
        } catch (Exception ignored) {}
        return original;
    }

}
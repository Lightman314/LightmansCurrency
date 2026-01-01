package io.github.lightman314.lightmanscurrency.integration.computercraft.detail_providers;

import dan200.computercraft.api.detail.DetailProvider;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class AncientCoinDetailProviders implements DetailProvider<ItemStack> {

    public static final DetailProvider<ItemStack> INSTANCE = new AncientCoinDetailProviders();
    private AncientCoinDetailProviders() {}
    @Override
    public void provideDetails(Map<? super String, Object> map, ItemStack stack) {
        if(stack.getItem() instanceof AncientCoinItem item)
            map.put("AncientCoinType",AncientCoinItem.getAncientCoinType(stack));
    }
}

package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyHoldingItemResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import net.minecraft.world.item.ItemStack;

public class MoneyItemStorage extends NormalItemStorage implements MoneyHoldingItemResourceHandler {

    public final boolean allowCapabilities;
    public MoneyItemStorage(int size) { this(size,true); }
    public MoneyItemStorage(int size,boolean allowCapabilities) {
        super(size);
        this.allowCapabilities = allowCapabilities;
    }

    @Override
    public boolean isValid(int index,ItemStack stack) { return MoneyValueHelper.isAllowedInMoneySlot(stack,this.allowCapabilities); }

    @Override
    public boolean shouldWrapCapabilities() { return this.allowCapabilities; }

}

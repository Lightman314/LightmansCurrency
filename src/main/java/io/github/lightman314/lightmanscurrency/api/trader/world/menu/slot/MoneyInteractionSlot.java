package io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCCapabilities;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.NormalCustomerTab;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class MoneyInteractionSlot extends InteractionSlotData {

    public static final InteractionSlotData INSTANCE = new MoneyInteractionSlot();

    private static final Identifier TYPE = LCApi.id("money");

    protected MoneyInteractionSlot() {}

    @Override
    public Identifier getType() { return TYPE; }

    @Override
    public boolean allowItemInSlot(ItemStack item, SidedItemAccess access) { return access.getSidedCapability(LCCapabilities.Money.ITEM) != null; }

    @Override
    public void wrapResource(TradeContext.Builder builder, SidedItemAccess access) {
        MoneyResourceHandler resource = access.getSidedCapability(LCCapabilities.Money.ITEM);
        if(resource != null)
            builder.withResource(BuiltInResourceTypes.MONEY,SortableMoneyResourceHandler.wrapHandler(resource,NormalCustomerTab.TOOLTIP_MONEY_SOURCE_SLOTS_CAPABILITY.get(),-200,-200));
    }

    @Nullable
    @Override
    public Identifier getNoItemIcon() { return LCApi.id("container/slot/money"); }

}

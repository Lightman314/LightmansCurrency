package io.github.lightman314.lightmanscurrency.api.trader.world.menu.slot;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.access.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class InteractionSlotData {

    public abstract Identifier getType();
    public abstract boolean allowItemInSlot(ItemStack item,SidedItemAccess access);
    @Nullable
    public Identifier getNoItemIcon() { return null; }

    public abstract void wrapResource(TradeContext.Builder builder,SidedItemAccess access);

    public static boolean allowItemInSlot(Collection<InteractionSlotData> slots,ItemStack item,ISidedContext context) {
        SidedItemAccess access = SidedItemAccess.forStack(item,context);
        return slots.stream().anyMatch(d -> d.allowItemInSlot(item,access));
    }

}
package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

//Custom ticket item for the express purpose of showing the durability bar for their limited uses
public class TicketDurabilityItem extends Item {

    public TicketDurabilityItem(Properties properties) {
        super(properties.component(LCDataComponents.TICKET_USES,TicketUses.DEFAULT));
    }

    public boolean isBarVisible(ItemStack stack) {
        return TicketUses.get(stack).isFinite();
    }

    public int getBarWidth(ItemStack stack) {
        TicketUses uses = TicketUses.get(stack);
        return Mth.clamp(Math.round(uses.uses() * 13f / uses.maxUses()), 0, 13);
    }

    public int getBarColor(ItemStack stack) {
        TicketUses uses = TicketUses.get(stack);
        int maxUses = uses.maxUses();
        float healthPercentage = Math.max(0f,(float)uses.uses()/maxUses);
        return Mth.hsvToRgb(healthPercentage / 3f, 1f, 1f);
    }

}
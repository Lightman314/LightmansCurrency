package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ResourceDisplaySlot extends EasyResourceSlot {

    public ResourceDisplaySlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int handlerSlot, int x, int y) { super(handler, slotModifier, handlerSlot, x, y); }

    @Override
    public boolean mayPlace(ItemStack stack) { return false; }
    @Override
    public boolean mayPickup(Player player) { return false; }
}

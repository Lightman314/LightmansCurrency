package io.github.lightman314.lightmanscurrency.api.icon.events;

import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Event called by the {@link io.github.lightman314.lightmanscurrency.api.icon.ItemToIconCycler ItemToIconCycler} when someone attempts
 * to get the potential icons for an Item Stack.<br>
 * By default, it automatically adds an {@link ItemIcon} for the given item, but listeners of this event may register more
 */
public final class IconsForItemEvent extends Event {

    private final ItemStack stack;
    public ItemStack getStack() { return this.stack.copy(); }
    private final List<IconData> result;
    @ApiStatus.Internal
    public IconsForItemEvent(ItemStack item,List<IconData> result) {
        this.stack = item.copyWithCount(1);
        this.result = result;
        //Hardcode the normal item as the default result
        this.result.add(ItemIcon.of(this.stack));
    }

    public void registerIcon(IconData icon) {
        this.result.add(icon);
    }

}
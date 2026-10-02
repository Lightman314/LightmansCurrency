package io.github.lightman314.lightmanscurrency.api.icon;

import io.github.lightman314.lightmanscurrency.api.icon.events.IconsForItemEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ItemToIconCycler {

    private ItemToIconCycler() {}

    @Nullable
    public static IconData getNextIconInCycle(ItemStack stack,Optional<IconData> oldIcon) {
        List<IconData> icons = getIconCycle(stack);
        if(icons.isEmpty())
            return null;
        if(icons.size() == 1 || oldIcon.isEmpty())
            return icons.getFirst();
        IconData oi = oldIcon.get();
        for(int i = 0;i < icons.size();++i) {
            IconData icon = icons.get(i);
            if(icons.equals(oi) && i < icons.size() - 1)
                return icons.get(i + 1);
        }
        //Return the first icon in the cycle if the previous icon was the last in the loop OR it wasn't in the loop at all
        return icons.getFirst();
    }

    public static List<IconData> getIconCycle(ItemStack stack) {
        if(stack.isEmpty())
            return List.of();
        List<IconData> list = new ArrayList<>();
        IconsForItemEvent event = new IconsForItemEvent(stack,list);
        NeoForge.EVENT_BUS.post(event);
        return List.copyOf(list);
    }

}
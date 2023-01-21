package io.github.lightman314.lightmanscurrency.menus.slots.easy;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class EasyMultiBGSlot extends EasySlot {


    public EasyMultiBGSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }


    protected abstract List<Pair<ResourceLocation,ResourceLocation>> getPossibleNoItemIcons();

    @Nullable
    @Override
    public final Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        Minecraft mc = Minecraft.getInstance();
        //Use the game time as a timer. Divide by 20 ticks to make the timer change the index once a second.
        int timer = (int)(mc.level.getGameTime() / 20);
        List<Pair<ResourceLocation,ResourceLocation>> bgs = this.getPossibleNoItemIcons();
        if(bgs == null || bgs.size() == 0)
            return null;
        return bgs.get(timer % bgs.size());
    }
}

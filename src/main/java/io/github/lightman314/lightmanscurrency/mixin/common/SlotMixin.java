package io.github.lightman314.lightmanscurrency.mixin.common;

import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotMixin
{
    @Mutable
    @Accessor(value = "xPos")
    void setXPos(int x);

    @Mutable
    @Accessor(value = "yPos")
    void setYPos(int y);
}

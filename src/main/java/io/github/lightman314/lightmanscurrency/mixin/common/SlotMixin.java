package io.github.lightman314.lightmanscurrency.mixin.common;

//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Mutable;
//import org.spongepowered.asm.mixin.gen.Accessor;

//import net.minecraft.world.inventory.Slot;

//@Mixin(Slot.class)
public interface SlotMixin
{
    //@Mutable
    //@Accessor(value = "x")
    void setXPos(int x);

    //@Mutable
    //@Accessor(value = "y")
    void setYPos(int y);
}

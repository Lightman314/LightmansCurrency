package io.github.lightman314.lightmanscurrency.common.menus.variant;

import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemVariantSlot extends EasySlot {

    public final Container container;
    public ItemVariantSlot(int x, int y) { this(new SimpleContainer(1),x,y); }
    private ItemVariantSlot(Container container,int x, int y) { super(container, 0, x, y); this.container = container; }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return VariantProvider.getVariantItem(stack.getItem()) != null; }

}

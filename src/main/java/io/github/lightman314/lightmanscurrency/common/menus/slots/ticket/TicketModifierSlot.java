package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasyMultiBGSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TicketModifierSlot extends EasyMultiBGSlot {

    public static final ResourceLocation EMPTY_DYE_SLOT =  new ResourceLocation(LightmansCurrency.MODID, "item/empty_dye_slot");

    private final TicketStationMenu menu;

    public TicketModifierSlot(TicketStationMenu menu, Container inventory, int index, int x, int y) { super(inventory, index, x, y); this.menu = menu; }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return this.menu.getAllRecipes().stream().anyMatch(r -> r.validModifier(stack)); }

    @Override
    protected List<Pair<ResourceLocation, ResourceLocation>> getPossibleNoItemIcons() {
        return ImmutableList.of(Pair.of(InventoryMenu.BLOCK_ATLAS,TicketSlot.EMPTY_TICKET_SLOT), Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_DYE_SLOT));
    }

    @Nullable
    public static Color getColorFromDye(ItemStack stack) {
        if(stack.isEmpty())
            return null;
        for(Color color : Color.values())
        {
            if(InventoryUtil.ItemHasTag(stack, color.dyeTag))
                return color;
        }
        return null;
    }

}

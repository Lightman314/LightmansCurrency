package io.github.lightman314.lightmanscurrency.common.menus.slots.ticket;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasyMultiBGSlot;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TicketModifierSlot extends EasyMultiBGSlot {

    public static final ResourceLocation EMPTY_DYE_SLOT =  new ResourceLocation(LightmansCurrency.MODID, "item/empty_dye_slot");

    public TicketModifierSlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.getItem() == ModItems.TICKET_MASTER.get() || InventoryUtil.ItemHasTag(stack, Tags.Items.DYES);
    }

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
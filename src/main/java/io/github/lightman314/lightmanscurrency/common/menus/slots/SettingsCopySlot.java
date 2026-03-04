package io.github.lightman314.lightmanscurrency.common.menus.slots;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.BookRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SettingsCopySlot extends EasySlot {

    public SettingsCopySlot(IItemHandler container,int index,int x,int y) { super(container, index, x, y); }

    @Override
    public boolean mayPlace(ItemStack stack) { return super.mayPlace(stack) && stack.is(LCTags.Items.SETTINGS_READ_OR_WRITABLE); }
    @Override
    public int getMaxStackSize() { return 1; }
    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, BookRestriction.EMPTY_BOOK_SLOT); }

}

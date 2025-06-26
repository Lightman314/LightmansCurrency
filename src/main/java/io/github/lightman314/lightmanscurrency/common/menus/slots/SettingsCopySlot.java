package io.github.lightman314.lightmanscurrency.common.menus.slots;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.BookRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SettingsCopySlot extends EasySlot {

    public SettingsCopySlot(Container container, int index, int x, int y) { super(container, index, x, y); }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return super.mayPlace(stack) && InventoryUtil.ItemHasTag(stack, LCTags.Items.SETTINGS_READ_OR_WRITABLE); }
    @Override
    public int getMaxStackSize() { return 1; }
    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, BookRestriction.EMPTY_BOOK_SLOT); }

}

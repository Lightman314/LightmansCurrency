package io.github.lightman314.lightmanscurrency.common.menus.containers;

import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Container that changes its size depending on whether
 */
public class NonEmptyContainer implements Container {

    private List<ItemStack> items;

    public NonEmptyContainer(@Nonnull List<ItemStack> items) { this.items = clean(items); }
    public NonEmptyContainer(@Nonnull Container other) { this(InventoryUtil.buildList(other)); }

    public void save(@Nonnull CompoundTag tag, @Nonnull String key) { InventoryUtil.saveAllItems(key,tag,this); }
    public static NonEmptyContainer load(@Nonnull CompoundTag tag, @Nonnull String key) { return new NonEmptyContainer(InventoryUtil.loadAllItems(key,tag,tag.getList(key, Tag.TAG_COMPOUND).size())); }

    private static List<ItemStack> clean(@Nonnull List<ItemStack> list) { return new ArrayList<>(list.stream().filter(s -> !s.isEmpty()).toList()); }
    private void checkContents() { this.items = clean(this.items); }

    @Override
    public int getContainerSize() { return this.items.size(); }

    @Override
    public boolean isEmpty() { return this.items.isEmpty() || this.items.stream().allMatch(ItemStack::isEmpty); }

    private boolean validSlot(int slot) { return slot >= 0 && slot < this.items.size(); }

    @Nonnull
    @Override
    public ItemStack getItem(int slot) {
        if(!this.validSlot(slot))
            return ItemStack.EMPTY;
        return this.items.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack item = this.getItem(slot);
        ItemStack result = item.split(count);
        if(item.isEmpty() && this.validSlot(slot))
            this.items.remove(slot);
        return result;
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack item = this.getItem(slot);
        if(this.validSlot(slot))
            this.items.remove(slot);
        return item;
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack item) {
        if(this.validSlot(slot))
            this.items.set(slot,item);
        else
            this.items.add(item);
    }

    @Override
    public void setChanged() { this.checkContents(); }

    @Override
    public boolean stillValid(@Nonnull Player player) { return true; }

    @Override
    public void clearContent() { this.items = new ArrayList<>(); }

}
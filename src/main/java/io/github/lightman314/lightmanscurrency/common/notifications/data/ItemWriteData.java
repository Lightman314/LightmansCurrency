package io.github.lightman314.lightmanscurrency.common.notifications.data;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemWriteData
{
    public final boolean isEmpty;
    public final Component itemName;
    public final int count;

    public ItemWriteData(ItemStack item) { this(item, ""); }

    public ItemWriteData(ItemStack item, String customName) {
        this.isEmpty = item.isEmpty();
        if(this.isEmpty)
        {
            this.itemName = Component.empty();
            this.count = 0;
            return;
        }
        if(customName.isEmpty())
            itemName = item.getHoverName();
        else
            this.itemName = EasyText.literal(customName);
        this.count = item.getCount();
    }

    public ItemWriteData(CompoundTag compound) {
        this.isEmpty = compound.contains("Empty");
        if(this.isEmpty)
        {
            this.itemName = EasyText.empty();
            this.count = 0;
            return;
        }
        this.itemName = Component.Serializer.fromJson(compound.getString("Name"));
        this.count = compound.getInt("Count");
    }

    public CompoundTag save() {
        CompoundTag compound = new CompoundTag();
        if(this.isEmpty)
        {
            compound.putBoolean("Empty", true);
            return compound;
        }
        compound.putString("Name", Component.Serializer.toJson(this.itemName));
        compound.putInt("Count", this.count);
        return compound;
    }

    public Component format() { return Component.translatable("log.shoplog.item.itemformat", this.count, this.itemName); }

    public Component formatWith(Component other) { return Component.translatable("log.shoplog.and", this.format(), other); }

    public Component formatWith(ItemWriteData other) { return Component.translatable("log.shoplog.and", this.format(), other.format()); }

    public static Component getItemNames(ItemWriteData item1, ItemWriteData item2) {
        if(item1.isEmpty && item2.isEmpty)
            return Component.literal("ERROR");
        else if(item2.isEmpty)
            return item1.format();
        else if(item1.isEmpty)
            return item2.format();
        else
            return item1.formatWith(item2);
    }

    public static Component getItemNames(List<ItemWriteData> items) {
        Component result = null;
        for (ItemWriteData item : items) {
            if (result != null)
                result = item.formatWith(result);
            else
                result = item.format();
        }
        return result == null ? Component.literal("ERROR") : result;
    }

}

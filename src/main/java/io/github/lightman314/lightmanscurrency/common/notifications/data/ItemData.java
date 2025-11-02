package io.github.lightman314.lightmanscurrency.common.notifications.data;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemData
{
    private final ItemStack stack;
    private final MutableComponent deprecatedName;
    private final String customName;
    public ItemData(ItemStack stack) { this(stack, null, ""); }
    public ItemData(ItemStack stack, String customName) { this(stack, null, customName); }
    public ItemData(ItemStack stack, @Nullable MutableComponent deprecatedName, String customName)
    {
        this.stack = stack;
        this.deprecatedName = deprecatedName;
        this.customName = customName;
    }
    private ItemData(MutableComponent deprecatedName, int count)
    {
        this.stack = new ItemStack(Items.BARRIER,count);
        this.deprecatedName = deprecatedName;
        this.customName = "";
    }

    public MutableComponent getName()
    {
        if(this.deprecatedName != null)
            return this.deprecatedName;
        return this.customName.isEmpty() ? EasyText.empty().append(this.stack.getHoverName()) : EasyText.literal(this.customName);
    }

    public MutableComponent format() { return LCText.NOTIFICATION_ITEM_FORMAT.get(this.stack.getCount(), this.getName()); }
    public MutableComponent formatWith(ItemData other) { return LCText.GUI_AND.get(this.format(), other.format()); }
    public MutableComponent formatWith(MutableComponent other) { return LCText.GUI_AND.get(this.format(), other); }

    public static MutableComponent format(ItemData d1, ItemData d2)
    {
        if(d1.stack.isEmpty() && d2.stack.isEmpty())
            return EasyText.literal("ERROR");
        if(d1.stack.isEmpty())
            return d2.format();
        if(d2.stack.isEmpty())
            return d1.format();
        return d1.formatWith(d2);
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("Stack", this.stack.save(new CompoundTag()));
        tag.putString("CustomName", this.customName);
        if(this.deprecatedName != null)
            tag.putString("DeprecatedName", Component.Serializer.toJson(this.deprecatedName));
        return tag;
    }

    public static ItemData load(CompoundTag tag)
    {
        if(tag.contains("Empty"))
            return new ItemData(ItemStack.EMPTY,"");
        if(tag.contains("Name"))
        {
            MutableComponent deprecatedName = Component.Serializer.fromJson(tag.getString("Name"));
            int count = tag.getInt("Count");
            return new ItemData(deprecatedName,count);
        }
        ItemStack stack = ItemStack.of(tag.getCompound("Stack"));
        String customName = tag.getString("CustomName");
        MutableComponent deprecatedName = null;
        if(tag.contains("DeprecatedName"))
            deprecatedName = Component.Serializer.fromJson(tag.getString("DeprecatedName"));
        return new ItemData(stack,deprecatedName,customName);
    }

    public boolean matches(ItemData other)
    {
        //Cannot compare text components server-side apparently...
        if(this.deprecatedName != null || other.deprecatedName != null)
            return false;
        return this.customName.equals(other.customName) && InventoryUtil.ItemsFullyMatch(this.stack, other.stack);
    }

    public static Component getItemNames(List<ItemData> items) {
        MutableComponent result = null;
        for (ItemData item : items) {
            if (result != null)
                result = item.formatWith(result);
            else
                result = item.format();
        }
        return result == null ? Component.literal("ERROR") : result;
    }

}

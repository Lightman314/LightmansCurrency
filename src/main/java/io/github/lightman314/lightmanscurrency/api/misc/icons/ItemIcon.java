package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemIcon extends IconData
{

    public static final Type TYPE = new Type(VersionUtil.lcResource("item"),ItemIcon::load,ItemIcon::parse);

    private final ItemStack iconStack;
    private final String countTextOverride;
    private ItemIcon(ItemStack iconStack, @Nullable String countTextOverride) { super(TYPE); this.iconStack = iconStack; this.countTextOverride = countTextOverride; }

    public static IconData ofItem(Supplier<? extends ItemLike> item) { return ofItem(item.get()); }
    public static IconData ofItem(Supplier<? extends ItemLike> item, @Nullable String countTextOverride) { return ofItem(item.get(), countTextOverride); }
    public static IconData ofItem(ItemLike item) { return ofItem(new ItemStack(item)); }
    public static IconData ofItem(ItemLike item, @Nullable String countTextOverride) { return ofItem(new ItemStack(item),countTextOverride); }
    public static IconData ofItem(ItemStack item) { return new ItemIcon(item,null); }
    public static IconData ofItem(ItemStack item, @Nullable String countTextOverride) { return new ItemIcon(item,countTextOverride); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(EasyGuiGraphics gui, int x, int y) { gui.renderItem(this.iconStack, x, y, this.countTextOverride); }

    @Override
    protected void saveAdditional(ReadWriteContext<CompoundTag> context) {
        context.data.put("Item", InventoryUtil.saveItemNoLimits(this.iconStack,context));
        if(this.countTextOverride != null)
            context.data.putString("Text", this.countTextOverride);
    }

    @Override
    protected void writeAdditional(ReadWriteContext<JsonObject> context) {
        context.data.add("Item", FileUtil.convertItemStack(this.iconStack,context.lookup));
        if(this.countTextOverride != null)
            context.data.addProperty("Text",this.countTextOverride);
    }

    private static IconData load(ReadWriteContext<CompoundTag> context)
    {
        ItemStack stack = InventoryUtil.loadItemNoLimits(context.getEntry("Item"));
        String countText = null;
        CompoundTag tag = context.data;
        if(tag.contains("Text"))
            countText = tag.getString("Text");
        return new ItemIcon(stack,countText);
    }

    private static IconData parse(ReadWriteContext<JsonObject> context)
    {
        ItemStack stack = FileUtil.parseItemStack(GsonHelper.getAsJsonObject(context.data,"Item"),context.lookup);
        String countText = GsonHelper.getAsString(context.data,"Text",null);
        return new ItemIcon(stack,countText);
    }

}

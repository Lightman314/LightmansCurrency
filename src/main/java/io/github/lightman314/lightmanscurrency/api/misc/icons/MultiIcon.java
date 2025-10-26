package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiIcon extends IconData
{

    public static final Type TYPE = new Type(VersionUtil.lcResource("multi_icon"),MultiIcon::load,MultiIcon::parse);

    private final List<IconData> icons;
    private MultiIcon(List<IconData> icons) { super(TYPE); this.icons = icons; }

    public static IconData ofMultiple(IconData... icons) { return ofMultiple(ImmutableList.copyOf(icons)); }
    public static IconData ofMultiple(List<IconData> list) { return new MultiIcon(ImmutableList.copyOf(list)); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(EasyGuiGraphics gui, int x, int y) {
        for(IconData icon : this.icons)
            icon.render(gui, x, y);
    }

    @Override
    protected void saveAdditional(ReadWriteContext<CompoundTag> context) {
        ListTag children = new ListTag();
        for(IconData icon : this.icons)
            children.add(icon.save(context.lookup));
        context.data.put("Children",children);
    }

    @Override
    protected void writeAdditional(ReadWriteContext<JsonObject> context) {
        JsonArray children = new JsonArray();
        for(IconData icon : this.icons)
            children.add(icon.write(context.lookup));
        context.data.add("Children",children);
    }

    private static IconData load(ReadWriteContext<CompoundTag> context) {
        List<IconData> result = new ArrayList<>();
        ListTag children = context.data.getList("Children", Tag.TAG_COMPOUND);
        for(int i = 0; i < children.size(); ++i)
        {
            IconData icon = load(children.getCompound(i),context.lookup);
            if(icon != null)
                result.add(icon);
        }
        return new MultiIcon(result);
    }

    private static IconData parse(ReadWriteContext<JsonObject> context) {
        List<IconData> result = new ArrayList<>();
        JsonArray children = GsonHelper.getAsJsonArray(context.data,"Children");
        for(int i = 0; i < children.size(); ++i)
            result.add(parse(GsonHelper.convertToJsonObject(children.get(i),"Children[" + i + "]"),context.lookup));
        return new MultiIcon(result);
    }

}
package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
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

    public static final Type TYPE = new Type(VersionUtil.lcResource("multi_icon"),MultiIcon::loadMulti,MultiIcon::parseMulti);

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag children = new ListTag();
        for(IconData icon : this.icons)
            children.add(icon.save(lookup));
        tag.put("Children",children);
    }

    @Override
    protected void writeAdditional(JsonObject json, HolderLookup.Provider lookup) {
        JsonArray children = new JsonArray();
        for(IconData icon : this.icons)
            children.add(icon.write(lookup));
        json.add("Children",children);
    }

    private static IconData loadMulti(CompoundTag tag, HolderLookup.Provider lookup) {
        List<IconData> result = new ArrayList<>();
        ListTag children = tag.getList("Children", Tag.TAG_COMPOUND);
        for(int i = 0; i < children.size(); ++i)
        {
            IconData icon = load(children.getCompound(i),lookup);
            if(icon != null)
                result.add(icon);
        }
        return new MultiIcon(result);
    }

    private static IconData parseMulti(JsonObject json, HolderLookup.Provider lookup) {
        List<IconData> result = new ArrayList<>();
        JsonArray children = GsonHelper.getAsJsonArray(json,"Children");
        for(int i = 0; i < children.size(); ++i)
            result.add(parse(GsonHelper.convertToJsonObject(children.get(i),"Children[" + i + "]"),lookup));
        return new MultiIcon(result);
    }

}
package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ImageIcon extends IconData
{
    public static final Type TYPE = new Type(VersionUtil.lcResource("texture"),ImageIcon::load,ImageIcon::parse);

    private final NormalSprite sprite;
    private ImageIcon(NormalSprite sprite) { super(TYPE); this.sprite = sprite; }

    public static IconData ofImage(ResourceLocation image, int u, int v) { return ofImage(new SpriteSource(image,u,v,16,16)); }
    public static IconData ofImage(SpriteSource sprite) { return new ImageIcon(new NormalSprite(sprite)); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(EasyGuiGraphics gui, int x, int y) { this.sprite.render(gui,x,y); }

    @Override
    protected void saveAdditional(ReadWriteContext<CompoundTag> context) {
        CompoundTag tag = context.data;
        tag.putString("Image",this.sprite.image.texture().toString());
        tag.putInt("u",this.sprite.image.u());
        tag.putInt("v",this.sprite.image.v());
        tag.putInt("w",this.sprite.image.width());
        tag.putInt("h",this.sprite.image.height());
        tag.putInt("tw",this.sprite.image.textureWidth());
        tag.putInt("th",this.sprite.image.textureHeight());
    }

    @Override
    protected void writeAdditional(ReadWriteContext<JsonObject> context) {
        JsonObject json = context.data;
        json.addProperty("Image",this.sprite.image.texture().toString());
        json.addProperty("u",this.sprite.image.u());
        json.addProperty("v",this.sprite.image.v());
        json.addProperty("w",this.sprite.image.width());
        json.addProperty("h",this.sprite.image.height());
        json.addProperty("tw",this.sprite.image.textureWidth());
        json.addProperty("th",this.sprite.image.textureHeight());
    }

    private static IconData load(ReadWriteContext<CompoundTag> context) {
        CompoundTag tag = context.data;
        ResourceLocation image = VersionUtil.parseResource(tag.getString("Image"));
        int u = tag.getInt("u");
        int v = tag.getInt("v");
        int w = tag.getInt("w");
        int h = tag.getInt("h");
        int tw = tag.getInt("tw");
        int th = tag.getInt("th");
        return ofImage(new SpriteSource(image,u,v,w,h,tw,th));
    }

    private static IconData parse(ReadWriteContext<JsonObject> context)
    {
        JsonObject json = context.data;
        ResourceLocation image = VersionUtil.parseResource(GsonHelper.getAsString(json,"Image"));
        int u = GsonHelper.getAsInt(json,"u");
        int v = GsonHelper.getAsInt(json,"v");
        int w = GsonHelper.getAsInt(json,"w");
        int h = GsonHelper.getAsInt(json,"h");
        int tw = GsonHelper.getAsInt(json,"tw");
        int th = GsonHelper.getAsInt(json,"th");
        return ofImage(new SpriteSource(image,u,v,w,h,tw,th));
    }

}

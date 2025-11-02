package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextIcon extends IconData
{

    public static final Type TYPE = new Type(VersionUtil.lcResource("text"),TextIcon::loadIcon,TextIcon::parseIcon);

    private final Component iconText;
    private final int textColor;
    private TextIcon(Component iconText, int textColor) {
        super(TYPE);
        this.iconText = iconText;
        this.textColor = textColor;
    }

    public static IconData ofText(Component text) { return ofText(text,0xFFFFFF); }
    public static IconData ofText(Component text, int textColor) { return new TextIcon(text,textColor); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(EasyGuiGraphics gui, int x, int y)
    {
        int xPos = x + 8 - (gui.font.width(this.iconText)/2);
        int yPos = y + ((16 - gui.font.lineHeight) / 2);
        gui.drawShadowed(this.iconText, xPos, yPos, this.textColor);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("Text", Component.Serializer.toJson(this.iconText));
        tag.putInt("Color",this.textColor);
    }

    @Override
    protected void writeAdditional(JsonObject json) {
        json.add("Text", Component.Serializer.toJsonTree(this.iconText));
        json.addProperty("Color",this.textColor);
    }

    private static IconData loadIcon(CompoundTag tag) {
        Component text = Component.Serializer.fromJson(tag.getString("Text"));
        int color = tag.getInt("Color");
        return new TextIcon(text,color);
    }

    private static IconData parseIcon(JsonObject json) {
        Component text = Component.Serializer.fromJson(json.get("Text"));
        int color = GsonHelper.getAsInt(json,"Color",0x404040);
        return new TextIcon(text,color);
    }

}
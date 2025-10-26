package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.misc.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextIcon extends IconData
{

    public static final Type TYPE = new Type(VersionUtil.lcResource("text"),TextIcon::load,TextIcon::parse);

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
    protected void saveAdditional(ReadWriteContext<CompoundTag> context) {
        CompoundTag tag = context.data;
        tag.putString("Text", Component.Serializer.toJson(this.iconText,context.lookup));
        tag.putInt("Color",this.textColor);
    }

    @Override
    protected void writeAdditional(ReadWriteContext<JsonObject> context) {
        JsonObject json = context.data;
        json.add("Text", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,this.iconText).getOrThrow());
        json.addProperty("Color",this.textColor);
    }

    private static IconData load(ReadWriteContext<CompoundTag> context) {
        Component text = Component.Serializer.fromJson(context.data.getString("Text"),context.lookup);
        int color = context.data.getInt("Color");
        return new TextIcon(text,color);
    }

    private static IconData parse(ReadWriteContext<JsonObject> context) {
        Component text = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, context.data.get("Text")).getOrThrow(JsonSyntaxException::new).getFirst();
        int color = GsonHelper.getAsInt(context.data,"Color",0x404040);
        return new TextIcon(text,color);
    }



}
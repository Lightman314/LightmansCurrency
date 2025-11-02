package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextIcon extends IconData
{

    public static final Type TYPE = new Type(VersionUtil.lcResource("text"),TextIcon::loadText,TextIcon::parseText);

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putString("Text", Component.Serializer.toJson(this.iconText,lookup));
        tag.putInt("Color",this.textColor);
    }

    @Override
    protected void writeAdditional(JsonObject json, HolderLookup.Provider lookup) {
        json.add("Text", ComponentSerialization.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,lookup),this.iconText).getOrThrow());
        json.addProperty("Color",this.textColor);
    }

    private static IconData loadText(CompoundTag tag, HolderLookup.Provider lookup) {
        Component text = Component.Serializer.fromJson(tag.getString("Text"),lookup);
        int color = tag.getInt("Color");
        return new TextIcon(text,color);
    }

    private static IconData parseText(JsonObject json, HolderLookup.Provider lookup) {
        Component text = ComponentSerialization.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE,lookup),json.get("Text")).getOrThrow(JsonSyntaxException::new).getFirst();
        int color = GsonHelper.getAsInt(json,"Color",0x404040);
        return new TextIcon(text,color);
    }



}
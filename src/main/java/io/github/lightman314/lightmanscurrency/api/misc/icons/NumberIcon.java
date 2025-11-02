package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NumberIcon extends IconData
{
    public static final Type TYPE = new Type(VersionUtil.lcResource("number_icon"),NumberIcon::loadIcon,NumberIcon::parseIcon);
    private final int number;
    private NumberIcon(int number) { super(TYPE); this.number = number; }

    public static IconData ofNumber(int number) { return new NumberIcon(number); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(EasyGuiGraphics gui, int x, int y) {
        String text = String.valueOf(this.number);
        int width = gui.font.width(text);
        gui.drawShadowed(text,x + 17 - width,y + 9,0xFFFFFF);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) { tag.putInt("Number",this.number); }
    @Override
    protected void writeAdditional(JsonObject json) { json.addProperty("Number",this.number); }

    private static IconData loadIcon(CompoundTag tag) { return new NumberIcon(tag.getInt("Number")); }

    private static IconData parseIcon(JsonObject json) { return new NumberIcon(GsonHelper.getAsInt(json,"Number")); }

}
package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.misc.ReadWriteContext;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IconIcon extends IconData {

    public static final Type TYPE = new Type(VersionUtil.lcResource("icon"),IconIcon::load,IconIcon::parse);

    private final ResourceLocation location;
    public IconIcon(ResourceLocation location) {
        super(TYPE);
        this.location = location;
    }

    public static IconData ofIcon(ResourceLocation location) { return new IconIcon(location.withPrefix("textures/gui/icons/").withSuffix(".png")); }

    @Override
    public void render(EasyGuiGraphics gui, int x, int y) {
        gui.blit(this.location,x,y,0,0,16,16,16,16);
    }

    @Override
    protected void saveAdditional(ReadWriteContext<CompoundTag> context) { context.data.putString("location",this.location.toString()); }

    @Override
    protected void writeAdditional(ReadWriteContext<JsonObject> context) { context.data.addProperty("location",this.location.toString()); }

    private static IconData load(ReadWriteContext<CompoundTag> context)
    {
        return new IconIcon(VersionUtil.parseResource(context.data.getString("location")));
    }

    private static IconData parse(ReadWriteContext<JsonObject> context) { return new IconIcon(VersionUtil.parseResource(GsonHelper.getAsString(context.data,"location"))); }

}

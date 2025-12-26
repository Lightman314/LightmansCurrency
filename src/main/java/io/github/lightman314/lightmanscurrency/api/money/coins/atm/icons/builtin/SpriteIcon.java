package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpriteIcon extends ATMIconData {

	public static final ResourceLocation TYPE_NAME = VersionUtil.lcResource( "texture");
	public static final IconType TYPE = IconType.create(TYPE_NAME, SpriteIcon::new);

    public Object sprite = null;

	public final ResourceLocation texture;
    public final int u;
    public final int v;
    public final int width;
    public final int height;
    public final int textureWidth;
    public final int textureHeight;

	public SpriteIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);

        this.texture = VersionUtil.parseResource(GsonHelper.getAsString(data, "texture"));
        this.u = GsonHelper.getAsInt(data,"u");
        this.v = GsonHelper.getAsInt(data,"v");
        this.width = GsonHelper.getAsInt(data,"width");
        this.height = GsonHelper.getAsInt(data,"height");
        this.textureWidth = GsonHelper.getAsInt(data,"textureWidth",256);
        this.textureHeight = GsonHelper.getAsInt(data,"textureHeight",256);

	}

	public SpriteIcon(int xPos, int yPos, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		super(xPos,yPos);
		this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
	}
	
	@Override
	protected void saveAdditional(JsonObject data, HolderLookup.Provider lookup) {
		
		data.addProperty("texture", this.texture.toString());
		data.addProperty("u", this.u);
		data.addProperty("v", this.v);
		data.addProperty("width", this.width);
		data.addProperty("height", this.height);
        data.addProperty("textureWidth",this.textureWidth);
        data.addProperty("textureHeight",this.textureHeight);

	}

	@Override
	public ResourceLocation getType() { return TYPE_NAME; }
	
}

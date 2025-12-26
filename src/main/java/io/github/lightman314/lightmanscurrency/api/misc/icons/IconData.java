package io.github.lightman314.lightmanscurrency.api.misc.icons;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class IconData {

	private static IconData NULL = null;
	public static IconData Null() {
		if(NULL == null)
			NULL = new NullIcon();
		return NULL;
	}

	private static final Map<ResourceLocation,Type> ICON_TYPES = new HashMap<>();
	public static void registerIconType(Type type)
	{
		if(ICON_TYPES.containsKey(type.id))
		{
			LightmansCurrency.LogDebug("Attempted to register icon of type '" + type + "' twice!");
			return;
		}
		ICON_TYPES.put(type.id,type);
	}

	public static void registerDefaultIcons() {
		if(ICON_TYPES.containsKey(NullIcon.TYPE.id))
		{
			LightmansCurrency.LogWarning("Attempted to register the default icons twice!");
			return;
		}
		registerIconType(NullIcon.TYPE);
		registerIconType(ItemIcon.TYPE);
		registerIconType(ImageIcon.TYPE);
		registerIconType(IconIcon.TYPE);
		registerIconType(TextIcon.TYPE);
		registerIconType(NumberIcon.TYPE);
		registerIconType(MultiIcon.TYPE);
	}

	@Nullable
	public static IconData load(CompoundTag tag, HolderLookup.Provider lookup)
	{
		if(tag.contains("Type"))
		{
			ResourceLocation type = VersionUtil.parseResource(tag.getString("Type"));
			if(ICON_TYPES.containsKey(type))
				return ICON_TYPES.get(type).loader.apply(tag,lookup);
		}
		return null;
	}
    public static IconData safeLoad(CompoundTag tag, HolderLookup.Provider lookup, IconData defaultIcon) { return Objects.requireNonNullElse(load(tag,lookup),defaultIcon); }

    public static IconData parse(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
    {
        ResourceLocation type = VersionUtil.parseResource(GsonHelper.getAsString(json,"Type"));
        if(ICON_TYPES.containsKey(type))
            return ICON_TYPES.get(type).parser.apply(json,lookup);
        throw new JsonSyntaxException("Unknown icon type " + type);
    }

	protected final Type type;
	public final boolean isNull() { return this instanceof NullIcon; }
	protected IconData(Type type) { this.type = type; }

	@OnlyIn(Dist.CLIENT)
	public final void render(EasyGuiGraphics gui, ScreenPosition pos) { this.render(gui, pos.x, pos.y); }
	@OnlyIn(Dist.CLIENT)
	public abstract void render(EasyGuiGraphics gui, int x, int y);

	public final CompoundTag save(HolderLookup.Provider lookup)
	{
        CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag,lookup);
        tag.putString("Type", this.type.toString());
		return tag;
	}
	protected abstract void saveAdditional(CompoundTag context, HolderLookup.Provider lookup);

    public final JsonObject write(HolderLookup.Provider lookup)
    {
        JsonObject json = new JsonObject();
        this.writeAdditional(json,lookup);
        json.addProperty("Type",this.type.toString());
        return json;
    }

    protected abstract void writeAdditional(JsonObject json, HolderLookup.Provider lookup);

	private static class NullIcon extends IconData
	{
        private static final Type TYPE = new Type(VersionUtil.lcResource("null"),c -> IconData.Null(),j -> IconData.Null());
		private NullIcon() { super(TYPE); }
		@Override
		@OnlyIn(Dist.CLIENT)
		public void render(EasyGuiGraphics gui, int x, int y) {}
		@Override
		protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) { }
        @Override
        protected void writeAdditional(JsonObject json, HolderLookup.Provider lookup) { }
	}

    public record Type(ResourceLocation id, BiFunction<CompoundTag,HolderLookup.Provider,IconData> loader, BiFunction<JsonObject,HolderLookup.Provider,IconData> parser) {
        public Type(ResourceLocation id, Function<CompoundTag,IconData> loader,Function<JsonObject,IconData> parser) { this(id,(c,l) -> loader.apply(c),(j,l) -> parser.apply(j)); }
        @Override
        public String toString() { return this.id.toString(); }
    }

}
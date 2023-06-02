package io.github.lightman314.lightmanscurrency.common.atm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.common.atm.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.common.atm.icons.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.common.atm.icons.SpriteIcon;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ATMIconData {
	
	private static Map<String,IconType> REGISTERED_TYPES = null;
	private static boolean initialized = false;
	
	public static void init() {
		if(initialized)
			return;
		initialized = true;
		LightmansCurrency.LogInfo("Collecting ATM Icon Types");
		REGISTERED_TYPES = new HashMap<>();
		register(ItemIcon.TYPE);
		register(SimpleArrowIcon.TYPE);
		register(SpriteIcon.TYPE);
	}
	
	/**
	 * Used to register ATM Icon Types.
	 * If called before the map is initialized, it will initialize it for you.
	 * Recommended to be run during the FMLCommonSetupEvent, but at the very least must be run before the ServerStartedEvent
	 */
	public static void register(IconType iconType) {
		if(!initialized)
			init();
		String type = iconType.type.toString();
		if(REGISTERED_TYPES.containsKey(type))
		{
			if(REGISTERED_TYPES.get(type) == iconType)
				LightmansCurrency.LogWarning("ATM Icon Type '" + type + "' was registered twice.");
			else
				LightmansCurrency.LogWarning("Attempted to register an ATM Icon Type of type '" + type + "', but an ATM Icon of that type is already registered.");
		}
		REGISTERED_TYPES.put(type, iconType);
		LightmansCurrency.LogInfo("ATM Icon Type '" + type + "' has been registered successfully.");
	}
	
	
	protected final int xPos;
	protected final int yPos;
	
	protected ATMIconData(JsonObject data) throws RuntimeException {
		this.xPos = data.get("x").getAsInt();
		this.yPos = data.get("y").getAsInt();
	}
	
	protected ATMIconData(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}
	
	public final JsonObject save() {
		JsonObject data = new JsonObject();
		data.addProperty("type", this.getType().toString());
		data.addProperty("x", this.xPos);
		data.addProperty("y", this.yPos);
		this.saveAdditional(data);
		return data;
	}
	
	protected abstract ResourceLocation getType();
	
	protected abstract void saveAdditional(JsonObject data);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void render(ATMConversionButton button, PoseStack pose, boolean isHovered);
	
	public static ATMIconData parse(JsonObject data) throws Exception {
		if(data.has("type"))
		{
			String type = data.get("type").getAsString();
			if(REGISTERED_TYPES.containsKey(type))
				return REGISTERED_TYPES.get(type).parse(data);
			else
				throw new Exception("No ATM Icon of type '" + type + "'. Unable to parse.");
		}
		else
			throw new Exception("ATM Icon data has no 'type' entry. Unable to parse.");
	}
	
	public static final class IconType {
		public final ResourceLocation type;
		private final Function<JsonObject,ATMIconData> deserializer;
		
		public boolean matches(String type) { return this.type.toString().equals(type); }
		
		public ATMIconData parse(JsonObject data) { return this.deserializer.apply(data); }
		
		private IconType(ResourceLocation type, Function<JsonObject,ATMIconData> deserializer) {
			this.type = type;
			this.deserializer = deserializer;
		}
		
		public static IconType create(ResourceLocation type, Function<JsonObject,ATMIconData> deserializer) { return new IconType(type, deserializer); }
		
	}
	
	public static List<ATMIconData> getConvertAllUpDefault() {
		return Lists.newArrayList();
	}
	
}

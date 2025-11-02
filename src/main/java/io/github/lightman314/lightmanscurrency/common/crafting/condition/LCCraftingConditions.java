package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import javax.annotation.Nonnull;

public class LCCraftingConditions {

	public static final ICondition NETWORK_TRADER = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftNetworkTraders);
	public static final ICondition TRADER_INTERFACE = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftTraderInterfaces);


	public static void register()
	{
		try{
			//Register Crafting Conditions
			CraftingHelper.register(ConfigCraftingCondition.SERIALIZER);
		} catch(IllegalStateException ignored) { }
	}

	private static class UpdateSerializer implements IConditionSerializer<ConfigCraftingCondition>
	{
		private final ResourceLocation id;
		private final ConfigCraftingCondition condition;
		UpdateSerializer(@Nonnull ResourceLocation id, @Nonnull ConfigCraftingCondition condition) { this.id = id; this.condition = condition; }

		@Override
		public void write(JsonObject jsonObject, ConfigCraftingCondition condition) { ConfigCraftingCondition.SERIALIZER.write(jsonObject,this.condition); }

		@Override
		public ConfigCraftingCondition read(JsonObject jsonObject) { return this.condition; }

		@Override
		public ResourceLocation getID() { return this.id; }

		@Override
		public JsonObject getJson(ConfigCraftingCondition value) {
			JsonObject json = new JsonObject();
			this.write(json, value);
			json.addProperty("type", ConfigCraftingCondition.ID.toString());
			return json;
		}
	}
	
}

package io.github.lightman314.lightmanscurrency.loot.glm;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.loot.LootManager;
import io.github.lightman314.lightmanscurrency.loot.LootManager.PoolLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class CoinsInChestsModifier implements IGlobalLootModifier {
	
	private CoinsInChestsModifier() {
		LightmansCurrency.LogInfo("CoinsInChestModifier was deserialized!");
	}
	
	@Override
	public @NotNull List<ItemStack> apply(List<ItemStack> generatedLoot, LootContext context) {
		
		//If chest loot is disabled, do nothing.
		if(!Config.COMMON.enableChestLoot.get())
			return generatedLoot;
		
		String lootTable = context.getQueriedLootTableId().toString();
		
		PoolLevel lootLevel = LootManager.GetChestPoolLevel(lootTable);
		
		if(lootLevel != null)
		{
			LightmansCurrency.LogDebug("Loot table '" + lootTable + "' has " + lootLevel.toString() + " level chest loot. Adding coins to the spawned loot.");
			List<ItemStack> coinLoot = LootManager.GetRandomChestLoot(lootLevel, context);
			for(ItemStack coin : coinLoot) {
				LightmansCurrency.LogDebug("Adding " + coin.getCount() + "x " + ForgeRegistries.ITEMS.getKey(coin.getItem()).toString() + " to the chest loot.");
				generatedLoot.add(coin);
			}
		}
		
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<CoinsInChestsModifier> {

		@Override
		public CoinsInChestsModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
			return new CoinsInChestsModifier();
		}

		@Override
		public JsonObject write(CoinsInChestsModifier instance) {
			return new JsonObject();
		}
		
	}
	
}
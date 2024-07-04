package io.github.lightman314.lightmanscurrency.common.loot.glm;

import java.util.List;

import com.mojang.serialization.*;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.ChestPoolLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import javax.annotation.Nonnull;

public class CoinsInChestsModifier implements IGlobalLootModifier {
	
	private CoinsInChestsModifier() { LightmansCurrency.LogInfo("CoinsInChestModifier was deserialized!"); }

	public static final MapCodec<CoinsInChestsModifier> SERIALIZER = MapCodec.unit(new CoinsInChestsModifier());

	@Override
	@Nonnull
	public ObjectArrayList<ItemStack> apply(@Nonnull ObjectArrayList<ItemStack> generatedLoot, @Nonnull LootContext context) {
		
		//If chest loot is disabled, do nothing.
		if(!LCConfig.COMMON.enableChestLoot.get())
			return generatedLoot;
		
		String lootTable = context.getQueriedLootTableId().toString();
		
		ChestPoolLevel lootLevel = LootManager.GetChestPoolLevel(lootTable);
		
		if(lootLevel != null)
		{
			LightmansCurrency.LogDebug("Loot table '" + lootTable + "' has " + lootLevel + " level chest loot. Adding coins to the spawned loot.");
			List<ItemStack> coinLoot = LootManager.getLoot(lootLevel.lootTable, context);
			for(ItemStack coin : coinLoot) {
				LightmansCurrency.LogDebug("Adding " + coin.getCount() + "x " + BuiltInRegistries.ITEM.getKey(coin.getItem()) + " to the chest loot.");
				generatedLoot.add(coin);
			}
		}
		
		return generatedLoot;
	}

	@Nonnull
	public MapCodec<? extends IGlobalLootModifier> codec() { return SERIALIZER; }
	
}

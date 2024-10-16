package io.github.lightman314.lightmanscurrency.common.loot.glm;

import java.util.List;

import com.mojang.serialization.Codec;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.ChestPoolLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class CoinsInChestsModifier implements IGlobalLootModifier {

	private CoinsInChestsModifier() { LightmansCurrency.LogInfo("CoinsInChestModifier was deserialized!"); }

	public static final CoinsInChestsModifier INSTANCE = new CoinsInChestsModifier();
	public static final Codec<CoinsInChestsModifier> CODEC = Codec.unit(INSTANCE);
	
	@Override
	@Nonnull
	public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		
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
				LightmansCurrency.LogDebug("Adding " + coin.getCount() + "x " + ForgeRegistries.ITEMS.getKey(coin.getItem()).toString() + " to the chest loot.");
				generatedLoot.add(coin);
			}
		}
		
		return generatedLoot;
	}
	
	@Override
	public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
	
}

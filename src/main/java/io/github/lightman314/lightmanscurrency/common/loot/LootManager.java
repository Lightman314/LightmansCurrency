package io.github.lightman314.lightmanscurrency.common.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.SpawnTrackerCapability;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.LootPool.Builder;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.functions.LootingEnchantBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class LootManager {

	public static final float LOOTING_MODIFIER = 0.01f;
	
	public enum PoolLevel
	{
		T1(0, true),
		T2(1, true),
		T3(2, true),
		T4(3, true),
		T5(4, true),
		T6(5, true),
		BOSS_T1(6, false),
		BOSS_T2(7, false),
		BOSS_T3(8, false),
		BOSS_T4(9, false),
		BOSS_T5(10, false),
		BOSS_T6(11, false);
		
		public final int level;
		private final boolean requiresPlayerKill;
		
		PoolLevel(int level, boolean requiresPlayerKill) { this.level = level; this.requiresPlayerKill = requiresPlayerKill; }
		
	}
	
	public static SpawnReason deserializeSpawnReason(String reasonString)
	{
		return deserializeSpawnReason(reasonString, SpawnReason.NATURAL);
	}
	
	public static SpawnReason deserializeSpawnReason(String reasonString, SpawnReason defaultReason)
	{
		for(SpawnReason reason : SpawnReason.values())
		{
			if(reason.toString().contentEquals(reasonString))
				return reason;
		}
		LightmansCurrency.LogWarning("Reason string \"" + reasonString + "\" could not be properly deserialized. Returning the default spawn reason.");
		return defaultReason;
	}
	
	private static final String ENTITY = "minecraft:";
	private static final String CHEST = "minecraft:chests/";
	
	public static final List<String> ENTITY_DROPLIST_T1 = ImmutableList.of(ENTITY + "slime", ENTITY + "silverfish");
	public static final List<String> ENTITY_DROPLIST_T2 = ImmutableList.of(ENTITY + "zombie", ENTITY + "skeleton", ENTITY + "creeper", ENTITY + "spider", ENTITY + "cave_spider", ENTITY + "husk", ENTITY + "stray", ENTITY + "magma_cube", ENTITY + "zombie_villager", ENTITY + "drowned");
	public static final List<String> ENTITY_DROPLIST_T3 = ImmutableList.of(ENTITY + "guardian", ENTITY + "elder_guardian", ENTITY + "phantom", ENTITY + "blaze", ENTITY + "ghast", ENTITY + "witch", ENTITY + "hoglin", ENTITY + "piglin_brute", ENTITY + "piglin", ENTITY + "zombified_piglin");
	public static final List<String> ENTITY_DROPLIST_T4 = ImmutableList.of(ENTITY + "enderman", ENTITY + "evoker", ENTITY + "vindicator", ENTITY + "pillager", ENTITY + "ravager", ENTITY + "shulker");
	public static final List<String> ENTITY_DROPLIST_T5 = ImmutableList.of(ENTITY + "wither_skeleton");
	public static final List<String> ENTITY_DROPLIST_T6 = ImmutableList.of();
	
	public static final List<String> BOSS_ENTITY_DROPLIST_T1 = ImmutableList.of();
	public static final List<String> BOSS_ENTITY_DROPLIST_T2 = ImmutableList.of();
	public static final List<String> BOSS_ENTITY_DROPLIST_T3 = ImmutableList.of();
	public static final List<String> BOSS_ENTITY_DROPLIST_T4 = ImmutableList.of(ENTITY + "warden");
	public static final List<String> BOSS_ENTITY_DROPLIST_T5 = ImmutableList.of(ENTITY + "ender_dragon");
	public static final List<String> BOSS_ENTITY_DROPLIST_T6 = ImmutableList.of(ENTITY + "wither");
	
	public static final List<String> CHEST_DROPLIST_T1 = ImmutableList.of(CHEST + "underwater_ruin_small", CHEST + "underwater_ruin_big");
	public static final List<String> CHEST_DROPLIST_T2 = ImmutableList.of();
	public static final List<String> CHEST_DROPLIST_T3 = ImmutableList.of(CHEST + "jungle_temple", CHEST + "nether_bridge", CHEST + "simple_dungeon", CHEST + "ruined_portal");
	public static final List<String> CHEST_DROPLIST_T4 = ImmutableList.of(CHEST + "stronghold_crossing", CHEST + "stronghold_corridor", CHEST + "stronghold_library", CHEST + "ancient_city");
	public static final List<String> CHEST_DROPLIST_T5 = ImmutableList.of(CHEST + "buried_treasure", CHEST + "bastion_hoglin_stable", CHEST + "bastion_bridge", CHEST + "bastion_other", CHEST + "bastion_treasure", CHEST + "end_city_treasure");
	public static final List<String> CHEST_DROPLIST_T6 = ImmutableList.of();
	
	private static final Map<String,PoolLevel> EXTERNAL_ENTITY_ENTRIES = new HashMap<>();
	private static final Map<String,PoolLevel> EXTERNAL_CHEST_ENTRIES = new HashMap<>();
	
	private static boolean lootTablesBuilt = false;
	
	//Normal entity loot
	private static Builder ENTITY_LOOT_T1 = null;
	private static Builder ENTITY_LOOT_T2 = null;
	private static Builder ENTITY_LOOT_T3 = null;
	private static Builder ENTITY_LOOT_T4 = null;
	private static Builder ENTITY_LOOT_T5 = null;
	private static Builder ENTITY_LOOT_T6 = null;
	
	//Boss loot
	private static List<Builder> ENTITY_LOOT_BOSS_T1 = null;
	private static List<Builder> ENTITY_LOOT_BOSS_T2 = null;
	private static List<Builder> ENTITY_LOOT_BOSS_T3 = null;
	private static List<Builder> ENTITY_LOOT_BOSS_T4 = null;
	private static List<Builder> ENTITY_LOOT_BOSS_T5 = null;
	private static List<Builder> ENTITY_LOOT_BOSS_T6 = null;
	
	//Chest loot
	private static Builder CHEST_LOOT_T1 = null;
	private static Builder CHEST_LOOT_T2 = null;
	private static Builder CHEST_LOOT_T3 = null;
	private static Builder CHEST_LOOT_T4 = null;
	private static Builder CHEST_LOOT_T5 = null;
	private static Builder CHEST_LOOT_T6 = null;

	private static void confirmLootTablesGenerated() {
		if(lootTablesBuilt)
			return;
		regenerateLootTables();
	}

	public static void regenerateLootTables()
	{
		Item lootItem1 = Config.COMMON.lootItem1.get();
		Item lootItem2 = Config.COMMON.lootItem2.get();
		Item lootItem3 = Config.COMMON.lootItem3.get();
		Item lootItem4 = Config.COMMON.lootItem4.get();
		Item lootItem5 = Config.COMMON.lootItem5.get();
		Item lootItem6 = Config.COMMON.lootItem6.get();

		//Normal Loot
		ENTITY_LOOT_T1 = GenerateEntityCoinPool(lootItem1, 1, 10, 0.75f, "lightmanscurrency:entityloot_copper", true);
		ENTITY_LOOT_T2 = GenerateEntityCoinPool(lootItem2, 1, 5, 0.5f, "lightmanscurrency:entityloot_iron", true);
		ENTITY_LOOT_T3 = GenerateEntityCoinPool(lootItem3, 1, 5, 0.25f, "lightmanscurrency:entityloot_gold", true);
		ENTITY_LOOT_T4 = GenerateEntityCoinPool(lootItem4, 1, 3, 0.1f, "lightmanscurrency:entityloot_emerald", true);
		ENTITY_LOOT_T5 = GenerateEntityCoinPool(lootItem5, 1, 3, 0.05f, "lightmanscurrency:entityloot_diamond", true);
		ENTITY_LOOT_T6 = GenerateEntityCoinPool(lootItem6, 1, 3, 0.025F, "lightmanscurrency:entityloot_netherite", true);

		//Boss loot
		ENTITY_LOOT_BOSS_T1 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false));
		ENTITY_LOOT_BOSS_T2 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false));
		ENTITY_LOOT_BOSS_T3 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false));
		ENTITY_LOOT_BOSS_T4 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false));
		ENTITY_LOOT_BOSS_T5 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(lootItem5, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false));
		ENTITY_LOOT_BOSS_T6 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(lootItem5, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false),GenerateEntityCoinPool(lootItem6, 1, 5, 1.0f, "lightmanscurrency:coinloot_boss_netherite", false));

		//Chest loot
		CHEST_LOOT_T1 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER}, 1, 5, "lightmanscurrency:chestloot_copper");
		CHEST_LOOT_T2 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON}, 1, 5, "lightmanscurrency:chestloot_iron");
		CHEST_LOOT_T3 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD}, 2, 6, "lightmanscurrency:chestloot_gold");
		CHEST_LOOT_T4 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD}, 3, 6, "lightmanscurrency:chestloot_emerald");
		CHEST_LOOT_T5 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND}, 3, 6, "lightmanscurrency:chestloot_diamond");
		CHEST_LOOT_T6 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND, ChestLootEntryData.NETHERITE}, 3, 6, "lightmanscurrency:chestloot_netherite");

		lootTablesBuilt = true;
	}


	
	private static String getValueList(ConfigValue<List<? extends String>> config) {
		StringBuilder buffer = new StringBuilder();
		List<? extends String> list = config.get();
		for(String value : list)
		{
			if(buffer.length() > 0)
				buffer.append(", ");
			buffer.append("\"").append(value).append("\"");
		}
		return buffer.toString();
	}
	
	public static void debugLootConfigs() {
		
		LightmansCurrency.LogDebug("Lightman's Currency common configs have been loaded. Coin loot values are as follows.");
		//Chests
		LightmansCurrency.LogDebug("Chest T1: " + getValueList(Config.COMMON.chestDropsT1));
		LightmansCurrency.LogDebug("Chest T2: " + getValueList(Config.COMMON.chestDropsT2));
		LightmansCurrency.LogDebug("Chest T3: " + getValueList(Config.COMMON.chestDropsT3));
		LightmansCurrency.LogDebug("Chest T4: " + getValueList(Config.COMMON.chestDropsT4));
		LightmansCurrency.LogDebug("Chest T5: " + getValueList(Config.COMMON.chestDropsT5));
		LightmansCurrency.LogDebug("Chest T6: " + getValueList(Config.COMMON.chestDropsT6));
		
		//Entity (normal)
		LightmansCurrency.LogDebug("Entity T1: " + getValueList(Config.COMMON.entityDropsT1));
		LightmansCurrency.LogDebug("Entity T2: " + getValueList(Config.COMMON.entityDropsT2));
		LightmansCurrency.LogDebug("Entity T3: " + getValueList(Config.COMMON.entityDropsT3));
		LightmansCurrency.LogDebug("Entity T4: " + getValueList(Config.COMMON.entityDropsT4));
		LightmansCurrency.LogDebug("Entity T5: " + getValueList(Config.COMMON.entityDropsT5));
		LightmansCurrency.LogDebug("Entity T6: " + getValueList(Config.COMMON.entityDropsT6));
		
		//Entity (boss)
		LightmansCurrency.LogDebug("Boss Entity T1: " + getValueList(Config.COMMON.bossEntityDropsT1));
		LightmansCurrency.LogDebug("Entity Iron (Boss): " + getValueList(Config.COMMON.bossEntityDropsT2));
		LightmansCurrency.LogDebug("Entity Gold (Boss): " + getValueList(Config.COMMON.bossEntityDropsT3));
		LightmansCurrency.LogDebug("Entity Emerald (Boss): " + getValueList(Config.COMMON.bossEntityDropsT4));
		LightmansCurrency.LogDebug("Entity Diamond (Boss): " + getValueList(Config.COMMON.bossEntityDropsT5));
		LightmansCurrency.LogDebug("Entity Netherite (Boss): " + getValueList(Config.COMMON.bossEntityDropsT6));
		
	}
	
	@SubscribeEvent
	public static void onEntitySpawned(LivingSpawnEvent.SpecialSpawn event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(entity instanceof PlayerEntity)
			return;
		
		SpawnTrackerCapability.getSpawnerTracker(event.getEntityLiving()).ifPresent(spawnerTracker -> spawnerTracker.setSpawnReason(event.getSpawnReason()));
		if(!SpawnTrackerCapability.getSpawnerTracker(event.getEntityLiving()).isPresent())
			LightmansCurrency.LogWarning(event.getEntityLiving().getName().getString() + " does not have a ISpawnerTracker attached. Unable to flag it's SpawnReason.");
		
	}
	
	@SubscribeEvent
	public static void attachSpawnTrackerCapability(AttachCapabilitiesEvent<Entity> event)
	{
		//Attach the spawn trader capability to all LivingEntities that aren't players
		if(event.getObject() instanceof LivingEntity && !(event.getObject() instanceof PlayerEntity))
		{
			event.addCapability(CurrencyCapabilities.ID_SPAWN_TRACKER, SpawnTrackerCapability.createProvider((LivingEntity)event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event)
	{
		//Check if this is the server
		if(event.getEntityLiving().level.isClientSide)
			return;
		
		if(!Config.COMMON.enableSpawnerEntityDrops.get())
		{
			//Spawner drops aren't allowed. Check if the entity was spawner-spawned
			AtomicReference<SpawnReason> spawnReason = new AtomicReference<>();
			SpawnTrackerCapability.getSpawnerTracker(event.getEntityLiving()).ifPresent(spawnerTracker -> spawnReason.set(spawnerTracker.spawnReason()));
			if(spawnReason.get() == SpawnReason.SPAWNER)
			{
				LightmansCurrency.LogDebug(event.getEntityLiving().getName().getString() + " did not drop coins, as it was spawned by a spawner.");
				return;
			}
		}
		
		String name = ForgeRegistries.ENTITIES.getKey(event.getEntityLiving().getType()).toString();
		
		if(event.getSource().getDirectEntity() instanceof PlayerEntity || event.getSource().getEntity() instanceof PlayerEntity)
		{
			//Assign the player that killed it
			final PlayerEntity player = event.getSource().getDirectEntity() instanceof PlayerEntity ? (PlayerEntity)event.getSource().getDirectEntity() : (PlayerEntity)event.getSource().getEntity();

			//Block coin drops if the killer was a fake player and fake player coin drops aren't allowed.
			if(player instanceof FakePlayer && !Config.COMMON.allowFakePlayerCoinDrops.get())
				return;
			
			if(Config.COMMON.entityDropsT1.get().contains(name))
	    	{
				DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T1);
	    	}
	    	else if(Config.COMMON.entityDropsT2.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T2);
	    	}
	    	else if(Config.COMMON.entityDropsT3.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T3);
	    	}
	    	else if(Config.COMMON.entityDropsT4.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T4);
	    	}
	    	else if(Config.COMMON.entityDropsT5.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T5);
	    	}
	    	else if(Config.COMMON.entityDropsT6.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.T6);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT1.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T1);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT2.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T2);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT3.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T3);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT4.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T4);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT5.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T5);
	    	}
	    	else if(Config.COMMON.bossEntityDropsT6.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_T6);
	    	}
	    	else
	    	{
	    		EXTERNAL_ENTITY_ENTRIES.forEach((entity,level) -> {
	    			if(entity.equals(name))
	    				DropEntityLoot(event.getEntityLiving(), player, level);
	    		});
	    	}
			return;
		}
		//Boss deaths don't require a player kill to drop coins
    	if(Config.COMMON.bossEntityDropsT1.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T1);
    	}
    	else if(Config.COMMON.bossEntityDropsT2.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T2);
    	}
    	else if(Config.COMMON.bossEntityDropsT3.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T3);
    	}
    	else if(Config.COMMON.bossEntityDropsT4.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T4);
    	}
    	else if(Config.COMMON.bossEntityDropsT5.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T5);
    	}
    	else if(Config.COMMON.bossEntityDropsT6.get().contains(name))
    	{
    		DropEntityLoot(event.getEntityLiving(), null, PoolLevel.BOSS_T6);
    	}
    	else
    	{
    		EXTERNAL_ENTITY_ENTRIES.forEach((entity,level) -> {
    			if(entity.equals(name) && !level.requiresPlayerKill)
    			{
    				DropEntityLoot(event.getEntityLiving(), null, level);
    			}
    		});
    	}
	}
	
	public static void validateEntityDropList()
	{
		validateDropList(Config.COMMON.entityDropsT1);
		validateDropList(Config.COMMON.entityDropsT2);
		validateDropList(Config.COMMON.entityDropsT3);
		validateDropList(Config.COMMON.entityDropsT4);
		validateDropList(Config.COMMON.entityDropsT5);
		validateDropList(Config.COMMON.entityDropsT6);
		validateDropList(Config.COMMON.bossEntityDropsT1);
		validateDropList(Config.COMMON.bossEntityDropsT2);
		validateDropList(Config.COMMON.bossEntityDropsT3);
		validateDropList(Config.COMMON.bossEntityDropsT4);
		validateDropList(Config.COMMON.bossEntityDropsT5);
		validateDropList(Config.COMMON.bossEntityDropsT6);
	}
	
	private static void validateDropList(ConfigValue<List<? extends String>> config)
	{
		List<? extends String> configList = config.get();
		List<String> list = Lists.newArrayList();
		list.addAll(configList);
		boolean modified = false;
		for(int i = 0; i < list.size(); ++i)
		{
			String value = list.get(i);
			if(value.contains("entities/"))
			{
				value = value.replace("entities/", "");
				list.set(i, value);
				modified = true;
			}
			if(value.contains("entities\\"))
			{
				value = value.replace("entities\\", "");
				list.set(i, value);
				modified = true;
			}
		}
		if(modified)
			config.set(list);
	}
	
	private static void DropEntityLoot(Entity entity, PlayerEntity player, PoolLevel coinPool)
	{
		
		if(!Config.COMMON.enableEntityDrops.get())
			return;
		
		confirmLootTablesGenerated();
		
		//LightmansCurrency.LOGGER.info("Dropping entity loot level " + coinPool);
		
		LootTable table = LootTable.lootTable().build();
		LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld) entity.level);
		//Add the KilledByPlayer condition to the Loot Context
		if(player != null)
			contextBuilder.withParameter(LootParameters.KILLER_ENTITY, player)
			.withParameter(LootParameters.LAST_DAMAGE_PLAYER, player);
		
		LootContext context = contextBuilder.create(new LootParameterSet.Builder().optional(LootParameters.LAST_DAMAGE_PLAYER).optional(LootParameters.KILLER_ENTITY).build());
		
		try {
			
			//Boss loot done separately due to loops and exclusiveness.
			if(coinPool == PoolLevel.BOSS_T1)
			{
				//Drop copper boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T1)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			else if(coinPool == PoolLevel.BOSS_T2)
			{
				//Drop iron boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T2)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			else if(coinPool == PoolLevel.BOSS_T3)
			{
				//Drop gold boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T3)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			else if(coinPool == PoolLevel.BOSS_T4)
			{
				//Drop emerald boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T4)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			else if(coinPool == PoolLevel.BOSS_T5)
			{
				//Drop diamond boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T5)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			else if(coinPool == PoolLevel.BOSS_T6)
			{
				//Drop netherite boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T6)
				{
					table.addPool(builder.build());
				}
				//Generate the loot 
				SpawnLootDrops(entity, table.getRandomItems(context));
				return;
			}
			
			//LightmansCurrency.LOGGER.debug("Added " + coinPool + " level entity loot to the " + name + " loot entry.");
			table.addPool(ENTITY_LOOT_T1.build());
			if(coinPool != PoolLevel.T1)
			{
				table.addPool(ENTITY_LOOT_T2.build());
				if(coinPool != PoolLevel.T2)
				{
					table.addPool(ENTITY_LOOT_T3.build());
					if(coinPool != PoolLevel.T3)
					{
						table.addPool(ENTITY_LOOT_T4.build());
						if(coinPool != PoolLevel.T4)
						{
							table.addPool(ENTITY_LOOT_T5.build());
							if(coinPool != PoolLevel.T5)
								table.addPool(ENTITY_LOOT_T6.build());
						}
					}
				}
			}
			
			SpawnLootDrops(entity, table.getRandomItems(context));
		
		} catch(Exception e) { LightmansCurrency.LogError("Error spawning coin drops!", e); }
		
	}
	
	public static List<ItemStack> GetRandomChestLoot(PoolLevel coinPool, LootContext context) {

		confirmLootTablesGenerated();

		try {

			if(coinPool == PoolLevel.T1)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T1.build());
				return safelyGetResults(table, context);
			}
			else if(coinPool == PoolLevel.T2)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T2.build());
				return safelyGetResults(table, context);
			}
			else if(coinPool == PoolLevel.T3)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T3.build());
				return safelyGetResults(table, context);
			}
			else if(coinPool == PoolLevel.T4)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T4.build());
				return safelyGetResults(table, context);
			}
			else if(coinPool == PoolLevel.T5)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T5.build());
				return safelyGetResults(table, context);
			}
			else if(coinPool == PoolLevel.T6)
			{
				LootTable table = LootTable.lootTable().build();
				table.addPool(CHEST_LOOT_T6.build());
				return safelyGetResults(table, context);
			}
			else
			{
				LightmansCurrency.LogError("Attempting to get random chest loot from an invalid chest pool level of '" + (coinPool == null ? "NULL" : coinPool.toString()) + "'");
				return new ArrayList<>();
			}

		} catch(Throwable e) {
			LightmansCurrency.LogError("Error spawning chest coin drops!", e);
			return new ArrayList<>();
		}
	}
	
	@SuppressWarnings("deprecation")
	private static List<ItemStack> safelyGetResults(LootTable table, LootContext context) {
		List<ItemStack> results = new ArrayList<>();
		//Call getRandomItems(LootContext,Consumer<ItemStack>) to keep it from being modified by the GLM's and getting stuck in an infinite loop.
		table.getRandomItems(context, results::add);
		return results;
	}

	public static PoolLevel GetChestPoolLevel(String lootTable) {
		if(Config.COMMON.chestDropsT1.get().contains(lootTable))
			return PoolLevel.T1;
		if(Config.COMMON.chestDropsT2.get().contains(lootTable))
			return PoolLevel.T2;
		if(Config.COMMON.chestDropsT3.get().contains(lootTable))
			return PoolLevel.T3;
		if(Config.COMMON.chestDropsT4.get().contains(lootTable))
			return PoolLevel.T4;
		if(Config.COMMON.chestDropsT5.get().contains(lootTable))
			return PoolLevel.T5;
		if(Config.COMMON.chestDropsT6.get().contains(lootTable))
			return PoolLevel.T6;
		for(String chest : EXTERNAL_CHEST_ENTRIES.keySet())
		{
			if(chest.equals(lootTable))
				return EXTERNAL_CHEST_ENTRIES.get(chest);
		}
		return null;
	}
	
	private static void SpawnLootDrops(Entity entity, List<ItemStack> lootDrops)
	{
		//LightmansCurrency.LOGGER.info("Spawning " + lootDrops.size() + " coin drops.");
		InventoryUtil.dumpContents(entity.level, entity.blockPosition(), lootDrops);
	}
	
	/**
	 * Adds the given entity's loot table to the list so that it will drop coins in addition to its already given loot.
	 * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:entities/zombie"), or of the entities id (e.g. "minecraft:sheep")
	 * @param coinPool The highest level coin that the entity should be allowed to drop.
	 */
	public static void AddEntityCoinPoolToTable(String resource, PoolLevel coinPool)
	{
		EXTERNAL_ENTITY_ENTRIES.put(resource, coinPool);
	}
	
	/**
	 * Adds the given chest's loot table to the list so that it will spawn coins in addition to its already given loot.
	 * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:chests/buried_treasure")
	 * @param coinPool The highest level coin that the chest should spawn. Should not include the BOSS pool levels, as those are for entities only.
	 */
	public static void AddChestCoinPoolToTable(String resource, PoolLevel coinPool)
	{
		if(coinPool.level > PoolLevel.T6.level)
		{
			LightmansCurrency.LogError("Attempted to add a chest to the coin pool at level " + coinPool.name() + ", but that level is not valid for chests.");
			return;
		}
		EXTERNAL_CHEST_ENTRIES.put(resource, coinPool);
	}
	
	
	private static Builder GenerateEntityCoinPool(Item item, float min, float max, float chance, String name, boolean requirePlayerKill)
	{

		Builder lootPoolBuilder = LootPool.lootPool()
				.setRolls(ConstantRange.exactly(1))
				.add(ItemLootEntry.lootTableItem(item).apply(SetCount.setCount(RandomValueRange.between(min, max))).apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0f, 1f))))
				.name(name);
		
		//Require that the player killed it (usually only disabled for bosses)
		if(requirePlayerKill)
			lootPoolBuilder.when(KilledByPlayer.killedByPlayer());
		//Add a random chance to the loot (if applicable, usually only disabled for bosses)
		if(chance < 1.0f)
			lootPoolBuilder.when(RandomChanceWithLooting.randomChanceAndLootingBoost(chance, LOOTING_MODIFIER));
		
		return lootPoolBuilder;
		
	}
	
	
	private static Builder GenerateChestCoinPool(ChestLootEntryData[] lootEntries, float minRolls, float maxRolls, String name)
	{
		
		Builder lootPoolBuilder = LootPool.lootPool()
				.setRolls(RandomValueRange.between(minRolls, maxRolls))
				.name(name);
		
		//Add each loot entry
		for(ChestLootEntryData entry : lootEntries)
		{
			lootPoolBuilder.add(ItemLootEntry.lootTableItem(entry.item.get()).apply(SetCount.setCount(RandomValueRange.between(entry.minCount, entry.maxCount))).setWeight(entry.weight));
		}
		
		return lootPoolBuilder;
		
	}

	private static class ChestLootEntryData {

		public final Supplier<Item> item;
		public final float minCount;
		public final float maxCount;
		public final int weight;
		public ChestLootEntryData(Supplier<Item> item, float minCount, float maxCount, int weight) {
			this.item = item; this.minCount = minCount; this.maxCount = maxCount; this.weight = weight;
		}

		public static ChestLootEntryData COPPER = new ChestLootEntryData(Config.COMMON.lootItem1, 1, 10, 1);
		public static ChestLootEntryData IRON = new ChestLootEntryData(Config.COMMON.lootItem2, 1, 10, 2);
		public static ChestLootEntryData GOLD = new ChestLootEntryData(Config.COMMON.lootItem3, 1, 10, 3);
		public static ChestLootEntryData EMERALD = new ChestLootEntryData(Config.COMMON.lootItem4, 1, 10, 4);
		public static ChestLootEntryData DIAMOND = new ChestLootEntryData(Config.COMMON.lootItem5, 1, 8, 5);
		public static ChestLootEntryData NETHERITE = new ChestLootEntryData(Config.COMMON.lootItem6, 1, 3, 6);

	}
	
	
	
}

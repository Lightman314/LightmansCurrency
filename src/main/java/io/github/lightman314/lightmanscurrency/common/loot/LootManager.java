package io.github.lightman314.lightmanscurrency.common.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.SpawnTrackerCapability;
import io.github.lightman314.lightmanscurrency.common.events.DroplistConfigGenerator;
import io.github.lightman314.lightmanscurrency.integration.alexsmobs.LCAlexsMobs;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.storage.loot.LootPool.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class LootManager {

	public static void registerDroplistListeners()
	{
		//Register vanilla entry listeners
		DroplistConfigGenerator.registerEntityListener(LootManager::AddDefaultEntityEntries);
		DroplistConfigGenerator.registerChestListener(LootManager::AddDefaultChestEntries);
		//Register compat listeners
		LCAlexsMobs.registerDroplistListeners();
	}

	public static final float LOOTING_MODIFIER = 0.01f;

	public enum EntityPoolLevel
	{
		T1(false),
		T2(false),
		T3(false),
		T4(false),
		T5(false),
		T6(false),
		BOSS_T1(true),
		BOSS_T2(true),
		BOSS_T3(true),
		BOSS_T4(true),
		BOSS_T5(true),
		BOSS_T6(true);

		public final boolean isBoss;
		EntityPoolLevel(boolean isBoss) { this.isBoss = isBoss; }

	}

	public enum ChestPoolLevel { T1,T2,T3,T4,T5,T6 }

	public static MobSpawnType deserializeSpawnReason(String reasonString)
	{
		return deserializeSpawnReason(reasonString, MobSpawnType.NATURAL);
	}

	public static MobSpawnType deserializeSpawnReason(String reasonString, MobSpawnType defaultReason)
	{
		for(MobSpawnType reason : MobSpawnType.values())
		{
			if(reason.toString().contentEquals(reasonString))
				return reason;
		}
		LightmansCurrency.LogWarning("Reason string \"" + reasonString + "\" could not be properly deserialized. Returning the default spawn reason.");
		return defaultReason;
	}

	public static void AddDefaultEntityEntries(DroplistConfigGenerator.Entity event)
	{
		switch (event.getTier())
		{
			case T1 -> {
				event.addVanillaEntry("slime");
				event.addVanillaEntry("silverfish");
			}
			case T2 -> {
				event.addVanillaEntry("zombie");
				event.addVanillaEntry("skeleton");
				event.addVanillaEntry("creeper");
				event.addVanillaEntry("spider");
				event.addVanillaEntry("cave_spider");
				event.addVanillaEntry("husk");
				event.addVanillaEntry("stray");
				event.addVanillaEntry("magma_cube");
				event.addVanillaEntry("zombie_villager");
				event.addVanillaEntry("drowned");
			}
			case T3 -> {
				event.addVanillaEntry("guardian");
				event.addVanillaEntry("elder_guardian");
				event.addVanillaEntry("phantom");
				event.addVanillaEntry("blaze");
				event.addVanillaEntry("ghast");
				event.addVanillaEntry("witch");
				event.addVanillaEntry("hoglin");
				event.addVanillaEntry("piglin_brute");
				event.addVanillaEntry("piglin");
				event.addVanillaEntry("zombified_piglin");
			}
			case T4 -> {
				event.addVanillaEntry("enderman");
				event.addVanillaEntry("evoker");
				event.addVanillaEntry("vindicator");
				event.addVanillaEntry("pillager");
				event.addVanillaEntry("ravager");
				event.addVanillaEntry("shulker");
			}
			case T5 -> event.addVanillaEntry("wither_skeleton");
			case BOSS_T4 -> event.addVanillaEntry("warden");
			case BOSS_T5 -> event.addVanillaEntry("ender_dragon");
			case BOSS_T6 -> event.addVanillaEntry("wither");
		}
	}

	public static void AddDefaultChestEntries(DroplistConfigGenerator.Chest event)
	{
		LightmansCurrency.LogDebug("Adding default vanilla chest entries of tier '" + event.getTier().toString() + "'");
		switch (event.getTier())
		{
			case T1 -> {
				event.addVanillaEntry("underwater_ruin_small");
				event.addVanillaEntry("underwater_ruin_big");
			}
			case T3 -> {
				event.addVanillaEntry("jungle_temple");
				event.addVanillaEntry("nether_bridge");
				event.addVanillaEntry("simple_dungeon");
				event.addVanillaEntry("ruined_portal");
			}
			case T4 -> {
				event.addVanillaEntry("stronghold_crossing");
				event.addVanillaEntry("stronghold_corridor");
				event.addVanillaEntry("stronghold_library");
				event.addVanillaEntry("ancient_city");

			}
			case T5 -> {
				event.addVanillaEntry("buried_treasure");
				event.addVanillaEntry("bastion_hoglin_stable");
				event.addVanillaEntry("bastion_bridge");
				event.addVanillaEntry("bastion_other");
				event.addVanillaEntry("bastion_treasure");
				event.addVanillaEntry("end_city_treasure");
			}
		}
	}

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
		ENTITY_LOOT_T1 = GenerateEntityCoinPool(lootItem1, 1, 10, 0.75f, true);
		ENTITY_LOOT_T2 = GenerateEntityCoinPool(lootItem2, 1, 5, 0.5f, true);
		ENTITY_LOOT_T3 = GenerateEntityCoinPool(lootItem3, 1, 5, 0.25f, true);
		ENTITY_LOOT_T4 = GenerateEntityCoinPool(lootItem4, 1, 3, 0.1f, true);
		ENTITY_LOOT_T5 = GenerateEntityCoinPool(lootItem5, 1, 3, 0.05f, true);
		ENTITY_LOOT_T6 = GenerateEntityCoinPool(lootItem6, 1, 3, 0.025F, true);

		//Boss loot
		ENTITY_LOOT_BOSS_T1 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false));
		ENTITY_LOOT_BOSS_T2 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f, false));
		ENTITY_LOOT_BOSS_T3 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f, false));
		ENTITY_LOOT_BOSS_T4 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f, false));
		ENTITY_LOOT_BOSS_T5 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem5, 10, 30, 1.0f, false));
		ENTITY_LOOT_BOSS_T6 = ImmutableList.of(GenerateEntityCoinPool(lootItem1, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem2, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem3, 10,30,1.0f, false), GenerateEntityCoinPool(lootItem4, 10,30,1.0f, false),GenerateEntityCoinPool(lootItem5, 10, 30, 1.0f, false),GenerateEntityCoinPool(lootItem6, 1, 5, 1.0f, false));

		//Chest loot
		CHEST_LOOT_T1 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER}, 1, 5);
		CHEST_LOOT_T2 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON}, 1, 5);
		CHEST_LOOT_T3 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD}, 2, 6);
		CHEST_LOOT_T4 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD}, 3, 6);
		CHEST_LOOT_T5 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND}, 3, 6);
		CHEST_LOOT_T6 = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND, ChestLootEntryData.NETHERITE}, 3, 6);

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
	public static void onEntitySpawned(MobSpawnEvent.FinalizeSpawn event)
	{
		LivingEntity entity = event.getEntity();
		if(entity instanceof Player)
			return;

		ISpawnTracker tracker = SpawnTrackerCapability.lazyGetSpawnerTracker(entity);
		if(tracker == null)
			LightmansCurrency.LogDebug("Entity of type '" + ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString() + "' does not have a ISpawnTracker attached. Unable to flag it's SpawnReason.");
		else
			tracker.setSpawnReason(event.getSpawnType());
	}

	@SubscribeEvent
	public static void attachSpawnTrackerCapability(AttachCapabilitiesEvent<Entity> event)
	{
		//Attach the spawn trader capability to all LivingEntities that aren't players
		if(event.getObject() instanceof Mob)
		{
			event.addCapability(CurrencyCapabilities.ID_SPAWN_TRACKER, SpawnTrackerCapability.createProvider((LivingEntity)event.getObject()));
		}
	}

	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		//Check if this is the server
		if(entity.level().isClientSide)
			return;

		if(!Config.COMMON.enableSpawnerEntityDrops.get())
		{
			//Spawner drops aren't allowed. Check if the entity was spawner-spawned
			ISpawnTracker tracker = SpawnTrackerCapability.lazyGetSpawnerTracker(entity);
			if(tracker != null && tracker.spawnReason() == MobSpawnType.SPAWNER)
			{
				LightmansCurrency.LogDebug(entity.getName().getString() + " did not drop coins, as it was spawned by a spawner.");
				return;
			}
		}

		String name = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();

		if(event.getSource().getDirectEntity() instanceof Player || event.getSource().getEntity() instanceof Player)
		{
			//Assign the player that killed it
			final Player player = event.getSource().getDirectEntity() instanceof Player ? (Player)event.getSource().getDirectEntity() : (Player)event.getSource().getEntity();

			//Block coin drops if the killer was a fake player and fake player coin drops aren't allowed.
			if(player instanceof FakePlayer && !Config.COMMON.allowFakePlayerCoinDrops.get())
				return;

			if(Config.COMMON.entityDropsT1.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T1);
			}
			else if(Config.COMMON.entityDropsT2.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T2);
			}
			else if(Config.COMMON.entityDropsT3.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T3);
			}
			else if(Config.COMMON.entityDropsT4.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T4);
			}
			else if(Config.COMMON.entityDropsT5.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T5);
			}
			else if(Config.COMMON.entityDropsT6.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.T6);
			}
			else if(Config.COMMON.bossEntityDropsT1.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T1);
			}
			else if(Config.COMMON.bossEntityDropsT2.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T2);
			}
			else if(Config.COMMON.bossEntityDropsT3.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T3);
			}
			else if(Config.COMMON.bossEntityDropsT4.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T4);
			}
			else if(Config.COMMON.bossEntityDropsT5.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T5);
			}
			else if(Config.COMMON.bossEntityDropsT6.get().contains(name))
			{
				DropEntityLoot(entity, player, EntityPoolLevel.BOSS_T6);
			}
			return;
		}
		//Boss deaths don't require a player kill to drop coins
		if(Config.COMMON.bossEntityDropsT1.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T1);
		}
		else if(Config.COMMON.bossEntityDropsT2.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T2);
		}
		else if(Config.COMMON.bossEntityDropsT3.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T3);
		}
		else if(Config.COMMON.bossEntityDropsT4.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T4);
		}
		else if(Config.COMMON.bossEntityDropsT5.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T5);
		}
		else if(Config.COMMON.bossEntityDropsT6.get().contains(name))
		{
			DropEntityLoot(entity, null, EntityPoolLevel.BOSS_T6);
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

	private static String getSafeId(@Nonnull Entity entity) {
		ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		if(id == null)
			return "null";
		return id.toString().replace(':','_');
	}

	private static void DropEntityLoot(Entity entity, Player player, EntityPoolLevel coinPool)
	{

		if(!Config.COMMON.enableEntityDrops.get())
			return;

		confirmLootTablesGenerated();

		//LightmansCurrency.LOGGER.info("Dropping entity loot level " + coinPool);

		LootTable.Builder tableBuilder = LootTable.lootTable();
		LootParams.Builder parameterBuilder = new LootParams.Builder((ServerLevel)entity.level());
		//Add the KilledByPlayer condition to the Loot Context
		if(player != null)
			parameterBuilder.withParameter(LootContextParams.KILLER_ENTITY, player)
					.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);

		LootParams params = parameterBuilder.create(new LootContextParamSet.Builder().optional(LootContextParams.LAST_DAMAGE_PLAYER).optional(LootContextParams.KILLER_ENTITY).build());
		LootContext context = new LootContext.Builder(params).create(new ResourceLocation(LightmansCurrency.MODID, "generated_entity_loot/" + getSafeId(entity)));

		try {

			//Boss loot done separately due to loops and exclusiveness.
			if(coinPool == EntityPoolLevel.BOSS_T1)
			{
				//Drop copper boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T1)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}
			else if(coinPool == EntityPoolLevel.BOSS_T2)
			{
				//Drop iron boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T2)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}
			else if(coinPool == EntityPoolLevel.BOSS_T3)
			{
				//Drop gold boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T3)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}
			else if(coinPool == EntityPoolLevel.BOSS_T4)
			{
				//Drop emerald boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T4)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}
			else if(coinPool == EntityPoolLevel.BOSS_T5)
			{
				//Drop diamond boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T5)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}
			else if(coinPool == EntityPoolLevel.BOSS_T6)
			{
				//Drop netherite boss loot
				for(Builder builder : ENTITY_LOOT_BOSS_T6)
					tableBuilder.withPool(builder);
				//Generate the loot
				tableBuilder.build().getRandomItems(context, LootSpawner(entity));
				return;
			}

			//LightmansCurrency.LOGGER.debug("Added " + coinPool + " level entity loot to the " + name + " loot entry.");
			tableBuilder.withPool(ENTITY_LOOT_T1);
			if(coinPool != EntityPoolLevel.T1)
			{
				tableBuilder.withPool(ENTITY_LOOT_T2);
				if(coinPool != EntityPoolLevel.T2)
				{
					tableBuilder.withPool(ENTITY_LOOT_T3);
					if(coinPool != EntityPoolLevel.T3)
					{
						tableBuilder.withPool(ENTITY_LOOT_T4);
						if(coinPool != EntityPoolLevel.T4)
						{
							tableBuilder.withPool(ENTITY_LOOT_T5);
							if(coinPool != EntityPoolLevel.T5)
								tableBuilder.withPool(ENTITY_LOOT_T6);
						}
					}
				}
			}

			tableBuilder.build().getRandomItems(context, LootSpawner(entity));

		} catch(Exception e) { LightmansCurrency.LogError("Error spawning coin drops!", e); }

	}

	public static List<ItemStack> GetRandomChestLoot(ChestPoolLevel coinPool, LootContext context) {

		confirmLootTablesGenerated();

		try {

			if(coinPool == ChestPoolLevel.T1)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T1);
				return safelyGetResults(table, context);
			}
			else if(coinPool == ChestPoolLevel.T2)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T2);
				return safelyGetResults(table, context);
			}
			else if(coinPool == ChestPoolLevel.T3)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T3);
				return safelyGetResults(table, context);
			}
			else if(coinPool == ChestPoolLevel.T4)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T4);
				return safelyGetResults(table, context);
			}
			else if(coinPool == ChestPoolLevel.T5)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T5);
				return safelyGetResults(table, context);
			}
			else if(coinPool == ChestPoolLevel.T6)
			{
				LootTable.Builder table = LootTable.lootTable();
				table.withPool(CHEST_LOOT_T6);
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
	private static List<ItemStack> safelyGetResults(LootTable.Builder table, LootContext context) {
		List<ItemStack> results = new ArrayList<>();
		//Call getRandomItems(LootContext,Consumer<ItemStack>) to keep it from being modified by the GLM's and getting stuck in an infinite loop.
		table.build().getRandomItems(context, results::add);
		return results;
	}

	public static ChestPoolLevel GetChestPoolLevel(String lootTable) {
		if(Config.COMMON.chestDropsT1.get().contains(lootTable))
			return ChestPoolLevel.T1;
		if(Config.COMMON.chestDropsT2.get().contains(lootTable))
			return ChestPoolLevel.T2;
		if(Config.COMMON.chestDropsT3.get().contains(lootTable))
			return ChestPoolLevel.T3;
		if(Config.COMMON.chestDropsT4.get().contains(lootTable))
			return ChestPoolLevel.T4;
		if(Config.COMMON.chestDropsT5.get().contains(lootTable))
			return ChestPoolLevel.T5;
		if(Config.COMMON.chestDropsT6.get().contains(lootTable))
			return ChestPoolLevel.T6;
		return null;
	}

	private static Consumer<ItemStack> LootSpawner(Entity entity) { return i -> InventoryUtil.dumpContents(entity.level(), entity.blockPosition(), i); }


	private static Builder GenerateEntityCoinPool(Item item, float min, float max, float chance, boolean requirePlayerKill)
	{

		Builder lootPoolBuilder = LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0f, 1f))));

		//Require that the player killed it (usually only disabled for bosses)
		if(requirePlayerKill)
			lootPoolBuilder.when(LootItemKilledByPlayerCondition.killedByPlayer());
		//Add a random chance to the loot (if applicable, usually only disabled for bosses)
		if(chance < 1.0f)
			lootPoolBuilder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(chance, LOOTING_MODIFIER));

		return lootPoolBuilder;

	}


	private static Builder GenerateChestCoinPool(ChestLootEntryData[] lootEntries, float minRolls, float maxRolls)
	{

		Builder lootPoolBuilder = LootPool.lootPool()
				.setRolls(UniformGenerator.between(minRolls, maxRolls));

		//Add each loot entry
		for(ChestLootEntryData entry : lootEntries)
		{
			lootPoolBuilder.add(LootItem.lootTableItem(entry.item.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(entry.minCount, entry.maxCount))).setWeight(entry.weight));
		}

		return lootPoolBuilder;

	}

	private record ChestLootEntryData(Supplier<Item> item, float minCount, float maxCount, int weight) {

		public static ChestLootEntryData COPPER = new ChestLootEntryData(Config.COMMON.lootItem1, 1, 10, 1);
		public static ChestLootEntryData IRON = new ChestLootEntryData(Config.COMMON.lootItem2, 1, 10, 2);
		public static ChestLootEntryData GOLD = new ChestLootEntryData(Config.COMMON.lootItem3, 1, 10, 3);
		public static ChestLootEntryData EMERALD = new ChestLootEntryData(Config.COMMON.lootItem4, 1, 10, 4);
		public static ChestLootEntryData DIAMOND = new ChestLootEntryData(Config.COMMON.lootItem5, 1, 8, 5);
		public static ChestLootEntryData NETHERITE = new ChestLootEntryData(Config.COMMON.lootItem6, 1, 3, 6);

	}

}
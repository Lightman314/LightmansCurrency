package io.github.lightman314.lightmanscurrency.common.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.SpawnTrackerCapability;
import io.github.lightman314.lightmanscurrency.common.events.DroplistConfigGenerator;
import io.github.lightman314.lightmanscurrency.common.loot.tiers.*;
import io.github.lightman314.lightmanscurrency.integration.alexsmobs.LCAlexsMobs;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class LootManager {

	//public static final LootContextParamSet ENTITY_PARAMS = new LootContextParamSet.Builder().optional(LootContextParams.KILLER_ENTITY).build();


	public static void registerDroplistListeners()
	{
		//Register vanilla entry listeners
		DroplistConfigGenerator.registerEntityListener(LootManager::AddDefaultEntityEntries);
		DroplistConfigGenerator.registerChestListener(LootManager::AddDefaultChestEntries);
		//Register compat listeners
		LCAlexsMobs.registerDroplistListeners();
	}

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

		Player player = null;
		if(event.getSource().getDirectEntity() instanceof Player || event.getSource().getEntity() instanceof Player)
		{
			player = event.getSource().getDirectEntity() instanceof Player p ? p : (Player)event.getSource().getEntity();
			//Block coin drops if the killer was a fake player and fake player coin drops aren't allowed.
			//if(player instanceof FakePlayer && !Config.COMMON.allowFakePlayerCoinDrops.get())
			//	return;
		}

		EntityPoolLevel poolLevel = GetEntityPoolLevel(entity);
		if(poolLevel != null)
			DropEntityLoot(entity, player, event, poolLevel);

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

	@SuppressWarnings("deprecation")
	public static List<ItemStack> safelyGetLoot(LootTable table, LootContext context) {
		List<ItemStack> results = new ArrayList<>();
		//Call getRandomItems(LootContext,Consumer<ItemStack>) to keep it from being modified by the GLM's and getting stuck in an infinite loop.
		table.getRandomItemsRaw(context, results::add);
		return results;
	}

	public static List<ItemStack> getLoot(@Nonnull ResourceLocation lootTable, @Nonnull LootContext context)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null)
			return new ArrayList<>();
		LootTable table = server.getLootData().getLootTable(lootTable);
		if(table == null)
			return new ArrayList<>();
		//Filter results
		return safelyGetLoot(table, context);
	}
	private static void DropEntityLoot(@Nonnull Entity entity, @Nullable Player player, @Nonnull LivingDeathEvent event, @Nonnull EntityPoolLevel coinPool)
	{

		if(!Config.COMMON.enableEntityDrops.get())
			return;

		if(!coinPool.isBoss && player == null)
			return;

		LootContext context = generateEntityContext(entity, player, event);

		InventoryUtil.dumpContents(entity.level(), entity.blockPosition(), getLoot(coinPool.lootTable, context));

	}

	public static LootContext generateEntityContext(@Nonnull Entity entity, @Nullable Player player, @Nonnull LivingDeathEvent event)
	{
		if(!(entity.level() instanceof ServerLevel level))
			throw new IllegalArgumentException("Function must be run on the server side!");
		LootParams.Builder parameterBuilder = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
				.withOptionalParameter(LootContextParams.KILLER_ENTITY, event.getSource().getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, event.getSource().getDirectEntity())
				.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);

		LootParams params = parameterBuilder.create(LootContextParamSets.ENTITY);
		return new LootContext.Builder(params).create(Optional.empty());
	}

	@Nullable
	public static ChestPoolLevel GetChestPoolLevel(@Nonnull String lootTable) {
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

	@Nullable
	public static EntityPoolLevel GetEntityPoolLevel(@Nonnull Entity entity) {
		ResourceLocation entityID = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		if(entityID == null)
			return null;
		String id = entityID.toString();
		if(Config.COMMON.entityDropsT1.get().contains(id))
			return EntityPoolLevel.T1;
		if(Config.COMMON.entityDropsT2.get().contains(id))
			return EntityPoolLevel.T2;
		if(Config.COMMON.entityDropsT3.get().contains(id))
			return EntityPoolLevel.T3;
		if(Config.COMMON.entityDropsT4.get().contains(id))
			return EntityPoolLevel.T4;
		if(Config.COMMON.entityDropsT5.get().contains(id))
			return EntityPoolLevel.T5;
		if(Config.COMMON.entityDropsT6.get().contains(id))
			return EntityPoolLevel.T6;
		if(Config.COMMON.bossEntityDropsT1.get().contains(id))
			return EntityPoolLevel.BOSS_T1;
		if(Config.COMMON.bossEntityDropsT2.get().contains(id))
			return EntityPoolLevel.BOSS_T2;
		if(Config.COMMON.bossEntityDropsT3.get().contains(id))
			return EntityPoolLevel.BOSS_T3;
		if(Config.COMMON.bossEntityDropsT4.get().contains(id))
			return EntityPoolLevel.BOSS_T4;
		if(Config.COMMON.bossEntityDropsT5.get().contains(id))
			return EntityPoolLevel.BOSS_T5;
		if(Config.COMMON.bossEntityDropsT6.get().contains(id))
			return EntityPoolLevel.BOSS_T6;
		return null;
	}

}
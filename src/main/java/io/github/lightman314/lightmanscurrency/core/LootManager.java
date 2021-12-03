package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootPool.Builder;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.minecraft.loot.functions.LootingEnchantBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LootManager {

	public static final float LOOTING_MODIFIER = 0.01f;
	
	public enum PoolLevel { COPPER, IRON, GOLD, EMERALD, DIAMOND, NETHERITE, BOSS_COPPER, BOSS_IRON, BOSS_GOLD, BOSS_EMERALD, BOSS_DIAMOND, BOSS_NETHERITE };
	
	private static List<String> NON_LOOT_TABLE_ENTITIES = null;
	
	private static final String ENTITY = "minecraft:entities/";
	private static final String CHEST = "minecraft:chests/";
	
	public static final List<String> ENTITY_COPPER_DROPLIST = ImmutableList.of(ENTITY + "slime", ENTITY + "silverfish");
	public static final List<String> ENTITY_IRON_DROPLIST = ImmutableList.of(ENTITY + "zombie", ENTITY + "skeleton", ENTITY + "creeper", ENTITY + "spider", ENTITY + "cave_spider", ENTITY + "husk", ENTITY + "stray", ENTITY + "magma_cube", ENTITY + "zombie_villager", ENTITY + "drowned");
	public static final List<String> ENTITY_GOLD_DROPLIST = ImmutableList.of(ENTITY + "guardian", ENTITY + "elder_guardian", ENTITY + "phantom", ENTITY + "blaze", ENTITY + "ghast", ENTITY + "witch", ENTITY + "hoglin", ENTITY + "piglin_brute", ENTITY + "piglin", ENTITY + "zombified_piglin");
	public static final List<String> ENTITY_EMERALD_DROPLIST = ImmutableList.of(ENTITY + "enderman", ENTITY + "evoker", ENTITY + "vindicator", ENTITY + "pillager", ENTITY + "ravager", ENTITY + "shulker");
	public static final List<String> ENTITY_DIAMOND_DROPLIST = ImmutableList.of(ENTITY + "wither_skeleton");
	public static final List<String> ENTITY_NETHERITE_DROPLIST = ImmutableList.of();
	
	public static final List<String> ENTITY_BOSS_COPPER_DROPLIST = ImmutableList.of();
	public static final List<String> ENTITY_BOSS_IRON_DROPLIST = ImmutableList.of();
	public static final List<String> ENTITY_BOSS_GOLD_DROPLIST = ImmutableList.of();
	public static final List<String> ENTITY_BOSS_EMERALD_DROPLIST = ImmutableList.of();
	public static final List<String> ENTITY_BOSS_DIAMOND_DROPLIST = ImmutableList.of(ENTITY + "ender_dragon");
	public static final List<String> ENTITY_BOSS_NETHERITE_DROPLIST = ImmutableList.of(ENTITY + "wither");
	
	public static final List<String> CHEST_COPPER_DROPLIST = ImmutableList.of(CHEST + "underwater_ruin_small", CHEST + "underwater_ruin_big");
	public static final List<String> CHEST_IRON_DROPLIST = ImmutableList.of();
	public static final List<String> CHEST_GOLD_DROPLIST = ImmutableList.of(CHEST + "jungle_temple", CHEST + "nether_bridge", CHEST + "simple_dungeon", CHEST + "ruined_portal");
	public static final List<String> CHEST_EMERALD_DROPLIST = ImmutableList.of(CHEST + "stronghold_crossing", CHEST + "stronghold_corridor", CHEST + "stronghold_library");
	public static final List<String> CHEST_DIAMOND_DROPLIST = ImmutableList.of(CHEST + "buried_treasure", CHEST + "bastion_hoglin_stable", CHEST + "bastion_bridge", CHEST + "bastion_other", CHEST + "bastion_treasure", CHEST + "end_city_treasure");
	public static final List<String> CHEST_NETHERITE_DROPLIST = ImmutableList.of();
	
	private static final List<Pair<String,PoolLevel>> EXTERNAL_ENTITY_ENTRIES = new ArrayList<>();
	private static final List<Pair<String,PoolLevel>> EXTERNAL_CHEST_ENTRIES = new ArrayList<>();
	
	//Normal entity loot
	private static final Builder ENTITY_LOOT_COPPER = GenerateEntityCoinPool(ModItems.COIN_COPPER, 1, 10, 0.75f, "lightmanscurrency:entityloot_copper", true);
	private static final Builder ENTITY_LOOT_IRON = GenerateEntityCoinPool(ModItems.COIN_IRON, 1, 5, 0.5f, "lightmanscurrency:entityloot_iron", true);
	private static final Builder ENTITY_LOOT_GOLD = GenerateEntityCoinPool(ModItems.COIN_GOLD, 1, 5, 0.25f, "lightmanscurrency:entityloot_gold", true);
	private static final Builder ENTITY_LOOT_EMERALD = GenerateEntityCoinPool(ModItems.COIN_EMERALD, 1, 3, 0.1f, "lightmanscurrency:entityloot_emerald", true);
	private static final Builder ENTITY_LOOT_DIAMOND = GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 1, 3, 0.05f, "lightmanscurrency:entityloot_diamond", true);
	private static final Builder ENTITY_LOOT_NETHERITE = GenerateEntityCoinPool(ModItems.COIN_NETHERITE, 1, 3, 0.025F, "lightmanscurrency:entityloot_netherite", true);
	
	//Boss loot
	private static final List<Builder> ENTITY_LOOT_BOSS_COPPER = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false));
	private static final List<Builder> ENTITY_LOOT_BOSS_IRON = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false));
	private static final List<Builder> ENTITY_LOOT_BOSS_GOLD = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false));
	private static final List<Builder> ENTITY_LOOT_BOSS_EMERALD = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false));
	private static final List<Builder> ENTITY_LOOT_BOSS_DIAMOND = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false));
	private static final List<Builder> ENTITY_LOOT_BOSS_NETHERITE = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false),GenerateEntityCoinPool(ModItems.COIN_NETHERITE, 1, 5, 1.0f, "lightmanscurrency:coinloot_boss_netherite", false));
	
	//Chest loot
	private static final Builder CHEST_LOOT_COPPER = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER}, 1, 5, "lightmanscurrency:chestloot_copper");
	private static final Builder CHEST_LOOT_IRON = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON}, 1, 5, "lightmanscurrency:chestloot_iron");
	private static final Builder CHEST_LOOT_GOLD = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD}, 2, 6, "lightmanscurrency:chestloot_gold");
	private static final Builder CHEST_LOOT_EMERALD = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD}, 3, 6, "lightmanscurrency:chestloot_emerald");
	private static final Builder CHEST_LOOT_DIAMOND = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND}, 3, 6, "lightmanscurrency:chestloot_diamond");
	private static final Builder CHEST_LOOT_NETHERITE = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND, ChestLootEntryData.NETHERITE}, 3, 6, "lightmanscurrency:chestloot_netherite");
	
	@SubscribeEvent
    public static void onLootTablesLoaded(LootTableLoadEvent event)
    {
		if(NON_LOOT_TABLE_ENTITIES == null)
			initNonLootTableEntities();
		
    	String name = event.getName().toString();
    	nonLootTableEntityRemovalCheck(name);
    	LootTable table = event.getTable();
    	//Normal Entity Drops
    	if(Config.COMMON.copperEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded copper entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.COPPER, name);
    	}
    	else if(Config.COMMON.ironEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded iron entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.IRON, name);
    	}
    	else if(Config.COMMON.goldEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded gold entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.GOLD, name);
    	}
    	else if(Config.COMMON.emeraldEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded emerald entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.EMERALD, name);
    	}
    	else if(Config.COMMON.diamondEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded diamond entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.DIAMOND, name);
    	}
    	else if(Config.COMMON.netheriteEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded netherite entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.NETHERITE, name);
    	}
    	//Boss Entity Drops
    	else if(Config.COMMON.bossCopperEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss copper entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_COPPER, name);
    	}
    	else if(Config.COMMON.bossIronEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss iron entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_IRON, name);
    	}
    	else if(Config.COMMON.bossGoldEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss gold entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_GOLD, name);
    	}
    	else if(Config.COMMON.bossEmeraldEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss emerald entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_EMERALD, name);
    	}
    	else if(Config.COMMON.bossDiamondEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss diamond entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_DIAMOND, name);
    	}
    	else if(Config.COMMON.bossNetheriteEntityDrops.get().contains(name))
    	{
    		//CurrencyMod.LOGGER.info("Loaded boss netherite entity drop for entity '" + name + "' from the config.");
    		AddEntityPoolToTable(table, PoolLevel.BOSS_NETHERITE, name);
    	}
    	//Chest Drops
    	else if(Config.COMMON.copperChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.COPPER, name);
    	}
    	else if(Config.COMMON.ironChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.IRON, name);
    	}
    	else if(Config.COMMON.goldChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.GOLD, name);
    	}
    	else if(Config.COMMON.emeraldChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.EMERALD, name);
    	}
    	else if(Config.COMMON.diamondChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.DIAMOND, name);
    	}
    	else if(Config.COMMON.netheriteChestDrops.get().contains(name))
    	{
    		AddChestPoolToTable(table, PoolLevel.NETHERITE, name);
    	}
    	else
    	{
    		//External entries
    		for(Pair<String,PoolLevel> pair : EXTERNAL_ENTITY_ENTRIES)
    		{
    			if(pair.getFirst() == name)
    			{
    				AddEntityPoolToTable(table, pair.getSecond(), name);
    			}
    		}
    		for(Pair<String,PoolLevel> pair : EXTERNAL_CHEST_ENTRIES)
    		{
    			if(pair.getFirst() == name)
    			{
    				AddChestPoolToTable(table, pair.getSecond(), name);
    			}
    		}
    	}
    }
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event)
	{
		//Check if this is the server
		if(event.getEntityLiving().world.isRemote)
			return;
		
		/*String debugString = "";
		for(int i = 0; i < NON_LOOT_TABLE_ENTITIES.size(); i++)
		{
			String entity = NON_LOOT_TABLE_ENTITIES.get(i);
			debugString += entity;
			if(i < NON_LOOT_TABLE_ENTITIES.size() - 1)
				debugString += ", ";
		}
		LightmansCurrency.LOGGER.info("NON_LOOT_TABLE_ENTITIES: " + debugString);*/
		
		String name = event.getEntityLiving().getEntityString();
		//LightmansCurrency.LOGGER.info("Entity with id '" + name + "' just died.");
		if(NON_LOOT_TABLE_ENTITIES.contains(name))
		{
			//LightmansCurrency.LOGGER.info("Entity with id '" + name + "' is on the non-loot table coin drop list.");
			PlayerEntity player = null;
			
			if(event.getSource().getImmediateSource() instanceof PlayerEntity || event.getSource().getTrueSource() instanceof PlayerEntity)
			{
				//LightmansCurrency.LOGGER.info("Entity with id '" + name + "' was indeed killed by a player.");
				
				//Assign the player that killed it
				if(event.getSource().getImmediateSource() instanceof PlayerEntity)
					player = (PlayerEntity)event.getSource().getImmediateSource();
				else
					player = (PlayerEntity)event.getSource().getTrueSource();
				
				
				if(Config.COMMON.copperEntityDrops.get().contains(name))
		    	{
					DropEntityLoot(event.getEntityLiving(), player, PoolLevel.COPPER);
		    	}
		    	else if(Config.COMMON.ironEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.IRON);
		    	}
		    	else if(Config.COMMON.goldEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.GOLD);
		    	}
		    	else if(Config.COMMON.emeraldEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.EMERALD);
		    	}
		    	else if(Config.COMMON.diamondEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.DIAMOND);
		    	}
		    	else if(Config.COMMON.netheriteEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.NETHERITE);
		    	}
		    	else if(Config.COMMON.bossCopperEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_COPPER);
		    	}
		    	else if(Config.COMMON.bossIronEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_IRON);
		    	}
		    	else if(Config.COMMON.bossGoldEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_GOLD);
		    	}
		    	else if(Config.COMMON.bossEmeraldEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_EMERALD);
		    	}
		    	else if(Config.COMMON.bossDiamondEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_DIAMOND);
		    	}
		    	else if(Config.COMMON.bossNetheriteEntityDrops.get().contains(name))
		    	{
		    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_NETHERITE);
		    	}
		    	else
		    	{
		    		for(Pair<String,PoolLevel> pair : EXTERNAL_ENTITY_ENTRIES)
		    		{
		    			if(pair.getFirst() == name && pair.getSecond() != PoolLevel.BOSS_COPPER &&pair.getSecond() != PoolLevel.BOSS_IRON &&pair.getSecond() != PoolLevel.BOSS_GOLD &&pair.getSecond() != PoolLevel.BOSS_EMERALD && pair.getSecond() != PoolLevel.BOSS_DIAMOND && pair.getSecond() != PoolLevel.BOSS_NETHERITE)
		    			{
		    				DropEntityLoot(event.getEntityLiving(), player, pair.getSecond());
		    			}
		    		}
		    	}
			}
			//Boss deaths don't require a player kill to drop coins
	    	if(Config.COMMON.bossCopperEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_COPPER);
	    	}
	    	else if(Config.COMMON.bossIronEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_IRON);
	    	}
	    	else if(Config.COMMON.bossGoldEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_GOLD);
	    	}
	    	else if(Config.COMMON.bossEmeraldEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_EMERALD);
	    	}
	    	else if(Config.COMMON.bossDiamondEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_DIAMOND);
	    	}
	    	else if(Config.COMMON.bossNetheriteEntityDrops.get().contains(name))
	    	{
	    		DropEntityLoot(event.getEntityLiving(), player, PoolLevel.BOSS_NETHERITE);
	    	}
	    	else
	    	{
	    		for(Pair<String,PoolLevel> pair : EXTERNAL_ENTITY_ENTRIES)
	    		{
	    			if(pair.getFirst() == name && (pair.getSecond() == PoolLevel.BOSS_COPPER || pair.getSecond() == PoolLevel.BOSS_IRON || pair.getSecond() == PoolLevel.BOSS_GOLD || pair.getSecond() == PoolLevel.BOSS_EMERALD || pair.getSecond() == PoolLevel.BOSS_DIAMOND || pair.getSecond() == PoolLevel.BOSS_NETHERITE))
	    			{
	    				DropEntityLoot(event.getEntityLiving(), player, pair.getSecond());
	    			}
	    		}
	    	}
			
		}
	}
	
	private static void initNonLootTableEntities()
	{
		NON_LOOT_TABLE_ENTITIES = new ArrayList<>();
		for(String entity : Config.COMMON.copperEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.ironEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.goldEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.emeraldEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.diamondEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.netheriteEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossCopperEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossIronEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossGoldEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossEmeraldEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossDiamondEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
		for(String entity : Config.COMMON.bossNetheriteEntityDrops.get())
		{
			NON_LOOT_TABLE_ENTITIES.add(entity);
			//LightmansCurrency.LOGGER.info(entity + " was added to the non-loot table entity list in init.");
		}
	}
	
	private static void nonLootTableEntityRemovalCheck(String entity)
	{
		if(NON_LOOT_TABLE_ENTITIES.contains(entity))
		{
			NON_LOOT_TABLE_ENTITIES.remove(entity);
			//LightmansCurrency.LOGGER.info(entity + " was removed from the non-loot table entity list.");
		}
	}
	
	private static void DropEntityLoot(Entity entity, PlayerEntity player, PoolLevel coinPool)
	{
		
		if(!Config.COMMON.enableEntityDrops.get())
			return;
		
		//LightmansCurrency.LOGGER.info("Dropping entity loot level " + coinPool);
		
		LootTable table = LootTable.builder().build();
		LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld)entity.world);
		//Add the KilledByPlayer condition to the Loot Context
		if(player != null)
			contextBuilder.withParameter(LootParameters.KILLER_ENTITY, player)
			.withParameter(LootParameters.LAST_DAMAGE_PLAYER, player);
		
		LootContext context = contextBuilder.build(new LootParameterSet.Builder().optional(LootParameters.LAST_DAMAGE_PLAYER).optional(LootParameters.KILLER_ENTITY).build());
		
		//Boss loot done separately due to loops and exclusiveness.
		if(coinPool == PoolLevel.BOSS_COPPER)
		{
			//Drop copper boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_COPPER)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		else if(coinPool == PoolLevel.BOSS_IRON)
		{
			//Drop iron boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_IRON)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		else if(coinPool == PoolLevel.BOSS_GOLD)
		{
			//Drop gold boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_GOLD)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		else if(coinPool == PoolLevel.BOSS_EMERALD)
		{
			//Drop emerald boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_EMERALD)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		else if(coinPool == PoolLevel.BOSS_DIAMOND)
		{
			//Drop diamond boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_DIAMOND)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		else if(coinPool == PoolLevel.BOSS_NETHERITE)
		{
			//Drop netherite boss loot
			for(Builder builder : ENTITY_LOOT_BOSS_NETHERITE)
			{
				table.addPool(builder.build());
			}
			//Generate the loot 
			SpawnLootDrops(entity, table.generate(context));
			return;
		}
		
		//LightmansCurrency.LOGGER.debug("Added " + coinPool + " level entity loot to the " + name + " loot entry.");
		table.addPool(ENTITY_LOOT_COPPER.build());
		if(coinPool != PoolLevel.COPPER)
		{
			table.addPool(ENTITY_LOOT_IRON.build());
			if(coinPool != PoolLevel.IRON)
			{
				table.addPool(ENTITY_LOOT_GOLD.build());
				if(coinPool != PoolLevel.GOLD)
				{
					table.addPool(ENTITY_LOOT_EMERALD.build());
					if(coinPool != PoolLevel.EMERALD)
					{
						table.addPool(ENTITY_LOOT_DIAMOND.build());
						if(coinPool != PoolLevel.DIAMOND)
							table.addPool(ENTITY_LOOT_NETHERITE.build());
					}
				}
			}
		}
		
		SpawnLootDrops(entity, table.generate(context));
		
	}
	
	private static void SpawnLootDrops(Entity entity, List<ItemStack> lootDrops)
	{
		//LightmansCurrency.LOGGER.info("Spawning " + lootDrops.size() + " coin drops.");
		for(ItemStack stack : lootDrops)
		{
			InventoryHelper.spawnItemStack(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
		}
	}
	
	private static void AddEntityPoolToTable(LootTable table, PoolLevel coinPool, String name)
	{
		if(!Config.COMMON.enableEntityDrops.get())
			return;
		
		
		if(coinPool == PoolLevel.BOSS_COPPER)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_COPPER)
			{
				table.addPool(builder.build());
			}
			return;
		}
		else if(coinPool == PoolLevel.BOSS_IRON)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_IRON)
			{
				table.addPool(builder.build());
			}
			return;
		}
		else if(coinPool == PoolLevel.BOSS_GOLD)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_GOLD)
			{
				table.addPool(builder.build());
			}
			return;
		}
		else if(coinPool == PoolLevel.BOSS_EMERALD)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_EMERALD)
			{
				table.addPool(builder.build());
			}
			return;
		}
		else if(coinPool == PoolLevel.BOSS_DIAMOND)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_DIAMOND)
			{
				table.addPool(builder.build());
			}
			return;
		}
		else if(coinPool == PoolLevel.BOSS_NETHERITE)
		{
			for(Builder builder : ENTITY_LOOT_BOSS_NETHERITE)
			{
				table.addPool(builder.build());
			}
			return;
		}
		
		LightmansCurrency.LogDebug("Added " + coinPool + " level entity loot to the " + name + " loot entry.");
		table.addPool(ENTITY_LOOT_COPPER.build());
		if(coinPool == PoolLevel.COPPER)
			return;
		table.addPool(ENTITY_LOOT_IRON.build());
		if(coinPool == PoolLevel.IRON)
			return;
		table.addPool(ENTITY_LOOT_GOLD.build());
		if(coinPool == PoolLevel.GOLD)
			return;
		table.addPool(ENTITY_LOOT_EMERALD.build());
		if(coinPool == PoolLevel.EMERALD)
			return;
		table.addPool(ENTITY_LOOT_DIAMOND.build());
		if(coinPool == PoolLevel.DIAMOND)
			return;
		table.addPool(ENTITY_LOOT_NETHERITE.build());
	}
	
	private static void AddChestPoolToTable(LootTable table, PoolLevel coinPool, String name)
	{
		if(!Config.COMMON.enableChestLoot.get())
		{
			//CurrencyMod.LOGGER.info("Entity loot is disabled. Aborting adding loot to " + name);
			return;
		}
		
		LightmansCurrency.LogDebug("Added " + coinPool + " level chest loot to the " + name + " loot entry.");
		if(coinPool == PoolLevel.COPPER)
		{
			table.addPool(CHEST_LOOT_COPPER.build());
		}
		else if(coinPool == PoolLevel.IRON)
		{
			table.addPool(CHEST_LOOT_IRON.build());
		}
		else if(coinPool == PoolLevel.GOLD)
		{
			table.addPool(CHEST_LOOT_GOLD.build());
		}
		else if(coinPool == PoolLevel.EMERALD)
		{
			table.addPool(CHEST_LOOT_EMERALD.build());
		}
		else if(coinPool == PoolLevel.DIAMOND)
		{
			table.addPool(CHEST_LOOT_DIAMOND.build());
		}
		else if(coinPool == PoolLevel.NETHERITE)
		{
			table.addPool(CHEST_LOOT_NETHERITE.build());
		}
		
	}
	
	/**
	 * Adds the given entity's loot table to the list so that it will drop coins in addition to its already given loot.
	 * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:entities/zombie"), or of the entities id (e.g. "minecraft:sheep")
	 * @param coinPool The highest level coin that the entity should be allowed to drop.
	 */
	public static void AddEntityCoinPoolToTable(String resource, PoolLevel coinPool)
	{
		EXTERNAL_ENTITY_ENTRIES.add(new Pair<String,PoolLevel>(resource, coinPool));
	}
	
	/**
	 * Adds the given chest's loot table to the list so that it will spawn coins in addition to its already given loot.
	 * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:chests/buried_treasure")
	 * @param coinPool The highest level coin that the chest should spawn.
	 */
	public static void AddChestCoinPoolToTable(String resource, PoolLevel coinPool)
	{
		EXTERNAL_CHEST_ENTRIES.add(new Pair<String,PoolLevel>(resource, coinPool));
	}
	
	
	private static Builder GenerateEntityCoinPool(Item item, float min, float max, float chance, String name, boolean requirePlayerKill)
	{
		
		Builder lootPoolBuilder = LootPool.builder()
				.rolls(ConstantRange.of(1))
				.addEntry(ItemLootEntry.builder(item).acceptFunction(SetCount.builder(RandomValueRange.of(min, max))).acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
				.name(name);
		
		//Require that the player killed it (usually only disabled for bosses)
		if(requirePlayerKill)
			lootPoolBuilder.acceptCondition(KilledByPlayer.builder());
		//Add a random chance to the loot (if applicable, usually only disabled for bosses)
		if(chance < 1.0f)
			lootPoolBuilder.acceptCondition(RandomChanceWithLooting.builder(chance, LOOTING_MODIFIER));
		
		return lootPoolBuilder;
		
	}
	
	
	private static Builder GenerateChestCoinPool(ChestLootEntryData[] lootEntries, float minRolls, float maxRolls, String name)
	{
		
		Builder lootPoolBuilder = LootPool.builder()
				.rolls(RandomValueRange.of(minRolls, maxRolls))
				.name(name);
		
		//Add each loot entry
		for(ChestLootEntryData entry : lootEntries)
		{
			lootPoolBuilder.addEntry(ItemLootEntry.builder(entry.item).acceptFunction(SetCount.builder(RandomValueRange.of(entry.minCount, entry.maxCount))).weight(entry.weight));
		}
		
		return lootPoolBuilder;
		
	}
	
	private static class ChestLootEntryData
	{
		public final Item item;
		public final float minCount;
		public final float maxCount;
		public final int weight;
		
		public ChestLootEntryData(Item item, float minCount, float maxCount, int weight)
		{
			this.item = item;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.weight = weight;
		}
		
		public static ChestLootEntryData COPPER = new ChestLootEntryData(ModItems.COIN_COPPER, 1,10, 1);
		public static ChestLootEntryData IRON = new ChestLootEntryData(ModItems.COIN_IRON, 1,10, 2);
		public static ChestLootEntryData GOLD = new ChestLootEntryData(ModItems.COIN_GOLD, 1,10, 3);
		public static ChestLootEntryData EMERALD = new ChestLootEntryData(ModItems.COIN_EMERALD, 1,10, 4);
		public static ChestLootEntryData DIAMOND = new ChestLootEntryData(ModItems.COIN_DIAMOND, 1, 8, 5);
		public static ChestLootEntryData NETHERITE = new ChestLootEntryData(ModItems.COIN_NETHERITE, 1,3, 6);
		
	}
	
	
	
}

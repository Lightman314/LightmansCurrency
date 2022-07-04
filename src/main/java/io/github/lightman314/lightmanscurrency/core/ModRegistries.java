package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.crafting.RecipeTypes;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomPointsOfInterest;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.loot.LootModifiers;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRegistries {

	public static void register(IEventBus bus) {
		//Items
		ITEMS.register(bus);
		ModItems.init();
		//Blocks
		BLOCKS.register(bus);
		ModBlocks.init();
		
		//Block Entities
		BLOCK_ENTITIES.register(bus);
		ModBlockEntities.init();
		
		//Enchantments
		ENCHANTMENTS.register(bus);
		ModEnchantments.init();
		
		//Menu Types
		MENUS.register(bus);
		ModMenus.init();
		
		//Recipe Types
		RECIPE_TYPES.register(bus);
		RecipeTypes.init();
		//Recipe Serializers
		RECIPE_SERIALIZERS.register(bus);
		ModRecipes.init();
		
		//Villager Professions
		PROFESSIONS.register(bus);
		CustomProfessions.init();
		//Points of Interest
		POI_TYPES.register(bus);
		CustomPointsOfInterest.init();
		
		//Global Loot Modifiers
		GLOBAL_LOOT_MODIFIERS.register(bus);
		LootModifiers.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LightmansCurrency.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LightmansCurrency.MODID);
    
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, LightmansCurrency.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, LightmansCurrency.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, LightmansCurrency.MODID);
	
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, LightmansCurrency.MODID);
    
}

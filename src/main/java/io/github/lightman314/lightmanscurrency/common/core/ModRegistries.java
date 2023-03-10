package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.CustomPointsOfInterest;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.common.loot.LootModifiers;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.village.PointOfInterestType;
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
		//RECIPE_TYPES.register(bus);
		//RecipeTypes.init();
		//Recipe Serializers
		RECIPE_SERIALIZERS.register(bus);
		ModRecipes.init();
		
		//Villager Professions
		PROFESSIONS.register(bus);
		CustomProfessions.init();
		//Points of Interest
		POI_TYPES.register(bus);
		CustomPointsOfInterest.init();
		
		//Sound Events
		//SOUND_EVENTS.register(bus);
		//ModSounds.init();
		
		//Global Loot Modifiers
		GLOBAL_LOOT_MODIFIERS.register(bus);
		LootModifiers.init();
		
		//Command Argument Types
		//COMMAND_ARGUMENT_TYPES.register(bus);
		ModCommandArguments.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LightmansCurrency.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, LightmansCurrency.MODID);
    
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<ContainerType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, LightmansCurrency.MODID);
    
    //public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries, LightmansCurrency.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, LightmansCurrency.MODID);
    public static final DeferredRegister<PointOfInterestType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, LightmansCurrency.MODID);

    public static final DeferredRegister<GlobalLootModifierSerializer<?>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, LightmansCurrency.MODID);

    
}

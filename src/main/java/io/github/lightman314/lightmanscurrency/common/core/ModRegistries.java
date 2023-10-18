package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.Codec;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeTypes;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.CustomPointsOfInterest;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.common.loot.LootModifiers;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
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
		
		//Sound Events
		SOUND_EVENTS.register(bus);
		ModSounds.init();
		
		//Global Loot Modifiers
		GLOBAL_LOOT_MODIFIERS.register(bus);
		LootModifiers.init();
		
		//Command Argument Types
		COMMAND_ARGUMENT_TYPES.register(bus);
		ModCommandArguments.init();

		CREATIVE_TABS.register(bus);
		ModCreativeGroups.init();

		//Loot Pool Entry Types
		LOOT_POOL_ENTRY_TYPES.register(bus);
		ModLootPoolEntryTypes.init();

		//Crafting Conditions
		CRAFTING_CONDITIONS.register(bus);
		LCCraftingConditions.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LightmansCurrency.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LightmansCurrency.MODID);
    
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, LightmansCurrency.MODID);
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, LightmansCurrency.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, LightmansCurrency.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, LightmansCurrency.MODID);
	
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, LightmansCurrency.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LightmansCurrency.MODID);

	public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, LightmansCurrency.MODID);

	public static final DeferredRegister<Codec<? extends ICondition>> CRAFTING_CONDITIONS = DeferredRegister.create(ForgeRegistries.CONDITION_SERIALIZERS, LightmansCurrency.MODID);

}

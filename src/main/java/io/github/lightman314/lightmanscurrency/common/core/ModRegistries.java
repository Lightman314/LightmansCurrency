package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.common.advancements.LCAdvancementTriggers;
import io.github.lightman314.lightmanscurrency.common.core.custom.*;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeTypes;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.*;
import io.github.lightman314.lightmanscurrency.common.loot.LootModifiers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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
		ENCHANTMENT_EFFECT_COMPONENTS.register(bus);
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

		LOOT_ITEM_FUNCTION_TYPES.register(bus);
		ModLootFunctionTypes.init();

		//Attachment Types
		ATTACHMENT_TYPES.register(bus);
		ModAttachmentTypes.init();

		//Crafting Conditions
		CRAFTING_CONDITIONS.register(bus);
		LCCraftingConditions.init();

		//Data Component Types
		DATA_COMPONENTS.register(bus);
		ModDataComponents.init();

		//Criterion Triggers
		CRITERION_TRIGGERS.register(bus);
		LCAdvancementTriggers.init();

        //Stats
        CUSTOM_STAT.register(bus);
        ModStats.init();

		//LC Custom Registries
		EJECTION_DATA.register(bus);
		ModEjectionDataTypes.init();

		CUSTOM_DATA.register(bus);
		ModCustomDataTypes.init();
		
	}
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LightmansCurrency.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LightmansCurrency.MODID);
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LightmansCurrency.MODID);
    
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, LightmansCurrency.MODID);
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, LightmansCurrency.MODID);
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, LightmansCurrency.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LightmansCurrency.MODID);
    
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, LightmansCurrency.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, LightmansCurrency.MODID);
	
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, LightmansCurrency.MODID);
    
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LightmansCurrency.MODID);
    
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, LightmansCurrency.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, LightmansCurrency.MODID);

	public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, LightmansCurrency.MODID);

	public static final DeferredRegister<LootItemFunctionType<?>> LOOT_ITEM_FUNCTION_TYPES = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, LightmansCurrency.MODID);

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LightmansCurrency.MODID);

	public static final DeferredRegister<MapCodec<? extends ICondition>> CRAFTING_CONDITIONS = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, LightmansCurrency.MODID);

	public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE,LightmansCurrency.MODID);

	public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_EFFECT_COMPONENTS = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,LightmansCurrency.MODID);

	public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES,LightmansCurrency.MODID);

    public static final DeferredRegister<ResourceLocation> CUSTOM_STAT = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT,LightmansCurrency.MODID);

	//Custom Registries
	public static final DeferredRegister<EjectionDataType> EJECTION_DATA = DeferredRegister.create(LCRegistries.EJECTION_DATA_KEY,LightmansCurrency.MODID);
	public static final DeferredRegister<CustomDataType<?>> CUSTOM_DATA = DeferredRegister.create(LCRegistries.CUSTOM_DATA_KEY,LightmansCurrency.MODID);

}

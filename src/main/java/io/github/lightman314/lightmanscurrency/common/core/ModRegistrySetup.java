package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.ModCreativeGroups;
import io.github.lightman314.lightmanscurrency.common.core.custom.*;
import io.github.lightman314.lightmanscurrency.common.core.neo.ModHolderSets;
import io.github.lightman314.lightmanscurrency.common.core.neo.ModLootModifiers;
import net.neoforged.bus.api.IEventBus;

public class ModRegistrySetup {

	public static void register(IEventBus bus) {

		//Items
		ModItems.REGISTER.register(bus);
		//Blocks
		ModBlocks.REGISTER.register(bus);
		
		//Block Entities
        ModBlockEntities.REGISTER.register(bus);
		
		//Enchantments
        ModEnchantments.REGISTER.register(bus);
		
		//Menu Types
        ModMenus.REGISTER.register(bus);
		
		//Recipe Types
		ModRecipeTypes.REGISTER.register(bus);
		//Recipe Serializers
		ModRecipeSerializers.REGISTER.register(bus);
		
		//Villager Professions
		ModProfessions.REGISTER.register(bus);
		//Points of Interest
		ModPointsOfInterest.REGISTER.register(bus);
		
		//Sound Events
		ModSounds.REGISTER.register(bus);
		
		//Command Argument Types
		ModCommandArguments.REGISTER.register(bus);

        //Creative Mode Tabs
		ModCreativeGroups.REGISTER.register(bus);

		//Loot Pool Entry Types
		ModLootPoolEntryTypes.REGISTER.register(bus);

        //Loot Function Types
		ModLootFunctionTypes.REGISTER.register(bus);

		//Attachment Types
		ModAttachmentTypes.REGISTER.register(bus);

		//Crafting Conditions
		ModCraftingConditions.REGISTER.register(bus);

		//Data Component Types
		ModDataComponents.REGISTER.register(bus);

		//Criterion Triggers
		ModAdvancementTriggers.REGISTER.register(bus);

        //Stats
        ModStats.REGISTER.register(bus);

        //Neoforge Custom Registries
        ModHolderSets.REGISTER.register(bus);
        ModLootModifiers.REGISTER.register(bus);

		//LC Custom Registries
		ModEjectionDataTypes.REGISTER.register(bus);
		ModCustomDataTypes.REGISTER.register(bus);
        ModNotifications.TYPES.register(bus);
        ModNotifications.CATEGORIES.register(bus);
        ModOwnerTypes.REGISTER.register(bus);
        ModTraderTypes.REGISTER.register(bus);
        ModTraderNodes.REGISTER.register(bus);
        ModTradeRules.REGISTER.register(bus);
        ModItemTradeTypes.REGISTER.register(bus);
        ModLazyPackets.REGISTER.register(bus);
        ModIcons.REGISTER.register(bus);
        ModATMIcons.REGISTER.register(bus);
        ModBankReferences.REGISTER.register(bus);
        ModCurrencyTypes.REGISTER.register(bus);

	}

}

package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.items.colored.ColoredItem;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.integration.jeiplugin.util.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class LCJeiPlugin implements IModPlugin{

	//private static final ResourceLocation COIN_MINT_UID = VersionUtil.lcResource("coin_mint");
	public static final RecipeType<CoinMintRecipe> COIN_MINT_TYPE = RecipeType.create(LightmansCurrency.MODID, "coin_mint", CoinMintRecipe.class);
	public static final RecipeType<TicketStationRecipe> TICKET_TYPE = RecipeType.create(LightmansCurrency.MODID, "ticket_station", TicketStationRecipe.class);

	@Override
	public ResourceLocation getPluginUid() { return VersionUtil.lcResource(LightmansCurrency.MODID); }

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(new CoinMintCategory(guiHelper));
		registry.addRecipeCategories(new TicketStationCategory(guiHelper));
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		List<CoinMintRecipe> mintRecipes = RecipeValidator.getAllMintRecipes(Minecraft.getInstance().level);
		registration.addRecipes(COIN_MINT_TYPE, mintRecipes);

		List<TicketStationRecipe> ticketRecipes = RecipeValidator.getValidTicketStationRecipes(Minecraft.getInstance().level);
		registration.addRecipes(TICKET_TYPE, ticketRecipes);

		registration.addIngredientInfo(new ItemStack(ModItems.TICKET_STUB.get()), VanillaTypes.ITEM_STACK, LCText.JEI_INFO_TICKET_STUB.get());
		registration.addIngredientInfo(new ItemStack(ModItems.GOLDEN_TICKET_STUB.get()), VanillaTypes.ITEM_STACK, LCText.JEI_INFO_TICKET_STUB.get());

	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(ModItems.COIN_ANCIENT.get(),new AncientCoinSubtype());
		VariantSubtype variantSubtype = new VariantSubtype();
		for(Item item : ForgeRegistries.ITEMS)
		{
			if(VariantProvider.getVariantItem(item) != null)
				registration.registerSubtypeInterpreter(item,variantSubtype);
		}
        registration.registerSubtypeInterpreter(ModBlocks.SUS_JAR.get().asItem(),new SusJarSubtype());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{
		this.registerExclusionZones(registration, TraderScreen.class);
		this.registerExclusionZones(registration, SlotMachineScreen.class);
		this.registerExclusionZones(registration, GachaMachineScreen.class);
		this.registerExclusionZones(registration, TraderStorageScreen.class);
		this.registerExclusionZones(registration, ATMScreen.class);
		this.registerExclusionZones(registration, TaxCollectorScreen.class);
		this.registerExclusionZones(registration, CoinChestScreen.class);
		this.registerExclusionZones(registration, EjectionRecoveryScreen.class);
		this.registerExclusionZones(registration, PlayerTradeScreen.class);
		this.registerExclusionZones(registration, TraderInterfaceScreen.class);
		this.registerExclusionZones(registration, WalletScreen.class);
		this.registerExclusionZones(registration, WalletBankScreen.class);
		this.registerExclusionZones(registration, TeamManagerScreen.class);
		this.registerExclusionZones(registration, NotificationScreen.class);
		this.registerExclusionZones(registration, TransactionRegisterScreen.class);

        this.registerGhostSlots(registration,ItemFilterScreen.class);
        this.registerGhostSlots(registration,TraderStorageScreen.class);

	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(ModBlocks.COIN_MINT.get()), COIN_MINT_TYPE);
		registration.addRecipeCatalyst(new ItemStack(ModBlocks.TICKET_STATION.get()), TICKET_TYPE);
	}
	
	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(MintMenu.class, ModMenus.MINT.get(), COIN_MINT_TYPE, 0, 1, 2, 36);
		registration.addRecipeTransferHandler(TicketStationMenu.class, ModMenus.TICKET_MACHINE.get(), TICKET_TYPE, 0, 2, 3, 36);
	}

	private <T extends EasyMenuScreen<?>> void registerExclusionZones(IGuiHandlerRegistration registration, Class<T> clazz)
	{
		registration.addGuiContainerHandler(clazz,JEIScreenArea.create(clazz,registration.getJeiHelpers().getIngredientManager()));
	}

    private <T extends EasyMenuScreen<?>> void registerGhostSlots(IGuiHandlerRegistration registration, Class<T> clazz)
    {
        registration.addGhostIngredientHandler(clazz,new EasyGhostIngredientHandler<>(clazz));
    }

	private static class AncientCoinSubtype implements IIngredientSubtypeInterpreter<ItemStack>
	{
		@Override
		public String apply(ItemStack stack, UidContext uidContext) {
			AncientCoinType type = AncientCoinItem.getAncientCoinType(stack);
			if(type == null)
				type = AncientCoinType.COPPER;
			return type.toString();
		}
	}

	private static class VariantSubtype implements IIngredientSubtypeInterpreter<ItemStack>
	{
		@Override
		public String apply(ItemStack stack, UidContext uidContext) {
			ResourceLocation variant = IVariantItem.getItemVariant(stack);
			return variant == null ? "" : variant.toString();
		}
	}

    private static class SusJarSubtype implements IIngredientSubtypeInterpreter<ItemStack>
    {
        @Override
        public String apply(ItemStack stack, UidContext uidContext) {
            if(stack.getItem() instanceof ColoredItem item)
                return Integer.toHexString(item.getColor(stack));
            return Integer.toHexString(0xFFFFFF);
        }
    }
	
}

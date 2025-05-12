package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
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
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.integration.jeiplugin.util.JEIScreenArea;
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
import net.minecraft.world.item.ItemStack;

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
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration zones)
	{
		this.registerExclusionZones(zones, TraderScreen.class);
		this.registerExclusionZones(zones, SlotMachineScreen.class);
		this.registerExclusionZones(zones, GachaMachineScreen.class);
		this.registerExclusionZones(zones, TraderStorageScreen.class);
		this.registerExclusionZones(zones, ATMScreen.class);
		this.registerExclusionZones(zones, TaxCollectorScreen.class);
		this.registerExclusionZones(zones, CoinChestScreen.class);
		this.registerExclusionZones(zones, EjectionRecoveryScreen.class);
		this.registerExclusionZones(zones, PlayerTradeScreen.class);
		this.registerExclusionZones(zones, TraderInterfaceScreen.class);
		this.registerExclusionZones(zones, WalletScreen.class);
		this.registerExclusionZones(zones, WalletBankScreen.class);
		this.registerExclusionZones(zones, TeamManagerScreen.class);
		this.registerExclusionZones(zones, NotificationScreen.class);
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
	
}

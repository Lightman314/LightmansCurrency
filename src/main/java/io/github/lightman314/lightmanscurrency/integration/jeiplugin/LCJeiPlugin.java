package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator.Results;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class LCJeiPlugin implements IModPlugin{

	//private static final ResourceLocation COIN_MINT_UID = new ResourceLocation(LightmansCurrency.MODID, "coin_mint");
	public static final RecipeType<CoinMintRecipe> COIN_MINT_TYPE = RecipeType.create(LightmansCurrency.MODID, "coin_mint", CoinMintRecipe.class);
	
	@Override
	public @NotNull ResourceLocation getPluginUid() { return new ResourceLocation(LightmansCurrency.MODID, LightmansCurrency.MODID); }

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(new CoinMintCategory(guiHelper));
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		Results recipes = RecipeValidator.getValidRecipes(Minecraft.getInstance().level);
		registration.addRecipes(COIN_MINT_TYPE, recipes.getCoinMintRecipes());

		ItemStack masterTicket = TicketItem.CreateMasterTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR);
		registration.addIngredientInfo(masterTicket, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket_master"));
		registration.addIngredientInfo(masterTicket, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket_materials", TicketItem.getTicketMaterialsList()));

		ItemStack ticket = TicketItem.CreateTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR);
		registration.addIngredientInfo(ticket, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket"));
		registration.addIngredientInfo(ticket, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket_materials", TicketItem.getTicketMaterialsList()));

		ItemStack pass = TicketItem.CreatePass(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR);
		registration.addIngredientInfo(pass, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket.pass"));
		registration.addIngredientInfo(pass, VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket_materials", TicketItem.getTicketMaterialsList()));

		registration.addIngredientInfo(new ItemStack(ModItems.TICKET_STUB.get()), VanillaTypes.ITEM_STACK, EasyText.translatable("lightmanscurrency.jei.info.ticket_stub"));
		
	}
	
	@Override
	public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration)
	{
		
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(ModBlocks.COIN_MINT.get()), COIN_MINT_TYPE);
	}
	
	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(MintMenu.class, ModMenus.MINT.get(), COIN_MINT_TYPE, 0, 1, 2, 36);
	}
	
}

package io.github.lightman314.lightmanscurrency.integration.jeiplugin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator.Results;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

@JeiPlugin
public class LCJeiPlugin implements IModPlugin{

	public static final ResourceLocation COIN_MINT_UID = new ResourceLocation(LightmansCurrency.MODID, "coin_mint");
	
	@Nonnull
	@Override
	public ResourceLocation getPluginUid() { return new ResourceLocation(LightmansCurrency.MODID, LightmansCurrency.MODID); }

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
		registration.addRecipes(recipes.getCoinMintRecipes(), COIN_MINT_UID);
		
		registration.addIngredientInfo(new ItemStack(ModItems.TICKET_MASTER.get()), VanillaTypes.ITEM, EasyText.translatable("lightmanscurrency.jei.info.ticket_master"));
		registration.addIngredientInfo(new ItemStack(ModItems.TICKET_MASTER.get()), VanillaTypes.ITEM, EasyText.translatable("lightmanscurrency.jei.info.ticket_materials", TicketItem.getTicketMaterialsList()));
		
		registration.addIngredientInfo(new ItemStack(ModItems.TICKET.get()), VanillaTypes.ITEM, EasyText.translatable("lightmanscurrency.jei.info.ticket"));
		registration.addIngredientInfo(new ItemStack(ModItems.TICKET.get()), VanillaTypes.ITEM, EasyText.translatable("lightmanscurrency.jei.info.ticket_materials", TicketItem.getTicketMaterialsList()));
		
		registration.addIngredientInfo(new ItemStack(ModItems.TICKET_STUB.get()), VanillaTypes.ITEM, EasyText.translatable("lightmanscurrency.jei.info.ticket_stub"));
		
	}
	
	@Override
	public void registerGuiHandlers(@Nonnull IGuiHandlerRegistration registration)
	{
		
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_MINT.get()), COIN_MINT_UID);
	}
	
	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(MintMenu.class, COIN_MINT_UID, 0, 1, 2, 36);
	}
	
}

package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.LCClientRecipeCache;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public class LCJei implements IModPlugin {

    public static final Identifier UID = LCApi.id("plugin");

    @Override
    public Identifier getPluginUid() { return UID; }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CoinMintCategory.TYPE,List.copyOf(LCClientRecipeCache.getRecipes(LCRecipeTypes.COIN_MINT.get())));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CoinMintCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(TraderCustomerScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGenericGuiContainerHandler(TraderStorageScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGhostIngredientHandler(TraderStorageScreen.class,new FancyGhostIngredientHandler<>());
    }

}
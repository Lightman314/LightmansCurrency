package io.github.lightman314.lightmanscurrency.integration.jei;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.LCClientRecipeCache;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.features.player_data.PlayerDataScreen;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class LCJei implements IModPlugin {

    public static final Identifier UID = LCApi.id("plugin");

    @Override
    public Identifier getPluginUid() { return UID; }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CoinMintCategory.TYPE,List.copyOf(LCClientRecipeCache.getRecipes(LCRecipeTypes.COIN_MINT.get())));
        registration.addRecipes(TicketStationCategory.TYPE,List.copyOf(LCClientRecipeCache.getRecipes(LCRecipeTypes.TICKET_STATION.get())));
        //Ticket Stub Info
        registration.addItemStackInfo(List.of(new ItemStack(LCItems.TICKET_STUB.asItem())),LCText.Jei.TICKET_STUB.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CoinMintCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TicketStationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CoinMintCategory.TYPE,LCBlocks.COIN_MINT);
        registration.addCraftingStation(TicketStationCategory.TYPE,LCBlocks.TICKET_STATION);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(TraderCustomerScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGenericGuiContainerHandler(TraderStorageScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGenericGuiContainerHandler(ATMScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGenericGuiContainerHandler(PlayerDataScreen.class,FancyGuiHandlerArea.INSTANCE);
        registration.addGhostIngredientHandler(TraderStorageScreen.class,new FancyGhostIngredientHandler<>());
    }

}
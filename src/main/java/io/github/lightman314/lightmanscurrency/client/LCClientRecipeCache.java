package io.github.lightman314.lightmanscurrency.client;

import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public final class LCClientRecipeCache {
    private LCClientRecipeCache() {}

    private static RecipeMap cache = RecipeMap.EMPTY;

    public static <I extends RecipeInput,T extends Recipe<I>> Collection<RecipeHolder<T>> getRecipes(RecipeType<T> type) {
        return cache.byType(type);
    }

    public static <I extends RecipeInput,T extends Recipe<I>> List<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, I container, Level level) {
        return getRecipeStreamFor(type,container,level).toList();
    }
    public static <I extends RecipeInput,T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipeStreamFor(RecipeType<T> type, I container, Level level) {
        return cache.getRecipesFor(type,container,level);
    }

    @SubscribeEvent
    private static void onRecipesReceived(RecipesReceivedEvent event) {
        cache = event.getRecipeMap();
    }

    @SubscribeEvent
    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        cache = RecipeMap.EMPTY;
    }

}
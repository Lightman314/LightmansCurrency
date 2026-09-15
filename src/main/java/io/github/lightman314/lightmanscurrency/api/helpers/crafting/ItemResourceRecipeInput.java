package io.github.lightman314.lightmanscurrency.api.helpers.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemResourceRecipeInput implements RecipeInput {

    public final ResourceHandler<ItemResource> handler;
    public ItemResourceRecipeInput(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    public static ItemResourceRecipeInput ranged(ResourceHandler<ItemResource> handler,int startIndex,int stopIndex) {
        return new ItemResourceRecipeInput(RangedResourceHandler.of(handler,startIndex,stopIndex));
    }

    @Override
    public ItemStack getItem(int index) {
        ItemResource resource = this.handler.getResource(index);
        return resource.toStack(this.handler.getAmountAsInt(index));
    }

    @Override
    public int size() { return this.handler.size(); }
}

package io.github.lightman314.lightmanscurrency.api.client.gui.helpers;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record IngredientResult(Object ingredient,ScreenArea area) {

    public static Optional<IngredientResult> forItem(ItemStack item,ScreenPosition corner) { return item.isEmpty() ? Optional.empty() : Optional.of(new IngredientResult(item,corner.asArea(18,18))); }

}
package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.Optional;

public interface IRecipeIngredientViewer {

    default Optional<IngredientResult> getHoveredIngredient(ScreenPosition mousePos) { return Optional.empty(); }

}
package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import java.util.List;
import java.util.Optional;

public interface IFancyScreen extends IWidgetHolder {

    ScreenPosition getCorner();
    ScreenArea getArea();
    int getWidth();
    int getHeight();

    Optional<IngredientResult> getHoveredIngredient(ScreenPosition mousePos);
    List<GhostSlot<?>> getGhostSlots();

}
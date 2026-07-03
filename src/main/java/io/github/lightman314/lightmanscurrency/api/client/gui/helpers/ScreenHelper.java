package io.github.lightman314.lightmanscurrency.api.client.gui.helpers;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.IFancyScreen;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Contains {@link ScreenPosition} and {@link ScreenArea} helper methods that relate to client-only classes,
 * allowing the ScreenPosition and ScreenArea classes themselves to remain in use by the logical server.
 */
public final class ScreenHelper {
    private ScreenHelper() {}

    public static ScreenPosition offset(ScreenPosition position, IFancyScreen screen) { return position.offset(screen.getCorner()); }
    public static ScreenPosition offsetScreen(ScreenPosition position, AbstractContainerScreen<?> screen) { return position.offset(screen.getLeftPos(),screen.getTopPos()); }
    public static ScreenPosition getScreenCorner(AbstractContainerScreen<?> screen) { return ScreenPosition.of(screen.getLeftPos(),screen.getTopPos()); }

    public static ScreenArea getScreenArea(AbstractContainerScreen<?> screen) { return ScreenArea.of(getScreenCorner(screen),screen.getImageWidth(),screen.getImageHeight()); }

}
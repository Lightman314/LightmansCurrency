package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MiscTabAddon {

    private SettingsSubTab tab;
    public void initialize(SettingsSubTab tab) { this.tab = tab; }
    protected final SettingsSubTab getTab() { return this.tab; }

    public abstract void onOpenBefore(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen, @Nonnull AtomicInteger nextYPosition);
    public abstract void onOpenAfter(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen, @Nonnull AtomicInteger nextYPosition);

    public abstract void renderBG(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui);
    public abstract void renderAfterWidgets(@Nonnull SettingsSubTab tab, @Nonnull EasyGuiGraphics gui);

    public abstract void tick(@Nonnull SettingsSubTab tab);

    public abstract void onClose(@Nonnull SettingsSubTab tab);

}

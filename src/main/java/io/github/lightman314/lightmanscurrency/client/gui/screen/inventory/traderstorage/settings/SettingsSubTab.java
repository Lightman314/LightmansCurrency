package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.settings.TraderSettingsTab;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public abstract class SettingsSubTab implements TabButton.ITab {

    public final TraderSettingsClientTab parent;
    public final TraderSettingsTab commonTab;
    public final TraderStorageScreen screen;
    public final TraderStorageMenu menu;
    public final Font font;

    protected SettingsSubTab(@Nonnull TraderSettingsClientTab parent)
    {
        this.parent = parent;
        this.commonTab = this.parent.commonTab;
        this.screen = this.parent.screen;
        this.menu = this.parent.menu;
        this.font = this.parent.font;
    }

    @Override
    public int getColor() { return 0xFFFFFF; }

    public final <T> T addWidget(T widget) { return this.parent.addWidget(widget); }
    public final void removeWidget(Object widget) { this.parent.removeWidget(widget); }

    public abstract boolean canOpen();

    public abstract void onOpen();
    public abstract void onClose();

    public void tick() {}

    public abstract void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks);
    public abstract void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY);

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }

    public final void sendNetworkMessage(@Nonnull CompoundTag message) { this.commonTab.SendSettingsMessage(message); }

    public boolean shouldRenderInventoryText() { return true; }

}
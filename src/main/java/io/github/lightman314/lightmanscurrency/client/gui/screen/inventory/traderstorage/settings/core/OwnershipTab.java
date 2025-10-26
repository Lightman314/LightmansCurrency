package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.OwnerSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public class OwnershipTab extends SettingsSubTab {

    public OwnershipTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    OwnerSelectionWidget ownerSelectionWidget;

    private boolean manualSelectionMode = false;
    EditBox playerOwnerInput;
    EasyButton setPlayerButton;

    //Input Mode
    EasyButton buttonToggleInputMode;

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_ALEX_HEAD; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_SETTINGS_OWNER.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.TRANSFER_OWNERSHIP); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Manual player selection
        this.playerOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 20, screenArea.y + 50, 160, 20, EasyText.empty()));
        this.playerOwnerInput.setMaxLength(16);

        this.setPlayerButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(20,80))
                .width(160)
                .text(LCText.BUTTON_OWNER_SET_PLAYER)
                .pressAction(this::setPlayerOwner)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)))
                .build());

        //Owner Selection
        this.ownerSelectionWidget = this.addChild(OwnerSelectionWidget.builder()
                .position(screenArea.pos.offset(20,27))
                .width(160)
                .rows(5)
                .selected(this::getCurrentOwner)
                .handler(this::setOwner)
                .oldWidget(this.ownerSelectionWidget)
                .build());

        //Toggle Mode button
        this.buttonToggleInputMode = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 25,5))
                .pressAction(this::toggleInputMode)
                .icon(this::getModeIcon)
                .addon(EasyAddonHelper.tooltip(this::getModeTooltip))
                .build());

        this.updateMode();

    }

    @Nullable
    protected OwnerData getCurrentOwner()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return trader.getOwner();
        return null;
    }

    protected void setOwner(@Nonnull Owner newOwner)
    {
        this.sendMessage(this.builder().setOwner("ChangeOwner", newOwner));
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TraderData trader = this.menu.getTrader();
        if(trader != null)
            gui.drawString(LCText.GUI_OWNER_CURRENT.get(trader.getOwner().getName()), 20, 10, 0x404040);

    }

    @Override
    public void tick() {

        if(this.manualSelectionMode)
            this.setPlayerButton.active = !this.playerOwnerInput.getValue().isBlank();

    }

    private void setPlayerOwner(EasyButton button)
    {
        if(this.playerOwnerInput.getValue().isBlank())
            return;
        this.sendMessage(this.builder().setString("ChangePlayerOwner", this.playerOwnerInput.getValue()));
        this.playerOwnerInput.setValue("");
    }

    private void toggleInputMode(EasyButton button)
    {
        this.manualSelectionMode = !this.manualSelectionMode;
        this.updateMode();
    }

    private void updateMode()
    {
        this.playerOwnerInput.visible = this.setPlayerButton.visible = this.manualSelectionMode;
        if(this.manualSelectionMode)
            this.setPlayerButton.active = !this.playerOwnerInput.getValue().isBlank();
        this.ownerSelectionWidget.setVisible(!this.manualSelectionMode);
    }

    private IconData getModeIcon() { return this.manualSelectionMode ? ItemIcon.ofItem(Items.COMMAND_BLOCK) : IconUtil.ICON_ALEX_HEAD; }

    private Component getModeTooltip() { return this.manualSelectionMode ? LCText.TOOLTIP_OWNERSHIP_MODE_SELECTION.get() : LCText.TOOLTIP_OWNERSHIP_MODE_MANUAL.get(); }

}

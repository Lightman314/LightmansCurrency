package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.client.gui.widget.OwnerSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestSecurityUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SecurityUpgradeTab extends CoinChestTab.Upgrade {

    public SecurityUpgradeTab(CoinChestUpgradeData data, Object screen) { super(data, screen); }

    @Override
    public boolean isVisible() {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestSecurityUpgrade upgrade)
            return upgrade.isAdmin(this.screen.be, data, this.screen.getMenu().player);
        return false;
    }

    @Override
    public boolean coinSlotsVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public boolean titleVisible() { return false; }

    OwnerSelectionWidget ownerSelectionWidget;

    private boolean manualSelectionMode = false;
    EditBox playerOwnerInput;
    EasyButton setPlayerButton;

    //Input Mode
    EasyButton buttonToggleInputMode;

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        //Manual player selection
        this.playerOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 30, this.screen.getXSize() - 20, 20, EasyText.empty()));
        this.playerOwnerInput.setMaxLength(16);

        this.setPlayerButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,60))
                .width(screenArea.width - 20)
                .text(LCText.BUTTON_OWNER_SET_PLAYER)
                .pressAction(this::SetPlayerOwner)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)))
                .build());

        //Owner Selection
        this.ownerSelectionWidget = this.addChild(new OwnerSelectionWidget(screenArea.pos.offset(7, 27), this.screen.getXSize() - 22, 5, this::getCurrentOwner, this::setOwner, this.ownerSelectionWidget));

        //Toggle Mode button
        this.buttonToggleInputMode = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width - 25, 5), this::toggleInputMode, this::getModeIcon).withAddons(EasyAddonHelper.tooltip(this::getModeTooltip)));

        this.updateMode();

    }

    @Nullable
    protected OwnerData getCurrentOwner()
    {
        CoinChestUpgradeData data = this.getUpgradeData();
        if(data != null && data.upgrade instanceof CoinChestSecurityUpgrade upgrade)
            return upgrade.parseOwnerData(this.menu.be, data);
        return null;
    }

    private Component getOwnerName()
    {
        OwnerData data = this.getCurrentOwner();
        if(data != null)
            return data.getName();
        return LCText.GUI_OWNER_NULL.get();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        gui.drawString(TextRenderUtil.fitString(LCText.GUI_OWNER_CURRENT.get(this.getOwnerName()), this.screen.getXSize() - 20), 8, 6, 0x404040);

    }

    private void SetPlayerOwner(EasyButton button)
    {
        if(this.playerOwnerInput.getValue().isBlank())
            return;
        this.menu.SendMessageToServer(this.builder().setString("SetPlayerOwner", this.playerOwnerInput.getValue()));
        this.playerOwnerInput.setValue("");
    }

    private void setOwner(Owner newOwner) {
        this.menu.SendMessageToServer(this.builder().setOwner("SetOwner", newOwner));
    }

    @Override
    public void tick() {

        if(this.manualSelectionMode)
            this.setPlayerButton.active = !this.playerOwnerInput.getValue().isBlank();

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

    private IconData getModeIcon() { return this.manualSelectionMode ? IconData.of(Items.COMMAND_BLOCK) : IconUtil.ICON_ALEX_HEAD; }

    private Component getModeTooltip() { return this.manualSelectionMode ? LCText.TOOLTIP_OWNERSHIP_MODE_SELECTION.get() : LCText.TOOLTIP_OWNERSHIP_MODE_MANUAL.get(); }

}

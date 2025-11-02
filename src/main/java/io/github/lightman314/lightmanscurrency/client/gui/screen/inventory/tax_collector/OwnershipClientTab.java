package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.OwnerSelectionWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.OwnershipTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OwnershipClientTab extends TaxCollectorClientTab<OwnershipTab> {

    public OwnershipClientTab(Object screen, OwnershipTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return ItemIcon.ofItem(Items.PLAYER_HEAD); }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_OWNER.get(); }

    private boolean manualMode = false;

    private OwnerSelectionWidget ownerSelectionWidget;
    private EditBox playerOwnerInput;
    private EasyButton playerOwnerButton;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        if(firstOpen)
            this.manualMode = false;

        //Manual player selection
        this.playerOwnerInput = this.addChild(new EditBox(this.getFont(), screenArea.x + 10, screenArea.y + 50, 160, 20, EasyText.empty()));
        this.playerOwnerInput.setMaxLength(16);

        this.playerOwnerButton = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,72))
                .width(screenArea.width - 20)
                .text(LCText.BUTTON_OWNER_SET_PLAYER)
                .pressAction(this::SetOwnerPlayer)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_WARNING_CANT_BE_UNDONE.getWithStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)))
                .build());


        this.ownerSelectionWidget = this.addChild(OwnerSelectionWidget.builder()
                .position(screenArea.pos.offset(12,30))
                .width(152)
                .rows(6)
                .selected(this::getCurrentOwner)
                .handler(this.commonTab::SetOwner)
                .oldWidget(this.ownerSelectionWidget)
                .build());

        //Toggle Mode button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 25,5))
                .pressAction(this::toggleInputMode)
                .icon(this::getModeIcon)
                .addon(EasyAddonHelper.tooltip(this::getModeTooltip))
                .build());

        this.updateMode();

    }

    @Override
    public void tick() {
        if(this.manualMode)
            this.playerOwnerButton.active = !this.playerOwnerInput.getValue().isBlank();
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TaxEntry entry = this.getEntry();
        Component ownerName = LCText.GUI_OWNER_NULL.get();
        if(entry != null)
            ownerName = entry.getOwner().getName();

        gui.drawString(LCText.GUI_OWNER_CURRENT.get(ownerName), 8, 6, 0x404040);

    }

    @Nullable
    protected OwnerData getCurrentOwner()
    {
        TaxEntry entry = this.menu.getEntry();
        if(entry != null)
            return entry.getOwner();
        return null;
    }

    private void toggleInputMode(EasyButton button) { this.manualMode = !this.manualMode; this.updateMode(); }

    private void updateMode()
    {
        this.playerOwnerInput.visible = this.playerOwnerButton.visible = this.manualMode;
        if(this.manualMode)
            this.playerOwnerButton.active = !this.playerOwnerInput.getValue().isBlank();
        this.ownerSelectionWidget.setVisible(!this.manualMode);
    }

    private IconData getModeIcon() { return this.manualMode ? ItemIcon.ofItem(Items.COMMAND_BLOCK) : IconUtil.ICON_ALEX_HEAD; }

    private Component getModeTooltip() { return this.manualMode ? LCText.TOOLTIP_OWNERSHIP_MODE_SELECTION.get() : LCText.TOOLTIP_OWNERSHIP_MODE_MANUAL.get(); }

    private void SetOwnerPlayer()
    {
        if(this.playerOwnerInput != null)
        {
            this.commonTab.SetOwnerPlayer(this.playerOwnerInput.getValue());
            this.playerOwnerInput.setValue("");
        }
    }

    @Override
    public boolean blockInventoryClosing() { return true; }

}
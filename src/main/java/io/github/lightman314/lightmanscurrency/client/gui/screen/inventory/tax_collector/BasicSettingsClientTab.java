package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.TaxCollectorClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.tax_collector.tabs.BasicSettingsTab;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BasicSettingsClientTab extends TaxCollectorClientTab<BasicSettingsTab> {

    public BasicSettingsClientTab(Object screen, BasicSettingsTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TAX_COLLECTOR_BASIC.get(); }

    private EditBox nameInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        TaxEntry entry = this.getEntry();

        //Activate Toggle
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(8, 16))
                .pressAction(this::ToggleActiveState)
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::isEntryActive))
                .addon(EasyAddonHelper.visibleCheck(this::canActiveToggle))
                .build());

        //Render Mode Selection
        this.addChild(DropdownWidget.builder()
                        .position(screenArea.pos.offset(screenArea.width - 88, 26))
                        .width(80)
                        .selected(entry == null ? 0 : entry.getRenderMode())
                        .selectAction(this.commonTab::SetRenderMode)
                        .option(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_NONE)
                        .option(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_MEMBERS)
                        .option(LCText.GUI_TAX_COLLECTOR_RENDER_MODE_ALL)
                        .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                        .build());

        //Tax Rate Selection
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(6,32))
                .pressAction(() -> this.commonTab.SetRate(this.getCurrentRate() + (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getTaxRate() < TaxEntry.maxTaxRate();
                }))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(6, 42))
                .pressAction(() -> this.commonTab.SetRate(this.getCurrentRate() - (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getTaxRate() > 1;
                }))
                .build());

        //Radius Selection
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(22, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetRadius(this.getCurrentRadius() + (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getRadius() < TaxEntry.maxRadius();
                }))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(32, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetRadius(this.getCurrentRadius() - (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getRadius() > TaxEntry.minRadius();
                }))
                .build());

        //Height Selection
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset((screenArea.width / 2) - 10, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetHeight(this.getCurrentHeight() + (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getHeight() < TaxEntry.maxHeight();
                }))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(screenArea.width / 2, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetHeight(this.getCurrentHeight() - (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getHeight() > TaxEntry.minHeight();
                }))
                .build());


        //Vert Offset Selection
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 42, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetVertOffset(this.getCurrentVertOffset() + (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getVertOffset() < TaxEntry.maxVertOffset();
                }))
                .build());
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 32, screenArea.height - 16))
                .pressAction(() -> this.commonTab.SetVertOffset(this.getCurrentVertOffset() - (Screen.hasShiftDown() ? 10 : 1)))
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .addon(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .addon(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getVertOffset() > TaxEntry.minVertOffset();
                }))
                .build());

        //Bank Account Link Toggle
        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(8, 58))
                .pressAction(() -> this.commonTab.SetBankAccountLink(!this.getCurrentBankAccountLink()))
                .sprite(IconAndButtonUtil.SPRITE_CHECK(this::getCurrentBankAccountLink))
                .addon(EasyAddonHelper.visibleCheck(() -> !this.menu.isServerEntry()))
                .build());

        //Name Edit
        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.pos.x + 10, screenArea.pos.y + 80, screenArea.width - 20, 18, EasyText.empty()));
        this.nameInput.setValue(entry != null ? entry.getCustomName() : "");
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,102))
                .width(70)
                .text(LCText.BUTTON_SETTINGS_CHANGE_NAME)
                .pressAction(() -> this.commonTab.SetName(this.getCurrentNameInput()))
                .addon(EasyAddonHelper.activeCheck(this::isNameDifferent))
                .build());
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(96,102))
                .width(70)
                .text(LCText.BUTTON_SETTINGS_RESET_NAME)
                .pressAction(this::ResetName)
                .addon(EasyAddonHelper.activeCheck(this::hasCustomName))
                .build());

        this.tick();

    }

    @Override
    public void tick() {
        TaxEntry entry = this.getEntry();
        this.showAreaButtons = entry != null && !entry.isInfiniteRange();
    }

    private boolean isEntryActive() {
        TaxEntry entry = this.getEntry();
        return entry != null && entry.isActive();
    }

    private boolean canActiveToggle() {
        TaxEntry entry = this.getEntry();
        return entry != null && (!LCConfig.SERVER.taxCollectorAdminOnly.get() || this.menu.isAdmin() || entry.isActive());
    }

    private boolean showAreaButtons = true;
    private boolean showAreaButtons() { return this.showAreaButtons; }

    private int getCurrentRate() { TaxEntry entry = this.getEntry(); return entry != null ? entry.getTaxRate() : 0; }
    private int getCurrentRadius() { TaxEntry entry = this.getEntry(); return entry != null ? entry.getRadius() : 0; }
    private int getCurrentHeight() { TaxEntry entry = this.getEntry(); return entry != null ? entry.getHeight() : 0; }
    private int getCurrentVertOffset() { TaxEntry entry = this.getEntry(); return entry != null ? entry.getVertOffset() : 0; }
    private boolean getCurrentBankAccountLink() { TaxEntry entry = this.getEntry(); return entry != null && entry.isLinkedToBank(); }
    private String getCurrentNameInput()
    {
        if(this.nameInput == null)
            return "";
        return this.nameInput.getValue();
    }
    private boolean isNameDifferent()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
            return !entry.getCustomName().equals(this.getCurrentNameInput());
        return false;
    }
    private boolean hasCustomName()
    {
        TaxEntry entry = this.getEntry();
        if(entry != null)
            return !entry.getCustomName().isBlank();
        return false;
    }
    private void ResetName()
    {
        this.commonTab.SetName("");
        this.nameInput.setValue("");
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        TaxEntry entry = this.getEntry();
        if(entry == null)
            return;

        //Render Title
        gui.drawString(entry.getName(), 8, 6, 0x404040);

        //Render Active Label
        gui.drawString(LCText.GUI_TAX_COLLECTOR_ACTIVE.get(), 20, 18, entry != null && entry.isActive() ? 0x00FF00 : 0xFF0000);

        //Render Mode Label
        if(this.showAreaButtons)
        {
            Component label = LCText.GUI_TAX_COLLECTOR_RENDER_MODE_LABEL.get();
            gui.drawString(label, this.screen.getXSize() - 8 - gui.font.width(label), 16, 0x404040);
        }

        //Render Tax Rate Label
        gui.drawString(LCText.GUI_TAX_COLLECTOR_TAX_RATE.get(entry.getTaxRate()), 18, 39, 0x404040);

        //Bank Link Label
        if(!entry.isServerEntry())
            gui.drawString(LCText.GUI_SETTINGS_BANK_LINK.get(), 20, 60, 0x404040);

        if(entry.isInfiniteRange())
        {
            //Infinite Range Area Labels
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_LABEL.get(), this.screen.getXSize() / 2, this.screen.getYSize() - 38, 0x404040);
            Component areaText;
            if(entry.getCenter().isVoid())
                areaText = LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_VOID.get();
            else
                areaText = LCText.GUI_TAX_COLLECTOR_AREA_INFINITE_DIMENSION.get(entry.getCenter().getDimension().location());
            TextRenderUtil.drawCenteredText(gui, areaText, this.screen.getXSize() / 2, this.screen.getYSize() - 28, 0x404040);
        }
        else //Normal Area Labels
        {
            //Radius Labels
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TAX_COLLECTOR_AREA_RADIUS.get(), 32, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getRadius()), 32, this.screen.getYSize() - 28, 0x404040);

            //Height Labels
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TAX_COLLECTOR_AREA_HEIGHT.get(), this.screen.getXSize() / 2, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getHeight()), this.screen.getXSize() / 2, this.screen.getYSize() - 28, 0x404040);

            //Y Offset Labels
            TextRenderUtil.drawCenteredText(gui, LCText.GUI_TAX_COLLECTOR_AREA_VERTOFFSET.get(), this.screen.getXSize() - 32, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getVertOffset()), this.screen.getXSize() - 32, this.screen.getYSize() - 28, 0x404040);
        }

    }

    @Override
    public boolean blockInventoryClosing() { return this.nameInput.isFocused(); }

    private void ToggleActiveState(EasyButton button) { this.commonTab.SetActive(!this.isEntryActive()); }

}

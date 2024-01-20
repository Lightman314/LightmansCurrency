package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.tax_collector;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
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
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BasicSettingsClientTab extends TaxCollectorClientTab<BasicSettingsTab> {

    public BasicSettingsClientTab(Object screen, BasicSettingsTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TAXES; }

    @Nullable
    @Override
    public Component getTooltip() { return EasyText.translatable("gui.lightmanscurrency.tax_collector.basic"); }

    private EditBox nameInput;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        TaxEntry entry = this.getEntry();

        //Activate Toggle
        this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(8, 16), this::ToggleActiveState, this::isEntryActive)
                .withAddons(EasyAddonHelper.visibleCheck(this::canActiveToggle)));

        //Render Mode Selection
        this.addChild(new DropdownWidget(screenArea.pos.offset(screenArea.width - 88, 26), 80, entry == null ? 0 : entry.getRenderMode(), this.commonTab::SetRenderMode,
                EasyText.translatable("gui.lightmanscurrency.tax_collector.render_mode.0"),
                EasyText.translatable("gui.lightmanscurrency.tax_collector.render_mode.1"),
                EasyText.translatable("gui.lightmanscurrency.tax_collector.render_mode.2"))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons)));

        //Tax Rate Selection
        this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(6, 32), b -> this.commonTab.SetRate(this.getCurrentRate() + (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getTaxRate() < TaxEntry.maxTaxRate();
                })));
        this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(6, 42), b -> this.commonTab.SetRate(this.getCurrentRate() - (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getTaxRate() > 1;
                })));

        //Radius Selection
        this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(22, screenArea.height - 16), b -> this.commonTab.SetRadius(this.getCurrentRadius() + (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getRadius() < TaxEntry.maxRadius();
                })));
        this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(32, screenArea.height - 16), b -> this.commonTab.SetRadius(this.getCurrentRadius() - (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getRadius() > TaxEntry.minRadius();
                })));

        //Height Selection
        this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset((screenArea.width / 2) - 10, screenArea.height - 16), b -> this.commonTab.SetHeight(this.getCurrentHeight() + (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getHeight() < TaxEntry.maxHeight();
                })));
        this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(screenArea.width / 2, screenArea.height - 16), b -> this.commonTab.SetHeight(this.getCurrentHeight() - (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getHeight() > TaxEntry.minHeight();
                })));

        //Vert Offset Selection
        this.addChild(IconAndButtonUtil.plusButton(screenArea.pos.offset(screenArea.width - 42, screenArea.height - 16), b -> this.commonTab.SetVertOffset(this.getCurrentVertOffset() + (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getVertOffset() < TaxEntry.maxVertOffset();
                })));
        this.addChild(IconAndButtonUtil.minusButton(screenArea.pos.offset(screenArea.width - 32, screenArea.height - 16), b -> this.commonTab.SetVertOffset(this.getCurrentVertOffset() - (Screen.hasShiftDown() ? 10 : 1)))
                .withAddons(EasyAddonHelper.visibleCheck(this::showAreaButtons))
                .withAddons(EasyAddonHelper.activeCheck(() -> {
                    TaxEntry e = this.getEntry();
                    return e != null && e.getVertOffset() > TaxEntry.minVertOffset();
                })));

        //Bank Account Link Toggle
        this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.pos.offset(8, 58), b -> this.commonTab.SetBankAccountLink(!this.getCurrentBankAccountLink()), this::getCurrentBankAccountLink)
                .withAddons(EasyAddonHelper.visibleCheck(() -> !this.menu.isServerEntry())));

        //Name Edit (low enough to not
        this.nameInput = this.addChild(new EditBox(this.getFont(), screenArea.pos.x + 10, screenArea.pos.y + 80, screenArea.width - 20, 18, EasyText.empty()));
        this.nameInput.setValue(entry != null ? entry.getCustomName() : "");
        this.addChild(new EasyTextButton(screenArea.pos.offset(10, 102), 70, 20, EasyText.translatable("gui.lightmanscurrency.changename"), () -> this.commonTab.SetName(this.getCurrentNameInput()))
                .withAddons(EasyAddonHelper.activeCheck(this::isNameDifferent)));
        this.addChild(new EasyTextButton(screenArea.pos.offset(96, 102), 70, 20, EasyText.translatable("gui.lightmanscurrency.resetname"), this::ResetName)
                .withAddons(EasyAddonHelper.activeCheck(this::hasCustomName)));

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
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.active"), 20, 18, entry != null && entry.isActive() ? 0x00FF00 : 0xFF0000);

        //Render Mode Label
        if(this.showAreaButtons)
        {
            Component label = EasyText.translatable("gui.lightmanscurrency.tax_collector.render_mode.label");
            gui.drawString(label, this.screen.getXSize() - 8 - gui.font.width(label), 16, 0x404040);
        }

        //Render Tax Rate Label
        gui.drawString(EasyText.translatable("gui.lightmanscurrency.tax_collector.tax_rate", entry != null ? entry.getTaxRate() : "ERROR"), 18, 39, 0x404040);

        //Bank Link Label
        if(!entry.isServerEntry())
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.settings.banklink"), 20, 60, 0x404040);

        if(entry.isInfiniteRange())
        {
            //Infinite Range Area Labels
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("gui.lightmanscurrency.tax_collector.area.infinite.label"), this.screen.getXSize() / 2, this.screen.getYSize() - 38, 0x404040);
            Component areaText;
            if(entry.getCenter().isVoid())
                areaText = EasyText.translatable("gui.lightmanscurrency.tax_collector.area.infinite.void");
            else
                areaText = EasyText.translatable("gui.lightmanscurrency.tax_collector.area.infinite.dimension", entry.getCenter().getDimension().location());
            TextRenderUtil.drawCenteredText(gui, areaText, this.screen.getXSize() / 2, this.screen.getYSize() - 28, 0x404040);
        }
        else //Normal Area Labels
        {
            //Radius Labels
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("gui.lightmanscurrency.tax_collector.area.radius"), 32, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getRadius()), 32, this.screen.getYSize() - 28, 0x404040);

            //Height Labels
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("gui.lightmanscurrency.tax_collector.area.height"), this.screen.getXSize() / 2, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getHeight()), this.screen.getXSize() / 2, this.screen.getYSize() - 28, 0x404040);

            //Y Offset Labels
            TextRenderUtil.drawCenteredText(gui, EasyText.translatable("gui.lightmanscurrency.tax_collector.area.vertOffset"), this.screen.getXSize() - 32, this.screen.getYSize() - 38, 0x404040);
            TextRenderUtil.drawCenteredText(gui, Integer.toString(entry.getVertOffset()), this.screen.getXSize() - 32, this.screen.getYSize() - 28, 0x404040);
        }

    }

    @Override
    public boolean blockInventoryClosing() { return this.nameInput.isFocused(); }

    private void ToggleActiveState(EasyButton button) { this.commonTab.SetActive(!this.isEntryActive()); }

}

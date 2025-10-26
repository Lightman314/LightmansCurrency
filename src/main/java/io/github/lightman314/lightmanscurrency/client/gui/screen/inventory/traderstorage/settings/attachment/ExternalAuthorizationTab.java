package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.attachment;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment.AccessLevel;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExternalAuthorizationTab extends SettingsSubTab implements IScrollable {

    public ExternalAuthorizationTab(TraderSettingsClientTab parent) { super(parent); }

    public static final int ROWS = 3;

    @Override
    public IconData getIcon() { return IconUtil.ICON_COUNT; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_EXTERNAL_AUTH.get(); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(ExternalAuthorizationAttachment.EDIT_AUTHORIZATION_PERMISSION) && !this.getPossibleTargets().isEmpty(); }

    private String selectedMachine = null;
    private DropdownWidget accessSelection;

    private int scroll = 0;

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ROWS,this.getPossibleTargets().size()); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        if(firstOpen)
            this.selectedMachine = null;

        for(int i = 0; i < ROWS; ++i)
        {
            final int index = i;
            this.addChild(EasyTextButton.builder()
                    .position(screenArea.pos.offset(20,20 + (20 * i)))
                    .width(screenArea.width - 40)
                    .text(() -> EasyText.literal(this.getPossibleTarget(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.doesTargetExist(index)))
                    .addon(EasyAddonHelper.activeCheck(() -> !this.isTargetSelected(index)))
                    .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRADER_SETTINGS_EXTERNAL_AUTH_SELECT))
                    .pressAction(() -> this.selectTarget(index))
                    .build());
        }

        //Scroll listener/bar
        this.addChild(ScrollListener.builder()
                .position(screenArea.pos.offset(20,20))
                .size(screenArea.width - 40,20 * ROWS)
                .listener(this)
                .build());
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - 20,20))
                .height(20 * ROWS)
                .scrollable(this)
                .build());

        //Access Level Selection
        this.accessSelection = this.addChild(DropdownWidget.builder()
                .position(screenArea.pos.offset(20,40 + (20 * ROWS)))
                .width(screenArea.width - 40)
                .enumOptions(LCText.GUI_TRADER_SETTINGS_EXTERNAL_AUTH_ACCESS_LEVEL,AccessLevel.values())
                .addon(EasyAddonHelper.visibleCheck(() -> this.selectedMachine != null))
                .selected(this.getSelectedAccessLevel().ordinal())
                .selectAction(this::changeAccessLevel)
                .build());


    }

    private boolean doesTargetExist(int internalIndex) { return !this.getPossibleTarget(internalIndex).isEmpty(); }
    private boolean isTargetSelected(int internalIndex) { return this.selectedMachine != null && Objects.equals(this.selectedMachine,this.getPossibleTarget(internalIndex)); }

    private String getPossibleTarget(int internalIndex)
    {
        List<String> attemptedAccessors = this.getPossibleTargets();
        int index = this.scroll + internalIndex;
        if (index >= 0 && index < attemptedAccessors.size())
            return attemptedAccessors.get(index);
        return "";
    }

    private List<String> getPossibleTargets()
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            return trader.getAttachment(ExternalAuthorizationAttachment.TYPE).getAttemptedAccessors();
        return new ArrayList<>();
    }

    private void selectTarget(int internalIndex)
    {
        List<String> attemptedAccessors = this.getPossibleTargets();
        int index = this.scroll + internalIndex;
        if(index >= 0 && index < attemptedAccessors.size())
        {
            this.selectedMachine = attemptedAccessors.get(index);
            if(this.accessSelection != null)
                this.accessSelection.setCurrentlySelected(this.getSelectedAccessLevel().ordinal());
        }
    }

    private AccessLevel getSelectedAccessLevel()
    {
        if(this.selectedMachine == null)
            return AccessLevel.NONE;
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.hasAttachment(ExternalAuthorizationAttachment.TYPE))
            return trader.getAttachment(ExternalAuthorizationAttachment.TYPE).getAccessLevel(this.selectedMachine);
        return AccessLevel.NONE;
    }

    private void changeAccessLevel(int ordinal)
    {
        if(this.selectedMachine == null)
            return;
        if(this.getSelectedAccessLevel().ordinal() == ordinal)
            return;
        this.menu.SendMessage(this.builder()
                .setString(ExternalAuthorizationAttachment.TYPE + "-ChangeAuthorization",this.selectedMachine)
                .setInt(ExternalAuthorizationAttachment.TYPE + "-NewLevel",ordinal));
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        //Label for access level perhaps?

    }

}

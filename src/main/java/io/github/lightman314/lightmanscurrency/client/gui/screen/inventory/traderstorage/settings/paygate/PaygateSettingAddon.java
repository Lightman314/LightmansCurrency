package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.paygate;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.addons.MiscTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.OutputConflictHandling;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PaygateSettingAddon extends MiscTabAddon {

    private int yPos = 0;

    public static PaygateSettingAddon INSTANCE = new PaygateSettingAddon();

    private PaygateSettingAddon() { }

    @Override
    public void onOpenBefore(SettingsSubTab tab, ScreenArea screenArea, boolean firstOpen, AtomicInteger nextYPosition) {
        this.yPos = nextYPosition.getAndAdd(25);
        List<Component> options = new ArrayList<>();
        for(OutputConflictHandling type : OutputConflictHandling.values())
            options.add(LCText.GUI_TRADER_PAYGATE_CONFLICT_HANDLING.get(type).get());
        tab.addChild(DropdownWidget.builder()
                .position(screenArea.pos.offset(30,this.yPos + 10))
                .width(screenArea.width - 60)
                .options(options)
                .selected(this.getSelected())
                .selectAction(this::selectType)
                .build());
    }

    @Override
    public void onOpenAfter(@Nonnull SettingsSubTab tab, @Nonnull ScreenArea screenArea, boolean firstOpen, @Nonnull AtomicInteger nextYPosition) {

    }

    private int getSelected() {
        if(this.getTab() != null && this.getTab().menu.getTrader() instanceof PaygateTraderData paygate)
            return paygate.getConflictHandling().ordinal();
        return 0;
    }

    @Override
    public void renderBG(SettingsSubTab tab, EasyGuiGraphics gui) {
        gui.drawString(LCText.GUI_TRADER_PAYGATE_CONFLICT_LABEL.get(),30,this.yPos,0x404040);
    }

    @Override
    public void renderAfterWidgets(SettingsSubTab tab, EasyGuiGraphics gui) {

    }

    @Override
    public void tick(SettingsSubTab tab) {

    }

    @Override
    public void onClose(SettingsSubTab tab) {

    }

    private void selectType(int selected)
    {
        if(this.getTab() == null)
            return;
        this.getTab().sendMessage(this.getTab().builder().setInt("ChangeConflictMode",selected));
    }

}
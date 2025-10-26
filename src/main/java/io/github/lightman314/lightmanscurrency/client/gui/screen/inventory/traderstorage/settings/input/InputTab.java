package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.settings.client.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class InputTab extends SettingsSubTab {

    public InputTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    DirectionalSettingsWidget inputWidget;

    protected InputTraderData getInputTrader() {
        TraderData trader = this.menu.getTrader();
        if(trader instanceof InputTraderData t)
            return t;
        return null;
    }

    @Nonnull
    @Override
    public IconData getIcon() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsTabIcon();
        return ItemIcon.ofItem(Items.HOPPER);
    }

    @Override
    public MutableComponent getTooltip() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsTabTooltip();
        return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_GENERIC.get();
    }

    public List<? extends InputTabAddon> getAddons() {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
            return trader.inputSettingsAddons();
        return ImmutableList.of();
    }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.InputTrader.EXTERNAL_INPUTS); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.inputWidget = this.addChild(DirectionalSettingsWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2,25))
                .object(this::getInputTrader)
                .handlers(this::ToggleSide)
                .addon(EasyAddonHelper.visibleCheck(this::allowInputs))
                .build());

        this.getAddons().forEach(a -> a.onOpen(this, screenArea, firstOpen));

    }

    @Override
    protected void onSubtabClose() { this.getAddons().forEach(a -> a.onClose(this)); }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        //Side Widget Labels
        TextRenderUtil.drawCenteredText(gui,LCText.GUI_SETTINGS_INPUT_SIDE.get(), this.screen.getXSize() / 2, 7, 0x404040);

        this.getAddons().forEach(a -> a.renderBG(this, gui));

    }

    @Override
    public void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {

        this.getAddons().forEach(a -> a.renderAfterWidgets(this, gui));

    }

    @Override
    public void tick() { this.getAddons().forEach(a -> a.tick(this)); }

    private boolean allowInputs()
    {
        InputTraderData trader = this.getInputTrader();
        return trader != null && trader.allowInputs();
    }

    private boolean allowOutputs()
    {
        InputTraderData trader = this.getInputTrader();
        return trader != null && trader.allowOutputs();
    }

    private void ToggleSide(Direction side,boolean inverse)
    {
        InputTraderData trader = this.getInputTrader();
        if(trader != null)
        {
            DirectionalSettingsState state = trader.getSidedState(side);
            if(inverse)
                state = state.getPrevious(trader);
            else
                state = state.getNext(trader);

            this.sendMessage(this.builder()
                    .setString("SetDirectionalState",state.toString())
                    .setInt("Side",side.get3DDataValue()));
        }
    }

}

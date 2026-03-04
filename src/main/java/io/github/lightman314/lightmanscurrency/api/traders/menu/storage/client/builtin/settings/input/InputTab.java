package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.input;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.input.IInputClientNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;

public class InputTab extends SettingsSubTab {

    public InputTab(TraderSettingsClientTab parent) { super(parent); }

    DirectionalSettingsWidget inputWidget;

    @Override
    public IconData getIcon() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return IInputClientNode.lookupIcon(trader);
        return ItemIcon.ofItem(Items.HOPPER);
    }

    @Override
    public Component getTooltip() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return IInputClientNode.lookupTooltip(trader);
        return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_GENERIC.get();
    }

    public List<InputTabAddon> getAddons() {
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            return IInputClientNode.lookupAddons(trader);
        return ImmutableList.of();
    }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.InputTrader.EXTERNAL_INPUTS); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) {

        this.inputWidget = this.addChild(DirectionalSettingsWidget.builder()
                .position(screenArea.pos.offset(screenArea.width / 2,25))
                .object(() -> this.menu.getTraderNode(InputNode.TYPE))
                .handlers(this::ToggleSide)
                .addon(EasyAddonHelper.visibleCheck(this::allowInputs))
                .build());

        this.getAddons().forEach(a -> a.onOpen(this, screenArea, firstOpen));

    }

    @Override
    protected void onSubtabClose() { this.getAddons().forEach(a -> a.onClose(this)); }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        //Side Widget Labels
        TextRenderUtil.drawCenteredText(gui,LCText.GUI_SETTINGS_INPUT_SIDE.get(), this.screen.getXSize() / 2, 7, 0x404040);

        this.getAddons().forEach(a -> a.renderBG(this, gui));

    }

    @Override
    public void renderAfterWidgets(EasyGuiGraphics gui) {

        this.getAddons().forEach(a -> a.renderAfterWidgets(this, gui));

    }

    @Override
    public void tick() { this.getAddons().forEach(a -> a.tick(this)); }

    private boolean allowInputs()
    {
        InputNode node = this.menu.getTraderNode(InputNode.TYPE);
        return node != null && node.allowInputs();
    }

    private boolean allowOutputs()
    {
        InputNode node = this.menu.getTraderNode(InputNode.TYPE);
        return node != null && node.allowOutputs();
    }

    private void ToggleSide(Direction side,boolean inverse)
    {
        InputNode node = this.menu.getTraderNode(InputNode.TYPE);
        if(node != null)
        {
            DirectionalSettingsState state = node.getSidedState(side);
            if(inverse)
                state = state.getPrevious(node);
            else
                state = state.getNext(node);

            this.sendMessage(this.builder()
                    .setString("SetDirectionalState",state.toString())
                    .setInt("Side",side.get3DDataValue()));
        }
    }

}

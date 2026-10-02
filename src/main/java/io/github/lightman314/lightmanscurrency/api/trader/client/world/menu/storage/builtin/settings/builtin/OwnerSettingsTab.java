package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.OwnerSelectionWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class OwnerSettingsTab extends SettingsSubTab.ForNode<OwnerNode> {

    public OwnerSettingsTab(SettingsClientTab parent) { super(parent,OwnerNode.TYPE); }

    private static final IconData MANUAL_ICON = ItemIcon.of(Items.COMMAND_BLOCK);
    private static final IconData PLAYER_ICON = ItemIcon.of(ItemHelper.ALEX_HEAD);

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.PLAYER_HEAD); }

    @Override
    public Component getName() { return OwnerNode.SETTINGS_TOOLTIP.get(); }

    @Override
    public boolean isVisible() { return this.getPermission(LCPermissions.TRANSFER_OWNERSHIP); }

    private boolean manualMode = false;
    private boolean isManualMode() { return this.manualMode; }
    private boolean isNormalMode() { return !this.manualMode; }

    private TextBoxWrapper<String> playerOwnerInput;
    private OwnerSelectionWidget ownerSelectionWidget;

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        //Manual Player Selection
        this.playerOwnerInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(20,50))
                .ofWidth(area.width - 40)
                .withOldWidget(this.playerOwnerInput)
                .withMaxLength(16)
                .visible(this::isManualMode)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,80))
                .ofWidth(area.width - 40)
                .withText(OwnerNode.BUTTON_OWNER_SET_PLAYER)
                .onPress(this::setPlayerOwner)
                .visible(this::isManualMode)
                .active(this::canSetPlayerOwner)
                .build());

        //Fake Player selection for admins
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,110))
                .ofWidth(area.width - 40)
                .withText(OwnerNode.BUTTON_OWNER_SET_FAKEPLAYER)
                .onPress(this::setFakePlayerOwner)
                .visible(this::viewFakePlayerButton)
                .active(this::canSetPlayerOwner)
                .build());

        //Owner Selection
        this.ownerSelectionWidget = this.addChild(OwnerSelectionWidget.builder()
                .atPos(area.pos.offset(20,27))
                .ofWidth(area.width - 40)
                .rows(5)
                .selectedHolder(this::getCurrentOwner)
                .handler(this::setOwner)
                .oldWidget(this.ownerSelectionWidget)
                .visible(this::isNormalMode)
                .build());

        //Toggle Mode Button
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width - 25,5))
                .onPress(this::toggleInputMode)
                .withIcon(this::getModeIcon)
                .tooltip(TooltipSource.deferredSingle(this::getModeTooltip))
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        OwnerHolder currentOwner = this.getCurrentOwner();
        if(currentOwner != null)
            gui.text(OwnerNode.GUI_OWNER_CURRENT.get(currentOwner.getName()),20,10,0xFF404040,false);
    }

    @Nullable
    private OwnerHolder getCurrentOwner() {
        return this.getNodeValue(OwnerNode.TYPE,OwnerNode::getOwner);
    }

    private void setOwner(Owner newOwner) {
        this.sendSettingRequest(FancyPacketMap.map().set("setOwner",LCFancyPacketTypes.OWNER,newOwner));
    }

    private boolean canSetPlayerOwner() { return !this.playerOwnerInput.getValue().isBlank(); }
    private boolean viewFakePlayerButton() { return this.manualMode && LCApi.isInAdminMode(this.getPlayer()); }

    private void setPlayerOwner() {
        if(this.playerOwnerInput.getValue().isBlank())
            return;
        this.sendSettingRequest(FancyPacketMap.map().setString("setPlayerOwner",this.playerOwnerInput.getValue()));
        this.playerOwnerInput.setValue("");
    }

    private void setFakePlayerOwner() {
        if(this.playerOwnerInput.getValue().isBlank())
            return;
        this.sendSettingRequest(FancyPacketMap.map().setString("setFakePlayerOwner",this.playerOwnerInput.getValue()));
        this.playerOwnerInput.setValue("");
    }

    private void toggleInputMode() { this.manualMode = !this.manualMode; }

    private IconData getModeIcon() { return this.manualMode ? MANUAL_ICON : PLAYER_ICON; }

    private Component getModeTooltip() { return this.manualMode ? OwnerNode.TOOLTIP_OWNER_NODE_SELECTION.get() : OwnerNode.TOOLTIP_OWNER_NODE_MANUAL.get(); }

    @Override
    public boolean displayTitle() { return false; }

}

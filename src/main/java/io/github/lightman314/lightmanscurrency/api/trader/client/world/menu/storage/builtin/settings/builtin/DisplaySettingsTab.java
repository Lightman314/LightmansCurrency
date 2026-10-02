package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.GhostSlot;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.DisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DisplaySettingsTab extends SettingsSubTab.ForNode<DisplayNode> implements IMouseListener {

    public DisplaySettingsTab(SettingsClientTab parent) { super(parent,DisplayNode.TYPE); }

    private ScreenArea iconArea = ScreenArea.ZERO;
    private boolean iconViewable() { return INetworkController.visibleToNetwork(this); }
    private boolean iconEditable() { return this.iconViewable() && this.hasDisplayPermission(); }

    private TextBoxWrapper<String> nameInput;

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.WRITABLE_BOOK); }
    @Override
    public Component getName() { return DisplayNode.NAME.get(); }
    @Override
    public boolean isVisible() { return true; }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        DisplayNode node = this.getNode(DisplayNode.TYPE);
        if(node == null)
            return;

        this.iconArea = ScreenArea.of(area.pos.offset(area.halfWidth() - 9,95),18,18);

        this.nameInput = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(20,25))
                .ofWidth(area.width - 40)
                .withMaxLength(32)
                .withStartingString(node.getInternalCustomName())
                .withOldWidget(this.nameInput)
                .active(this::hasDisplayPermission)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,50))
                .ofWidth(74)
                .withText(DisplayNode.BUTTON_CHANGE_NAME)
                .onPress(this::setCustomName)
                .visible(this::hasDisplayPermission)
                .active(this::canSetName)
                .build());

        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(area.width - 93,50))
                .ofWidth(74)
                .withText(DisplayNode.BUTTON_RESET_NAME)
                .onPress(this::resetName)
                .visible(this::hasDisplayPermission)
                .build());

        //Add Ghost Slot for the trader icons
        this.addChild(GhostSlot.simpleItem(this.iconArea.pos,this::changeIcon).asProvider(this::iconEditable));

        //Add "Destroy Trader" button
        this.addChild(TextButton.builder()
                .atPos(area.pos.offset(20,118))
                .ofWidth(area.width - 40)
                .withText(WorldNode.BUTTON_TRADER_SETTINGS_DESTROY_TRADER)
                .tooltip(TooltipSource.simple(WorldNode.TOOLTIP_TRADER_SETTINGS_DESTROY_TRADER)
                        .withAutoWrap(ChatFormatting.RED,ChatFormatting.BOLD))
                .visible(this::isBlockLoaded)
                .active(this::canDestroyTrader)
                .onPress(this::destroyTrader)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        if(this.iconViewable()) {
            ScreenPosition iconPos = this.iconArea.pos.relativeTo(this.getCorner());
            gui.blitSlot(iconPos);
            boolean hovered = this.iconArea.isInArea(gui.getMousePos());
            if(hovered)
                gui.blitSlotHighlightBack(this.iconArea.pos);
            IconData icon = this.getNodeValue(DisplayNode.TYPE,DisplayNode::getCustomIcon);
            if(icon != null)
                IconRenderer.extractState(gui,icon,iconPos.offset(1,1));
            if(hovered)
                gui.blitSlotHighlightFront(this.iconArea.pos);
        }
    }

    private boolean hasDisplayPermission() { return this.getPermission(LCPermissions.EDIT_DISPLAY); }
    private boolean canSetName() {
        DisplayNode node = this.getNode();
        if(node == null || this.nameInput == null)
            return false;
        String input = this.nameInput.getValue();
        return !input.equals(node.getInternalCustomName()) && !input.isBlank();
    }

    private void setCustomName() {
        DisplayNode node = this.getNode();
        if(node == null || this.nameInput == null)
            return;
        String value = this.nameInput.getValue();
        if(!value.equals(node.getInternalCustomName()) && !value.isBlank())
            this.sendSettingRequest(FancyPacketMap.map().setString("changeName",value));
    }

    private void resetName() {
        DisplayNode node = this.getNode();
        if(node == null || this.nameInput == null)
            return;
        if(!node.getInternalCustomName().isEmpty()) {
            this.sendSettingRequest(FancyPacketMap.map().setString("changeName",""));
            this.nameInput.setValue("");
        }
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.iconEditable() && this.iconArea.isInArea(ScreenHelper.getMousePos(event))) {
            this.changeIcon(this.getMenu().getCarried());
            return true;
        }
        return false;
    }

    private void changeIcon(ItemStack iconItem) {
        this.sendSettingRequest(FancyPacketMap.map().setItem("cycleIcon",iconItem));
    }

    private boolean isBlockLoaded() {
        TraderBlockEntity be = this.getTrader().getNodeArgValue(WorldNode.TYPE,this.getPlayer().level(),WorldNode::getBlockEntity);
        return be != null;
    }

    private boolean canDestroyTrader() {
        return this.isBlockLoaded() && this.getPermission(LCPermissions.BREAK_TRADER).hasHigherPermission();
    }

    private void destroyTrader() {
        this.sendSettingRequest(WorldNode.TYPE,FancyPacketMap.flag("destroyTrader"));
    }

}
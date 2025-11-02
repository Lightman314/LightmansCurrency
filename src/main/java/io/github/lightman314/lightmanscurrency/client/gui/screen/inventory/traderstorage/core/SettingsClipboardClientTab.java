package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsSubNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.SettingsClipboardTab;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SettingsClipboardClientTab extends TraderStorageClientTab<SettingsClipboardTab> implements IScrollable {

    public SettingsClipboardClientTab(Object screen, SettingsClipboardTab commonTab) { super(screen, commonTab); }

    public static final int NODES_PER_PAGE = 8;

    private NodeSelections selections = new NodeSelections();

    private final List<Pair<NodeKey,PlainButton>> toggleSwitches = new ArrayList<>();

    private int scroll = 0;

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        TraderData trader = this.menu.getTrader();

        //Copy and Paste Buttons
        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(10,120))
                .width(74)
                .text(LCText.BUTTON_TRADER_SETTINGS_COPY)
                .pressAction(this::tryCopy)
                .addon(EasyAddonHelper.activeCheck(this.commonTab::canWriteSettings))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(122,120))
                .width(74)
                .text(LCText.BUTTON_TRADER_SETTINGS_PASTE)
                .pressAction(this::tryLoad)
                .addon(EasyAddonHelper.activeCheck(this.commonTab::canReadSettings))
                .build());

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(10,8))
                .height(14 * NODES_PER_PAGE)
                .scrollable(this)
                .build());

        this.addChild(ScrollListener.builder()
                .area(screenArea.ofSize(screenArea.width,14 * NODES_PER_PAGE + 30))
                .listener(this)
                .build());

        if(trader == null)
            return;

        if(firstOpen)
        {
            this.scroll = 0;
            this.selections = trader.defaultNodeSelections(this.menu.getPlayer());
        }

        this.toggleSwitches.clear();
        for(SettingsNode node : trader.getAllSettingNodes())
        {
            NodeKey key = new NodeKey(node.key,null);
            PlainButton button = this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(20,0))
                    .sprite(SpriteUtil.createCheckbox(() -> this.isNodeSelected(key)))
                    .pressAction(() -> this.toggleNode(key))
                    .build());
            this.toggleSwitches.add(Pair.of(key,button));
            LightmansCurrency.LogDebug("Adding node switch for " + key);
            //Add sub-node switches
            for(SettingsSubNode<?> subNode : node.getSubNodes())
            {
                NodeKey subKey = new NodeKey(node.key,subNode.getSubKey());
                button = this.addChild(PlainButton.builder()
                        .position(screenArea.pos.offset(30,0))
                        .sprite(SpriteUtil.createCheckbox(() -> this.isNodeSelected(subKey)))
                        .pressAction(() -> this.toggleNode(subKey))
                        .build());
                this.toggleSwitches.add(Pair.of(subKey,button));
                LightmansCurrency.LogDebug("Adding subnode switch for " + subKey);
            }
        }

    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {

        this.validateScroll();
        List<Pair<Boolean,Component>> visibleNodesNames = this.refactorToggleButtons();

        //Render the Slot BG
        gui.renderSlot(this.screen,this.commonTab.getSlot());

        //Render the node labels
        TraderData trader = this.menu.getTrader();
        int startIndex = this.scroll;

        int y = 9;
        for(var nodeName : visibleNodesNames)
        {
            gui.drawString(nodeName.getSecond(),nodeName.getFirst() ? 42 : 32,y,0x404040);
            y += 14;
        }
    }

    private List<Pair<Boolean,Component>> refactorToggleButtons() {

        List<Pair<Boolean,Component>> visibleNodes = new ArrayList<>();
        int index = 0;
        int visibleIndex = 0;
        TraderData trader = this.menu.getTrader();
        ScreenPosition corner = this.screen.getCorner();
        for(Pair<NodeKey, PlainButton> pair : this.toggleSwitches)
        {
            NodeKey nodeKey = pair.getFirst();
            SettingsNode node = trader == null ? null : trader.getNode(nodeKey.node);
            if (node == null || !node.allowSelecting(this.menu.getPlayer()))
                pair.getSecond().setVisible(false);
            else {
                if (index < this.scroll || (index >= this.scroll + NODES_PER_PAGE))
                    pair.getSecond().setVisible(false);
                else
                {
                    Pair<Boolean,Component> nodeName;
                    if(nodeKey.hasSubnode())
                    {
                        SettingsSubNode<?> subNode = node.getSubNode(nodeKey.subNode);
                        //Hide sub-nodes if the main node isn't selected
                        if(subNode == null || !subNode.allowSelecting(this.menu.getPlayer()) || !this.isNodeSelected(new NodeKey(node.key,null)))
                        {
                            pair.getSecond().setVisible(false);
                            continue;
                        }
                        nodeName = Pair.of(true,subNode.getName());
                    }
                    else
                        nodeName = Pair.of(false,node.getName());
                    PlainButton toggle = pair.getSecond();
                    toggle.setVisible(true);
                    toggle.setPosition(corner.offset(nodeName.getFirst() ? 30 : 20,8 + (14 * visibleIndex)));
                    visibleNodes.add(nodeName);
                    visibleIndex++;
                }
                index++;
            }
        }
        return visibleNodes;
    }

    @Override
    public IconData getIcon() { return IconUtil.ICON_COUNT; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_CLIPBOARD.get(); }

    private boolean isNodeSelected(NodeKey key) {
        if(this.selections.nodeSelected(key.node))
            return !key.hasSubnode() || this.selections.subNodeSelected(key.node,key.subNode);
        return false;
    }

    private void toggleNode(NodeKey key) {
        if(key.hasSubnode())
            this.selections.toggleSubNode(key.node,key.subNode);
        else
        {
            this.selections.toggleNode(key.node);
            //By default, select all sub-nodes when a full node is selected
            if(this.selections.nodeSelected(key.node))
            {
                TraderData trader = this.menu.getTrader();
                SettingsNode node = trader == null ? null : trader.getNode(key.node);
                if(node != null)
                {
                    for(SettingsSubNode<?> subNode : node.getSubNodes())
                        this.selections.addSubNode(node.key,subNode.getSubKey());
                }
            }
        }
    }

    private void tryCopy() { this.commonTab.copySettings(this.selections); }

    private void tryLoad() { this.commonTab.loadSettings(this.selections); }

    public int getVisibleNodes() {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return 0;
        return (int)this.toggleSwitches.stream().filter(p -> {
            NodeKey nodeKey = p.getFirst();
            SettingsNode node = trader.getNode(nodeKey.node);
            if(node != null && node.allowSelecting(this.menu.getPlayer()))
            {
                if(nodeKey.hasSubnode())
                {
                    SettingsSubNode<?> subNode = node.getSubNode(nodeKey.subNode);
                    return subNode != null && subNode.allowSelecting(this.menu.getPlayer()) && this.selections.nodeSelected(nodeKey.node);
                }
                //If no sub-node, we've already passed all the requirements
                return true;
            }
            return false;
        }).count();
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(NODES_PER_PAGE,this.getVisibleNodes()); }

    private record NodeKey(String node,@Nullable String subNode) {
        boolean hasSubnode() { return this.subNode != null; }
        @Override
        public String toString() { return this.hasSubnode() ? this.node + "." + this.subNode : this.node; }
    }

}
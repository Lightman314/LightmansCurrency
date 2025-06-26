package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.core;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.settings.data.NodeSelections;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.core.SettingsClipboardTab;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SettingsClipboardClientTab extends TraderStorageClientTab<SettingsClipboardTab> implements IScrollable {

    public SettingsClipboardClientTab(Object screen, SettingsClipboardTab commonTab) { super(screen, commonTab); }

    public static final int NODES_PER_PAGE = 8;

    private NodeSelections selections = new NodeSelections();

    private final List<Pair<String,PlainButton>> toggleSwitches = new ArrayList<>();

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
            PlainButton button = this.addChild(PlainButton.builder()
                    .position(screenArea.pos.offset(20,0))
                    .sprite(IconAndButtonUtil.SPRITE_CHECK(() -> this.isNodeSelected(node.key)))
                    .pressAction(() -> this.toggleNode(node.key))
                    .build());
            this.toggleSwitches.add(Pair.of(node.key,button));
        }

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

        this.validateScroll();
        List<SettingsNode> visibleNodes = this.refactorToggleButtons();

        //Render the Slot BG
        gui.renderSlot(this.screen,this.commonTab.getSlot());

        //Render the node labels
        TraderData trader = this.menu.getTrader();
        int startIndex = this.scroll;

        int y = 9;
        for(SettingsNode node : visibleNodes)
        {
            gui.drawString(node.getName(),32,y,0x404040);
            y += 14;
        }
    }

    private List<SettingsNode> refactorToggleButtons() {

        List<SettingsNode> visibleNodes = new ArrayList<>();
        int index = 0;
        int visibleIndex = 0;
        TraderData trader = this.menu.getTrader();
        ScreenPosition corner = this.screen.getCorner();
        for(Pair<String, PlainButton> pair : this.toggleSwitches)
        {
            SettingsNode node = trader == null ? null : trader.getNode(pair.getFirst());
            if (node == null || !node.allowSelecting(this.menu.getPlayer()))
                pair.getSecond().setVisible(false);
            else {
                if (index < this.scroll || (index >= this.scroll + NODES_PER_PAGE))
                    pair.getSecond().setVisible(false);
                else
                {
                    PlainButton toggle = pair.getSecond();
                    toggle.setVisible(true);
                    toggle.setPosition(corner.offset(20,8 + (14 * visibleIndex)));
                    visibleNodes.add(node);
                    visibleIndex++;
                }
                index++;
            }
        }
        return visibleNodes;

    }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_COUNT; }

    @Nullable
    @Override
    public Component getTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_CLIPBOARD.get(); }

    private boolean isNodeSelected(String node) { return this.selections.nodeSelected(node); }

    private void toggleNode(String node) { this.selections.toggleNode(node); }

    private void tryCopy() { this.commonTab.copySettings(this.selections); }

    private void tryLoad() { this.commonTab.loadSettings(this.selections); }

    public int getVisibleNodes() {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return 0;
        return (int)this.toggleSwitches.stream().filter(p -> {
            SettingsNode node = trader.getNode(p.getFirst());
            return node != null && node.allowSelecting(this.menu.getPlayer());
        }).count();
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() {
        return IScrollable.calculateMaxScroll(NODES_PER_PAGE,this.getVisibleNodes());
    }
}

package io.github.lightman314.lightmanscurrency.features.trader.misc.client;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.features.trader.misc.FlexibleItemStorage;
import io.github.lightman314.lightmanscurrency.features.trader.misc.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.features.trader.misc.ItemStorageTab;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.text.DecimalFormat;

public class ItemStorageClientTab extends TraderStorageClientTab<ItemStorageTab> implements IScrollable, IMouseListener {

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 17;

    private static final int COLUMNS = 8;
    private static final int COLUMNS_WITHOUT_UPGRADES = 10;
    private static final int ROWS = 6;

    public static final TabBuilder<TraderStorageMenu,ItemStorageTab,TraderStorageTab,TraderStorageScreen> BUILDER = ItemStorageClientTab::new;

    protected ItemStorageClientTab(TraderStorageMenu menu, ItemStorageTab commonTab, TraderStorageScreen screen) { super(menu, commonTab, screen); }

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.CHEST); }
    @Override
    public Component getName() { return LCText.Trader.TOOLTIP_ITEM_STORAGE.get(); }

    private int columns = Integer.MAX_VALUE;
    private ScreenArea slotArea = ScreenArea.ZERO;

    private int scroll = 0;
    private int storageSize() {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        return node != null ? node.getStorage().size() : 0;
    }
    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.storageSize(),this.columns,this.columns * ROWS); }

    protected boolean useBonusColumns() {
        return !this.hasNode(UpgradeNode.TYPE);
    }

    @Override
    protected void initialize(ScreenArea area,FancyPacketMap message) {
        //Calculate our column count and slot area
        if(this.useBonusColumns())
            this.columns = COLUMNS_WITHOUT_UPGRADES;
        else
            this.columns = COLUMNS;
        this.slotArea = ScreenArea.of(area.pos.offset(X_OFFSET,Y_OFFSET),this.columns * 18,ROWS * 18);
        this.addChild(this);
        //Add the scroll listener for the relevant area
        this.addChild(ScrollArea.builder()
                .atPosition(area.pos)
                .ofSize(area.width,ROWS * 18 + 32)
                .forScrollable(this)
                .build());
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render the upgrade slots
        gui.blitSlots(this.getCommonTab().getSlots());
        //Render the rest of the slots
        gui.pushZero();
        int slot = this.scroll * this.columns;
        FlexibleItemStorage storage = this.getNodeValue(ItemStorageNode.TYPE,ItemStorageNode::getStorage,null);
        for(int x = 0; x < this.columns; ++x)
        {
            for(int y = 0; y < ROWS; ++y)
            {
                ItemStack stack = storage == null ? ItemStack.EMPTY : storage.getStack(slot++);
                ScreenPosition slotPos = this.slotArea.pos.offset(x * 18,y * 18);
                gui.blitSlot(slotPos);
                boolean hovered = ScreenArea.of(slotPos,18,18).isMouseInArea(gui.getMousePos());
                if(hovered) //Slot highlight before the item
                    gui.blitSlotHighlightBack(slotPos);
                //The item itself
                gui.item(stack,slotPos.offset(1,1),ItemHelper.getBigStackText(stack));
                if(hovered) //The slot hightlight after the item
                {
                    gui.blitSlotHighlightFront(slotPos);
                    //Also render the tooltip here if the stack isn't empty
                    if(storage != null && !stack.isEmpty())
                    {
                        gui.renderItemTooltipAtMouse(stack,tooltip ->
                            tooltip.add(formatItemCount(stack.getCount(),storage.getCapacity(),ChatFormatting.YELLOW))
                        );
                    }
                }

            }
        }
        gui.pop(); //Undo our zero push
    }

    public static Component formatItemCount(int count, int maxCount, ChatFormatting... formatting) {
        DecimalFormat format = new DecimalFormat();
        MutableComponent c = Component.literal(format.format(count));
        if(count == maxCount)
            c.withStyle(ChatFormatting.GOLD);
        else if(count > maxCount)
            c.withStyle(ChatFormatting.DARK_RED);
        return LCText.Misc.TOOLTIP_ITEM_COUNT.get(c,format.format(maxCount)).withStyle(formatting);
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(this.slotArea.isMouseInArea(event.x(),event.y()))
        {
            int localX = (int)Math.round(event.x()) - this.slotArea.x;
            int localY = (int)Math.round(event.y()) - this.slotArea.y;
            int col = localX / 18;
            int row = localY / 18;
            int slot = (row * this.columns) + col + (this.scroll * this.columns);
            this.getCommonTab().onItemClick(slot,event.button());
            return true;
        }
        return false;
    }


}
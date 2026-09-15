package io.github.lightman314.lightmanscurrency.client.features.trader.item_common;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.IngredientResult;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IMouseListener;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRecipeIngredientViewer;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.PersistentDataNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.UpgradeNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.FlexibleItemStorage;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.features.trader.item_common.ItemStorageTab;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class ItemStorageClientTab extends TraderStorageClientTab<ItemStorageTab> implements IScrollable, IMouseListener, IRecipeIngredientViewer {

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
    public Component getName() { return ItemStorageTab.TOOLTIP_ITEM_STORAGE.get(); }

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
        //Add the scroll listener for the relevant area
        this.addChild(ScrollArea.builder()
                .atPosition(area.pos)
                .ofSize(area.width,ROWS * 18 + 32)
                .forScrollable(this)
                .build());

        //Add quick-move buttons
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(24,Y_OFFSET + 18 * ROWS + 4))
                .onPress(this.getCommonTab()::quickInsert)
                .withSprite(LCSprites.BUTTON_QUICK_INSERT)
                .visible(this::quickButtonsVisible)
                .build());
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(36,Y_OFFSET + 18 * ROWS + 4))
                .onPress(this.getCommonTab()::quickExtract)
                .withSprite(LCSprites.BUTtON_QUICK_EXTRACT)
                .visible(this::quickButtonsVisible)
                .build());

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render the title
        gui.text(this.getName(),8,6,0xFF404040,false);
        //Render the upgrade slots
        gui.blitSlots(this.getCommonTab().getSlots());
        //Render the rest of the slots
        gui.pushZero();
        int slot = this.scroll * this.columns;
        FlexibleItemStorage storage = this.getNodeValue(ItemStorageNode.TYPE,ItemStorageNode::getStorage,null);
        for(int y = 0; y < ROWS; ++y)
        {
            for(int x = 0; x < this.columns; ++x)
            {
                ItemStack stack = storage == null ? ItemStack.EMPTY : storage.getStack(slot++);
                ScreenPosition slotPos = this.slotArea.pos.offset(x * 18,y * 18);
                gui.blitSlot(slotPos);
                boolean hovered = ScreenArea.of(slotPos,18,18).isInArea(gui.getMousePos());
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
        MutableComponent c = Component.literal(NumberHelper.prettyInteger(count));
        if(count == maxCount)
            c.withStyle(ChatFormatting.GOLD);
        else if(count > maxCount)
            c.withStyle(ChatFormatting.DARK_RED);
        return LCText.TOOLTIP_ITEM_COUNT.get(c,NumberHelper.prettyInteger(maxCount)).withStyle(formatting);
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        HoveredSlotResults hoveredSlot = this.getHoveredSlot(ScreenHelper.getMousePos(event));
        if(hoveredSlot.isPresent())
        {
            this.getCommonTab().onItemClick(hoveredSlot.slot,event.button(),event.hasShiftDown());
            return true;
        }
        return false;
    }

    private HoveredSlotResults getHoveredSlot(ScreenPosition mousePos) {
        if(!this.slotArea.isInArea(mousePos))
            return HoveredSlotResults.NULL;
        int localX = mousePos.x - this.slotArea.x;
        int localY = mousePos.y - this.slotArea.y;
        int col = localX / 18;
        int row = localY / 18;
        return new HoveredSlotResults((row * this.columns) + col + (this.scroll * this.columns),col,row);
    }

    private boolean quickButtonsVisible() {
        ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
        return node != null && !this.getNodeValue(PersistentDataNode.TYPE,PersistentDataNode::isPersistent,false);
    }

    @Override
    public Optional<IngredientResult> getHoveredIngredient(ScreenPosition mousePos) {
        HoveredSlotResults hoveredSlot = this.getHoveredSlot(mousePos);
        if(hoveredSlot.isPresent()) {
            ItemStorageNode node = this.getNode(ItemStorageNode.TYPE);
            if(node == null)
                return Optional.empty();
            ItemStack hoveredStack = node.getStorage().getStack(hoveredSlot.slot);
            return IngredientResult.forItem(hoveredStack,this.slotArea.pos.offset(hoveredSlot.column * 18,hoveredSlot.row * 18));
        }
        return Optional.empty();
    }

    record HoveredSlotResults(int slot,int column,int row) {
        static final HoveredSlotResults NULL = new HoveredSlotResults(-1,0,0);
        boolean isPresent() { return this.slot >= 0; }
    }

}
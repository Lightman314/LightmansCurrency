package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.items.data.FilterData;
import io.github.lightman314.lightmanscurrency.common.menus.ItemFilterMenu;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemFilterScreen extends EasyMenuScreen<ItemFilterMenu> implements IScrollable {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/item_filter.png");

    public static final int ROWS = 5;

    private ItemStack fakeSlotItem = ItemStack.EMPTY;

    private int scroll = 0;
    private final List<FilterOption> filterOptions = new ArrayList<>();

    private ScreenArea filterItemArea = ScreenArea.of(0,0,18,18);
    private ScreenArea fakeSlotArea = ScreenArea.of(0,0,18,18);

    public ItemFilterScreen(ItemFilterMenu menu, Inventory inventory, Component ignored) {
        super(menu, inventory);
        this.resize(176,216);
        this.menu.setQuickMoveConsumer(this::setFakeSlotItem);
    }

    public void setFakeSlotItem(ItemStack item)
    {
        if(item.getItem() == this.fakeSlotItem.getItem())
        {
            this.fakeSlotItem = item.copyWithCount(1);
            return;
        }
        this.fakeSlotItem = item.copyWithCount(1);
        this.filterOptions.clear();
        if(this.fakeSlotItem.isEmpty())
        {
            this.scroll = 0;
            return;
        }
        Item newItem = this.fakeSlotItem.getItem();
        //Use a clean item stack so that the filter description uses the actual name, and not an anvil-generated name (etc.)
        this.filterOptions.add(new ItemFilterOption(ForgeRegistries.ITEMS.getKey(newItem),new ItemStack(newItem)));
        this.fakeSlotItem.getTags().forEach(key -> this.filterOptions.add(new TagFilterOption(key.location())));
        this.validateScroll();
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.filterItemArea = this.filterItemArea.atPosition(screenArea.pos.offset(7,17));
        this.fakeSlotArea = this.fakeSlotArea.atPosition(screenArea.pos.offset(7,101));

        this.addChild(ScrollListener.builder()
                .listener(this)
                .position(screenArea.pos.offset(25,17))
                .size(144,102)
                .build());

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(161,17))
                .height(2 + (20 * ROWS))
                .scrollable(this)
                .build());

        for(int i = 0; i < ROWS; ++i)
        {
            final int index = i;
            this.addChild(IconButton.builder()
                    .position(screenArea.pos.offset(140,18 + (20 * index)))
                    .icon(() -> this.getIcon(index))
                    .pressAction(() -> this.toggleOption(index))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.optionExists(index)))
                    .build());
        }

        //Create a ghost slot provider
        this.addChild(new GhostSlot<>(this.fakeSlotArea,this::setFakeSlotItem,ItemStack.class).asProvider());

    }

    @Override
    protected void renderBG(EasyGuiGraphics gui) {

        gui.renderNormalBackground(GUI_TEXTURE,this);

        ItemStack filterItem = this.menu.getTargetedStack();
        //Render Label
        gui.drawString(filterItem.getHoverName(),6,8,0x404040);
        //Render Fake Slot
        gui.renderItem(filterItem,8,18);

        //Render Fake Slot
        gui.renderItem(this.fakeSlotItem,8,102);

        FilterData data = this.menu.getData();
        //Render Options
        for(int i = 0; i < ROWS; ++i)
        {
            FilterOption option = this.getOption(i);
            if(option != null)
            {
                int color = option.isActive(data) ? 0x00FF00 : 0xFFFFFF;
                Component text = TextRenderUtil.fitString(option.getText(),112,"");
                gui.drawString(text,28,24 + (20 * i),color);
            }
        }

    }

    @Override
    protected void renderAfterWidgets(EasyGuiGraphics gui) {
        if(!this.menu.getCarried().isEmpty())
            return;
        if(this.filterItemArea.isMouseInArea(gui.mousePos))
            gui.renderTooltip(this.menu.getTargetedStack());
        else if(this.fakeSlotArea.isMouseInArea(gui.mousePos) && !this.fakeSlotItem.isEmpty())
            gui.renderTooltip(this.fakeSlotItem);
        ScreenPosition corner = this.getCorner();
        ScreenPosition mousePos = gui.mousePos;
        if(mousePos.x >= corner.x + 26 && mousePos.x < corner.x + 140 && mousePos.y >= corner.y + 18 && mousePos.y < corner.y + 118)
        {
            //Render overflow text as a tooltip
            int index = (mousePos.y - corner.y - 18) / 20;
            FilterOption option = this.getOption(index);
            if(option != null)
                gui.renderComponentTooltip(TooltipHelper.splitTooltips(option.getText()));
        }
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ROWS,this.filterOptions.size()); }

    private boolean optionExists(int relativeIndex) { return this.getOption(relativeIndex) != null; }

    private IconData getIcon(int relativeIndex)
    {
        FilterOption option = this.getOption(relativeIndex);
        if(option != null && option.isActive(this.menu.getData()))
            return IconUtil.ICON_MINUS;
        return IconUtil.ICON_PLUS;
    }

    @Nullable
    private FilterOption getOption(int relativeIndex)
    {
        int index = relativeIndex + this.scroll;
        if(index < 0 || index >= this.filterOptions.size())
            return null;
        return this.filterOptions.get(index);
    }

    private void toggleOption(int relativeIndex)
    {
        FilterOption option = this.getOption(relativeIndex);
        if(option != null)
        {
            if(option.isActive(this.menu.getData()))
                option.onRemove(this.menu);
            else
                option.onAdd(this.menu);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.fakeSlotArea.isMouseInArea(mouseX,mouseY))
        {
            this.setFakeSlotItem(this.menu.getCarried());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private interface FilterOption
    {
        Component getText();
        boolean isActive(FilterData filter);
        void onAdd(ItemFilterMenu menu);
        void onRemove(ItemFilterMenu menu);
    }

    private record ItemFilterOption(ResourceLocation itemID,ItemStack item) implements FilterOption
    {
        @Override
        public Component getText() { return this.item.getHoverName(); }
        @Override
        public boolean isActive(FilterData filter) { return filter.entries().contains(this.itemID); }
        @Override
        public void onAdd(ItemFilterMenu menu) { menu.SendMessage(menu.builder().setResourceLocation("AddEntry",this.itemID)); }
        @Override
        public void onRemove(ItemFilterMenu menu) { menu.SendMessage(menu.builder().setResourceLocation("RemoveEntry",this.itemID)); }
    }

    private record TagFilterOption(ResourceLocation tag) implements FilterOption
    {
        @Override
        public Component getText() { return EasyText.literal("#" + this.tag.toString()); }
        @Override
        public boolean isActive(FilterData filter) { return filter.tags().contains(this.tag); }
        @Override
        public void onAdd(ItemFilterMenu menu) { menu.SendMessage(menu.builder().setResourceLocation("AddTag",this.tag)); }
        @Override
        public void onRemove(ItemFilterMenu menu) { menu.SendMessage(menu.builder().setResourceLocation("RemoveTag",this.tag)); }

    }

}
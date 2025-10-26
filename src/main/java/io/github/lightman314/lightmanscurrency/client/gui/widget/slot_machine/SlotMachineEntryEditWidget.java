package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.GhostSlot;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IGhostSlotProvider;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.HorizScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.DoubleParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlotMachineEntryEditWidget extends EasyWidgetWithChildren implements IEasyTickable, ITooltipSource, IGhostSlotProvider, IScrollable {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 64;

    public final SlotMachineEntryClientTab tab;
    public final Supplier<Integer> entryIndex;

    private TextBoxWrapper<Double> oddsEdit;
    private PlainButton removeEntryButton;

    private int previousIndex = -1;

    private static final int ITEM_POSY = 34;

    private SlotMachineEntryEditWidget(Builder builder)
    {
        super(builder);
        this.tab = builder.tab;
        this.entryIndex = builder.index;
    }

    @Override
    public void addChildren(ScreenArea area) {
        SlotMachineEntry entry = this.getEntry();

        this.oddsEdit = this.addChild(TextInputUtil.doubleBuilder()
                .position(area.pos.offset(this.tab.getFont().width(LCText.GUI_TRADER_SLOT_MACHINE_ODDS_INPUT_LABEL_PRE.get()) - 2,11))
                .size(36,10)
                .maxLength(5)
                .startingString(entry == null ? "0.01" : entry.getOddsString())
                .apply(DoubleParser.builder()
                        .min(0.01)
                        .max(99.99)
                        .empty(0.01)
                        .consumer())
                .handler(this::onOddsChanged)
                .wrap()
                .addon(EasyAddonHelper.visibleCheck(this::hasEntry))
                .addon(EasyAddonHelper.activeCheck(this::hasPermissions))
                .build());

        this.removeEntryButton = this.addChild(PlainButton.builder()
                .position(area.pos)
                .pressAction(this::removeEntry)
                .sprite(SpriteUtil.BUTTON_SIGN_MINUS)
                .addon(EasyAddonHelper.visibleCheck(this::hasPermissions))
                .build());

        this.addChild(PlainButton.builder()
                .position(area.pos.offset(80,0))
                .sprite(SpriteUtil.createCheckbox(this::hasCustomIcons))
                .pressAction(this::toggleCustomIcons)
                .build());

        int sliderWidth = 100 + HorizScrollBarWidget.SLIDER_SPRITE.getWidth();
        this.addChild(HorizScrollBarWidget.builder()
                .position(area.pos.offset((WIDTH - sliderWidth)/2,24))
                .width(sliderWidth)
                .scrollable(this)
                .customKnob(HorizScrollBarWidget.SLIDER_SPRITE)
                .addon(EasyAddonHelper.activeCheck(this::hasPermissions))
                .build());

    }

    private SlotMachineEntry getEntry() { return this.tab.getEntry(this.entryIndex.get()); }

    private boolean hasEntry() { return this.getEntry() != null; }

    private boolean hasPermissions() { return this.hasEntry() && this.tab.menu.hasPermission(Permissions.EDIT_TRADES); }

    private boolean hasCustomIcons() {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
            return entry.hasCustomIcons();
        return false;
    }

    //IScrollable imitations for selecting percentage
    @Override
    public int currentScroll() {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
            return (int)Math.round(entry.getOdds());
        return 0;
    }
    @Override
    public void setScroll(int newScroll) {
        double newOdds = MathUtil.clamp(newScroll,0.01d,99.99d);
        //Manually change the text input with the new value, and it'll automatically trigger the packet
        this.oddsEdit.setStringValue(SlotMachineEntry.ODDS_FORMATTER.format(newOdds));
    }
    @Override
    public int getMaxScroll() { return 100; }

    @Override
    public void renderWidget(EasyGuiGraphics gui) {

        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            //Draw label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ENTRY_LABEL.get(this.entryIndex.get() + 1).withStyle(entry.isValid() ? s -> s : s->s.withColor(ChatFormatting.RED)), 12, 1, 0x404040);
            //Draw Odds label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ODDS_INPUT_LABEL_PRE.get(), 0, 12, 0x404040);
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ODDS_INPUT_LABEL_SUF.get(), this.oddsEdit.getX() - this.getX() + this.oddsEdit.getWidth() + 2, 12, 0x404040);
            //Draw Custom Icons Label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_CUSTOM_ICON_LABEL.get(), 92,1,0x404040);

            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                int xPos = (18 * i);
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                    gui.renderItem(entry.items.get(i), xPos, ITEM_POSY);
                else
                    gui.renderSlotBackground(EasySlot.BACKGROUND, xPos, ITEM_POSY);
            }

            //Render Icons
            if(entry.hasCustomIcons())
            {
                for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
                {
                    int xPos = 80 + (18 * i);
                    SpriteUtil.EMPTY_SLOT_NORMAL.render(gui,xPos - 1,ITEM_POSY - 1);
                    if(i >= entry.getCustomIcons().size())
                        SlotMachineEntry.DEFAULT_ICON.render(gui,xPos,ITEM_POSY);
                    else
                        entry.getCustomIcons().get(i).render(gui,xPos,ITEM_POSY);
                }
            }

        }

    }

    @Override
    protected boolean isValidClickButton(int button) { return button == 0 || button == 1; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //Confirm that the mouse is in the general area
        if(this.clicked(mouseX, mouseY) && this.isValidClickButton(button))
        {
            boolean rightClick = button == 1;
            SlotMachineEntry entry = this.getEntry();
            if(entry != null)
            {
                int entryIndex = this.entryIndex.get();
                ItemStack heldItem = this.tab.menu.getHeldItem();
                if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
                {
                    int itemIndex = getItemSlotIndex(mouseX);
                    if(itemIndex >= 0)
                    {
                        if(itemIndex >= entry.items.size())
                        {
                            if(!heldItem.isEmpty())
                            {
                                if(rightClick) //If right-clicked, set as 1
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem.copyWithCount(1));
                                else //Otherwise add whole stack
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem);
                                return true;
                            }
                        }
                        else if(heldItem.isEmpty())
                        {
                            if(rightClick) //If right-clicked, reduce by 1
                            {
                                ItemStack newStack = entry.items.get(itemIndex).copy();
                                newStack.shrink(1);
                                if(newStack.isEmpty())
                                    this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                                else
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, newStack);
                            }
                            else //If left-clicked, remove entirely
                                this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                            return true;
                        }
                        else {
                            if(rightClick) //If right-clicked, either set as 1 or increase by 1
                            {
                                ItemStack oldStack = entry.items.get(itemIndex);
                                if(InventoryUtil.ItemMatches(heldItem, oldStack))
                                {
                                    ItemStack newStack = entry.items.get(itemIndex).copy();
                                    if(newStack.getCount() >= newStack.getMaxStackSize())
                                        return false;
                                    newStack.grow(1);
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, newStack);
                                }
                                else
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, heldItem.copyWithCount(1));
                                return true;
                            }
                            else //Replace with new held item
                                this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, heldItem);
                        }
                    }
                    else
                    {
                        int iconIndex = getIconSlotIndex(mouseX);
                        if(iconIndex >= 0)
                        {
                            IconData newIcon = heldItem.isEmpty() ? SlotMachineEntry.DEFAULT_ICON : ItemIcon.ofItem(heldItem.copyWithCount(1));
                            this.tab.commonTab.ChangeEntryCustomIcon(this.entryIndex.get(),iconIndex,newIcon);
                        }
                    }
                }
            }
        }
        return false;
    }

    private int getItemSlotIndex(double mouseX)
    {
        int x = (int)mouseX - this.getX();
        if(x < 0)
            return -1;
        int result = x / 18;
        return result >= SlotMachineEntry.ITEM_LIMIT ? -1 : result;
    }

    private int getIconSlotIndex(double mouseX) {
        int x = (int)mouseX - this.getX();
        if(x < 80)
            return -1;
        int result = (x - 80) / 18;
        return result >= SlotMachineEntry.ITEM_LIMIT ? -1 : result;
    }

    @Override
    public void tick()
    {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null && this.tab.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            if(trader.areEntriesChanged())
            {
                this.oddsEdit.setStringValue(entry.getOddsString());
                return;
            }
            int thisIndex = this.entryIndex.get();
            if(thisIndex != this.previousIndex)
                this.oddsEdit.setStringValue(entry.getOddsString());
            this.previousIndex = thisIndex;
        }
        else
            this.oddsEdit.visible = this.removeEntryButton.visible = false;
    }

    private void removeEntry(EasyButton button) { this.tab.commonTab.RemoveEntry(this.entryIndex.get()); }

    private void onOddsChanged(double newOdds)
    {
        int thisIndex = this.entryIndex.get();
        this.tab.commonTab.ChangeEntryOdds(thisIndex,newOdds);
    }

    private void toggleCustomIcons()
    {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
            this.tab.commonTab.ChangeEntryHasCustomIcons(this.entryIndex.get(),!entry.hasCustomIcons());
    }

    @Override
    public List<Component> getTooltipText(int mouseX, int mouseY) {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
            {
                int itemIndex = this.getItemSlotIndex(mouseX);
                if(itemIndex >= 0 && itemIndex < entry.items.size())
                {
                    ItemStack item = entry.items.get(itemIndex);
                    if(!item.isEmpty())
                        return EasyScreenHelper.getTooltipFromItem(item);
                }
            }
        }
        return null;
    }

    public static Builder builder() { return new Builder(); }

    @Nullable
    @Override
    public List<GhostSlot<?>> getGhostSlots() {
        SlotMachineEntry entry = this.getEntry();
        if(entry == null)
            return null;
        List<GhostSlot<?>> results = new ArrayList<>();
        for(int i = 0; i < 4; ++i)
        {
            ScreenPosition pos = ScreenPosition.of(18 * i,ITEM_POSY).offset(this.getPosition());
            final int index = i;
            results.add(GhostSlot.simpleItem(pos,item -> this.handleGhostItem(item,index)));
        }
        if(entry.hasCustomIcons())
        {
            for(int i = 0; i < 4; ++i)
            {
                ScreenPosition pos = ScreenPosition.of(80 + (18 * i),ITEM_POSY).offset(this.getPosition());
                final int index = i;
                results.add(GhostSlot.simpleItem(pos,item -> this.onIconClick(item,index)));
            }
        }
        return results;
    }

    private void handleGhostItem(ItemStack item, int index)
    {
        int entryIndex = this.entryIndex.get();
        SlotMachineEntry entry = this.tab.getEntry(entryIndex);
        if(entry != null)
        {
            if(index >= entry.items.size())
                this.tab.commonTab.AddEntryItem(entryIndex,item);
            else
            {
                ItemStack existingItem = entry.items.get(index).copy();
                if(InventoryUtil.ItemMatches(existingItem,item))
                {
                    if(existingItem.getCount() < existingItem.getMaxStackSize())
                    {
                        existingItem.grow(1);
                        this.tab.commonTab.EditEntryItem(entryIndex,index,existingItem);
                    }
                }
                else
                    this.tab.commonTab.EditEntryItem(entryIndex,index,item);
            }
        }
    }

    private void onIconClick(ItemStack item, int index)
    {
        int entryIndex = this.entryIndex.get();
        SlotMachineEntry entry = this.tab.getEntry(entryIndex);
        if(entry != null)
            this.tab.commonTab.ChangeEntryCustomIcon(entryIndex,index,ItemIcon.ofItem(item.copyWithCount(1)));
    }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(WIDTH,HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        private SlotMachineEntryClientTab tab = null;
        private Supplier<Integer> index = () -> 0;

        public Builder tab(SlotMachineEntryClientTab tab) { this.tab = tab; return this; }
        public Builder index(Supplier<Integer> index) { this.index = index; return this; }

        public SlotMachineEntryEditWidget build() { return new SlotMachineEntryEditWidget(this); }

    }

}

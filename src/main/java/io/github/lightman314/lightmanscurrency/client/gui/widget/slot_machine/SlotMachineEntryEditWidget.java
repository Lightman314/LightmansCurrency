package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryEditWidget extends EasyWidgetWithChildren implements IEasyTickable, ITooltipSource {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 46;

    public final SlotMachineEntryClientTab tab;
    public final Supplier<Integer> entryIndex;

    private EditBox weightEdit;
    private PlainButton removeEntryButton;

    private int previousIndex = -1;

    private static final int ITEM_POSY = 22;

    private SlotMachineEntryEditWidget(Builder builder)
    {
        super(builder);
        this.tab = builder.tab;
        this.entryIndex = builder.index;
    }

    @Override
    public void addChildren(@Nonnull ScreenArea area) {
        SlotMachineEntry entry = this.getEntry();

        this.weightEdit = this.addChild(TextInputUtil.intBuilder()
                .position(area.pos.offset(this.tab.getFont().width(LCText.GUI_TRADER_SLOT_MACHINE_WEIGHT_LABEL.get()),10))
                .size(36,10)
                .maxLength(4)
                .startingValue(entry == null ? 1 : entry.getWeight())
                .apply(IntParser.builder()
                        .min(1)
                        .max(1000)
                        .empty(1)
                        .consumer())
                .handler(this::onWeightChanged)
                .build());

        this.removeEntryButton = this.addChild(PlainButton.builder()
                .position(area.pos)
                .pressAction(this::Remove)
                .sprite(IconAndButtonUtil.SPRITE_MINUS)
                .build());
    }

    private SlotMachineEntry getEntry() { return this.tab.getEntry(this.entryIndex.get()); }

    private void Remove(EasyButton button) { this.tab.commonTab.RemoveEntry(this.entryIndex.get()); }

    @Override
    public void renderWidget(@Nonnull EasyGuiGraphics gui) {

        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            //Draw label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ENTRY_LABEL.get(this.entryIndex.get() + 1), 12, 0, 0x404040);
            //Draw Weight label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_WEIGHT_LABEL.get(), 0, 12, 0x404040);
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                    gui.renderItem(entry.items.get(i), 18 * i, ITEM_POSY);
                else
                    gui.renderSlotBackground(EasySlot.BACKGROUND, 18 * i, ITEM_POSY);
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

    @Override
    public void tick()
    {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null && this.tab.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            this.weightEdit.visible = true;
            boolean hasPerms = this.tab.menu.hasPermission(Permissions.EDIT_TRADES);
            this.removeEntryButton.visible = hasPerms;
            this.weightEdit.setEditable(hasPerms);
            if(trader.areEntriesChanged())
            {
                this.weightEdit.setValue(Integer.toString(entry.getWeight()));
                return;
            }

            int thisIndex = this.entryIndex.get();
            if(thisIndex != this.previousIndex)
                this.weightEdit.setValue(String.valueOf(entry.getWeight()));
            this.previousIndex = thisIndex;
        }
        else
            this.weightEdit.visible = this.removeEntryButton.visible = false;
    }

    private void onWeightChanged(int newWeight)
    {
        int thisIndex = this.entryIndex.get();
        this.tab.commonTab.ChangeEntryWeight(thisIndex,newWeight);
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

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
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
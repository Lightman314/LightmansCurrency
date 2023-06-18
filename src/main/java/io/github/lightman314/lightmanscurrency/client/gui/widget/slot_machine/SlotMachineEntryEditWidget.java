package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.easy.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SlotMachineEntryEditWidget extends AbstractWidget implements IEasyTickable {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 46;

    public final SlotMachineEntryClientTab tab;
    public final Supplier<Integer> entryIndex;

    private final EditBox weightEdit;
    private final PlainButton removeEntryButton;

    private int previousIndex = -1;

    private static final int ITEM_POSY = 22;

    public SlotMachineEntryEditWidget(int x, int y, SlotMachineEntryClientTab tab, Supplier<Integer> entryIndex) {
        super(x, y, WIDTH, HEIGHT, EasyText.empty());
        this.tab = tab;
        this.entryIndex = entryIndex;
        this.weightEdit = this.tab.screen.addRenderableTabWidget(new EditBox(this.tab.font, this.getX() + this.tab.font.width(EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.weight_label")), this.getY() + 10, 36, 10, EasyText.empty()));
        this.weightEdit.setMaxLength(4);
        this.removeEntryButton = this.tab.screen.addRenderableTabWidget(IconAndButtonUtil.minusButton(this.getX(), this.getY(), this::Remove));
    }

    private SlotMachineEntry getEntry() { return this.tab.getEntry(this.entryIndex.get()); }

    private void Remove(Button button) { this.tab.commonTab.RemoveEntry(this.entryIndex.get()); }

    @Override
    public void renderWidget(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            //Draw label
            this.tab.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.entry_label", this.entryIndex.get() + 1), this.getX() + 12, this.getY(), 0x404040);
            //Draw Weight label
            this.tab.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.weight_label"), this.getX(), this.getY() + 12, 0x404040);
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size())
                    ItemRenderUtil.drawItemStack(pose, this.tab.font, entry.items.get(i), this.getX() + (18 * i), this.getY() + 22);
                else
                    ItemRenderUtil.drawSlotBackground(pose, this.getX() + (18 * i), this.getY() + ITEM_POSY, EasySlot.BACKGROUND);
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
                ItemStack heldItem = this.tab.menu.getCarried();
                if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
                {
                    int itemIndex = getItemSlotIndex(mouseX);
                    if(itemIndex >= 0)
                    {
                        if(itemIndex >= entry.items.size())
                        {
                            if(!heldItem.isEmpty())
                            {
                                if(rightClick) //If right-click, set as 1
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem.copyWithCount(1));
                                else //Otherwise add whole stack
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem);
                                return true;
                            }
                        }
                        else if(heldItem.isEmpty())
                        {
                            if(rightClick) //If right-click, reduce by 1
                            {
                                ItemStack newStack = entry.items.get(itemIndex).copy();
                                newStack.shrink(1);
                                if(newStack.isEmpty())
                                    this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                                else
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, newStack);
                            }
                            else //If left-click, remove entirely
                                this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                            return true;
                        }
                        else {
                            if(rightClick) //If right-click, either set as 1 or increase by 1
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
                this.weightEdit.setValue(Integer.toString(entry.getWeight()));
            int newWeight = TextInputUtil.getIntegerValue(this.weightEdit, 1);
            if(newWeight != entry.getWeight())
                this.tab.commonTab.ChangeEntryWeight(thisIndex, newWeight);
            this.previousIndex = thisIndex;
        }
        else
            this.weightEdit.visible = this.removeEntryButton.visible = false;

        TextInputUtil.whitelistInteger(this.weightEdit, 1, 1000);
    }

    @Override
    protected void updateWidgetNarration(@Nonnull NarrationElementOutput narrator) { }

    @Override
    public void playDownSound(@Nonnull SoundManager soundManager) { }

}

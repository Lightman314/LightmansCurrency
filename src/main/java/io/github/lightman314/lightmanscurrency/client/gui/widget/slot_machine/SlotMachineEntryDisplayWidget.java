package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryDisplayWidget extends EasyWidget implements ITooltipSource {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 46;

    public final Supplier<SlotMachineTraderData> trader;
    public final Supplier<Integer> index;

    private static final int ITEM_POSY = 22;

    public SlotMachineEntryDisplayWidget(ScreenPosition pos, Supplier<SlotMachineTraderData> trader, Supplier<Integer> index) { this(pos.x, pos.y, trader, index); }
    public SlotMachineEntryDisplayWidget(int x, int y, Supplier<SlotMachineTraderData> trader, Supplier<Integer> index) {
        super(x, y, WIDTH, HEIGHT);
        this.trader = trader;
        this.index = index;
    }

    @Override
    public SlotMachineEntryDisplayWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

    @Nullable
    private SlotMachineEntry getEntry() {
        SlotMachineTraderData trader = this.trader.get();
        if(trader != null)
        {
            int index = this.index.get();
            List<SlotMachineEntry> entries = trader.getValidEntries();
            if(index >= 0 && index < entries.size())
                return entries.get(index);
        }
        return null;
    }

    @Override
    public void renderWidget(@Nonnull EasyGuiGraphics gui) {

        SlotMachineEntry entry = this.getEntry();
        SlotMachineTraderData trader = this.trader.get();
        if(trader != null && entry != null)
        {
            //Draw label
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.entry_label", this.index.get() + 1), 0, 0, 0x404040);
            //Draw Weight label
            gui.drawString(EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.odds_label", trader.getOdds(entry.getWeight())), 0, 12, 0x404040);
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                    gui.renderItem(entry.items.get(i), 18 * i, ITEM_POSY);
            }
        }

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
    public List<Component> getTooltipText(int mouseX, int mouseY) {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
            {
                int itemIndex = this.getItemSlotIndex(mouseX);
                if(itemIndex >= 0 && itemIndex < entry.items.size())
                {
                    if(entry.isMoney())
                        return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.money", entry.getMoneyValue().getComponent("0")));
                    else
                    {
                        ItemStack item = entry.items.get(itemIndex);
                        if(!item.isEmpty())
                            return EasyScreenHelper.getTooltipFromItem(item);
                    }
                }
            }
        }
        return null;
    }

}

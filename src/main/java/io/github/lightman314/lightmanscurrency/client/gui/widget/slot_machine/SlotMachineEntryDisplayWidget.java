package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.client.gui.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryDisplayWidget extends EasyWidget implements ITooltipSource {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 46;

    public final Supplier<TraderData> trader;
    public final Supplier<Integer> index;

    private static final int ITEM_POSY = 22;

    private SlotMachineEntryDisplayWidget(Builder builder)
    {
        super(builder);
        this.trader = builder.trader;
        this.index = builder.index;
    }

    @Nullable
    private SlotMachineEntry getEntry() {
        TraderData trader = this.trader.get();
        if(trader != null)
        {
            SlotMachineNode node = trader.getNode(SlotMachineNode.TYPE);
            if(node != null)
            {
                int index = this.index.get();
                List<SlotMachineEntry> entries = node.getValidEntries();
                if(index >= 0 && index < entries.size())
                    return entries.get(index);
            }
        }
        return null;
    }

    @Override
    public void renderWidget(EasyGuiGraphics gui) {

        SlotMachineEntry entry = this.getEntry();
        TraderData trader = this.trader.get();
        if(trader != null && entry != null)
        {
            //Draw label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ENTRY_LABEL.get(this.index.get() + 1), 0, 0, 0x404040);
            //Draw Weight label
            gui.drawString(LCText.GUI_TRADER_SLOT_MACHINE_ODDS_LABEL.get(entry.getOddsString()), 0, 12, 0x404040);
            int xOffset = 0;
            if(entry.hasCustomIcons())
            {
                //Render Icons and Arrow
                xOffset = 80;
                List<IconData> customIcons = entry.getCustomIcons();
                for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
                {
                    if(i < customIcons.size())
                        IconRenderer.renderIcon(customIcons.get(i),gui,18 * i, ITEM_POSY);
                    else
                        IconRenderer.renderIcon(SlotMachineEntry.DEFAULT_ICON,gui,18 * i, ITEM_POSY);
                }
                SpriteUtil.SMALL_ARROW_RIGHT.render(gui,72,ITEM_POSY + 4);
            }
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                    gui.renderItem(entry.items.get(i), xOffset + (18 * i), ITEM_POSY);
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
        //Do nothing if not visible
        if(!this.isVisible())
            return null;
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
            {
                int itemIndex = this.getItemSlotIndex(mouseX);
                if(itemIndex >= 0 && itemIndex < entry.items.size())
                {
                    if(entry.isMoney())
                        return ImmutableList.of(LCText.TOOLTIP_SLOT_MACHINE_MONEY.get(entry.getMoneyValue().getText()));
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

    public static Builder builder() { return new Builder(); }

    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(WIDTH,HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        private Supplier<TraderData> trader = () -> null;
        private Supplier<Integer> index = () -> 0;

        public Builder trader(Supplier<TraderData> trader) { this.trader = trader; return this; }
        public Builder index(Supplier<Integer> index) { this.index = index; return this; }

        public SlotMachineEntryDisplayWidget build() { return new SlotMachineEntryDisplayWidget(this); }

    }

}

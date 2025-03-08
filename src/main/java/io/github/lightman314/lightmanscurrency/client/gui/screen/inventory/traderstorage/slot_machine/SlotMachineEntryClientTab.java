package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryEditWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryClientTab extends TraderStorageClientTab<SlotMachineEntryTab> implements IScrollable {

    public static final int ENTRY_ROWS = 3;
    public static final int ENTRY_COLUMNS = 2;
    public static final int ENTRIES_PER_PAGE = ENTRY_ROWS * ENTRY_COLUMNS;

    private int scroll = 0;
    private EasyButton buttonAddEntry;

    public SlotMachineEntryClientTab(Object screen, SlotMachineEntryTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconUtil.ICON_TRADER_ALT; }

    @Override
    public MutableComponent getTooltip() { return LCText.TOOLTIP_TRADER_SLOT_MACHINE_EDIT_ENTRIES.get(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen)
    {

        this.addChild(ScrollListener.builder()
                .position(screenArea.pos)
                .size(screenArea.width,145)
                .listener(this)
                .build());

        for(int y = 0; y < ENTRY_ROWS; ++y)
        {
            for(int x = 0; x < ENTRY_COLUMNS; x++)
            {
                this.addChild(SlotMachineEntryEditWidget.builder()
                        .position(screenArea.pos.offset(19 + (x * SlotMachineEntryEditWidget.WIDTH), 10 + (y * SlotMachineEntryEditWidget.HEIGHT)))
                        .tab(this)
                        .index(this.supplierForIndex((y * 2) + x))
                        .build());
            }
        }

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(19 + (SlotMachineEntryEditWidget.WIDTH * 2),10))
                .height(SlotMachineEntryEditWidget.HEIGHT * ENTRY_ROWS)
                .scrollable(this)
                .build());

        this.buttonAddEntry = this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 14,4))
                .pressAction(this::AddEntry)
                .sprite(IconAndButtonUtil.SPRITE_PLUS)
                .build());

        this.tick();

    }

    @Nullable
    public SlotMachineEntry getEntry(int entryIndex)
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            List<SlotMachineEntry> entries = trader.getAllEntries();
            if(entryIndex < 0 || entryIndex >= entries.size())
                return null;
            return entries.get(entryIndex);
        }
        return null;
    }

    private Supplier<Integer> supplierForIndex(int index) { return () -> (this.scroll * ENTRY_COLUMNS) + index; }

    @Override
    public void tick() {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            trader.clearEntriesChangedCache();
        this.validateScroll();
        this.buttonAddEntry.visible = this.menu.hasPermission(Permissions.EDIT_TRADES);
    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

    private void AddEntry(EasyButton button) { this.commonTab.AddEntry(); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = MathUtil.clamp(newScroll, 0, this.getMaxScroll()); }

    private int getEntryCount()
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            return trader.getAllEntries().size();
        return 0;
    }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ENTRIES_PER_PAGE, ENTRY_COLUMNS, this.getEntryCount()); }

}

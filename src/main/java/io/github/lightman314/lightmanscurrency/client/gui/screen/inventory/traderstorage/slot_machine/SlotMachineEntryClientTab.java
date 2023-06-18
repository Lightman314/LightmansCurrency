package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.easy.interfaces.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryEditWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.slot_machine.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryClientTab extends TraderStorageClientTab<SlotMachineEntryTab> implements ScrollBarWidget.IScrollable, IScrollListener {

    public final int ENTRY_ROWS = 3;
    public final int ENTRIES_PER_PAGE = ENTRY_ROWS * 2;


    private int scroll = 0;
    private ScrollBarWidget scrollBar;
    private final List<SlotMachineEntryEditWidget> entryEditWidgets = new ArrayList<>();
    private Button buttonAddEntry;

    public SlotMachineEntryClientTab(TraderStorageScreen screen, SlotMachineEntryTab commonTab) { super(screen, commonTab); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TRADER_ALT; }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.slot_machine.edit_entries"); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public void onOpen()
    {

        this.entryEditWidgets.clear();

        for(int y = 0; y < ENTRY_ROWS; ++y)
        {
            for(int x = 0; x < 2; x++)
            {
                this.entryEditWidgets.add(this.screen.addRenderableTabWidget(new SlotMachineEntryEditWidget(this.screen.getGuiLeft() + 19 + (x * SlotMachineEntryEditWidget.WIDTH), this.screen.getGuiTop() + 10 + (y * SlotMachineEntryEditWidget.HEIGHT), this, this.supplierForIndex((y * 2) + x))));
            }
        }

        this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + 19 + (SlotMachineEntryEditWidget.WIDTH * 2), this.screen.getGuiTop() + 10, SlotMachineEntryEditWidget.HEIGHT * ENTRY_ROWS, this));

        this.buttonAddEntry = this.screen.addRenderableTabWidget(IconAndButtonUtil.plusButton(this.screen.getGuiLeft() + this.screen.getXSize() - 14, this.screen.getGuiTop() + 4, this::AddEntry));

        this.screen.addTabListener(new ScrollListener(ScreenPosition.getScreenCorner(this.screen), this.screen.getXSize(), 145, this));

        this.tick();

        this.menu.SetCoinSlotsActive(false);

    }

    @Override
    public void onClose() { this.menu.SetCoinSlotsActive(true); }

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

    private Supplier<Integer> supplierForIndex(int index) { return () -> this.scroll + index; }

    @Override
    public void tick() {
        for(SlotMachineEntryEditWidget editWidget : this.entryEditWidgets)
            editWidget.tick();
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            trader.clearEntriesChangedCache();
        this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll());
        this.buttonAddEntry.visible = this.menu.hasPermission(Permissions.EDIT_TRADES);

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        this.scrollBar.beforeWidgetRender(mouseY);

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    private void AddEntry(Button button) { this.commonTab.AddEntry(); }

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
    public int getMaxScroll() { return ScrollBarWidget.IScrollable.calculateMaxScroll(this.ENTRIES_PER_PAGE, this.getEntryCount()); }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) { return this.handleScrollWheel(delta); }

}

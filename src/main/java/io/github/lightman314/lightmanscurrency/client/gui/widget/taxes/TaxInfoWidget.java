package io.github.lightman314.lightmanscurrency.client.gui.widget.taxes;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TaxInfoWidget extends EasyWidgetWithChildren {

    public static final Sprite SPRITE_IGNORE_TC = Sprite.SimpleSprite(IconAndButtonUtil.WIDGET_TEXTURE, 10, 0, 10, 10);
    public static final Sprite SPRITE_STOP_IGNORING_TC = Sprite.SimpleSprite(IconAndButtonUtil.WIDGET_TEXTURE, 0, 0, 10, 10);

    public static final int HEIGHT = 30;
    public static final int WIDTH = 176;

    private final Supplier<ITaxCollector> entrySource;
    private final ITaxInfoInteractable parent;

    public TaxInfoWidget(@Nonnull ScreenPosition pos, @Nonnull Supplier<ITaxCollector> entry, @Nonnull ITaxInfoInteractable parent) { this(pos.x, pos.y, entry, parent); }
    public TaxInfoWidget(int x, int y, @Nonnull Supplier<ITaxCollector> entry, @Nonnull ITaxInfoInteractable parent) {
        super(x, y, WIDTH, HEIGHT);
        this.entrySource = entry;
        this.parent = parent;
    }

    @Override
    public TaxInfoWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {

        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
        {
            //Get status color
            int statusColor = 0x404040;
            if(trader.ShouldIgnoreTaxEntryOnly(entry))
                statusColor = 0xFF0000;
            else if(trader.ShouldIgnoreAllTaxes())
                statusColor = 0xFF7F00;
            else if(entry.ShouldTax(trader))
                statusColor = 0x00FF00;
            gui.drawString(entry.getName(), 0, 0, statusColor);
            gui.drawString(LCText.GUI_TAX_COLLECTOR_TAX_RATE.get(entry.getTaxRate()), this.parent.canPlayerForceIgnore() ? 12 : 0, 15, 0x404040);
        }

    }

    @Override
    public void addChildren() {
        this.addChild(EasyTextButton.builder()
                .position(this.getPosition().offset(80,10))
                .width(60)
                .text(LCText.GUI_TAX_COLLECTOR_TAXABLE_ACCEPT_COLLECTOR)
                .pressAction(this::AcceptTaxCollector)
                .addon(EasyAddonHelper.visibleCheck(this::shouldAcceptBeVisible))
                .build());
        this.addChild(new PlainButton(this.getPosition().offset(0, 13), this::ToggleIgnoreState, this::getForceIgnoreSprite)
                .withAddons(EasyAddonHelper.visibleCheck(() -> this.parent.canPlayerForceIgnore() && this.entrySource.get() != null),
                        EasyAddonHelper.tooltip(this::getForceIgnoreTooltip)));

    }

    private boolean shouldAcceptBeVisible() {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return entry.IsInArea(trader) && !entry.ShouldTax(trader);
        return false;
    }

    private boolean isCurrentlyIgnored() {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
            return trader.ShouldIgnoreTaxEntryOnly(entry);
        return false;
    }

    private Sprite getForceIgnoreSprite() { return this.isCurrentlyIgnored() ? SPRITE_STOP_IGNORING_TC : SPRITE_IGNORE_TC; }

    private Component getForceIgnoreTooltip() { return this.isCurrentlyIgnored() ? LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_PARDON_IGNORED.get() : LCText.TOOLTIP_TAX_COLLECTOR_TAXABLE_FORCE_IGNORE.get(); }

    private void AcceptTaxCollector(EasyButton button)
    {
        ITaxCollector entry = this.entrySource.get();
        if(entry != null)
            this.parent.AcceptTaxCollector(entry.getID());
    }

    private void ToggleIgnoreState(EasyButton button)
    {
        ITaxCollector entry = this.entrySource.get();
        TraderData trader = this.parent.getTrader();
        if(entry != null && trader != null)
        {
            if(trader.ShouldIgnoreTaxEntryOnly(entry))
                this.parent.PardonIgnoredTaxCollector(entry.getID());
            else
                this.parent.ForceIgnoreTaxCollector(entry.getID());
        }
    }

}

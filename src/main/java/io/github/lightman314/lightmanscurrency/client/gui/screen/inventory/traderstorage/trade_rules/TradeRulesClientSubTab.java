package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.trade_rules.TradeRulesTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.client.gui.Font;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TradeRulesClientSubTab implements TabButton.ITab {

    public final TradeRulesClientTab<?> parent;
    public final TradeRulesTab commonTab;
    public final TraderStorageScreen screen;
    public final TraderStorageMenu menu;
    public final Font font;

    @Nonnull
    public List<TradeRule> getTradeRules()
    {
        ITradeRuleHost host = this.commonTab.getHost();
        if(host != null)
            return host.getRules();
        return new ArrayList<>();
    }

    @Nonnull
    public List<TradeRule> getFilteredRules() { return this.filterRules(this.getTradeRules()); }

    /**
     * Hides trade rules that cannot be activated in the trader/trades current state.
     */
    protected final  List<TradeRule> filterRules(@Nonnull List<TradeRule> rules) { return rules.stream().filter(TradeRule::canActivate).collect(Collectors.toList()); }

    protected TradeRulesClientSubTab(@Nonnull TradeRulesClientTab<?> parent)
    {
        this.parent = parent;
        this.commonTab = this.parent.commonTab;
        this.screen = this.parent.screen;
        this.menu = this.parent.menu;
        this.font = this.parent.font;
    }

    @Override
    public int getColor() { return 0xFFFFFF; }

    public final <T> T addWidget(T widget) { return this.parent.addWidget(widget); }
    public final void removeWidget(Object widget) { this.parent.removeWidget(widget); }

    public abstract boolean isVisible();


    public abstract void onOpen();
    public abstract void onClose();

    public void tick() {}

    public abstract void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks);
    public abstract void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY);

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }

}

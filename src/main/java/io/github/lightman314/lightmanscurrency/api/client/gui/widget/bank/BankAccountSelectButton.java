package io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.FancyButton;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.client.IconRenderer;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BankAccountSelectButton extends FancyButton {

    public static final int HEIGHT = 20;

    private final Supplier<BankReference> account;
    private final Predicate<BankReference> activeFilter;
    private final Predicate<BankReference> highlight;

    @Nullable
    private BankReference accountCache = null;

    protected BankAccountSelectButton(Builder builder) {
        super(builder);
        this.account = builder.account;
        this.activeFilter = builder.active;
        this.highlight = builder.highlight;
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        this.accountCache = this.account.get();
        if(this.accountCache == null)
            this.hideThisFrame();
        else
            this.active = this.activeFilter.test(this.accountCache);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui,ScreenArea area) {
        if(this.accountCache == null)
            return;

        //Render Background
        WidgetSprites bgSprite;
        if(this.highlight.test(this.accountCache))
            bgSprite = LCSprites.BUTTON_GREEN;
        else
            bgSprite = LCSprites.BUTTON_BROWN;
        gui.blitSprite(bgSprite.get(this.active,this.isHovered()),area,this.getSpriteColor());

        //Render Owner
        IconData icon = this.accountCache.getIcon();
        if(icon != null)
            IconRenderer.extractState(gui,icon,2,2);
        //Render the name
        int textColor = this.active ? 0xFFFFFFFF : 0xFF202020;
        gui.textWithScrollingOverflow(this.accountName(),22,5,area.width - 24,textColor,true);

    }

    private Component accountName() {
        BankAccount account = this.accountCache == null ? null : this.accountCache.get();
        return account == null ? Component.empty() : account.getName();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends ButtonBuilder<Builder,BankAccountSelectButton> {

        private Builder() { super(100,HEIGHT); }

        private Supplier<BankReference> account = () -> null;
        private Predicate<BankReference> active = br -> true;
        private Predicate<BankReference> highlight = br -> false;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder forAccount(Supplier<BankReference> account) { this.account = account; return this; }
        public Builder active(Predicate<BankReference> active) { this.active = active; return this; }
        public Builder highlighted(Predicate<BankReference> highlight) { this.highlight = highlight; return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public BankAccountSelectButton build() { return new BankAccountSelectButton(this); }

    }

}

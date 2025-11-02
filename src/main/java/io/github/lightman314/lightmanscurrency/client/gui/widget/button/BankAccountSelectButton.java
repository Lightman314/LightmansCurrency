package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BankAccountSelectButton extends EasyButton implements ITooltipWidget {

    public static final int HEIGHT = 20;

    private final Predicate<BankReference> activeCheck;
    private final Predicate<BankReference> hightlightCheck;
    private final Supplier<BankReference> account;
    private final Supplier<Boolean> parentVisible;

    private BankAccountSelectButton(Builder builder)
    {
        super(builder);
        this.activeCheck = builder.active;
        this.hightlightCheck = builder.hightlight;
        this.account = builder.account;
        this.parentVisible = builder.visible;
    }


    private Component accountName(){
        BankReference br = this.account.get();
        IBankAccount account = br != null ? br.get() : null;
        if(account == null)
            return EasyText.empty();
        return account.getName();
    }

    @Override
    public boolean renderTooltip(EasyGuiGraphics gui) {
        if(gui.font.width(this.accountName()) > (this.width - 22))
            return ITooltipWidget.super.renderTooltip(gui);
        return false;
    }

    @Override
    public List<Component> getTooltipText() { return Lists.newArrayList(this.accountName()); }

    @Override
    protected void renderTick() {
        BankReference reference = this.account.get();
        this.setVisible(this.parentVisible.get() && reference != null && reference.get() != null);
        if(this.visible)
            this.setActive(this.activeCheck.test(reference));
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        BankReference reference = this.account.get();
        if(reference == null)
        {
            this.setVisible(false);
            return;
        }
        //Set to gray if not active
        float color = this.isActive() ? 1f : 0.5f;
        gui.setColor(color,color,color);
        FixedSizeSprite sprite;
        if(this.hightlightCheck.test(reference))
            sprite = SpriteUtil.createButtonGreen(this.width,this.height);
        else
            sprite = SpriteUtil.createButtonBrown(this.width,this.height);
        //Render Background
        sprite.render(gui,0,0,this);

        //Render owner
        IconData icon = reference.getIcon();
        if(icon != null)
            icon.render(gui, 2, 2);
        //Render the name
        //Component name = TextRenderUtil.fitString(this.accountName(), this.width - 22);
        int textColor = this.isActive() ? 0xFFFFFFFF : 0xFF404040 / 2;
        gui.drawScrollingString(this.accountName(), ScreenArea.of(22,0,this.width - 24,this.height), textColor);
        //gui.drawShadowed(name, 22, 6, textColor);
        //Reset color
        gui.resetColor();
    }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyButtonBuilder<Builder>
    {
        private Builder() { super(100,HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        Predicate<BankReference> active = r -> true;
        Predicate<BankReference> hightlight = r -> false;
        Supplier<BankReference> account = () -> null;
        Supplier<Boolean> visible = () -> true;

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder active(Predicate<BankReference> active) { this.active = active; return this; }
        public Builder highlight(Predicate<BankReference> hightlight) { this.hightlight = hightlight; return this; }
        public Builder account(Supplier<BankReference> account) { this.account = account; return this; }
        public Builder visible(Supplier<Boolean> visible) { this.visible = visible; return this; }

        public BankAccountSelectButton build() { return new BankAccountSelectButton(this); }

    }

}
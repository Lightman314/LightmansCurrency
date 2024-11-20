package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class BankAccountSelectButton extends EasyButton implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/buttons.png");

    public static final int HEIGHT = 20;

    private final Supplier<BankReference> selectedAccount;
    private final Supplier<BankReference> account;
    private final Supplier<Boolean> parentVisible;

    private BankAccountSelectButton(@Nonnull Builder builder)
    {
        super(builder);
        this.selectedAccount = builder.selectedAccount;
        this.account = builder.account;
        this.parentVisible = builder.visible;
    }

    @Nonnull
    private Component accountName(){
        BankReference br = this.account.get();
        IBankAccount account = br != null ? br.get() : null;
        if(account == null)
            return EasyText.empty();
        return account.getName();
    }

    @Override
    public void renderTooltip(EasyGuiGraphics gui) {
        if(gui.font.width(this.accountName()) > (this.width - 22))
            ITooltipWidget.super.renderTooltip(gui);
    }

    @Override
    public List<Component> getTooltipText() { return Lists.newArrayList(this.accountName()); }

    @Override
    protected void renderTick() {
        BankReference reference = this.account.get();
        this.setVisible(this.parentVisible.get() && reference != null && reference.get() != null);
        if(this.visible)
            this.setActive(!reference.equals(this.selectedAccount.get()));
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        BankReference reference = this.account.get();
        if(reference == null)
        {
            this.setVisible(false);
            return;
        }
        //Set to gray if not active
        float color = this.isActive() ? 1f : 0.5f;
        gui.setColor(color,color,color);
        //Render Background
        gui.blitBackgroundOfSize(GUI_TEXTURE,0,0, this.width, this.height,0,0,256,20,2);

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

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyButtonBuilder<Builder>
    {
        private Builder() { super(100,HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        Supplier<BankReference> selectedAccount = () -> null;
        Supplier<BankReference> account = () -> null;
        Supplier<Boolean> visible = () -> true;

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder currentlySelected(Supplier<BankReference> selectedAccount) { this.selectedAccount = selectedAccount; return this; }
        public Builder account(Supplier<BankReference> account) { this.account = account; return this; }
        public Builder visible(Supplier<Boolean> visible) { this.visible = visible; return this; }

        public BankAccountSelectButton build() { return new BankAccountSelectButton(this); }

    }

}
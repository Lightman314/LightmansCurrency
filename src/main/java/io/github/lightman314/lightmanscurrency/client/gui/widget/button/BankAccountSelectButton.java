package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class BankAccountSelectButton extends EasyButton implements ITooltipWidget {

    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"textures/gui/buttons.png");

    public static final int HEIGHT = 20;

    private final Supplier<BankReference> selectedAccount;
    private final Supplier<BankReference> account;
    private final Supplier<Boolean> parentVisible;

    public BankAccountSelectButton(@Nonnull ScreenPosition position, int width, @Nonnull Runnable press, @Nonnull Supplier<BankReference> currentAccount, @Nonnull Supplier<BankReference> account, @Nonnull Supplier<Boolean> parentVisible) {
        super(position, width, HEIGHT, press);
        this.selectedAccount = currentAccount;
        this.account = account;
        this.parentVisible = parentVisible;
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
    public BankAccountSelectButton withAddons(WidgetAddon... addons) {
        this.withAddonsInternal(addons);
        return this;
    }

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
        Component name = TextRenderUtil.fitString(this.accountName(), this.width - 22);
        int textColor = this.isActive() ? 0xFFFFFF : 0x7F7F7F / 2;
        gui.drawShadowed(name, 22, 6, textColor);
        //Reset color
        gui.resetColor();
    }

}

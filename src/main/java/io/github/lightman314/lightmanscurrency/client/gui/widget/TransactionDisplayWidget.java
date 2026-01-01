package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipWidget;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TransactionRegisterScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionData;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionHelpfulness;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TransactionDisplayWidget extends EasyButton implements ITooltipWidget {

    public static final int INDEX_POS = 2;
    public static final int COMMENT_POS = 20;
    public static final int ARGUMENT_POS = 103;
    public static final int BALANCE_POS = 140;

    private final int offset;
    private final Supplier<Integer> index;
    private final Supplier<FlexibleMoneyValue> previousBalance;
    private final Supplier<TransactionData> transaction;
    protected TransactionDisplayWidget(Builder builder) {
        super(builder);
        this.offset = builder.offset;
        this.index = builder.index;
        this.previousBalance = builder.previousBalance;
        this.transaction = builder.transaction;
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {

        //Render the frame
        gui.blit(TransactionRegisterScreen.GUI_TEXTURE,0,0,0,TransactionRegisterScreen.HEIGHT + 1 - this.offset,this.width,this.height);
        TransactionData data = this.transaction.get();
        if(data == null)
            return;
        int textY = 1 + this.offset;
        //Render Index
        TextRenderUtil.drawScrollingString(gui,String.valueOf(this.index.get()),INDEX_POS,textY,15,0);
        //Render Comment/Description
        TextRenderUtil.drawScrollingString(gui,data.comment,COMMENT_POS,textY,80,0);
        //Render Argument
        TextRenderUtil.drawScrollingString(gui,getArgumentText(data),ARGUMENT_POS,textY,34,data.type.getTextColor(this.previousBalance.get(),data.argument));
        //Render Balance
        TextRenderUtil.drawScrollingString(gui,data.resultValue.getText(0,0xC00000),BALANCE_POS,textY,34,0);
    }

    private static Component getArgumentText(TransactionData data)
    {
        AtomicReference<Component> result = new AtomicReference<>(EasyText.literal("ERROR"));
        data.argument.ifLeft(val -> result.set(val.getText("0")))
                .ifRight(mult -> result.set(LCText.GUI_TRANSACTION_REGISTER_MULT_FORMAT.get(mult)));
        return result.get();
    }

    public static Builder builder(int offset) { return new Builder(offset); }

    @Override
    public List<Component> getTooltipText(int mouseX, int mouseY) {
        if(this.isMouseOver(mouseX,mouseY))
        {
            List<Component> tooltip = new ArrayList<>();
            TransactionData transaction = this.transaction.get();
            if(transaction != null) {
                FlexibleMoneyValue previousBalance = this.previousBalance.get();
                int localX = mouseX - this.getX() + 2;
                if(localX < ARGUMENT_POS)
                {
                    String index = String.valueOf(this.index.get());
                    if(this.getFont().width(index) > COMMENT_POS - INDEX_POS)
                        tooltip.add(EasyText.literal(index));
                }
                if(localX >= COMMENT_POS && localX < ARGUMENT_POS)
                {
                    if(this.getFont().width(transaction.comment) > ARGUMENT_POS - COMMENT_POS)
                        tooltip.add(EasyText.literal(transaction.comment));
                }
                if(localX >= ARGUMENT_POS && localX < BALANCE_POS)
                {
                    TextEntry text = LCText.TOOLTIP_TRANSACTION_REGISTER_TRANSACTION.get(transaction.type);
                    TransactionHelpfulness helpfulness = transaction.type.getHelpfulness(previousBalance,transaction.argument);
                    AtomicReference<Component> arg = new AtomicReference<>(EasyText.empty());
                    transaction.argument
                            .ifLeft(val -> arg.set(val.getText()))
                            .ifRight(mult -> arg.set(EasyText.literal(String.valueOf(mult))));
                    tooltip.add(text.get(arg.get()).withStyle(helpfulness.style));
                }
                else if(localX >= BALANCE_POS)
                    tooltip.add(LCText.TOOLTIP_TRANSACTION_REGISTER_BALANCE.get(transaction.resultValue.getText(-1,ChatFormatting.RED.getColor())));
                tooltip.add(LCText.TOOLTIP_TRANSACTION_REGISTER_EDIT.get());
            }
            else
                tooltip.add(LCText.TOOLTIP_TRANSACTION_REGISTER_CREATE.get());
            return tooltip;
        }
        return null;
    }

    @Override
    public List<Component> getTooltipText() { return List.of(); }

    public static class Builder extends EasyButtonBuilder<Builder>
    {

        private Builder(int offset) { super(176,11 + offset); this.offset = offset; }

        private final int offset;
        private Supplier<Integer> index = () -> 0;
        private Supplier<FlexibleMoneyValue> previousBalance = () -> FlexibleMoneyValue.EMPTY;
        private Supplier<TransactionData> transaction = () -> null;
        @Override
        protected Builder getSelf() { return this; }

        public Builder ofIndex(Supplier<Integer> index) { this.index = index; return this; }
        public Builder withPreviousBalance(Supplier<FlexibleMoneyValue> previousBalance) { this.previousBalance = previousBalance; return this; }
        public Builder withTransaction(Supplier<TransactionData> transaction) { this.transaction = transaction; return this; }

        public TransactionDisplayWidget build() { return new TransactionDisplayWidget(this); }

    }

}
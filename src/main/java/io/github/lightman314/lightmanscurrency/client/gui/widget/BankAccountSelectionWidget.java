package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.BankAccountSelectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BankAccountSelectionWidget extends EasyWidgetWithChildren implements IScrollable, Comparator<BankReference> {

    private final int rows;
    private final Predicate<BankReference> filter;
    private final Supplier<BankReference> selectedAccount;
    private final Consumer<BankReference> consumer;

    private int scroll = 0;

    private BankAccountSelectionWidget(@Nonnull Builder builder)
    {
        super(builder);
        this.rows = builder.rows;
        this.filter = builder.filter;
        this.selectedAccount = builder.selectedAccount;
        this.consumer = builder.handler;
    }

    @Override
    public void addChildren(@Nonnull ScreenArea area) {
        //Scroll Bar
        this.addChild(ScrollBarWidget.builder()
                .onRight(this)
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
                .build());
        this.addChild(ScrollListener.builder()
                .area(area)
                .listener(this)
                .build());
        for(int i = 0; i < this.rows; ++i)
        {
            final int index = i;
            this.addChild(BankAccountSelectButton.builder()
                    .position(area.pos.offset(0,i * BankAccountSelectButton.HEIGHT))
                    .width(this.width)
                    .pressAction(() -> this.selectAccount(index))
                    .currentlySelected(this.selectedAccount)
                    .account(() -> this.getAccount(index))
                    .visible(this::isVisible)
                    .build());
        }
    }

    private List<BankReference> getBankAccounts()
    {
        List<BankReference> list = new ArrayList<>(BankAPI.API.GetAllBankReferences(true).stream().filter(this.filter).toList());
        list.sort(this);
        return list;
    }

    @Nullable
    private BankReference getAccount(int index)
    {
        List<BankReference> accounts = this.getBankAccounts();
        int i = index + this.scroll;
        if(i >= 0 && i < accounts.size())
            return accounts.get(i);
        return null;
    }

    private void selectAccount(int index)
    {
        BankReference account = this.getAccount(index);
        if(account != null)
            this.consumer.accept(account);
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {
        gui.fill(this.getArea().atPosition(ScreenPosition.ZERO),0xFF000000);
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.rows,this.getBankAccounts().size()); }

    //Filter so that the currently selected bank account is at the top of the list
    @Override
    public int compare(BankReference rA, BankReference rB) {
        boolean matchA = this.isCurrentAccount(rA);
        boolean matchB = this.isCurrentAccount(rB);
        //Put current selection at the top of the list
        if(matchA && !matchB)
            return -1;
        if(matchB && !matchA)
            return 1;
        //Put null entries at the bottom of the list
        if(rA != null && rB == null)
            return -1;
        if(rB != null && rA == null)
            return 1;
        if(rA == null && rB == null)
            return 0;
        //Otherwise sort by the accounts priority (inverted so that high priority is first)
        return Integer.compare(rB.sortPriority(),rA.sortPriority());
    }

    private boolean isCurrentAccount(@Nullable BankReference reference)
    {
        return reference != null && reference.equals(this.selectedAccount.get());
    }

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(100,BankAccountSelectButton.HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        int rows = 1;
        Predicate<BankReference> filter = r -> true;
        Supplier<BankReference> selectedAccount = () -> null;
        Consumer<BankReference> handler = r -> {};

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder rows(int rows) { this.rows = rows; this.changeHeight(rows * BankAccountSelectButton.HEIGHT); return this; }
        public Builder filter(Predicate<BankReference> filter) { this.filter = filter; return this; }
        public Builder selected(Supplier<BankReference> selectedAccount) { this.selectedAccount = selectedAccount; return this; }
        public Builder handler(Consumer<BankReference> handler) { this.handler = handler; return this; }

        public BankAccountSelectionWidget build() { return new BankAccountSelectionWidget(this); }

    }

}

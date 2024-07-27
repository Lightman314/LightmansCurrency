package io.github.lightman314.lightmanscurrency.client.gui.widget;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.BankAccountSelectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

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

    public BankAccountSelectionWidget(int x, int y, int width, int rows,@Nonnull Predicate<BankReference> filter, @Nonnull Supplier<BankReference> currentlySelected, @Nonnull Consumer<BankReference> consumer) { this(ScreenPosition.of(x,y),width,rows,filter,currentlySelected,consumer); }
    public BankAccountSelectionWidget(@Nonnull ScreenPosition position, int width, int rows, @Nonnull Predicate<BankReference> filter, @Nonnull Supplier<BankReference> selectedAccount, @Nonnull Consumer<BankReference> consumer) {
        super(position, width, rows * BankAccountSelectButton.HEIGHT);
        this.rows = rows;
        this.filter = filter;
        this.selectedAccount = selectedAccount;
        this.consumer = consumer;
    }

    @Override
    public void addChildren() {
        //Scroll Bar
        this.addChild(new ScrollBarWidget(this.getPosition().offset(this.getWidth(),0),this.getHeight(),this).withAddons(EasyAddonHelper.visibleCheck(this::isVisible)));
        this.addChild(new ScrollListener(this.getArea(), this));
        for(int i = 0; i < this.rows; ++i)
        {
            final int index = i;
            this.addChild(new BankAccountSelectButton(this.getPosition().offset(0,i * BankAccountSelectButton.HEIGHT), this.width, () -> this.selectAccount(index), this.selectedAccount, () -> this.getAccount(index), this::isVisible));
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
    public BankAccountSelectionWidget withAddons(WidgetAddon... addons) {
        this.withAddonsInternal(addons);
        return this;
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
}

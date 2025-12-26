package io.github.lightman314.lightmanscurrency.client.gui.widget;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.BankAccountSelectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BankAccountSelectionWidget extends EasyWidgetWithChildren implements IScrollable, Comparator<BankReference> {

    private final int rows;
    private final Predicate<BankReference> filter;
    private final Supplier<BankReference> selectedAccount;
    private final Predicate<BankReference> accountActive;
    private final Predicate<BankReference> accountHighlighted;
    private final Consumer<BankReference> consumer;

    private EditBox searchBox;

    private String lastSearch = "";
    private int scroll = 0;

    private List<BankReference> bankAccountCache = new ArrayList<>();

    private BankAccountSelectionWidget(Builder builder)
    {
        super(builder);
        this.rows = builder.rows;
        this.filter = builder.filter;

        this.selectedAccount = builder.selected;
        this.accountActive = builder.active;
        this.accountHighlighted = builder.highlighted;
        this.consumer = builder.handler;
        if(builder.oldWidget != null)
        {
            this.scroll = builder.oldWidget.scroll;
            this.lastSearch = builder.oldWidget.lastSearch;
        }
    }

    @Override
    public void addChildren(ScreenArea area) {
        //Search
        this.searchBox = this.addChild(TextInputUtil.stringBuilder()
                .position(area.pos.offset(area.width - 88,2))
                .startingString(this.lastSearch)
                .handler(s -> this.lastSearch = s)
                .size(79,9)
                .noBorder()
                .build());
        //Scroll Bar
        this.addChild(ScrollBarWidget.builder()
                .position(area.pos.offset(area.width,12))
                .height(area.height - 12)
                .scrollable(this)
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
                    .position(area.pos.offset(0,12 + (i * BankAccountSelectButton.HEIGHT)))
                    .width(this.width)
                    .pressAction(() -> this.selectAccount(index))
                    .active(this.accountActive)
                    .highlight(this.accountHighlighted)
                    .account(() -> this.getAccount(index))
                    .visible(this::isVisible)
                    .build());
        }
    }

    private boolean searchFilter(BankReference reference)
    {
        if(this.lastSearch.isBlank())
            return true;
        IBankAccount account = reference == null ? null : reference.get();
        if(account == null)
            return false;
        return account.getName().getString().toLowerCase().contains(this.lastSearch.toLowerCase());
    }

    @Nullable
    private BankReference getAccount(int index)
    {
        int i = index + this.scroll;
        if(i >= 0 && i < this.bankAccountCache.size())
            return this.bankAccountCache.get(i);
        return null;
    }

    private void selectAccount(int index)
    {
        BankReference account = this.getAccount(index);
        if(account != null)
            this.consumer.accept(account);
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {
        //Render Search Box
        SpriteUtil.SEARCH_FIELD.render(gui,this.width - 90,0,90);
        //Render Black BG
        gui.fill(ScreenArea.of(ScreenPosition.of(0,12),this.width,this.height - 12),0xFF000000);
    }

    @Override
    protected void renderTick() {
        this.searchBox.visible = this.visible;
        if(this.visible)
            this.validateScroll();
        this.updateBankAccountCache();
    }

    private void updateBankAccountCache()
    {
        if(!this.visible)
        {
            this.bankAccountCache = new ArrayList<>();
            return;
        }
        this.bankAccountCache = new ArrayList<>(BankAPI.getApi().GetAllBankReferences(true).stream().filter(this.filter).filter(this::searchFilter).toList());
        this.bankAccountCache.sort(this);
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.rows,this.bankAccountCache.size()); }

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
        //Put "highlighted" entries at the top of the list
        boolean hA = this.accountHighlighted.test(rA);
        boolean hB = this.accountHighlighted.test(rB);
        if(hA && !hB)
            return -1;
        if(hB && !hA)
            return 1;
        //Put null entries at the bottom of the list
        if(rA != null && rB == null)
            return -1;
        if(rB != null && rA == null)
            return 1;
        if(rA == null && rB == null)
            return 0;


        //Otherwise sort by the accounts priority (inverted so that high priority is first)
        int priority = Integer.compare(rB.sortPriority(),rA.sortPriority());
        if(priority == 0)
        {
            //Sort by name
            IBankAccount baA = rA.get();
            IBankAccount baB = rB.get();
            //Put null entries at the bottom of the list
            if(baA != null && baB == null)
                return -1;
            if(baB != null && baA == null)
                return 1;
            if(baA == null && baB == null)
                return 0;
            //Sort by name
            return baA.getName().getString().compareToIgnoreCase(baB.getName().getString());
        }
        else
            return priority;
    }

    private boolean isCurrentAccount(@Nullable BankReference reference)
    {
        return reference != null && reference.equals(this.selectedAccount.get());
    }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(100,BankAccountSelectButton.HEIGHT + 12); }

        @Override
        protected Builder getSelf() { return this; }

        @Nullable
        BankAccountSelectionWidget oldWidget;
        int rows = 1;
        Supplier<BankReference> selected = () -> null;
        Predicate<BankReference> filter = r -> true;
        Predicate<BankReference> active = r -> true;
        Predicate<BankReference> highlighted = r -> false;
        Consumer<BankReference> handler = r -> {};

        public Builder oldWidget(@Nullable BankAccountSelectionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder width(int width) { this.changeWidth(width); return this; }
        public Builder rows(int rows) { this.rows = rows; this.changeHeight((rows * BankAccountSelectButton.HEIGHT) + 12); return this; }
        public Builder filter(Predicate<BankReference> filter) { this.filter = filter; return this; }
        public Builder selected(Supplier<BankReference> selectedAccount) { this.selected = selectedAccount; this.active = r -> !r.equals(selectedAccount.get()); return this; }
        public Builder active(Predicate<BankReference> activeCheck) { this.active = activeCheck; return this; }
        public Builder highlight(Predicate<BankReference> highlightCheck) { this.highlighted = highlightCheck; return this; }
        public Builder handler(Consumer<BankReference> handler) { this.handler = handler; return this; }

        public BankAccountSelectionWidget build() { return new BankAccountSelectionWidget(this); }

    }

}

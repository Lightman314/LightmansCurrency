package io.github.lightman314.lightmanscurrency.api.client.gui.widget.bank;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BankAccountSelectionWidget extends AbstractMultiWidget.LateChildren implements IScrollable,Comparator<BankReference> {

    private final int rows;
    private final Predicate<BankReference> filter;
    private final Supplier<BankReference> selected;
    private final Predicate<BankReference> accountActive;
    private final Predicate<BankReference> accountHighlighted;
    private final Consumer<BankReference> handler;

    private TextBoxWrapper<String> searchBox = null;

    private String lastSearch = "";
    private int scroll = 0;

    private List<BankReference> accountCache = new ArrayList<>();

    protected BankAccountSelectionWidget(Builder builder) {
        super(builder);
        this.rows = builder.rows;
        this.filter = builder.filter;

        this.selected = builder.selected;
        this.accountActive = builder.active;
        this.accountHighlighted = builder.highlighted;
        this.handler = builder.handler;
        if(builder.oldWidget != null) {
            this.scroll = builder.oldWidget.scroll;
            this.lastSearch = builder.oldWidget.lastSearch;
            this.searchBox = builder.oldWidget.searchBox;
        }

    }

    @Override
    protected void addLateChildren(ScreenArea area) {
        //Search Box
        this.searchBox = this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(area.width - 88,2))
                .withStartingString(this.lastSearch)
                .withHandler(s -> this.lastSearch = s)
                .ofSize(79,9)
                .noBorder()
                .build());
        //Scroll Bar
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width,12))
                .ofHeight(area.height - 12)
                .visible(this::isVisible)
                .build());
        this.addChild(ScrollArea.builder()
                .ofArea(area)
                .withListener(this.buildScrollListener())
                .build());
        //Account Buttons
        for(int i = 0; i < this.rows; ++i) {
            final int index = i;
            this.addChild(BankAccountSelectButton.builder()
                    .atPos(area.pos.offset(0,12 + (i * BankAccountSelectButton.HEIGHT)))
                    .ofWidth(area.width)
                    .onPress(() -> this.selectAccount(index))
                    .active(this.accountActive)
                    .highlighted(this.accountHighlighted)
                    .forAccount(() -> this.getAccount(index))
                    .visible(this::isVisible)
                    .build());
        }
    }

    private boolean searchFilter(BankReference reference) {
        if(this.lastSearch.isBlank())
            return true;
        BankAccount account = reference == null ? null : reference.get();
        if(account == null)
            return false;
        return account.getName().getString().toLowerCase().contains(this.lastSearch.toLowerCase());
    }

    private BankReference getAccount(int index) { return ListHelper.getOrNull(this.accountCache,index); }

    private void selectAccount(int index) {
        BankReference account = this.getAccount(index);
        if(account != null)
            this.handler.accept(account);
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        if(this.visible)
            this.validateScroll();
        this.accountCache = new ArrayList<>(LCApi.getBankAPI().getAllBankReferences(ISidedContext.LOGICAL_CLIENT).stream().filter(this.filter).filter(this::searchFilter).toList());
        this.accountCache.sort(this);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        //Render Search Box
        gui.blitSprite(LCSprites.SEARCH_FIELD,area.width - 90,0,90);
        //Render Black BG
        gui.fill(0,12,area.width,area.height - 12,0xFF000000);
    }

    @Override
    public int getScroll() { return this.scroll; }
    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.accountCache.size(),this.rows); }

    //Filter so that the currently selected bank account is at the top of the list
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
            BankAccount baA = rA.get();
            BankAccount baB = rB.get();
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

    private boolean isCurrentAccount(@Nullable BankReference reference) {
        return reference != null && reference.equals(this.selected.get());
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,BankAccountSelectionWidget> {

        private Builder() { super(100,12 + BankAccountSelectButton.HEIGHT); }

        @Nullable
        BankAccountSelectionWidget oldWidget;
        int rows = 1;
        Supplier<BankReference> selected = () -> null;
        Predicate<BankReference> filter = r -> true;
        Predicate<BankReference> active = r -> true;
        Predicate<BankReference> highlighted = r -> false;
        Consumer<BankReference> handler = r -> {};

        public Builder oldWidget(@Nullable BankAccountSelectionWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder ofWidth(int width) { this.setWidth(width); return this; }
        public Builder withRows(int rows) { this.rows = rows; this.setHeight(12 + rows * BankAccountSelectButton.HEIGHT); return this; }

        public Builder withFilter(Predicate<BankReference> filter) { this.filter = filter; return this; }
        public Builder withSelected(Supplier<BankReference> selectedAccount) { this.selected = selectedAccount; this.active = r -> !r.equals(selectedAccount.get()); return this; }
        public Builder withActiveFilter(Predicate<BankReference> activeFilter) { this.active = activeFilter; return this; }
        public Builder withHighlight(Predicate<BankReference> highlighted) { this.highlighted = highlighted; return this; }

        public Builder withHandler(Consumer<BankReference> handler) { this.handler = handler; return this; }

        @Override
        protected Builder getSelf() { return this; }

        @Override
        public BankAccountSelectionWidget build() { return new BankAccountSelectionWidget(this); }
    }

}

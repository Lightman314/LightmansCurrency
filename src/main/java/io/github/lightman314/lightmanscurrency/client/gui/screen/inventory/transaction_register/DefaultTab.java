package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.transaction_register;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleHeightSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.VerticalSliceSprite;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.FlexibleMoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TransactionRegisterScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TransactionDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.items.data.register.TransactionData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;

public class DefaultTab extends TransactionRegisterTab implements IScrollable {

    public static final FlexibleHeightSprite PAPER_SCROLL_BG = new VerticalSliceSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_vert_background_paper"),8,128),8);
    public static final FixedSizeSprite PAPER_SCROLL_KNOB = new NormalSprite(SpriteSource.create(VersionUtil.lcResource("common/widgets/scrollbar_vert_knob_paper"),8,29));

    private final StartingBalanceTab startingBalanceTab;

    public DefaultTab(TransactionRegisterScreen screen) {
        super(screen);
        this.scroll = this.getMaxScroll();
        this.startingBalanceTab = new StartingBalanceTab(screen);
    }

    public static final int ROWS = 15;

    private int scroll = 0;

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ROWS,this.menu.getData().transactions.size() + 1); }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        int yPos = 26;
        //Create "Edit" buttons for each entry
        for(int i = 0; i < ROWS; ++i)
        {
            final int index = i;
            int offset = i == 0 ? 1 : 0;
            this.addChild(TransactionDisplayWidget.builder(offset)
                    .position(screenArea.pos.offset(9,yPos))
                    .ofIndex(() -> index + this.scroll + 1)
                    .withPreviousBalance(() -> this.getPreviousBalance(index))
                    .withTransaction(() -> this.getNullableTransaction(index + this.scroll))
                    .pressAction(() -> this.editTransaction(index))
                    .build());
            yPos += 11 + offset;
        }

        //Scroll Listeners/Bars
        this.addChild(ScrollListener.builder().area(screenArea).listener(this).build());
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - 16,26))
                .height((11 * ROWS) + 1)
                .scrollable(this)
                .customBackground(PAPER_SCROLL_BG)
                .customKnob(PAPER_SCROLL_KNOB)
                .alwaysShow()
                .build());

        //Starting Balance Button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,0))
                .icon(ItemIcon.ofItem(ModBlocks.COINPILE_GOLD))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRANSACTION_REGISTER_STARTING_BALANCE))
                .pressAction(this::editStartingBalance)
                .build());

        //Recalculate Button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,20))
                .icon(IconUtil.ICON_PLUS)
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_TRANSACTION_REGISTER_REDO_CALCULATIONS))
                .pressAction(this::redoCalculations)
                .build());

    }

    private FlexibleMoneyValue getPreviousBalance(int localIndex)
    {
        int index = localIndex + this.scroll;
        TransactionData previousTransaction = this.getNullableTransaction(index);
        if(previousTransaction != null)
            return previousTransaction.resultValue;
        return FlexibleMoneyValue.positive(this.getData().startingValue);
    }

    @Override
    public void renderBG(EasyGuiGraphics gui) {
        this.validateScroll();

        //Render Labels
        gui.drawString(LCText.GUI_TRANSACTION_REGISTER_LABEL_INDEX.get(),10 + TransactionDisplayWidget.INDEX_POS,17,0);
        gui.drawString(LCText.GUI_TRANSACTION_REGISTER_LABEL_COMMENT.get(),10 + TransactionDisplayWidget.COMMENT_POS,17,0);
        gui.drawString(LCText.GUI_TRANSACTION_REGISTER_LABEL_CHANGE.get(),10 + TransactionDisplayWidget.ARGUMENT_POS,17,0);
        gui.drawString(LCText.GUI_TRANSACTION_REGISTER_LABEL_BALANCE.get(),10 + TransactionDisplayWidget.BALANCE_POS,17,0);

    }

    private boolean isButtonVisible(int localIndex)
    {
        int index = this.scroll + localIndex;
        return this.hasTransaction(index) || this.hasTransaction(index - 1);
    }

    private void editTransaction(int localIndex)
    {
        int index = this.scroll + localIndex;
        if(this.hasTransaction(index))
            this.screen.setTab(new EditTransactionTab(this.screen,index));
        else
        {
            this.menu.CreateTransaction();
            index = this.menu.getData().transactions.size() - 1;
            if(this.hasTransaction(index))
                this.screen.setTab(new EditTransactionTab(this.screen,index));
        }
    }

    private void editStartingBalance() { this.screen.setTab(this.startingBalanceTab); }

    private void redoCalculations() { this.menu.RedoCalculations(); }

}
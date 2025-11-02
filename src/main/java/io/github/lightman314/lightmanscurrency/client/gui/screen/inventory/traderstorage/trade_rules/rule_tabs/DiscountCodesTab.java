package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRuleSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.DiscountCodes;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscountCodesTab extends TradeRuleSubTab<DiscountCodes> implements IScrollable {

    public static final int ROWS = 4;

    public DiscountCodesTab(@Nonnull TradeRulesClientTab<?> parent) { super(parent, DiscountCodes.TYPE); }

    String selectedCode = "";
    private int scroll = 0;

    private EditBox entryCreationInput = null;

    private EditBox codeModificationInput = null;
    private EditBox discountInput = null;
    private EditBox limitInput = null;
    private TimeInputWidget timerInput = null;

    private boolean hasCodeSelected() { return !this.selectedCode.isBlank() && this.getData().containsKey(this.selectedCode); }
    private boolean isSelectionMode() { return !this.hasCodeSelected(); }

    private Map<String,DiscountCodes.DiscountRules> getData()
    {
        DiscountCodes rule = this.getRule();
        return rule == null ? new HashMap<>() : rule.getRules();
    }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

        /// Selection Mode
        for(int i = 0; i < ROWS; ++i)
        {
            final int index = i;
            this.addChild(EasyTextButton.builder()
                    .position(screenArea.pos.offset(30,10 + (20 * i)))
                    .width(screenArea.width - 60)
                    .text(() -> this.getSelectionButtonText(index))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.isSelectionButtonVisible(index)))
                    .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_DISCOUNT_CODES_ENTRY))
                    .pressAction(() -> this.selectEntry(index))
                    .build());
        }
        //Scroll Area & Bar
        this.addChild(ScrollListener.builder()
                .position(screenArea.pos.offset(30,10))
                .size(screenArea.width - 60,20 * ROWS)
                .listener(this)
                .build());

        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(30 + screenArea.width - 60, 30))
                .height(20 * ROWS)
                .scrollable(this)
                .addon(EasyAddonHelper.visibleCheck(this::isSelectionMode))
                .build());

        //Create Input and Button
        this.entryCreationInput = this.addChild(TextInputUtil.stringBuilder()
                .maxLength(16)
                .position(screenArea.pos.offset(30, 109))
                .width(screenArea.width - 60)
                .filter(TicketStationRecipe.CODE_INPUT_PREDICATE)
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 110,131))
                .text(LCText.BUTTON_DISCOUNT_CODES_CREATE)
                .width(80)
                .addon(EasyAddonHelper.visibleCheck(this::isSelectionMode))
                .addon(EasyAddonHelper.activeCheck(() -> this.isCodeAvailable(this.entryCreationInput.getValue())))
                .pressAction(() -> {
                    String newCode = this.entryCreationInput.getValue();
                    if(!this.isCodeAvailable(newCode))
                        return;
                    //Send creation request
                    this.sendUpdateMessage(this.builder().setString("Create",newCode));
                    //Select the created code automatically
                    this.changeSelection(newCode);
                    //Reset the input field
                    this.entryCreationInput.setValue("");
                })
                .build());

        /// Edit Mode
        //Delete Button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(10,10))
                .icon(IconUtil.ICON_X)
                .pressAction(() -> this.sendUpdateMessage(this.builder()
                        .setString("Remove",this.selectedCode)))
                .addon(EasyAddonHelper.visibleCheck(this::hasCodeSelected))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_DISCOUNT_CODES_DELETE.get()))
                .build());

        //Back Button
        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 30,10))
                .icon(IconUtil.ICON_BACK)
                .pressAction(() -> this.changeSelection(""))
                .addon(EasyAddonHelper.visibleCheck(this::hasCodeSelected))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_DISCOUNT_CODES_BACK.get()))
                .build());

        //Code Edit Input and Button
        this.codeModificationInput = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(40,10))
                .width(screenArea.width - 80)
                .maxLength(16)
                .filter(TicketStationRecipe.CODE_INPUT_PREDICATE)
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(40,35))
                .width(screenArea.width - 80)
                .text(LCText.BUTTON_DISCOUNT_CODES_CHANGE)
                .addon(EasyAddonHelper.activeCheck(() -> this.isCodeAvailable(this.codeModificationInput.getValue())))
                .addon(EasyAddonHelper.visibleCheck(this::hasCodeSelected))
                .pressAction(() -> {
                    String newCode = this.codeModificationInput.getValue();
                    if(!this.isCodeAvailable(newCode))
                        return;
                    //Request a rename
                    this.sendUpdateMessage(this.builder()
                            .setString("Edit",this.selectedCode)
                            .setString("Rename",newCode)
                    );
                    //Select the newly edited code
                    this.changeSelection(newCode);
                })
                .build());

        //Discount Input
        this.discountInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset(20,75))
                .width(75)
                .parser(IntParser.ONE_TO_ONE_HUNDRED)
                .maxLength(3)
                .startingValue(10)
                .handler(discount -> {
                            this.sendUpdateMessage(this.builder()
                                    .setString("Edit",this.selectedCode)
                                    .setInt("Discount",discount));
                        }
                ).build());

        //Limit Input
        this.limitInput = this.addChild(TextInputUtil.intBuilder()
                .position(screenArea.pos.offset((screenArea.width / 2) + 10,75))
                .width(75)
                .apply(IntParser.builder().min(0).max(100).consumer())
                .maxLength(3)
                .handler(limit -> {
                            this.sendUpdateMessage(this.builder()
                                    .setString("Edit",this.selectedCode)
                                    .setInt("Limit",limit));
                        }
                ).build());

        //Timer Input
        this.timerInput = this.addChild(TimeInputWidget.builder()
                .position(screenArea.pos.offset((screenArea.width / 2) - 25,108))
                .unitRange(TimeUtil.TimeUnit.HOUR,TimeUtil.TimeUnit.DAY)
                .maxDuration(TimeUtil.DURATION_DAY * 365)
                .handler(time -> {
                    this.sendUpdateMessage(this.builder()
                            .setString("Edit",this.selectedCode)
                            .setLong("Timer",time.miliseconds));
                })
                .addon(EasyAddonHelper.visibleCheck(this::hasCodeSelected))
                .build());

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {
        this.validateWidgets();
        if(this.hasCodeSelected())
        {
            //Render Input Labels
            gui.drawString(LCText.GUI_DISCOUNT_CODES_DISCOUNT.get(),20, 65,0x404040);
            gui.drawString(LCText.GUI_DISCOUNT_CODES_LIMIT.get(),(this.screen.getXSize() / 2) + 10, 65,0x404040);

            //Render Time Limit Info
            DiscountCodes.DiscountRules rule = this.getData().get(this.selectedCode);
            if(rule == null)
                return;
            Component text = rule.timeLimit > 0 ? LCText.GUI_PLAYER_TRADE_LIMIT_DURATION.get(new TimeUtil.TimeData(rule.timeLimit).getShortString()) : LCText.GUI_PLAYER_TRADE_LIMIT_NO_DURATION.get();
            TextRenderUtil.drawCenteredText(gui, text, this.screen.getXSize() / 2, 98, 0x404040);

        }

    }

    private void validateWidgets()
    {
        this.entryCreationInput.visible = this.isSelectionMode();
        this.codeModificationInput.visible = this.discountInput.visible = this.limitInput.visible = this.hasCodeSelected();
    }

    @Nullable
    private Pair<String,DiscountCodes.DiscountRules> getEntry(int index)
    {
        index += this.scroll;
        List<Map.Entry<String, DiscountCodes.DiscountRules>> list = new ArrayList<>(this.getData().entrySet());
        if(index < 0 || index >= list.size())
            return null;
        Map.Entry<String,DiscountCodes.DiscountRules> entry = list.get(index);
        return Pair.of(entry.getKey(),entry.getValue());
    }

    private void changeSelection(String newSelection)
    {
        this.selectedCode = newSelection;
        this.codeModificationInput.setValue(newSelection);
        DiscountCodes.DiscountRules rules = this.getData().get(newSelection);
        if(rules == null)
            return;
        this.discountInput.setValue(String.valueOf(rules.discount));
        this.limitInput.setValue(String.valueOf(rules.limit));
        this.timerInput.setTime(rules.timeLimit);
    }

    private boolean isSelectionButtonVisible(int index)
    {
        if(this.hasCodeSelected())
            return false;
        return this.getEntry(index) != null;
    }

    private Component getSelectionButtonText(int index)
    {
        var entry = this.getEntry(index);
        if(entry != null)
            return LCText.BUTTON_DISCOUNT_CODES_ENTRY.get(entry.getFirst(),entry.getSecond().discount);
        return EasyText.empty();
    }

    private void selectEntry(int index)
    {
        var entry = this.getEntry(index);
        if(entry != null)
            this.changeSelection(entry.getFirst());
    }

    @Override
    public boolean handleScrollWheel(double delta) {
        if(this.hasCodeSelected())
            return false;
        return IScrollable.super.handleScrollWheel(delta);
    }

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(ROWS,this.getData().size()); }

    private boolean isCodeAvailable(String newCode) { return TicketStationRecipe.CODE_PREDICATE.test(newCode) && !this.getData().containsKey(newCode); }

}
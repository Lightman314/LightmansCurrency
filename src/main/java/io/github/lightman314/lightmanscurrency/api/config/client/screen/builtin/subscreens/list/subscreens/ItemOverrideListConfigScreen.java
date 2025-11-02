package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LCVersionText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.config.ItemOverrideListOption;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.ItemOverride;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemOverrideListConfigScreen extends ConfigScreen implements IScrollable {

    private final ConfigFileOption file;
    private final ItemOverrideListOption option;
    private final int index;
    private final Consumer<Object> changeHandler;
    private final Consumer<ConfigOption<?>> optionListener;
    public ItemOverrideListConfigScreen(Screen parentScreen, ConfigFileOption file, ItemOverrideListOption option, int index, Consumer<Object> changeHandler)
    {
        super(parentScreen);
        this.file = file;
        this.option = option;
        this.index = index;
        this.changeHandler = changeHandler;
        this.optionListener = this::onOptionChange;
        this.option.addListener(this.optionListener);
    }

    private MoneyValueWidget valueWidget;
    private final List<TextBoxWrapper<String>> textInputs = new ArrayList<>();
    private int scroll = 0;
    private boolean ignoreInputs = false;
    @Nullable
    private String latestInput = null;
    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        if(!this.ignoreInputs)
            this.updateTextInputs(this.getValue());
    }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.textInputs.size(),this.getValue().writeList().size() + 1); }

    private ItemOverride getValue()
    {
        List<ItemOverride> list = this.option.get();
        if(this.index < 0 || this.index >= list.size())
            return new ItemOverride(MoneyValue.empty(),new ArrayList<>());
        return list.get(this.index);
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        ItemOverride currentValue = this.getValue();
        //Money Value Input
        this.valueWidget = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.centerX() - (MoneyValueWidget.WIDTH / 2), this.headerSize() + 20)
                .startingValue(currentValue.baseCost)
                .valueHandler(this::changePrice)
                .addon(EasyAddonHelper.activeCheck(() -> this.file.canEdit(this.minecraft)))
                .build());

        //List of scrollable text fields for entries
        this.textInputs.clear();
        int availableSpace = screenArea.height - this.headerSize() - this.footerSize() - 110;
        int rows = Math.max(1,availableSpace/25);
        List<String> startingList = currentValue.writeList();
        for(int i = 0; i < rows; ++i)
        {
            final int index = i;
            String startingValue = index >= startingList.size() ? "" : startingList.get(i);
            this.textInputs.add(this.addChild(TextInputUtil.stringBuilder()
                    .position(screenArea.centerX() - 100,this.headerSize() + 110 + (25 * i))
                    .width(200)
                    .handler(s -> this.changeInput(s,index))
                    .startingValue(startingValue)
                    .wrap()
                    .addon(EasyAddonHelper.visibleCheck(() -> this.isFieldVisible(index)))
                    .build()));
        }

        //Scroll Bar
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.centerX() + 120,this.headerSize() + 110)
                .height((rows * 25) - 5)
                .scrollable(this)
                .build());
        this.addChild(ScrollListener.builder()
                .position(0, this.headerSize() + 110)
                .size(screenArea.width,(rows * 25) - 5)
                .listener(this)
                .build());

        //Back button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() - 100, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    private void changePrice(MoneyValue value)
    {
        if(this.ignoreInputs)
            return;
        ItemOverride override = this.getValue();
        this.changeHandler.accept(new ItemOverride(value,override.writeList()));
    }

    private void changeInput(String value, int index)
    {
        if(this.ignoreInputs)
            return;
        if(!this.isValidString(value))
            return;
        //Factor in the new scroll value
        index += this.scroll;
        ItemOverride override = this.getValue();
        List<String> inputs = override.writeList();
        if(index >= 0 && index < inputs.size())
        {
            if(value.isBlank())
                inputs.remove(index);
            else
                inputs.set(index,value);
        }
        else if(!value.isBlank())
        {
            //Check for changes input index to automatically change targeted input
            inputs.add(value);
            this.latestInput = value;
        }
        try { this.changeHandler.accept(new ItemOverride(override.baseCost,inputs));
        } catch (ResourceLocationException ignored) {}
    }

    private boolean isValidString(String value)
    {
        if(value.isEmpty())
            return true;
        //Block incomplete resource locations as otherwise the option change fill-in makes things awkward if you're attempting to target a non-minecraft item/tag
        if(!value.contains(":"))
            return false;
        if(value.startsWith("#"))
            return isValidResourceLocation(value.substring(1));
        return isValidResourceLocation(value);
    }

    private boolean isValidResourceLocation(String value)
    {
        try {
            VersionUtil.parseResource(value);
            return true;
        } catch (ResourceLocationException ignored) { return false; }
    }

    private boolean isFieldVisible(int index)
    {
        int trueIndex = index + this.scroll;
        List<String> inputs = this.getValue().writeList();
        return trueIndex >= 0 && trueIndex <= inputs.size();
    }

    private void updateTextInputs(ItemOverride newValue)
    {
        this.ignoreInputs = true;
        List<String> values = newValue.writeList();
        int focusIndex = -1;
        if(this.latestInput != null)
        {
            focusIndex = values.indexOf(this.latestInput);
            //Clear the latestInput tracking
            this.latestInput = null;
            if(focusIndex >= 0)
            {
                //Confirm that the current scroll will still display this text box
                int range = this.textInputs.size();
                if(this.scroll < focusIndex - range)
                    this.scroll = focusIndex - range;
                else if(this.scroll > focusIndex)
                    this.scroll = focusIndex;
            }
        }
        this.validateScroll();
        for(int i = 0; i < this.textInputs.size(); ++i)
        {
            int actualIndex = i + this.scroll;
            String newString = actualIndex >= 0 && actualIndex < values.size() ? values.get(actualIndex) : "";
            TextBoxWrapper<String> textBox = this.textInputs.get(i);
            textBox.setValue(newString);
            if(actualIndex == focusIndex)
                textBox.getWrappedWidget().setFocused(true);
            else if(focusIndex >= 0)
                textBox.getWrappedWidget().setFocused(false);
        }
        this.ignoreInputs = false;
    }

    private void onOptionChange(ConfigOption<?> option)
    {
        ItemOverride newValue = this.getValue();
        this.ignoreInputs = true;
        if(this.valueWidget != null)
            this.valueWidget.changeValue(newValue.baseCost);
        this.updateTextInputs(newValue);
        this.ignoreInputs = false;
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName(), LCText.CONFIG_OPTION_LIST_ENTRY.get(this.index + 1)); }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        int centerX = this.getArea().centerX();
        if(this.valueWidget != null)
            SpriteUtil.GENERIC_BACKGROUND.render(gui,this.valueWidget.getX() - 5, this.valueWidget.getY() - 10, MoneyValueWidget.WIDTH + 10, MoneyValueWidget.HEIGHT + 20);
        TextRenderUtil.drawCenteredText(gui, LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_MONEY.get(), centerX, this.headerSize() + 2,0xFFFFFF,true);
        TextRenderUtil.drawCenteredText(gui, LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_ITEMS.get(), centerX, this.headerSize() + 100,0xFFFFFF,true);
    }

    @Override
    protected void renderAfterWidgets(EasyGuiGraphics gui) {
        if(ScreenArea.of(this.getArea().centerX() - 100,this.headerSize() + 100,200,10).isMouseInArea(gui.mousePos))
            gui.renderTooltip(LCVersionText.CONFIG_ITEM_OVERRIDE_LABEL_ITEMS_TOOLTIP.get());
    }

    @Override
    protected void screenTick() {
        if(this.index < 0 || this.index >= this.option.getSize())
            this.onClose();
    }

    @Override
    protected void afterClose() {
        this.option.removeListener(this.optionListener);
    }
}

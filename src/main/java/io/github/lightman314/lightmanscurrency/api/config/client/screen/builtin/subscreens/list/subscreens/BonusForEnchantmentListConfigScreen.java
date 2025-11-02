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
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.config.BonusForEnchantmentListOption;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.BonusForEnchantment;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BonusForEnchantmentListConfigScreen extends ConfigScreen {

    private final ConfigFileOption file;
    private final BonusForEnchantmentListOption option;
    private final int index;
    private final Consumer<Object> changeHandler;
    private final Consumer<ConfigOption<?>> optionListener;
    public BonusForEnchantmentListConfigScreen(Screen parentScreen, ConfigFileOption file, BonusForEnchantmentListOption option, int index, Consumer<Object> changeHandler)
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
    private TextBoxWrapper<String> enchantmentInput;
    private TextBoxWrapper<Integer> levelInput;
    private boolean ignoreInput = false;

    private BonusForEnchantment getValue()
    {
        List<BonusForEnchantment> list = this.option.get();
        if(this.index < 0 || this.index >= list.size())
            return new BonusForEnchantment(MoneyValue.empty(), VersionUtil.vanillaResource("null"),0);
        return list.get(this.index);
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        int centerX = screenArea.centerX();
        int centerY = screenArea.centerY();

        BonusForEnchantment currentValue = this.getValue();

        this.valueWidget = this.addChild(MoneyValueWidget.builder()
                .position(centerX - (MoneyValueWidget.WIDTH / 2),centerY - MoneyValueWidget.HEIGHT - 10)
                .startingValue(currentValue.bonusCost)
                .valueHandler(this::changeValue)
                .addon(EasyAddonHelper.activeCheck(() -> this.file.canEdit(this.minecraft)))
                .build());

        this.enchantmentInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,centerY + 10)
                .width(200)
                .startingValue(currentValue.enchantment.toString())
                .handler(this::changeEnchantment)
                .wrap()
                .addon(EasyAddonHelper.activeCheck(() -> this.file.canEdit(this.minecraft)))
                .build());

        this.levelInput = this.addChild(TextInputUtil.intBuilder()
                .position(centerX - 100,centerY + 50)
                .width(200)
                .startingValue(currentValue.maxLevelCalculation)
                .apply(IntParser.builder().min(0).consumer())
                .handler(this::changeMaxLevel)
                .wrap()
                .addon(EasyAddonHelper.activeCheck(() -> this.file.canEdit(this.minecraft)))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(centerX - 100, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    private void changeValue(MoneyValue value)
    {
        if(this.ignoreInput)
            return;
        BonusForEnchantment val = this.getValue();
        this.changeHandler.accept(new BonusForEnchantment(value,val.enchantment,val.maxLevelCalculation));
    }

    private void changeEnchantment(String newVal)
    {
        if(this.ignoreInput)
            return;
        BonusForEnchantment val = this.getValue();
        try {
            ResourceLocation enchant = VersionUtil.parseResource(newVal);
            this.changeHandler.accept(new BonusForEnchantment(val.bonusCost,enchant,val.maxLevelCalculation));
        } catch (ResourceLocationException ignored) {}
    }

    private void changeMaxLevel(int newVal)
    {
        if(this.ignoreInput)
            return;
        BonusForEnchantment val = this.getValue();
        this.changeHandler.accept(new BonusForEnchantment(val.bonusCost,val.enchantment,newVal));
    }

    private void onOptionChange(ConfigOption<?> option)
    {
        BonusForEnchantment newVal = this.getValue();
        this.ignoreInput = true;
        if(this.valueWidget != null)
            this.valueWidget.changeValue(newVal.bonusCost);
        if(this.enchantmentInput != null)
            this.enchantmentInput.setValue(newVal.enchantment.toString());
        if(this.levelInput != null)
            this.levelInput.setValue(newVal.maxLevelCalculation);

        this.ignoreInput = false;
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName(), LCText.CONFIG_OPTION_LIST_ENTRY.get(this.index + 1)); }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        int centerX = this.getArea().centerX();
        int centerY = this.getArea().centerY();
        if(this.valueWidget != null)
            SpriteUtil.GENERIC_BACKGROUND.render(gui,this.valueWidget.getX() - 5, this.valueWidget.getY() - 10, MoneyValueWidget.WIDTH + 10, MoneyValueWidget.HEIGHT + 20);
        TextRenderUtil.drawCenteredText(gui, LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_MONEY.get(),centerX,centerY - MoneyValueWidget.HEIGHT - 32,0xFFFFFF,true);
        TextRenderUtil.drawCenteredText(gui, LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_ENCHANTMENT.get(),centerX,centerY,0xFFFFFF,true);
        TextRenderUtil.drawCenteredText(gui, LCVersionText.CONFIG_ENCHANTMENT_BONUS_LABEL_LEVEL.get(),centerX,centerY + 40,0xFFFFFF,true);
    }

    @Override
    protected void screenTick() {
        if(this.index < 0 || this.index >= this.option.getSize())
            this.onClose();
    }

    @Override
    protected void afterClose() { this.option.removeListener(this.optionListener); }

}
package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyValueConfigScreen extends ConfigScreen {

    private final ConfigFileOption file;
    private final MoneyValueOption option;
    private final Consumer<Object> changeHandler;
    public MoneyValueConfigScreen(Screen parentScreen, ConfigFileOption file, MoneyValueOption option, Consumer<Object> changeHandler) {
        super(parentScreen);
        this.file = file;
        this.option = option;
        this.changeHandler = changeHandler;
    }

    private MoneyValueWidget valueWidget;

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.valueWidget = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.centerX() - (MoneyValueWidget.WIDTH / 2),screenArea.centerY() - (MoneyValueWidget.HEIGHT / 2))
                .startingValue(this.option.get())
                .allowFreeInputs(this.option.allowed.test(MoneyValue.free()))
                .valueHandler(this::changeValue)
                .addon(EasyAddonHelper.activeCheck(() -> this.file.canEdit(this.minecraft)))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() - 100,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        if(this.valueWidget != null)
            SpriteUtil.GENERIC_BACKGROUND.render(gui,this.valueWidget.getX() - 5, this.valueWidget.getY() - 10, MoneyValueWidget.WIDTH + 10, MoneyValueWidget.HEIGHT + 20);
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName()); }

    private void changeValue(MoneyValue value)
    {
        //Don't send onwards if it's an invalid option
        if(this.option.allowed.test(value))
            this.changeHandler.accept(value);
    }

}
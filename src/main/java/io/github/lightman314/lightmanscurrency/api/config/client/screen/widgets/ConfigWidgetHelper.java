package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.*;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.MoneyValueConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings.*;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.ScreenPositionOptionInput;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SimpleButtonOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SimpleEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.*;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.*;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ConfigWidgetHelper {
    private ConfigWidgetHelper() {}

    private static final List<OptionWidgetBuilder> builders;

    static {
        builders = new ArrayList<>();
        registerWidgetBuilder(new DefaultWidgetBuilder());
    }

    public static void registerWidgetBuilder(OptionWidgetBuilder builder) { builders.add(builder); }

    public static ScrollingWidgetBuilder getBuilderForOption(Screen screen, ConfigFileOption file, ConfigOption<?> option, Consumer<Object> changeValueConsumer, BooleanSupplier canEdit) {
        for(OptionWidgetBuilder builder : builders) {
            ScrollingWidgetBuilder result = builder.createBuilderForOption(screen,file,option,changeValueConsumer,canEdit);
            if(result != null)
                return result;
        }
        return SimpleButtonOption.createUndefined(option);
    }

    private static class DefaultWidgetBuilder implements OptionWidgetBuilder {

        @Nullable
        @Override
        public ScrollingWidgetBuilder createBuilderForOption(Screen screen, ConfigFileOption file, ConfigOption<?> o, Consumer<Object> changeValueConsumer, BooleanSupplier canEdit) {
            if(o instanceof BooleanOption option) {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.getComponent(option.get()))
                        .clickHandler2(handler -> handler.accept(!option.get()))
                        .build();
            }
            if(o instanceof DoubleOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.doubleBuilder()
                                .parser(DoubleParser.builder().min(option.lowerLimit).max(option.upperLimit).build())
                                .withStartingValue(option.get()))
                        .numberChangeHandler(option::get)
                        .build();
            }
            if(o instanceof EnumOption<?> option) {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> Component.literal(option.get().name()))
                        .clickHandler((left,handler) ->
                            handler.accept(EnumHelper.unsafeEnumCycle(option.clazz,option.get(),left)))
                        .build();
            }
            if(o instanceof FloatOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.floatBuilder()
                                .parser(FloatParser.builder().min(option.lowerLimit).max(option.upperLimit).build())
                                .withStartingValue(option.get()))
                        .numberChangeHandler(option::get)
                        .build();
            }
            if(o instanceof LongOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.longBuilder()
                                .parser(LongParser.builder().min(option.lowerLimit).max(option.upperLimit).build())
                                .withStartingValue(option.get()))
                        .numberChangeHandler(option::get)
                        .build();
            }
            if(o instanceof IntOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.intBuilder()
                                .parser(IntParser.builder().min(option.lowerLimit).max(option.upperLimit).build())
                                .withStartingValue(option.get()))
                        .numberChangeHandler(option::get)
                        .build();
            }
            if(o instanceof StringOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.stringBuilder()
                                .withStartingValue(option.getDefaultValue()))
                        .optionChangeHandler(text -> text.setStringValue(option.get()))
                        .build();
            }
            if(o instanceof ItemOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler -> TextBoxWrapper.builder(RegistryParser.builder(BuiltInRegistries.ITEM).requiresNamespace().withEmptyValue(Items.AIR).build(),RegistryParser::writer)
                                .withStartingValue(option.get())
                                .withHandler(item -> {
                                    if(option.allowedValue(item))
                                        handler.accept(item);
                                }))
                        .optionChangeHandler(text -> {
                            Item newValue = option.get();
                            if(newValue == Items.AIR && text.getString().isEmpty())
                                return;
                            text.setStringValue(BuiltInRegistries.ITEM.getKey(newValue).toString());
                        })
                        .build();
            }
            if(o instanceof MoneyValueOption option) {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> option.get().getText(MoneyDisplayHelper.GUI_MONEY_STORAGE_EMPTY.get()))
                        .openScreen(handler -> new MoneyValueConfigScreen(screen,file,option,handler))
                        .build();
            }
            if(o instanceof IdentifierOption option) {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(() -> TextBoxWrapper.identifierBuilder(true)
                                .withStartingValue(option.get()))
                        .optionChangeHandler(text -> text.setStringValue(option.get().toString()))
                        .build();
            }
            if(o instanceof ScreenPositionOption option)
                return ScreenPositionOptionInput.create(option,changeValueConsumer,canEdit);
            //List Options
            if(o instanceof DoubleListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new DoubleListSettings(option,handler));
            if(o instanceof FloatListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new FloatListSettings(option,handler));
            if(o instanceof IntListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new IntListSettings(option,handler));
            if(o instanceof LongListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new LongListSettings(option,handler));
            if(o instanceof StringListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new StringListSettings(option,handler));
            if(o instanceof ItemListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new ItemListSettings(option,handler));
            if(o instanceof MoneyValueListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new MoneyValueListSettings(option,handler));
            if(o instanceof IdentifierListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new IdentifierListSettings(option,handler));
            if(o instanceof WildcardSelectorListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new WildcardSelectorListSettings(option,handler));
            //TODO custom item scale config option
            //TODO item test list option
            return null;
        }

    }

}
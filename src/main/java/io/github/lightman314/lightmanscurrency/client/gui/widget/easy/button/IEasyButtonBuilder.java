package io.github.lightman314.lightmanscurrency.client.gui.widget.easy.button;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.IEasyWidgetBuilder;

import java.util.function.Consumer;

public interface IEasyButtonBuilder extends IEasyWidgetBuilder {

    Consumer<Object> getClickConsumer();

}

package io.github.lightman314.lightmanscurrency.api.traders.settings;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;

public abstract class EasyTraderNodeSettings<T extends TraderData,N extends TraderNode> extends EasyTraderSettingsNode<T> {

    protected final N node;
    public EasyTraderNodeSettings(String key, T trader,N node) { super(key, trader); this.node = node; }
    public EasyTraderNodeSettings(String key, T trader,N node, int priority) { super(key, trader, priority); this.node = node; }

}

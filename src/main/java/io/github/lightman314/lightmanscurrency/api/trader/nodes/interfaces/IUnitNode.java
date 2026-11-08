package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;

/**
 * Interface to be applied to a {@link TraderNode TraderNode}
 * that will prevent it from being saved to file.<br>
 * If a codec is included in the type, it will still be written to json when displayed with the /lcdebug command.
 */
public interface IUnitNode { }
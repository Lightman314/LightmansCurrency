package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

public interface IRenderTick {

    default boolean renderTickLate() { return false; }
    void renderTick();

}
package io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces;

public interface IMouseListener {

    boolean onMouseClicked(double mouseX, double mouseY, int button);
    default boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }

}

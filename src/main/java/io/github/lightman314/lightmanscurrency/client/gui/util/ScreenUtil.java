package io.github.lightman314.lightmanscurrency.client.gui.util;

import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

public class ScreenUtil {

    //Mouse Over checks (int)
    public static boolean isMouseOver(int mouseX, int mouseY, int startX, int startY, int width, int height) { return mouseX >= startX && mouseX < startX + width && mouseY >= startY && mouseY < startY + height; }
    public static boolean isMouseOver(int mouseX, int mouseY, ScreenPosition position, int width, int height) { return isMouseOver(mouseX, mouseY, position.x, position.y, width, height); }

    //Mouse Over checks (double)
    public static boolean isMouseOver(double mouseX, double mouseY, int startX, int startY, int width, int height) { return mouseX >= startX && mouseX < startX + width && mouseY >= startY && mouseY < startY + height; }
    public static boolean isMouseOver(double mouseX, double mouseY, ScreenPosition position, int width, int height) { return isMouseOver(mouseX, mouseY, position.x, position.y, width, height); }

}
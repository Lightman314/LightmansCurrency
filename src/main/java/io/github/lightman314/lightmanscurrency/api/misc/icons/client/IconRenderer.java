package io.github.lightman314.lightmanscurrency.api.misc.icons.client;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconType;
import io.github.lightman314.lightmanscurrency.client.util.ClientRegistry;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

public abstract class IconRenderer<T extends IconData> {

    private static final ClientRegistry<IconType<?>,IconRenderer<?>> REGISTRY = new ClientRegistry<>(LCRegistries.ICON_TYPE,"Icon Renderer",() -> NullIconRenderer.INSTANCE);

    public static <T extends IconData> void register(IconType<? extends T> type,IconRenderer<T> renderer) { REGISTRY.register(type,renderer); }

    public final void render(IconData icon,EasyGuiGraphics gui,int x, int y) throws ClassCastException {
        this.renderInternal((T)icon,gui,x,y);
    }
    protected abstract void renderInternal(T icon, EasyGuiGraphics gui, int x, int y);

    public static void renderIcon(IconData icon, EasyGuiGraphics gui, ScreenPosition position) { renderIcon(icon,gui,position.x,position.y); }
    public static void renderIcon(IconData icon,EasyGuiGraphics gui,int x, int y) {
        try {
            IconRenderer<?> renderer =REGISTRY.getOrThrow(icon.getType());
            renderer.render(icon,gui,x,y);
        } catch (ClassCastException e) { LightmansCurrency.LogError("Icon Renderer of type " + LCRegistries.ICON_TYPE.getKey(icon.getType()) + " does not match its renderers class"); }
    }

}

package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.EasyClientUnenforcedTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.transaction_register.*;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TransactionRegisterMenu;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TransactionRegisterScreen extends EasyClientUnenforcedTabbedMenuScreen<TransactionRegisterMenu,TransactionRegisterScreen,TransactionRegisterTab> {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;
    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/transaction_register.png");
    public static final ResourceLocation OVERLAY_TEXTURE = VersionUtil.lcResource("textures/gui/transaction_register_overlay.png");

    public TransactionRegisterScreen(TransactionRegisterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, DefaultTab::new);
        this.resize(WIDTH,HEIGHT);
    }

    @Override
    protected void init(ScreenArea screenArea) {
        //Nothing to initialize here since everything is tab-based
    }

    @Override
    protected void renderBackground(EasyGuiGraphics gui) {
        //Render BG
        gui.renderNormalBackground(GUI_TEXTURE,this);
        //Render Overlay
        gui.setColor(this.menu.getColor());
        gui.blit(OVERLAY_TEXTURE,0,0,0,0,this.imageWidth,this.imageHeight);
        gui.resetColor();
        //Re-render the BG behind the item name
        Component name = this.menu.getTitle();
        int width = Math.min(gui.font.width(name) + 1,this.imageWidth - 10);
        gui.blit(GUI_TEXTURE,5,3,5,3,width,10);
        //Render the Items Name
        gui.drawString(TextRenderUtil.fitString(name,this.imageWidth - 10),6,4,0);
    }

}

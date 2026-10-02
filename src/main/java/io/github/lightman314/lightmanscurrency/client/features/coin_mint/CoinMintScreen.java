package io.github.lightman314.lightmanscurrency.client.features.coin_mint;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.FancyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CoinMintScreen extends FancyMenuScreen<CoinMintMenu> {

    public static final Identifier GUI_TEXTURE = LCApi.id("textures/gui/container/coinmint.png");
    public static final SizedSprite ARROW = new SizedSprite.Simple(LCApi.id("container/coin_mint/mint_progress"),24,16);

    public CoinMintScreen(CoinMintMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,176,138);
    }

    @Override
    protected void initialize(ScreenArea area) { }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        gui.blitBackground(GUI_TEXTURE,area);

        gui.blitSpriteFadeHoriz(ARROW,79,21,this.menu.getMintProgress());

    }

}

package io.github.lightman314.lightmanscurrency.client.features.wallet;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.FancyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.WidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.MultiIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.AbstractWalletMenu;
import io.github.lightman314.lightmanscurrency.features.wallet.menu.WalletMenu;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public class WalletScreen extends FancyMenuScreen<WalletMenu> {

    public static final Identifier BACKGROUND = LCApi.id("container/wallet/background");

    public static final IconData EXCHANGE_ICON = SpriteIcon.of(LCApi.id("icon/wallet_exchange"));
    public static final IconData AUTO_EXCHANGE_ICON_ON = SpriteIcon.of(LCApi.id("icon/wallet_auto_exchange"));
    public static final IconData AUTO_EXCHANGE_ICON_OFF = MultiIcon.of(AUTO_EXCHANGE_ICON_ON, ItemIcon.of(Items.BARRIER));

    private static final IconData FORCED_DEFAULT_OFF = ItemIcon.of(Items.DRAGON_HEAD);
    private static final IconData FORCED_DEFAULT_ON = MultiIcon.of(FORCED_DEFAULT_OFF,ItemIcon.of(Items.BARRIER));

    public WalletScreen(WalletMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title,176 + menu.bonusWidth,114 + menu.coinSlotHeight * 18);
        this.inventoryLabelX += menu.halfBonusWidth;
    }

    @Override
    protected void initialize(ScreenArea area) {

        WidgetPositioner positioner = this.addChild(WidgetPositioner.leftEdge(this,20));

        //Exchange Button
        IconButton exchangeButton = this.addChild(IconButton.builder()
                .onPress(() -> this.menu.exchangeCoins())
                .withIcon(EXCHANGE_ICON)
                .visible(this.menu::hasExchangeAbility)
                .tooltip(TooltipSource.simple(AbstractWalletMenu.TOOLTIP_WALLET_EXCHANGE))
                .build());

        //Auto Exchange Button
        IconButton autoExchangeButton = this.addChild(IconButton.builder()
                .onPress(this::toggleAutoExchange)
                .withIcon(this::autoExchangeIcon)
                .visible(this.menu::hasAutoExchangeAbility)
                .tooltip(TooltipSource.deferredSingle(this::autoExchangeTooltip))
                .build());

        IconButton buttonToggleDefaultSound = this.addChild(IconButton.builder()
                .onPress(this::toggleDefaultSound)
                .withIcon(this::getDefaultSoundIcon)
                .tooltip(TooltipSource.deferredSingle(this::getDefaultSoundTooltip))
                .visible(this.menu::canToggleSoundSettings)
                .build());

        //Bank Button
        IconButton bankButton = this.addChild(IconButton.builder()
                .onPress(this.menu::openBankMenu)
                .withIcon(ItemIcon.of(LCBlocks.COIN_PILE_GOLD))
                .visible(this.menu::hasBankAbility)
                .tooltip(TooltipSource.simple(AbstractWalletMenu.TOOLTIP_WALLET_OPEN_BANK))
                .build());

        positioner.addWidgets(exchangeButton,autoExchangeButton,buttonToggleDefaultSound,bankButton);

        //Quick Insert Button
        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(159 + this.menu.halfBonusWidth,area.height - 95))
                .onPress(this.menu::quickInsert)
                .withSprite(LCSprites.BUTTON_QUICK_INSERT)
                .build());

    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if(WalletClientEvents.KEY_WALLET.isActiveAndMatches(InputConstants.getKey(event))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        gui.blitSprite(BACKGROUND,area);
        //Slots
        gui.blitSlots(this.menu.slots);
    }

    private IconData autoExchangeIcon() { return this.menu.getAutoExchange() ? AUTO_EXCHANGE_ICON_ON : AUTO_EXCHANGE_ICON_OFF; }

    private Component autoExchangeTooltip() { return this.menu.getAutoExchange() ? AbstractWalletMenu.TOOLTIP_WALLET_AUTO_EXCHANGE_DISABLE.get() : AbstractWalletMenu.TOOLTIP_WALLET_AUTO_EXCHANGE_ENABLE.get(); }

    private void toggleAutoExchange() { this.menu.setAutoExchange(!this.menu.getAutoExchange()); }

    private IconData getDefaultSoundIcon() { return this.menu.getForceDefaultSound() ? FORCED_DEFAULT_ON : FORCED_DEFAULT_OFF; }

    private Component getDefaultSoundTooltip() { return this.menu.getForceDefaultSound() ? AbstractWalletMenu.TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_DISABLE.get() : AbstractWalletMenu.TOOLTIP_WALLET_FORCE_DEFAULT_SOUND_ENABLE.get(); }

    private void toggleDefaultSound() { this.menu.setForceDefaultSound(!this.menu.getForceDefaultSound()); }

}
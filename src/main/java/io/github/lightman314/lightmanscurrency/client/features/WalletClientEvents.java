package io.github.lightman314.lightmanscurrency.client.features;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.ClientEventListeners;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletSlot;
import io.github.lightman314.lightmanscurrency.mixin.client.SlotWrapperAccessor;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketSetWalletVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber
public final class WalletClientEvents {
    private WalletClientEvents() {}

    @SubscribeEvent
    private static void addVisibilityToggle(ScreenEvent.Init.Post event) {
        if(event.getScreen() instanceof AbstractContainerScreen<?> screen && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen))
        {
            boolean isCreative = screen instanceof CreativeModeInventoryScreen;
            //TODO don't add the visibility toggle if curios is installed
            Player player = Minecraft.getInstance().player;
            ScreenPosition walletPos = isCreative ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get();
            walletPos = ScreenHelper.offsetScreen(walletPos,screen);
            event.addListener(SpriteButton.builder()
                    .ofPos(walletPos.offset(18,0))
                    .withSprite(LCSprites.TOGGLE.buildSprite(() -> player.getData(LCDataAttachments.WALLET).isVisible()))
                    .visible(isCreative ? ClientEventListeners::isInventoryTabOpen : () -> true)
                    .onPress(() -> new CPacketSetWalletVisibility(player).send())
                    .build());
        }
    }

    @SubscribeEvent
    private static void renderWalletSlot(ScreenEvent.Render.Background event) {
        if(event.getScreen() instanceof AbstractContainerScreen<?> screen && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)) {
            AbstractContainerMenu menu = screen.getMenu();
            if(screen instanceof CreativeModeInventoryScreen creativeScreen && !ClientEventListeners.isInventoryTabOpen())
                return;
            Slot walletSlot = null;
            for(Slot slot : menu.slots)
            {
                if(slot instanceof WalletSlot)
                {
                    walletSlot = slot;
                    break;
                }
                if(slot instanceof SlotWrapperAccessor accessor && accessor.getTarget() instanceof WalletSlot)
                {
                    walletSlot = slot;
                    break;
                }
            }
            if(walletSlot != null)
            {
                FancyGuiExtractor gui = new FancyGuiExtractor(event.getGuiGraphics(),event.getMouseX(),event.getMouseY(), event.getPartialTick());
                gui.push(ScreenHelper.getScreenCorner(screen));
                gui.blitSlot(walletSlot);
            }
        }
    }

}
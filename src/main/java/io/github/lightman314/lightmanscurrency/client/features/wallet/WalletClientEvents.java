package io.github.lightman314.lightmanscurrency.client.features.wallet;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.DeferredSizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.WidgetContextSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.helpers.data.ItemContents;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.client.ClientEventListeners;
import io.github.lightman314.lightmanscurrency.client.features.wallet.gui.WalletGuiLayer;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.LCSounds;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletSlot;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import io.github.lightman314.lightmanscurrency.mixin.client.SlotWrapperAccessor;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWalletMenu;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketSetWalletVisibility;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

@EventBusSubscriber
public final class WalletClientEvents {
    private WalletClientEvents() {}

    private static final SizedSprite.Builder WALLET_SPRITE = WidgetContextSprite.hoverToggleSprite(LCApi.id("widget/open_wallet"),10,10);
    public static final SizedSprite.Template<BooleanSupplier> WALLET_VISIBILITY_SPRITE = DeferredSizedSprite.toggleSprite(LCApi.id("widget/wallet_visible"),LCApi.id("widget/wallet_hidden"),6,6);

    public static final KeyMapping KEY_WALLET = new KeyMapping(LCText.Resources.KEY_WALLET.getKey(),KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,KeyMapping.Category.INVENTORY);

    @SubscribeEvent
    private static void openWalletInput(ClientTickEvent.Post event) {
        boolean ignore = false;
        while(KEY_WALLET.consumeClick()) {
            //Only consume the action once, even if the button has been pressed multiple times
            if(ignore)
                continue;
            ignore = true;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if(player != null && mc.screen == null)
            {
                //Send the open wallet packet
                CPacketOpenWalletMenu.sendToServer();
                //Play the wallet sounds
                ItemStack wallet = LCApi.getCoinAPI().getEquippedWallet(player);
                if(!wallet.isEmpty())
                {
                    SoundManager sound = mc.getSoundManager();
                    sound.play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER.value(),1.25f + player.level().getRandom().nextFloat() * 0.5f,0.75f));
                    if(!wallet.getOrDefault(LCDataComponents.WALLET_CONTENTS,ItemContents.EMPTY).isEmpty())
                        sound.play(SimpleSoundInstance.forUI(LCSounds.COINS_CLINKING.get(),1f,0.4f));
                }
            }
        }
    }

    @SubscribeEvent
    private static void addWalletButtons(ScreenEvent.Init.Post event) {
        if(event.getScreen() instanceof AbstractContainerScreen<?> screen && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen))
        {
            Player player = Minecraft.getInstance().player;
            //Don't do anything if curios is installed
            if(LCCuriosHelper.get().hasWalletSlot(player))
                return;
            boolean isCreative = screen instanceof CreativeModeInventoryScreen;
            ScreenPosition walletPos = isCreative ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get();
            walletPos = ScreenHelper.offsetScreen(walletPos,screen);
            event.addListener(SpriteButton.builder()
                    .atPos(walletPos.offset(12,12))
                    .withSprite(WALLET_VISIBILITY_SPRITE.buildSprite(() -> player.getData(LCDataAttachments.WALLET).isVisible()))
                    .alwaysActive()
                    .visible(isButtonVisibleAndWalletPresent(isCreative,player))
                    .onPress(() -> new CPacketSetWalletVisibility(player).send())
                    .renderOnTop()
                    .build());

            event.addListener(SpriteButton.builder()
                    .atPos(walletPos.offset(LCConfig.CLIENT.walletButtonOffset.get()))
                    .withSprite(WALLET_SPRITE)
                    .onPress(CPacketOpenWalletMenu::sendToServer)
                    .alwaysActive()
                    .visible(isButtonVisibleAndWalletPresent(isCreative,player))
                    .build());
        }
    }

    private static BooleanSupplier isButtonVisible(boolean isCreative) { return isCreative ? ClientEventListeners::isInventoryTabOpen : () -> true;}

    private static BooleanSupplier isButtonVisibleAndWalletPresent(boolean isCreative,Player player) {
        BooleanSupplier normal = isButtonVisible(isCreative);
        return () -> normal.getAsBoolean() && !LCApi.getCoinAPI().getEquippedWallet(player).isEmpty();
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

    @SubscribeEvent
    private static void addKeyBindTooltip(RegisterTooltipAppendersEvent event) {
        event.registerAppender(TooltipLocation.HEAD,new KeybindTooltipAppender(WalletItem.class,WalletItem.TOOLTIP_WALLET_KEY_BIND,KEY_WALLET));
    }

    @SubscribeEvent
    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_WALLET);
    }

    @SubscribeEvent
    private static void addWalletLayer(EntityRenderersEvent.AddLayers event) {
        for(PlayerModelType type : PlayerModelType.values())
            addWalletLayer(event,type);
    }

    @SuppressWarnings("rawtypes")
    private static void addWalletLayer(EntityRenderersEvent.AddLayers event,PlayerModelType skin) {
        AvatarRenderer<? extends Player> renderer = event.getPlayerRenderer(skin);
        renderer.addLayer(new WalletLayer(renderer));
    }

    @SubscribeEvent
    private static void addWalletDataToState(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new WalletStateModifier());
    }

    @SubscribeEvent
    private static void registerWalletOverlay(RegisterGuiLayersEvent event) {
        event.registerBelowAll(LCApi.id("wallet"), WalletGuiLayer.INSTANCE);
    }

    private static final class WalletStateModifier extends AvatarRenderStateModifier {
        @Override
        public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
            if(avatar.hasData(LCDataAttachments.WALLET.get())) {
                ItemStackRenderState state = new ItemStackRenderState();
                //Get the visible wallet
                WalletAttachment data = avatar.getData(LCDataAttachments.WALLET);
                if(data.isVisible()) {
                    ItemStack wallet = data.getVisibleWallet().copy();
                    //Export the item stack state to the render state
                    renderState.setRenderData(WalletLayer.EQUIPPED_WALLET,WalletLayer.extractWalletState(wallet,avatar));
                }
                else
                    renderState.setRenderData(WalletLayer.EQUIPPED_WALLET,null);

            }
        }
    }

}
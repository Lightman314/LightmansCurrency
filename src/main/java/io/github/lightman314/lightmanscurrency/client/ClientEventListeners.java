package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.ILateRenderer;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRenderTick;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.features.atm.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.features.coin_mint.CoinMintScreen;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.ItemPositionSetManager;
import io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position.ItemPositionManager;
import io.github.lightman314.lightmanscurrency.client.features.wallet.WalletScreen;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.MoneyAPIImpl;
import io.github.lightman314.lightmanscurrency.mixin.client.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.util.function.BiFunction;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventListeners {

    @SubscribeEvent
    private static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event)
    {
        MoneyAPIImpl.INSTANCE.onPlayerLeave(event.getPlayer());
        SyncedConfigFile.onClientLeavesServer();
    }

    @SubscribeEvent
    private static void registerMenuScreens(RegisterMenuScreensEvent event)
    {
        //Machines
        event.register(LCMenuTypes.WALLET.get(),WalletScreen::new);
        event.register(LCMenuTypes.COIN_MINT.get(),CoinMintScreen::new);
        event.register(LCMenuTypes.ATM.get(),simpleFactory(ATMScreen::new));

        //Traders
        event.register(LCMenuTypes.TRADER_DIRECT.get(),simpleFactory(TraderCustomerScreen::new));
        event.register(LCMenuTypes.TRADER_BLOCK_ENTITY.get(),simpleFactory(TraderCustomerScreen::new));
        event.register(LCMenuTypes.TRADER_STORAGE.get(),simpleFactory(TraderStorageScreen::new));

    }

    private static <M extends AbstractContainerMenu,U extends Screen & MenuAccess<M>> MenuScreens.ScreenConstructor<M,U> simpleFactory(BiFunction<M,Inventory,U> factory) {
        return (menu,inv,title) -> factory.apply(menu,inv);
    }

    @SubscribeEvent
    private static void triggerInventoryRenderTick(ScreenEvent.Render.Pre event) {
        if(event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen)
        {
            ScreenPosition mousePos = ScreenPosition.of(event.getMouseX(),event.getMouseY());
            for(Renderable r : event.getScreen().renderables)
            {
                if(r instanceof IRenderTick t)
                    t.renderTick(mousePos);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void triggerLateRenderWidgets(ContainerScreenEvent.Render.Foreground event) {
        //Use forground render event to render above slots, but before the tooltip and carried item (hopefully)
        if(event.getContainerScreen() instanceof InventoryScreen || event.getContainerScreen() instanceof CreativeModeInventoryScreen) {
            FancyGuiExtractor gui = new FancyGuiExtractor(event.getGuiGraphics(),event.getMouseX(),event.getMouseY(),0f);
            for(Renderable r : event.getContainerScreen().renderables) {
                if(r instanceof ILateRenderer lr)
                    lr.extractLateRender(gui);
            }
        }
    }

    @SubscribeEvent
    private static void addInventoryWidgets(ScreenEvent.Init.Post event) {
        if(event.getScreen() instanceof AbstractContainerScreen<?> screen && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen))
        {
            boolean isCreative = screen instanceof CreativeModeInventoryScreen;

            //TODO Team/Ejection/Notification buttons

        }
    }

    public static boolean isInventoryTabOpen() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(CreativeModeInventoryScreenAccessor.getSelectedTab()) == CreativeModeTabs.INVENTORY.identifier();
    }

    @SubscribeEvent
    private static void registerResourceListeners(AddClientReloadListenersEvent event) {
        event.addListener(LCApi.id("item_position_data"),ItemPositionManager.INSTANCE);
        event.addListener(LCApi.id("item_position_sets"),ItemPositionSetManager.INSTANCE);
    }

}

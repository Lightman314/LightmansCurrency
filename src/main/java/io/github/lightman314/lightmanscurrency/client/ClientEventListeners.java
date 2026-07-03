package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.helpers.TooltipHelper;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.features.api_impl.MoneyAPIImpl;
import io.github.lightman314.lightmanscurrency.mixin.client.CreativeModeInventoryScreenAccessor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventListeners {

    @SubscribeEvent
    private static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event)
    {
        MoneyAPIImpl.INSTANCE.onPlayerLeave(event.getPlayer());
    }

    @SubscribeEvent
    private static void registerMenuScreens(RegisterMenuScreensEvent event)
    {
        event.register(LCMenuTypes.TRADER_STORAGE.get(),simpleFactory(TraderStorageScreen::new));
    }

    private static <M extends AbstractContainerMenu,U extends Screen & MenuAccess<M>> MenuScreens.ScreenConstructor<M,U> simpleFactory(BiFunction<M,Inventory,U> factory) {
        return (menu,inv,title) -> factory.apply(menu,inv);
    }

    @SubscribeEvent
    private static void addItemTooltips(ItemTooltipEvent event)
    {
        //Collect data for easier use
        List<Component> tooltips = event.getToolTip();
        ItemStack item = event.getItemStack();
        Item.TooltipContext context = event.getContext();
        TooltipFlag flag = event.getFlags();

        //Add all LC TooltipProviders to the items tooltips
        Consumer<Component> builder = TooltipHelper.endOfTooltipBuilder(tooltips);
        for(var type : LCDataComponents.REGISTER.getEntries())
        {
            Object value = item.get(type);
            if(value instanceof TooltipProvider tp)
                tp.addToTooltip(context,builder,flag,item);
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
        return CreativeModeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.INVENTORY;
    }

    @SubscribeEvent
    private static void loadClientConfigs(ClientPlayerNetworkEvent.LoggingIn event) {
        ConfigFile.loadClientFiles(ConfigFile.LoadPhase.GAME_START);
    }

}

package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.event.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStorageMenuTabProvider;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.MenuTrackingManager;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.SimpleTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;

public class TraderStorageMenu extends TabbedMenu.Validated<TraderStorageMenu,TraderStorageTab> implements INodeAccess {

    public static final int SLOT_OFFSET = AbstractTabbedCustomerMenu.SLOT_OFFSET;
    public static final int INVENTORY_SLOTS = 9 * 4;

    public static final TextEntry TOOLTIP_TRADER_OPEN_TRADES = TextEntry.tooltip(LCApi.MODID,"trader.open_trades");

    private final Map<Identifier,Integer> keyToSlotMap;
    private final long traderID;
    @Nullable
    @Override
    public TraderData getTrader() { return LCApi.getTraderAPI().getTrader(this,this.traderID); }
    public boolean traderValid() {
        TraderData trader = this.getTrader();
        return trader != null && trader.getPermission(this.getPlayer(),BuiltInPermissions.OPEN_STORAGE);
    }

    private final MenuTrackingManager trackingManager;

    public TraderStorageMenu(int containerId,Player player,long traderID,MenuValidator validator) {
        this.traderID = traderID;
        this.keyToSlotMap = new HashMap<>();
        super(LCMenuTypes.TRADER_STORAGE.get(),containerId,player,validator);
        this.addCheck(this::traderValid,this::openCustomerMenu);
        this.trackingManager = new MenuTrackingManager(TraderSource.deferred(this::getTrader),player,TrackingLevel.STORAGE);
    }

    @Override
    protected void addInventorySlots(Inventory inventory) {
        //Add Inventory Slots
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
        }
    }

    @Override
    protected int calculateDefaultTab() {
        //Pick the first default tab key
        //We can just iterate over the list here, I don't see a need to cache this in the builder or anything since this is only calculated once
        for(var entry : this.getTabs().entrySet())
        {
            if(entry.getValue().isDefaultTab())
                return entry.getKey();
        }
        return super.calculateDefaultTab();
    }

    public final void changeTab(Identifier tabKey) { this.changeTab(tabKey,FancyPacketMap.EMPTY); }
    public final void changeTab(Identifier tabKey,FancyPacketMap packet) {
        if(this.keyToSlotMap.containsKey(tabKey))
            this.changeTab(this.keyToSlotMap.get(tabKey),packet);
    }

    @Override
    protected void collectTabs(TabBuilder<TraderStorageMenu,TraderStorageTab> builder) {
        TraderData trader = this.getTrader();
        this.keyToSlotMap.clear();
        MenuTabBuilder b = new MenuTabBuilder(this,this.keyToSlotMap,builder);
        if(trader == null)
            b.addTab(SimpleTradeEditTab::new);
        else
        {
            //Post tabs event
            NeoForge.EVENT_BUS.post(new TraderEvent.StorageMenuTabsEvent.Pre(trader,b));
            //Collect from the nodes
            for(IStorageMenuTabProvider node : trader.getNodes(IStorageMenuTabProvider.class))
                node.addTabs(b);
            //Post tabs event again
            NeoForge.EVENT_BUS.post(new TraderEvent.StorageMenuTabsEvent.Post(trader,b));
        }

    }

    public final TradeContext.Builder getTradeContext() {
        TraderData trader = this.getTrader();
        if(trader == null)
            return TradeContext.builder(new TraderData(LCRegistries.Trader.TRADER_TYPES.getValue(LCApi.id("null"))));
        return TradeContext.builder(trader);
    }

    public void openCustomerMenu() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("openCustomer"));
            return;
        }
        TraderData trader = this.getTrader();
        if(trader != null)
            trader.openCustomerMenu(this.getPlayer(),this.getValidator(),false);
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        super.handleMessage(message);
        if(message.contains("openCustomer"))
            this.openCustomerMenu();
    }

    public static MenuProvider getProvider(TraderData trader,MenuValidator validator,boolean mouseUpdate) {
        return new FancyMenuProvider((id,inv,player) -> new TraderStorageMenu(id,player,trader.getID(),validator),
                mouseUpdate,
                buffer -> {
                    buffer.writeLong(trader.getID());MenuValidator.STREAM_CODEC.encode(buffer,validator);
        });
    }

    @Override
    @Nullable
    public <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getNode(type);
        return null;
    }

    @Override
    public List<TraderNode> getAllNodes() {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getAllNodes();
        return ImmutableList.of();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void serverTick() {
        super.serverTick();
        this.trackingManager.tick();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void removed(Player player) {
        super.removed(player);
        this.trackingManager.onClose();
    }

    private static class MenuTabBuilder implements StorageTabBuilder
    {

        private final TraderStorageMenu menu;
        private final Map<Identifier,Integer> keyToSlotMap;
        private final TabBuilder<TraderStorageMenu,TraderStorageTab> builder;

        private MenuTabBuilder(TraderStorageMenu menu,Map<Identifier,Integer> keyToSlotMap,TabBuilder<TraderStorageMenu,TraderStorageTab> builder) {
            this.menu = menu;
            this.keyToSlotMap = keyToSlotMap;
            this.builder = builder;
        }

        @Override
        public TraderStorageMenu menu() { return this.menu; }

        @Override
        public void addTab(TraderStorageTab tab) {
            //Add to the normal builder
            this.builder.setTab(tab.getTabSlot(),tab);
            //Cache the tab slot for easier future lookup
            this.keyToSlotMap.put(tab.getKey(),tab.getTabSlot());
        }

        @Override
        public void removeTab(Identifier tabKey) {
            if(this.keyToSlotMap.containsKey(tabKey))
            {
                //Remove the tab from the normal builder
                this.builder.removeTab(this.keyToSlotMap.get(tabKey));
                //Remove from the key map
                this.keyToSlotMap.remove(tabKey);
            }
        }

        @Override
        public boolean hasTab(Identifier tabKey) { return this.keyToSlotMap.containsKey(tabKey); }

    }

}
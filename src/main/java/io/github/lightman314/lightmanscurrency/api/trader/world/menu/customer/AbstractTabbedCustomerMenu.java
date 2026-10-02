package io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.event.CustomerMenuTabsEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ICustomerMenuProvider;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.MenuTrackingManager;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeIndexes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.builtin.NormalCustomerTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.provider.FancyMenuProvider;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabBuilder;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.TabbedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;


public abstract class AbstractTabbedCustomerMenu extends TabbedMenu.Validated<AbstractTabbedCustomerMenu, TraderCustomerTab> implements TraderCustomerMenu {

    public static final int SLOT_OFFSET = 17;

    public static final Identifier MENU_KEY = LCApi.id("trader_customer");

    private final TraderSource source;
    @Override
    public final TraderSource getTraderSource() { return this.source; }
    @Override
    public TradeCustomer getCustomer() { return TradeCustomer.of(this.getPlayer()); }
    @Override
    public RandomSource getRandom() { return this.getPlayer().getRandom(); }

    private boolean isSourceValid() { return !this.source.getTraders().isEmpty(); }

    @Override
    public Identifier getMenuKey() { return MENU_KEY; }

    private final MenuTrackingManager trackingManager;

    protected AbstractTabbedCustomerMenu(@Nullable MenuType<?> menuType, int containerId, Player player, MenuValidator validator, TraderSource source) {
        this.source = source;
        super(menuType,containerId, player,validator);
        this.addCheck(this::isSourceValid);
        this.trackingManager = new MenuTrackingManager(this.source,player,TrackingLevel.CUSTOMER);
    }

    @Override
    protected void addInventorySlots(Inventory inventory) {
        //Player items
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory,x + y * 9 + 9,SLOT_OFFSET + 8 + x * 18,154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inventory,x,SLOT_OFFSET + 8 + x * 18,212));
        }
    }

    @Override
    protected void collectTabs(TabBuilder<AbstractTabbedCustomerMenu,TraderCustomerTab> builder) {
        //Add normal tab
        builder.addTab(new NormalCustomerTab(this));
        //Run pre-tabs event
        NeoForge.EVENT_BUS.post(new CustomerMenuTabsEvent.Pre(builder,this));
        for(TraderData trader : this.getTraders()) {
            for(ICustomerMenuProvider node : trader.getNodes(ICustomerMenuProvider.class)) {
                node.addCustomerTabs(builder,this);
            }
        }
        //Run post tabs event
        NeoForge.EVENT_BUS.post(new CustomerMenuTabsEvent.Post(builder,this));
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

    @Override
    public void quickCollectMoney() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("CollectMoney"));
            return;
        }
        if(!this.canQuickCollectMoney())
            return;
        TraderData trader = this.getSimpleTrader();
        if(trader != null && trader.hasNode(MoneyStorageNode.TYPE))
        {
            MoneyResourceHandler storage = trader.getNodeValue(MoneyStorageNode.TYPE,MoneyStorageNode::getStorage, EmptyMoneyResource.INSTANCE);
            MoneyResourceHandler playersMoney = LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer());
            try(Transaction transaction = Transaction.openRoot()) {
                boolean success = false;
                for(MoneyValue storedMoney : storage.getAllResources()) {
                    try(Transaction tx = Transaction.open(transaction)) {
                        MoneyValue inserted = playersMoney.insert(storedMoney,tx);
                        //If the money couldn't be inserted, don't bother continuing for this
                        if(inserted.isEmpty())
                            continue;
                        MoneyValue extracted = storage.extract(inserted,tx);
                        if(inserted.equals(extracted))
                        {
                            tx.commit();
                            success = true;
                        }
                    }
                }
                if(success) //Only commit if at least one of the collections actually succeeded
                    transaction.commit();
            }
        }
    }

    @Override
    public void openStorage() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("OpenStorage"));
            return;
        }
        if(!this.canOpenStorage())
            return;
        TraderData trader = this.getSimpleTrader();
        if(trader != null)
            trader.openStorageMenu(this.getPlayer(),this.getValidator(),false);
    }

    @Override
    public void openTerminal() {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.flag("OpenTerminal"));
            return;
        }
        //Ignore if we weren't actually accessed through the terminal
        if(!this.getValidator().isNetworkAccess())
            return;
        //Open the Terminal Menu
        this.getPlayer().openMenu(TradingTerminalMenu.createProvider(this.getValidator()));
    }

    @Override
    public TradeContext.Builder buildContext(TradeContext.Builder builder) {
        //Tab resources first as they may wish to make some things accessible before the normal player inventory
        this.appendTabResources(builder);
        this.appendPlayerResources(builder);
        return builder;
    }

    protected final void appendTabResources(TradeContext.Builder builder) {
        for(TraderCustomerTab tab : this.getTabs().values())
            tab.buildContext(builder);
    }

    protected final void appendPlayerResources(TradeContext.Builder builder) {
        //Give access to the players personal money
        builder.withResource(BuiltInResourceTypes.MONEY,LCApi.getMoneyAPI().getPlayersMoneyHandler(this.getPlayer()))
                //Give access to the players inventory for item sales
                .withResource(BuiltInResourceTypes.ITEM,PlayerInventoryWrapper.of(this.getPlayer()).getMainSlots());
    }

    @Override
    public void attemptTrade(TradeIndexes location)
    {
        if(this.isClient())
        {
            this.sendToServer(FancyPacketMap.map()
                    .setList("AttemptTrade",LCFancyPacketTypes.INT,location.asData()));
            return;
        }
        TraderData t = location.getTrader(this.source);
        if(t == null)
            return;
        TradeResult result = t.attemptTrade(this.getContext(t),location.nodeIndex(),location.tradeIndex());
        if(result.isFailure()) //Log the failure
            LightmansCurrency.LogDebug("Trade Failed: " + result.getMessage().getString());
    }

    @Override
    public void handleMessage(FancyPacketMap message) {
        super.handleMessage(message);
        if(message.contains("CollectMoney"))
            this.quickCollectMoney();
        if(message.contains("AttemptTrade"))
        {
            TradeIndexes location = TradeIndexes.fromData(message.getList("AttemptTrade",LCFancyPacketTypes.INT));
            if(location != null)
                this.attemptTrade(location);
        }
        if(message.contains("OpenStorage"))
            this.openStorage();
        if(message.contains("OpenTerminal"))
            this.openTerminal();
    }

    public static MenuProvider directProvider(long traderID,MenuValidator validator,boolean mouseUpdate) {
        return new FancyMenuProvider((id,inv,player) -> new Direct(id,player,traderID,validator),
                mouseUpdate,
                buf -> {
                    buf.writeLong(traderID);
                    MenuValidator.STREAM_CODEC.encode(buf,validator);
        });
    }

    public static MenuProvider blockEntityProvider(BlockPos blockPos) {
        return new FancyMenuProvider((id,inv,player) -> new Block(id,inv.player,blockPos),
                buf -> buf.writeBlockPos(blockPos));
    }

    public static MenuProvider allNetworkTraderProvider(MenuValidator validator, boolean mouseUpdate) {
        return new FancyMenuProvider((id,inv,player) -> new AllNetworkTraders(id,player,validator),
                mouseUpdate,
                buf -> MenuValidator.STREAM_CODEC.encode(buf,validator));
    }

    public static class Direct extends AbstractTabbedCustomerMenu {

        public Direct(int containerId,Player player,long traderID,MenuValidator validator) {
            super(LCMenuTypes.TRADER_DIRECT.get(),containerId,player,validator,TraderSource.direct(player,traderID));
        }

    }

    public static class Block extends AbstractTabbedCustomerMenu {

        public Block(int containerId,Player player,BlockPos blockPos) {
            BlockEntity be = player.level().getBlockEntity(blockPos);
            MenuValidator validator = be == null ? new SimpleValidator(p -> false) : new BlockEntityValidator(be);
            super(LCMenuTypes.TRADER_DIRECT.get(),containerId,player,validator,TraderSource.blockEntity(player.level(),blockPos));
        }

    }

    public static class AllNetworkTraders extends AbstractTabbedCustomerMenu {
        public AllNetworkTraders(int containerID,Player player,MenuValidator validator) {
            super(LCMenuTypes.TRADER_ALL_NETWORK.get(),containerID,player,validator,new TraderSource.AllNetworkTraders(player));
        }
    }

}

package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.notifications.BankInteractionNotification;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.UnlimitedMoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.values.ItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.*;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceCollector;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.StorageTabBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin.MoneyStorageTab;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MoneyStorageNode extends SimpleSyncedNode implements IPermissionUser, ITradeResourceProvider, IStorageMenuTabProvider, ITraderDestructionListener {

    private static final MapCodec<MoneyStorageNode> MAP_CODEC = MoneyValue.NON_EMPTY_OR_FREE_CODEC.listOf().fieldOf("storage")
            .xmap(MoneyStorageNode::new,n -> n.getStorage().getAllResources());
    public static final TraderNodeType<MoneyStorageNode> TYPE = TraderNodeType.simple(MoneyStorageNode::new,MAP_CODEC);

    private final UnlimitedMoneyStorage storage = new UnlimitedMoneyStorage().withListener(this::onStorageChanged);
    public UnlimitedMoneyStorage getStorage() { return this.storage; }
    private MoneyStorageNode() { }
    private MoneyStorageNode(List<MoneyValue> storage) { this.storage.copyFrom(storage); }

    private void onStorageChanged() {
        this.setChanged(builder -> builder.setList("storage",LCFancyPacketTypes.MONEY,this.storage.getAllResources()));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setList("storage",LCFancyPacketTypes.MONEY,this.storage.getAllResources());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("storage"))
            this.storage.copyFrom(data.getList("storage",LCFancyPacketTypes.MONEY));
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.COLLECT_MONEY);
        handler.accept(LCPermissions.STORE_MONEY);
    }

    @Override
    public void attachResource(ResourceCollector collector) {
        collector.addResource(BuiltInResourceTypes.MONEY,SortableMoneyResourceHandler.wrapHandler(this.storage,Component.literal("Internal Storage (WIP)")));
    }

    @Override
    public void addTabs(StorageTabBuilder builder) {
        builder.addTab(MoneyStorageTab::new);
    }

    @Override
    public void onTraderDestroyed(IOwnerHolder owner, Consumer<ItemStack> itemSpawner, Optional<MoneyResourceHandler> playerMoney) {
        MoneyResourceHandler playerMoneyConsumer = null;
        BankAccount bank = null;
        Component traderName = IDisplayNode.getTraderName(this.getTrader());
        if(playerMoney.isPresent())
            playerMoneyConsumer = playerMoney.get();
        else {
            BankReference br = owner.getValidOwner().asBankReference();
            if(br != null)
                bank = br.get();
        }
        try(Transaction transaction = Transaction.openRoot()) {
            for(MoneyValue money : this.storage.getAllResources()) {
                //First attempt to give to the player money handler
                if(playerMoneyConsumer != null)
                    playerMoneyConsumer.insert(money,transaction);
                //Otherwise spawn as an item (if possible)
                else if(money instanceof ItemBasedValue itemValue) {
                    for(ItemStack i : itemValue.getAsSeperatedItemList())
                        itemSpawner.accept(i);
                } //If not possible to spawn as an item, give to their bank account as a last resort
                else if(bank != null) {
                    MoneyValue inserted = bank.insert(money,transaction);
                    if(!inserted.isEmpty()) //Log the bank account deposit
                        bank.pushLocalNotification(BankInteractionNotification.forMachine(bank.getName(),true,inserted,traderName));
                }
            }
            transaction.commit();
        }

    }
}
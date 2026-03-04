package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxableContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IBankListener;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IContentProvider;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.TraderMoneyStorageTab;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MoneyStorageNode extends SyncedTraderNode implements IBankListener, IContentProvider {

    private static final MapCodec<MoneyStorageNode> MAP_CODEC = MoneyStorage.CODEC.fieldOf("storage").xmap(MoneyStorageNode::new,MoneyStorageNode::getStorage);

    public static final TraderNodeType<MoneyStorageNode> TYPE = TraderNodeType.simple(MoneyStorageNode::new,MAP_CODEC);

    private final MoneyStorage storage;
    public MoneyStorage getStorage() { return this.storage; }

    private MoneyStorageNode() { this(new MoneyStorage()); }
    private MoneyStorageNode(MoneyStorage storage) {
        this.storage = storage.withListener(this::setStorageChanged);
    }

    private void setStorageChanged() {
        this.setChanged(builder -> builder.setList("storage",this.storage.allValues(),ModLazyPackets.MONEY_VALUE));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    public MoneyValue addStoredMoney(MoneyValue amount, @Nullable ITaxableContext context) {
        if(amount == null)
            return MoneyValue.empty();
        MoneyValue taxesPaid = MoneyValue.empty();
        if(context != null)
        {
            taxesPaid = this.trader.payTaxesOn(amount,context);
            if(!amount.containsValue(taxesPaid))
            {
                //Remove excess money
                //Will add warning for owner if tax percent is somehow greater than 100%
                //May also lock the trader from interactions until they can report the issue to the relevant parties
                this.removeStoredMoney(taxesPaid.subtractValue(amount), null);
                return taxesPaid;
            }
            else
            {
                amount = amount.subtractValue(taxesPaid);
                if(amount.isEmpty())
                    return taxesPaid;
            }
        }
        BankNode node = this.trader.getNode(BankNode.TYPE);
        if(node != null)
        {
            IBankAccount ba = node.getBankAccount();
            if(ba != null)
            {
                ba.depositMoney(amount);
                ba.pushLocalNotification(new DepositWithdrawNotification.Custom(this.trader.getName(),ba.getName(),true,amount));
                return taxesPaid;
            }
        }
        this.storage.addValue(amount);
        return taxesPaid;
    }

    public MoneyValue removeStoredMoney(MoneyValue amount, @Nullable ITaxableContext context) {
        MoneyValue taxesPaid = MoneyValue.empty();
        if(context != null)
        {
            //Then pay taxes
            taxesPaid = this.trader.payTaxesOn(amount,context);
            if(!taxesPaid.isEmpty())
                amount = amount.addValue(taxesPaid);
        }
        BankNode node = this.trader.getNode(BankNode.TYPE);
        if(node != null)
        {
            IBankAccount ba = node.getBankAccount();
            if(ba != null) {
                ba.withdrawMoney(amount);
                ba.pushLocalNotification(new DepositWithdrawNotification.Custom(this.trader.getName(),ba.getName(),false,amount));
                return taxesPaid;
            }
        }
        this.storage.removeValue(amount);
        return taxesPaid;
    }

    public void collectStoredMoney(Player player)
    {
        if(this.hasPermission(player,Permissions.COLLECT_COINS))
        {
            if(this.storage.isEmpty())
                return;
            this.storage.GiveToPlayer(player);
        }
        else
            Permissions.PermissionWarning(player, "collect stored coins", Permissions.COLLECT_COINS);
    }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setList("storage",this.storage.allValues(),ModLazyPackets.MONEY_VALUE);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("storage"))
            this.storage.load(data.getList("storage",ModLazyPackets.MONEY_VALUE));
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("StoredMoney"))
            this.storage.load(tag.getList("StoredMoney", Tag.TAG_COMPOUND));
    }

    @Override
    public void afterBankLink(IBankAccount account) {
        for(MoneyValue value : this.storage.allValues())
            account.depositMoney(value);
        this.storage.clear();
    }

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> results = new ArrayList<>();
        //Add stored money
        for(MoneyValue value : this.storage.allValues())
        {
            List<ItemStack> items = value.onBlockBroken(this.trader.getOwner());
            if(items != null)
                results.addAll(items);
        }
        return results;
    }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new TraderMoneyStorageTab(menu));
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.COLLECT_COINS, 0);
        defaultConsumer.accept(Permissions.STORE_COINS, 0);
    }

}

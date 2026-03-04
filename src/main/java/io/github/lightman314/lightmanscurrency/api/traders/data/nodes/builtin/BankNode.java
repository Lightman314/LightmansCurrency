package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.BankSettings;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IBankListener;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IOwnerListener;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BankNode extends SyncedTraderNode implements IOwnerListener {

    private static final MapCodec<BankNode> MAP_CODEC = Codec.BOOL.fieldOf("linked").xmap(BankNode::new,BankNode::isLinkedToBank);

    public static final TraderNodeType<BankNode> TYPE = TraderNodeType.simple(BankNode::new,MAP_CODEC);

    private BankNode() {}
    private BankNode(boolean linked) {}

    public static boolean isLinkedToBankAccount(TraderData trader)
    {
        BankNode node = trader.getNode(TYPE);
        return node != null && node.isLinkedToBank();
    }

    private boolean linkedToBank = false;
    public boolean isLinkedToBank() { return this.linkedToBank; }
    public boolean setLinkedToBank(@Nullable PlayerReference admin, boolean newValue)
    {
        if(newValue == this.linkedToBank)
            return false;
        this.linkedToBank = newValue;
        if(this.linkedToBank)
        {
            if(this.trader.isInQuarantine())
                this.linkedToBank = false;
            else
            {
                IBankAccount account = this.getBankAccount();
                if(account != null)
                {
                    for(TraderNode node : this.trader.getNodeIterable())
                    {
                        if(node instanceof IBankListener listener)
                            listener.afterBankLink(account);
                    }
                    this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_BANK_LINK.get(),this.linkedToBank));
                    this.setChanged(builder -> builder.setBoolean("linked",this.linkedToBank));
                    return true;
                }
                else
                    this.linkedToBank = false;
            }
            return false;
        }
        else
        {
            for(TraderNode node : this.trader.getNodeIterable())
            {
                if(node instanceof IBankListener listener)
                    listener.afterBankUnlink();
            }
            this.setChanged(builder -> builder.setBoolean("linked",this.linkedToBank));
            this.pushLocalNotification(ChangeSettingNotification.simple(admin, LCText.DATA_ENTRY_TRADER_BANK_LINK.get(),this.linkedToBank));
        }
        return true;
    }
    public boolean canLinkBankAccount()
    {
        if(this.trader.isInQuarantine())
            return false;
        BankReference reference = this.trader.getOwner().getValidOwner().asBankReference();
        return reference != null && reference.get() != null;
    }

    public boolean hasBankAccount() { return this.getBankAccount() != null; }
    @Nullable
    public BankReference getBankReference() {
        if(this.linkedToBank && !this.trader.isInQuarantine())
            return this.trader.getOwner().getValidOwner().asBankReference();
        return null;
    }
    @Nullable
    public IBankAccount getBankAccount() {
        BankReference reference = this.getBankReference();
        if(reference != null)
            return reference.get();
        return null;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setBoolean("linked",this.linkedToBank);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("linked"))
            this.linkedToBank = data.getBoolean("linked");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Bank Settings
        if(tag.contains("LinkedToBank"))
            this.linkedToBank = tag.getBoolean("LinkedToBank");
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("LinkToBankAccount"))
        {
            if(this.hasPermission(player, Permissions.BANK_LINK))
            {
                boolean newValue = message.getBoolean("LinkToBankAccount");
                if(newValue != this.linkedToBank)
                    this.setLinkedToBank(PlayerReference.of(player),newValue);
            }
        }
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new BankSettings(trader,this));
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.BANK_LINK, 0);
    }

    @Override
    public void onOwnerChanged() {
        //Force the bank link off when the owner is changed
        this.setLinkedToBank(null,false);
    }
}

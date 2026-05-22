package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.CreativeSettings;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IBaseRuleModifier;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITerminalDisplay;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeCreativeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AdminNode extends SyncedTraderNode implements IBaseRuleModifier, ITerminalDisplay {

    private static final MapCodec<AdminNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("creative").forGetter(AdminNode::isCreative),
            Codec.BOOL.fieldOf("storeMoney").forGetter(AdminNode::shouldStoreCreativeMoney)
    ).apply(builder,AdminNode::new));

    public static final TraderNodeType<AdminNode> TYPE = TraderNodeType.simple(AdminNode::new,MAP_CODEC);

    private boolean creative = false;
    public boolean isCreative() { return this.creative; }
    public void setCreative(boolean creative)
    {
        if(this.creative == creative)
            return;
        this.creative = creative;
        this.setChanged(builder -> builder.setBoolean("creative",this.creative));
    }

    private boolean storeCreativeMoney = false;
    public boolean shouldStoreCreativeMoney() { return this.storeCreativeMoney; }
    public void setStoreCreativeMoney(boolean storeCreativeMoney)
    {
        if(this.storeCreativeMoney == storeCreativeMoney)
            return;
        this.storeCreativeMoney = storeCreativeMoney;
        this.setChanged(builder -> builder.setBoolean("storeMoney",this.storeCreativeMoney));
    }

    private AdminNode() { }
    private AdminNode(boolean creative,boolean storeCreativeMoney) {
        this.creative = creative;
        this.storeCreativeMoney = storeCreativeMoney;
    }

    @Override
    public void hasInfiniteStock(AtomicBoolean result) {
        if(this.creative)
            result.set(true);
    }

    @Override
    public void shouldStoreMoney(AtomicBoolean result) {
        if(this.creative && !this.storeCreativeMoney)
            result.set(false);
    }

    @Override
    public void showOwnerInTitle(AtomicBoolean result) {
        if(this.creative)
            result.set(false);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setBoolean("creative",this.creative)
                .setBoolean("storeMoney",this.storeCreativeMoney);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("creative"))
            this.creative = data.getBoolean("creative");
        if(data.contains("storeMoney"))
            this.storeCreativeMoney = data.getBoolean("storeMoney");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Creative"))
            this.creative = tag.getBoolean("Creative");
        if(tag.contains("StoreCreativeMoney"))
            this.storeCreativeMoney = tag.getBoolean("StoreCreativeMoney");
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("MakeCreative"))
        {
            if(LCAdminMode.isAdminPlayer(player))
            {
                boolean newValue = message.getBoolean("MakeCreative");
                if(newValue != this.creative)
                {
                    this.creative = newValue;
                    this.pushLocalNotification(new ChangeCreativeNotification(PlayerReference.of(player),this.creative));
                }
            }
        }
        if(message.contains("StoreCreativeMoney"))
        {
            if(LCAdminMode.isAdminPlayer(player))
            {
                boolean newValue = message.getBoolean("StoreCreativeMoney");
                if(newValue != this.storeCreativeMoney)
                {
                    this.storeCreativeMoney = newValue;
                    this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), LCText.DATA_ENTRY_STORE_CREATIVE_MONEY.get(),this.storeCreativeMoney));
                }
            }
        }
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new CreativeSettings(trader,this));
    }

}

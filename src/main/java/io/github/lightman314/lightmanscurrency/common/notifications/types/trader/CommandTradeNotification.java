package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.traders.commands.trade.CommandTrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class CommandTradeNotification extends SingleLineTaxableNotification {

    public static final NotificationType<CommandTradeNotification> TYPE = new Type();

    TraderCategory traderData = TraderCategory.getEmpty();
    String command = "";
    MoneyValue cost = MoneyValue.empty();

    String customer = "";

    private CommandTradeNotification() {}
    private CommandTradeNotification(TraderCategory trader, String command, MoneyValue cost, String customer, MoneyValue taxesPaid, CommonData data) {
        super(taxesPaid,data);
        this.traderData = trader;
        this.command = command;
        this.cost = cost;
        this.customer = customer;
    }
    private CommandTradeNotification(CommandTrade trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid)
    {
        super(taxesPaid);
        this.traderData = traderData;
        this.command = trade.getCommand();
        this.cost = cost;
        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(CommandTrade trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) { return () -> new CommandTradeNotification(trade,cost,customer,traderData,taxesPaid); }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    protected Component getNormalMessage() { return LCText.NOTIFICATION_TRADE_COMMAND.get(this.customer,this.cost.getText("NULL"),this.command); }

    @Override
    protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {

        this.traderData = TraderCategory.loadOldData(compound.getCompound("TraderInfo"),lookup);
        this.command = compound.getString("Command");
        this.cost = MoneyValue.load(compound.getCompound("Price"));
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof CommandTradeNotification ctn)
        {
            if(!ctn.traderData.matches(this.traderData))
                return false;
            if(!ctn.command.equals(this.command))
                return false;
            if(!ctn.cost.equals(this.cost))
                return false;
            if(!ctn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return this.TaxesMatch(ctn);
        }
        return false;
    }

    private static class Type extends NotificationType<CommandTradeNotification>
    {
        private static final MapCodec<CommandTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().codec().fieldOf("trader").forGetter(n -> n.traderData),
                Codec.STRING.fieldOf("command").forGetter(n -> n.command),
                MoneyValue.CODEC.fieldOf("cost").forGetter(n -> n.cost),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer)
        ).and(taxableFields(builder)).apply(builder,CommandTradeNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,CommandTradeNotification> STREAM_CODEC = StreamHelper.combine(taxableStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                ByteBufCodecs.STRING_UTF8,n -> n.command,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                CommandTradeNotification::new);

        @Override
        protected CommandTradeNotification createNew() { return new CommandTradeNotification(); }
        @Override
        public MapCodec<CommandTradeNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CommandTradeNotification> streamCodec() { return STREAM_CODEC; }
    }

}

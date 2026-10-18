package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReferenceList;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.MultiIcon;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCIconTypes;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class PlayerDiscounts extends PriceModifyingTradeRule implements IPersistentRule {

    private static final MapCodec<PlayerDiscounts> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            PlayerReferenceList.CODEC.fieldOf("players").forGetter(r -> r.playerList),
            Codec.intRange(1,100).fieldOf("discount").forGetter(PlayerDiscounts::getDiscount)
    ).apply(builder,PlayerDiscounts::new));

    public static final TradeRuleType<PlayerDiscounts> TYPE = new TradeRuleType<>(MAP_CODEC,PlayerDiscounts::new);

    public static final TextEntry INFO_SALE = TextEntry.tradeRuleMessage(TYPE,"info.sale");
    public static final TextEntry INFO_PURCHASE = TextEntry.tradeRuleMessage(TYPE,"info.purchase");

    public static final TextEntry GUI_LABEL = TextEntry.tradeRuleGui(TYPE,"label");

    private final PlayerReferenceList playerList = new PlayerReferenceList().withListener(this::setChanged);
    public List<PlayerReference> getPlayerList() { return this.playerList.getPlayers(); }

    private int discount = 10;
    public int getDiscount() { return this.discount; }
    private void setDiscount(int discount) {
        discount = Math.clamp(discount,1,100);
        if(this.discount != discount) {
            this.discount = discount;
            this.setChanged(p -> p.setInt("discount",this.discount));
        }
    }

    private PlayerDiscounts() {}
    private PlayerDiscounts(boolean active, PlayerReferenceList playerList, int discount) {
        super(active);
        this.playerList.copyFrom(playerList,false);
        this.discount = discount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return MultiIcon.of(SpriteIcon.of(LCApi.id("icon/counting")),ItemIcon.of(LCItems.COIN_GOLD)); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.playerList.isEmpty() && this.discount == 10; }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        builder.setInt("discount",this.discount);
        this.playerList.writePacket(builder);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("discount"))
            this.discount = message.getInt("discount");
        this.playerList.handlePacket(message);
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        if(this.isOnList(event)) {
            switch (event.getTrade().getDirection()) {
                case SALE -> event.addHelpful(INFO_SALE.get(this.discount));
                case PURCHASE -> event.addHelpful(INFO_PURCHASE.get(this.discount));
            }
        }
    }

    @Override
    protected void tradeCost(TradeEvent.Cost event) {
        if(this.isOnList(event)) {
            switch (event.getTrade().getDirection()) {
                case SALE -> event.giveDiscount(this.discount);
                case PURCHASE -> event.hikePrice(this.discount);
            }
        }
    }

    public boolean isOnList(TradeEvent event) { return this.isOnList(event.getCustomer().asReference()); }
    public boolean isOnList(PlayerReference player) { return this.playerList.isInList(player); }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("setDiscount"))
            this.setDiscount(request.getInt("setDiscount"));
        if(request.contains("addPlayer")) {
            PlayerReference added = request.get("addPlayer",LCFancyPacketTypes.PLAYER_REFERENCE);
            if(added == null || this.isOnList(added))
                return;
            this.playerList.add(added);
        }
        if(request.contains("removePlayer")) {
            PlayerReference removed = request.get("removePlayer",LCFancyPacketTypes.PLAYER_REFERENCE);
            if(removed == null)
                return;
            this.playerList.remove(removed);
        }
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.playerList.clear();
        this.setDiscount(10);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("players", PlayerReferenceList.CODEC,this.playerList);
        output.putInt("discount",this.discount);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.playerList.copyFrom(data.read("players", PlayerReferenceList.CODEC).orElse(new PlayerReferenceList()),true);
        this.setDiscount(data.getIntOr("discount",10));
    }

}
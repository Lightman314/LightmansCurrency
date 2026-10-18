package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReferenceList;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class PlayerInteractionListing extends TradeRule {

    private static final MapCodec<PlayerInteractionListing> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.BOOL.fieldOf("whitelist").forGetter(PlayerInteractionListing::isWhitelistMode),
            PlayerReferenceList.CODEC.fieldOf("players").forGetter(r -> r.playerList)
    ).apply(builder,PlayerInteractionListing::new));

    public static final TradeRuleType<PlayerInteractionListing> TYPE = new TradeRuleType<>(MAP_CODEC,PlayerInteractionListing::new);

    private static final IconData ICON_WHITELIST = SpriteIcon.ofMulti(LCApi.id("icon/counting"),LCApi.id("icon/checkmark"));
    private static final IconData ICON_BLACKLIST = SpriteIcon.ofMulti(LCApi.id("icon/counting"),LCApi.id("icon/sign_x"));

    public static final TextEntry DENIAL_BLACKLIST = TextEntry.tradeRuleMessage(TYPE,"denial.blacklist");
    public static final TextEntry DENIAL_WHITELIST = TextEntry.tradeRuleMessage(TYPE,"denial.whitelist");
    public static final TextEntry ALLOWED = TextEntry.tradeRuleMessage(TYPE,"allowed");

    public static final TextEntry BUTTON_MODE_WHITELIST = TextEntry.tradeRuleButton(TYPE,"mode.whitelist");
    public static final TextEntry BUTTON_MODE_BLACKLIST = TextEntry.tradeRuleButton(TYPE,"mode.blacklist");

    private boolean whitelistMode = true;
    public boolean isWhitelistMode() { return this.whitelistMode; }
    public boolean isBlacklistMode() { return !this.whitelistMode; }
    private void setWhitelistMode(boolean newMode) {
        if(this.whitelistMode != newMode) {
            this.whitelistMode = newMode;
            this.setChanged(p -> p.setBoolean("whitelist",this.whitelistMode));
        }
    }

    private final PlayerReferenceList playerList = new PlayerReferenceList().withListener(this::setChanged);
    public List<PlayerReference> getPlayerList() { return this.playerList.getPlayers(); }

    private PlayerInteractionListing() {}
    private PlayerInteractionListing(boolean active,boolean whitelistMode,PlayerReferenceList playerList) {
        super(active);
        this.whitelistMode = whitelistMode;
        this.playerList.copyFrom(playerList,false);
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return this.whitelistMode ? ICON_WHITELIST : ICON_BLACKLIST; }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.whitelistMode && this.playerList.isEmpty(); }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setBoolean("whitelist",this.whitelistMode);
        this.playerList.writePacket(builder);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        if(message.contains("whitelist"))
            this.whitelistMode = message.getBoolean("whitelist");
        this.playerList.handlePacket(message);
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        if(this.isBlacklistMode() && this.playerList.isInList(event))
            event.addDenial(DENIAL_BLACKLIST.get());
        else if(this.isWhitelistMode()) {
            if(this.playerList.isInList(event))
                event.addHelpful(ALLOWED.get());
            else
                event.addDenial(DENIAL_WHITELIST.get());
        }
    }

    /**
     * Method used by the <code>/lcadmin trader addToWhitelist [trader] [players]</code> command to add players to the whitelist.
     * Will force the listing into whitelist mode, and clear any players already on it if it was in blacklist mode.
     */
    public void addToWhitelistWithCommand(ServerPlayer player) {
        if(this.isBlacklistMode()) {
            this.playerList.clear();
            this.setWhitelistMode(true);
        }
        this.playerList.add(PlayerReference.of(player));
    }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        if(request.contains("addPlayer"))
            this.playerList.add(request.get("addPlayer",LCFancyPacketTypes.PLAYER_REFERENCE));
        if(request.contains("removePlayer")) {
            PlayerReference player = request.get("removePlayer",LCFancyPacketTypes.PLAYER_REFERENCE);
            this.playerList.remove(player);
        }
        if(request.contains("changeMode"))
            this.setWhitelistMode(request.getBoolean("changeMode"));
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.setWhitelistMode(true);
        this.playerList.clear();
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.putBoolean("whitelist",this.whitelistMode);
        output.store("players",PlayerReferenceList.CODEC,this.playerList);
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setWhitelistMode(data.getBooleanOr("whitelist",true));
        this.playerList.copyFrom(data.read("players",PlayerReferenceList.CODEC).orElse(new PlayerReferenceList()),true);
    }

}
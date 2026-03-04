package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerListing extends TradeRule implements ICopySupportingRule {

    public static final TradeRuleType<PlayerListing> TYPE = new Type();

    private static final MapCodec<PlayerListing> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("whitelist").forGetter(PlayerListing::isWhitelistMode),
            PlayerReference.LIST_CODEC.fieldOf("players").forGetter(PlayerListing::getPlayerList),
            baseFields()
    ).apply(builder,PlayerListing::new));

    boolean whitelistMode = true;
    public boolean isWhitelistMode() { return this.whitelistMode; }
    public boolean isBlacklistMode() { return !this.whitelistMode; }

    List<PlayerReference> playerList = new ArrayList<>();
    public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }

    private PlayerListing() { }
    private PlayerListing(boolean whitelistMode,List<PlayerReference> playerList,boolean active) {
        super(active);
        this.whitelistMode = whitelistMode;
        this.playerList = new ArrayList<>(playerList);
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, Player player) {
        builder.setBoolean("whitelist",this.whitelistMode)
                .setList("players",this.playerList,ModLazyPackets.PLAYER_REFERENCE);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.whitelistMode = data.getBoolean("whitelist");
        this.playerList = data.getList("players",ModLazyPackets.PLAYER_REFERENCE);
    }

    @Override
    public IconData getIcon() { return this.isWhitelistMode() ? IconUtil.ICON_WHITELIST : IconUtil.ICON_BLACKLIST; }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        if(this.isBlacklistMode() && this.isInList(event.getPlayerReference()))
            event.addDenial(LCText.TRADE_RULE_PLAYER_LISTING_DENIAL_BLACKLIST.get());
        else if(this.isWhitelistMode())
        {
            if(this.isInList(event.getPlayerReference()))
                event.addHelpful(LCText.TRADE_RULE_PLAYER_LISTING_ALLOWED.get());
            else
                event.addDenial(LCText.TRADE_RULE_PLAYER_LISTING_DENIAL_WHITELIST.get());
        }
    }

    public boolean isInList(PlayerReference player) { return PlayerReference.isInList(this.playerList,player); }

    /**
     * Method used by the <code>/lcadmin traderdata addToWhitelist [trader] [players]</code> command to add players to the whitelist.
     * Will force the listing into whitelist mode, and clear any players already on it if it was in blacklist mode.
     */
    public boolean addToWhitelist(ServerPlayer player)
    {
        boolean changed = false;
        PlayerReference pr = PlayerReference.of(player);
        if(this.isBlacklistMode())
        {
            this.playerList.clear();
            changed = true;
        }
        if(!this.whitelistMode)
        {
            this.whitelistMode = true;
        }
        if(!this.isInList(pr))
        {
            this.playerList.add(pr);
            changed = true;
        }
        return changed;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        if(compound.contains("Players", Tag.TAG_LIST))
            this.playerList = PlayerReference.loadList(compound,"Players");
        if(compound.contains("WhitelistMode"))
            this.whitelistMode = compound.getBoolean("WhitelistMode");
    }

    @Override
    public void writeSettings(SavedSettingData.MutableNodeAccess node) {
        node.setBooleanValue("whitelist_mode",this.whitelistMode);
        for(int i = 0; i < this.playerList.size(); ++i)
            node.setCompoundValue("player_" + i,this.playerList.get(i).save());
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess node) {
        this.whitelistMode = node.getBooleanValue("whitelist_mode");
        List<PlayerReference> temp = new ArrayList<>();
        for(int i = 0; node.hasCompoundValue("player_" + i); ++i)
            temp.add(PlayerReference.load(node.getCompoundValue("player_" + i)));
        this.playerList = temp;
    }

    @Override
    public void resetToDefaultState() {
        this.whitelistMode = true;
        this.playerList = new ArrayList<>();
    }

    @Override
    protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
        if(updateInfo.contains("AddPlayer"))
        {
            PlayerReference added = PlayerReference.load(updateInfo.getTag("AddPlayer"));
            if(added == null || this.isInList(added))
                return;
            this.playerList.add(added);
        }
        if(updateInfo.contains("RemovePlayer"))
        {
            PlayerReference removed = PlayerReference.load(updateInfo.getTag("RemovePlayer"));
            if(removed == null || !this.isInList(removed))
                return;
            PlayerReference.removeFromList(this.playerList,removed);
        }
        if(updateInfo.contains("ChangeMode"))
        {
            this.whitelistMode = updateInfo.getBoolean("ChangeMode");
        }
    }

    private static class Type extends TradeRuleType<PlayerListing>
    {
        @Override
        public PlayerListing create() { return new PlayerListing(); }
        @Override
        public MapCodec<PlayerListing> mapCodec() { return MAP_CODEC;}
    }

}

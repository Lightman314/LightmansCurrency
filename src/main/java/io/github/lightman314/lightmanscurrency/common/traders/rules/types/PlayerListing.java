package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.PlayerListingTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.IRuleLoadListener;
import io.github.lightman314.lightmanscurrency.common.traders.rules.ITradeRuleHost;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerListing extends TradeRule {

    public static final TradeRuleType<PlayerListing> TYPE = new TradeRuleType<>(VersionUtil.lcResource("player_list"),PlayerListing::new);

    public static final IRuleLoadListener LISTENER = new DataListener();

    boolean whitelistMode = true;
    public boolean isWhitelistMode() { return this.whitelistMode; }
    public boolean isBlacklistMode() { return !this.whitelistMode; }

    List<PlayerReference> playerList = new ArrayList<>();
    public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }

    private PlayerListing() { super(TYPE); }

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
    protected void saveAdditional(CompoundTag compound) {
        compound.putBoolean("WhitelistMode", this.whitelistMode);
        PlayerReference.saveList(compound,this.playerList, "Players");
    }

    @Override
    protected void loadAdditional(CompoundTag compound) {
        if(compound.contains("Players", Tag.TAG_LIST))
            this.playerList = PlayerReference.loadList(compound,"Players");
        if(compound.contains("WhitelistMode"))
            this.whitelistMode = compound.getBoolean("WhitelistMode");
    }

    @Override
    public JsonObject saveToJson(JsonObject json) { return json; }

    @Override
    public void loadFromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException { }

    @Override
    public CompoundTag savePersistentData() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void loadPersistentData(CompoundTag data) {
        this.loadAdditional(data);
    }

    @Override
    protected void handleUpdateMessage(Player player,LazyPacketData updateInfo) {
        if(updateInfo.contains("AddPlayer"))
        {
            PlayerReference added = PlayerReference.load(updateInfo.getNBT("AddPlayer"));
            if(added == null || this.isInList(added))
                return;
            this.playerList.add(added);
        }
        if(updateInfo.contains("RemovePlayer"))
        {
            PlayerReference removed = PlayerReference.load(updateInfo.getNBT("RemovePlayer"));
            if(removed == null || !this.isInList(removed))
                return;
            PlayerReference.removeFromList(this.playerList,removed);
        }
        if(updateInfo.contains("ChangeMode"))
        {
            this.whitelistMode = updateInfo.getBoolean("ChangeMode");
        }
    }

    
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerListingTab(parent,TYPE); }

    private static final class DataListener implements IRuleLoadListener
    {
        @Override
        public void afterLoading(@Nullable ITradeRuleHost host, List<CompoundTag> allData, List<TradeRule> rules) {
            if(TradeRule.getRule(TYPE.type,rules) == null)
            {
                PlayerListing rule = new PlayerListing();
                if(host == null || (host.allowTradeRule(rule) && rule.allowHost(host)))
                {
                    rule.setHost(host);
                    Pair<Boolean,Boolean> whitelistState = Pair.of(false,false);
                    Pair<Boolean,Boolean> blacklistState = Pair.of(false,false);
                    //Check for old whitelist/blacklist data
                    for(CompoundTag tag : allData)
                    {
                        if(tag.contains("Type"))
                        {
                            String type = tag.getString("Type");
                            if(type.equals("lightmanscurrency:whitelist") && tag.contains("WhitelistedPlayers") && !whitelistState.first())
                            {
                                List<PlayerReference> whitelist = PlayerReference.loadList(tag,"WhitelistedPlayers");
                                boolean relevant = !whitelist.isEmpty();
                                for(PlayerReference pr : whitelist)
                                {
                                    if(!PlayerReference.isInList(rule.playerList,pr))
                                        rule.playerList.add(pr);
                                }
                                boolean active = tag.contains("Active") && tag.getBoolean("Active");
                                whitelistState = Pair.of(relevant,active);
                            }
                            if(type.equals("lightmanscurrency:blacklist") && tag.contains("BannedPlayers") && !blacklistState.first())
                            {
                                List<PlayerReference> blacklist = PlayerReference.loadList(tag,"BannedPlayers");
                                boolean relevant = !blacklist.isEmpty();
                                for(PlayerReference pr : blacklist)
                                {
                                    if(!PlayerReference.isInList(rule.playerList,pr))
                                        rule.playerList.add(pr);
                                }
                                boolean active = tag.contains("Active") && tag.getBoolean("Active");
                                blacklistState = Pair.of(relevant,active);
                            }
                        }
                    }
                    if(whitelistState.first() || blacklistState.first())
                    {
                        LightmansCurrency.LogDebug("Successfully loaded data from the old whitelist/blacklist rules!");
                        if(whitelistState.first() != blacklistState.first())
                        {
                            //Set rule as active if the relevant old rule was also active
                            rule.setActive(whitelistState.first() ? whitelistState.second() : blacklistState.second());
                        }
                    }
                }
            }
        }
    }

}

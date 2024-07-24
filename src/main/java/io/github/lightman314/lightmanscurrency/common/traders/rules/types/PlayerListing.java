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
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerListing extends TradeRule {

    public static final TradeRuleType<PlayerListing> TYPE = new TradeRuleType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "player_list"),PlayerListing::new);

    public static final IRuleLoadListener LISTENER = new DataListener();

    boolean whitelistMode = true;
    public boolean isWhitelistMode() { return this.whitelistMode; }
    public boolean isBlacklistMode() { return !this.whitelistMode; }

    List<PlayerReference> playerList = new ArrayList<>();
    public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }

    private PlayerListing() { super(TYPE); }

    @Override
    public void beforeTrade(@Nonnull TradeEvent.PreTradeEvent event) {
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

    public boolean isInList(@Nonnull PlayerReference player) { return PlayerReference.isInList(this.playerList,player); }

    /**
     * Method used by the <code>/lcadmin traderdata addToWhitelist [trader] [players]</code> command to add players to the whitelist.
     * Will force the listing into whitelist mode, and clear any players already on it if it was in blacklist mode.
     */
    public boolean addToWhitelist(@Nonnull ServerPlayer player)
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
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putBoolean("WhitelistMode", this.whitelistMode);
        PlayerReference.saveList(compound,this.playerList, "Players");
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        if(compound.contains("Players", Tag.TAG_LIST))
            this.playerList = PlayerReference.loadList(compound,"Players");
        if(compound.contains("WhitelistMode"))
            this.whitelistMode = compound.getBoolean("WhitelistMode");
    }

    @Override
    public JsonObject saveToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) { return json; }

    @Override
    public void loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException { }

    @Override
    public CompoundTag savePersistentData(@Nonnull HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag,lookup);
        return tag;
    }

    @Override
    public void loadPersistentData(@Nonnull CompoundTag data, @Nonnull HolderLookup.Provider lookup) {
        this.loadAdditional(data,lookup);
    }

    @Override
    protected void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
        if(updateInfo.contains("Add"))
        {
            boolean add = updateInfo.getBoolean("Add");
            String name = updateInfo.getString("Name");
            PlayerReference player = PlayerReference.of(false, name);
            if(player == null)
                return;
            if(add && !this.isInList(player))
            {
                this.playerList.add(player);
            }
            else if(!add && this.isInList(player))
            {
                PlayerReference.removeFromList(this.playerList, player);
            }
        }
        if(updateInfo.contains("ChangeMode"))
        {
            this.whitelistMode = updateInfo.getBoolean("ChangeMode");
        }
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new PlayerListingTab(parent,TYPE); }

    private static final class DataListener implements IRuleLoadListener
    {
        @Override
        public void afterLoading(@Nullable ITradeRuleHost host, @Nonnull List<CompoundTag> allData, @Nonnull List<TradeRule> rules) {
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
                                rule.playerList.addAll(whitelist);
                                boolean active = tag.contains("Active") && tag.getBoolean("Active");
                                whitelistState = Pair.of(relevant,active);
                            }
                            if(type.equals("lightmanscurrency:blacklist") && tag.contains("BannedPlayers") && !blacklistState.first())
                            {
                                List<PlayerReference> blacklist = PlayerReference.loadList(tag,"BannedPlayers");
                                boolean relevant = !blacklist.isEmpty();
                                rule.playerList.addAll(blacklist);
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

package io.github.lightman314.lightmanscurrency.api.helpers.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeCustomer;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlayerReferenceList {

    public static final Codec<PlayerReferenceList> CODEC = PlayerReference.LIST_CODEC.xmap(PlayerReferenceList::new, PlayerReferenceList::getPlayers);
    public static final StreamCodec<FriendlyByteBuf, PlayerReferenceList> STREAM_CODEC = PlayerReference.LIST_STREAM_CODEC.map(PlayerReferenceList::new, PlayerReferenceList::getPlayers);

    private FancyPacketMap.Listener changeListener = p -> {};
    public PlayerReferenceList withListener(FancyPacketMap.Listener listener) { this.changeListener = listener; return this; }

    private final String fullKey;
    private final String changeKey;

    private final List<PlayerReference> players = new ArrayList<>();
    public List<PlayerReference> getPlayers() { return Collections.unmodifiableList(this.players); }
    public boolean isInList(Entity player) { return PlayerReference.isInList(this.players,player); }
    public boolean isInList(UUID player) { return PlayerReference.isInList(this.players,player); }
    public boolean isInList(PlayerReference player) { return PlayerReference.isInList(this.players,player); }
    public boolean isInList(TradeCustomer customer) { return this.isInList(customer.asReference()); }
    public boolean isInList(TradeEvent event) { return this.isInList(event.getCustomer().asReference()); }

    public PlayerReferenceList() { this("players","playerChanges"); }
    public PlayerReferenceList(String fullKey, String changeKey) {
        this.fullKey = fullKey;
        this.changeKey = changeKey;
    }
    private PlayerReferenceList(List<PlayerReference> players) {
        this();
        for(PlayerReference p : players)
            PlayerReference.addToList(this.players,p);
    }

    public void copyFrom(PlayerReferenceList other, boolean setChanged) {
        this.players.clear();
        for(PlayerReference p : other.players)
            PlayerReference.addToList(this.players,p);
        if(setChanged)
            this.changeListener.accept(p -> this.writePacket(p).remove(this.changeKey));
    }


    public void clear() {
        if(this.players.isEmpty())
            return;
        this.players.clear();
        this.changeListener.accept(p -> this.writePacket(p).remove(this.changeKey));
    }

    public int size() { return this.players.size(); }
    public boolean isEmpty() { return this.players.isEmpty(); }

    public FancyPacketMap.Mutable writePacket(FancyPacketMap.Mutable packet) {
        return packet.setList(this.fullKey,LCFancyPacketTypes.PLAYER_REFERENCE,this.players);
    }

    public void handlePacket(FancyPacketMap packet) {
        if(packet.contains(this.fullKey)) {
            this.players.clear();
            this.players.addAll(packet.getList(this.fullKey,LCFancyPacketTypes.PLAYER_REFERENCE));
        }
        if(packet.contains(this.changeKey)) {
            for(FancyPacketMap entry : packet.getList(this.changeKey,LCFancyPacketTypes.MAP)) {
                PlayerReference player = entry.get("player",LCFancyPacketTypes.PLAYER_REFERENCE);
                if(player != null) {
                    if(entry.getBoolean("add"))
                        PlayerReference.addToList(this.players,player);
                    else
                        PlayerReference.removeFromList(this.players,player);
                }
            }
        }
    }

    private void setChanged(PlayerReference player,boolean add) {
        this.changeListener.accept(p -> p.addToList(this.changeKey, LCFancyPacketTypes.MAP,FancyPacketMap.map()
                .set("player",LCFancyPacketTypes.PLAYER_REFERENCE,player)
                .setBoolean("add",add)));
    }

    public boolean add(Player player) { return this.add(PlayerReference.of(player)); }
    public boolean add(@Nullable PlayerReference player) {
        if(player == null)
            return false;
        if(PlayerReference.addToList(this.players,player)) {
            this.setChanged(player,true);
            return true;
        }
        return false;
    }

    public boolean remove(@Nullable PlayerReference player) {
        if(player == null)
            return false;
        if(PlayerReference.removeFromList(this.players,player)) {
            this.setChanged(player,false);
            return true;
        }
        return false;
    }

}

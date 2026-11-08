package io.github.lightman314.lightmanscurrency.features.tickets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketID;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public class TicketDataCache extends FancyData {

    private static final Codec<TicketDataCache> FALLBACK_CODEC = Codec.LONG.xmap(l -> new TicketDataCache(l,""),TicketDataCache::peekNextID);
    private static final Codec<TicketDataCache> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("nextID").forGetter(TicketDataCache::peekNextID),
            Codec.STRING.fieldOf("codeKey").forGetter(TicketDataCache::getCodeKey)
    ).apply(builder,TicketDataCache::new));

    public static final FancyDataType<TicketDataCache> TYPE = new FancyDataType<>("ticket_data",TicketDataCache::new,Codec.withAlternative(CODEC,FALLBACK_CODEC));

    private long nextID;
    private String codeKey;

    private TicketDataCache() { this(TicketID.STARTING_ID,""); }
    private TicketDataCache(long nextID,String codeKey) {
        this.nextID = nextID;
        this.codeKey = codeKey;
    }

    public long peekNextID() { return this.nextID; }
    public long useNextID() {
        if(this.isClient())
            return this.nextID;
        long result = this.nextID++;
        this.setChanged();
        this.sendPacketToAll(FancyPacketMap.map().setLong("nextID",this.nextID));
        return result;
    }

    public String getCodeKey() {
        if(this.codeKey.isEmpty() && this.isServer()) {
            RandomSource random = RandomSource.create();
            String remaining = TicketStationRecipeInput.VALID_CODE_CHARS;
            StringBuilder result = new StringBuilder();
            while(!remaining.isEmpty()) {
                int next = random.nextInt(remaining.length());
                char c = remaining.charAt(next);
                //Remove the entry from the string array
                if(next == 0)
                    remaining = remaining.substring(1);
                else if(next == remaining.length() - 1)
                    remaining = remaining.substring(0,remaining.length() - 1);
                else
                    remaining = remaining.substring(0,next) + remaining.substring(next + 1);
                result.append(c);
            }
            this.codeKey = result.toString();
            this.setChanged();
            LightmansCurrency.LogDebug("Randomized code key: " + this.codeKey);
        }
        return this.codeKey;
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.sendPacket(player,FancyPacketMap.map()
                .setLong("nextID",this.nextID)
                .setString("codeKey",this.getCodeKey()));
    }

    @Override
    public void syncTick() { }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        if(data.contains("nextID"))
            this.nextID = data.getLong("nextID");
        if(data.contains("codeKey"))
            this.codeKey = data.getString("codeKey");
    }

}

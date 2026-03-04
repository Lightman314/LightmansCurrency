package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.*;

public class TeamDataCache extends CustomData {

    public static final CustomDataType<TeamDataCache> TYPE = new CustomDataType<>("lightmanscurrency_team_data",TeamDataCache::new);

    private long nextID = 0;
    private long getNextID() {
        long id = this.nextID;
        this.nextID++;
        this.setChanged();
        return id;
    }

    private final Map<Long,Team> teams = new HashMap<>();

    private Set<Long> changedTeams = new HashSet<>();

    private TeamDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag,DataContext<Tag> context) {
        tag.putLong("NextID", this.nextID);

        tag.put("Teams",context.write(new ArrayList<>(this.teams.values()),Team.CODEC.listOf()));
    }

    @Override
    protected void load(CompoundTag tag,DataContext<Tag> context) {
        this.nextID = tag.getLong("NextID");

        List<Team> list = context.safeReadList(tag.get("Teams"),Team.CODEC,e -> LightmansCurrency.LogError("Error loading team: " + e));
        for(Team t : list)
            this.teams.put(t.getID(),t.initialize());
    }

    public List<ITeam> getAllTeams() { return new ArrayList<>(this.teams.values()); }

    @Nullable
    public ITeam getTeam(long teamID) { return this.teams.get(teamID); }

    public void markTeamDirty(long teamID)
    {
        if(this.isClient())
            return;
        this.setChanged();
        this.changedTeams.add(teamID);
    }

    @Nullable
    public ITeam registerTeam(Player owner, String teamName)
    {
        long teamID = this.getNextID();
        Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
        this.teams.put(teamID, newTeam.initialize());

        this.sendSyncPacket(this.builder().setCustom("CreateTeam",newTeam,ModLazyPackets.TEAM));

        return newTeam;
    }

    public void removeTeam(long teamID)
    {
        if(this.teams.containsKey(teamID))
        {
            this.teams.remove(teamID);
            this.setChanged();

            this.sendSyncPacket(this.builder().setLong("DeleteTeam",teamID));
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {
        if(message.contains("CreateTeam"))
        {
            Team team = message.getCustom("CreateTeam",ModLazyPackets.TEAM);
            if(team != null)
                this.teams.put(team.getID(),team.flagAsClient(this).initialize());
        }
        if(message.contains("UpdateTeam"))
        {
            long id = message.getLong("UpdateTeam");
            if(this.teams.containsKey(id))
                this.teams.get(id).handlePacket(message.getMap("UpdateData"));
        }
        if(message.contains("DeleteTeam"))
            this.teams.remove(message.getLong("DeleteTeam"));
    }

    @Override
    protected void serverInit() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    private void serverTick(ServerTickEvent.Post event)
    {
        Set<Long> changed = this.changedTeams;
        this.changedTeams = new HashSet<>();
        for(long teamID : changed)
        {
            Team team = this.teams.get(teamID);
            if(team != null)
            {
                this.sendSyncPacket(this.builder()
                        .setLong("UpdateTeam",teamID)
                        .setMap("UpdateData",team.getAndCleanPacket()));
            }
        }
    }

    @SubscribeEvent
    private void onServerShutdown(ServerStoppingEvent event) { NeoForge.EVENT_BUS.unregister(this); }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        HolderLookup.Provider lookup = LookupHelper.getRegistryAccess();
        for(Team team : this.teams.values())
            this.sendSyncPacket(this.builder().setCustom("CreateTeam",team,ModLazyPackets.TEAM),player);
    }

}

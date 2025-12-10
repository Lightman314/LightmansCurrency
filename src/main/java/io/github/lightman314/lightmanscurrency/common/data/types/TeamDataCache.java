package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.teams.ITeam;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
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

    private TeamDataCache() {}

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("NextID", this.nextID);

        ListTag teamList = new ListTag();
        this.teams.forEach((teamID, team) ->{
            if(team != null)
                teamList.add(team.save());
        });
        tag.put("Teams", teamList);
    }

    @Override
    protected void load(CompoundTag tag) {
        this.nextID = tag.getLong("NextID");

        ListTag teamList = tag.getList("Teams", Tag.TAG_COMPOUND);
        for(int i = 0; i < teamList.size(); ++i)
        {
            Team team = Team.load(teamList.getCompound(i));
            if(team != null)
                this.teams.put(team.getID(),team.initialize());
        }
    }

    public List<ITeam> getAllTeams() { return new ArrayList<>(this.teams.values()); }

    @Nullable
    public ITeam getTeam(long teamID) { return this.teams.get(teamID); }

    public void markTeamDirty(long teamID)
    {
        if(this.isClient())
            return;
        this.setChanged();
        Team team = this.teams.get(teamID);
        if(team != null)
            this.sendSyncPacket(this.builder().setCompound("UpdateTeam",team.save()));
    }

    @Nullable
    public ITeam registerTeam(Player owner, String teamName)
    {
        long teamID = this.getNextID();
        Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
        this.teams.put(teamID, newTeam.initialize());

        this.markTeamDirty(teamID);

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
    protected void parseSyncPacket(LazyPacketData message) {
        if(message.contains("UpdateTeam"))
        {
            Team team = Team.load(message.getNBT("UpdateTeam"));
            if(team != null)
                this.teams.put(team.getID(),team.flagAsClient(this).initialize());
        }
        if(message.contains("DeleteTeam"))
            this.teams.remove(message.getLong("DeleteTeam"));
    }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        for(Team team : this.teams.values())
            this.sendSyncPacket(this.builder().setCompound("UpdateTeam",team.save()),player);
    }

}
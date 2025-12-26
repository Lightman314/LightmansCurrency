package io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.TraderAttachment;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExternalAuthorizationAttachment extends TraderAttachment {

    public static final TraderAttachmentType<ExternalAuthorizationAttachment> TYPE = new TraderAttachmentType<>(VersionUtil.lcResource("external_authorization"),ExternalAuthorizationAttachment::new);

    private ExternalAuthorizationAttachment(TraderData trader) { super(trader); }

    public static final String EDIT_AUTHORIZATION_PERMISSION = "allowExternalAuthorization";

    @Override
    public TraderAttachmentType<?> getType() { return TYPE; }

    public enum AccessLevel { NONE, ALLY, ADMIN }

    private final Map<String,AccessLevel> accessLevels = new HashMap<>();

    public void flagAttemptedAccess(String id) {
        if(this.accessLevels.containsKey(id))
            return;
        this.accessLevels.put(id,AccessLevel.NONE);
        this.markDirty();
    }

    public List<String> getAttemptedAccessors() { return new ArrayList<>(this.accessLevels.keySet().stream().sorted(String::compareTo).toList()); }

    public AccessLevel getAccessLevel(@Nullable String id) {
        if(id == null)
            return AccessLevel.NONE;
        return this.accessLevels.getOrDefault(id,AccessLevel.NONE);
    }

    @Override
    public CompoundTag save(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        this.accessLevels.forEach((id,level) -> tag.putString(id,level.name()));
        return tag;
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider lookup) {
        this.accessLevels.clear();
        for(String key : tag.getAllKeys())
            this.accessLevels.put(key,EnumUtil.enumFromString(tag.getString(key),AccessLevel.values(),AccessLevel.NONE));
    }

    @Override
    public void modifyDefaultPermissions(Map<String, Integer> defaultPermissions) {
        defaultPermissions.put(EDIT_AUTHORIZATION_PERMISSION,0);
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains(this.getType() + "-ChangeAuthorization"))
        {
            if(!this.trader.hasPermission(player,EDIT_AUTHORIZATION_PERMISSION))
                return;
            String entry = message.getString(this.getType() + "-ChangeAuthorization");
            AccessLevel newLevel = EnumUtil.enumFromOrdinal(message.getInt(this.getType() + "-NewLevel"),AccessLevel.values(),AccessLevel.NONE);
            if(this.getAccessLevel(entry) != newLevel)
            {
                this.accessLevels.put(entry,newLevel);
                this.markDirty();
            }
        }
    }
}

package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderSettingsNode;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PermissionSettings extends EasyTraderSettingsNode<TraderData> {

    public PermissionSettings(TraderData trader) { super("permissions", trader, 500); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_TRADER_ALLY_PERMS.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.EDIT_PERMISSIONS; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        CompoundTag tag = new CompoundTag();
        ListTag permList = new ListTag();
        this.trader.getAllyPermissionMap().forEach((key,level) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("perm",key);
            entry.putInt("level",level);
            permList.add(entry);
        });
        tag.put("Perms",permList);
        data.setCompoundValue("permissions",tag);
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        if(data.hasCompoundValue("permissions"))
        {
            CompoundTag tag = data.getCompoundValue("permissions");
            ListTag permList = tag.getList("Perms", Tag.TAG_COMPOUND);
            Map<String,Integer> temp = new HashMap<>();
            for(int i = 0; i < permList.size(); ++i)
            {
                CompoundTag entry = permList.getCompound(i);
                String key = entry.getString("perm");
                int level = entry.getInt("level");
                temp.put(key,level);
            }
            this.trader.overwriteAllyPermissions(temp);
        }
    }

    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        if(data.hasCompoundValue("permissions"))
        {
            CompoundTag tag = data.getCompoundValue("permissions");
            ListTag permList = tag.getList("Perms",Tag.TAG_COMPOUND);
            int count = 0;
            for(int i = 0; i < permList.size(); ++i)
            {
                if(permList.getCompound(i).getInt("level") > 0)
                    count++;
            }
            lineWriter.accept(formatEntry(LCText.DATA_ENTRY_PERMISSIONS.get(),LCText.DATA_ENTRY_PERMISSIONS_COUNT.get(count)));
        }
    }

}
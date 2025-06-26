package io.github.lightman314.lightmanscurrency.api.traders.settings;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;

import java.util.List;

public abstract class EasyTraderSettingsNode<T extends TraderData> extends TraderSettingsNode<T> {

    public EasyTraderSettingsNode(String key, T trader) { super(key, trader); }
    public EasyTraderSettingsNode(String key, T trader, int priority) { super(key, trader, priority); }

    protected String getRequiredPermission() { return ""; }
    protected List<String> getRequiredPermissions() { return ImmutableList.of(this.getRequiredPermission()); }

    @Override
    public boolean allowLoading(LoadContext context) { return this.hasPermission(context); }

    public boolean hasPermission(LoadContext context) {
        for(String perm : this.getRequiredPermissions())
        {
            if(!context.hasPermission(perm))
            {
                LightmansCurrency.LogDebug("Missing " + perm + " permission. Cannot load " + this.getClass().getSimpleName());
                return false;
            }
        }
        LightmansCurrency.LogDebug("All permissions were present for " + this.getClass().getSimpleName());
        return true;
    }

}

package io.github.lightman314.lightmanscurrency.api.config.client.screen.options;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigFileScreen;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.message.config.CPacketEditConfig;
import io.github.lightman314.lightmanscurrency.network.message.config.CPacketTrackServerFile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ConfigFileOption {

    public static final Predicate<Minecraft> ALWAYS_TRUE = m -> true;
    public static final Predicate<Minecraft> OFFLINE_OR_ADMIN = m -> m.player == null || m.player.hasPermissions(2);
    public static final Predicate<Minecraft> ONLINE_AND_ADMIN = m -> m.player != null && m.player.hasPermissions(2);
    public static final Predicate<Minecraft> ONLINE = m -> m.player != null;

    public abstract Component name();
    @Nullable
    public abstract List<Component> buttonTooltip();
    public abstract boolean canAccess(Minecraft minecraft);
    public abstract boolean canEdit(Minecraft minecraft);
    public abstract Screen openScreen(Screen parentScreen);
    public void onSelectionScreenOpened(Minecraft minecraft) { }
    public void onSelectionScreenClosed(Minecraft minecraft) { }

    public static ConfigFileOption create(ConfigFile file) {
        if(file.isClientOnly()) //If client config, can always be edited and viewed
            return create(file,ALWAYS_TRUE,ALWAYS_TRUE);
        else if(file instanceof SyncedConfigFile) //If synced, can only be viewed when on a server
            return create(file,ONLINE,ONLINE_AND_ADMIN);
        else //If not client but not synced, can be viewed any time but can only be edited if an admin
            return create(file,ALWAYS_TRUE,OFFLINE_OR_ADMIN);
    }
    public static ConfigFileOption create(ConfigFile file, Predicate<Minecraft> canAccess, Predicate<Minecraft> canEdit) { return new DefaultConfigOption(file,canAccess,canEdit); }

    public static class DefaultConfigOption extends ConfigFileOption
    {
        public final ConfigFile file;
        private final Predicate<Minecraft> canAccess;
        private final Predicate<Minecraft> canEdit;
        private DefaultConfigOption(ConfigFile file, Predicate<Minecraft> canAccess, Predicate<Minecraft> canEdit) {
            this.file = file; this.canAccess = canAccess; this.canEdit = canEdit;
        }

        @Override
        public Component name() { return this.file.getDisplayName(); }
        @Nullable
        @Override
        public List<Component> buttonTooltip() {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(LCText.CONFIG_LABEL_FILE.get(this.file.getFilePath()));
            tooltip.add(LCText.CONFIG_OPTION_COUNT.get(this.file.getAllOptions().size()));
            return tooltip;
        }
        @Override
        public boolean canAccess(Minecraft minecraft) { return this.canAccess.test(minecraft); }
        @Override
        public boolean canEdit(Minecraft minecraft) { return this.canEdit.test(minecraft); }
        @Override
        public Screen openScreen(Screen parentScreen) { return new ConfigFileScreen(parentScreen,this); }

        private boolean requiresServerTracking(Minecraft minecraft) { return minecraft.level != null && !this.file.isClientOnly() && !(this.file instanceof SyncedConfigFile); }
        @Override
        public void onSelectionScreenOpened(Minecraft minecraft) {
            if(this.requiresServerTracking(minecraft))
                new CPacketTrackServerFile(this.file.getFileID(),true).send();
        }

        @Override
        public void onSelectionScreenClosed(Minecraft minecraft) {
            if(this.requiresServerTracking(minecraft))
            {
                new CPacketTrackServerFile(this.file.getFileID(),false).send();
                //Clear the synced data
                this.file.clearSyncedData();
            }
        }
        public void changeValue(Minecraft minecraft,ConfigOption<?> option,Object newValue)
        {
            //Do nothing if we don't have write access to the config
            if(!this.canAccess(minecraft))
                return;
            boolean hasLevel = minecraft.level != null;
            if(this.file.isClientOnly())
                option.setUnsafe(newValue);
            else
            {
                if(hasLevel)
                {
                    //Send sync packet
                    String parsedNewValue = option.writeUnsafe(newValue);
                    if(parsedNewValue != null)
                        new CPacketEditConfig(this.file.getFileID(),option.getFullName(),parsedNewValue).send();
                    else
                        LightmansCurrency.LogWarning("Unable to send config change packet to the server!");
                }
                else
                    option.setUnsafe(newValue);
            }
        }
    }

}

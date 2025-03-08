package io.github.lightman314.lightmanscurrency.api.config.client.screen.options;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigFileScreen;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ConfigFileOption {

    public static final Predicate<Minecraft> ALWAYS_TRUE = m -> true;
    public static final Predicate<Minecraft> OFFLINE_OR_ADMIN = m -> m.player == null || m.player.hasPermissions(2);
    public static final Predicate<Minecraft> ONLINE_AND_ADMIN = m -> m.player != null && m.player.hasPermissions(2);

    public abstract Component name();
    @Nullable
    public abstract List<Component> buttonTooltip();
    public abstract boolean canAccess(Minecraft minecraft);
    public abstract Screen openScreen(Screen parentScreen);

    public static ConfigFileOption create(ConfigFile file) {
        if(file.isClientOnly())
            return create(file,ALWAYS_TRUE);
        else if(file instanceof SyncedConfigFile)
            return create(file,ONLINE_AND_ADMIN);
        else
            return create(file,OFFLINE_OR_ADMIN);
    }
    public static ConfigFileOption create(ConfigFile file, Predicate<Minecraft> canAccess) { return new DefaultConfigOption(file,canAccess); }

    public static class DefaultConfigOption extends ConfigFileOption
    {
        private final ConfigFile file;
        private final Predicate<Minecraft> canAccess;
        private DefaultConfigOption(ConfigFile file, Predicate<Minecraft> canAccess) { this.file = file; this.canAccess = canAccess; }

        //TODO make translatable later
        @Override
        public Component name() { return EasyText.literal(this.file.getFileName()); }
        //TODO make the file name the tooltip
        @Nullable
        @Override
        public List<Component> buttonTooltip() { return null; }

        @Override
        public boolean canAccess(Minecraft minecraft) { return this.canAccess.test(minecraft); }
        @Override
        public Screen openScreen(Screen parentScreen) { return new ConfigFileScreen(parentScreen,this); }

    }

}

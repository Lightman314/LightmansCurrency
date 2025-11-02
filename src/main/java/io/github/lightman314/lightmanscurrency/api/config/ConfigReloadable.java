package io.github.lightman314.lightmanscurrency.api.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface ConfigReloadable
{

    int PRIORITY_MONEY_PHASE = 100;
    int PRIORITY_AFTER_MONEY_CONFIGS = 500;
    int PRIORITY_AFTER_ALL = 1000;

    ResourceLocation getID();
    default int getDelayPriority() { return 0; }
    void onCommandReload(CommandSourceStack stack) throws CommandSyntaxException;
    boolean canReload(CommandSourceStack stack);
    boolean alertAdmins();

    static ConfigReloadable simpleReloader(ResourceLocation id, Consumer<CommandSourceStack> reloader)
    {
        return new ConfigReloadable() {
            @Override
            public ResourceLocation getID() { return id; }
            @Override
            public void onCommandReload(CommandSourceStack stack) { reloader.accept(stack); }
            @Override
            public boolean canReload(CommandSourceStack stack) { return stack.hasPermission(2); }
            @Override
            public boolean alertAdmins() { return true; }
        };
    }
    static ConfigReloadable simpleReloader(ResourceLocation id,int priority,Consumer<CommandSourceStack> reloader)
    {
        return new ConfigReloadable() {
            @Override
            public ResourceLocation getID() { return id; }
            @Override
            public int getDelayPriority() { return priority; }
            @Override
            public void onCommandReload(CommandSourceStack stack) { reloader.accept(stack); }
            @Override
            public boolean canReload(CommandSourceStack stack) { return stack.hasPermission(2); }
            @Override
            public boolean alertAdmins() { return true; }
        };
    }

}
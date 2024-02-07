package io.github.lightman314.lightmanscurrency.api.config;

import javax.annotation.Nonnull;

public abstract class ClientConfigFile extends ConfigFile {
    protected ClientConfigFile(@Nonnull String fileName) { super(fileName); }
    protected ClientConfigFile(@Nonnull String fileName, @Nonnull LoadPhase loadPhase) { super(fileName, loadPhase); }
    @Override
    public boolean isClientOnly() { return true; }
}

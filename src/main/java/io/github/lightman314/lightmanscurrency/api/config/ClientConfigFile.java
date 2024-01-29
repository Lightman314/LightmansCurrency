package io.github.lightman314.lightmanscurrency.api.config;

import javax.annotation.Nonnull;

public abstract class ClientConfigFile extends ConfigFile {
    protected ClientConfigFile(@Nonnull String fileName) { super(fileName); }
    protected ClientConfigFile(@Nonnull String fileName, boolean loadEarly) { super(fileName, loadEarly); }
    @Override
    public boolean isClientOnly() { return true; }
}

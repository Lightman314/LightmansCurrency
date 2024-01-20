package io.github.lightman314.lightmanscurrency.api.config;

import javax.annotation.Nonnull;

public abstract class ClientConfigFile extends ConfigFile {
    protected ClientConfigFile(@Nonnull String fileName) { super(fileName); }
    @Override
    protected boolean isClientOnly() { return true; }
}

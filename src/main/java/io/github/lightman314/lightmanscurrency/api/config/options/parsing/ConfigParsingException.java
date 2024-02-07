package io.github.lightman314.lightmanscurrency.api.config.options.parsing;

public class ConfigParsingException extends Exception{

    public ConfigParsingException(String message) { super(message); }
    public ConfigParsingException(String message, Throwable cause) { super(message, cause); }
    public ConfigParsingException(Throwable cause) { super(cause); }

}

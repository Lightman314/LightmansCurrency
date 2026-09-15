package io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers;

import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import java.util.function.Function;
import java.util.function.Predicate;

public class IdentifierParser implements Function<String,Identifier>, Predicate<String> {

    public static final IdentifierParser DEFAULT = new IdentifierParser(false);
    public static final IdentifierParser REQUIRE_NAMESPACE = new IdentifierParser(true);

    private final boolean requireNamespace;
    private IdentifierParser(boolean requireNamespace) { this.requireNamespace = requireNamespace; }

    @Override
    public Identifier apply(String s) {
        if(this.requireNamespace && !s.contains(":"))
            return null;
        try { return Identifier.parse(s);
        } catch (IdentifierException ignored) { return null; }
    }

    @Override
    public boolean test(String s) {
        if(s.isEmpty())
            return true;
        if(s.contains(":"))
        {
            String[] split = s.split(":",2);
            return Identifier.isValidNamespace(split[0]) && Identifier.isValidPath(split[1]);
        }
        return this.requireNamespace ? Identifier.isValidNamespace(s) : Identifier.isValidNamespace(s) || Identifier.isValidPath(s);
    }

}
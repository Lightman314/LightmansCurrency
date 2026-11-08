package io.github.lightman314.lightmanscurrency.api.trader.persistent;

import net.minecraft.util.ProblemReporter;

public class PersistentDataException extends Exception implements ProblemReporter.Problem {

    public PersistentDataException(String message) { super(message); }

    @Override
    public String description() { return this.getMessage(); }

}
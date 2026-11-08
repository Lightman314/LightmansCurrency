package io.github.lightman314.lightmanscurrency.api.helpers.data.io;

import net.minecraft.util.ProblemReporter;
import org.apache.logging.log4j.Logger;

public class LoggedProblemReporter extends ProblemReporter.Collector implements AutoCloseable {

    private final Logger logger;
    public LoggedProblemReporter(Logger logger) { this.logger = logger; }
    public LoggedProblemReporter(PathElement element, Logger logger) {
        super(element);
        this.logger = logger;
    }

    @Override
    public void close() {
        if(!this.isEmpty())
            this.logger.warn("[{}] Serialization Errors:\n{}",this.logger.getName(),this.getTreeReport());
    }

}
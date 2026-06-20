package io.github.lightman314.lightmanscurrency.api.helpers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    public static void writeStringToFile(File file, String string) throws IOException {
        PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
        writer.print(string);
        writer.close();
    }

}

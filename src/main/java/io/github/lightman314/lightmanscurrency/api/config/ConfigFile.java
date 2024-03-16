package io.github.lightman314.lightmanscurrency.api.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public abstract class ConfigFile {

    private static final List<ConfigFile> loadableFiles = new ArrayList<>();
    public static Iterable<ConfigFile> getAvailableFiles() { return ImmutableList.copyOf(loadableFiles); }

    private static void registerConfig(@Nonnull ConfigFile file) { loadableFiles.add(file); }

    /**
     * Load flag to ensure the configs are loaded at the correct times.
     * It is up to the config file provider to ensure that all relevant data will be loaded by the time the config gets built.
     */
    public enum LoadPhase {
        /**
         * File will not be loaded automatically.
         * Use for manually called instant reloads.
         */
        NULL,
        /**
         * File will be loaded during the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent FMLCommonSetupEvent},<br>
         * or the {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent FMLClientSetupEvent} (if flagged as client-only)
         */
        SETUP,
        /**
         * File will be loaded during the {@link net.minecraftforge.event.server.ServerStartedEvent ServerStartedEvent},<br>
         * or the {@link net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn ClientPlayerNetworkEvent.LoggingIn} (if flagged as client-only)
         */
        GAME_START
    }

    //Used to load client & common files
    public static void loadClientFiles(@Nonnull LoadPhase phase) { loadFiles(true, phase); }
    //Used to load server & common files
    public static void loadServerFiles(@Nonnull LoadPhase phase) { loadFiles(false, phase); }
    public static void loadFiles(boolean logicalClient, @Nonnull LoadPhase phase) {
        for(ConfigFile file : loadableFiles)
        {
            try {
                if(!file.isLoaded() && file.shouldReload(logicalClient) && file.loadPhase == phase)
                    file.reload();
            } catch (IllegalArgumentException | NullPointerException e) { LightmansCurrency.LogError("Error reloading config file!", e); }
        }
    }
    public static void reloadClientFiles() { reloadFiles(true); }
    public static void reloadServerFiles() { reloadFiles(false); }

    private static void reloadFiles(boolean logicalClient)
    {
        for(ConfigFile file : loadableFiles)
        {
            try {
                if(file.shouldReload(logicalClient))
                    file.reload();
            } catch (IllegalArgumentException | NullPointerException e) { LightmansCurrency.LogError("Error reloading config file!", e); }
        }
    }

    @Nonnull
    protected String getConfigFolder() { return "config"; }

    private final String fileName;
    @Nonnull
    public String getFileName() { return this.fileName; }

    private final List<Runnable> reloadListeners = new ArrayList<>();
    public final void addListener(@Nonnull Runnable listener) {
        if(!this.reloadListeners.contains(listener))
            this.reloadListeners.add(listener);
    }

    @Nonnull
    protected String getFilePath() { return this.getConfigFolder() + "/" + this.fileName + ".lcconfig"; }
    @Nonnull
    protected final File getFile() { return new File(this.getFilePath()); }

    private ConfigSection root = null;
    public final LoadPhase loadPhase;
    public final void confirmSetup() {
        if(this.root == null)
        {
            ConfigBuilder builder = new ConfigBuilder();
            this.setup(builder);
            this.root = builder.build(this);
        }
    }

    protected final void forEach(@Nonnull Consumer<ConfigOption<?>> action) { this.confirmSetup(); this.root.forEach(action); }
    @Nonnull
    public final Map<String,ConfigOption<?>> getAllOptions()
    {
        this.confirmSetup();
        Map<String,ConfigOption<?>> results = new HashMap<>();
        this.collectOptionsFrom(this.root, results);
        return ImmutableMap.copyOf(results);
    }

    private void collectOptionsFrom(@Nonnull ConfigSection section, @Nonnull Map<String,ConfigOption<?>> resultMap)
    {
        //Collect options from this section
        section.options.forEach((key,option) -> resultMap.put(section.fullNameOfChild(key),option));
        //Collect options from child sections
        section.sectionsInOrder.forEach(s -> collectOptionsFrom(s, resultMap));
    }

    @Nullable
    protected final ConfigSection findSection(@Nonnull String sectionName)
    {
        String[] subSections = sectionName.split("\\.");
        ConfigSection currentSection = this.root;
        for(String ss : subSections)
        {
            if(currentSection.sections.containsKey(ss))
                currentSection = currentSection.sections.get(ss);
            else
                return null;
        }
        return currentSection;
    }

    protected ConfigFile(@Nonnull String fileName) { this(fileName, LoadPhase.SETUP); }
    protected ConfigFile(@Nonnull String fileName, @Nonnull LoadPhase loadPhase) {
        this.fileName = fileName;
        this.loadPhase = loadPhase;
        registerConfig(this);
    }


    public boolean isClientOnly() { return false; }
    public boolean isServerOnly() { return false; }

    protected abstract void setup(@Nonnull ConfigBuilder builder);

    public final boolean shouldReload(boolean isLogicalClient) {
        if(this.isClientOnly() && !isLogicalClient)
            return false;
        if(this.isServerOnly() && isLogicalClient)
            return false;
        return this.canReload(isLogicalClient);
    }

    protected boolean canReload(boolean isLogicalClient) { return true; }

    private boolean reloading = false;
    private boolean loaded = false;
    public boolean isLoaded() { return this.loaded; }

    public final void reload()
    {

        if(this.reloading)
            return;

        this.reloading = true;

        try {
            LightmansCurrency.LogInfo("Reloading " + this.getFilePath());

            this.confirmSetup();

            List<String> lines = this.readLines();

            //Clear local files
            this.forEach(ConfigOption::clear);

            ConfigSection currentSection = this.root;

            for(String line : lines)
            {
                String cleanLine = cleanStartingWhitespace(line);
                if(cleanLine.startsWith("#")) //Ignore Comments
                    continue;
                int equalIndex = cleanLine.indexOf('=');
                if(equalIndex > 0)
                {
                    String optionName = cleanLine.substring(0, equalIndex);
                    String optionValue = "";
                    if(equalIndex < cleanLine.length() - 1)
                        optionValue = cleanLine.substring(equalIndex + 1);
                    if(currentSection.options.containsKey(optionName))
                        currentSection.options.get(optionName).load(optionValue, ConfigOption.LoadSource.FILE);
                    else
                        LightmansCurrency.LogWarning("Option " + currentSection.fullName() + "." + optionName + " found in the file, but is not present in the config setup!");
                    continue;
                }
                if(cleanLine.startsWith("["))
                {
                    String fullyCleaned = ConfigOption.cleanWhitespace(cleanLine);
                    if(fullyCleaned.endsWith("]"))
                    {
                        String section = fullyCleaned.substring(1,fullyCleaned.length() - 1);
                        ConfigSection query = this.findSection(section);
                        if(query != null)
                            currentSection = query;
                        else
                        {
                            LightmansCurrency.LogWarning("Line " + (lines.indexOf(line) + 1) + " of " + this.fileName + " contained a section (" + section + ") that is not present in this config!");
                            currentSection = this.root;
                        }
                    }
                    else
                        LightmansCurrency.LogWarning("Line " + (lines.indexOf(line) + 1) + " of config '" + this.fileName + "' is missing the ']' for the section label!");
                }
            }

            this.getAllOptions().forEach((id,option) -> {
                if(!option.isLoaded())
                {
                    option.loadDefault();
                    LightmansCurrency.LogWarning("Option " + id + " was missing from the config. Default value will be used instead.");
                }
            });

            this.loaded = true;

            this.writeToFile();

            this.afterReload();

            for(Runnable l : this.reloadListeners)
                l.run();
        } catch (Throwable e) {
            //Flag as no longer reloading if an error is thrown while reloading
            this.reloading = false;
            throw e;
        }

        this.reloading = false;

    }

    @Nonnull
    public static String cleanStartingWhitespace(@Nonnull String line)
    {
        for(int i = 0; i < line.length(); ++i)
        {
            char c = line.charAt(i);
            if(!Character.isWhitespace(c))
                return line.substring(i);
        }
        return line;
    }

    private List<String> readLines() {
        File file = this.getFile();
        if(!file.exists())
            return new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String line;
            while((line = br.readLine()) != null)
                lines.add(line);
            br.close();
            return lines;
        } catch (IOException e) {
            LightmansCurrency.LogError("Error loading config file '" + file.getPath() + "'!",e);
            return new ArrayList<>();
        }
    }

    public final void onOptionChanged(@Nonnull ConfigOption<?> option)
    {
        this.writeToFile();
        this.afterOptionChanged(option);
    }

    public final void writeToFile() {
        File file = this.getFile();
        try {
            if(!file.exists())
            {
                File folder = new File(file.getParent());
                if(!folder.exists())
                    folder.mkdirs();

                if(!file.createNewFile())
                {
                    LightmansCurrency.LogError("Unable to create " + this.fileName + "!");
                    return;
                }
            }

            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {

                this.confirmSetup();

                this.writeSection(writer, this.root);

            }

        } catch (IOException | SecurityException e) { LightmansCurrency.LogError("Error modifying " + this.fileName + "!", e); }
    }

    private void writeSection(@Nonnull PrintWriter writer, @Nonnull ConfigSection section) {
        //Write prefix if not the root section
        if(section.parent != null)
        {
            Consumer<String> w = section.parent.lineConsumer(writer);
            writeComments(section.comments, w);
            w.accept("[" + section.fullName() + "]");
        }
        //Write Options
        Consumer<String> w = section.lineConsumer(writer);
        section.optionsInOrder.forEach(pair -> pair.getSecond().write(pair.getFirst(),w));
        //Write sub-sections
        section.sectionsInOrder.forEach(s -> this.writeSection(writer,s));
    }

    public static Consumer<String> lineConsumer(@Nonnull PrintWriter writer, int depth) {
        return s -> writer.println("\t".repeat(Math.max(0, depth)) + s);
    }

    public static void writeComments(@Nonnull List<String> comments, @Nonnull Consumer<String> writer) {
        for(String c : comments)
        {
            for(String c2 : c.split("\n"))
                writer.accept("#" + c2);
        }
    }

    protected void afterReload() {}

    protected void afterOptionChanged(@Nonnull ConfigOption<?> option) {}

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static final class ConfigBuilder
    {

        private final ConfigSectionBuilder root = new ConfigSectionBuilder("root", 0, null);
        private ConfigSection build(ConfigFile file) { return this.root.build(null, file); }
        private ConfigBuilder() {}

        private final List<String> comments = new ArrayList<>();

        private ConfigSectionBuilder currentSection = this.root;

        public ConfigBuilder push(String newSection) {
            if(invalidName(newSection))
                throw new IllegalArgumentException("Illegal section name '" + newSection + "'!");
            if(this.currentSection.sections.containsKey(newSection))
                this.currentSection = this.currentSection.sections.get(newSection);
            else
                this.currentSection = this.currentSection.addChild(newSection);

            this.currentSection.comments.addAll(this.comments);
            this.comments.clear();
            return this;
        }

        public static boolean invalidName(String name) {
            if(name.equals("root"))
                return false;
            for(int i = 0; i < name.length(); ++i)
                if(!validNameChar(name.charAt(i)))
                    return true;
            return false;
        }

        public static boolean validNameChar(char c) { return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c >= 'A' && c <= 'Z'; }

        public ConfigBuilder pop() {
            if(this.currentSection == this.root)
                throw new IllegalArgumentException("Cannot pop the builder when we're already at root level!");
            this.currentSection = this.currentSection.parent;
            return this;
        }

        public ConfigBuilder comment(String... comment) { this.comments.addAll(ImmutableList.copyOf(comment)); return this; }

        public ConfigBuilder add(@Nonnull String optionName, @Nonnull ConfigOption<?> option) {
            if(invalidName(optionName))
                throw new IllegalArgumentException("Illegal option name '" + optionName + "'!");
            if(this.currentSection == null)
                this.currentSection = this.root;
            if(this.currentSection.options.containsKey(optionName))
            {
                LightmansCurrency.LogError("Duplicate option '" + this.currentSection.fullNameOfChild(optionName) + "'!\nDuplicate option will be ignored!");
                return this;
            }
            this.currentSection.addOption(optionName, option);
            option.setComments(this.comments);
            this.comments.clear();
            return this;
        }

    }

    private static final class ConfigSection
    {
        private final ConfigSection parent;
        private final int depth;
        private final String name;
        private String fullName()
        {
            if(this.parent != null)
                return this.parent.fullNameOfChild(this.name);
            return this.name;
        }
        private String fullNameOfChild(@Nonnull String childName)
        {
            if(this.parent == null)
                return childName;
            return this.fullName() + "." + childName;
        }
        private final List<String> comments;
        private final List<ConfigSection> sectionsInOrder;
        private final Map<String,ConfigSection> sections;
        private final List<Pair<String,ConfigOption<?>>> optionsInOrder;
        private final Map<String,ConfigOption<?>> options;

        void forEach(@Nonnull Consumer<ConfigOption<?>> action) {
            this.optionsInOrder.forEach(p -> action.accept(p.getSecond()));
            this.sectionsInOrder.forEach(s -> s.forEach(action));
        }

        Consumer<String> lineConsumer(@Nonnull PrintWriter writer) { return ConfigFile.lineConsumer(writer,this.depth); }

        private ConfigSection(ConfigSectionBuilder builder, ConfigSection parent, ConfigFile file) {
            this.name = builder.name;
            this.depth = builder.depth;
            this.parent = parent;
            this.comments = ImmutableList.copyOf(builder.comments);
            this.optionsInOrder = ImmutableList.copyOf(builder.optionsInOrder);
            this.options = ImmutableMap.copyOf(builder.options);
            this.options.forEach((key,option) -> option.init(file, key, this.fullNameOfChild(key)));
            List<ConfigSection> temp1 = new ArrayList<>();
            builder.sectionsInOrder.forEach(b -> temp1.add(b.build(this,file)));
            this.sectionsInOrder = ImmutableList.copyOf(temp1);
            Map<String,ConfigSection> temp2 = new HashMap<>();
            this.sectionsInOrder.forEach(section -> temp2.put(section.name, section));
            this.sections = ImmutableMap.copyOf(temp2);
        }
    }

    private static final class ConfigSectionBuilder
    {
        private final ConfigSectionBuilder parent;
        private final String name;
        private String fullName()
        {
            if(this.parent != null)
                return this.parent.fullNameOfChild(this.name);
            return this.name;
        }
        private String fullNameOfChild(@Nonnull String childName)
        {
            if(this.parent == null)
                return childName;
            return this.fullName() + "." + childName;
        }
        private final int depth;
        private final List<String> comments = new ArrayList<>();
        private final List<ConfigSectionBuilder> sectionsInOrder = new ArrayList<>();
        private final Map<String,ConfigSectionBuilder> sections = new HashMap<>();
        private final List<Pair<String,ConfigOption<?>>> optionsInOrder = new ArrayList<>();
        private final Map<String,ConfigOption<?>> options = new HashMap<>();
        private ConfigSectionBuilder(@Nonnull String name, int depth, ConfigSectionBuilder parent) { this.name = name; this.depth = depth; this.parent = parent; }

        private void addOption(@Nonnull String name, @Nonnull ConfigOption<?> option)
        {
            this.optionsInOrder.add(Pair.of(name,option));
            this.options.put(name,option);
        }
        private ConfigSectionBuilder addChild(@Nonnull String name)
        {
            ConfigSectionBuilder builder = new ConfigSectionBuilder(name, this.depth + 1, this);
            this.sectionsInOrder.add(builder);
            this.sections.put(name,builder);
            return builder;
        }
        private ConfigSection build(@Nullable ConfigSection parent, @Nonnull ConfigFile file) { return new ConfigSection(this, parent, file); }
    }

}

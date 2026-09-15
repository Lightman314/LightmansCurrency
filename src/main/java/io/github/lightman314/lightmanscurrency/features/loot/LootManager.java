package io.github.lightman314.lightmanscurrency.features.loot;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringListOption;
import io.github.lightman314.lightmanscurrency.api.loot.DefaultLootConfigEvent;
import io.github.lightman314.lightmanscurrency.api.loot.modifiers.IBonusLootModifier;
import io.github.lightman314.lightmanscurrency.api.loot.LootAPI;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.ChestPoolLevel;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.EntityPoolLevel;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public final class LootManager implements LootAPI {

    public static final ContextKeySet ENTITY_PARAMS = new ContextKeySet.Builder().optional(LootContextParams.ATTACKING_ENTITY).required(LootContextParams.DAMAGE_SOURCE).build();

    private boolean initialized = false;
    private LootManager() {}

    public static final LootManager INSTANCE = new LootManager();

    private final Set<IBonusLootModifier> modifiers = new HashSet<>();

    public void init() {
        if(this.initialized)
            return;
        LCConfig.COMMON.addListener(LootManager::debugLootConfigs);
        LootContextParamSets.REGISTRY.put(LCApi.id("entity_addon"),ENTITY_PARAMS);
        this.initialized = true;
    }

    @Override
    public void addLootModifier(IBonusLootModifier modifier) { this.modifiers.add(modifier); }
    @Override
    public void removeLootModifier(IBonusLootModifier modifier) { this.modifiers.remove(modifier); }

    @SubscribeEvent
    private static void addDefaultEntityEntries(DefaultLootConfigEvent.Entity event) {
        switch (event.getTier()) {
            case T1 -> {
                event.addVanillaEntry("slime");
                event.addVanillaEntry("silverfish");
            }
            case T2 -> {
                event.addVanillaEntry("zombie");
                event.addVanillaEntry("skeleton");
                event.addVanillaEntry("creeper");
                event.addVanillaEntry("spider");
                event.addVanillaEntry("cave_spider");
                event.addVanillaEntry("husk");
                event.addVanillaEntry("stray");
                event.addVanillaEntry("zombie_villager");
                event.addVanillaEntry("drowned");
            }
            case T3 -> {
                event.addVanillaEntry("guardian");
                event.addVanillaEntry("elder_guardian");
                event.addVanillaEntry("phantom");
                event.addVanillaEntry("blaze");
                event.addVanillaEntry("ghast");
                event.addVanillaEntry("hoglin");
                event.addVanillaEntry("piglin_brute");
                event.addVanillaEntry("piglin");
                event.addVanillaEntry("zombified_piglin");
            }
            case T4 -> {
                event.addVanillaEntry("enderman");
                event.addVanillaEntry("shulker");
                event.addTag("raiders");
            }
            case T5 -> event.addVanillaEntry("wither_skeleton");
            case BOSS_T4 -> event.addVanillaEntry("warden");
            case BOSS_T5 -> event.addVanillaEntry("ender_dragon");
            case BOSS_T6 -> event.addVanillaEntry("wither");
        }
    }

    @SubscribeEvent
    private static void addDefaultChestEntries(DefaultLootConfigEvent.Chest event) {
        switch (event.getTier()) {
            case T1 -> {
                event.addVanillaEntry("underwater_ruin_small");
                event.addVanillaEntry("underwater_ruin_big");
            }
            case T3 -> {
                event.addVanillaEntry("jungle_temple");
                event.addVanillaEntry("nether_bridge");
                event.addVanillaEntry("simple_dungeon");
                event.addVanillaEntry("ruined_portal");
            }
            case T4 -> {
                event.addVanillaEntry("stronghold_crossing");
                event.addVanillaEntry("stronghold_corridor");
                event.addVanillaEntry("stronghold_library");
                event.addVanillaEntry("ancient_city");

            }
            case T5 -> {
                event.addVanillaEntry("buried_treasure");
                event.addVanillaEntry("bastion_hoglin_stable");
                event.addVanillaEntry("bastion_bridge");
                event.addVanillaEntry("bastion_other");
                event.addVanillaEntry("bastion_treasure");
                event.addVanillaEntry("end_city_treasure");
            }
        }
    }

    private static void debugLootConfigs() {

    }

    @SubscribeEvent
    private static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        //Don't perform logic on the client
        //Also abort here if entity drops are disabled entirely so we don't do unecessary calculations
        if(entity.level().isClientSide() || !LCConfig.COMMON.enableEntityDrops.get())
            return;

        //Also follow the mob drops game rule to be safe
        if(entity.level() instanceof ServerLevel sl && !sl.getServer().getGameRules().get(GameRules.MOB_DROPS))
            return;

        if(!LCConfig.COMMON.allowSpawnerEntityDrops.get()) {
            if(entity instanceof Mob mob && mob.getSpawnType() == EntitySpawnReason.SPAWNER)
                return;
        }

        Player player = extractPlayer(event.getSource());
        if(player instanceof FakePlayer && !LCConfig.COMMON.allowFakePlayerCoinDrops.get())
            return;

        EntityPoolLevel level = INSTANCE.getEntityPoolLevel(entity.getType());
        if(level != null)
            dropEntityLoot(entity,player,level,event.getSource());

    }

    @Nullable
    private static Player extractPlayer(DamageSource source) {
        if(source.getDirectEntity() instanceof Player p)
            return p;
        if(source.getEntity() instanceof Player p)
            return p;
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getLoot(Identifier lootTable, LootContext context) {
        List<ItemStack> results = new ArrayList<>();
        MinecraftServer server = context.getLevel().getServer();
        LootTable table = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,lootTable));
        table.getRandomItemsRaw(context,results::add);
        this.applyLootModifiers(server,results);
        return results;
    }

    @Override
    public void applyLootModifiers(MinecraftServer server,List<ItemStack> loot) {
        RandomSource random = server.overworld().getRandom();
        for(IBonusLootModifier modifier : new HashSet<>(this.modifiers)) {
            if(modifier.tryModifyLoot(random,loot))
                return;
        }
    }

    private static void dropEntityLoot(Entity entity, @Nullable Player player, EntityPoolLevel level, DamageSource damageSource) {
        //Require a valid player if the loot type isn't a boss type
        if(!level.isBoss && player == null)
            return;

        LootContext context = generateEntityContext(entity,player,damageSource);
        spawnItems(entity,INSTANCE.getLoot(level.lootTable,context));
    }

    public static LootContext generateEntityContext(Entity entity,@Nullable Player player,DamageSource damageSource) {
        if(!(entity.level() instanceof ServerLevel sl))
            throw new IllegalArgumentException("Function must be run on the server side!");
        LootParams.Builder parameterBuilder = new LootParams.Builder(sl);
        //Add the killed by player condition to the loot context
        if(player != null)
            parameterBuilder.withParameter(LootContextParams.ATTACKING_ENTITY,player);
        parameterBuilder.withParameter(LootContextParams.DAMAGE_SOURCE,damageSource);
        LootParams params = parameterBuilder.create(ENTITY_PARAMS);
        return new LootContext.Builder(params).create(Optional.empty());
    }

    private static void spawnItems(Entity entity,List<ItemStack> items) {
        Level level = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        for(ItemStack item : items)
            level.addFreshEntity(new ItemEntity(level,x,y,z,item));
    }

    @Nullable
    public ChestPoolLevel getChestPoolLevel(Identifier tableID) {
        String lootTable = tableID.toString();
        if(LCConfig.COMMON.chestDropsT1.contains(lootTable))
            return ChestPoolLevel.T1;
        if(LCConfig.COMMON.chestDropsT2.contains(lootTable))
            return ChestPoolLevel.T2;
        if(LCConfig.COMMON.chestDropsT3.contains(lootTable))
            return ChestPoolLevel.T3;
        if(LCConfig.COMMON.chestDropsT4.contains(lootTable))
            return ChestPoolLevel.T4;
        if(LCConfig.COMMON.chestDropsT5.contains(lootTable))
            return ChestPoolLevel.T5;
        if(LCConfig.COMMON.chestDropsT6.contains(lootTable))
            return ChestPoolLevel.T6;
        return null;
    }

    @Nullable
    public EntityPoolLevel getEntityPoolLevel(EntityType<?> entity) {
        if(configContainsEntity(LCConfig.COMMON.entityDropsT1,entity))
            return EntityPoolLevel.T1;
        if(configContainsEntity(LCConfig.COMMON.entityDropsT2,entity))
            return EntityPoolLevel.T2;
        if(configContainsEntity(LCConfig.COMMON.entityDropsT3,entity))
            return EntityPoolLevel.T3;
        if(configContainsEntity(LCConfig.COMMON.entityDropsT4,entity))
            return EntityPoolLevel.T4;
        if(configContainsEntity(LCConfig.COMMON.entityDropsT5,entity))
            return EntityPoolLevel.T5;
        if(configContainsEntity(LCConfig.COMMON.entityDropsT6,entity))
            return EntityPoolLevel.T6;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT1,entity))
            return EntityPoolLevel.BOSS_T1;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT2,entity))
            return EntityPoolLevel.BOSS_T2;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT3,entity))
            return EntityPoolLevel.BOSS_T3;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT4,entity))
            return EntityPoolLevel.BOSS_T4;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT5,entity))
            return EntityPoolLevel.BOSS_T5;
        if(configContainsEntity(LCConfig.COMMON.bossEntityDropsT6,entity))
            return EntityPoolLevel.BOSS_T6;
        return null;
    }

    private static boolean configContainsEntity(StringListOption option,EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if(id == null)
            return false;
        Stream<TagKey<EntityType<?>>> entityTags = type.getTags();
        for(String entry : option.get()) {
            try {
                if(entry.startsWith("#")) {
                    Identifier tagKey = Identifier.parse(entry.substring(1));
                    if(entityTags.anyMatch(tag -> tag.location().equals(tagKey)))
                        return true;
                }
                else {
                    if(entry.endsWith("*"))
                        return id.toString().startsWith(entry.substring(0,entry.length() - 1));
                    else
                        return id.toString().equals(entry);
                }
            } catch (IdentifierException ignored) {}
        }
        return false;
    }

}
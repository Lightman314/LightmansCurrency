package io.github.lightman314.lightmanscurrency.common.blockentity;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.MoneyBagItemViewer;
import io.github.lightman314.lightmanscurrency.common.blocks.MoneyBagBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.data.types.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.data.MoneyBagData;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyBagBlockEntity extends EasyBlockEntity implements IServerTicker {

    public static final int MAX_ITEM_COUNT = 9 * 64;

    public boolean shouldDropItem = true;

    private Component customName = null;

    private boolean lootTableChecked = false;
    private BlockPos savedPosition;
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed = -1;

    private final List<ItemStack> contents = new ArrayList<>();

    public final IItemHandler viewer = new MoneyBagItemViewer(this);

    public MoneyBagBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { this(ModBlockEntities.MONEY_BAG.get(),pos,state); }
    protected MoneyBagBlockEntity(@Nonnull BlockEntityType<?> type, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(type, pos, state);
    }

    public List<ItemStack> viewContents() { return InventoryUtil.copyList(this.contents); }

    public List<ItemStack> clearContents() {
        this.checkLootTable();
        List<ItemStack> result = new ArrayList<>(this.contents);
        this.contents.clear();
        //This should only be called right before the bag is destroyed, but I'm going to flag it as changed anyway just to be safe
        this.setChanged();
        return result;
    }

    public boolean isEmpty() { return this.contents.isEmpty(); }

    @Range(from = 0, to = 3)
    public int getBlockSize() { return getBlockSize(this.getTotalContentCount()); }

    @Range(from = 0, to = 3)
    public static int getBlockSize(List<ItemStack> contents) { return getBlockSize(getTotalContentCount(contents)); }

    @Range(from = 0, to = 3)
    public static int getBlockSize(int totalCount)
    {
        if(totalCount < 3 * 64)
            return 0;
        if(totalCount < 6 * 64)
            return 1;
        if(totalCount < 9 * 64)
            return 2;
        return 3;
    }

    public int getTotalContentCount() { return getTotalContentCount(this.contents); }

    public static int getTotalContentCount(List<ItemStack> contents)
    {
        int count = 0;
        for(ItemStack item : contents)
            count += item.getCount();
        return count;
    }

    public boolean tryInsertItem(ItemStack item, @Nullable Player player)
    {
        this.checkLootTable();
        if(this.getTotalContentCount() >= MAX_ITEM_COUNT)
            return false;
        if(CoinAPI.API.IsAllowedInCoinContainer(item,false))
        {
            for(ItemStack i : this.contents)
            {
                if(InventoryUtil.ItemMatches(i,item))
                {
                    i.grow(1);
                    this.onContentsChanged();
                    return true;
                }
            }
            this.contents.add(item.copyWithCount(1));
            this.onContentsChanged();
            return true;
        }
        return false;
    }

    public ItemStack removeRandomItem()
    {
        this.checkLootTable();
        ItemStack result = removeRandomItem(this.contents,this.level.random);
        if(!result.isEmpty())
            this.onContentsChanged();
        return result;
    }

    public static ItemStack removeRandomItem(List<ItemStack> contents, RandomSource random)
    {
        if(contents.isEmpty())
            return ItemStack.EMPTY;
        int totalCount = 0;
        for(ItemStack item : contents)
            totalCount += item.getCount();
        int rand = random.nextInt(totalCount);
        for(int i = 0; i < contents.size(); ++i)
        {
            ItemStack item = contents.get(i);
            rand -= item.getCount();
            if(rand < 0)
            {
                ItemStack result = item.split(1);
                if(item.isEmpty())
                    contents.remove(i);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private void onContentsChanged()
    {
        if(this.isClient())
            return;
        //Flag as changed so that it saves to file
        this.setChanged();
        //Send update packet to connected players
        BlockEntityUtil.sendUpdatePacket(this);
        //Check if we should update the block state
        BlockState state = this.getBlockState();
        int currentSize = state.getValue(MoneyBagBlock.SIZE);
        int actualSize = this.getBlockSize();
        if(currentSize != actualSize)
            this.level.setBlockAndUpdate(this.worldPosition,state.setValue(MoneyBagBlock.SIZE,actualSize));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);

        if(this.customName != null)
            tag.putString("CustomName",Component.Serializer.toJson(this.customName,lookup));

        ListTag list = new ListTag();
        for(ItemStack item : new ArrayList<>(this.contents))
        {
            if(item.isEmpty())
                continue;
            list.add(InventoryUtil.saveItemNoLimits(item,lookup));
        }
        tag.put("Contents",list);
        if(this.lootTable != null)
        {
            tag.putString("LootTable",this.lootTable.location().toString());
            if(this.lootTableSeed >= 0)
                tag.putLong("LootTableSeed",this.lootTableSeed);
            tag.put("SavedPos",TagUtil.saveBlockPos(this.savedPosition == null ? this.worldPosition : this.savedPosition));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);

        if(tag.contains("CustomName",Tag.TAG_STRING))
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"),lookup);

        this.contents.clear();
        ListTag list = tag.getList("Contents",Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            ItemStack item = InventoryUtil.loadItemNoLimits(list.getCompound(i),lookup);
            if(item.isEmpty())
                continue;
            this.contents.add(item);
        }
        if(tag.contains("LootTable"))
        {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE,VersionUtil.parseResource(tag.getString("LootTable")));
            if(tag.contains("LootTableSeed"))
                this.lootTableSeed = tag.getLong("LootTableSeed");
            if(tag.contains("SavedPos"))
                this.savedPosition = TagUtil.loadBlockPos(tag.getCompound("SavedPos"));
        }
    }

    public void loadFromItem(ItemStack moneybag)
    {
        this.setComponents(DataComponentMap.EMPTY);

        if(moneybag.has(DataComponents.CUSTOM_NAME))
            this.customName = moneybag.get(DataComponents.CUSTOM_NAME);

        //Clear components that were copied from the item by vanilla means
        MoneyBagData data = moneybag.getOrDefault(ModDataComponents.MONEY_BAG_CONTENTS,MoneyBagData.EMPTY);
        this.contents.clear();
        this.contents.addAll(InventoryUtil.copyList(data.contents()));
        this.onContentsChanged();
        //Load loot table
        if(moneybag.has(ModDataComponents.LOOT_TABLE_ENTRY))
        {
            LootTableEntry tableEntry = moneybag.get(ModDataComponents.LOOT_TABLE_ENTRY);
            this.lootTable = tableEntry.lootTable();
            this.lootTableSeed = tableEntry.seed();
            this.savedPosition = this.worldPosition;
            this.setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.isClient())
            BlockEntityUtil.requestUpdatePacket(this);
        else
            this.checkLootTable();
    }

    @Override
    public void serverTick() {
        if(!this.lootTableChecked)
            this.checkLootTable();
    }

    private void checkLootTable()
    {
        if(this.lootTableChecked)
            return;
        if(this.level instanceof ServerLevel sl && this.lootTable != null && this.savedPosition != null)
        {
            this.lootTableChecked = true;
            if(this.savedPosition.equals(this.worldPosition))
                return;
            //Spawn items from the loot table
            RegistryAccess lookup = this.level.registryAccess();
            LootTable table = lookup.registryOrThrow(Registries.LOOT_TABLE).get(this.lootTable);
            if(table == null)
            {
                this.lootTable = null;
                this.lootTableSeed = -1;
                this.savedPosition = null;
            }
            LootParams lootParams = new LootParams.Builder(sl)
                    .withParameter(LootContextParams.ORIGIN,this.worldPosition.getCenter())
                    .create(LootContextParamSets.CHEST);
            this.contents.clear();
            List<ItemStack> loot;
            if(this.lootTableSeed >= 0)
                loot = table.getRandomItems(lootParams,this.lootTableSeed);
            else
                loot = table.getRandomItems(lootParams);
            this.contents.addAll(InventoryUtil.combineQueryItems(loot));
            //Clear the Loot Table Data
            this.lootTable = null;
            this.lootTableSeed = -1;
            this.savedPosition = null;
            //Recalculate the bags size
            this.onContentsChanged();
        }
    }

    public void copyContentsTo(ItemStack item)
    {
        if(this.customName != null)
            item.set(DataComponents.CUSTOM_NAME,this.customName);

        if(!this.contents.isEmpty())
        {
            MoneyBagData data = new MoneyBagData(ImmutableList.copyOf(this.viewContents()),this.getBlockSize());
            item.set(ModDataComponents.MONEY_BAG_CONTENTS,data);
        }

        if(this.lootTable != null)
            item.set(ModDataComponents.LOOT_TABLE_ENTRY,new LootTableEntry(this.lootTable,this.lootTableSeed));

    }

}

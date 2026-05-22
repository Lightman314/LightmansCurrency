package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class AuctionPlayerStorage {

    public static final Codec<AuctionPlayerStorage> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(AuctionPlayerStorage::getOwner),
            MoneyStorage.CODEC.fieldOf("money").forGetter(AuctionPlayerStorage::getStoredCoins),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(AuctionPlayerStorage::getStoredItems),
            Codec.INT.fieldOf("winStats").forGetter(s -> s.pendingWinStats)
    ).apply(builder,AuctionPlayerStorage::new)),
            CodecHelper.oldValueLoader(AuctionPlayerStorage::loadOldData,"Auction Player Storage"));
    public static final Codec<Map<UUID,AuctionPlayerStorage>> SET_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,AuctionPlayerStorage.CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf,AuctionPlayerStorage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,AuctionPlayerStorage::getOwner,
            MoneyStorage.STREAM_CODEC,AuctionPlayerStorage::getStoredCoins,
            ItemStack.LIST_STREAM_CODEC,AuctionPlayerStorage::getStoredItems,
            AuctionPlayerStorage::new);

    private Runnable listener = () -> {};

	UUID owner;
	public UUID getOwner() { return this.owner; }
	
	private final MoneyStorage storedCoins;
	public MoneyStorage getStoredCoins() { return this.storedCoins; }
	private final List<ItemStack> storedItems;
	public List<ItemStack> getStoredItems() { return this.storedItems; }
    public int pendingWinStats;
	
	public AuctionPlayerStorage(UUID player) { this(player,new MoneyStorage(),new ArrayList<>(),0); }

    private AuctionPlayerStorage(UUID player, MoneyStorage moneyStorage, List<ItemStack> items) { this(player,moneyStorage,items,0); }
    private AuctionPlayerStorage(UUID player, MoneyStorage moneyStorage, List<ItemStack> items, int pendingWinStats)
    {
        this.owner = player;
        this.storedCoins = moneyStorage.withListener(() -> this.listener.run());
        this.storedItems = new ArrayList<>(items);
        this.pendingWinStats = pendingWinStats;
    }

    public void withListener(Runnable listener) { this.listener = listener; }

    public void setChanged() { this.listener.run(); }

    public CompoundTag save(DataContext<Tag> context) { return (CompoundTag)CODEC.encodeStart(context.ops(),this).getOrThrow(); }

    public static AuctionPlayerStorage load(CompoundTag tag, DataContext<Tag> context) { return CODEC.decode(context.ops(),tag).getOrThrow().getFirst(); }

    @SuppressWarnings("deprecation")
	private static AuctionPlayerStorage loadOldData(CompoundTag compound,HolderLookup.Provider lookup) {

        PlayerReference owner = PlayerReference.load(compound.getCompound("Owner"));

		MoneyStorage money = new MoneyStorage();
        money.load(compound.getList("StoredMoney",Tag.TAG_COMPOUND));

		ListTag itemList = compound.getList("StoredItems", Tag.TAG_COMPOUND);
        List<ItemStack> items = OldDataHelper.loadNonEmptyList(itemList,lookup);

        int pendingStats = 0;
        if(compound.contains("PendingStats"))
            pendingStats = compound.getInt("PendingStats");

        return new AuctionPlayerStorage(owner.id,money,items,pendingStats);
	}
	
	public void giveMoney(MoneyValue amount) { this.storedCoins.addValue(amount); }
	
	public void collectedMoney(Player player) { this.storedCoins.GiveToPlayer(player); }
	
	public void giveItem(ItemStack item) {
		if(!item.isEmpty())
        {
            this.storedItems.add(item);
            this.listener.run();
        }
	}
	
	public void collectItems(Player player) {
		for(ItemStack stack : this.storedItems) ItemHandlerHelper.giveItemToPlayer(player, stack);
		this.storedItems.clear();
        this.listener.run();
	}
	
}

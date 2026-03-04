package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GachaItemHandler {

    private final TraderData trader;
    public GachaItemHandler(TraderData trader) { this.trader = trader; }

    private final Map<Direction, IItemHandler> handlers = new HashMap<>();
    private IItemHandler fullHandler = null;
    public IItemHandler getHandler(Direction side)
    {
        if(!this.handlers.containsKey(side))
            this.handlers.put(side,new GachaHandler(this.trader,side));
        return this.handlers.get(side);
    }
    public IItemHandler getFullyAuthorizedHandler()
    {
        if(this.fullHandler == null)
            this.fullHandler = new AuthorizedGachaHandler(this.trader);
        return this.fullHandler;
    }

    public static IItemHandler getFullyAuthorizedHandler(GachaStorageNode node) {
        return new AuthorizedGachaHandler(node.getTrader());
    }

    private static class GachaHandler implements IItemHandler
    {
        private final TraderData trader;
        private final Supplier<GachaStorageNode> source;
        private final Direction side;
        private GachaHandler(TraderData trader, Direction side) { this(trader,() -> trader.getNode(GachaStorageNode.TYPE),side); }
        private GachaHandler(TraderData trader, Supplier<GachaStorageNode> source, Direction side) { this.trader = trader; this.source = source; this.side = side; }

        protected boolean allowsInputs() { return this.trader.findNodeValue(InputNode.TYPE, n -> n.allowInputSide(this.side),false); }
        protected boolean allowsOutputs() { return this.trader.findNodeValue(InputNode.TYPE,n -> n.allowOutputSide(this.side),false); }

        protected GachaStorage getStorage() {
            GachaStorageNode node = this.source.get();
            return node == null ? new GachaStorage(() -> 0) : node.getStorage();
        }

        @Override
        public int getSlots() { return this.getStorage().getContents().size() + 1; }

        @Override
        public ItemStack getStackInSlot(int slot) { return this.getStorage().getStackInSlot(slot); }

        @Override
        public int getSlotLimit(int slot) {
            GachaStorage storage = this.getStorage();
            ItemStack contents = storage.getStackInSlot(slot);
            int space = storage.getSpace();
            return contents.getCount() + space;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return this.allowsInputs(); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack copyStack = stack.copy();
            if(this.allowsInputs())
            {
                if(simulate)
                {
                    int inputAmount = Math.min(copyStack.getCount(),this.getStorage().getSpace());
                    copyStack.shrink(inputAmount);
                }
                else
                {
                    this.getStorage().insertItem(copyStack);
                }
            }
            return copyStack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(this.allowsOutputs())
            {
                GachaStorage storage = this.getStorage();
                if(simulate)
                {
                    ItemStack stack = storage.getStackInSlot(slot);
                    return stack.copyWithCount(Math.min(amount,stack.getCount()));
                }
                else
                {
                    return storage.removeItem(slot,amount);
                }
            }
            return ItemStack.EMPTY;
        }

    }

    private static class AuthorizedGachaHandler extends GachaHandler
    {

        private AuthorizedGachaHandler(TraderData trader) { super(trader, null); }
        private AuthorizedGachaHandler(GachaStorageNode node) { super(null,() -> node, null); }
        @Override
        protected boolean allowsInputs() { return true; }
        @Override
        protected boolean allowsOutputs() { return true; }
    }

}

package io.github.lightman314.lightmanscurrency.util;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Function;

public class DebugUtil {

	public static String getItemDebug(ItemStack item) { return item.getCount() + "x " + ForgeRegistries.ITEMS.getKey(item.getItem()); }
	public static String getContainerDebug(Container container) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < container.getContainerSize(); ++i)
		{
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty())
			{
				if(!builder.isEmpty())
					builder.append('\n');
				builder.append("Slot ").append(i).append(": ").append(getItemDebug(stack));
			}
		}
		return builder.toString();
	}

	public static String getSideText(Entity entity) { return getSideText(entity.level()); }
	public static String getSideText(Level level) { return getSideText(level.isClientSide); }
	public static String getSideText(IClientTracker tracker) { return getSideText(tracker.isClient()); }
	public static String getSideText(boolean isClient) { return isClient ? "client" : "server"; }

	public static String debugList(List<?> list) { return debugList(list,Object::toString); }
	public static <T> String debugList(List<T> list, Function<T,String> writer)
	{
		StringBuilder string = new StringBuilder().append("[");
		boolean notFirst = false;
		for(T value : list)
		{
			if(notFirst)
				string.append(",");
			string.append(writer.apply(value));
			notFirst = true;
		}
		return string.append("]").toString();
	}

}

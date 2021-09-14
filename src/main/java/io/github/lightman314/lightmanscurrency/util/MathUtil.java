package io.github.lightman314.lightmanscurrency.util;

import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;

public class MathUtil {

	/**
	 * Multiplies all parts of a Vector3f by a float
	 */
	public static Vector3f VectorMult(Vector3f vector, float num)
	{
		return new Vector3f(vector.x() * num, vector.y() * num, vector.z() * num);
	}
	
	/**
	 * Sum all of the Vector3f's together
	 */
	public static Vector3f VectorAdd(Vector3f... vectors)
	{
		float x = 0f;
		float y = 0f;
		float z = 0f;
		
		for(Vector3f vector : vectors)
		{
			x += vector.x();
			y += vector.y();
			z += vector.z();
		}
		
		return new Vector3f(x, y, z);
	}
	
	/**
	 * Restricts an integer between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static int clamp(int value, int min, int max)
	{
		if(min > max)
		{
			int temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
	public static int getHorizontalFacing(Direction dir)
	{
		switch(dir)
		{
			case WEST:
				return 1;
			case NORTH:
				return 2;
			case EAST:
				return 3;
			default:
				return 0;
		}
	}
	
}

package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.util.math.vector.Vector3f;

public class MathUtil {

	/**
	 * Multiplies all parts of a Vector3f by a float
	 */
	public static Vector3f VectorMult(Vector3f vector, float num)
	{
		return new Vector3f(vector.getX() * num, vector.getY() * num, vector.getZ() * num);
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
			x += vector.getX();
			y += vector.getY();
			z += vector.getZ();
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
	
	/**
	 * Restricts an integer between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float clamp(float value, float min, float max)
	{
		if(min > max)
		{
			float temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
	/**
	 * Restricts an integer between a min & max value
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static double clamp(double value, double min, double max)
	{
		if(min > max)
		{
			double temp = min;
			min = max;
			max = temp;
		}
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		return value;
	}
	
}

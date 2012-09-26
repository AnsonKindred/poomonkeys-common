package poomonkeys.common;

import java.util.Random;

public class TerrainGenerator 
{
	static Random r = new Random(41555777L);

	public static void generate(Terrain t) 
	{
		t.yValues[t.NUM_POINTS - 1] = r.nextFloat();
		t.yValues[0] = r.nextFloat();
		midPointDivision(0, t.NUM_POINTS - 1, 0, t);
	}

	static void midPointDivision(int start, int end, int depth, Terrain t) 
	{
		if (end <= start+1) 
		{
			return;
		}
		
		int midPoint = (end + start) / 2;
		float midY = (t.yValues[start] + t.yValues[end]) / 2;
		float depthFactor = (float) (.5 / Math.pow(2, depth));
		
		float y = midY + r.nextFloat() * 2 * depthFactor - depthFactor;
		
		y = Math.max(Math.min(1, y), 0); // Make sure y is between 0 and 1
		
		t.yValues[midPoint] = y;
		
		midPointDivision(start, midPoint, depth + 1, t);
		midPointDivision(midPoint, end, depth + 1, t);
	}
}
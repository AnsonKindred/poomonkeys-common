package poomonkeys.common;

import java.util.Random;

public class TerrainGenerator 
{
	static Random r = new Random(42565777L);

	public static void generate(Terrain t) 
	{
		t.segmentWidth = t.width / (t.NUM_POINTS-1);
		t.points[t.NUM_POINTS - 1] = r.nextFloat()*t.height;
		t.points[0] = r.nextFloat()*t.height;
		midPointDivision(0, t.NUM_POINTS - 1, 0, t);
	}

	static void midPointDivision(int start, int end, int depth, Terrain t) 
	{
		if (end <= start+1) 
		{
			return;
		}
		
		int midPoint = (end + start) / 2;
		float midY = (t.points[start] + t.points[end]) / 2;
		float depthFactor = (float) (.5 / Math.pow(2, depth));
		
		float y = midY + (r.nextFloat() * 2 * depthFactor - depthFactor)*t.height;
		
		y = Math.max(Math.min(t.height, y), 0); // Make sure y is between 0 and height
		
		t.points[midPoint] = y;
		
		midPointDivision(start, midPoint, depth + 1, t);
		midPointDivision(midPoint, end, depth + 1, t);
	}
}
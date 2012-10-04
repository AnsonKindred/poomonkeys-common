package poomonkeys.common;

import java.util.ArrayList;

public class ExplosionController extends Thread
{
	Terrain t;

	static float DIRT_SIZE = .2f;
	static ExplosionController instance = null;

	public static ExplosionController getInstance()
	{
		if (instance == null)
		{
			instance = new ExplosionController();
		}
		return instance;
	}

	public void init(Terrain t)
	{
		this.t = t;
	}

	// x and y are relative to the bottom left of the terrain
	public void explode(float x, float y, float r)
	{
		// Helpful square at click point
		Dirt test = new Dirt();
		test.x = x;
		test.y = y;
		test.width = 1;
		test.height = 1;
		t.registerDrawable(test);

		// The min and max terrain index that lies within the explosion radius
		int min_index = (int) ((x - r) / t.segmentWidth) + 1;
		int max_index = (int) ((x + r) / t.segmentWidth);

		if (max_index > t.NUM_POINTS - 1)
		{
			max_index = t.NUM_POINTS - 1;
		}
		if (min_index < 0)
		{
			min_index = 0;
		}

		// The actual x value at the min and max indexes
		float minX = min_index * t.segmentWidth;
		float maxX = max_index * t.segmentWidth;
		
		ArrayList<Dirt> dirt;
		float totalDirtVolume = 0;

		dirt = _generateDirtpoints(minX, maxX, x, y, r);
		totalDirtVolume = _collapseTerrain(min_index, max_index, x, y, r);

		// Assign a portion of the removed dirt volume to each dirt point
		float individualDirtVolume = totalDirtVolume / dirt.size();
		for (int d = 0; d < dirt.size(); d++)
		{
			Dirt dirtPoint = dirt.get(d);
			dirtPoint.volume = individualDirtVolume;
			PhysicsController.getInstance().addCollidable(dirtPoint);
		}
	}

	/*
	 * Sets terrain heights to the bottom of the explosion circle
	 */
	private float _collapseTerrain(int min_index, int max_index, float x, float y, float r)
	{
		float dirtVolume = 0;

		for (int i = min_index; i <= max_index; i++)
		{
			float xFromI = i * t.segmentWidth;
			float offset = (float) Math.sqrt(r * r - Math.pow(xFromI - x, 2));
			float tCircleY = (float) (offset + y);
			float bCircleY = (float) (y - offset);

			// checks if the land is above the bottom of the circle, otherwise
			// don't mess with it.
			if (t.points[i] > bCircleY)
			{
				if (t.points[i] > tCircleY)
				{
					dirtVolume += t.points[i] - tCircleY;
				}

				t.points[i] = bCircleY;
			}
		}
		t.buildGeometry(t.width, t.height);
		t.finalizeGeometry();

		return dirtVolume;
	}

	private ArrayList<Dirt> _generateDirtpoints(float minX, float maxX, float x, float y, float r)
	{
		ArrayList<Dirt> dirt = new ArrayList<Dirt>();

		float explosion_width = Math.abs(maxX - minX);
		int num_dirt_columns = (int) ((explosion_width + t.segmentWidth) / (2 * DIRT_SIZE));
		float gapTotal = (explosion_width + t.segmentWidth) % (2 * DIRT_SIZE);
		float gap = gapTotal / num_dirt_columns;
		float col_x = minX - t.segmentWidth/2 + DIRT_SIZE + gap/2;
		float end_x = maxX + t.segmentWidth/2 - DIRT_SIZE - gap/2;
		float EPSILON = .0001f;
		for (; col_x <= end_x + EPSILON; col_x += DIRT_SIZE * 2 + gap)
		{
			_generateDirtColumn(col_x, x, y, r, gap, dirt);
		}

		return dirt;
	}

	private float _generateDirtColumn(float col_x, float x, float y, float r, float gap, ArrayList<Dirt> dirt)
	{
		float thing = (float) (r * r - Math.pow(col_x - x, 2));
		float offset = 0;
		if(thing > 0)
		{
			offset = (float) Math.sqrt(thing);
		}
		else
		{
			offset = 0;
		}
		float tCircleY = (float) (offset + y);

		int iFromX = (int) (col_x / t.segmentWidth);
		if(iFromX < 0 || iFromX >= t.points.length-1)
		{
			return 0;
		}
		
		float xPercent = (col_x % t.segmentWidth) / t.segmentWidth;
		float p1y = t.points[iFromX];
		float p2y = t.points[iFromX + 1];
		float top = p1y + (p2y - p1y) * xPercent;
		if (top > tCircleY)
		{
			float d = 0;
			while (tCircleY + d <= top)
			{
				Dirt dirtPoint = new Dirt();
				dirtPoint.x = col_x;
				dirtPoint.y = tCircleY + d;
				float distance = (float) Math.sqrt(Math.pow(dirtPoint.x - x, 2) + Math.pow(dirtPoint.y - y, 2));
				float nx = (dirtPoint.x - x) / distance;
				float ny = (dirtPoint.y - y) / distance;
				distance += 1;
				dirtPoint.vx = nx / (distance*distance) * 5;
				dirtPoint.vy = ny / (distance*distance) * 5;
				dirtPoint.width = DIRT_SIZE * 2 + gap;
				dirtPoint.height = DIRT_SIZE * 2 + gap;
				dirt.add(dirtPoint);
				t.registerDrawable(dirtPoint);
				d += DIRT_SIZE * 2 + gap;
			}
		}

		return top - tCircleY;
	}
}

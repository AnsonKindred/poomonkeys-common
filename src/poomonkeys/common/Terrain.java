package poomonkeys.common;

import java.util.ArrayList;
import java.util.Random;
import javax.media.opengl.GL2;

public class Terrain extends Drawable
{

	static final int NUM_POINTS = 16;
	static final float DIRT_SIZE = .1f;
	static final float DIRT_VISCOSITY = 1.1f;
	float segmentWidth;
	float points[] = new float[NUM_POINTS];
	float previousPoints[] = new float[NUM_POINTS];
	float offsets[] = new float[NUM_POINTS];
	boolean needsUpdate;

	GameEngine engine;
	Renderer renderer;
	private DirtGeometry lastDirtGeometry;

	public Terrain(GameEngine e, Renderer r)
	{
		engine = e;
		renderer = r;
	}

	public void addTankRandom(Tank tank)
	{
		tank.p[0] = (float) (Math.random() * width);
		int min_index = Math.max(0, (int) ((tank.p[0] - tank.width / 2) / segmentWidth));
		int max_index = Math.min(NUM_POINTS - 1, (int) ((tank.p[0] + tank.width / 2) / segmentWidth) + 1);
		float y1 = points[min_index];
		float y2 = points[max_index];

		float average = (y1 + y2) / 2;
		for (int i = min_index; i <= max_index; i++)
		{
			points[i] = average;
		}
		update();
		tank.p[1] = average + tank.height / 2;
	}

	// explodes width 2r from x,y and above
	public void explodeRectangle(float x, float y, float r)
	{
		// The min and max terrain index that lies within the tank's radius
		int min_index = Math.max(0, (int) ((x - r) / segmentWidth) + 1);
		int max_index = Math.min(NUM_POINTS - 1, (int) ((x + r) / segmentWidth));

		// The actual x value at the min and max indexes
		float min_x = (min_index) * segmentWidth;
		float max_x = (max_index) * segmentWidth;

		int num_dirts = 0;
		float totalDirtVolume = 0;

		synchronized(GameEngine.terrainLock)
		{
			num_dirts = _generateDirtpointsRectangle(min_x, max_x, x, y, r);
			totalDirtVolume = _collapseToRectangleBottom(min_index, max_index, x, y, r);
			float individualDirtVolume = totalDirtVolume / num_dirts;
			ArrayList<Movable[]> movables = renderer.getMovables();
			Movable[] instances = movables.get(lastDirtGeometry.geometryID);
			for (int d = 0; d < num_dirts; d++)
			{
				int index = (instances.length - d - 1);
				instances[index].volume = individualDirtVolume;
			}
		}
	}

	private int _generateDirtpointsRectangle(float min_x, float max_x, float x, float y, float r)
	{
		float explosion_width = Math.abs(max_x - min_x);
		int num_dirt_columns = (int) ((explosion_width + segmentWidth) / (2 * DIRT_SIZE));
		float gapTotal = (explosion_width + segmentWidth) % (2 * DIRT_SIZE);
		float gap = gapTotal / num_dirt_columns;
		float col_x = min_x - segmentWidth / 2 + DIRT_SIZE + gap / 2;
		float end_x = max_x + segmentWidth / 2 - DIRT_SIZE - gap / 2;
		float EPSILON = .0001f;
		int count = 0;

		lastDirtGeometry = DirtGeometry.getInstance(DIRT_SIZE * 2 + gap);

		for (; col_x <= end_x + EPSILON; col_x += DIRT_SIZE * 2 + gap)
		{
			float thing = (float) (r * r - Math.pow(col_x - x, 2));
			float offset = 0;
			if (thing > 0)
			{
				offset = (float) Math.sqrt(thing);
			} else
			{
				offset = 0;
			}
			float tCircleY = (float) (offset + y);

			int iFromX = (int) (col_x / segmentWidth);
			if (iFromX < 0 || iFromX >= points.length - 1)
			{
				return 0;
			}

			float xPercent = (col_x % segmentWidth) / segmentWidth;
			float p1y = points[iFromX];
			float p2y = points[iFromX + 1];
			float top = p1y + (p2y - p1y) * xPercent;
			if (top > tCircleY)
			{
				float d = 0;
				while (tCircleY + d <= top)
				{
					renderer.addGeometryInstance(col_x, tCircleY + d, lastDirtGeometry);
					d += DIRT_SIZE * 2 + gap;
				}
				count += top - tCircleY;
			}
		}

		return count;
	}

	private float _collapseToRectangleBottom(int min_index, int max_index, float x, float y, float r)
	{
		float dirtVolume = 0;

		for (int i = min_index; i <= max_index; i++)
		{
			// change r for rectangles
			float offset = (float) r;
			float tCircleY = (float) (offset + y);
			float bCircleY = (float) (y - offset);

			// checks if the land is above the bottom of the circle, otherwise
			// don't mess with it.
			if (points[i] > bCircleY)
			{
				if (points[i] > tCircleY)
				{
					dirtVolume += points[i] - tCircleY;
				}

				points[i] = bCircleY;
				previousPoints[i] = bCircleY;
			}
		}
		buildGeometry(width, height);
		finalizeGeometry();

		return dirtVolume;
	}

	// x and y are relative to the bottom left of the terrain
	public void explodeCircle(float x, float y, float r)
	{
		// The min and max terrain index that lies within the explosion radius
		int min_index = Math.max(0, (int) ((x - r) / segmentWidth) + 1);
		int max_index = Math.min(NUM_POINTS - 1, (int) ((x + r) / segmentWidth));

		// The actual x value at the min and max indexes
		float min_x = (min_index) * segmentWidth;
		float max_x = (max_index) * segmentWidth;

		float totalDirtVolume = 0;
		int num_dirts = 0;
		synchronized(GameEngine.terrainLock)
		{
			num_dirts = _generateDirtpoints(min_x, max_x, x, y, r);
			totalDirtVolume = _collapseToCircleBottom(min_index, max_index, x, y, r);
			
			if(num_dirts == 0) 
			{
				return;
			}
			
			float individualDirtVolume = totalDirtVolume / num_dirts;

			synchronized(Renderer.instanceLock)
			{
				ArrayList<Movable[]> movables = renderer.getMovables();
				Movable[] instances = movables.get(lastDirtGeometry.geometryID);
				for (int d = 0; d < num_dirts; d++)
				{
					int index = (lastDirtGeometry.num_instances - d - 1);
					instances[index].volume = individualDirtVolume;
				}
			}
		}
	}

	private int _generateDirtpoints(float min_x, float max_x, float x, float y, float r)
	{
		float explosion_width = Math.abs(max_x - min_x);
		int num_dirt_columns = (int) ((explosion_width + segmentWidth) / (2 * DIRT_SIZE));
		float gapTotal = (explosion_width + segmentWidth) % (2 * DIRT_SIZE);
		float gap = gapTotal / num_dirt_columns;
		float col_x = min_x - segmentWidth / 2 + DIRT_SIZE + gap / 2;
		float end_x = max_x + segmentWidth / 2 - DIRT_SIZE - gap / 2;
		float EPSILON = .0001f;
		int count = 0;

		lastDirtGeometry = DirtGeometry.getInstance(DIRT_SIZE * 2 + gap);

		for (; col_x <= end_x + EPSILON; col_x += DIRT_SIZE * 2 + gap)
		{
			float thing = (float) (r * r - Math.pow(col_x - x, 2));
			float offset = 0;
			if (thing > 0)
			{
				offset = (float) Math.sqrt(thing);
			} else
			{
				offset = 0;
			}
			float tCircleY = (float) (offset + y);

			int iFromX = (int) (col_x / segmentWidth);
			if (iFromX < 0 || iFromX >= points.length - 1)
			{
				return 0;
			}

			float xPercent = (col_x % segmentWidth) / segmentWidth;
			float p1y = points[iFromX];
			float p2y = points[iFromX + 1];
			float top = p1y + (p2y - p1y) * xPercent;

			if (top > tCircleY)
			{
				float d = 0;
				while (tCircleY + d <= top)
				{
					renderer.addGeometryInstance(col_x, tCircleY + d, lastDirtGeometry);
					d += DIRT_SIZE * 2 + gap;
					count++;
				}
			}
		}

		return count;
	}

	/*
	 * Sets terrain heights to the bottom of a circle at (x, y) with radius r
	 * Also returns the total amount of land removed between the top of the
	 * circle and the current land height
	 */
	private float _collapseToCircleBottom(int min_index, int max_index, float x, float y, float r)
	{
		float dirtVolume = 0;

		for (int i = min_index; i <= max_index; i++)
		{
			float xFromI = i * segmentWidth;
			float offset = (float) Math.sqrt(r * r - Math.pow(xFromI - x, 2));
			float tCircleY = (float) (offset + y);
			float bCircleY = (float) (y - offset);

			// checks if the land is above the bottom of the circle, otherwise
			// don't mess with it.
			if (points[i] > bCircleY)
			{
				if (points[i] > tCircleY)
				{
					dirtVolume += points[i] - tCircleY;
				}

				points[i] = bCircleY;
				previousPoints[i] = bCircleY;
			}
		}

		buildGeometry(width, height);
		finalizeGeometry();

		return dirtVolume;
	}

	public void update()
	{
		for (int i = 0; i < offsets.length; i++)
		{
			previousPoints[i] = points[i];
			points[i] += offsets[i];
			offsets[i] = 0;
		}

		buildGeometry(width, height);
		finalizeGeometry();
	}

	public void buildGeometry(float viewWidth, float viewHeight)
	{
		vertices = new float[NUM_POINTS * 3];
		for (int i = 0; i < NUM_POINTS; i++)
		{
			vertices[i * 3] = segmentWidth * i;
			;
			vertices[i * 3 + 1] = points[i];
			vertices[i * 3 + 2] = 0;
		}
		drawMode = GL2.GL_LINE_STRIP;
	}

	/*
	 * public void dropDirt(float x, float y) { PhysicsController
	 * physicsController = PhysicsController.getInstance(this); Dirt dirtPoint =
	 * new Dirt(); dirtPoint.p[0] = x; dirtPoint.p[1] = y; dirtPoint.v[1] = -1;
	 * dirtPoint.width = DIRT_SIZE; dirtPoint.height = DIRT_SIZE;
	 * dirtPoint.volume = 1; physicsController.addCollidable(dirtPoint);
	 * registerDrawable(dirtPoint); }
	 */
}
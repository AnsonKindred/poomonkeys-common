package poomonkeys.common;

import java.util.ArrayList;
import javax.media.opengl.GL2;

public class Terrain extends Drawable
{

	static final int NUM_POINTS = 128;
	static final float DIRT_SIZE = .1f;
	static final float DIRT_VISCOSITY = 1.5f;
	float segmentWidth;
	float points[] = new float[NUM_POINTS];
	float previousPoints[] = new float[NUM_POINTS];
	float offsets[] = new float[NUM_POINTS];
	boolean needsUpdate;

	public Terrain() 
	{
	}
	
	public void addTankRandom(Tank tank)
	{
		tank.p[0] = (float) (Math.random()*width);
		int min_index = Math.max(0, (int) ((tank.p[0] - tank.width/2) / segmentWidth));
		int max_index = Math.min(NUM_POINTS - 1, (int) ((tank.p[0] + tank.width/2) / segmentWidth)+1);
		float y1 = points[min_index];
		float y2 = points[max_index];
		
		float average = (y1+y2)/2;
		for(int i = min_index; i <= max_index; i++)
		{
			points[i] = average;
		}
		update();
		tank.p[1] = average + tank.height/2;
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
		ArrayList<Dirt> dirt = null;
		synchronized(this)
		{
			dirt = _generateDirtpoints(min_x, max_x, x, y, r);
			totalDirtVolume = _collapseToCircleBottom(min_index, max_index, x, y, r);
		}
		// Assign a portion of the removed dirt volume to each dirt point
		float individualDirtVolume = totalDirtVolume / dirt.size();
		for (int d = 0; d < dirt.size(); d++)
		{
			Dirt dirtPoint = dirt.get(d);
			dirtPoint.volume = individualDirtVolume;
		}
		PhysicsController physicsController = PhysicsController.getInstance(this);
		physicsController.addCollidables((ArrayList<? extends Drawable>)dirt);
		float[] f = new float[3];
		f[0] = x; f[1] = y; f[2] = 10;
		physicsController.pointForces.add(f);
	}

	private ArrayList<Dirt> _generateDirtpoints(float min_x, float max_x, float x, float y, float r)
	{
		ArrayList<Dirt> dirt = new ArrayList<Dirt>();
		
		float explosion_width = Math.abs(max_x - min_x);
		int num_dirt_columns = (int) ((explosion_width + segmentWidth) / (2 * DIRT_SIZE));
		float gapTotal = (explosion_width + segmentWidth) % (2 * DIRT_SIZE);
		float gap = gapTotal / num_dirt_columns;
		float col_x = min_x - segmentWidth/2 + DIRT_SIZE + gap/2;
		float end_x = max_x + segmentWidth/2 - DIRT_SIZE - gap/2;
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

		int iFromX = (int) (col_x / segmentWidth);
		if(iFromX < 0 || iFromX >= points.length-1)
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
				Dirt dirtPoint = new Dirt();
				dirtPoint.p[0] = col_x;
				dirtPoint.p[1] = tCircleY + d;
				dirtPoint.setWidth(DIRT_SIZE * 2 + gap);
				dirtPoint.setHeight(DIRT_SIZE * 2 + gap);
				dirt.add(dirtPoint);
				registerDrawable(dirtPoint);
				d += DIRT_SIZE * 2 + gap;
			}
		}

		return top - tCircleY;
	}
	
	/*
	 * Sets terrain heights to the bottom of a circle at (x, y) with radius r
	 * Also returns the total amount of land removed between the top of the circle
	 * and the current land height
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
			if(points[i] > bCircleY)
			{
				if(points[i] > tCircleY)
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
		vertices = new float[NUM_POINTS*3];
		for(int i = 0; i < NUM_POINTS; i++)
		{
			vertices[i*3] = segmentWidth*i;;
			vertices[i*3+1] = points[i];
			vertices[i*3+2] = 0;
		}
		drawMode = GL2.GL_LINE_STRIP;
	}

	public void dropDirt(float x, float y) 
	{
		PhysicsController physicsController = PhysicsController.getInstance(this);
		Dirt dirtPoint = new Dirt();
		dirtPoint.p[0] = x;
		dirtPoint.p[1] = y;
		dirtPoint.v[1] = -1;
		dirtPoint.width = DIRT_SIZE;
		dirtPoint.height = DIRT_SIZE;
		dirtPoint.volume = 1;
		physicsController.addCollidable(dirtPoint);
		registerDrawable(dirtPoint);
	}
}
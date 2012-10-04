package poomonkeys.common;

import java.util.ArrayList;
import javax.media.opengl.GL2;

public class Terrain extends Drawable
{

	static final int NUM_POINTS = 256;
	static final float DIRT_SIZE = .2f;
	float segmentWidth;
	float points[] = new float[NUM_POINTS];
	float previousPoints[] = new float[NUM_POINTS];
	float offsets[] = new float[NUM_POINTS];

	public Terrain() 
	{
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
		baseGeometry = new float[NUM_POINTS*3];
		for(int i = 0; i < NUM_POINTS; i++)
		{
			baseGeometry[i*3] = segmentWidth*i;;
			baseGeometry[i*3+1] = points[i];
			baseGeometry[i*3+2] = 0;
		}
		drawMode = GL2.GL_LINE_STRIP;
	}
	
	public void addTankRandom(Tank tank)
	{
		tank.p.x = (float) (Math.random()*width);
		int min_index = Math.max(0, (int) ((tank.p.x - tank.width/2) / segmentWidth));
		int max_index = Math.min(NUM_POINTS - 1, (int) ((tank.p.x + tank.width/2) / segmentWidth)+1);
		float y1 = points[min_index];
		float y2 = points[max_index];
		
		float average = (y1+y2)/2;
		for(int i = min_index; i <= max_index; i++)
		{
			points[i] = average;
		}
		update();
		tank.p.y = average + tank.height/2;
	}
	
	// x and y are relative to the bottom left of the terrain
	public void explodeCircle(float x, float y, float r)
	{
		// Helpful square at click point
		/*Dirt test = new Dirt();
		test.p.x = x;
		test.p.y = y;
		test.width = 1;
		test.height = 1;
		t.registerDrawable(test);*/
		
		// The min and max terrain index that lies within the explosion radius
		int min_index = Math.max(0, (int) ((x - r) / segmentWidth) + 1);
		int max_index = Math.min(NUM_POINTS - 1, (int) ((x + r) / segmentWidth));

		// The actual x value at the min and max indexes
		float min_x = min_index * segmentWidth;
		float max_x = max_index * segmentWidth;
		
		float totalDirtVolume = 0;
		ArrayList<Dirt> dirt = _generateDirtpoints(min_x, max_x, x, y, r);
		totalDirtVolume = _collapseToCircleBottom(min_index, max_index, x, y, r);

		// Assign a portion of the removed dirt volume to each dirt point
		float individualDirtVolume = totalDirtVolume / dirt.size();
		for (int d = 0; d < dirt.size(); d++)
		{
			Dirt dirtPoint = dirt.get(d);
			dirtPoint.volume = individualDirtVolume;
			PhysicsController.getInstance(this).addCollidable(dirtPoint);
		}
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
				dirtPoint.p.x = col_x;
				dirtPoint.p.y = tCircleY + d;
				float distance = (float) Math.sqrt(Math.pow(dirtPoint.p.x - x, 2) + Math.pow(dirtPoint.p.y - y, 2));
				float nx = (dirtPoint.p.x - x) / distance;
				float ny = (dirtPoint.p.y - y) / distance;
				distance += 1;
				dirtPoint.v.x = nx / (distance*distance) * 5;
				dirtPoint.v.y = ny / (distance*distance) * 5;
				dirtPoint.width = DIRT_SIZE * 2 + gap;
				dirtPoint.height = DIRT_SIZE * 2 + gap;
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
			}
		}
		buildGeometry(width, height);
		finalizeGeometry();

		return dirtVolume;
	}
}
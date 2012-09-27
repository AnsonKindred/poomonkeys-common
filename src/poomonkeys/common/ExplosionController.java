package poomonkeys.common;

import java.util.ArrayList;

public class ExplosionController extends Thread
{
	ArrayList<ArrayList<Dirt>> explosions = new ArrayList<ArrayList<Dirt>>();
	Terrain t;

	static float DIRT_SIZE = .2f;
	
	static ExplosionController instance = null;
	
	public static ExplosionController getInstance()
	{
		if(instance == null)
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
		
		
		int min_index = (int) ((x - r) / t.segmentWidth) + 1;
		int max_index = (int) ((x + r) / t.segmentWidth);
		
		
		if (max_index > t.NUM_POINTS-1) 
		{
			max_index = t.NUM_POINTS-1;
		}
		if (min_index < 0) 
		{
			min_index = 0;
		}

		float minX = (min_index / (t.NUM_POINTS-1.f)) * t.width;
		float maxX = (max_index / (t.NUM_POINTS-1.f)) * t.width;
		
		_generateDirtpoints(minX, maxX, x, y, r);
		_collapseTerrain(min_index, max_index, x, y, r);
		
		
		if(!this.isAlive())
		{
			this.start();
		}
	}
	
	private void _collapseTerrain(int min_index, int max_index, float x, float y, float r)
	{
		// Sets the terrain heights to the bottom of the explosion circle
		for (int i = min_index; i <= max_index; i++) 
		{
			float xFromI = i*t.segmentWidth;
			float offset = (float) Math.sqrt(r*r - Math.pow(xFromI - x, 2));
			float bCircleY = (float) (y - offset);
			
			// checks if the land is above the bottom of the circle, otherwise
			// don't mess with it.
			if (t.points[i] > bCircleY) 
			{
				t.points[i] = bCircleY;
			}
		}
		t.buildGeometry(t.width, t.height);
		t.finalizeGeometry();
	}
	
	private void _generateDirtpoints(float minX, float maxX, float x, float y, float r)
	{
		ArrayList<Dirt> dirt = new ArrayList<Dirt>();
		
		float explosion_width = Math.abs(maxX - minX);
		int num_dirt_columns = (int)(1 + explosion_width/(2*DIRT_SIZE) );
		float gap = explosion_width%(2*DIRT_SIZE) / (num_dirt_columns-1);
		float totalDirtVolume = 0;
		for (float col_x = minX; col_x <= maxX+.0001; col_x += DIRT_SIZE*2+gap) 
		{
			if(col_x > maxX)
			{
				col_x = maxX;
			}
			totalDirtVolume += _generateDirtColumn(col_x, x, y, r, gap, dirt);
		}
		
		float individualDirtVolume = totalDirtVolume / dirt.size();
		for(int d = 0; d < dirt.size(); d++)
		{
			dirt.get(d).volume = individualDirtVolume;
		}
		
		explosions.add(dirt);
	}
	
	private float _generateDirtColumn(float col_x, float x, float y, float r, float gap, ArrayList<Dirt> dirt)
	{
		float offset = (float) Math.sqrt(r*r - Math.pow(col_x - x, 2));
		float tCircleY = (float) (offset + y);
		int iFromX = (int) ((t.NUM_POINTS-1)*col_x/t.width);
		
		float xPercent = (col_x - iFromX*t.segmentWidth)/t.segmentWidth;
		float p1y = t.points[iFromX];
		float p2y = t.points[iFromX+1];
		float top = p1y + (p2y - p1y)*xPercent;
		if (top > tCircleY) 
		{
			float d = 0;
			while(tCircleY + d <= top)
			{
				Dirt dirtPoint = new Dirt();
				dirtPoint.x = col_x;
				dirtPoint.y = tCircleY+d;
				dirtPoint.width = DIRT_SIZE*2;
				dirtPoint.height = DIRT_SIZE*2;
				dirt.add(dirtPoint);
				t.registerDrawable(dirtPoint);
				d += DIRT_SIZE*2+gap;
			}
		}
		
		return top-tCircleY;
	}
	
	public void run()
	{
		while(!explosions.isEmpty())
		{
			for(int e = 0; e < explosions.size(); e++)
			{
				ArrayList<Dirt> dirt = explosions.get(e);
				for(int i = 0; i < dirt.size(); i++)
				{
					Dirt d = dirt.get(i);
					d.vy += -.01;
					d.x += d.vx;
					d.y += d.vy;
				}
			}
			
			try
			{
				Thread.currentThread().sleep(15);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}

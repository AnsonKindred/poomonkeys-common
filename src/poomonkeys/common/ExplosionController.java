package poomonkeys.common;

import java.util.ArrayList;

public class ExplosionController
{
	static ArrayList<Dirt> dirt = new ArrayList<Dirt>();

	static float DIRT_SIZE = .1f;
	
	public static void explode(Terrain t, float x, float y, float r, Renderer renderer)
	{
		Dirt test = new Dirt();
		test.x = x;
		test.y = y;
		test.width = 1;
		test.height = 1;
		
		dirt.add(test);
		renderer.registerDrawable(test);
		
		float viewWidth = renderer.getViewWidth();
		float viewHeight = renderer.getViewHeight();
		
		int minX = (int) ((x+viewWidth/2 - r) / viewWidth * (t.NUM_POINTS-1))+1;
		int maxX = (int) ((x+viewWidth/2 + r) / viewWidth * (t.NUM_POINTS-1));
		
		if (maxX > t.NUM_POINTS-1) 
		{
			maxX = t.NUM_POINTS-1;
		}
		if (minX < 0) 
		{
			minX = 0;
		}

		float minViewX = (minX / (t.NUM_POINTS-1.f)) * viewWidth - viewWidth/2;
		float maxViewX = (maxX / (t.NUM_POINTS-1.f)) * viewWidth - viewWidth/2;
		
		for (float dirt_x = minViewX; dirt_x <= maxViewX; dirt_x += DIRT_SIZE) 
		{
			float offset = (float) Math.sqrt(r*r - Math.pow(dirt_x - x, 2));
			float tCircleY = (float) (offset + y);
			int iFromX = (int) ((t.NUM_POINTS-1)*(dirt_x+viewWidth/2)/viewWidth);
			if (t.yValues[iFromX]*viewHeight+t.y > tCircleY) 
			{
				float j = 0;
				while(tCircleY + j <= t.yValues[iFromX]*viewHeight+t.y)
				{
					Dirt dirtPoint = new Dirt();
					dirtPoint.x = dirt_x;
					dirtPoint.y = tCircleY+j;
					dirtPoint.width = DIRT_SIZE;
					dirtPoint.height = DIRT_SIZE;
					dirt.add(dirtPoint);
					renderer.registerDrawable(dirtPoint);
					j += DIRT_SIZE;
				}
			}
		}
		
		for (int i = minX; i <= maxX; i++) 
		{
			float xFromI = (i/(t.NUM_POINTS-1.f))*viewWidth+t.x;
			float offset = (float) Math.sqrt(r*r - Math.pow(xFromI - x, 2));
			float bCircleY = (float) (y - offset);
			if (t.yValues[i]*viewHeight+t.y > bCircleY) 
			{
				t.yValues[i] = (bCircleY+viewHeight/2)/viewHeight;
			}
		}
		t.buildGeometry(viewWidth, viewHeight);
		t.finalizeGeometry();
	}
}

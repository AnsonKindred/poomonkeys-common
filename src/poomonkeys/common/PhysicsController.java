package poomonkeys.common;

import java.util.ArrayList;

public class PhysicsController extends Thread
{
	public static final float GRAVITY = -.01f;
	
	private ArrayList<Drawable> collidables = new ArrayList<Drawable>();
	private Terrain t;
	public ArrayList<Point2D> globalForces = new ArrayList<Point2D>();
	public ArrayList<Point2D> permanentGlobalForces = new ArrayList<Point2D>();
	public ArrayList<PointForce> pointForces = new ArrayList<PointForce>();
	public ArrayList<PointForce> permanentPointForces = new ArrayList<PointForce>();

	private static PhysicsController instance = null;

	public static PhysicsController getInstance(Terrain t)
	{
		if(instance == null)
		{
			instance = new PhysicsController(t);
			instance.start();
		}
		else
		{
			instance.t = t;
		}
		
		return instance;
	}
	
	public static PhysicsController getInstance()
	{
		if(instance == null)
		{
			instance = new PhysicsController();
			instance.start();
		}
		
		return instance;
	}
	
	public PhysicsController()
	{
	}
	
	public PhysicsController(Terrain t)
	{
		this.t = t;
	}
	
	/**
	 * Animates the dirt points and handles adding them back to the terrain
	 */
	public void run()
	{
		while (true)
		{
			if(collidables.isEmpty() || t == null)
			{
				try
				{
					Thread.currentThread().sleep(20);
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				continue;
			}
			synchronized (collidables) 
			{ 
				for (int i = collidables.size()-1; i >= 0; i--)
				{
					Drawable d = collidables.get(i);
					
					Point2D totalForce = new Point2D();
					for(int f = 0; f < permanentGlobalForces.size(); f++)
					{
						Point2D force = permanentGlobalForces.get(f);
						totalForce.x += force.x;
						totalForce.y += force.y;
					}
					
					for(int f = 0; f < globalForces.size(); f++)
					{
						Point2D force = globalForces.get(f);
						totalForce.x += force.x;
						totalForce.y += force.y;
					}
					
					for(int f = 0; f < permanentPointForces.size(); f++)
					{
						PointForce force = permanentPointForces.get(f);
						float distance_factor = (float) (1 / Math.pow(1 + VectorUtil.distance(force, d), 2));
						Point2D forceDirection = new Point2D(d.x-force.x, d.y-force.y);
						VectorUtil.scaleTo2D(forceDirection, force.magnitude);
						totalForce.x += forceDirection.x * distance_factor;
						totalForce.y += forceDirection.y * distance_factor;
					}
					
					for(int f = 0; f < pointForces.size(); f++)
					{
						PointForce force = pointForces.get(f);
						float distance_factor = (float) (1 / Math.pow(1 + VectorUtil.distance(force, d), 2));
						Point2D forceDirection = new Point2D(d.x-force.x, d.y-force.y);
						VectorUtil.scaleTo2D(forceDirection, force.magnitude);
						totalForce.x += forceDirection.x * distance_factor;
						totalForce.y += forceDirection.y * distance_factor;
					}
					
					d.a.x = totalForce.x/d.m;
					d.a.y = totalForce.y/d.m;
					
					d.v.x += d.a.x;
					d.v.y += d.a.y;
					
					d.v.y += GRAVITY;
					
					d.x += d.v.x;
					d.y += d.v.y;
					
					int iFromLeftX = (int) ((d.x-d.width/2)/t.segmentWidth);
					int iFromRightX = (int) ((d.x+d.width/2)/t.segmentWidth);
					int iFromCenterX = (int) (d.x/t.segmentWidth);
					int iFromPreviousCenterX = (int) ((d.x-d.v.x)/t.segmentWidth);
					
					if(iFromCenterX < 0 || iFromCenterX >= t.points.length-1)
					{
						d.removeFromGLEngine = true;
						d.removeFromPhysicsEngine = true;
					}
					else
					{						
						double leftPercent = ((d.x-d.width/2) % t.segmentWidth) / t.segmentWidth;
						double rightPercent = ((d.x+d.width/2) % t.segmentWidth) / t.segmentWidth;
						double landYatLeftX = t.points[iFromLeftX] + (t.points[iFromLeftX + 1] - t.points[iFromLeftX]) * leftPercent;
						double landYatRightX = t.points[iFromRightX] + (t.points[iFromRightX + 1] - t.points[iFromRightX]) * rightPercent;
						
						boolean leftIntersected = d.y-d.height/2 <= landYatLeftX;
						boolean rightIntersected = d.y-d.height/2 <= landYatRightX;
						
						int min_index = 0;
						int max_index = 0;
						float x1 = 0;
						float x2 = 0;
						
						if(!leftIntersected && !rightIntersected)
						{
							continue;
						}
						
						else if(leftIntersected)
						{
							int iFromPreviousLeftX = (int) ((d.x - d.width/2 - d.v.x)/t.segmentWidth);
							min_index = iFromPreviousLeftX;
							max_index = iFromLeftX;
							x1 = d.x - d.v.x - d.width/2;
							x2 = d.x - d.width/2;
						}
						else //if(rightIntersected)
						{
							int iFromPreviousRightX = (int) ((d.x + d.width/2 - d.v.x)/t.segmentWidth);
							min_index = iFromPreviousRightX;
							max_index = iFromRightX;
							x1 = d.x - d.v.x + d.width/2;
							x2 = d.x + d.width/2;
						}
						
						if(min_index > max_index)
						{
							int temp = min_index;
							min_index = max_index;
							max_index = temp;
						}
						
						for(int s = 0; s <= t.NUM_POINTS-2; s++)
						{
							float xFromIndex = s*t.segmentWidth;
							float xFromNextIndex = (s+1)*t.segmentWidth;
							
							float y1 = d.y - d.v.y - d.height/2;
							float y2 = d.y - d.height/2;
							
							float[] intersect = lineIntersect(x1, y1, x2, y2, xFromIndex, t.points[s], xFromNextIndex, t.points[s+1], t.previousPoints[s], t.previousPoints[s+1]);
							if(intersect != null)
							{
								d.intersectTerrain(t, intersect[0], intersect[1]);
								break;
							}
						}
						if(!d.removeFromPhysicsEngine)
						{
							d.intersectTerrain(t, d.x, d.y);
						}
					}
					
					if(d.removeFromPhysicsEngine)
					{
						collidables.remove(i);
					}
				}
			}
			
			t.update();
			
			globalForces.clear();
			pointForces.clear();
			
			try
			{
				Thread.currentThread().sleep(20);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public float[] lineIntersect(float px1, float py1, float px2, float py2, float lx1, float ly1, float lx2, float ly2, float old_ly1, float old_ly2)
	{
		float a = px1;			// (* dirt point x *)
		float b = py1;			// (* dirt point y *)
		float c = px2 - px1;	// (* dirt point x velocity *)
		float d = py2 - py1;	// (* dirt point y velocity *)
		float w = lx2 - lx1;	// (* terrain segment width *)
		float i = lx1; 			// (* left terrain point x *)
		float e = old_ly1; 		// (* previous left terrain point y *)
		float f = old_ly2; 		// (* previous right terrain point y *)
		float g = ly1;			// (* current left terrain point y *)
		float h = ly2;			// (* current right terrain point y *)
		float j = g - e;		// (* change in left terrain point y *)
		float k = h - f;		// (* change in right terrain point y *)

		if(j == 0 && k == 0)
		{
			// Terrain hasn't moved, perform standard line intersection
			float denom = (ly2 - ly1) * (px2 - px1) - (lx2 - lx1) * (py2 - py1);
			if(denom == 0)
			{
				return null;
			}
			
			float ua = ((lx2 - lx1) * (py1 - ly1) - (ly2 - ly1) * (px1 - lx1)) / denom;
			float ub = ((px2 - px1) * (py1 - ly1) - (py2 - py1) * (px1 - lx1)) / denom;
			if (ua >= 0 && ua <= 1.0f && ub >= 0 && ub <= 1.0f)
			{
				float intersect[] = new float[2];
				intersect[0] = px1 + ua * (px2 - px1);
				intersect[1] = py1 + ua * (py2 - py1);
				return intersect;
			}
			
			return null;
		}
		
		if(c<=0.000001 && c>=-0.000001)
		{
			// dirt is moving straight down, special case
			float t = (-a*e + a*f + e*i - f*i - b*w + e*w)/(a*j - i*j - a*k + i*k + d*w - j*w);
			
			if(t >= 0 && t <= 1)
			{
				float intersect[] = new float[2];
				intersect[0] = px1+c*t;
				intersect[1] = py1+d*t;
				
				return intersect;
			}
		}
		
		float A = c*j-c*k;
		
		if(A == 0)
		{
			return null;
		}
		
		float B = c*e - c*f + a*j - i*j - a*k + i*k + d*w - j*w;
		float C = a*e - a*f - e*i + f*i + b*w - e*w;
		
		float t1 = (float) ((-B - Math.sqrt(B*B - 4*A*C)) / (2*A));
		float t2 = (float) ((-B + Math.sqrt(B*B - 4*A*C)) / (2*A));
		float m1 = (a+c*t1-i)/w;
		float m2 = (a+c*t2-i)/w;
		
		float intersect[] = new float[2];
		if(t1 >= 0 && t1 <= 1 && m1 >=0 && m1 <= 1)
		{
			//System.out.println("1");
			intersect[0] = px1+c*t1;
			intersect[1] = py1+d*t1;

			return intersect;
		}
		/*else if(t2 >= 0 && t2 <= 1 && m2 >= 0 && m2 <= 1)
		{
			System.out.println("2");
			intersect[0] = px1+c*t2;
			intersect[1] = py1+d*t2;
			return intersect;
		}*/
		return null;
	}
	
	public void addCollidable(Drawable c)
	{
		synchronized (collidables) 
		{
			collidables.add(c);
		}
	}

	public void addCollidables(ArrayList<? extends Drawable> things)
	{
		synchronized (collidables) 
		{
			collidables.addAll(things);
		}
		
	}
}

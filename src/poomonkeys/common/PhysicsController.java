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
					
					Point2D totalForce = new Point2D(0, 0);
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
					
					if(iFromLeftX < 0 || iFromRightX >= t.points.length-1)
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
						
						if(leftIntersected || rightIntersected)
						{
							Point2D point = null;
						
							if(leftIntersected)
							{
								int iFromPreviousLeftX = (int) ((d.x - d.width/2 - d.v.x)/t.segmentWidth);
								min_index = iFromPreviousLeftX;
								max_index = iFromLeftX;
								point = new Point2D(d.x-d.v.x-d.width/2, d.y-d.v.y-d.height/2);
							}
							else //if(rightIntersected)
							{
								int iFromPreviousRightX = (int) ((d.x + d.width/2 - d.v.x)/t.segmentWidth);
								min_index = iFromPreviousRightX;
								max_index = iFromRightX;
								point = new Point2D(d.x-d.v.x+d.width/2, d.y-d.v.y-d.height/2);
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
								
								Point2D segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
								Point2D segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
								Point2D segmentRight = new Point2D(xFromNextIndex, t.points[s+1]);
								Point2D segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
								Point2D intersect = lineIntersect(point, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
								if(intersect != null)
								{
									System.out.println(intersect.x + ", " + intersect.y);
									d.intersectTerrain(t, intersect.x, intersect.y);
									break;
								}
							}

							/*if(!d.removeFromPhysicsEngine)
							{
								d.intersectTerrain(t, d.x, d.y);
							}*/
						}
					}
					
					if(d.removeFromPhysicsEngine)
					{
						d.removeFromPhysicsEngine=false;
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
	
	public Point2D lineIntersect(Point2D p1, Point2D v1, Point2D p2, Point2D v2, Point2D p3, Point2D v3)
	{
		
		if(v2.x == 0 && v2.y == 0 && v3.x == 0 && v3.y == 0)
		{
			// Line segment points haven't moved, perform standard line segment intersect
			float denom = (p3.y - p2.y) * v1.x - (p3.x-p2.x) * v1.y;
			if(denom == 0)
			{
				return null;
			}
			
			float ua = ((p3.x-p2.x) * (p1.y - p2.y) - (p3.y - p2.y) * (p1.x - p2.x)) / denom;
			float ub = (v1.x * (p1.y - p2.y) - v1.y * (p1.x - p2.x)) / denom;
			if (ua >= 0 && ua <= 1.0f && ub >= 0 && ub <= 1.0f)
			{
				Point2D intersect = new Point2D();
				intersect.x = p1.x + ua * v1.x;
				intersect.y = p1.y + ua * v1.y;
				return intersect;
			}
			
			return null;
		}
		
		float A = v1.y*v2.x - v1.x*v2.y - v1.y*v3.x + v2.y*v3.x + v1.x*v3.y - v2.x*v3.y;
		
		if(A == 0)
		{
			return null;
		}
		
		float B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y + p1.y*v2.x - p3.y*v2.x - p1.x*v2.y + p3.x*v2.y - p1.y*v3.x + p2.y*v3.x + p1.x*v3.y - p2.x*v3.y;
		float C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
		float sqrt = (float) Math.sqrt(B*B - 4*A*C);
		
		float t = (-B + sqrt) / (2*A);
		if(t < 0 || t > 1 || Float.isNaN(t)) 
		{
			t = (-B - sqrt) / (2*A);
			if(t < 0 || t > 1 || Float.isNaN(t))
			{
				return null;
			}
		}
		Point2D intersect = new Point2D(p1.x+v1.x*t, p1.y+v1.y*t);
		Point2D one = new Point2D(p2.x+v2.x*t, p2.y+v2.y*t);
		Point2D two = new Point2D(p3.x+v3.x*t, p3.y+v3.y*t);
		if(!isBetween(intersect.x, one.x, two.x))
		{
			return null;
		}
		if(!isBetween(intersect.y, one.y, two.y))
		{
			return null;
		}
		
		return intersect;
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

	public boolean hasCollidable(Drawable d)
	{
		synchronized (collidables) 
		{
			return collidables.contains(d);
		}
	}
	
	// Return true if c is between a and b.
	public static boolean isBetween(float c, float a, float b) 
	{
	    return b > a ? c > a && c < b : c > b && c < a;
	}
}

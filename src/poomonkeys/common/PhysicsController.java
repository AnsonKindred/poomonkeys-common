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
			try
			{
				Thread.currentThread().sleep(20);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			synchronized (collidables) 
			{ 
				synchronized(t)
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
							
							boolean leftIntersected = (d.y-d.height/2) <= landYatLeftX;
							boolean rightIntersected = (d.y-d.height/2) <= landYatRightX;
							
							if(leftIntersected || rightIntersected)
							{
							
								//if(leftIntersected)
								//{
									int iFromPreviousLeftX = (int) ((d.x - d.width/2 - d.v.x)/t.segmentWidth);
									int left_min_index = iFromPreviousLeftX;
									int left_max_index = iFromLeftX;
									Point2D leftPoint = new Point2D(d.x-d.v.x-d.width/2, d.y-d.v.y-d.height/2);
								//}
								//else //if(rightIntersected)
								//{
									int iFromPreviousRightX = (int) ((d.x + d.width/2 - d.v.x)/t.segmentWidth);
									int right_min_index = iFromPreviousRightX;
									int right_max_index = iFromRightX;
									Point2D rightPoint = new Point2D(d.x-d.v.x+d.width/2, d.y-d.v.y-d.height/2);
								//}
								
								if(left_min_index > left_max_index)
								{
									int temp = left_min_index;
									left_min_index = left_max_index;
									left_max_index = temp;
								}
								if(right_min_index > right_max_index)
								{
									int temp = right_min_index;
									right_min_index = right_max_index;
									right_max_index = temp;
								}
								
								for(int s = 0; s <= t.NUM_POINTS-2; s++)
								{
									float xFromIndex = s*t.segmentWidth;
									float xFromNextIndex = (s+1)*t.segmentWidth;
									
									Point2D segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
									Point2D segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
									Point2D segmentRight = new Point2D(xFromNextIndex, t.previousPoints[s+1]);
									Point2D segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
									
									Point2D intersectLeft = lineIntersect(leftPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
									Point2D intersectRight = lineIntersect(rightPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
									if(intersectLeft != null)
									{
										d.intersectTerrain(t, intersectLeft.x, intersectLeft.y);
										break;
									}
									else if(intersectRight != null)
									{
										d.intersectTerrain(t, intersectRight.x, intersectRight.y);
										break;
									}
								}
	
								if(!d.removeFromPhysicsEngine)
								{
									if(leftIntersected)
									{
										for(int s = left_min_index; s <= left_max_index; s++)
										{
											float xFromIndex = s*t.segmentWidth;
											float xFromNextIndex = (s+1)*t.segmentWidth;
											System.out.println("(" + xFromIndex + ", " + t.previousPoints[s] + ")-(" + xFromNextIndex + ", " + t.previousPoints[s+1]+")");
											System.out.println("<0, " + (t.points[s] - t.previousPoints[s]) + ">-<0, " + (t.points[s+1] - t.previousPoints[s+1])+">");
											Point2D segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
											Point2D segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
											Point2D segmentRight = new Point2D(xFromNextIndex, t.previousPoints[s+1]);
											Point2D segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
											Point2D intersectLeft = lineIntersect(leftPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
										}
										System.out.println((d.x-d.width/2-d.v.x) + ", " + (d.y-d.height/2-d.v.y) + ", " + d.v.x + ", " + d.v.y);
									}
									if(rightIntersected)
									{
										for(int s = right_min_index; s <= right_max_index; s++)
										{
											float xFromIndex = s*t.segmentWidth;
											float xFromNextIndex = (s+1)*t.segmentWidth;
											System.out.println("(" + xFromIndex + ", " + t.previousPoints[s] + ")-(" + xFromNextIndex + ", " + t.previousPoints[s+1]+")");
											System.out.println("<0, " + (t.points[s] - t.previousPoints[s]) + ">-<0, " + (t.points[s+1] - t.previousPoints[s+1])+">");
											Point2D segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
											Point2D segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
											Point2D segmentRight = new Point2D(xFromNextIndex, t.previousPoints[s+1]);
											Point2D segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
											Point2D intersectRight = lineIntersect(rightPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
										}
										System.out.println((d.x+d.width/2-d.v.x) + ", " + (d.y-d.height/2-d.v.y) + ", " + d.v.x + ", " + d.v.y);
									}
									
									System.exit(0);
									//d.intersectTerrain(t, d.x-d.v.x, d.y-d.v.y);
								}
							}
						}
						
						if(d.removeFromPhysicsEngine)
						{
							d.removeFromPhysicsEngine=false;
							collidables.remove(i);
						}
					}
				}
			}
			
			t.update();
			
			globalForces.clear();
			pointForces.clear();
		}
	}
	
	public Point2D lineIntersect(Point2D p1, Point2D v1, Point2D p2, Point2D v2, Point2D p3, Point2D v3)
	{
		float EPSILON = .0001f;
		

		// Line segment points haven't moved, perform standard point / line segment intersection
		if(v2.x == 0 && v2.y == 0 && v3.x == 0 && v3.y == 0)
		{
			float denom = -p2.x + p3.x;
			float dif = -p2.y + p3.y;
			float t = (-p1.y + p2.y + p1.x*dif/denom - p2.x*dif/denom) / (-v1.x*dif/denom + v1.y);
			
			if(t >= -EPSILON && t <= 1+EPSILON)
			{
				Point2D intersect = new Point2D(p1.x+v1.x*t, p1.y+v1.y*t);
				return intersect;
			}
			
			//System.out.println("Z " + t);
			
			return null;
			
			/*float denom = (p3.y - p2.y) * v1.x - (p3.x-p2.x)*v1.y;
			if(denom == 0)
			{
				return null;
			}
			
			float ua = ((p3.x-p2.x)*(p1.y - p2.y) - (p3.y - p2.y)*(p1.x - p2.x)) / denom;
			float ub = (v1.x*(p1.y - p2.y) - v1.y*(p1.x - p2.x)) / denom;
			if (ua >= -EPSILON && ua <= 1.0f+EPSILON && ub >= -EPSILON && ub <= 1.0f+EPSILON)
			{
				return new Point2D(p1.x + ua*v1.x, p1.y + ua*v1.y);
			}

			System.out.println("ua" + ua);
			System.out.println("ub" + ub);
			return null;*/
		}

		// Line segment and point both moving vertically
		if(v1.x > -EPSILON && v1.x < EPSILON && v2.x > -EPSILON && v2.x < EPSILON && v3.x > -EPSILON && v3.x < EPSILON)
		{
			float denom = -p2.x+p3.x; 
			float dif = p1.x - p2.x;
			float t = (-p1.y + p2.y - (dif*p2.y)/denom + (dif*p3.y)/denom)/(v1.y - v2.y + (dif*v2.y)/denom - (dif*v3.y)/denom);
			if(t >= -EPSILON && t <= 1+EPSILON)
			{
				Point2D intersect = new Point2D(p1.x+v1.x*t, p1.y+v1.y*t);
				return intersect;
			}
			return null;
		}
		
		// Line segment end points moving vertically
		if(v2.x > -EPSILON && v2.x < EPSILON && v3.x > -EPSILON && v3.x < EPSILON)
		{
			// Both end points moving vertically at the same velocity (I can't believe I need special code for this...)
			if(Math.abs(v2.y - v3.y) < EPSILON)
			{
				float denom = -p2.x+p3.x; 
				float dif = -p2.y + p3.y;
				
				float t = (-p1.y + p2.y + p1.x*dif/denom - p2.x*dif/denom)/(-v1.x*dif/denom + v1.y - v2.y);
				if(t >= 0-EPSILON && t <= 1+EPSILON)
				{
					Point2D intersect = new Point2D(p1.x+v1.x*t, p1.y+v1.y*t);
					return intersect;
				}

				System.out.println("A" + t);
				return null;
			}
			
			// End points moving vertically at different velocities
			double A = -v1.x*v2.y + v1.x*v3.y;
			double B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y - p1.x*v2.y + p3.x*v2.y + p1.x*v3.y - p2.x*v3.y;
			double C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
			double sqrt = Math.sqrt(B*B - 4*A*C);
			double t = (-B + sqrt) / (2*A);

			System.out.println("B" + t);			
			System.out.println("a" + A);
			System.out.println("b" + B);
			System.out.println("c" + C);
			if(t < -EPSILON || t > 1+EPSILON || Double.isNaN(t)) 
			{
				t = (-B - sqrt) / (2*A);
				if(t < -EPSILON || t > 1+EPSILON || Double.isNaN(t))
				{
					System.out.println("C" + t);
					return null;
				}
			}
			
			// make sure the intersection lies on the line segment
			Point2D intersect = new Point2D((float)(p1.x+v1.x*t), (float)(p1.y+v1.y*t));
			Point2D one = new Point2D((float)(p2.x+v2.x*t), (float)(p2.y+v2.y*t));
			Point2D two = new Point2D((float)(p3.x+v3.x*t), (float)(p3.y+v3.y*t));
			System.out.println(intersect.x + ", " + intersect.y);
			System.out.println(one.x + ", " + one.y);
			System.out.println(two.x + ", " + two.y);
			if(!isBetween(intersect.x, one.x, two.x, EPSILON))
			{
				return null;
			}
			if(!isBetween(intersect.y, one.y, two.y, EPSILON))
			{
				return null;
			}
			
			return intersect;
		}
		
		// End points of line segment moving in x and y, point also moving
		float A = v1.y*v2.x - v1.x*v2.y - v1.y*v3.x + v2.y*v3.x + v1.x*v3.y - v2.x*v3.y;
		
		if(A == 0)
		{
			return null;
		}
		
		float B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y + p1.y*v2.x - p3.y*v2.x - p1.x*v2.y + p3.x*v2.y - p1.y*v3.x + p2.y*v3.x + p1.x*v3.y - p2.x*v3.y;
		float C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
		float sqrt = (float) Math.sqrt(B*B - 4*A*C);
				
		float t = (-B + sqrt) / (2*A);
		
		// make sure the intersection happens within one time step
		if(t < -EPSILON || t > 1+EPSILON || Float.isNaN(t)) 
		{
			t = (-B - sqrt) / (2*A);
			
			if(t < -EPSILON || t > 1+EPSILON || Float.isNaN(t))
			{
				return null;
			}
		}
		
		// make sure the intersection lies on the line segment
		Point2D intersect = new Point2D(p1.x+v1.x*t, p1.y+v1.y*t);
		Point2D one = new Point2D(p2.x+v2.x*t, p2.y+v2.y*t);
		Point2D two = new Point2D(p3.x+v3.x*t, p3.y+v3.y*t);
		
		if(!isBetween(intersect.x, one.x, two.x, EPSILON))
		{
			return null;
		}
		if(!isBetween(intersect.y, one.y, two.y, EPSILON))
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
	public static boolean isBetween(float c, float a, float b, float EPSILON) 
	{
	    return b > a ? c >= a-EPSILON && c <= b+EPSILON : c >= b-EPSILON && c <= a+EPSILON;
	}
}

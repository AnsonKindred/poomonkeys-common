package poomonkeys.common;

import java.util.ArrayList;

public class PhysicsController extends Thread
{
	public static final float GRAVITY = -.01f;
	float EPSILON = .00001f;
	
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
						
						float next_x = d.x + d.v.x;
						float next_y = d.y + d.v.y;
						
						int iFromLeftX = (int) ((next_x-d.width/2)/t.segmentWidth);
						int iFromRightX = (int) ((next_x+d.width/2)/t.segmentWidth);
						
						if(iFromLeftX < 0 || iFromRightX >= t.points.length-1)
						{
							d.removeFromGLEngine = true;
							d.removeFromPhysicsEngine = true;
						}
						else
						{						
							double leftPercent = ((next_x-d.width/2) % t.segmentWidth) / t.segmentWidth;
							double rightPercent = ((next_x+d.width/2) % t.segmentWidth) / t.segmentWidth;
							double landYatLeftX = t.points[iFromLeftX] + (t.points[iFromLeftX + 1] - t.points[iFromLeftX]) * leftPercent;
							double landYatRightX = t.points[iFromRightX] + (t.points[iFromRightX + 1] - t.points[iFromRightX]) * rightPercent;
							
							boolean leftIntersected = (next_y-d.height/2) <= landYatLeftX;
							boolean rightIntersected = (next_y-d.height/2) <= landYatRightX;
							
							int iFromPreviousLeftX = (int) ((d.x - d.width/2)/t.segmentWidth);
							int left_min_index = iFromPreviousLeftX;
							int left_max_index = iFromLeftX;
							Point2D leftPoint = new Point2D(d.x-d.width/2, d.y-d.height/2);
								
							int iFromPreviousRightX = (int) ((d.x + d.width/2)/t.segmentWidth);
							int right_min_index = iFromPreviousRightX;
							int right_max_index = iFromRightX;
							Point2D rightPoint = new Point2D(d.x+d.width/2, d.y-d.height/2);
							
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

							float lowestT = Float.MAX_VALUE;
							float firstIntersection[] = null;

							if(leftIntersected || rightIntersected)
							{
								Point2D segmentLeft = null;
								Point2D segmentLeftV = null;
								Point2D segmentRight = null;
								Point2D segmentRightV = null;
								for(int s = left_min_index; s <= left_max_index; s++)
								{
									float xFromIndex = s*t.segmentWidth;
									float xFromNextIndex = (s+1)*t.segmentWidth;
									
									segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
									segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
									segmentRight = new Point2D(xFromNextIndex, t.previousPoints[s+1]);
									segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
									
									float intersect[] = findCollision(leftPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
									if(intersect != null)
									{
										if(intersect[2] < lowestT)
										{
											lowestT = intersect[2];
											firstIntersection = intersect;
										}
									}
								}
								
								for(int s = right_min_index; s <= right_max_index; s++)
								{
									float xFromIndex = s*t.segmentWidth;
									float xFromNextIndex = (s+1)*t.segmentWidth;
									
									segmentLeft = new Point2D(xFromIndex, t.previousPoints[s]);
									segmentLeftV = new Point2D(0, t.points[s]-t.previousPoints[s]);
									segmentRight = new Point2D(xFromNextIndex, t.previousPoints[s+1]);
									segmentRightV = new Point2D(0, t.points[s+1]-t.previousPoints[s+1]);
									
									float intersect[] = findCollision(rightPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV);
									if(intersect != null)
									{
										if(intersect[2] < lowestT)
										{
											lowestT = intersect[2];
											firstIntersection = intersect;
										}
									}
								}

								// Somehow a collision got missed, at least let the drawable know that it is beneath the terrain
								if(firstIntersection == null)
								{
									d.underTerrain();
								}
							}
							else
							{
								d.aboveTerrain();
							}
							
							if(d.width > 2)
							{
								for(int s = left_min_index; s <= right_max_index; s++)
								{
									float xFromIndex = s*t.segmentWidth;
									
									Point2D segmentPoint = new Point2D(xFromIndex, t.previousPoints[s]);
									Point2D segmentV = new Point2D(0, t.points[s]-t.previousPoints[s]);
									
									float intersect[] = findCollision(segmentPoint, segmentV, leftPoint, d.v, rightPoint, d.v);
									if(intersect != null)
									{
										if(intersect[2] < lowestT)
										{
											lowestT = intersect[2];
											firstIntersection = intersect;
										}
									}
								}
							}
							
							if(firstIntersection != null)
							{
								d.intersectTerrain(t, firstIntersection);
							}
						}
						
						if(d.removeFromPhysicsEngine)
						{
							d.removeFromPhysicsEngine=false;
							collidables.remove(i);
						}

						d.x += d.v.x;
						d.y += d.v.y;
					}
				}
			}

			t.update();
			
			globalForces.clear();
			pointForces.clear();
		}
	}
	
	
	/**
	 * Finds the time at which a point with a fixed velocity will intersect 
	 * a line segment whose end points are also moving at fixed (but independent)
	 * velocities.
	 * 
	 * If the end points of the line segment are both moving at the same 
	 * velocity, consider using the slightly faster findCollision_pointAndMovingLinesegment
	 * 
	 * If the line segment isn't moving at all consider using findCollision_pointAndLinesegment
	 *  
	 * @param p1	point starting location
	 * @param v1	point velocity
	 * @param p2	first line segment end point starting location
	 * @param v2	first line segment end point velocity
	 * @param p3	second line segment end point starting location
	 * @param v3	second line segment end point velocity
	 * 
	 * @return an array of length 3 containing the x, y intersect point and time of intersection (in that order)
	 */
	public float[] findCollision(Point2D p1, Point2D v1, Point2D p2, Point2D v2, Point2D p3, Point2D v3)
	{
		float EPSILON_A = .002f;
		double t[] = {-1f, -1f};
		float result[] = new float[3];

		// Line segment points haven't moved, perform standard point / line segment intersection
		if(v2.x > -EPSILON && v2.x < EPSILON && 
			v2.y > -EPSILON && v2.y < EPSILON && 
			v3.x > -EPSILON && v3.x < EPSILON && 
			v3.y > -EPSILON && v3.y < EPSILON)
		{
			t[0] = findCollision_pointAndLinesegment(p1, v1, p2, p3);
		}
		// Line segment only moving vertically
		else if(v2.x > -EPSILON && v2.x < EPSILON && v3.x > -EPSILON && v3.x < EPSILON)
		{
			// Both end points moving vertically at the same velocity (I can't believe I need special code for this...)
			if(Math.abs(v2.y - v3.y) < EPSILON)
			{
				double denom = -p2.x+p3.x; 
				double dif = -p2.y + p3.y;
				
				t[0] = (-p1.y + p2.y + p1.x*dif/denom - p2.x*dif/denom)/(-v1.x*dif/denom + v1.y - v2.y);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v3.y > -EPSILON && v3.y < EPSILON)
			{
				double C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
				double B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y - p1.x*v2.y + p3.x*v2.y;
				double A = v1.x*v2.y;
				double sqrt = Math.sqrt(4*A*C + B*B);
				double frac = -1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v2.y > -EPSILON && v2.y < EPSILON)
			{
				double C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
				double B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y + p1.x*v3.y - p2.x*v3.y;
				double A = v1.x*v3.y;
				double sqrt = Math.sqrt(-4*A*C + B*B);
				double frac = 1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// End points moving vertically at different velocities
			else
			{
				double A = -v1.x*v2.y + v1.x*v3.y;
				double B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y - p1.x*v2.y + p3.x*v2.y + p1.x*v3.y - p2.x*v3.y;
				double C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
				double sqrt = Math.sqrt(B*B - 4*A*C);
				
				t[0] = (-B + sqrt) / (2*A);
				t[1] = (-B - sqrt) / (2*A);
			}
		}
		// Line segment endpoints both moving at the same velocity
		else if(Math.abs(v2.x-v3.x) < EPSILON && Math.abs(v3.y-v2.y) < EPSILON)
		{
			t[0] = findCollision_pointAndMovingLinesegment(p1, v1, p2, p3, v2);
		}
		// Line segment and point both moving vertically
		else if(v1.x > -EPSILON && v1.x < EPSILON && v2.x > -EPSILON && v2.x < EPSILON && v3.x > -EPSILON && v3.x < EPSILON)
		{
			double denom = -p2.x+p3.x; 
			double dif = p1.x - p2.x;
			t[0] = (-p1.y + p2.y - (dif*p2.y)/denom + (dif*p3.y)/denom)/(v1.y - v2.y + (dif*v2.y)/denom - (dif*v3.y)/denom);
		}
		// End points of line segment moving at different velocities, point also moving
		else
		{
			findCollision_pointAndLinesegmentWithIndependentlyMovingEndpoints(p1, v1, p2, v2, p3, v3, t);
		}
		

		// make sure the intersection happens within one time step
		float final_t = (float) t[0];
		if(t[0] < -EPSILON_A || t[0] > 1+EPSILON_A || Double.isNaN(t[0])) 
		{
			final_t = (float) t[1];
			if(t[1] < -EPSILON_A || t[1] > 1+EPSILON_A || Double.isNaN(t[1]))
			{
				return null;
			}
		}
		
		// make sure the intersection lies on the line segment
		result[0] = p1.x+v1.x*final_t;
		result[1] = p1.y+v1.y*final_t;
		result[2] = final_t;
		float p1x = p2.x+v2.x*final_t;
		float p1y = p2.y+v2.y*final_t;
		float p2x = p3.x+v3.x*final_t;
		float p2y = p3.y+v3.y*final_t;
		if(!isBetween(result[0], p1x, p2x, EPSILON_A))
		{
			return null;
		}
		if(!isBetween(result[1], p1y, p2y, EPSILON_A))
		{
			return null;
		}
		
		return result;
	}
	
	public float findCollision_pointAndLinesegment(Point2D p1, Point2D v1, Point2D p2, Point2D p3)
	{
		float denom = -p2.x + p3.x;
		float dif = -p2.y + p3.y;
		float t = (-p1.y + p2.y + p1.x*dif/denom - p2.x*dif/denom) / (-v1.x*dif/denom + v1.y);
		
		return t;
	}
	
	public double findCollision_pointAndMovingLinesegment(Point2D p1, Point2D v1, Point2D p2, Point2D p3, Point2D lv)
	{
		float denom = -p2.x + p3.x;
		float dif = -p2.y + p3.y;
		
		// point moving vertically
		if(v1.x > -EPSILON && v1.x < EPSILON)
		{
			return (p1.y - p2.y - (p1.x*dif)/denom + (p2.x*dif)/denom)/(-v1.y - (dif*lv.x)/denom + lv.y);
		}
		// point moving non-vertically
		else
		{
			return (p1.y - p2.y - (p1.x*dif)/denom + (p2.x*dif)/denom)/((dif*v1.x)/denom - v1.y - (dif*lv.x)/denom + lv.y);
		}
	}
	
	public void findCollision_pointAndLinesegmentWithIndependentlyMovingEndpoints(Point2D p1, Point2D v1, Point2D p2, Point2D v2, Point2D p3, Point2D v3, double[] t)
	{
		double A = v1.y*v2.x - v1.x*v2.y - v1.y*v3.x + v2.y*v3.x + v1.x*v3.y - v2.x*v3.y;
		
		if(A == 0)
		{
			return;
		}
		
		double B = -p2.y*v1.x + p3.y*v1.x + p2.x*v1.y - p3.x*v1.y + p1.y*v2.x - p3.y*v2.x - p1.x*v2.y + p3.x*v2.y - p1.y*v3.x + p2.y*v3.x + p1.x*v3.y - p2.x*v3.y;
		double C = p1.y*p2.x - p1.x*p2.y - p1.y*p3.x + p2.y*p3.x + p1.x*p3.y - p2.x*p3.y;
		double sqrt = Math.sqrt(B*B - 4*A*C);
				
		t[0] = (-B + sqrt) / (2*A);
		t[1] = (-B - sqrt) / (2*A);
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
	
	public static boolean isBetween(float c, float a, float b) 
	{
	    return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
	
	// Return true if c is between a and b.
	public static boolean isBetween(float c, float a, float b, float EPSILON) 
	{
	    return b > a ? c >= a-EPSILON && c <= b+EPSILON : c >= b-EPSILON && c <= a+EPSILON;
	}
}

package poomonkeys.common;

import java.util.ArrayList;
import java.util.ListIterator;

public class PhysicsController extends Thread
{
	public static final float GRAVITY = -.01f;
	float EPSILON = .00001f;
	
	private ArrayList<Drawable> collidables = new ArrayList<Drawable>();
	private Terrain t;
	public ArrayList<float[]> globalForces = new ArrayList<float[]>();
	public ArrayList<float[]> permanentGlobalForces = new ArrayList<float[]>();
	public ArrayList<float[]> pointForces = new ArrayList<float[]>();
	public ArrayList<float[]> permanentPointForces = new ArrayList<float[]>();

	private static PhysicsController instance = null;
	
	
	float lastIntersect[] = new float[3];
	float[] segmentLeft = new float[2];
	float[] segmentLeftV = new float[2];
	float[] segmentRight = new float[2];
	float[] segmentRightV = new float[2];
	float[] leftPoint = new float[2];
	float[] rightPoint = new float[2];
	float[] segmentPoint = new float[2];
	float[] segmentV = new float[2];

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
					ListIterator<Drawable> itr = collidables.listIterator();
					while(itr.hasNext())
					{
						Drawable d = itr.next();
						Point2D totalForce = new Point2D(0, 0);
						for(int f = 0; f < permanentGlobalForces.size(); f++)
						{
							float[] force = permanentGlobalForces.get(f);
							totalForce.x += force[0];
							totalForce.y += force[1];
						}
						
						for(int f = 0; f < globalForces.size(); f++)
						{
							float[] force = globalForces.get(f);
							totalForce.x += force[0];
							totalForce.y += force[1];
						}
						
						for(int f = 0; f < permanentPointForces.size(); f++)
						{
							float[] force = permanentPointForces.get(f);
							float distance_factor = (float) (1 / Math.pow(1 + VectorUtil.distance(force, d.p), 2));
							Point2D forceDirection = new Point2D(d.p[0]-force[0], d.p[1]-force[1]);
							VectorUtil.scaleTo2D(forceDirection, force[2]);
							totalForce.x += forceDirection.x * distance_factor;
							totalForce.y += forceDirection.y * distance_factor;
						}
						
						for(int f = 0; f < pointForces.size(); f++)
						{
							float[] force = pointForces.get(f);
							float distance_factor = (float) (1 / Math.pow(1 + VectorUtil.distance(force, d.p), 2));
							Point2D forceDirection = new Point2D(d.p[0]-force[0], d.p[1]-force[1]);
							VectorUtil.scaleTo2D(forceDirection, force[2]);
							totalForce.x += forceDirection.x * distance_factor;
							totalForce.y += forceDirection.y * distance_factor;
						}
						
						d.a[0] = totalForce.x/d.m;
						d.a[1] = totalForce.y/d.m;
						
						d.v[0] += d.a[0];
						d.v[1] += d.a[1];
						
						d.v[1] += GRAVITY;
						
						float next_x = d.p[0] + d.v[0];
						float next_y = d.p[1] + d.v[1];
						
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
							
							if(leftIntersected || rightIntersected || d.width > 1)
							{
								int iFromPreviousLeftX = (int) ((d.p[0] - d.width/2)/t.segmentWidth);
								int left_min_index = iFromPreviousLeftX;
								int left_max_index = iFromLeftX;
								leftPoint[0] = d.p[0] - d.width/2;
								leftPoint[1] = d.p[1] - d.height/2;
									
								int iFromPreviousRightX = (int) ((d.p[0] + d.width/2)/t.segmentWidth);
								int right_min_index = iFromPreviousRightX;
								int right_max_index = iFromRightX;
								rightPoint[0] = d.p[0] + d.width/2;
								rightPoint[1] = d.p[1] - d.height/2;
								
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
									for(int s = left_min_index; s <= left_max_index && lowestT > EPSILON; s++)
									{
										float xFromIndex = s*t.segmentWidth;
										float xFromNextIndex = (s+1)*t.segmentWidth;
										
										segmentLeft[0] = xFromIndex;
										segmentLeft[1] = t.previousPoints[s];
										segmentLeftV[0] = 0;
										segmentLeftV[1] = t.points[s]-t.previousPoints[s];
										segmentRight[0] = xFromNextIndex;
										segmentRight[1] = t.previousPoints[s+1];
										segmentRightV[0] = 0;
										segmentRightV[1] = t.points[s+1]-t.previousPoints[s+1];
										
										boolean intersected = findCollision(leftPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < lowestT)
											{
												lowestT = lastIntersect[2];
												firstIntersection = lastIntersect;
											}
										}
									}
									
									for(int s = right_min_index; s <= right_max_index && lowestT > EPSILON; s++)
									{
										float xFromIndex = s*t.segmentWidth;
										float xFromNextIndex = (s+1)*t.segmentWidth;
										
										segmentLeft[0] = xFromIndex;
										segmentLeft[1] = t.previousPoints[s];
										segmentLeftV[0] = 0;
										segmentLeftV[1] = t.points[s]-t.previousPoints[s];
										segmentRight[0] = xFromNextIndex;
										segmentRight[1] = t.previousPoints[s+1];
										segmentRightV[0] = 0;
										segmentRightV[1] = t.points[s+1]-t.previousPoints[s+1];
										
										boolean intersected = findCollision(rightPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < lowestT)
											{
												lowestT = lastIntersect[2];
												firstIntersection = lastIntersect;
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
								
								if(d.width > 1)
								{
									for(int s = left_min_index; s <= right_max_index; s++)
									{
										float xFromIndex = s*t.segmentWidth;
										
										segmentPoint[0] = xFromIndex;
										segmentPoint[1] = t.previousPoints[s];
										segmentV[0] = 0; 
										segmentV[1] = t.points[s]-t.previousPoints[s];
										
										boolean intersected = findCollision(segmentPoint, segmentV, leftPoint, d.v, rightPoint, d.v, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < lowestT)
											{
												lowestT = lastIntersect[2];
												firstIntersection = lastIntersect;
											}
										}
									}
								}

								
								if(firstIntersection != null)
								{
									d.intersectTerrain(t, firstIntersection);
								}
							}
						}
						
						if(d.removeFromPhysicsEngine)
						{
							d.removeFromPhysicsEngine=false;
							itr.remove();
						}

						d.p[0] += d.v[0];
						d.p[1] += d.v[1];
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
	public boolean findCollision(float[] p1, float[] v1, float[] p2, float[] v2, float[] p3, float[] v3, float[] result)
	{
		float EPSILON_A = .002f;
		double t[] = {-1f, -1f};

		// Line segment points haven't moved, perform standard point / line segment intersection
		if(v2[0] > -EPSILON && v2[0] < EPSILON && 
			v2[1] > -EPSILON && v2[1] < EPSILON && 
			v3[0] > -EPSILON && v3[0] < EPSILON && 
			v3[1] > -EPSILON && v3[1] < EPSILON)
		{
			t[0] = findCollision_pointAndLinesegment(p1, v1, p2, p3);
		}
		// Line segment only moving vertically
		else if(v2[0] > -EPSILON && v2[0] < EPSILON && v3[0] > -EPSILON && v3[0] < EPSILON)
		{
			// Both end points moving vertically at the same velocity (I can't believe I need special code for this...)
			if(Math.abs(v2[1] - v3[1]) < EPSILON)
			{
				double denom = -p2[0]+p3[0]; 
				double dif = -p2[1] + p3[1];
				
				t[0] = (-p1[1] + p2[1] + p1[0]*dif/denom - p2[0]*dif/denom)/(-v1[0]*dif/denom + v1[1] - v2[1]);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v3[1] > -EPSILON && v3[1] < EPSILON)
			{
				double C = p1[1]*p2[0] - p1[0]*p2[1] - p1[1]*p3[0] + p2[1]*p3[0] + p1[0]*p3[1] - p2[0]*p3[1];
				double B = -p2[1]*v1[0] + p3[1]*v1[0] + p2[0]*v1[1] - p3[0]*v1[1] - p1[0]*v2[1] + p3[0]*v2[1];
				double A = v1[0]*v2[1];
				double sqrt = Math.sqrt(4*A*C + B*B);
				double frac = -1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v2[1] > -EPSILON && v2[1] < EPSILON)
			{
				double C = p1[1]*p2[0] - p1[0]*p2[1] - p1[1]*p3[0] + p2[1]*p3[0] + p1[0]*p3[1] - p2[0]*p3[1];
				double B = -p2[1]*v1[0] + p3[1]*v1[0] + p2[0]*v1[1] - p3[0]*v1[1] + p1[0]*v3[1] - p2[0]*v3[1];
				double A = v1[0]*v3[1];
				double sqrt = Math.sqrt(-4*A*C + B*B);
				double frac = 1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// End points moving vertically at different velocities
			else
			{
				double A = -v1[0]*v2[1] + v1[0]*v3[1];
				double B = -p2[1]*v1[0] + p3[1]*v1[0] + p2[0]*v1[1] - p3[0]*v1[1] - p1[0]*v2[1] + p3[0]*v2[1] + p1[0]*v3[1] - p2[0]*v3[1];
				double C = p1[1]*p2[0] - p1[0]*p2[1] - p1[1]*p3[0] + p2[1]*p3[0] + p1[0]*p3[1] - p2[0]*p3[1];
				double sqrt = Math.sqrt(B*B - 4*A*C);
				
				t[0] = (-B + sqrt) / (2*A);
				t[1] = (-B - sqrt) / (2*A);
			}
		}
		// Line segment endpoints both moving at the same velocity
		else if(Math.abs(v2[0]-v3[0]) < EPSILON && Math.abs(v3[1]-v2[1]) < EPSILON)
		{
			t[0] = findCollision_pointAndMovingLinesegment(p1, v1, p2, p3, v2);
		}
		// Line segment and point both moving vertically
		else if(v1[0] > -EPSILON && v1[0] < EPSILON && v2[0] > -EPSILON && v2[0] < EPSILON && v3[0] > -EPSILON && v3[0] < EPSILON)
		{
			double denom = -p2[0]+p3[0]; 
			double dif = p1[0] - p2[0];
			t[0] = (-p1[1] + p2[1] - (dif*p2[1])/denom + (dif*p3[1])/denom)/(v1[1] - v2[1] + (dif*v2[1])/denom - (dif*v3[1])/denom);
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
				return false;
			}
		}
		
		// make sure the intersection lies on the line segment
		result[0] = p1[0]+v1[0]*final_t;
		result[1] = p1[1]+v1[1]*final_t;
		result[2] = final_t;
		float p1x = p2[0]+v2[0]*final_t;
		float p1y = p2[1]+v2[1]*final_t;
		float p2x = p3[0]+v3[0]*final_t;
		float p2y = p3[1]+v3[1]*final_t;
		if(!isBetween(result[0], p1x, p2x, EPSILON_A))
		{
			return false;
		}
		if(!isBetween(result[1], p1y, p2y, EPSILON_A))
		{
			return false;
		}
		
		return true;
	}
	
	public float findCollision_pointAndLinesegment(float[] p1, float[] v1, float[] p2, float[] p3)
	{
		float denom = -p2[0] + p3[0];
		float dif = -p2[1] + p3[1];
		float t = (-p1[1] + p2[1] + p1[0]*dif/denom - p2[0]*dif/denom) / (-v1[0]*dif/denom + v1[1]);
		
		return t;
	}
	
	public double findCollision_pointAndMovingLinesegment(float[] p1, float[] v1, float[] p2, float[] p3, float[] lv)
	{
		float denom = -p2[0] + p3[0];
		float dif = -p2[1] + p3[1];
		
		// point moving vertically
		if(v1[0] > -EPSILON && v1[0] < EPSILON)
		{
			return (p1[1] - p2[1] - (p1[0]*dif)/denom + (p2[0]*dif)/denom)/(-v1[1] - (dif*lv[0])/denom + lv[1]);
		}
		// point moving non-vertically
		else
		{
			return (p1[1] - p2[1] - (p1[0]*dif)/denom + (p2[0]*dif)/denom)/((dif*v1[0])/denom - v1[1] - (dif*lv[0])/denom + lv[1]);
		}
	}
	
	public void findCollision_pointAndLinesegmentWithIndependentlyMovingEndpoints(float[] p1, float[] v1, float[] p2, float[] v2, float[] p3, float[] v3, double[] t)
	{
		double A = v1[1]*v2[0] - v1[0]*v2[1] - v1[1]*v3[0] + v2[1]*v3[0] + v1[0]*v3[1] - v2[0]*v3[1];
		
		if(A == 0)
		{
			return;
		}
		
		double B = -p2[1]*v1[0] + p3[1]*v1[0] + p2[0]*v1[1] - p3[0]*v1[1] + p1[1]*v2[0] - p3[1]*v2[0] - p1[0]*v2[1] + p3[0]*v2[1] - p1[1]*v3[0] + p2[1]*v3[0] + p1[0]*v3[1] - p2[0]*v3[1];
		double C = p1[1]*p2[0] - p1[0]*p2[1] - p1[1]*p3[0] + p2[1]*p3[0] + p1[0]*p3[1] - p2[0]*p3[1];
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

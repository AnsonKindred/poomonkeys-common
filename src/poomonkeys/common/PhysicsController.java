package poomonkeys.common;

import java.util.ArrayList;
import java.util.ListIterator;

public class PhysicsController extends Thread
{
	public static final float GRAVITY = -.01f;
	public static final float EPSILON = .00001f;
	
	private ArrayList<Drawable> collidables = new ArrayList<Drawable>();
	
	public ArrayList<float[]> pointForces = new ArrayList<float[]>();
	
	GameEngine engine;
	
	public PhysicsController(GameEngine e)
	{
		engine = e;
		
		start();
	}
	
	/**
	 * Animates the dirt points and handles adding them back to the terrain
	 */
	public void run()
	{
		float lastIntersect[] = new float[3];
		float[] segmentLeft = new float[2];
		float[] segmentLeftV = new float[2];
		float[] segmentRight = new float[2];
		float[] segmentRightV = new float[2];
		float[] leftPoint = new float[2];
		float[] rightPoint = new float[2];
		float[] segmentPoint = new float[2];
		float[] segmentV = new float[2];
		float[] totalForce = new float[2];
		float[] forceDirection = new float[2];
		
		while (true)
		{
			try
			{
				Thread.currentThread().sleep(20);
			}
			catch(Exception e) {}

			float[] movables = engine.getMovables();
			Terrain terrain = engine.getTerrain();
			
			if(terrain == null || (collidables.isEmpty() && engine.getNumMovables() == 0))
			{
				continue;
			}
			
			synchronized(terrain)
			{
				synchronized(GameEngine.movableLock)
				{
					int num_movables = engine.getNumMovables();
					int lastGeometryID = (int) movables[GameEngine.GEOM];
					Geometry geom = engine.getGeometry(lastGeometryID);
					for(int i = 0; i < num_movables && i >= 0; i++)
					{
						int movable_index = i*GameEngine.ITEMS_PER_MOVABLE;
						boolean dirt_removed = false;
						totalForce[0] = 0;
						totalForce[1] = 0;
						float x = movables[movable_index+GameEngine.X];
						float y = movables[movable_index+GameEngine.Y];
						float vx = movables[movable_index+GameEngine.VX];
						float vy = movables[movable_index+GameEngine.VY];
						float m = movables[movable_index+GameEngine.M];
						
						if(movables[movable_index+GameEngine.GEOM] != lastGeometryID)
						{
							lastGeometryID = (int) movables[movable_index+GameEngine.GEOM];
							geom = engine.getGeometry((int) movables[movable_index+GameEngine.GEOM]);
						}
						
						for(int f = 0; f < pointForces.size(); f++)
						{
							float[] force = pointForces.get(f);
							float distance_squared = (force[0]-x)*(force[0]-x) + (force[1]-y)*(force[1]-y);
							float distance = (float) Math.sqrt(distance_squared);
							float distance_factor = (float) (1 / (1 + distance_squared));
							forceDirection[0] = (x - force[0])/distance;
							forceDirection[1] = (y - force[1])/distance;
							totalForce[0] += forceDirection[0]*distance_factor*force[2];
							totalForce[1] += forceDirection[1]*distance_factor*force[2];
						}
						
						float ax = totalForce[0]/m;
						float ay = totalForce[1]/m;
						
						vx += ax;
						vy += ay;
						
						vy += GRAVITY;
						float next_x = x + vx;
						float next_y = y + vy;
						
						int iFromLeftX = (int) ((next_x - geom.width/2)/terrain.segmentWidth);
						int iFromRightX = (int) ((next_x + geom.width/2)/terrain.segmentWidth);
						
						if(iFromLeftX < 0 || iFromRightX >= terrain.points.length-1)
						{
							engine.removeMovable(i);
							dirt_removed = true;
						}
						else
						{						
							double leftPercent = ((next_x-geom.width/2) % terrain.segmentWidth) / terrain.segmentWidth;
							double rightPercent = ((next_x+geom.width/2) % terrain.segmentWidth) / terrain.segmentWidth;
							double landYatLeftX = terrain.points[iFromLeftX] + (terrain.points[iFromLeftX + 1] - terrain.points[iFromLeftX]) * leftPercent;
							double landYatRightX = terrain.points[iFromRightX] + (terrain.points[iFromRightX + 1] - terrain.points[iFromRightX]) * rightPercent;
							
							boolean leftIntersected = (next_y-geom.height/2) <= landYatLeftX;
							boolean rightIntersected = (next_y-geom.height/2) <= landYatRightX;
							
							if(leftIntersected || rightIntersected)
							{
								int iFromPreviousLeftX = (int) ((x - geom.width/2)/terrain.segmentWidth);
								int left_min_index = iFromPreviousLeftX;
								int left_max_index = iFromLeftX;
								leftPoint[0] = x - geom.width/2;
								leftPoint[1] = y - geom.height/2;
									
								int iFromPreviousRightX = (int) ((x + geom.width/2)/terrain.segmentWidth);
								int right_min_index = iFromPreviousRightX;
								int right_max_index = iFromRightX;
								rightPoint[0] = x + geom.width/2;
								rightPoint[1] = y - geom.height/2;
								
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

								float firstIntersection[] = new float[3];
								firstIntersection[2] = Float.MAX_VALUE;
								
								for(int s = left_min_index; s <= left_max_index; s++)
								{
									float xFromIndex = s*terrain.segmentWidth;
									float xFromNextIndex = (s+1)*terrain.segmentWidth;
									
									segmentLeft[0] = xFromIndex;
									segmentLeft[1] = terrain.previousPoints[s];
									segmentLeftV[0] = 0;
									segmentLeftV[1] = terrain.points[s]-terrain.previousPoints[s];
									segmentRight[0] = xFromNextIndex;
									segmentRight[1] = terrain.previousPoints[s+1];
									segmentRightV[0] = 0;
									segmentRightV[1] = terrain.points[s+1]-terrain.previousPoints[s+1];
									
									boolean intersected = findCollision(leftPoint, vx, vy, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
									if(intersected)
									{
										if(lastIntersect[2] < firstIntersection[2])
										{
											firstIntersection[0] = lastIntersect[0];
											firstIntersection[1] = lastIntersect[1];
											firstIntersection[2] = lastIntersect[2];
										}
									}
								}
								
								for(int s = right_min_index; s <= right_max_index; s++)
								{
									float xFromIndex = s*terrain.segmentWidth;
									float xFromNextIndex = (s+1)*terrain.segmentWidth;
									
									segmentLeft[0] = xFromIndex;
									segmentLeft[1] = terrain.previousPoints[s];
									segmentLeftV[0] = 0;
									segmentLeftV[1] = terrain.points[s]-terrain.previousPoints[s];
									segmentRight[0] = xFromNextIndex;
									segmentRight[1] = terrain.previousPoints[s+1];
									segmentRightV[0] = 0;
									segmentRightV[1] = terrain.points[s+1]-terrain.previousPoints[s+1];
									
									boolean intersected = findCollision(rightPoint, vx, vy, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
									if(intersected)
									{
										if(lastIntersect[2] < firstIntersection[2])
										{
											firstIntersection[0] = lastIntersect[0];
											firstIntersection[1] = lastIntersect[1];
											firstIntersection[2] = lastIntersect[2];
										}
									}
								}

								// Somehow a collision got missed, at least let the drawable know that it is beneath the terrain
								if(firstIntersection[2] == Float.MAX_VALUE)
								{
									engine.removeMovable(i);
									//System.out.println("Dirt fucking missed");
								}
								else if(firstIntersection[2] != Float.MAX_VALUE)
								{
									addDirtToTerrain(firstIntersection, i);
								}

								dirt_removed = true;
							}
						}
						
						if(!dirt_removed)
						{
							x += vx;
							y += vy;
							
							movables[movable_index+GameEngine.X] = x;
							movables[movable_index+GameEngine.Y] = y;
							movables[movable_index+GameEngine.VX] = vx;
							movables[movable_index+GameEngine.VY] = vy;
						}
						else
						{
							i--;
							num_movables--;
						}
					}
				}
				synchronized (collidables) 
				{ 
					ListIterator<Drawable> itr = collidables.listIterator();
					while(itr.hasNext())
					{
						Drawable d = itr.next();
						totalForce[0] = 0;
						totalForce[1] = 0;
						
						for(int f = 0; f < pointForces.size(); f++)
						{
							float[] force = pointForces.get(f);
							float distance_squared = (force[0]-d.p[0])*(force[0]-d.p[0]) + (force[1]-d.p[1])*(force[1]-d.p[1]);
							float distance = (float) Math.sqrt(distance_squared);
							float distance_factor = (float) (1 / (1 + distance_squared));
							forceDirection[0] = (d.p[0] - force[0])/distance;
							forceDirection[1] = (d.p[1] - force[1])/distance;
							totalForce[0] += forceDirection[0]*distance_factor*force[2];
							totalForce[1] += forceDirection[1]*distance_factor*force[2];
						}
						
						d.a[0] = totalForce[0]/d.m;
						d.a[1] = totalForce[1]/d.m;
						
						d.v[0] += d.a[0];
						d.v[1] += d.a[1];
						
						d.v[1] += GRAVITY;
						
						float next_x = d.p[0] + d.v[0];
						float next_y = d.p[1] + d.v[1];
						
						int iFromLeftX = (int) ((next_x-d.width/2)/terrain.segmentWidth);
						int iFromRightX = (int) ((next_x+d.width/2)/terrain.segmentWidth);
						
						if(iFromLeftX < 0 || iFromRightX >= terrain.points.length-1)
						{
							d.removeFromGLEngine = true;
							d.removeFromPhysicsEngine = true;
						}
						else
						{						
							double leftPercent = ((next_x-d.width/2) % terrain.segmentWidth) / terrain.segmentWidth;
							double rightPercent = ((next_x+d.width/2) % terrain.segmentWidth) / terrain.segmentWidth;
							double landYatLeftX = terrain.points[iFromLeftX] + (terrain.points[iFromLeftX + 1] - terrain.points[iFromLeftX]) * leftPercent;
							double landYatRightX = terrain.points[iFromRightX] + (terrain.points[iFromRightX + 1] - terrain.points[iFromRightX]) * rightPercent;
							
							boolean leftIntersected = (next_y-d.height/2) <= landYatLeftX;
							boolean rightIntersected = (next_y-d.height/2) <= landYatRightX;
							
							if(leftIntersected || rightIntersected || d.width > 1)
							{
								int iFromPreviousLeftX = (int) ((d.p[0] - d.width/2)/terrain.segmentWidth);
								int left_min_index = iFromPreviousLeftX;
								int left_max_index = iFromLeftX;
								leftPoint[0] = d.p[0] - d.width/2;
								leftPoint[1] = d.p[1] - d.height/2;
									
								int iFromPreviousRightX = (int) ((d.p[0] + d.width/2)/terrain.segmentWidth);
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

								float firstIntersection[] = new float[3];
								firstIntersection[2] = Float.MAX_VALUE;
	
								if(leftIntersected || rightIntersected)
								{
									for(int s = left_min_index; s <= left_max_index; s++)
									{
										float xFromIndex = s*terrain.segmentWidth;
										float xFromNextIndex = (s+1)*terrain.segmentWidth;
										
										segmentLeft[0] = xFromIndex;
										segmentLeft[1] = terrain.previousPoints[s];
										segmentLeftV[0] = 0;
										segmentLeftV[1] = terrain.points[s]-terrain.previousPoints[s];
										segmentRight[0] = xFromNextIndex;
										segmentRight[1] = terrain.previousPoints[s+1];
										segmentRightV[0] = 0;
										segmentRightV[1] = terrain.points[s+1]-terrain.previousPoints[s+1];
										
										boolean intersected = findCollision(leftPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < firstIntersection[2])
											{
												firstIntersection[0] = lastIntersect[0];
												firstIntersection[1] = lastIntersect[1];
												firstIntersection[2] = lastIntersect[2];
											}
										}
									}
									
									for(int s = right_min_index; s <= right_max_index; s++)
									{
										float xFromIndex = s*terrain.segmentWidth;
										float xFromNextIndex = (s+1)*terrain.segmentWidth;
										
										segmentLeft[0] = xFromIndex;
										segmentLeft[1] = terrain.previousPoints[s];
										segmentLeftV[0] = 0;
										segmentLeftV[1] = terrain.points[s]-terrain.previousPoints[s];
										segmentRight[0] = xFromNextIndex;
										segmentRight[1] = terrain.previousPoints[s+1];
										segmentRightV[0] = 0;
										segmentRightV[1] = terrain.points[s+1]-terrain.previousPoints[s+1];
										
										boolean intersected = findCollision(rightPoint, d.v, segmentLeft, segmentLeftV, segmentRight, segmentRightV, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < firstIntersection[2])
											{
												firstIntersection[0] = lastIntersect[0];
												firstIntersection[1] = lastIntersect[1];
												firstIntersection[2] = lastIntersect[2];
											}
										}
									}
	
									// Somehow a collision got missed, at least let the drawable know that it is beneath the terrain
									if(firstIntersection[2] == Float.MAX_VALUE)
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
										float xFromIndex = s*terrain.segmentWidth;
										
										segmentPoint[0] = xFromIndex;
										segmentPoint[1] = terrain.previousPoints[s];
										segmentV[0] = 0; 
										segmentV[1] = terrain.points[s]-terrain.previousPoints[s];
										
										boolean intersected = findCollision(segmentPoint, segmentV, leftPoint, d.v, rightPoint, d.v, lastIntersect);
										if(intersected)
										{
											if(lastIntersect[2] < firstIntersection[2])
											{
												firstIntersection[0] = lastIntersect[0];
												firstIntersection[1] = lastIntersect[1];
												firstIntersection[2] = lastIntersect[2];
											}
										}
									}
								}

								
								if(firstIntersection[2] != Float.MAX_VALUE)
								{
									d.intersectTerrain(terrain, firstIntersection);
								}
							}
						}
						
						if(d.removeFromPhysicsEngine)
						{
							d.removeFromPhysicsEngine=false;
							itr.remove();
						}
						else
						{
							d.p[0] += d.v[0];
							d.p[1] += d.v[1];
						}
					}
				}
			}

			terrain.update();
			
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
	public boolean findCollision(float p1x, float p1y, float v1x, float v1y, float p2x, float p2y, float v2x, float v2y, float p3x, float p3y, float v3x, float v3y, float[] result)
	{
		float EPSILON_A = .002f;
		double t[] = {-1f, -1f};

		// Line segment points haven't moved, perform standard point / line segment intersection
		if(v2x > -EPSILON && v2x < EPSILON && 
			v2y > -EPSILON && v2y < EPSILON && 
			v3x > -EPSILON && v3x < EPSILON && 
			v3y > -EPSILON && v3y < EPSILON)
		{
			t[0] = findCollision_pointAndLinesegment(p1x, p1y, v1x, v1y, p2x, p2y, p3x, p3y);
		}
		// Line segment only moving vertically
		else if(v2x > -EPSILON && v2x < EPSILON && v3x > -EPSILON && v3x < EPSILON)
		{
			// Both end points moving vertically at the same velocity (I can't believe I need special code for this...)
			if(Math.abs(v2y - v3y) < EPSILON)
			{
				double denom = -p2x+p3x; 
				double dif = -p2y + p3y;
				
				t[0] = (-p1y + p2y + p1x*dif/denom - p2x*dif/denom)/(-v1x*dif/denom + v1y - v2y);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v3y > -EPSILON && v3y < EPSILON)
			{
				double C = p1y*p2x - p1x*p2y - p1y*p3x + p2y*p3x + p1x*p3y - p2x*p3y;
				double B = -p2y*v1x + p3y*v1x + p2x*v1y - p3x*v1y - p1x*v2y + p3x*v2y;
				double A = v1x*v2y;
				double sqrt = Math.sqrt(4*A*C + B*B);
				double frac = -1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// One of the end points is not moving, the other is moving vertically
			else if(v2y > -EPSILON && v2y < EPSILON)
			{
				double C = p1y*p2x - p1x*p2y - p1y*p3x + p2y*p3x + p1x*p3y - p2x*p3y;
				double B = -p2y*v1x + p3y*v1x + p2x*v1y - p3x*v1y + p1x*v3y - p2x*v3y;
				double A = v1x*v3y;
				double sqrt = Math.sqrt(-4*A*C + B*B);
				double frac = 1/(2*A);
				
				t[0] = frac*(-B - sqrt);
				t[1] = frac*(-B + sqrt);
			}
			// End points moving vertically at different velocities
			else
			{
				double A = -v1x*v2y + v1x*v3y;
				double B = -p2y*v1x + p3y*v1x + p2x*v1y - p3x*v1y - p1x*v2y + p3x*v2y + p1x*v3y - p2x*v3y;
				double C = p1y*p2x - p1x*p2y - p1y*p3x + p2y*p3x + p1x*p3y - p2x*p3y;
				double sqrt = Math.sqrt(B*B - 4*A*C);
				
				t[0] = (-B + sqrt) / (2*A);
				t[1] = (-B - sqrt) / (2*A);
			}
		}
		// Line segment endpoints both moving at the same velocity
		else if(Math.abs(v2x-v3x) < EPSILON && Math.abs(v3y-v2y) < EPSILON)
		{
			t[0] = findCollision_pointAndMovingLinesegment(p1x, p1y, v1x, v1y, p2x, p2y, p3x, p3y, v2x, v2y);
		}
		// Line segment and point both moving vertically
		else if(v1x > -EPSILON && v1x < EPSILON && v2x > -EPSILON && v2x < EPSILON && v3x > -EPSILON && v3x < EPSILON)
		{
			double denom = -p2x+p3x; 
			double dif = p1x - p2x;
			t[0] = (-p1y + p2y - (dif*p2y)/denom + (dif*p3y)/denom)/(v1y - v2y + (dif*v2y)/denom - (dif*v3y)/denom);
		}
		// End points of line segment moving at different velocities, point also moving
		else
		{
			findCollision_pointAndLinesegmentWithIndependentlyMovingEndpoints(p1x, p1y, v1x, v1y, p2x, p2y, v2x, v2y, p3x, p3y, v3x, v3y, t);
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
		result[0] = p1x + v1x*final_t;
		result[1] = p1y + v1y*final_t;
		result[2] = final_t;
		float s1x = p2x+v2x*final_t;
		float s1y = p2y+v2y*final_t;
		float s2x = p3x+v3x*final_t;
		float s2y = p3y+v3y*final_t;
		if(!isBetween(result[0], s1x, s2x, EPSILON_A))
		{
			return false;
		}
		if(!isBetween(result[1], s1y, s2y, EPSILON_A))
		{
			return false;
		}
		
		return true;
	}
	
	public boolean findCollision(float[] p1, float[] v1, float[] p2, float[] v2, float[] p3, float[] v3, float[] result)
	{
		return findCollision(p1[0], p1[1], v1[0], v1[1], p2[0], p2[1], v2[0], v2[1], p3[0], p3[1], v3[0], v3[1], result);
	}
	
	public boolean findCollision(float[] p1, float[] v1, float[] p2, float v2x, float v2y, float[] p3, float v3x, float v3y, float[] result)
	{
		return findCollision(p1[0], p1[1], v1[0], v1[1], p2[0], p2[1], v2x, v2y, p3[0], p3[1], v3x, v3y, result);
	}
	
	public boolean findCollision(float[] p1, float v1x, float v1y, float[] p2, float[] v2, float[] p3, float[] v3, float[] result)
	{
		return findCollision(p1[0], p1[1], v1x, v1y, p2[0], p2[1], v2[0], v2[1], p3[0], p3[1], v3[0], v3[1], result);
	}
	
	public float findCollision_pointAndLinesegment(float p1x, float p1y, float v1x, float v1y, float p2x, float p2y, float p3x, float p3y)
	{
		float denom = -p2x + p3x;
		float dif = -p2y + p3y;
		float t = (-p1y + p2y + p1x*dif/denom - p2x*dif/denom) / (-v1x*dif/denom + v1y);
		
		return t;
	}
	
	public double findCollision_pointAndMovingLinesegment(float p1x, float p1y, float v1x, float v1y, float p2x, float p2y, float p3x, float p3y, float lvx, float lvy)
	{
		float denom = -p2x + p3x;
		float dif = -p2y + p3y;
		
		// point moving vertically
		if(v1x > -EPSILON && v1x < EPSILON)
		{
			return (p1y - p2y - (p1x*dif)/denom + (p2x*dif)/denom)/(-v1y - (dif*lvx)/denom + lvy);
		}
		// point moving non-vertically
		else
		{
			return (p1y - p2y - (p1x*dif)/denom + (p2x*dif)/denom)/((dif*v1x)/denom - v1y - (dif*lvx)/denom + lvy);
		}
	}
	
	public void findCollision_pointAndLinesegmentWithIndependentlyMovingEndpoints(float p1x, float p1y, float v1x, float v1y, float p2x, float p2y, float v2x, float v2y, float p3x, float p3y, float v3x, float v3y, double[] t)
	{
		double A = v1y*v2x - v1x*v2y - v1y*v3x + v2y*v3x + v1x*v3y - v2x*v3y;
		
		if(A == 0)
		{
			return;
		}
		
		double B = -p2y*v1x + p3y*v1x + p2x*v1y - p3x*v1y + p1y*v2x - p3y*v2x - p1x*v2y + p3x*v2y - p1y*v3x + p2y*v3x + p1x*v3y - p2x*v3y;
		double C = p1y*p2x - p1x*p2y - p1y*p3x + p2y*p3x + p1x*p3y - p2x*p3y;
		double sqrt = Math.sqrt(B*B - 4*A*C);
				
		t[0] = (-B + sqrt) / (2*A);
		t[1] = (-B - sqrt) / (2*A);
	}
	
	public void addDirtToTerrain(float[] intersect, int i)
	{
		int dirt_index = (i*GameEngine.ITEMS_PER_MOVABLE);
		
		Terrain t = engine.getTerrain();
		float[] movables = engine.getMovables();
		float dirtVolume = movables[dirt_index+GameEngine.VOLUME];
		
		int index = (int)(intersect[0] / t.segmentWidth);
		index = Math.max(0, index);
		index = Math.min(t.points.length - 2, index);
		
		float leftHeight = t.points[index];
		float rightHeight = t.points[index + 1];
		
		if(rightHeight < leftHeight)
		{
			index++;
		}
		
		while(dirtVolume > .001 && index > 0 && index <= t.NUM_POINTS-2)
		{
			float currentHeight = t.points[index] + t.offsets[index];
			leftHeight = t.points[index - 1] + t.offsets[index - 1];
			rightHeight = t.points[index + 1] + t.offsets[index + 1];

			float rDiff = currentHeight - rightHeight;
			float lDiff = currentHeight - leftHeight;
			
			if(rDiff > 0 && rDiff >= lDiff)
			{
				float segmentLength = fastSqrt((float) (Math.pow((index + 1) * t.segmentWidth - index * t.segmentWidth, 2) + Math.pow(rightHeight - currentHeight, 2)));
				float slopeFactor = Math.min(.5f, (currentHeight - rightHeight)/segmentLength);
				float amount = dirtVolume*(1 - slopeFactor)/t.DIRT_VISCOSITY;
				t.offsets[index] += amount;
				dirtVolume -= amount;
				index++;
			}
			else if( lDiff > 0 && lDiff >= rDiff)
			{
				float segmentLength = fastSqrt((float) (Math.pow((index - 1) * t.segmentWidth - index * t.segmentWidth, 2) + Math.pow(leftHeight - currentHeight, 2)));
				float slopeFactor = Math.min(.5f, (currentHeight - leftHeight)/segmentLength);
				float amount = dirtVolume*(1 - slopeFactor)/t.DIRT_VISCOSITY;
				t.offsets[index] += amount;
				dirtVolume -= amount;
				index--;
			}
			else if (currentHeight <= leftHeight && currentHeight <= rightHeight)
			{
				// current point is lower than neighbors
				float amount = dirtVolume / t.DIRT_VISCOSITY;
				t.offsets[index] += amount;
				dirtVolume -= amount;
			}
		}
		
		t.offsets[index] += dirtVolume;
		dirtVolume=0;
		
		engine.removeMovable(i);
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
	
	// credit jeff_g on forum.processing.org
	public static float fastSqrt(float x) 
	{
	    return Float.intBitsToFloat(532483686 + (Float.floatToRawIntBits(x) >> 1));
	}

}

package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable
{
	static final float TURRENT_LENGTH = 1f;
	static final float WIDTH = 4f;
	static final float HEIGHT = 4f;
	float GRAVITY = .01f;
	float tFriction = .1f;
	float acceleration = 0;
	float groundSpeed = 5;
	float baseGeometry[];

	public Drawable turret = new Drawable();
	float turretLength = 1;

	public Tank()
	{
		width = WIDTH;
		height = HEIGHT;
		m = 3;
		this.registerDrawable(turret);
	}

	public void buildGeometry(float viewWidth, float viewHeight)
	{
		float[] baseGeometry = {
				// X, Y
				-width / 2, -height / 2, 0, width / 2, -height / 2f, 0, 0, height / 2, 0 };

		this.drawMode = GL2.GL_LINE_LOOP;
		super.vertices = baseGeometry;

		turretLength = TURRENT_LENGTH;
		float turretGeometry[] = { 0, 0, 0, 0, turretLength, 0 };
		turret.drawMode = GL2.GL_LINES;
		turret.vertices = turretGeometry;
		turret.p[0] = 0;
		turret.p[1] = height / 2;
	}

	public void intersectTerrain(Terrain t, float[] intersect)
	{
		if(intersect[4] == 3)
		{
			// middle intersection
			if(intersect[2] == 1 || this.v[1] == 0)
			{
				return;
			}
			this.p[0] += this.v[0] * intersect[2];
			this.p[1] += this.v[1] * intersect[2];
			System.out.println("correcting position to intersect point");
			
			float velocityMagnitude = (float) Math.sqrt((this.v[0] * this.v[0]) + this.v[1] * this.v[1]);
			if(this.v[0] > 0)
			{
				this.v[0] = velocityMagnitude;
			}
			else
			{
				this.v[0] = -velocityMagnitude;
			}
			this.v[1] = 0;


			this.needsPositionUpdated = false;
		}
		else 
		{
			float velocityMagnitude = (float) Math.sqrt((this.v[0] * this.v[0]) + this.v[1] * this.v[1]);
			
			if(intersect[4] == 1) // left point
			{
				p[0] = (intersect[0] + width/2);
			}
			else // if(intersect[4] == 2) // right point
			{
				p[0] = intersect[0] - width/2;
			}
			p[1] = intersect[1] + height/2;
			
			this.needsPositionUpdated = false;
			
			System.out.println("t" + intersect[2]);
			System.out.println(intersect[0] - p[0]);
			System.out.println();
			
			
			
			int index = (int) intersect[3];
	
			float vectorToLeftTerrainPointX = index * t.segmentWidth - (index+1) * t.segmentWidth;
			float vectorToLeftTerrainPointY = (t.points[index] + t.offsets[index]) -  (t.points[index+1] + t.offsets[index+1]);
			
			float normalDistance = (float) Math.sqrt(vectorToLeftTerrainPointX * vectorToLeftTerrainPointX + vectorToLeftTerrainPointY
					* vectorToLeftTerrainPointY);
			
			float normalLX = vectorToLeftTerrainPointX / normalDistance;
			float normalLY = vectorToLeftTerrainPointY / normalDistance;
			
			if(this.v[0] > 0)
			{
				this.v[0] = -velocityMagnitude * normalLX - .001f*this.v[0];
				this.v[1] = -velocityMagnitude * normalLY + Math.abs(.001f*this.v[1]);
			}
			else
			{
				this.v[0] = velocityMagnitude * normalLX - .001f*this.v[0];
				this.v[1] = velocityMagnitude * normalLY + Math.abs(.001f*this.v[1]);
			}
			
			if(intersect[2] == 1)
			{
				// Update position to slide along terrain, mmm sexy
				this.p[0] += this.v[0];
				this.p[1] += this.v[1];
				System.out.println("slide");
			}
		}
	}

	public void underTerrain(Terrain t)
	{
		/*float leftX = this.p[0] - width / 2;
		float leftY = this.p[1] - height / 2;
		float rightX = this.p[0] + width / 2;
		float rightY = this.p[1] - height / 2;
		System.out.println("x" + this.v[0]);
		System.out.println(this.v[1]);
		System.out.println(leftX * t.segmentWidth);
		System.out.println((int) (leftX * t.segmentWidth));
		System.out.println("underT");
		float rPercentX = ((this.p[0] + width / 2) / t.segmentWidth) - (int) ((this.p[0] + width / 2) / t.segmentWidth);
		float rLandY = t.points[(int) (rightX / t.segmentWidth)] + (t.points[(int) (rightX / t.segmentWidth) + 1] - t.points[(int) (rightX / t.segmentWidth)])
				* rPercentX;
		float lPercentX = (((this.p[0] - width / 2) / t.segmentWidth) - (int) ((this.p[0] - width / 2) / t.segmentWidth));
		float lLandY = t.points[(int) (leftX / t.segmentWidth)] + (t.points[(int) (leftX / t.segmentWidth) + 1] - t.points[(int) (leftX / t.segmentWidth)])
				* lPercentX;
		if (rLandY - rightY >= lLandY - leftY)
		{
			this.p[0] = rightX - width / 2 + 1f;
			this.p[1] = rLandY + height / 2 + 1f;
		} else if (rLandY - rightY < lLandY - leftY)
		{
			this.p[0] = leftX + width / 2 + 1f;
			this.p[1] = lLandY + height / 2 + 1f;
		}*/
		// removeFromPhysicsEngine = true;
	}

	// checks to see if point C lies to the left of line segment AB
	public boolean isLeft(float aX, float aY, float bX, float bY, float cX, float cY)
	{
		return ((bX - aX) * (cY - aY) - (bY - aY) * (cX - aX)) > 0;
	}
}

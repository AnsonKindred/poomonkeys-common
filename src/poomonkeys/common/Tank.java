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
		this.p[0] += this.v[0] * intersect[2];
		this.p[1] += this.v[1] * intersect[2];
		// removeFromPhysicsEngine = true;
		float velocityVector = (float) Math.sqrt((this.v[0] * this.v[0]) + this.v[1] * this.v[1]);
		float force = velocityVector * m;
		if (force > 1000)
		{
			float xDistance = (.5f * m * (this.v[0] * this.v[0]));
			float yDistance = (.5f * m * (this.v[1] * this.v[1]));
			if (this.v[0] < 0)
			{
				xDistance = -xDistance;
			}
			if (this.v[1] < 0)
			{
				yDistance = -yDistance;
			}
			this.p[0] += xDistance;
			this.p[1] += yDistance;
			t.explodeRectangle(this.p[0], this.p[1], width / 2);
			this.v[0] = 0;
			this.v[1] = 0;
			removeFromPhysicsEngine = true;
		} else
		{
			int index = (int) (intersect[0] / t.segmentWidth);
			index = Math.max(0, index);
			index = Math.min(t.points.length - 2, index);

			float lX = index * t.segmentWidth - intersect[0];
			float lY = (t.points[index] + t.offsets[index]) - intersect[1];
			// if its on an endpoint, its on an endpoint for two different
			// linesegments so it chooses the one thats exactly an endpoint and
			// these end up as 0 in denominator
			if (lX + lY == 0)
			{
				return;
				// lX = (index - 1) * t.segmentWidth - intersect[0];
				// lY = (t.points[index - 1] + t.offsets[index - 1]) -
				// intersect[1];
			}
			if (lX == t.segmentWidth)
			{
				if (this.v[0] > 0 && t.points[index + 2] < t.points[index + 1])
				{
					return;
				} else if (this.v[0] > 0 && t.points[index + 2] >= t.points[index + 1])
				{
					// actually rX and rY
					lX = (index + 2) * t.segmentWidth - intersect[0];
					lY = (t.points[index + 2] + t.offsets[index + 2]) - intersect[1];
				} else if (this.v[0] <= 0 && t.points[index - 1] < t.points[index])
				{
					return;
				} else if (this.v[0] <= 0 && t.points[index - 1] >= t.points[index])
				{
					// actually rX and rY
					lX = (index - 1) * t.segmentWidth - intersect[0];
					lY = (t.points[index - 1] + t.offsets[index - 1]) - intersect[1];
				}

			}
			float dX = this.v[0];
			float dY = this.v[1];
			float dotProduct = lX * dX + lY * dY;
			float angle = (float) Math.acos(dotProduct / (Math.sqrt(lX * lX + lY * lY) * Math.sqrt(dX * dX + dY * dY)));
			if (angle > Math.PI / 2)
			{
				float normalDistance = (float) Math.sqrt(lX * lX + lY * lY);
				float normalLX = lX / normalDistance;
				float normalLY = lY / normalDistance;
				this.v[0] = -velocityVector * normalLX;
				this.v[1] = -velocityVector * normalLY;
			} else
			{
				// removeFromPhysicsEngine = true;
				float normalDistance = (float) Math.sqrt(lX * lX + lY * lY);
				//System.out.println(normalDistance);
				float normalLX = lX / normalDistance;
				float normalLY = lY / normalDistance;

				this.v[0] = velocityVector * normalLX;
				this.v[1] = velocityVector * normalLY;
			}
			//System.out.println("x"+this.v[0]);
			//System.out.println(this.v[1]);

			// this.v[0] = 0;
			// this.v[1] = 0;
		}
	}

	public void underTerrain(Terrain t)
	{
		//System.out.println("underT");
		float leftX = this.p[0] - width / 2;
		float leftY = this.p[1] - height / 2;
		float rightX = this.p[0] + width / 2;
		float rightY = this.p[1] - height / 2;
		float rPercentX = ((this.p[0] + width / 2) / t.segmentWidth) - (int) ((this.p[0] + width / 2) / t.segmentWidth);
		float rLandY = t.points[(int) (rightX / t.segmentWidth)] + (t.points[(int) (rightX / t.segmentWidth) + 1] - t.points[(int) (rightX / t.segmentWidth)])
				* rPercentX;
		float lPercentX = (((this.p[0] - width / 2) / t.segmentWidth) - (int) ((this.p[0] - width / 2) / t.segmentWidth));
		float lLandY = t.points[(int) (leftX / t.segmentWidth)] + (t.points[(int) (leftX / t.segmentWidth) + 1] - t.points[(int) (leftX / t.segmentWidth)])
				* lPercentX;
		if (rLandY - rightY >= lLandY - leftY)
		{
			this.p[0] = rightX - width / 2;
			this.p[1] = rLandY + height / 2;
		} else if (rLandY - rightY < lLandY - leftY)
		{
			this.p[0] = leftX + width / 2;
			this.p[1] = lLandY + height / 2;
		}
		// removeFromPhysicsEngine = true;
	}

	// credit jeff_g on forum.processing.org
	public static float fastSqrt(float x)
	{
		return Float.intBitsToFloat(532483686 + (Float.floatToRawIntBits(x) >> 1));
	}

	public float findCollision_pointAndLinesegment(float p1x, float p1y, float v1x, float v1y, float p2x, float p2y, float p3x, float p3y)
	{
		float denom = -p2x + p3x;
		float dif = -p2y + p3y;
		float t = (-p1y + p2y + p1x * dif / denom - p2x * dif / denom) / (-v1x * dif / denom + v1y);

		return t;
	}
}
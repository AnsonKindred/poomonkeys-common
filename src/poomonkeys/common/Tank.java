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
		// .5*m*v2 = F*d
		// d = v0t + 0.5at2
		// removeFromPhysicsEngine = true;

		this.p[0] += this.v[0] * intersect[2];
		this.p[1] += this.v[1] * intersect[2];
		removeFromPhysicsEngine = true;
		float velocityVector = (float) Math.sqrt((this.v[0] * this.v[0]) + this.v[1] * this.v[1]);
		float force = velocityVector * m;
		groundSpeed = this.v[0];
		if (force > 10000)
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
			float currentHeight = intersect[1];
			float leftHeight = t.points[index] + t.offsets[index];
			float rightHeight = t.points[index + 1] + t.offsets[index + 1];

			float lDiff = currentHeight - leftHeight;
			float rDiff = currentHeight - rightHeight;

			if (rDiff > 0 && rDiff >= lDiff)
			{
				float angle = (float) Math.atan((rightHeight - leftHeight) / t.segmentWidth);
				System.out.println(angle);
				this.v[0] = (float) (groundSpeed * Math.cos(2 * Math.PI - angle));
				this.v[1] = (float) (groundSpeed * -Math.sin(2 * Math.PI - angle));

			} else if (lDiff > 0 && lDiff >= rDiff)
			{
				float angle = (float) Math.atan((leftHeight - rightHeight) / t.segmentWidth);
				System.out.println(angle);
				this.v[0] = (float) (groundSpeed * Math.cos(2 * Math.PI - angle));
				this.v[1] = (float) (groundSpeed * -Math.sin(2 * Math.PI - angle));
			} else if (currentHeight <= leftHeight && currentHeight <= rightHeight)
			{
				// current point is lower than neighbors
				// impossible to happen
				removeFromPhysicsEngine = true;
			}

			// this.v[0] = 0;
			// this.v[1] = 0;
		}
	}

	public void underTerrain(Terrain t)
	{
		float leftX = this.p[0] - width / 2;
		float leftY = this.p[1] - height / 2;
		float rightX = this.p[0] + width / 2;
		float rightY = this.p[1] - height / 2;
		float rPercentX = ((this.p[0] + (width / 2)) / t.segmentWidth) - (int) ((this.p[0] + width / 2) / t.segmentWidth);
		float rLandY = t.points[(int) (rightX / t.segmentWidth)] + (t.points[(int) (rightX / t.segmentWidth) + 1] - t.points[(int) (rightX / t.segmentWidth)])
				* rPercentX;
		float lPercentX = (((this.p[0] - (width / 2)) / t.segmentWidth) - (int) ((this.p[0] - width / 2) / t.segmentWidth));
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
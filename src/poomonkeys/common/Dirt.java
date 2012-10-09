package poomonkeys.common;

import javax.media.opengl.GL2;

public class Dirt extends Drawable
{
	public float volume;
	int TRAILLENGTH = 10;

	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry = new float[4 * 3];
		baseGeometry[0] = -width / 2;
		baseGeometry[1] = -height / 2;
		baseGeometry[2] = 0;
		baseGeometry[3] = -width / 2;
		baseGeometry[4] = height / 2;
		baseGeometry[5] = 0;
		baseGeometry[6] = width / 2;
		baseGeometry[7] = height / 2;
		baseGeometry[8] = 0;
		baseGeometry[9] = width / 2;
		baseGeometry[10] = -height / 2;
		baseGeometry[11] = 0;
		drawMode = GL2.GL_LINE_LOOP;
	}

	public void intersectTerrain(Terrain t, float x, float y)
	{
		// this.x = x;
		// this.y = y;

		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;

		int index = Math.round(x / t.segmentWidth);

		index = Math.max(0, index);
		index = Math.min(t.points.length - 1, index);
		// t.offsets[index] += this.volume;
		addDirt(t, index, x, true);
	}

	public void addDirt(Terrain t, int index, float x, boolean firstTime)
	{
		if (index < 1 || index > t.NUM_POINTS - 2)
		{
			return;
		}

		if (this.volume < .001)
		{
			t.offsets[index] += this.volume;
			return;
		}

		float currentHeight = t.points[index] + t.offsets[index];
		float leftHeight = t.points[index - 1] + t.offsets[index - 1];
		float rightHeight = t.points[index + 1] + t.offsets[index + 1];

		if (currentHeight > rightHeight && (leftHeight >= rightHeight || firstTime == false))
		{
			//System.out.println("C>R " + firstTime);
			float segmentLength = (float) Math.sqrt(Math.pow((index + 1) * t.segmentWidth - index * t.segmentWidth, 2)
					+ Math.pow(rightHeight - currentHeight, 2));
			float slopeFactor = Math.min(.5f, (currentHeight - rightHeight) / segmentLength);
			t.offsets[index + 1] += (this.volume - (this.volume) * slopeFactor) / 3;
			this.volume -= (this.volume - (this.volume) * slopeFactor) / 3;
			addDirt(t, index + 1, x, false);
			return;
		} 
		if (currentHeight > leftHeight && (rightHeight >= leftHeight || firstTime == false))
		{
			//System.out.println("C>L " + firstTime);
			float segmentLength = (float) Math.sqrt(Math.pow((index - 1) * t.segmentWidth - index * t.segmentWidth, 2)
					+ Math.pow(leftHeight - currentHeight, 2));
			float slopeFactor = Math.min(.5f, (currentHeight - leftHeight) / segmentLength);
			t.offsets[index - 1] += (this.volume - (this.volume) * slopeFactor) / 3;
			this.volume -= (this.volume - (this.volume) * slopeFactor) / 3;
			addDirt(t, index - 1, x, false);
			return;
		} 
		if (currentHeight <= leftHeight && currentHeight <= rightHeight)
		// current point is lower than neighbors
		{
			t.offsets[index] += this.volume / 1;
			this.volume -= this.volume / 1;
			addDirt(t, index, x, false);
			return;
		}
		System.out.println("cHeight " + currentHeight);
		System.out.println("rHeight " + rightHeight);
		System.out.println("lHeight " + leftHeight);
		System.out.println(firstTime);
		System.out.println("x " + x/t.segmentWidth);
		System.out.println(index);
	}
}

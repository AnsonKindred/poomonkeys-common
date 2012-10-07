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
		//this.x = x;
		//this.y = y;
		
		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;

		int index = Math.round(x / t.segmentWidth);

		index = Math.max(0, index);
		index = Math.min(t.points.length - 1, index);
		t.offsets[index] += this.volume;
		//addDirt(t, index);
	}

	public void addDirt(Terrain t, int index)
	{
		if (index < 1 || index > t.NUM_POINTS - 2)
		{
			return;
		}
		
		if(this.volume < .001)
		{
			t.offsets[index] += this.volume;
			return;
		}
		
		float currentHeight = t.points[index]+t.offsets[index];
		float leftHeight = t.points[index-1]+t.offsets[index-1];
		float rightHeight = t.points[index+1]+t.offsets[index+1];
		
		if(currentHeight > rightHeight)
		{
			float segmentLength = (float) Math.sqrt(Math.pow((index+1)*t.segmentWidth - index*t.segmentWidth, 2) + Math.pow(rightHeight-currentHeight, 2));
			float slopeFactor = Math.min(1, (currentHeight-rightHeight)/segmentLength);
			t.offsets[index] += (this.volume/2)*slopeFactor;
			this.volume -= (this.volume/2)*slopeFactor;
			addDirt(t, index+1);
		}
		else if(currentHeight > leftHeight)
		{
			float segmentLength = (float) Math.sqrt(Math.pow((index-1)*t.segmentWidth - index*t.segmentWidth, 2) + Math.pow(leftHeight-currentHeight, 2));
			float slopeFactor = Math.min(1, (currentHeight-leftHeight)/segmentLength);
			t.offsets[index] += (this.volume/2)*slopeFactor;
			this.volume -= (this.volume/2)*slopeFactor;
			addDirt(t, index-1);
		}
		else // current point is lower than neighbors
		{
			t.offsets[index] += this.volume/2;
			this.volume -= this.volume/2;
			addDirt(t, index);
		}
	}
}

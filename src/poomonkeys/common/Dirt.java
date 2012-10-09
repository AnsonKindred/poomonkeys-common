package poomonkeys.common;

import javax.media.opengl.GL2;

public class Dirt extends Drawable
{
	public float volume;
	float min_index, max_index;

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

	public void intersectTerrain(Terrain t, float[] intersect)
	{
		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;

		int index = (int)(intersect[0] / t.segmentWidth);
		index = Math.max(0, index);
		index = Math.min(t.points.length - 2, index);
		
		float leftHeight = t.points[index];
		float rightHeight = t.points[index + 1];
		
		if(rightHeight < leftHeight)
		{
			index++;
		}
		
		addDirt(t, index);
	}
	
	public void addDirt(Terrain t, int index)
	{
		while(this.volume > .001 && index > 0 && index <= t.NUM_POINTS-2)
		{
			float currentHeight = t.points[index] + t.offsets[index];
			float leftHeight = t.points[index - 1] + t.offsets[index - 1];
			float rightHeight = t.points[index + 1] + t.offsets[index + 1];

			float rDiff = currentHeight - rightHeight;
			float lDiff = currentHeight - leftHeight;
			
			if(rDiff >= lDiff)
			{
				float segmentLength = (float) Math.sqrt(Math.pow((index + 1) * t.segmentWidth - index * t.segmentWidth, 2)
						+ Math.pow(rightHeight - currentHeight, 2));
				float slopeFactor = Math.min(.5f, (currentHeight - rightHeight) / segmentLength);
				t.offsets[index] += (this.volume - (this.volume) * slopeFactor) / t.DIRT_VISCOSITY;
				this.volume -= (this.volume - (this.volume) * slopeFactor) / t.DIRT_VISCOSITY;
				index++;
			}
			else if(lDiff >= rDiff)
			{
				float segmentLength = (float) Math.sqrt(Math.pow((index - 1) * t.segmentWidth - index * t.segmentWidth, 2)
						+ Math.pow(leftHeight - currentHeight, 2));
				float slopeFactor = Math.min(.5f, (currentHeight - leftHeight) / segmentLength);
				t.offsets[index] += (this.volume - (this.volume) * slopeFactor) / t.DIRT_VISCOSITY;
				this.volume -= (this.volume - (this.volume) * slopeFactor) / t.DIRT_VISCOSITY;
				index--;
			}
			else if (currentHeight <= leftHeight && currentHeight <= rightHeight)
			{
				// current point is lower than neighbors
				t.offsets[index] += this.volume / t.DIRT_VISCOSITY;
				this.volume -= this.volume / t.DIRT_VISCOSITY;
			}
		}
		
		t.offsets[index] += this.volume;
		this.volume=0;
		
		return;
	}
	
	public void underTerrain()
	{
		super.underTerrain();
		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;
	}
}

package poomonkeys.common;
import javax.media.opengl.GL2;

public class Dirt extends Drawable
{
	public float volume;

	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry = new float[4*3];
		baseGeometry[0]=-width/2; baseGeometry[1]=-height/2; baseGeometry[2]=0;
		baseGeometry[3]=-width/2; baseGeometry[4]=height/2; baseGeometry[5]=0;
		baseGeometry[6]=width/2; baseGeometry[7]=height/2; baseGeometry[8]=0;
		baseGeometry[9]=width/2; baseGeometry[10]=-height/2; baseGeometry[11]=0;
		drawMode = GL2.GL_LINE_LOOP;
	}
	
	public void intersectTerrain(float x, float y)
	{
		this.p.x = x;
		this.p.y = y;

		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;
		
		Terrain t = ExplosionController.getInstance().t;
		int firstIndex = Math.round((x - width/2) / t.segmentWidth);
		int lastIndex = Math.round((x + width/2) / t.segmentWidth);
		firstIndex = Math.max(0, firstIndex);
		lastIndex = Math.min(t.points.length-1, lastIndex);
		
		for(int j = firstIndex; j <= lastIndex; j++)
		{
			float amount_outside = (float) (Math.max(x+width/2 - (j+.5)*t.segmentWidth, 0) + Math.max((j-.5)*t.segmentWidth-x+width/2, 0));
			float percent_outside = amount_outside / width;
			float percent_overlap = 1 - percent_outside;
			
			t.offsets[j] += volume * percent_overlap;
		}
	}
}

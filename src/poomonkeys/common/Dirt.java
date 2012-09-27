package poomonkeys.common;
import javax.media.opengl.GL2;

public class Dirt extends Drawable
{
	float vx, vy;
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
	
}

package poomonkeys.common;

public class Dirt extends Drawable
{
	public float volume;
	float min_index, max_index;
	
	public Dirt()
	{
		geometry = DirtGeometry.getInstance();
	}
	
	public void setHeight(float h)
	{
		super.setHeight(h);
		scale.y = h;
	}

	public void setWidth(float w)
	{
		super.setWidth(w);
		scale.x = w;
	}
}

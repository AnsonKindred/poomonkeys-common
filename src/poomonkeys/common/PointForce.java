package poomonkeys.common;

public class PointForce extends Point2D
{
	public float magnitude;
	public PointForce(float x, float y, float m)
	{
		super(x, y);
		this.magnitude = m;
	}
}

package poomonkeys.common;

class VectorUtil
{

    public static void normalize2D(Point2D p)
    {
        float total = mag2D(p);
		if(total != 0)
		{
			p.x /= total;
			p.y /= total;
		}
    }
    
    public static float mag2D(Point2D p)
	{
		return (float) Math.sqrt(Math.pow(p.x, 2)+Math.pow(p.y, 2));
	}

	public static void scaleTo2D(Point2D p, float mag)
	{
		if(mag != 0)
		{
			float total = mag2D(p);
			if(total != 0)
			{
				total /= mag;
				p.x /= total;
				p.y /= total;
			}
		}
	}

	static void mult2D(Point2D p, float vm)
	{
		p.x *= vm;
		p.y *= vm;
	}
	
	static float distance(Point2D p1, Point2D p2)
	{
		return (float) Math.sqrt(Math.pow(p2.x-p1.x, 2) + Math.pow(p2.y-p1.y, 2));
	}
	
	public static void normalize2D(float[] p)
    {
        float total = mag2D(p);
		if(total != 0)
		{
			p[0] /= total;
			p[1] /= total;
		}
    }
    
    public static float mag2D(float[] p)
	{
		return (float) Math.sqrt(Math.pow(p[0], 2)+Math.pow(p[1], 2));
	}

	public static void scaleTo2D(float[] p, float mag)
	{
		if(mag != 0)
		{
			float total = mag2D(p);
			if(total != 0)
			{
				total /= mag;
				p[0] /= total;
				p[1] /= total;
			}
		}
	}

	static void mult2D(float[] p, float vm)
	{
		p[0] *= vm;
		p[1] *= vm;
	}
	
	static float distance(float[] p1, float[] p2)
	{
		return (float) Math.sqrt(Math.pow(p2[0]-p1[0], 2) + Math.pow(p2[1]-p1[1], 2));
	}

}

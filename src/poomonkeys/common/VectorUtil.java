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
				total *= mag;
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

}

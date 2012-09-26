package poomonkeys.common;

public class Player
{
	
	public Tank tank;
	float angle, power;

	public Player()
	{
		tank = new Tank();
	}

	public void fireShot(Shot shot)
	{
		/*String msg = "{" +
					"x:"+shot.shotX+","+
					"y:"+shot.shotY+","+
					"vx:"+shot.shotVelocity.x+","+
					"vy:"+shot.shotVelocity.y+
				"}";
		try {
			SocketUtil.sendMessage(msg);
		} catch (IOException e) {}*/
	}
	
	public void setAngle(float angle)
	{
		this.angle = angle;
		tank.turret.setRotation(angle);
	}

	
}

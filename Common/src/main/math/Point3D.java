package main.math;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * 3D point.
 * @author cambolbro
 */
public class Point3D
{
	private double x;
	private double y;
	private double z;
	private double tolerance = 0.0001;
	
	//-------------------------------------------------------------------------
	
	public Point3D(final double x, final double y)
	{
		this.x = x;
		this.y = y;
		this.z = 0;
	}
	
	public Point3D(final double x, final double y, final double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(final Point3D other)
	{
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public Point3D(final Point2D other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = 0;
	}
	
	//-------------------------------------------------------------------------

	public double x()
	{
		return x;
	}
	
	public double y()
	{
		return y;
	}
	
	public double z()
	{
		return z;
	}
	
	//-------------------------------------------------------------------------

	public void set(final double xx, final double yy)
	{
		x = xx;
		y = yy;
	}

	public void set(final double xx, final double yy, final double zz)
	{
		x = xx;
		y = yy;
		z = zz;
	}

	public void set(final Point3D other)
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public void set(final Point2D other)
	{
		x = other.getX();
		y = other.getY();
		z = 0;
	}
	
	//-------------------------------------------------------------------------

	public double distance(final Point3D other)
	{
		final double dx = other.x - x;
		final double dy = other.y - y;
		final double dz = other.z - z;

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public double distance(final Point2D other)
	{
		final double dx = other.getX() - x;
		final double dy = other.getY() - y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	//-------------------------------------------------------------------------

	public void translate(final double dx, final double dy)
	{
		x += dx;
		y += dy;
	}
		
	public void translate(final double dx, final double dy, final double dz)
	{
		x += dx;
		y += dy;
		z += dz;
	}
		
	public void scale(final double s)
	{
		x *= s;
		y *= s;
		z *= s;
	}
	
	public void scale(final double sx, final double sy)
	{
		x *= sx;
		y *= sy;
	}
	
	public void scale(final double sx, final double sy, final double sz)
	{
		x *= sx;
		y *= sy;
		z *= sz;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point3D other = (Point3D) obj;
        return Double.compare(x, other.x) < tolerance &&
               Double.compare(y, other.y) < tolerance &&
               Double.compare(z, other.z) < tolerance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}

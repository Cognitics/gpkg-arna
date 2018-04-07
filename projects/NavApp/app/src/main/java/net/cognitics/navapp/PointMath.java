package net.cognitics.navapp;

import mil.nga.wkb.geom.Point;

public class PointMath {

    public static Point add(Point a, Point b)
    {
        Point ret = new Point();
        ret.setX(a.getX() + b.getX());
        ret.setY(a.getY() + b.getY());
        if(a.hasZ() && b.hasZ())
            ret.setZ(a.getZ() + b.getZ());
        return ret;
    }

    public static Point subtract(Point a, Point b)
    {
        Point ret = new Point();
        ret.setX(a.getX() - b.getX());
        ret.setY(a.getY() - b.getY());
        if(a.hasZ() && b.hasZ())
            ret.setZ(a.getZ() - b.getZ());
        return ret;
    }

    public static Point multiply(Point a, double factor)
    {
        Point ret = new Point();
        ret.setX(a.getX() * factor);
        ret.setY(a.getY() * factor);
        if(a.hasZ())
            ret.setZ(a.getZ() * factor);
        return ret;
    }

    public static double dot(Point a, Point b)
    {
        double sum = a.getX() * b.getX();
        sum += (a.getY() * b.getY());
        if(a.hasZ() && b.hasZ())
            sum += (a.getZ() * b.getZ());
        return sum;
    }

    public static double dot2D(Point a, Point b)
    {
        double sum = a.getX() * b.getX();
        sum += (a.getY() * b.getY());
        return sum;
    }

    /**
     * Returns the distance squared between a and be
     * @param a the first vector
     * @param b the second vector
     * @return The square of the cartesian distance between a and be
     */
    public static double distance2(Point a, Point b)
    {
        double d2;
        double delta_x = a.getX() - b.getX();
        double delta_y = a.getY() - b.getY();
        double delta_z = 0;
        if(a.hasZ() && b.hasZ())
            delta_z = a.getZ() - b.getZ();
        d2 = (delta_x*delta_x) +
                (delta_y*delta_y) +
                (delta_z*delta_z);
        return d2;
    }

    /**
     * Returns the distance between a and be
     * @param a the first vector
     * @param b the second vector
     * @return The cartesian distance between a and be
     */
    public static double distance(Point a, Point b)
    {
        return Math.sqrt(distance2(a,b));
    }

    /**
     * Returns the distance squared between a and b, ignoring the Z component (a 2d distance)
     * @param a the first vector
     * @param b the second vector
     * @return The square of the cartesian distance between a and b, ignoring the Z component (a 2d distance)
     */
    public static double distance2D2(Point a, Point b)
    {
        double d2;
        double delta_x = a.getX() - b.getX();
        double delta_y = a.getY() - b.getY();
        d2 = (delta_x*delta_x) +
                (delta_y*delta_y);
        return d2;
    }

    /**
     * Returns the distance between a and be
     * @param a the first vector
     * @param b the second vector
     * @return The cartesian distance between a and b, ignoring the Z component (a 2d distance)
     */
    public static double distance2D(Point a, Point b)
    {
        return Math.sqrt(distance2D(a,b));
    }


    /**
     * Calculates the square of the 3D length of the (actually the distance between the {0,0,0} and
     * the vector)
     * @param a The vector to calculate
     * @return the distance between the origin and the point
     */
    public static double length2(Point a)
    {
        if(a.hasZ()) {
            return (a.getX() * a.getX()) +
                    (a.getY() * a.getY()) +
                    (a.getZ() * a.getZ());
        }
        else
        {
            return length2D2(a);
        }
    }

    /**
     * Calculates the the 3D length of the (actually the distance between the {0,0,0} and
     * the vector)
     * @param a The vector to calculate
     * @return the distance between the origin and the point
     */
    public static double length(Point a)
    {
        if(a.hasZ()) {
            return Math.sqrt(length2(a));
        }
        else {
            return Math.sqrt(length2D(a));
        }
    }

    /**
     * Calculates the square of the 2D length of the (actually the distance between the {0,0} and
     * the vector)
     * @param a The vector to calculate
     * @return the distance between the origin and the point
     */
    public static double length2D2(Point a)
    {
        return (a.getX() * a.getX()) +
                (a.getY() * a.getY());
    }

    /**
     * Calculates the the 2D length of the (actually the distance between the {0,0} and
     * the vector)
     * @param a The vector to calculate
     * @return the distance between the origin and the point
     */
    public static double length2D(Point a)
    {
        return Math.sqrt(length2D2(a));
    }

    public static Point normalize(Point a)
    {
        Point ret = new Point(a);
        double len = length(a);
        if(len > 0)
        {
            ret = multiply(a,len);
        }
        return ret;
    }


}

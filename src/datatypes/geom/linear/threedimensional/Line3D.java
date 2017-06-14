package datatypes.geom.linear.threedimensional;

import datatypes.geom.Vector;
import datatypes.geom.linear.twodimensional.Line2D;

/**
 * Created by psweeney on 3/7/17.
 */
public class Line3D implements LinearObject3D{
    private float xOfY, xOfZ, yOfX, yOfZ, zOfX, zOfY;
    private float xInterceptY, xInterceptZ, yInterceptX, yInterceptZ, zInterceptX, zInterceptY;
    private boolean staticX = false, staticY = false, staticZ = false, isPoint = false;

    public Line3D(float x1, float y1, float z1, float x2, float y2, float z2){
        if(x1 == x2) staticX = true;
        if(y1 == y2) staticY = true;
        if(z1 == z2) staticZ = true;

        if(staticX && staticY && staticZ){
            isPoint = true;
            xOfY = 0;
            xOfZ = 0;
            yOfX = 0;
            yOfZ = 0;
            zOfX = 0;
            zOfY = 0;
            xInterceptY = x1;
            xInterceptZ = x1;
            yInterceptX = y1;
            yInterceptZ = y1;
            zInterceptX = z1;
            zInterceptY = z1;
            return;
        }

        if(staticX && staticY){
            xOfY = 0;
            xOfZ = 0;
            yOfX = 0;
            yOfZ = 0;
            zOfX = 0;
            zOfY = 0;
            xInterceptY = x1;
            xInterceptZ = x1;
            yInterceptX = y1;
            yInterceptZ = y1;
            zInterceptX = 0;
            zInterceptY = 0;
            return;
        }

        if(staticX && staticZ){
            xOfY = 0;
            xOfZ = 0;
            yOfX = 0;
            yOfZ = 0;
            zOfX = 0;
            zOfY = 0;
            xInterceptY = x1;
            xInterceptZ = x1;
            yInterceptX = 0;
            yInterceptZ = 0;
            zInterceptX = z1;
            zInterceptY = z1;
            return;
        }

        if(staticY && staticZ){
            xOfY = 0;
            xOfZ = 0;
            yOfX = 0;
            yOfZ = 0;
            zOfX = 0;
            zOfY = 0;
            xInterceptY = 0;
            xInterceptZ = 0;
            yInterceptX = y1;
            yInterceptZ = y1;
            zInterceptX = z1;
            zInterceptY = z1;
            return;
        }

        if(staticX){
            xOfY = 0;
            xOfZ = 0;
            yOfX = 0;
            yOfZ = (y2 - y1)/(z2 - z1);
            zOfX = 0;
            zOfY = 1/yOfZ;
            xInterceptY = x1;
            xInterceptZ = x1;
            yInterceptX = 0;
            yInterceptZ = y1 - z1 * yOfZ;
            zInterceptX = 0;
            zInterceptY = z1 - y1 * zOfY;
            return;
        }

        if(staticY){
            xOfY = 0;
            xOfZ = (x2 - x1)/(z2 - z1);
            yOfX = 0;
            yOfZ = 0;
            zOfX = 1/xOfZ;
            zOfY = 0;
            xInterceptY = 0;
            xInterceptZ = x1 - z1 * xOfZ;
            yInterceptX = y1;
            yInterceptZ = y1;
            zInterceptX = z1 - x1 * zOfX;
            zInterceptY = 0;
            return;
        }

        if(staticZ){
            xOfY = (x2 - x1)/(y2 - y1);
            xOfZ = 0;
            yOfX = 1/xOfY;
            yOfZ = 0;
            zOfX = 0;
            zOfY = 0;
            xInterceptY = x1 - y1 * xOfY;
            xInterceptZ = 0;
            yInterceptX = y1 - x1 * yOfX;
            yInterceptZ = 0;
            zInterceptX = z1;
            zInterceptY = z1;

            return;
        }

        xOfY = (x2 - x1)/(y2 - y1);
        xOfZ = (x2 - x1)/(z2 - z1);
        yOfX = 1/xOfY;
        yOfZ = (y2 - y1)/(z2 - z1);
        zOfX = 1/xOfZ;
        zOfY = 1/yOfZ;
        xInterceptY = x1 - y1 * xOfY;
        xInterceptZ = x1 - z1 * xOfZ;
        yInterceptX = y1 - x1 * yOfX;
        yInterceptZ = y1 - z1 * yOfZ;
        zInterceptX = z1 - x1 * zOfX;
        zInterceptY = z1 - y1 * zOfY;
    }

    @Override
    public Vector getPointForX(float x) {
        if(isPoint){
            if(x == xInterceptY){
                return new Vector(xInterceptY, yInterceptX, zInterceptX);
            }
            return null;
        } else if(staticX) return null;
        else if(staticY && staticZ){
            return new Vector(x, yInterceptX, zInterceptX);
        } else if(staticY){
            return new Vector(x, yInterceptX, zInterceptX + x * zOfX);
        } else if(staticZ){
            return new Vector(x, yInterceptX + x * yOfX, zInterceptX);
        }
        return new Vector(x, yInterceptX + x * yOfX, zInterceptX * x * zOfX);
    }

    @Override
    public Vector getPointForY(float y) {
        if(isPoint){
            if(y == yInterceptX){
                return new Vector(xInterceptY, yInterceptX, zInterceptX);
            }
            return null;
        } else if(staticY) return null;
        else if(staticX && staticZ){
            return new Vector(xInterceptY, y, zInterceptX);
        } else if(staticX){
            return new Vector(xInterceptY, y, zInterceptY + y * zOfY);
        } else if(staticZ){
            return new Vector(xInterceptY + y * xOfY, y, zInterceptY);
        }
        return new Vector(xInterceptY * y * xOfY, y, zInterceptY + y * zOfY);
    }

    @Override
    public Vector getPointForZ(float z) {
        if(isPoint){
            if(z == zInterceptX) return new Vector(xInterceptY, yInterceptX, zInterceptX);
            return null;
        } else if(staticZ) return null;
        else if(staticX && staticY){
            return new Vector(xInterceptZ, yInterceptZ, z);
        } else if(staticX){
            return new Vector(xInterceptZ, yInterceptZ + z * yOfZ, z);
        } else if(staticY){
            return new Vector(xInterceptZ + z * xOfZ, yInterceptZ, z);
        }
        return new Vector(xInterceptZ + z * xOfZ, yInterceptZ + z * yOfZ, z);
    }

    public boolean isOnLine(Vector v){
        Vector tv = null;
        if(isPoint) return xInterceptZ == v.getX() && yInterceptZ == v.getY() && zInterceptX == v.getZ();
        else if(!staticX){
            tv = getPointForX(v.getX());
        } else if(!staticY){
            tv = getPointForY(v.getY());
        } else if(!staticZ){
            tv = getPointForZ(v.getZ());
        }

        return tv != null && v.dist(tv) <= 0;
    }

    @Override
    public Vector getIntersection(LinearObject3D other) {
        /*
        if(!(other instanceof Line3D)) return other.getIntersection(this);
        Line3D line3D = (Line3D) other;

        if(isPoint){
            Vector tv = new Vector(xInterceptZ, yInterceptZ, zInterceptX);
            if(line3D.isPoint){
                if(xInterceptZ == line3D.xInterceptZ && yInterceptZ == line3D.yInterceptZ &&
                        zInterceptX == line3D.zInterceptX) return tv;
                return null;
            } else if(line3D.isOnLine(tv)) return tv;
            return null;
        } else if(!staticX){
            if(line3D.isPoint){
                Vector v = new Vector(line3D.xInterceptZ, line3D.yInterceptZ, line3D.zInterceptX);
                if(isOnLine(v)) return v;
                else return null;
            } else if(line3D.staticX){
                Vector v = getPointForX(line3D.xInterceptZ);
                if(line3D.isOnLine(v)) return v;
                return null;
            }
        }
        */
        return null;
    }
}

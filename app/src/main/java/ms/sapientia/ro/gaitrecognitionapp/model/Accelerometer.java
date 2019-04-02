package ms.sapientia.ro.gaitrecognitionapp.model;

public class Accelerometer {
    /*
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_X = "x";
    public static final String FIELD_Y = "y";
    public static final String FIELD_Z = "z";
    public static final String FIELD_STEP = "step";
    */
    private long timestamp;
    private double x;
    private double y;
    private double z;
    private int step;

    public Accelerometer(long tt, double xx, double yy, double zz, int step) {
        this.timestamp = tt;
        this.x = xx;
        this.y = yy;
        this.z = zz;
        this.step = step;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public int getStep() {
        return step;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return timestamp + "," + x + "," + y + "," + z + "," + step;
    }
}

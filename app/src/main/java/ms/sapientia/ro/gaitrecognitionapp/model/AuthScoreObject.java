package ms.sapientia.ro.gaitrecognitionapp.model;

public class AuthScoreObject {

    private int mId;
    private double mScore;

    public AuthScoreObject(int mId, double mScore) {
        this.mId = mId;
        this.mScore = mScore;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public double getScore() {
        return mScore;
    }

    public void setScore(float mScore) {
        this.mScore = mScore;
    }
}

package app;

public class SEINalUnit {
    public byte[] seiUnit;
    public int byteNumber;
    public long pts;

    public SEINalUnit(byte[] seiUnit, int byteNumber, long pts){
        this.byteNumber = byteNumber;
        this.seiUnit = seiUnit;
        this.pts = pts;
    }

    public byte[] getSeiUnit() {
        return seiUnit;
    }

    private void setSeiUnit(byte[] seiUnit) {
        this.seiUnit = seiUnit;
    }

    public int getByteNumber() {
        return byteNumber;
    }

    private void setByteNumber(int byteNumber) {
        this.byteNumber = byteNumber;
    }

    public long getPts() {
        return pts;
    }

    public void setPts(long pts) {
        this.pts = pts;
    }

}

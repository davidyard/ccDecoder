package app;

public class SEINalUnit {
    public byte[] seiUnit;
    public int byteNumber;

    public SEINalUnit(byte[] seiUnit, int byteNumber){
        this.byteNumber = byteNumber;
        this.seiUnit = seiUnit;
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

}

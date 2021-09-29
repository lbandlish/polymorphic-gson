public class TopLevel {

    private int field = 4;
    public int fieldPub = 123;

    public int getField() {
        return field;
    }

    public int getFieldPub() {
        return fieldPubNikal();
    }

    public int fieldPubNikal() {
        return fieldPub;
    }

    public void setField(int field) {
        this.field = field;
    }
}

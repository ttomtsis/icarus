package gr.aegean.icsd.icarus.util;

import gr.aegean.icsd.icarus.util.enums.PatchOperation;

public class PatchDocument {


    private PatchOperation op;

    private String path;

    private String value;



    public PatchOperation getOp() {
        return op;
    }

    public void setOp(PatchOperation op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

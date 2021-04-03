package hk.kllstudio.eta.apiget.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ETA {
    private String co;
    private String route;
    private String dir;
    @SerializedName("service_type")
    private String serviceType;
    private int seq;
    @SerializedName("dest_en")
    private String destEn;
    @SerializedName("dest_tc")
    private String destTc;
    @SerializedName("dest_sc")
    private String destSc;
    @SerializedName("eta_seq")
    private int etaSeq;
    private Date eta;
    @SerializedName("rmk_en")
    private String remarkEn;
    @SerializedName("rmk_tc")
    private String remarkTc;
    @SerializedName("rmk_sc")
    private String remarkSc;
    @SerializedName("data_timestamp")
    private Date dataTimestamp;

    public String getCo() {
        return co;
    }

    public String getRoute() {
        return route;
    }

    public String getDir() {
        return dir;
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getSeq() {
        return seq;
    }

    public String getDestEn() {
        return destEn;
    }

    public String getDestTc() {
        return destTc;
    }

    public String getDestSc() {
        return destSc;
    }

    public int getEtaSeq() {
        return etaSeq;
    }

    public Date getEta() {
        return eta;
    }

    public String getRemarkEn() {
        return remarkEn;
    }

    public String getRemarkTc() {
        return remarkTc;
    }

    public String getRemarkSc() {
        return remarkSc;
    }

    public Date getDataTimestamp() {
        return dataTimestamp;
    }
}

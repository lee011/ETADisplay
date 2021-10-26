package hk.kllstudio.eta.apiget.nwstbus.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Route implements hk.kllstudio.eta.apiget.IRoute {
    private String co;
    private String route;
    @SerializedName("orig_en")
    private String origEn;
    @SerializedName("orig_tc")
    private String origTc;
    @SerializedName("orig_sc")
    private String origSc;
    @SerializedName("dest_en")
    private String destEn;
    @SerializedName("dest_tc")
    private String destTc;
    @SerializedName("dest_sc")
    private String destSc;
    @SerializedName("data_timestamp")
    private Date dataTimestamp;

    @Override
    public String getCo() {
        return co;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String getOrigEn() {
        return origEn;
    }

    @Override
    public String getOrigTc() {
        return origTc;
    }

    @Override
    public String getOrigSc() {
        return origSc;
    }

    @Override
    public String getDestEn() {
        return destEn;
    }

    @Override
    public String getDestTc() {
        return destTc;
    }

    @Override
    public String getDestSc() {
        return destSc;
    }

    public Date getDataTimestamp() {
        return dataTimestamp;
    }
}

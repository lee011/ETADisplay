package hk.kllstudio.eta.apiget.kmb.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import hk.kllstudio.eta.apiget.IRoute;

public class Route implements IRoute {
    private String route;
    private String bound;
    @SerializedName("service_type")
    private String serviceType;
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

    public String getCo() {
        return "KMB";
    }

    public String getRoute() {
        return route;
    }

    public String getBound() {
        return bound;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getOrigEn() {
        return origEn;
    }

    public String getOrigTc() {
        return origTc;
    }

    public String getOrigSc() {
        return origSc;
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

    public Date getDataTimestamp() {
        return dataTimestamp;
    }
}

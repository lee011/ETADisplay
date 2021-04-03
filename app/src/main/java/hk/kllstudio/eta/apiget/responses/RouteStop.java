package hk.kllstudio.eta.apiget.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class RouteStop {
    private String co;
    private String route;
    private String bound;
    @SerializedName("service_type")
    private String serviceType;
    private int seq;
    private String stop;
    @SerializedName("data_timestamp")
    private Date dataTimestamp;

    public String getCo() {
        return co;
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

    public int getSeq() {
        return seq;
    }

    public String getStop() {
        return stop;
    }

    public Date getDataTimestamp() {
        return dataTimestamp;
    }
}

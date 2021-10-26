package hk.kllstudio.eta.apiget.nwstbus.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class RouteStop {
    private String co;
    private String route;
    private String dir;
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

    public String getDir() {
        return dir;
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

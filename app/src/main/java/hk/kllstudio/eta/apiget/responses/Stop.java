package hk.kllstudio.eta.apiget.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Stop {
    private String stop;
    @SerializedName("name_tc")
    private String NameTc;
    @SerializedName("name_en")
    private String NameEn;
    @SerializedName("name_sc")
    private String NameSc;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("long")
    private double longitude;
    @SerializedName("data_timestamp")
    private Date dataTimestamp;

    public String getNameTc() {
        return NameTc;
    }

    public String getNameEn() {
        return NameEn;
    }

    public String getNameSc() {
        return NameSc;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getDataTimestamp() {
        return dataTimestamp;
    }

    public String getStop() {
        return stop;
    }
}

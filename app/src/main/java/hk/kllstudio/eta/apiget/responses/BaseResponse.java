package hk.kllstudio.eta.apiget.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BaseResponse<T> {
    private String type;
    private String version;
    @SerializedName("generated_timestamp")
    private Date generatedTimestamp;
    private T data;

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public Date getGeneratedTimestamp() {
        return generatedTimestamp;
    }

    public T getData() {
        return data;
    }
}

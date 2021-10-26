package hk.kllstudio.eta.apiget.nwstbus.responses;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ETA implements hk.kllstudio.eta.apiget.IETA {
    private String co;
    private String route;
    private String dir;
    private int seq;
    @SerializedName("dest_en")
    private String destEn;
    @SerializedName("dest_tc")
    private String destTc;
    @SerializedName("dest_sc")
    private String destSc;
    @SerializedName("eta_seq")
    private int etaSeq;
    private String eta;
    @SerializedName("rmk_en")
    private String remarkEn;
    @SerializedName("rmk_tc")
    private String remarkTc;
    @SerializedName("rmk_sc")
    private String remarkSc;

    public String getCo() {
        return co;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String getDir() {
        return dir;
    }

    @Override
    public int getSeq() {
        return seq;
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

    @Override
    public int getEtaSeq() {
        return etaSeq;
    }

    @Override
    public Date getEta() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.TRADITIONAL_CHINESE);
        if (eta.isEmpty())
            return null;
        else {
            try {
                return dateFormat.parse(eta);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    @Override
    public String getRemarkEn() {
        return remarkEn;
    }

    @Override
    public String getRemarkTc() {
        return remarkTc;
    }

    @Override
    public String getRemarkSc() {
        return remarkSc;
    }

}

package hk.kllstudio.eta.apiget;

import java.util.Date;

public interface IETA {
    String getRoute();

    String getDir();

    int getSeq();

    String getDestEn();

    String getDestTc();

    String getDestSc();

    int getEtaSeq();

    Date getEta();

    String getRemarkEn();

    String getRemarkTc();

    String getRemarkSc();
}

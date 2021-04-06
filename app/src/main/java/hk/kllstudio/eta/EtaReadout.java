package hk.kllstudio.eta;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import hk.kllstudio.eta.apiget.responses.ETA;

public class EtaReadout {
    static TextToSpeech tts;

    public static void read(Context context, final String stopName, final List<ETA> etas) {
        if (etas == null || etas.size() == 0) return;
        final String routeNumber = etas.get(0).getRoute();
        final String dest = etas.get(0).getDestTc();
        final int seq = etas.get(0).getSeq();
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("zh", "HK"));
                    StringBuilder sb = new StringBuilder();
                    if (etas.get(0).getEta() != null) {
                        sb.append("下一班路線，");
                        if (routeNumber.length() >= 3)
                            for (char c : routeNumber.toCharArray())
                                sb.append(c).append(" ");
                        else
                            sb.append(routeNumber);
                        sb.append("，往，").append(dest).append("，巴士將於，").append(getMinutes(etas.get(0).getEta())).append("分鐘後");
                        if (seq == 1) {
                            sb.append("從，").append(stopName).append("，開出。");
                        } else {
                            sb.append("抵達，").append(stopName).append("。");
                        }
                        if (etas.size() > 1) {
                            sb.append("其後班次：");
                            for (int i = 1; i < etas.size(); i++) {
                                sb.append(getMinutes(etas.get(i).getEta())).append("分鐘");
                                if (i != etas.size() - 1)
                                    sb.append("、");
                            }
                            sb.append("。");
                        }
                    } else {
                        sb.append("路線，");
                        if (routeNumber.length() >= 3)
                            for (char c : routeNumber.toCharArray())
                                sb.append(c).append(" ");
                        else
                            sb.append(routeNumber);
                        sb.append("，往，").append(dest).append("，").append(etas.get(0).getRemarkTc());
                    }
                    tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, "re");
                }
            }
        });
    }

    private static int getMinutes(Date date) {
        long startMs = new Date().getTime();
        long endMs = date.getTime();
        long totalMillis = Math.abs(endMs - startMs);
        return ((int) (totalMillis / 1000) / 60) % 60;
    }
}

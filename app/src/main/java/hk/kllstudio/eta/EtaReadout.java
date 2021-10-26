package hk.kllstudio.eta;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import hk.kllstudio.eta.apiget.IETA;

public class EtaReadout {
    static TextToSpeech tts;

    public static void read(final Context context, final String stopName, final List<? extends IETA> etas) {
        if (etas == null || etas.size() == 0) return;
        final String routeNumber = etas.get(0).getRoute();
        final String dest = etas.get(0).getDestTc();
        final int seq = etas.get(0).getSeq();
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                final AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(audioAttributes)
                        .build();
                final Object mFocusLock = new Object();
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        audioManager.abandonAudioFocusRequest(focusRequest);
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
                tts.setLanguage(new Locale("zh", "HK"));
                StringBuilder sb = new StringBuilder();
                if (etas.get(0).getEta() != null) {
                    int minutes = getMinutes(etas.get(0).getEta());
                    if (minutes >= 1) {
                        sb.append("下一班路線，");
                        if (routeNumber.length() >= 3)
                            for (char c : routeNumber.toCharArray())
                                sb.append(c).append(" ");
                        else
                            sb.append(routeNumber);
                        sb.append("，往，").append(dest).append("，巴士將於，").append(getMinuteRead(minutes)).append("分鐘後").append(etas.get(0).getRemarkTc());
                        if (seq == 1) {
                            sb.append("從，").append(stopName).append("，開出。");
                        } else {
                            sb.append("抵達，").append(stopName).append("。");
                        }
                        if (etas.size() > 1) {
                            sb.append("其後班次：");
                            for (int i = 1; i < etas.size(); i++) {
                                if (etas.get(i).getEta() != null)
                                    sb.append(getMinuteRead(getMinutes(etas.get(i).getEta()))).append("分鐘").append(etas.get(i).getRemarkTc());
                                else
                                    sb.append(etas.get(i).getRemarkTc());
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
                        sb.append("，往，").append(dest).append("，巴士即將抵達，").append(stopName).append("。");
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
                int res = audioManager.requestAudioFocus(focusRequest);
                synchronized (mFocusLock) {
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, "re");
                    }
                }
            }
        });
    }

    private static String getMinuteRead(int m) {
        if (m == 2) {
            return "兩";
        } else {
            return String.valueOf(m);
        }
    }

    private static int getMinutes(Date date) {
        long startMs = new Date().getTime();
        long endMs = date.getTime();
        long totalMillis = endMs - startMs;
        return ((int) Math.ceil((totalMillis / 1000.0) / 60)) % 60;
    }
}

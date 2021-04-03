package hk.kllstudio.eta.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import hk.kllstudio.eta.R;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.textView2);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日, EEEE HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(format.format(new Date()));
                        }
                    });
                }
            }
        }, 0, 500);
        return root;
    }
}
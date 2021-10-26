package hk.kllstudio.eta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import hk.kllstudio.eta.apiget.kmb.responses.Stop;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Observer<List<Stop>> {
        private ActivityResultLauncher<String> launcher;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        SwitchPreferenceCompat use_location = findPreference("use_location");
                        if (use_location != null) {
                            use_location.setChecked(true);
                        }
                    } else {
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle("未能啟用定位服務")
                                .setMessage("您已拒絕應用程式的位置存取權限。您必須前往系統設定重新啟用本應用程式的位置存取權限。")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                }
            });
            MyApplication application = (MyApplication) getActivity().getApplication();
            application.observeStops(this, this);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SwitchPreferenceCompat use_location = findPreference("use_location");
            Preference update_stops_data = findPreference("update_stops_data");
            Preference version = findPreference("version");
            int i = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (use_location != null) {
                use_location.setOnPreferenceChangeListener(this);
                if (i == PackageManager.PERMISSION_DENIED && use_location.isChecked()) {
                    use_location.setChecked(false);
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("定位服務已停用")
                            .setMessage("您尚未授予／已拒絕應用程式的位置存取權限。請嘗試重新啟用定位服務以再次授予應用程式的位置存取權限。")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
            if (update_stops_data != null) {
                update_stops_data.setSummaryProvider(new Preference.SummaryProvider<Preference>() {
                    @Override
                    public CharSequence provideSummary(Preference preference) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日, EEEE HH:mm:ss", Locale.TRADITIONAL_CHINESE);
                        File file = getActivity().getFileStreamPath("stops.json");
                        if (file.exists())
                            return "資料最後更新日期：" + dateFormat.format(new Date(file.lastModified()));
                        else
                            return null;
                    }
                });
                update_stops_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((MyApplication) getActivity().getApplication()).downloadStops();
                        return true;
                    }
                });
            }
            if (version != null) {
                version.setSummaryProvider(new Preference.SummaryProvider<Preference>() {
                    @Override
                    public CharSequence provideSummary(Preference preference) {
                        return String.format("版本 %s © 2021 KLLStudio。著作權所有，並保留一切權利。", BuildConfig.VERSION_NAME);
                    }
                });
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("tts_settings")) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            }
            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ((boolean) newValue) {
                int i = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (i == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void onChanged(List<Stop> stops) {
            getListView().getAdapter().notifyDataSetChanged();
        }
    }
}
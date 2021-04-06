package hk.kllstudio.eta;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import hk.kllstudio.eta.apiget.Request;
import hk.kllstudio.eta.apiget.responses.Stop;

public class MyApplication extends Application {
    private MutableLiveData<List<Stop>> stops;

    public void observeStops(LifecycleOwner owner, Observer<List<Stop>> observer) {
        if (stops == null){
            stops = new MutableLiveData<>();
        }
        fetchStopInfo();
        stops.observe(owner, observer);
    }

    public List<Stop> getStops() {
        return stops.getValue();
    }

    public void fetchStopInfo() {
        if (stops == null) {
            stops = new MutableLiveData<>();
        }
        if (stops.getValue() == null) {
            // Application does not have a local copy of stops array:
            // 1. Fetch stops from file storage first
            try (FileInputStream stream = openFileInput("stops.json")) {
                InputStreamReader r = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(r);
                StringBuilder sb = new StringBuilder();
                String l;
                while ((l = reader.readLine()) != null) {
                    sb.append(l).append('\n');
                }
                reader.close();
                Type returnType = new TypeToken<List<Stop>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
                stops.setValue(gson.<List<Stop>>fromJson(sb.toString(), returnType));
            } catch (FileNotFoundException ex) {
                // 2. Fetch stops online as stops file does not exist
                downloadStops();
            } catch (Exception ex) {
                // Other error, print stack trace
                ex.printStackTrace();
            }
        }
    }

    public void downloadStops() {
        final Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Stop> list = Request.getStop().getData();
                    stops.postValue(list);
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
                    String json = gson.toJson(list);
                    FileOutputStream stream = openFileOutput("stops.json", Context.MODE_PRIVATE);
                    stream.write(json.getBytes());
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

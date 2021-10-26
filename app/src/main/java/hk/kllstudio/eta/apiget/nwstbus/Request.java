package hk.kllstudio.eta.apiget.nwstbus;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import hk.kllstudio.eta.apiget.BaseResponse;
import hk.kllstudio.eta.apiget.nwstbus.responses.ETA;
import hk.kllstudio.eta.apiget.nwstbus.responses.Route;
import hk.kllstudio.eta.apiget.nwstbus.responses.RouteStop;
import hk.kllstudio.eta.apiget.nwstbus.responses.Stop;

public class Request {
    public static final String BASE_URI = "https://rt.data.gov.hk/";

    public static final String COMPANY_CTB = "ctb";
    public static final String COMPANY_NWFB = "nwfb";

    public static <T> BaseResponse<T> request(Type returnType, String ...path) throws IOException, JsonSyntaxException {
        List<String> p = new ArrayList<>(Arrays.asList("v1", "transport", "citybus-nwfb"));
        p.addAll(Arrays.asList(path));
        URL url = new URL(BASE_URI + TextUtils.join("/", p));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        InputStream r = connection.getInputStream();
        InputStreamReader rw = new InputStreamReader(r);
        BufferedReader br = new BufferedReader(rw);
        StringBuilder sb = new StringBuilder();
        String l;
        while ((l = br.readLine()) != null) {
            sb.append(l).append('\n');
        }
        br.close();
        connection.disconnect();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        return gson.fromJson(sb.toString(), returnType);
    }

    public static BaseResponse<Route> getRoute(String co, String route) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<Route>>(){}.getType();
        return request(t, "route", co, route);
    }

    public static BaseResponse<List<Route>> getRoute(String co) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<Route>>>(){}.getType();
        return request(t, "route", co);
    }

    public static BaseResponse<Stop> getStop(String stopID) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<Stop>>(){}.getType();
        return request(t, "stop", stopID);
    }

    public static BaseResponse<List<RouteStop>> getRouteStops(String co, String route, String dir) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<RouteStop>>>(){}.getType();
        return request(t, "route-stop", co, route, dir);
    }

    public static BaseResponse<List<ETA>> getETA(String co, String stopID, String route) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<ETA>>>(){}.getType();
        return request(t, "eta", co, stopID, route);
    }
}

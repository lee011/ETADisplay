package hk.kllstudio.eta.apiget.kmb;
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

import androidx.annotation.NonNull;
import hk.kllstudio.eta.apiget.BaseResponse;
import hk.kllstudio.eta.apiget.kmb.responses.ETA;
import hk.kllstudio.eta.apiget.kmb.responses.Route;
import hk.kllstudio.eta.apiget.kmb.responses.RouteStop;
import hk.kllstudio.eta.apiget.kmb.responses.Stop;

public class Request {
    public final static String BASE_URI = "https://data.etabus.gov.hk/";

    /**
     * 傳送指定的九巴到站時間 API 請求。
     *
     * @param path   字串陣列，表示請求的頁面部分。
     * @return {@link BaseResponse<T>}，表示回應主體。不為 {@code null}。
     * @throws IOException                    寫入請求或讀取回應資料流時發生錯誤。
     * @throws java.net.MalformedURLException 請求 URI 不正確。
     * @throws JsonSyntaxException            嘗試剖析或讀取 JSON 時發生錯誤。
     */
    @NonNull
    public static <T> BaseResponse<T> request(Type returnType, String ...path) throws IOException, JsonSyntaxException {
        List<String> p = new ArrayList<>(Arrays.asList("v1", "transport", "kmb"));
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

    public static BaseResponse<Route> getRoute(String routeNumber, String bound, String serviceType) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<Route>>(){}.getType();
        return request(t, "route", routeNumber, bound, serviceType);
    }

    public static BaseResponse<List<Route>> getRoute() throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<Route>>>(){}.getType();
        return request(t, "route");
    }

    public static BaseResponse<Stop> getStop(String stopID) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<Stop>>(){}.getType();
        return request(t, "stop", stopID);
    }

    public static BaseResponse<List<Stop>> getStop() throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<Stop>>>(){}.getType();
        return request(t, "stop");
    }

    public static BaseResponse<List<RouteStop>> getRouteStops(String routeNumber, String bound, String serviceType) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<RouteStop>>>(){}.getType();
        return request(t, "route-stop", routeNumber, bound, serviceType);
    }

    public static BaseResponse<List<RouteStop>> getRouteStops() throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<RouteStop>>>(){}.getType();
        return request(t, "route-stop");
    }

    public static BaseResponse<List<ETA>> getETA(String stopID, String routeNumber, String serviceType) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<ETA>>>(){}.getType();
        return request(t, "eta", stopID,routeNumber,serviceType);
    }

    public static BaseResponse<List<ETA>> getStopETA(String stopID) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<ETA>>>(){}.getType();
        return request(t, "stop-eta", stopID);
    }

    public static BaseResponse<List<ETA>> getRouteETA(String routeNumber, String serviceType) throws IOException, JsonSyntaxException {
        Type t = new TypeToken<BaseResponse<List<ETA>>>(){}.getType();
        return request(t, "route-eta", routeNumber, serviceType);
    }

}

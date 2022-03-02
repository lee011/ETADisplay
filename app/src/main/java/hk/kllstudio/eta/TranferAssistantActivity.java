package hk.kllstudio.eta;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import hk.kllstudio.eta.adapters.RouteAdapter;
import hk.kllstudio.eta.apiget.IRoute;
import hk.kllstudio.eta.apiget.kmb.Request;
import hk.kllstudio.eta.apiget.kmb.responses.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TranferAssistantActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tranfer_assistant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        button1.setOnClickListener(v -> showRouteSelectionDialog(routeNumber -> Log.i("ETADisplay", "chosen route: " + routeNumber)));
        button2.setOnClickListener(v -> showRouteSelectionDialog(routeNumber -> Log.i("ETADisplay", "chosen route: " + routeNumber)));
    }
    
    private void showRouteSelectionDialog(IRouteSelectionCallback callback) {
        AlertDialog root = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.dialog_route_selector)
                .setTitle("選擇路線")
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {})
                .show();
        final TextInputLayout textInputLayout = root.findViewById(R.id.searchRouteField);
        final TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
        final RecyclerView recyclerView = root.findViewById(R.id.filteredRouteList);
        final Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        textInputLayout.setHelperText("正在載入路線...");
        textInputLayout.setEnabled(false);
        executor.execute(() -> {
            try {
                final List<Route> kmbRoutes = Request.getRoute().getData();
                final List<IRoute> iRoutes = new ArrayList<>(kmbRoutes);
                handler.post(() -> {
                    final RouteAdapter adapter = new RouteAdapter(iRoutes);
                    textInputLayout.setHelperText(null);
                    textInputLayout.setEnabled(true);
                    adapter.setOnItemClickListener((view, position) -> {
                        IRoute route = adapter.get(position);
                        callback.routeSelected(route.getRoute());
                        root.dismiss();
                    });
                    recyclerView.setAdapter(adapter);
                });
            } catch (final Exception ex) {
                ex.printStackTrace();
                handler.post(() -> new MaterialAlertDialogBuilder(this)
                        .setTitle("未能讀取資料")
                        .setMessage(String.format("讀取路線資料時發生錯誤。\n\n錯誤類型：%s\n\n錯誤訊息：%s", ex.getClass().getName(), ex.getMessage()))
                        .setPositiveButton("確定", (dialog, which) -> {
                            root.dismiss();
                        })
                        .show());
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (recyclerView.getAdapter() != null) {
                    ((RouteAdapter) recyclerView.getAdapter()).filter(item -> {
                        if (editText.getText() != null && !s.toString().isEmpty()) {
                            return item.getRoute().startsWith(s.toString());
                        } else return false;
                    });
                }
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH && recyclerView.getAdapter() != null) {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
            return false;
        });
    }

    private interface IRouteSelectionCallback {
        void routeSelected(String routeNumber);
    }
}
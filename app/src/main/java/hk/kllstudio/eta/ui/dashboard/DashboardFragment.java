package hk.kllstudio.eta.ui.dashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hk.kllstudio.eta.EtaActivity;
import hk.kllstudio.eta.R;
import hk.kllstudio.eta.adapters.RouteAdapter;
import hk.kllstudio.eta.apiget.Request;
import hk.kllstudio.eta.apiget.responses.Route;

public class DashboardFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextInputLayout textInputLayout = root.findViewById(R.id.searchRouteField);
        final TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
        final RecyclerView recyclerView = root.findViewById(R.id.filteredRouteList);
        final Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Route> routes = Request.getRoute().getData();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            final RouteAdapter adapter = new RouteAdapter(routes);
                            adapter.setOnItemClickListener(new RouteAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    Intent intent = new Intent(getContext(), EtaActivity.class);
                                    Route route = adapter.get(position);
                                    intent.putExtra("routeNumber", route.getRoute())
                                            .putExtra("bound", route.getBound())
                                            .putExtra("serviceType", route.getServiceType());
                                    startActivity(intent);
                                }
                            });
                            recyclerView.setAdapter(adapter);
                        }
                    });
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle("未能讀取資料")
                                    .setMessage(String.format("讀取路線資料時發生錯誤。\n\n錯誤類型：%s\n\n錯誤訊息：%s", ex.getClass().getName(), ex.getMessage()))
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });
                }
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
                    ((RouteAdapter) recyclerView.getAdapter()).filter(new RouteAdapter.Predicate() {
                        @Override
                        public boolean apply(Route item) {
                            if (editText.getText() != null && !s.toString().isEmpty())
                                return item.getRoute().startsWith(s.toString());
                            else
                                return false;
                        }
                    });
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && isAdded() && recyclerView.getAdapter() != null) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    /*((RouteAdapter) recyclerView.getAdapter()).filter(new RouteAdapter.Predicate() {
                        @Override
                        public boolean apply(Route item) {
                            if (editText.getText() != null && !editText.getText().toString().isEmpty())
                                return item.getRoute().startsWith(editText.getText().toString());
                            else
                                return false;
                        }
                    });*/
                }
                return false;
            }
        });
        return root;
    }
}
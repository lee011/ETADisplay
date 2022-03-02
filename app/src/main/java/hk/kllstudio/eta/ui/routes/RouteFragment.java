package hk.kllstudio.eta.ui.routes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
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
import hk.kllstudio.eta.TranferAssistantActivity;
import hk.kllstudio.eta.adapters.RouteAdapter;
import hk.kllstudio.eta.apiget.IRoute;
import hk.kllstudio.eta.apiget.kmb.Request;
import hk.kllstudio.eta.apiget.kmb.responses.Route;

public class RouteFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_route, container, false);
        final TextInputLayout textInputLayout = root.findViewById(R.id.searchRouteField);
        final TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
        final RecyclerView recyclerView = root.findViewById(R.id.filteredRouteList);
        final Executor executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        final ChipGroup chipGroup = root.findViewById(R.id.coFilterChip);
        final Chip[] chips = new Chip[]{root.findViewById(R.id.filter_co_kmb),
                root.findViewById(R.id.filter_co_ctb), root.findViewById(R.id.filter_co_nwfb)};
        ExtendedFloatingActionButton efab = root.findViewById(R.id.button_transfer_assist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        textInputLayout.setHelperText("正在載入路線...");
        textInputLayout.setEnabled(false);
        executor.execute(() -> {
            try {
                final List<IRoute> iRoutes = new ArrayList<>();
                final List<Route> kmbRoutes = Request.getRoute().getData();
                final List<hk.kllstudio.eta.apiget.nwstbus.responses.Route> nwfbRoutes = hk.kllstudio.eta.apiget.nwstbus.Request.getRoute(hk.kllstudio.eta.apiget.nwstbus.Request.COMPANY_NWFB).getData();
                final List<hk.kllstudio.eta.apiget.nwstbus.responses.Route> ctbRoutes = hk.kllstudio.eta.apiget.nwstbus.Request.getRoute(hk.kllstudio.eta.apiget.nwstbus.Request.COMPANY_CTB).getData();
                iRoutes.addAll(kmbRoutes);
                iRoutes.addAll(nwfbRoutes);
                iRoutes.addAll(ctbRoutes);
                handler.post(() -> {
                    final RouteAdapter adapter = new RouteAdapter(iRoutes);
                    textInputLayout.setHelperText(null);
                    textInputLayout.setEnabled(true);
                    adapter.setOnItemClickListener((view, position) -> {
                        Intent intent = new Intent(getContext(), EtaActivity.class);
                        IRoute route = adapter.get(position);
                        if (route instanceof hk.kllstudio.eta.apiget.nwstbus.responses.Route) {
                            hk.kllstudio.eta.apiget.nwstbus.responses.Route nwstRoute = (hk.kllstudio.eta.apiget.nwstbus.responses.Route) route;
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle("選擇方向")
                                    .setItems(new String[]{nwstRoute.getDestTc(), nwstRoute.getOrigTc()}, ((dialog, which) -> {
                                        intent.putExtra("co", route.getCo())
                                                .putExtra("routeNumber", route.getRoute())
                                                .putExtra("bound", which == 1 ? "I" : "O");
                                        startActivity(intent);
                                    }))
                                    .show();
                        } else if (route instanceof Route) {
                            intent.putExtra("co", route.getCo())
                                    .putExtra("routeNumber", route.getRoute())
                                    .putExtra("bound", ((Route) route).getBound())
                                    .putExtra("serviceType", ((Route) route).getServiceType());
                            startActivity(intent);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                });
            } catch (final Exception ex) {
                ex.printStackTrace();
                handler.post(() -> new MaterialAlertDialogBuilder(getContext())
                        .setTitle("未能讀取資料")
                        .setMessage(String.format("讀取路線資料時發生錯誤。\n\n錯誤類型：%s\n\n錯誤訊息：%s", ex.getClass().getName(), ex.getMessage()))
                        .setPositiveButton("確定", (dialog, which) -> {
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
                            List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
                            String co = item.getCo();
                            if ((checkedChipIds.contains(R.id.filter_co_kmb) && co.equals("KMB"))
                                    || (checkedChipIds.contains(R.id.filter_co_ctb) && co.equals("CTB"))
                                    || (checkedChipIds.contains(R.id.filter_co_nwfb) && co.equals("NWFB")))
                                return item.getRoute().startsWith(s.toString());
                            else return false;
                        } else return false;
                    });
                }
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
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
        });
        for (Chip chip : chips)
            chip.setOnCheckedChangeListener((chip1, isChecked) -> {
                RouteAdapter adapter = (RouteAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.filter(item -> {
                        if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
                            List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
                            String co = item.getCo();
                            if ((checkedChipIds.contains(R.id.filter_co_kmb) && co.equals("KMB"))
                                    || (checkedChipIds.contains(R.id.filter_co_ctb) && co.equals("CTB"))
                                    || (checkedChipIds.contains(R.id.filter_co_nwfb) && co.equals("NWFB")))
                                return item.getRoute().startsWith(editText.getText().toString());
                            else return false;
                        } else return false;
                    });
                }
            });
        efab.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), TranferAssistantActivity.class));
        });
        return root;
    }
}
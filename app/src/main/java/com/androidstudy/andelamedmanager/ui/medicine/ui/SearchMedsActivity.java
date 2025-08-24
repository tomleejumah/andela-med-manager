package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.Medicine;
import com.androidstudy.andelamedmanager.databinding.ActivitySearchMedsBinding;
import com.androidstudy.andelamedmanager.ui.medicine.adapter.MonthlyIntakeAdapter;
import com.androidstudy.andelamedmanager.ui.medicine.viewmodel.MedicineViewModel;

import java.util.List;

public class SearchMedsActivity extends AppCompatActivity {

    private ActivitySearchMedsBinding binding;

    Toolbar toolbar;
    FrameLayout layout_empty;
    SearchView searchViewMedicine;
    RecyclerView recyclerView;
    MonthlyIntakeAdapter monthlyIntakeAdapter;
    private List<Medicine> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchMedsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        Drawable upArrow = getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationIcon(upArrow);
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.activity_search_meds));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MedicineViewModel medicineViewModel = ViewModelProviders.of(this).get(MedicineViewModel.class);
        medicineViewModel.getMedicineList().observe(this, medicines -> {
            if (SearchMedsActivity.this.medicineList == null) {
                setListData(medicines);
            }
        });

        loadSearch();
    }

    private void initViews() {
        toolbar = binding.toolbar;
        layout_empty = binding.layoutEmpty;
        searchViewMedicine = binding.searchViewMedicine;
        recyclerView = binding.recyclerView;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        onBackPressed();
        return true;
    }

    public void setListData(final List<Medicine> medicineList) {
        this.medicineList = medicineList;

        if (medicineList.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE);
        }

        monthlyIntakeAdapter = new MonthlyIntakeAdapter(this, medicineList, (v, position) -> {
            Medicine medicine = medicineList.get(position);
            Intent intent = new Intent(getApplicationContext(), MedicineActivity.class);

            Bundle b = new Bundle();
            b.putParcelable("MEDICINE", medicine);

            intent.putExtras(b);
            startActivity(intent);
        });
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(monthlyIntakeAdapter);
    }

    /**
     * Setup search view.
     */
    private void loadSearch() {
        searchViewMedicine.setFocusable(false);
        //adding search listener
        searchViewMedicine.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                monthlyIntakeAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
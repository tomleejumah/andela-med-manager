package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.Medicine;
import com.androidstudy.andelamedmanager.ui.medicine.adapter.MonthlyMedicineAdapter;
import com.androidstudy.andelamedmanager.ui.medicine.viewmodel.MedicineViewModel;

import java.util.List;


public class MonthlyIntakeActivity extends AppCompatActivity {

    Toolbar toolbar;
    FrameLayout emptyFrame;
    RecyclerView recyclerView;

    private List<Medicine> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_intake);

        recyclerView = findViewById(R.id.recyclerView);
        emptyFrame = findViewById(R.id.layout_empty);
        toolbar = findViewById(R.id.toolbar);

        Drawable upArrow = getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationIcon(upArrow);
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.activity_monthly_intake));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MedicineViewModel medicineViewModel = new ViewModelProvider(MonthlyIntakeActivity.this).get(MedicineViewModel.class);
        medicineViewModel.getMedicineList().observe(this, medicines -> {
            if (MonthlyIntakeActivity.this.medicineList == null) {
                setListData(medicines);
            }
        });
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
            emptyFrame.setVisibility(View.VISIBLE);
        }

        MonthlyMedicineAdapter monthlyMedicineAdapter = new MonthlyMedicineAdapter(this, medicineList, (v, position) -> {
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
        recyclerView.setAdapter(monthlyMedicineAdapter);
    }
}

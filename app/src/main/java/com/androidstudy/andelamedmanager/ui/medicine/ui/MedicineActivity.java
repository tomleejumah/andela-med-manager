package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.MedData;
import com.androidstudy.andelamedmanager.data.model.Medicine;
import com.androidstudy.andelamedmanager.databinding.ActivityMedicineBinding;
import com.androidstudy.andelamedmanager.ui.medicine.adapter.MedicineSparkAdapter;
import com.robinhood.spark.SparkView;

import java.util.ArrayList;
import java.util.List;


public class MedicineActivity extends AppCompatActivity {
    private ActivityMedicineBinding binding;

    Toolbar toolbar;
    TextView textViewMedicineName;
    SparkView sparkView;
    TextView textViewOne;
    TextView textViewTwo;
    TextView textViewThree;
    TextView textViewFour;
    TextView textViewInterval;
    TextView textViewMedPills;
    TextView textViewMedicinePercentage;
    TextView textViewMedDescription;

    private List<MedData> medDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        Drawable upArrow = getResources().getDrawable(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationIcon(upArrow);
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.activity_add_medicine));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        medDataList = getDummyMedData();
        setupSparkView();

        Intent in = getIntent();
        Bundle b = in.getExtras();

        assert b != null;
        Medicine medicine = b.getParcelable("MEDICINE");
        assert medicine != null;
        textViewMedicineName.setText(medicine.getName());
        //Calculate Percentage
        int takePercentage = (Integer.parseInt(medicine.getPillsTaken()) * 100 / Integer.parseInt(medicine.getPills()));
        textViewMedicinePercentage.setText(String.format("%d%s", 100 - takePercentage, getString(R.string.percentage)));

        textViewMedDescription.setText(medicine.getDescription());
        textViewInterval.setText(medicine.getInterval() + " Times");
        textViewMedPills.setText(medicine.getPills());

        switch (medicine.getInterval()) {
            case "1":
                textViewOne.setText("8:00 AM");
                break;
            case "2":
                textViewOne.setText("8:00 AM");
                textViewThree.setText("5:00 PM");
                break;
            case "3":
                textViewOne.setText("8:00 AM");
                textViewTwo.setText("12:00 Noon");
                textViewThree.setText("5:00 PM");
                break;
            case "4":
                textViewOne.setText("8:00 AM");
                textViewTwo.setText("12:00 Noon");
                textViewThree.setText("5:00 PM");
                textViewFour.setText("10:00 PM");
                break;
        }
    }

    private void initViews() {
        toolbar = binding.toolbar;
        textViewMedicineName = binding.textViewMedicineName;
        sparkView = binding.sparkView;
        textViewOne = binding.textViewOne;
        textViewTwo = binding.textViewTwo;
        textViewThree = binding.textViewThree;
        textViewFour = binding.textViewFour;
        textViewInterval = binding.textViewInterval;
        textViewMedPills = binding.textViewMedPills;
        textViewMedicinePercentage = binding.textViewMedicinePercentage;
        textViewMedDescription = binding.textViewMedDescription;
    }
    private List<MedData> getDummyMedData() {
        List<MedData> listViewItems = new ArrayList<>();
        listViewItems.add(new MedData(2L, 5L));
        listViewItems.add(new MedData(3L, 5L));
        listViewItems.add(new MedData(5L, 8L));
        listViewItems.add(new MedData(4L, 8L));
        listViewItems.add(new MedData(8L, 14L));
        listViewItems.add(new MedData(9L, 13L));
        listViewItems.add(new MedData(10L, 16L));
        return listViewItems;
    }

    private void setupSparkView() {
        MedicineSparkAdapter adapter = new MedicineSparkAdapter(medDataList);
        sparkView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.Medicine;
import com.androidstudy.andelamedmanager.databinding.ActivityConfirmMedicineBinding;
import com.androidstudy.andelamedmanager.ui.medicine.viewmodel.AddMedicineViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ConfirmMedicineActivity extends AppCompatActivity {

    private ActivityConfirmMedicineBinding binding;

    TextView textViewMedName;
    TextView textViewMedDescription;
    TextView textViewMedInterval;
    TextView textViewMedStartDate;
    TextView textViewMedEndDate;
    Button buttonEditMedicine;
    Button buttonSaveMedicine;

    String name;
    String description;
    String interval;
    String startDate;
    String endDate;
    String pills;
    int days;
    Bundle bundle;
    private AddMedicineViewModel addMedicineViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        addMedicineViewModel = new ViewModelProvider(ConfirmMedicineActivity.this).get(AddMedicineViewModel.class);

        Intent intent = getIntent();
        bundle = intent.getExtras();

        assert bundle != null;
        name = bundle.getString("name");
        description = bundle.getString("description");
        interval = bundle.getString("interval");
        startDate = bundle.getString("startDate");
        endDate = bundle.getString("endDate");
        pills = bundle.getString("pills");
        days = bundle.getInt("pills");

        textViewMedName.setText(name);
        textViewMedDescription.setText(description);
        textViewMedInterval.setText(interval);
        textViewMedStartDate.setText(startDate);
        textViewMedEndDate.setText(endDate);

        /**
         * User needs to edit the details again :)
         */
        buttonEditMedicine.setOnClickListener(v -> {
            Intent med = new Intent(getApplicationContext(), AddMedicineActivity.class);
            med.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(med);
        });

        /**
         * User is happy with the data :) Save :)
         */
        buttonSaveMedicine.setOnClickListener(v -> saveMedicine());
    }

    private void initViews() {
        textViewMedName = binding.textViewMedName;
        textViewMedDescription = binding.textViewMedDescription;
        textViewMedInterval = binding.textViewMedInterval;
        textViewMedStartDate = binding.textViewMedStartDate;
        textViewMedEndDate = binding.textViewMedEndDate;
        buttonEditMedicine = binding.buttonEditMedicine;
        buttonSaveMedicine = binding.buttonSaveMedicine;
    }

    private void saveMedicine() {

        Date dateStart = null;
        Date dateEnd = null;

        DateFormat srcDf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dateStart = srcDf.parse(String.valueOf(startDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            dateEnd = srcDf.parse(String.valueOf(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addMedicineViewModel.addMedicine(new Medicine(
                name,
                description,
                interval,
                pills,
                "0",
                true,
                dateStart,
                dateEnd,
                days
        ));

        Intent success = new Intent(ConfirmMedicineActivity.this, MedicineSuccessActivity.class);
        success.putExtras(bundle);
        startActivity(success);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.databinding.ActivityMedicineSuccessBinding;
import com.androidstudy.andelamedmanager.ui.main.ui.MainActivity;

public class MedicineSuccessActivity extends AppCompatActivity {

    private ActivityMedicineSuccessBinding binding;

    TextView textViewMedicineName;
    TextView textViewMedicineDescription;
    TextView textViewMedicineInterval;
    TextView textViewMedicineStartDate;
    TextView textViewMedicineEndDate;
    Button buttonAddMedicine;
    Button buttonHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        assert bundle != null;
        textViewMedicineName.setText(bundle.getString("name"));
        textViewMedicineDescription.setText(bundle.getString("description"));
        textViewMedicineInterval.setText(bundle.getString("interval"));
        textViewMedicineStartDate.setText(bundle.getString("startDate"));
        textViewMedicineEndDate.setText(bundle.getString("endDate"));

        /**
         * Redirect to add another medicine!
         */
        buttonAddMedicine.setOnClickListener(view -> {
            Intent addMedicine = new Intent(getApplicationContext(), AddMedicineActivity.class);
            startActivity(addMedicine);
            this.finish();
        });

        /**
         * Redirect back to home page, User is satisfied/done!
         */
        buttonHomePage.setOnClickListener(view -> {
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            this.finish();
        });
    }

    private void initViews() {
        textViewMedicineName = binding.textViewMedicineName;
        textViewMedicineDescription = binding.textViewMedicineDescription;
        textViewMedicineInterval = binding.textViewMedicineInterval;
        textViewMedicineStartDate = binding.textViewMedicineStartDate;
        textViewMedicineEndDate = binding.textViewMedicineEndDate;
        buttonAddMedicine = binding.buttonAddMedicine;
        buttonHomePage = binding.buttonHomePage;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

}}

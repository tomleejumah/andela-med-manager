package com.androidstudy.andelamedmanager.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.User;
import com.androidstudy.andelamedmanager.databinding.DialogProfileBinding;
import com.androidstudy.andelamedmanager.ui.main.viewmodel.MainViewModel;
import com.bumptech.glide.Glide;

public class ProfileDialog extends DialogFragment {

    private static MaterialDialog.SingleButtonCallback callback;
    private MainViewModel mainViewModel;
    private DialogProfileBinding binding;

    public static ProfileDialog newInstance(MaterialDialog.SingleButtonCallback buttonCallback) {
        callback = buttonCallback;
        return new ProfileDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

//        User user = mainViewModel.getUserLiveData().getValue();
//        User user = mainViewModel.getUserLiveData();

       mainViewModel.getUserLiveData().observe(this, user -> {
           if (user != null) {
               binding.textViewName.setText(user.getName());
               Glide.with(this)
                       .load(user.getImageUrl())
                       .into(binding.imageViewUser);
           }
       });

//        if (user != null) {
//            binding.textViewName.setText(user.getName());
//            Glide.with(this)
//                    .load(user.getImageUrl())
//                    .into(binding.imageViewUser);
//        }

        return new MaterialDialog.Builder(requireContext())
                .customView(view, false)
                .positiveText("Sign out")
                .onPositive(callback)
                .build();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

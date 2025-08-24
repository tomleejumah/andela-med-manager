package com.androidstudy.andelamedmanager.ui.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.MenuView;

import java.util.List;

public class MainDashboardAdapter extends RecyclerView.Adapter<MainDashboardAdapter.MenuOptionsViewHolder> {
    CustomItemClickListener listener;
    private Context context;
    private List<MenuView> menuList;

    public MainDashboardAdapter(Context context, List<MenuView> menuList, CustomItemClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @Override
    public MenuOptionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_main_dashboard, null);
        final MenuOptionsViewHolder mViewHolder = new MenuOptionsViewHolder(view);
        view.setOnClickListener(v -> listener.onItemClick(v, mViewHolder.getPosition()));
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(MenuOptionsViewHolder holder, int position) {
        MenuView menuItem = menuList.get(position);
        holder.bindData(menuItem);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MenuOptionsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public MenuOptionsViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }

        private void bindData(MenuView menuItem) {
            textView.setText(menuItem.getName());
            imageView.setImageResource(menuItem.getImage());
        }
    }

}
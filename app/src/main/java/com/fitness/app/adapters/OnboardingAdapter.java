package com.fitness.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> items;

    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIllustration;
        private final TextView tvTitle;
        private final TextView tvDesc;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIllustration = itemView.findViewById(R.id.ivOnboardingImage);
            tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
            tvDesc = itemView.findViewById(R.id.tvOnboardingDesc);
        }

        public void bind(OnboardingItem item) {
            ivIllustration.setImageResource(item.getImageResId());
            tvTitle.setText(item.getTitle());
            tvDesc.setText(item.getDesc());
        }
    }

    public static class OnboardingItem {
        private final int imageResId;
        private final String title;
        private final String desc;

        public OnboardingItem(int imageResId, String title, String desc) {
            this.imageResId = imageResId;
            this.title = title;
            this.desc = desc;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDesc() {
            return desc;
        }
    }
}

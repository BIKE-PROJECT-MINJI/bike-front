package com.bikeprojectminji.bikefront.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bikeprojectminji.bikefront.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeaturedCourseAdapter extends RecyclerView.Adapter<FeaturedCourseAdapter.FeaturedCourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(FeaturedCourseUiModel course);
    }

    private final List<FeaturedCourseUiModel> items = new ArrayList<>();
    private final OnCourseClickListener onCourseClickListener;

    public FeaturedCourseAdapter(OnCourseClickListener onCourseClickListener) {
        this.onCourseClickListener = onCourseClickListener;
    }

    public void submitList(List<FeaturedCourseUiModel> courses) {
        items.clear();
        items.addAll(courses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_course, parent, false);
        return new FeaturedCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedCourseViewHolder holder, int position) {
        holder.bind(items.get(position), onCourseClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FeaturedCourseViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView distanceTextView;
        private final TextView durationTextView;

        FeaturedCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.featuredCourseTitleTextView);
            distanceTextView = itemView.findViewById(R.id.featuredCourseDistanceTextView);
            durationTextView = itemView.findViewById(R.id.featuredCourseDurationTextView);
        }

        void bind(FeaturedCourseUiModel item, OnCourseClickListener listener) {
            titleTextView.setText(item.getTitle());
            distanceTextView.setText(formatDistance(item));
            durationTextView.setText(itemView.getContext().getString(R.string.featured_course_duration_format, item.getEstimatedDurationMin()));
            itemView.setOnClickListener(v -> listener.onCourseClick(item));
        }

        private String formatDistance(FeaturedCourseUiModel item) {
            if (item.getDistanceKm() < 1) {
                int distanceInMeters = (int) Math.round(item.getDistanceKm() * 1000);
                return itemView.getContext().getString(R.string.featured_course_distance_meter_format, distanceInMeters);
            }

            return String.format(Locale.KOREA, itemView.getContext().getString(R.string.featured_course_distance_km_format), item.getDistanceKm());
        }
    }
}

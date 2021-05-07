package org.oves.mobapp_mqtt_androidhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.oves.mobapp_mqtt_androidhub.R;

import java.util.List;

public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.ViewHolder> {
    private final List<String> topicsList;
    private final Context context;

    public SubscriptionsAdapter(List<String> topicsList, Context context) {
        this.topicsList = topicsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscriptions_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String topicSubscribedTo = topicsList.get(position);
        holder.textView.setText(topicSubscribedTo);
    }

    @Override
    public int getItemCount() {
        return topicsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.subscription);
        }
    }
}

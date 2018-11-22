package com.jsync.fileshare.sharedList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jsync.fileshare.R;

import java.util.ArrayList;

/**
 * Created by jaseem on 7/11/18.
 */

public class SharedListAdapter extends RecyclerView.Adapter<SharedListAdapter.ViewHolder> {
    private ArrayList<ShareListModel> models;

    public SharedListAdapter(){
        models = new ArrayList<>();
    }

    public void add(ShareListModel model){
        models.add(model);
        notifyDataSetChanged();
    }

    public void updateProgress(int id, int progress){
        ShareListModel model = models.get(id);
        model.setProgress(progress);
        model.setPercent(progress + "%");
        notifyItemChanged(id);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sr_progress_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShareListModel model = models.get(position);
        holder.txtFileName.setText(model.getFileName());
        holder.txtPercent.setText(model.getPercent());
        holder.progressBar.setProgress(model.getProgress());
        holder.txtWho.setText(model.getWho());
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;
        private TextView txtPercent;
        private TextView txtFileName;
        private TextView txtWho;

        public ViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progress_bar_sr);
            txtFileName = view.findViewById(R.id.txt_file_name);
            txtPercent = view.findViewById(R.id.txt_sr_percent);
            txtWho = view.findViewById(R.id.txt_who);
        }
    }
}

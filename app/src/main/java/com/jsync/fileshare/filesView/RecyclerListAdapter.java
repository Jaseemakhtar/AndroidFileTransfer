package com.jsync.fileshare.filesView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jsync.fileshare.R;

import java.util.ArrayList;

/**
 * Created by jaseem on 11/9/18.
 */

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.FileDetailHolder> {
    private ArrayList<String> mList = new ArrayList<>();
    private OnClickItem onClickItem;

    public void setOnClickItem(OnClickItem onClickItem){
        this.onClickItem = onClickItem;
    }

    public void add(String item){
        mList.add(item);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_file_single_row,parent, false);
        return new FileDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileDetailHolder holder, int position) {
        String name = mList.get(position);
        holder.txtName.setText(name);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class FileDetailHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtName;
        public FileDetailHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtName = itemView.findViewById(R.id.txt_fm_name);
        }

        @Override
        public void onClick(View v) {
            if (onClickItem != null)
                onClickItem.onClickItem(v, getAdapterPosition());
        }
    }

    public interface OnClickItem{
        void onClickItem(View view, int pos);
    }
}

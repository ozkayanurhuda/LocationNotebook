package com.nurozkaya.javamap.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nurozkaya.javamap.databinding.RecyclerRowBinding;
import com.nurozkaya.javamap.model.Place;
import com.nurozkaya.javamap.view.MapsActivity;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    List<Place> placeList;

    public PlaceAdapter(List<Place> placeList) {
        this.placeList=placeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.PlaceHolder holder, int position) {
        //liste içindeki maddeler tek tek gösterilecek
        holder.recyclerRowBinding.recyclerViewText.setText(placeList.get(position).name);
        //tıklanınca intent maps activitye gönder
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(holder.itemView.getContext(), MapsActivity.class);
                //herhangi bi veri yolluyorum, eski bir veri geliyor
                intent.putExtra("info","old");
                //yollamak için serializable
                intent.putExtra("place", placeList.get(position));
                holder.itemView.getContext().startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding recyclerRowBinding;


        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot()); //view istiyor
            this.recyclerRowBinding=recyclerRowBinding;
        }
    }
}

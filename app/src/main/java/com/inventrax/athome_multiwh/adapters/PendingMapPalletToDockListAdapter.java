package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.VlpdDto;

import java.util.List;

public class PendingMapPalletToDockListAdapter extends RecyclerView.Adapter{

    private List<VlpdDto> pendingMapPalletToDockList;

    Context context;

    public PendingMapPalletToDockListAdapter(Context context, List<VlpdDto> list) {
        this.context = context;
        this.pendingMapPalletToDockList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtPallet;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtPallet = (TextView) itemView.findViewById(R.id.txtPallet);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_mappallettodock_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        VlpdDto vlpdDto = (VlpdDto) pendingMapPalletToDockList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtPallet.setText(vlpdDto.getPickedPalletNumber());


    }


    @Override
    public int getItemCount() {
        return pendingMapPalletToDockList.size();
    }
}

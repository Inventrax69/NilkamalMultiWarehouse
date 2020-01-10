package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.pojos.InventoryDTO;
import com.inventrax.athome_multiwh.R;

import java.util.List;

public class LiveStockAdapter extends RecyclerView.Adapter {

    private List<InventoryDTO> liveStockList;

    Context context;

    public LiveStockAdapter(Context context, List<InventoryDTO> list) {
        this.context = context;
        this.liveStockList = list;

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvMCode, tvSLoc, tvDesc, tvQty, tvLocationCode, tvBatch, tvBox;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            tvMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            tvSLoc = (TextView) itemView.findViewById(R.id.txtSLoc);
            tvDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            tvQty = (TextView) itemView.findViewById(R.id.txtQty);
            tvLocationCode = (TextView) itemView.findViewById(R.id.txtLocationCode);
            tvBatch = (TextView) itemView.findViewById(R.id.txtBatch);
            tvBox = (TextView) itemView.findViewById(R.id.txtBox);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.livestock_row_rsn, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {



        InventoryDTO inventoryDTO = (InventoryDTO) liveStockList.get(position);

        // set the data in items
        ((MyViewHolder) holder).tvMCode.setText(inventoryDTO.getMaterialCode());
        ((MyViewHolder) holder).tvLocationCode.setText(inventoryDTO.getLocationCode());
        ((MyViewHolder) holder).tvQty.setText(inventoryDTO.getQuantity());
        ((MyViewHolder) holder).tvDesc.setText(inventoryDTO.getMaterialShortDescription());
        ((MyViewHolder) holder).tvBatch.setText(inventoryDTO.getBatchNumber());
        ((MyViewHolder) holder).tvBox.setText(inventoryDTO.getBoxQty());
        ((MyViewHolder) holder).tvSLoc.setText(inventoryDTO.getSLOC());

    }


    @Override
    public int getItemCount() {
        return liveStockList.size();
    }

}
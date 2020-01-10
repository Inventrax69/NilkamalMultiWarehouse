package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.StockCountDTO;

import java.util.List;

public class PendingStockTakeListAdapter extends RecyclerView.Adapter {


    private  List<StockCountDTO> itemInfoDTOLst;



    Context context;
    public PendingStockTakeListAdapter(Context context,   List<StockCountDTO>  list) {
        this.context = context;
        this.itemInfoDTOLst = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtMCode,txtQty,txtDesc,txtHU,txtHUNOSize,txtRSN;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtHU = (TextView) itemView.findViewById(R.id.txtHU);
            txtHUNOSize = (TextView) itemView.findViewById(R.id.txtHUNOSize);
            txtRSN = (TextView) itemView.findViewById(R.id.txtRSN);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_loading_list_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new PendingStockTakeListAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        StockCountDTO stockCountDTO = (StockCountDTO) itemInfoDTOLst.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtMCode.setText(stockCountDTO.getSKU());
        ((MyViewHolder) holder).txtDesc.setText(stockCountDTO.getPartdesc());
        ((MyViewHolder) holder).txtHU.setText(stockCountDTO.getStorageLocation());
        ((MyViewHolder) holder).txtQty.setText(stockCountDTO.getBoxQty());

    }


    @Override
    public int getItemCount() {
        return itemInfoDTOLst.size();
    }
}
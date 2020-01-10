package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;

import java.util.List;

public class PendingLoadingListAdapter extends RecyclerView.Adapter {


    private List<ItemInfoDTO> pendingLoadingList;

    Context context;
    public PendingLoadingListAdapter(Context context, List<ItemInfoDTO> list) {
        this.context = context;
        this.pendingLoadingList = list;
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
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ItemInfoDTO vlpdLoadingDTO = (ItemInfoDTO) pendingLoadingList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtMCode.setText(vlpdLoadingDTO.getMcode());
        ((MyViewHolder) holder).txtDesc.setText(vlpdLoadingDTO.getDescription());
        ((MyViewHolder) holder).txtHU.setText(vlpdLoadingDTO.getHuNo());
        ((MyViewHolder) holder).txtRSN.setText(vlpdLoadingDTO.getRSN());
        ((MyViewHolder) holder).txtHUNOSize.setText(vlpdLoadingDTO.getHuNo());
        ((MyViewHolder) holder).txtQty.setText(vlpdLoadingDTO.getAvlQuantity());

    }


    @Override
    public int getItemCount() {
        return pendingLoadingList.size();
    }
}
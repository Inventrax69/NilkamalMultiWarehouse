package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.PickPendingItemDTO;

import java.util.List;

public class PendingPickItemListAdapter extends RecyclerView.Adapter {

private List<PickPendingItemDTO> pendingOutboundList;

        Context context;
public PendingPickItemListAdapter(Context context, List<PickPendingItemDTO> list) {
        this.context = context;
        this.pendingOutboundList = list;
        }


public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView txtMCode,txtQty,txtDesc,txtHU,txtLocationCode,txtBatch,txtDispatchNo;// init the item view's

    public MyViewHolder(View itemView) {

        super(itemView);
        // get the reference of item view's
        txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
        txtDispatchNo = (TextView) itemView.findViewById(R.id.txtDispatchNo);
        txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
        txtLocationCode = (TextView) itemView.findViewById(R.id.txtLocationCode);
        txtBatch = (TextView) itemView.findViewById(R.id.txtBatch);
        txtHU = (TextView) itemView.findViewById(R.id.txtHU);
        txtQty = (TextView) itemView.findViewById(R.id.txtQty);
    }
}


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_pickitemlist_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        PickPendingItemDTO pickPendingItemDTO = (PickPendingItemDTO) pendingOutboundList.get(position);

        // set the data in items
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtMCode.setText(pickPendingItemDTO.getmCode());
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtDesc.setText(pickPendingItemDTO.getDescription());
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtBatch.setText(pickPendingItemDTO.getBatchNumber());

        if(pickPendingItemDTO.getHUSize()!=null && pickPendingItemDTO.getHUSize()!="0" &&
                pickPendingItemDTO.getHUNo()!=null && pickPendingItemDTO.getHUNo()!="0"){
            ((PendingOutboundListAdapter.MyViewHolder) holder).txtHU.setText(pickPendingItemDTO.getHUNo()+"/"+pickPendingItemDTO.getHUSize());
        }
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtQty.setText(pickPendingItemDTO.getQuantity());
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtLocationCode.setText(pickPendingItemDTO.getLocation());
        ((PendingOutboundListAdapter.MyViewHolder) holder).txtDispatchNo.setText(pickPendingItemDTO.getDispatchNumber());

    }


    @Override
    public int getItemCount() {
        return pendingOutboundList.size();
    }
}

package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.InboundDTO;

import java.util.List;

public class PendingInboundListAdapter extends RecyclerView.Adapter{

    private List<InboundDTO> pendingInboundList;

    Context context;
    public PendingInboundListAdapter(Context context, List<InboundDTO> list) {
        this.context = context;
        this.pendingInboundList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvMCode,tvHUN_Size,tvDesc,tvHUSize,tvInvQty,tvBatch,tvPenQty;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            tvMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            tvHUN_Size = (TextView) itemView.findViewById(R.id.txtHUN_Size);
            tvDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            tvInvQty = (TextView) itemView.findViewById(R.id.txtInvQty);
            tvBatch = (TextView) itemView.findViewById(R.id.txtBatch);
            tvPenQty = (TextView) itemView.findViewById(R.id.txtPenQty);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_inbound_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InboundDTO inboundDTO = (InboundDTO) pendingInboundList.get(position);

        // set the data in items
        ((MyViewHolder) holder).tvMCode.setText(inboundDTO.getMaterialCode());
        ((MyViewHolder) holder).tvDesc.setText(inboundDTO.getmDesc());
        ((MyViewHolder) holder).tvBatch.setText(inboundDTO.getBatchNo());

        if(inboundDTO.getHUsize()!=null && inboundDTO.getHUsize()!="0" &&
                inboundDTO.getHUNumber()!=null && inboundDTO.getHUNumber()!="0"){
            ((MyViewHolder) holder).tvHUN_Size.setText(inboundDTO.getHUNumber()+"/"+inboundDTO.getHUsize());
        }
        ((MyViewHolder) holder).tvInvQty.setText("Inv. Qty: " + inboundDTO.getInvoiceQuantity());
        ((MyViewHolder) holder).tvPenQty.setText("Pend. Qty: " + inboundDTO.getPendingQty());

    }


    @Override
    public int getItemCount() {
        return pendingInboundList.size();
    }
}

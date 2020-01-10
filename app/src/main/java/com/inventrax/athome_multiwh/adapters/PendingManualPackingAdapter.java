package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.OutboundDTO;

import java.util.List;

public class PendingManualPackingAdapter extends RecyclerView.Adapter {

    private List<OutboundDTO> pendingOutboundList;

    Context context;
    public PendingManualPackingAdapter(Context context, List<OutboundDTO> list) {
        this.context = context;
        this.pendingOutboundList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtMCode,txtQty,txtDesc,txtEAN,txtReqQty,txtPendingQty,txtDispatchNo;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtReqQty = (TextView) itemView.findViewById(R.id.txtReqQty);
            txtPendingQty = (TextView) itemView.findViewById(R.id.txtPendingQty);
            txtEAN = (TextView) itemView.findViewById(R.id.txtEAN);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_manual_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        OutboundDTO outboundDTO = (OutboundDTO) pendingOutboundList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtMCode.setText(outboundDTO.getSKU());
        ((MyViewHolder) holder).txtDesc.setText(outboundDTO.getDescription());
        ((MyViewHolder) holder).txtReqQty.setText(outboundDTO.getRequiredQty());
        ((MyViewHolder) holder).txtPendingQty.setText(outboundDTO.getSKUPendingQty());
        ((MyViewHolder) holder).txtEAN.setText(outboundDTO.getEANNumber());


    }


    @Override
    public int getItemCount() {
        return pendingOutboundList.size();
    }
}

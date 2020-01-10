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

public class PendingBintoBinListAdapter extends RecyclerView.Adapter {

    private List<OutboundDTO> pendingBintoBinList;

    Context context;
    public PendingBintoBinListAdapter(Context context, List<OutboundDTO> list) {
        this.context = context;
        this.pendingBintoBinList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtMCode,txtQty,txtDesc,txtHU,txtLocationCode,txtBatch,txtSLoc;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtSLoc = (TextView) itemView.findViewById(R.id.txtSLoc);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_bintobin_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        OutboundDTO outboundDTO = (OutboundDTO) pendingBintoBinList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtMCode.setText(outboundDTO.getSKU());
        ((MyViewHolder) holder).txtDesc.setText(outboundDTO.getDescription());
        ((MyViewHolder) holder).txtBatch.setText("Bat.#: " + outboundDTO.getBatchNo());

        if(outboundDTO.getHUsize()!=null && outboundDTO.getHUsize()!="0" &&
                outboundDTO.getHUNumber()!=null && outboundDTO.getHUNumber()!="0"){
            ((MyViewHolder) holder).txtHU.setText("HUNo/HU: " + outboundDTO.getHUNumber()+"/"+outboundDTO.getHUsize());
        }
        ((MyViewHolder) holder).txtQty.setText(outboundDTO.getAvailQuantity());
        ((MyViewHolder) holder).txtLocationCode.setText("Loc: " + outboundDTO.getLocation());
        ((MyViewHolder) holder).txtSLoc.setText("SLoc.: " + outboundDTO.getsLoc());

    }


    @Override
    public int getItemCount() {
        return pendingBintoBinList.size();
    }
}
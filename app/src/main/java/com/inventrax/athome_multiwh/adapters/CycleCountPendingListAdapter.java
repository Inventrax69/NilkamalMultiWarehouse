package com.inventrax.athome_multiwh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.pojos.CycleCountDTO;

import java.util.List;

public class CycleCountPendingListAdapter   extends RecyclerView.Adapter {

    private List<CycleCountDTO> pendingCycleCountList;

    Context context;
    public CycleCountPendingListAdapter(Context context, List<CycleCountDTO> list) {
        this.context = context;
        this.pendingCycleCountList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtMCode,txtCCQty,txtDesc,txtCCName,txtLocation,txtBarcode;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtCCQty = (TextView) itemView.findViewById(R.id.txtCCQty);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtCCName = (TextView) itemView.findViewById(R.id.txtCCName);
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtBarcode = (TextView) itemView.findViewById(R.id.txtBarcode);


        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_cyclecount_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        CycleCountDTO cycleCountDTO = (CycleCountDTO) pendingCycleCountList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtMCode.setText(cycleCountDTO.getMaterialCode());
        ((MyViewHolder) holder).txtDesc.setText(cycleCountDTO.getMDesc());
        ((MyViewHolder) holder).txtCCQty.setText(cycleCountDTO.getCCQty());


        ((MyViewHolder) holder).txtCCName.setText(cycleCountDTO.getSelectedCCName());
        ((MyViewHolder) holder).txtLocation.setText(cycleCountDTO.getLocation());
        ((MyViewHolder) holder).txtBarcode.setText(cycleCountDTO.getBarcode());

    }


    @Override
    public int getItemCount() {
        return pendingCycleCountList.size();
    }
}

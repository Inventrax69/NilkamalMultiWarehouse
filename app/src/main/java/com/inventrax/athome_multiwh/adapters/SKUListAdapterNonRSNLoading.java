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

public class SKUListAdapterNonRSNLoading extends RecyclerView.Adapter {

    private List<ItemInfoDTO> lstItems;
    OnItemClickListener listener;
    Context context;

    public SKUListAdapterNonRSNLoading(Context context, List<ItemInfoDTO> lstItems, OnItemClickListener mlistener) {
        this.context = context;
        this.lstItems = lstItems;
        listener = mlistener;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvMCode;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            tvMCode = (TextView) itemView.findViewById(R.id.tvMCode);

            //on item click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                }

            });

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_skulist_nonrsn_loading, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {



        ItemInfoDTO itemInfoDTO = (ItemInfoDTO) lstItems.get(position);

        // set the data in items
        ((MyViewHolder) holder).tvMCode.setText(itemInfoDTO.getMcode());


    }

    // Item click listener interface
    public interface OnItemClickListener {
        void onItemClick(int pos);


    }

    @Override
    public int getItemCount() {
        return lstItems.size();
    }

}
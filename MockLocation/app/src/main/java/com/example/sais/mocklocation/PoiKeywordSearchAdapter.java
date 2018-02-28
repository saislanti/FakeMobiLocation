package com.example.sais.mocklocation;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by ywq on 2017-11-03.
 */
public class PoiKeywordSearchAdapter extends RecyclerView.Adapter<PoiKeywordSearchAdapter.MyViewHolder>{

    List<PoiAddressBean> mPoiAddressBeans;
    Context mContext;

    public PoiKeywordSearchAdapter(List<PoiAddressBean> poiAddressBeans, Context context) {
        this.mPoiAddressBeans = poiAddressBeans;
        this.mContext = context;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.item_poi_key_search, parent, false);
        return new MyViewHolder(view);
    }

    public void onBindViewHolder(MyViewHolder holder, int position){
        final PoiAddressBean poiAddressBean = this.mPoiAddressBeans.get(position);
        holder.tv_detailAddress.setText(poiAddressBean.getDetailAddress());
        holder.tv_content.setText(poiAddressBean.getText());
        holder.ll_item_layout.setOnClickListener(new View.OnClickListener(){
            final String lat = poiAddressBean.getLatitude();
            final String log = poiAddressBean.getLongitude();
            final String detailAddress = poiAddressBean.getDetailAddress();

            public void onClick(View view){
                ((MapActivity)mContext).setNewMarkOptions(detailAddress, lat, log);
//                Intent i = new Intent();
//                i.putExtra("poiAddressBean", poiAddressBean);

            }
        });
    }

    public int getItemCount(){
        if(mPoiAddressBeans != null){
            return mPoiAddressBeans.size();
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_content;
        TextView tv_detailAddress;
        LinearLayout ll_item_layout;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_content = (TextView)itemView.findViewById(R.id.tv_content);
            tv_detailAddress = (TextView)itemView.findViewById(R.id.tv_detailAddress);
            ll_item_layout = (LinearLayout)itemView.findViewById(R.id.ll_item_layout);
        }
    }
}

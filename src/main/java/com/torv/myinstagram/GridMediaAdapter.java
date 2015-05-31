package com.torv.myinstagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

/**
 * Created by lijian on 5/31/15.
 */
public class GridMediaAdapter extends BaseAdapter{

    private LayoutInflater mInflater;
    List<String> mUrlList;

    public GridMediaAdapter(Context context, List<String> urlList) {
        mInflater = LayoutInflater.from(context);
        mUrlList = urlList;
    }

    @Override
    public int getCount() {
        return mUrlList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if(null == view){
            holder = new ViewHolder();

            view = mInflater.inflate(R.layout.grid_item, null);
            holder.iv_item = (NetworkImageView) view.findViewById(R.id.ni_grid_item);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.iv_item.setImageUrl(mUrlList.get(index), VolleyInstance.instance.getImageLoader());

        return view;
    }

    class ViewHolder{
        public NetworkImageView iv_item;
    }
}

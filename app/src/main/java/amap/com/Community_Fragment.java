package amap.com;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import amap.com.android_path_record.R;

public class Community_Fragment extends Fragment {
    private int[] picture=new int[]{R.mipmap.img1,R.mipmap.img2};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.community_fragment,null);
        GridView gridView=(GridView) view.findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(view.getContext()));
        return view;
    }
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ImageAdapter(Context c){
            mContext=c;
        }
        @Override
        public int getCount() {
            return picture.length;
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView;
            if(view==null){
                imageView=new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300,270));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else{
                imageView=(ImageView) view;
            }
            imageView.setImageResource(picture[i]);//i是position
            return imageView;
        }
    }
}
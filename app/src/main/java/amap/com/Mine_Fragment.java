package amap.com;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import amap.com.android_path_record.MainActivity;
import amap.com.android_path_record.R;

public class Mine_Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Button button_run= getView().findViewById(R.id.button3);
        //加了这一行后mine会闪退

        View view=inflater.inflate(R.layout.mine_fragment,null);
        RelativeLayout relativeLayout=view.findViewById(R.id.relativelayout);
        LinearLayout linearLayout=new LinearLayout(view.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView=new ScrollView(view.getContext());
        relativeLayout.addView(scrollView);
        scrollView.addView(linearLayout);
        Button button=new Button(view.getContext());
        linearLayout.addView(button);
        /*
        button_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        });
        */
        return view;
    }
}

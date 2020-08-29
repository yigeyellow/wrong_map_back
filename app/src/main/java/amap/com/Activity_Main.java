package amap.com;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import amap.com.android_path_record.MainActivity;
import amap.com.android_path_record.R;
import amap.com.android_path_record.RecordActivity;


public class Activity_Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView1=(TextView)findViewById(R.id.text1);
        TextView textView2=(TextView)findViewById(R.id.text2);
        TextView textView3=(TextView)findViewById(R.id.text3);
        textView1.setOnClickListener(l);
        textView2.setOnClickListener(l);
        textView3.setOnClickListener(l);

    }
    View.OnClickListener l=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager=getFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            Fragment fragment=null;
            switch(view.getId()){
                case R.id.text1:
                    fragment= new Community_Fragment();
                    break;
                case R.id.text2:
                    Intent intent = new Intent(Activity_Main.this,MainActivity.class);
                    startActivity(intent);
                break;
                case R.id.text3:
                    fragment= new Mine_Fragment();
                    break;
                default:
                    break;
            }
            fragmentTransaction.replace(R.id.fragment,fragment);
            fragmentTransaction.commit();
        }
    };
}
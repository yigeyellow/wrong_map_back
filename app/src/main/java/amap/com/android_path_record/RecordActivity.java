package amap.com.android_path_record;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import amap.com.database.DbAdapter;
import amap.com.record.PathRecord;

/**
 * 所有轨迹list展示activity
 *
 */
public class RecordActivity extends Activity implements OnItemClickListener {

	private RecordAdapter mAdapter;
	private ListView mAllRecordListView;
	private DbAdapter mDataBaseHelper;
	private List<PathRecord> mAllRecord = new ArrayList<PathRecord>();//pathrecord是一条轨迹
	public static final String RECORD_ID = "record_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recordlist);
		mAllRecordListView = (ListView) findViewById(R.id.recordlist);
		mDataBaseHelper = new DbAdapter(this);
		mDataBaseHelper.open();
		searchAllRecordFromDB();//从数据库取出到mAllRecord
		mAdapter = new RecordAdapter(this, mAllRecord);//将mAllRecord数据给mAdapter
		mAllRecordListView.setAdapter(mAdapter);//关联ListView和mAdapter
		mAllRecordListView.setOnItemClickListener(this);//设置监听器
		//速度要在DbAdapter类里获取，在PathRecord类里添加
	}
	//从数据库取出到mAllRecord
	private void searchAllRecordFromDB() {
		mAllRecord = mDataBaseHelper.queryRecordAll();
	}
	//按back按键
	public void onBackClick(View view) {
		this.finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		PathRecord recorditem = (PathRecord) parent.getAdapter().getItem(
				position);
		Intent intent = new Intent(RecordActivity.this,
				RecordShowActivity.class);
		intent.putExtra(RECORD_ID, recorditem.getId());
		startActivity(intent);
	}
}

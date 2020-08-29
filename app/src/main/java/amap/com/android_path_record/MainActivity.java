package amap.com.android_path_record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import amap.com.database.DbAdapter;
import amap.com.record.PathRecord;
import amap.com.recorduitl.Util;


public class MainActivity extends Activity implements LocationSource,
		AMapLocationListener, TraceListener {
	private final static int CALLTRACE = 0;
	private MapView mMapView;
	private AMap mAMap;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mLocationClient;
	private AMapLocationClientOption mLocationOption;
	private PolylineOptions mPolyoptions, tracePolytion;
	private Polyline mpolyline;
	private PathRecord record;
	private long mStartTime;
	private long mEndTime;
	private ToggleButton btn;
	private DbAdapter DbHepler;
	private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
	private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
	private List<AMapLocation> recordList = new ArrayList<AMapLocation>();//一条轨迹
	private int tracesize = 30;
	private int mDistance = 0;
	private TraceOverlay mTraceoverlay;
	private TextView mResultShow;
	private Marker mlocMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basicmap_activity);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		initpolyline();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (mAMap == null) {
			mAMap = mMapView.getMap();
			setUpMap();
		}
		btn = (ToggleButton) findViewById(R.id.locationbtn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btn.isChecked()) {
					mAMap.clear(true);
					if (record != null) {
						record = null;
					}
					record = new PathRecord();
					mStartTime = System.currentTimeMillis();
					record.setDate(getcueDate(mStartTime));
					mResultShow.setText("总距离？");
				} else {
					mEndTime = System.currentTimeMillis();
					mOverlayList.add(mTraceoverlay);
					DecimalFormat decimalFormat = new DecimalFormat("0.0");
					mResultShow.setText(
							decimalFormat.format(getTotalDistance() / 1d) + "m");//总距离
					LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
					mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) , LBSTraceClient.TYPE_AMAP, MainActivity.this);
					saveRecord(record.getPathline(), record.getDate());
				}
			}
		});
		mResultShow = (TextView) findViewById(R.id.show_all_dis);

		mTraceoverlay = new TraceOverlay(mAMap);
	}
	//数据库保存record
	protected void saveRecord(List<AMapLocation> list, String time) {
		if (list != null && list.size() > 0) {
			DbHepler = new DbAdapter(this);//数据库
			DbHepler.open();//数据库打开
			String duration = getDuration();//时间
			float distance = getDistance(list);//距离
			String average = getAverage(distance);//均速
			String pathlineSring = getPathLineString(list);//获取轨道
			AMapLocation firstLocaiton = list.get(0);//获取首末位置
			AMapLocation lastLocaiton = list.get(list.size() - 1);
			String stratpoint = amapLocationToString(firstLocaiton);//获取首末点
			String endpoint = amapLocationToString(lastLocaiton);
			DbHepler.createrecord(String.valueOf(distance), duration, average,
					pathlineSring, stratpoint, endpoint, time);//数据库保存：距离，时间长，均速，轨道，首末点，时间
			DbHepler.close();//数据库关闭
		} else {
			Toast.makeText(MainActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
					.show();
		}
	}
	//时间
	private String getDuration() {
		return String.valueOf((mEndTime - mStartTime) / 1000f);
	}
	//均速
	private String getAverage(float distance) {
		return String.valueOf(1000*distance / (mEndTime - mStartTime));
		//return String.valueOf(distance / ((mEndTime - mStartTime) / 1000f));
	}
	//距离
	private float getDistance(List<AMapLocation> list) {
		float distance = 0;
		if (list == null || list.size() == 0) {
			return distance;
		}
		for (int i = 0; i < list.size() - 1; i++) {
			AMapLocation firstpoint = list.get(i);
			AMapLocation secondpoint = list.get(i + 1);
			LatLng firstLatLng = new LatLng(firstpoint.getLatitude(),
					firstpoint.getLongitude());
			LatLng secondLatLng = new LatLng(secondpoint.getLatitude(),
					secondpoint.getLongitude());
			double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
					secondLatLng);
			distance = (float) (distance + betweenDis);
		}
		return distance;
	}
	//分析轨迹上的点的数据来获取轨迹
	private String getPathLineString(List<AMapLocation> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuffer pathline = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			AMapLocation location = list.get(i);
			String locString = amapLocationToString(location);
			pathline.append(locString).append(";");
		}
		String pathLineString = pathline.toString();
		pathLineString = pathLineString.substring(0,
				pathLineString.length() - 1);//截取字符串
		return pathLineString;
	}
	//经纬度，？，时间，均速，？toString
	private String amapLocationToString(AMapLocation location) {
		StringBuffer locString = new StringBuffer();
		locString.append(location.getLatitude()).append(",");//append添加到末尾
		locString.append(location.getLongitude()).append(",");
		locString.append(location.getProvider()).append(",");
		locString.append(location.getTime()).append(",");
		locString.append(location.getSpeed()).append(",");
		locString.append(location.getBearing());
		return locString.toString();
	}


	private void initpolyline() {
		mPolyoptions = new PolylineOptions();
		mPolyoptions.width(10f);
		mPolyoptions.color(Color.GRAY);
		tracePolytion = new PolylineOptions();
		tracePolytion.width(40);
		tracePolytion.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line));
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		mAMap.setLocationSource(this);// 设置定位监听
		mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		startlocation();
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
			mLocationClient.onDestroy();

		}
		mLocationClient = null;
	}

	/**
	 * 定位结果回调
	 * @param amapLocation 位置信息类
     */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				LatLng mylocation = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());//新位置转为latlng
				mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));//把视角移动到位置
				if (btn.isChecked()) {
					record.addpoint(amapLocation);//记录中加点
					mPolyoptions.add(mylocation);//轨迹中加点
					mTracelocationlist.add(Util.parseTraceLocation(amapLocation));
					redrawline();//实时轨迹画线
					if (mTracelocationlist.size() > tracesize - 1) {
						trace();
					}
				}
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
			}
		}
	}

	/**
	 * 开始定位。
	 */
	private void startlocation() {
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mLocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);

			mLocationOption.setInterval(2000);

			// 设置定位参数
			mLocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mLocationClient.startLocation();

		}
	}

	/**
	 * 实时轨迹画线
	 */
	private void redrawline() {
		if (mPolyoptions.getPoints().size() > 1) {
			if (mpolyline != null) {
				mpolyline.setPoints(mPolyoptions.getPoints());
			} else {
				mpolyline = mAMap.addPolyline(mPolyoptions);
			}
		}
//		if (mpolyline != null) {
//			mpolyline.remove();
//		}
//		mPolyoptions.visible(true);
//		mpolyline = mAMap.addPolyline(mPolyoptions);
//			PolylineOptions newpoly = new PolylineOptions();
//			mpolyline = mAMap.addPolyline(newpoly.addAll(mPolyoptions.getPoints()));
//		}
	}
	//返回"yyyy-MM-dd  HH:mm:ss "形式的时间
	@SuppressLint("SimpleDateFormat")
	private String getcueDate(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss ");
		Date curDate = new Date(time);
		String date = formatter.format(curDate);
		return date;
	}
//貌似是点击record的效果
	public void record(View view) {
		Intent intent = new Intent(MainActivity.this, RecordActivity.class);
		startActivity(intent);
	}
//？？
	private void trace() {
		List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
		LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
		mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this);
		TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
		mTracelocationlist.clear();
		mTracelocationlist.add(lastlocation);
	}

	/**
	 * 轨迹纠偏失败回调。
	 * @param i
	 * @param s
     */
	@Override
	public void onRequestFailed(int i, String s) {
		mOverlayList.add(mTraceoverlay);
		mTraceoverlay = new TraceOverlay(mAMap);
	}

	@Override
	public void onTraceProcessing(int i, int i1, List<LatLng> list) {

	}

	/**
	 * 轨迹纠偏成功回调。
	 * @param lineID 纠偏的线路ID
	 * @param linepoints 纠偏结果
	 * @param distance 总距离
	 * @param waitingtime 等待时间
     */
	@Override
	public void onFinished(int lineID, List<LatLng> linepoints, int distance, int waitingtime) {
		if (lineID == 1) {
			if (linepoints != null && linepoints.size()>0) {
				mTraceoverlay.add(linepoints);
				mDistance += distance;
				mTraceoverlay.setDistance(mTraceoverlay.getDistance()+distance);
				if (mlocMarker == null) {
					mlocMarker = mAMap.addMarker(new MarkerOptions().position(linepoints.get(linepoints.size() - 1))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.point))
							.title("距离：" + mDistance+"米"));
					mlocMarker.showInfoWindow();
				} else {
					mlocMarker.setTitle("距离：" + mDistance+"米");
					Toast.makeText(MainActivity.this, "距离"+mDistance, Toast.LENGTH_SHORT).show();
					mlocMarker.setPosition(linepoints.get(linepoints.size() - 1));
					mlocMarker.showInfoWindow();
				}
			}
		} else if (lineID == 2) {
			if (linepoints != null && linepoints.size()>0) {
				mAMap.addPolyline(new PolylineOptions()
						.color(Color.RED)
						.width(40).addAll(linepoints));
			}
		}

	}

	/**
	 * 最后获取总距离
	 * @return
     */
	private int getTotalDistance() {
		int distance = 0;
		for (TraceOverlay to : mOverlayList) {
			distance = distance + to.getDistance();
		}
		return distance;
	}
	//按back按键
	public void onBackClick(View view) {
		this.finish();
	}
}

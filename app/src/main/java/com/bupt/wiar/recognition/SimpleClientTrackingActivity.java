package com.bupt.wiar.recognition;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bupt.wiar.R;
import com.bupt.wiar.rendering.CustomSurfaceView;
import com.bupt.wiar.rendering.Driver;
import com.bupt.wiar.rendering.GLRenderer;
import com.bupt.wiar.utils.HttpConnUtils;
import com.bupt.wiar.utils.JsonParser;
import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;

import java.util.Date;

public class SimpleClientTrackingActivity extends Activity implements ClientTrackerEventListener, ExternalRendering {

    private static final String TAG = "SimpleClientTracking";

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _view;
    private Driver _driver;
    private GLSurfaceView view;
    private GLRenderer _glRenderer;
    private EditText editText;
    private RenderExtension renderExtension;
    private TextView campusLocationName, campusMessageBackGroup, campusOpenTime, campusMoreDetail;
    private String s;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeriesRenderer mCurrentRenderer = new XYSeriesRenderer();
    private XYSeries mCurrentSeries = new XYSeries("Test");
    private GraphicalView mChartView;

    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), startupConfiguration);

        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/tracker.wtc");
        tracker.registerTrackerEventListener(this);

    //    new DataTask().execute("m1");
    }


    class DataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String s = HttpConnUtils.doGet(params[0]);
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JsonParser jsonParser = new JsonParser(s);
                String title = "实时人流量";
                //set the render of chart
                mRenderer = buildRenderer(Color.RED, PointStyle.CIRCLE);
                //getDataset
                mDataset = buildDateset(title, jsonParser.getDates(), jsonParser.getTraffics());

                if (mChartView == null) {
//                    layout = (RelativeLayout) findViewById(R.id.layout);
                    mChartView = ChartFactory.getTimeChartView(SimpleClientTrackingActivity.this, mDataset, mRenderer, "HH:mm");
                    layout.addView(mChartView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    protected XYMultipleSeriesDataset buildDateset(String title, Date[] xValues,
                                                   double[] yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series = new TimeSeries(title);

        //画TimeChart横坐标需要为Date数据类型
        int seriesLength = xValues.length;
        for (int i = 0; i < seriesLength; i++) {
            series.add(xValues[i], yValues[i]);
        }


        dataset.addSeries(series);

        return dataset;
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        renderer.setAxisTitleTextSize(40); //各种设置。。。。
        renderer.setChartTitleTextSize(50);
        renderer.setXTitle("时间");
        renderer.setYTitle("人流量");
        renderer.setLabelsTextSize(30);//设置刻度显示文字的大小(XY轴都会被设置)
        renderer.setLegendTextSize(30);//图例文字大小

        renderer.setBackgroundColor(0xF0000000);
        renderer.setPointSize(10);
        renderer.setMargins(new int[]{0, 40, 0, 0});  //设置图形四周的留白
        renderer.setMarginsColor(Color.TRANSPARENT);
//        renderer.setApplyBackgroundColor(true);//设置是否显示背景色
//        renderer.setBackgroundColor(Color.TRANSPARENT);//设置背景色

        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(true);

        renderer.addSeriesRenderer(r);

        return renderer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
        _view.onResume();
        _driver.start();


    }

    @Override
    protected void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
        _view.onPause();
        _driver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _wikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRenderer(renderExtension_);
        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_view, 30);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(_view);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        layout = (RelativeLayout) inflater.inflate(R.layout.activity_campus_recoder, null);
        viewHolder.addView(layout);

    }



    @Override
    public void onErrorLoading(final ClientTracker clientTracker_, final String errorMessage_) {
        Log.v(TAG, "onErrorLoading: " + errorMessage_);
    }

    @Override
    public void onTrackerFinishedLoading(final ClientTracker clientTracker_, final String trackerFilePath_) {

    }

    @Override
    public void onTargetRecognized(final Tracker tracker_, final String targetName_) {

          new DataTask().execute(targetName_);

//        _glRenderer = new GLRenderer(renderExtension);
//        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
//        _driver = new Driver(view, 30);

//        CustomSurfaceView view = (CustomSurfaceView) View.inflate(this, R.layout.activity_continuous_cloud_tracking, null);
//        TextView textView = (TextView) _view.findViewById(R.id.continuous_tracking_info_field);
//        textView.setText(targetName_);
//        _glRenderer = new GLRenderer(renderExtension);
//        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
//        _driver = new Driver(_view, 30);
//        setContentView(_view);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                campusMessageBackGroup = (TextView) findViewById(R.id.campus_location_name);
//                campusMessageBackGroup.setVisibility(View.VISIBLE);
//                campusMessageBackGroup.setBackgroundColor(Color.argb(255,0,255,0));
//                campusMessageBackGroup.setAlpha(0.8f);
//                campusLocationName = (TextView) findViewById(R.id.campus_location_name_item3);
//                campusLocationName.setText("Locaion:" + targetDetails);
//                campusLocationName.setVisibility(View.VISIBLE);
//                campusOpenTime = (TextView) findViewById(R.id.campus_location_open_time);
//                campusOpenTime.setText("Open TIme :" + "6:00 - 23:00");
//                campusOpenTime.setVisibility(View.VISIBLE);
//                campusMoreDetail = (TextView) findViewById(R.id.campus_location_more_detail);
//                campusMoreDetail.setText("If you want more details. " +
//                        "Please Visit the website : www.baidu.com");
//                campusMoreDetail.setVisibility(View.VISIBLE);
//
//            }
//        });


    }


    @Override
    public void onTracking(final Tracker tracker_, final RecognizedTarget recognizedTarget_) {
        _glRenderer.setCurrentlyRecognizedTarget(recognizedTarget_);
//        FrameLayout viewHolder = new FrameLayout(getApplicationContext());


//        viewHolder.addView(_view);
//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//        LinearLayout controls = (LinearLayout) inflater.inflate(R.layout.activity_continuous_cloud_tracking, null);
//        TextView textView = (TextView) _view.findViewById(R.id.continuous_tracking_info_field);
//        textView.setText(recognizedTarget_.getName());
//        _view.addTouchables(textView);
//        viewHolder.addView(_view);
//        setContentView(viewHolder);
    }

    @Override
    public void onTargetLost(final Tracker tracker_, final String targetName_) {
        _glRenderer.setCurrentlyRecognizedTarget(null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChartView.setVisibility(View.INVISIBLE);
                mChartView = null;
            }
        });

    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }


}
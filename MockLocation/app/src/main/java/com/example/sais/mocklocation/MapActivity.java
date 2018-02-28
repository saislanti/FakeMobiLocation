package com.example.sais.mocklocation;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.core.AMapException;

import com.example.sais.mocklocation.ToastUtil;


import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements AMap.OnMapClickListener,PoiSearch.OnPoiSearchListener{

    private MapView mv;
    private AMap aMap;
    private LatLng mLatLng;
    private String packageName;

    private RecyclerView mRecyclerView;
    private EditText mEt_keyword;
    private String keyWord = "";// 要输入的poi搜索关键字
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch       poiSearch;// POI搜索

    private LatLng changeLatLng;

//    private MapView mMapView;
//    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar)findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

//        mMapView = (MapView)findViewById(R.id.mv);
//        mMapView.onCreate(savedInstanceState);
//
//
//        if (aMap == null) {
//            aMap = mMapView.getMap();
//        }

        //地图展示
        mv = (MapView)findViewById(R.id.mv);
        //开启断言，若mv == null 则报告一个AssertionError
        assert mv != null;
        mv.onCreate(savedInstanceState);
        aMap = mv.getMap();

        Intent intent = getIntent();
//        double latitude = intent.getDoubleExtra("lat_map", 117.536246);
//        double longitude = intent.getDoubleExtra("log_map", 36.681752);
        LatLng latLng1 = new LatLng(39.904960632324, 116.39364624023);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng1);
        markerOptions.draggable(true);
        markerOptions.title("经度:" + latLng1.longitude + "纬度:" + latLng1.latitude);
        aMap.addMarker(markerOptions);
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng1));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));

        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.setOnMapClickListener(this);

//        initView();
//        initListener();
//        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mv.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.ok_map:
                if(mLatLng == null){
                    Toast.makeText(this, "点击地图选择一个点", Toast.LENGTH_SHORT).show();
                    return true;
                }

                save();
                break;
            case R.id.search_map:
                View view = LayoutInflater.from(this).inflate(R.layout.search_map, null, false);
//                final EditText et_map = (EditText)view.findViewById(R.id.et_map);
//                initView();
                mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
                mEt_keyword = (EditText) view.findViewById(R.id.et_map);
                initListener();
                initData();

                new AlertDialog.Builder(this).setView(view)
                        .setTitle("搜索位置")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mv.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mv.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mv.onDestroy();
    }

    public void onMapClick(LatLng latLng){
        aMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("经度：" + latLng.longitude + ",纬度：" + latLng.latitude);
        aMap.addMarker(markerOptions);
        this.mLatLng = latLng;
    }

    public void save() {
        Intent intent =new Intent();
        intent.putExtra("lat_map", mLatLng.latitude);
        intent.putExtra("log_map", mLatLng.longitude);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEt_keyword = (EditText) findViewById(R.id.et_map);
    }

    private void initListener() {
        mEt_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyWord = String.valueOf(charSequence);
                if ("".equals(keyWord)) {
                    ToastUtil.show(MapActivity.this, "请输入搜索关键字");
                    return;
                } else {
                    doSearchQuery();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        currentPage = 0;
        //不输入城市名称有些地方搜索不到
        query = new PoiSearch.Query(keyWord, "", "深圳");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        //这里没有做分页加载了,默认给50条数据
        query.setPageSize(50);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    private void initData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {  // 搜索poi的结果
                if (result.getQuery().equals(query)) {  // 是否是同一条
                    poiResult = result;
                    ArrayList<PoiAddressBean> data = new ArrayList<PoiAddressBean>();//自己创建的数据集合
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    for(PoiItem item : poiItems){
                        //获取经纬度对象
                        LatLonPoint llp = item.getLatLonPoint();
                        double lon = llp.getLongitude();
                        double lat = llp.getLatitude();

                        String title = item.getTitle();
                        String text = item.getSnippet();
                        String provinceName = item.getProvinceName();
                        String cityName = item.getCityName();
                        String adName = item.getAdName();
                        data.add(new PoiAddressBean(String.valueOf(lon), String.valueOf(lat), title, text,provinceName,
                                cityName,adName));
                    }
                    PoiKeywordSearchAdapter adapter = new PoiKeywordSearchAdapter(data,MapActivity.this);
                    mRecyclerView.setAdapter(adapter);
                }
            } else {
                ToastUtil.show(MapActivity.this,
                        getString(R.string.no_result));
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
        // TODO Auto-generated method stub

    }


    /**
     * 设置详情地址
     * @param latitude
     * @param longitude
     */
    public void setNewMarkOptions(String detailAddress, String latitude, String longitude) {
        mEt_keyword.setText(detailAddress);
        changeLatLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(changeLatLng));
        //return latLng;
    }
}

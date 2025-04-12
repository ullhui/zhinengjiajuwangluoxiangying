package com.example.home.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.home.R;
import com.example.home.app.MyApplication;
import com.example.home.bean.DeviceBean;
import com.example.home.bean.EquipmentBean;
import com.example.home.db.DaoSession;
import com.example.home.db.EquipmentBeanDao;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;


/**
 * 成品物料Adapter
 */
public class HomeAdapter extends BaseQuickAdapter<EquipmentBean, BaseViewHolder> implements LoadMoreModule {
    Activity mActivity;
    public HomeAdapter(@Nullable List<EquipmentBean> data,Activity mActivity) {
        super(R.layout.item_homeadapter, data);
        this.mActivity=mActivity;
        init();
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, EquipmentBean item) {

        ImageView image = baseViewHolder.findView(R.id.image);
        Switch switch1 = baseViewHolder.findView(R.id.switch1);
        if (!TextUtils.isEmpty(item.getName())) {
            switch (item.getName()) {
                case "灯":
                    image.setBackgroundResource(R.mipmap.deng);
                    break;
                case "音响":
                    image.setBackgroundResource(R.mipmap.yinxiang);
                    break;
                case "空调":
                    image.setBackgroundResource(R.mipmap.kongtiao);
                    break;
                case "电视机":
                    image.setBackgroundResource(R.mipmap.dianshiji);
                    break;
            }
        }
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Log.e("状态", "开启");
                    http_Start(item.getId()+"");
                } else {
                    Log.e("状态", "关闭");
                    http_Close(item.getId()+"");
                }
            }
        });

        baseViewHolder.setText(R.id.tv_1, item.getName());
        baseViewHolder.setText(R.id.tv_2, item.getCatgeory());


    }
    EquipmentBeanDao mEquipmentBeanDao;

    //获取UserDao
    private void init() {
        MyApplication myApp = (MyApplication) mActivity.getApplication();
        DaoSession daoSession = myApp.getDaoSession();
        mEquipmentBeanDao = daoSession.getEquipmentBeanDao();
    }


    /**
     * 添加单一设备
     */
    public void http_Start(String driveId) {
        String pathUrl = "http://192.168.10.70:8082/home/sendOn";
        OkHttpUtils.postString()
                .url(pathUrl)
                .content(new Gson().toJson(driveId))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("第一次请求失败",e.getMessage());



                        List<EquipmentBean> all=mEquipmentBeanDao.loadAll();
                        for (int i = 0; i < all.size(); i++) {
                            if (all.get(i).getId()==Long.parseLong(driveId)){
                                EquipmentBean bean=all.get(i);
                                if (bean.getUserTime()==null||bean.getUserTime().length()==0){
                                    bean.setUserTime("1");
                                }else{
                                    int size=Integer.parseInt(bean.getUserTime());
                                    size++;
                                    bean.setUserTime(""+size);
                                    if (size==10){
                                        Toast.makeText(mActivity, "设备号id"+driveId+"已经使用超过10次。请及时检修", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                Log.e("打印改变后的的大小==",bean.getUserTime()+"==");
                                mEquipmentBeanDao.update(bean);
                            }

                        }




                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("第一次请求成功",response);

                    }
                });
    }


    /**
     * 关闭一设备
     */
    public void http_Close(String driveId) {
        String pathUrl = "http://192.168.10.70:8082/home/sendOff";
        OkHttpUtils.postString()
                .url(pathUrl)
                .content(new Gson().toJson(driveId))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("第一次请求失败",e.getMessage());

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("第一次请求成功",response);

                    }
                });
    }


}

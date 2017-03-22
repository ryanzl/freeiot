/*
 * Copyright (c) 2017. Noah Gao <noahgaocn@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.noahgao.freeiot.ui.pages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.XRecyclerView;

import net.noahgao.freeiot.R;
import net.noahgao.freeiot.adapter.devicesAdapter;
import net.noahgao.freeiot.api.ApiClient;
import net.noahgao.freeiot.model.DeviceModel;
import net.noahgao.freeiot.model.ProductSimpleModel;
import net.noahgao.freeiot.ui.DeviceActivity;
import net.noahgao.freeiot.ui.InitDeviceActivity;
import net.noahgao.freeiot.util.Auth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IndexFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IndexFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("unused")
public class IndexFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private XRecyclerView mRecyclerView;
    private devicesAdapter mAdapter;
    private List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>> listData;

    public IndexFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IndexFragment.
     */
    public static IndexFragment newInstance() {
        return new IndexFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String items[]={"激活预置设备","手动引导新设备入网"};
                final int[] whichF = new int[1];
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
                builder.setTitle("请选择新建设备方式"); //设置标题
                builder.setSingleChoiceItems(items,0,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.dismiss();
                        whichF[0] = which;
                    }
                }); // 设置单选选项
                //  设置按钮及其监听器
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (whichF[0]) {
                            case 0:
                                activiteDeviceBuild();
                                break;
                            case 1:
                                startActivity(new Intent(getActivity(), InitDeviceActivity.class));
                        }
                    }
                });
                builder.create().show();
            }
        });
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.devices_view);
        listData = new ArrayList<>();
        mAdapter = new devicesAdapter(listData);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_sample);
        mRecyclerView.addItemDecoration(mRecyclerView.new DividerItemDecoration(dividerDrawable));
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() { getDevices(true); }

            @Override
            public void onLoadMore() {}
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.setOnItemClickListener(new devicesAdapter.OnRecyclerViewItemClickListener(){
            @Override
            public void onItemClick(View view, DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>> data) {
                Intent intent = new Intent(getActivity(), DeviceActivity.class);
                intent.putExtra("id",data.get_id());
                intent.putExtra("name",data.getName());
                startActivity(intent);

            }
        });
        mRecyclerView.setAdapter(mAdapter);
        getDevices(false);
    }

    public void activiteDeviceBuild(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.item_dialog_activite,(ViewGroup) getActivity().findViewById(R.id.activite_dialog));
        final EditText editText = (EditText) dialog.findViewById(R.id.activite_device_id);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());  //先得到构造器
        builder.setTitle("激活设备");
        builder.setView(dialog);
        builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Call<Object> call = ApiClient.API.activiteDevice(editText.getText().toString(), Auth.getUser().get_id(), Auth.getToken());
                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if(response.isSuccessful()) {
                            getDevices(false);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(),"激活失败，请检查！",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) { t.printStackTrace(); }
                });
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void getDevices (final boolean refreshTag) {
        Call<List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>>> call = ApiClient.API.getDevices(Auth.getUser().get_id(),Auth.getToken());
        call.enqueue(new Callback<List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>>>() {
            @Override
            public void onResponse(Call<List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>>> call, Response<List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>>> response) {
                listData.clear();
                listData.addAll(response.body());
                if (mAdapter!=null) mAdapter.notifyDataSetChanged();
                if (mRecyclerView!=null&&refreshTag) mRecyclerView.refreshComplete();
            }

            @Override
            public void onFailure(Call<List<DeviceModel.DeviceMeta.DeviceMetaModel<ProductSimpleModel<String>>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDevices(false);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
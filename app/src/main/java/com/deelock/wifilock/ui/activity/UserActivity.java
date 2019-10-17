package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.UserAdapter;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleAddFprintActivity;
import com.deelock.wifilock.bluetooth.BleAddPwdActivity;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityUserBinding;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserDetail;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.entity.UserManage;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class UserActivity extends Activity {

    ActivityUserBinding binding;
    private List<UserPassword> passwordList;
    private List<UserFPrint> fPrintList;
    private List<UserFPrint> data;
    private UserAdapter adapter;
    private String sdlId;
    private String authId;
    private String name;
    private String headUrl;
    int userRight;
    int isPush;
    boolean isPrintFull;
    boolean isPasswordFull;
    int manager = 0;

    SelectDialog dialog;
    private File photoFile;
    private Bitmap photo;
    private int imageCropSize = 100;
    private boolean isDeleteUser = false;   //是否已确定网络加载完数据可以点击删除用户按钮

    private ArrayList<User> userList = new ArrayList<>();

    private final int PHOTO_REQUEST_TAKE_PHOTO = 1;// 拍照
    private final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private final int PHOTO_REQUEST_CUT = 3;// 裁剪
    private final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 180;

    private int authUid;

    public static final String CACHE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/local_cache";

    private UserDetail detail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        bindActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        doBusiness();
        setEvent();
    }

    private void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user);
    }

    private void doBusiness() {
        int statueHeight = DensityUtil.getStatueBarHeight(this);
        RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) binding.backIb.getLayoutParams();
        lp1.topMargin = statueHeight;
        binding.backIb.setLayoutParams(lp1);

        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) binding.titleTv.getLayoutParams();
        lp2.topMargin = statueHeight;
        binding.titleTv.setLayoutParams(lp2);

        sdlId = getIntent().getStringExtra("sdlId");
        authId = getIntent().getStringExtra("authId");
        userRight = getIntent().getIntExtra("userRight", 0);
        name = getIntent().getStringExtra("name");
        headUrl = getIntent().getStringExtra("headUrl");
        isPush = getIntent().getIntExtra("isPush", 1);
        authUid = getIntent().getIntExtra("authUid", -1);
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        userList = getIntent().getParcelableArrayListExtra("userList");

        if (!authId.equals("11111111111111111111111111111111")) {
            if (!TextUtils.isEmpty(headUrl)) {
                Glide.with(this).load(headUrl).into(binding.headCiv);
            }
        }

        binding.titleTv.setText(name);

        if (!authId.equals("00000000000000000000000000000000") && !authId.equals("11111111111111111111111111111111")
                && userRight < 0) {
            binding.deleteTv.setVisibility(View.VISIBLE);
        }

        passwordList = new ArrayList<>();
        fPrintList = new ArrayList<>();
        data = new ArrayList<>();

        adapter = new UserAdapter(this, userList, passwordList, fPrintList, sdlId, isPush, authId, phoneNumber);
        binding.rv.setAdapter(adapter);

        InitRecyclerView.initLinearLayoutVERTICAL(this, binding.rv);
    }

    private void requestData() {
        if (!isNetworkAvailable()) {
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("authId", authId);
        params.put("sdlId", sdlId);

        String url;
        if ("A003".equals(sdlId.substring(0, 4))) {
            url = RequestUtils.BLE_USER_DETAIL;
        } else {
            url = RequestUtils.USER_LIST_INFO;
        }
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        UserManage m = new Gson().fromJson(content, UserManage.class);
                        passwordList.clear();
                        fPrintList.clear();
                        passwordList.addAll(m.getPasswords());
                        fPrintList.addAll(m.getfPrints());
                        isDeleteUser = true;
                        adapter.notifyDataSetChanged();

                        if (m.getAuthList() == null || m.getAuthList().size() == 0) {
                            adapter.setShowShare(false);
                        } else {
                            adapter.setIsBtopen(m.getAuthList().get(0).getIsBtopen());
                            adapter.setAuthUid(m.getAuthList().get(0).getAuthUid());
                            if ("A003".equals(sdlId.substring(0, 4))) {
                                if (!authId.equals("00000000000000000000000000000000") && !authId.equals("11111111111111111111111111111111")) {
                                    adapter.setShowShare(true);
                                } else {
                                    adapter.setShowShare(false);
                                }
                            } else {
                                adapter.setShowShare(false);
                            }
                        }
                        if (!authId.equals("00000000000000000000000000000000") && !authId.equals("11111111111111111111111111111111")
                                && passwordList.size() == 0 && fPrintList.size() == 0) {
                            binding.deleteTv.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        Map params1 = new HashMap<>();
        params1.put("timestamp", TimeUtil.getTime());
        params1.put("uid", SPUtil.getUid(this));
        params1.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        data.addAll(new Gson().fromJson(content, FPrintList.class).getList());
                        if (data.size() > 36) {
                            isPrintFull = true;
                        }

                        for (UserFPrint u : data) {
                            if (u.getType() == 1) {
                                manager++;
                            }
                        }
                    }
                }
        );

        Map params2 = new HashMap<>();
        params2.put("timestamp", TimeUtil.getTime());
        params2.put("uid", SPUtil.getUid(this));
        params2.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params2).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList list = new Gson().fromJson(content, PasswordList.class);
                        List<UserPassword> up = list.getUserPasswords();
                        if (up.size() > 29) {
                            isPasswordFull = true;
                        }
                    }
                }
        );

        if (authId.equals("11111111111111111111111111111111")) {
            Map params3 = new HashMap();
            params3.put("timestamp", TimeUtil.getTime());
            params3.put("pid", SPUtil.getUid(this));
            params3.put("headUrl", SPUtil.getUid(this));
            RequestUtils.request(RequestUtils.DETAIL_INFO, this, params3).enqueue(
                    new ResponseCallback<BaseResponse>(this) {
                        @Override
                        protected void onSuccess(int code, String content) {
                            super.onSuccess(code, content);
                            detail = new Gson().fromJson(content, UserDetail.class);
                            String url = detail.getHeadUrl();
                            if (!TextUtils.isEmpty(url)) {
                                Glide.with(UserActivity.this).load(detail.getHeadUrl()).error(R.mipmap.mine_head).into(binding.headCiv);
                            }
                        }
                    }
            );
        }

    }

    private void setEvent() {
        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(headUrl)) {
                    deleteUser();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        deleteHead();
                    }
                }).start();
            }
        });

        binding.titleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NickNameDialog dialog = new NickNameDialog(UserActivity.this, R.style.dialog);
                dialog.setEvent(new NickNameDialog.Event() {
                    @Override
                    public void ensure(final String name) {
                        if (!isNetworkAvailable()) {
                            return;
                        }

                        Map params = new HashMap();
                        params.put("timestamp", TimeUtil.getTime());
                        params.put("uid", SPUtil.getUid(UserActivity.this));
                        params.put("sdlId", sdlId);
                        params.put("pid", authId);
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).getName().equals(name)) {
                                Toast.makeText(UserActivity.this, "该用户名已存在", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        params.put("name", name);

                        RequestUtils.request(RequestUtils.UPDATE_USER, UserActivity.this, params).enqueue(
                                new ResponseCallback<BaseResponse>(UserActivity.this) {
                                    @Override
                                    protected void onSuccess(int code, String content) {
                                        super.onSuccess(code, content);
                                        ToastUtil.toastShort(getApplicationContext(), "修改成功");
                                        binding.titleTv.setText(name);
                                    }

                                    @Override
                                    protected void onFailure(int code, String message) {
                                        super.onFailure(code, message);
                                        ToastUtil.toastShort(UserActivity.this, message);
                                    }

                                    @Override
                                    protected void onFinish() {
                                        super.onFinish();
                                    }
                                }
                        );
                    }
                });
                dialog.show();
            }
        });

        binding.headCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(UserActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(UserActivity.this, new String[]{Manifest.permission.CAMERA},
                                REQUEST_CODE_ACCESS_COARSE_LOCATION);
                    } else {
                        dialog = new SelectDialog(UserActivity.this, R.style.dialog);
                        dialog.show();
                    }
                } else {
                    dialog = new SelectDialog(UserActivity.this, R.style.dialog);
                    dialog.show();
                }
            }
        });

        adapter.setEvent(new UserAdapter.Event() {
            @Override
            public void addPrint(int position) {
                if (isPrintFull) {
                    ToastUtil.toastShort(getApplicationContext(), "指纹个数已达到上限");
                    return;
                }
                goToPrintPage();
            }

            @Override
            public void addPassword(int position) {
                if (isPasswordFull) {
                    ToastUtil.toastShort(getApplicationContext(), "密码个数已达到上限");
                    return;
                }
                goToPasswordPage();
            }
        });
    }

    private void goToPasswordPage() {
        if ("A003".equals(sdlId.substring(0, 4))) {
            boolean isBindWifi = SPUtil.getBooleanData(this, sdlId + "wifi");
            boolean isRemote = SPUtil.getBooleanData(this, sdlId + "remote");
            if (!isBindWifi || !isRemote) {
                Intent intent = new Intent(this, BleAddPwdActivity.class);
                intent.putExtra("authId", authId);
                intent.putExtra("deviceId", sdlId);
                intent.putExtra("mac", SPUtil.getStringData(this, sdlId + "mac"));
                intent.putExtra("type", "A1A2");
                startActivity(intent);
            } else {
                if (BleActivity.CanIUseBluetooth) {
                    Intent intent = new Intent(this, BleAddPwdActivity.class);
                    intent.putExtra("authId", authId);
                    intent.putExtra("deviceId", sdlId);
                    intent.putExtra("mac", SPUtil.getStringData(this, sdlId + "mac"));
                    intent.putExtra("type", "A1A2");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(UserActivity.this, PasswordStepActivity.class);
                    intent.putExtra("flag", 4);
                    intent.putExtra("sdlId", sdlId);
                    intent.putExtra("authId", authId);
                    startActivity(intent);
                }
            }
        } else {
            Intent intent = new Intent(UserActivity.this, PasswordStepActivity.class);
            intent.putExtra("flag", 4);
            intent.putExtra("sdlId", sdlId);
            intent.putExtra("authId", authId);
            startActivity(intent);
        }
    }

    private void goToPrintPage() {
        if ("A003".equals(sdlId.substring(0, 4))) {
            Intent intent = new Intent(this, BleAddFprintActivity.class);
            intent.putExtra("authId", authId);
            intent.putExtra("deviceId", sdlId);
            intent.putExtra("mac", SPUtil.getStringData(this, sdlId + "mac"));
            startActivity(intent);
        } else {
            Intent intent = new Intent(UserActivity.this, FPrintStepActivity.class);
            intent.putExtra("flag", 2);
            intent.putExtra("sdlId", sdlId);
            intent.putExtra("authId", authId);
            startActivity(intent);
        }
    }

    private void deleteHead() {
        String head = "";
        if (authId.equals("11111111111111111111111111111111")) {
            head = detail.getHeadUrl();
        } else {
            head = headUrl;
        }
        if (TextUtils.isEmpty(head)) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        String boundary = "----boundary";
        String address = "http://47.96.158.139:8080/file_server/deelock/image_del.json";

        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=uid");
        sb.append("\r\n\r\n");
        sb.append(SPUtil.getUid(this));
        sb.append("\r\n");

        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=token");
        sb.append("\r\n\r\n");
        sb.append(SPUtil.getToken(this));
        sb.append("\r\n");

        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=timestamp");
        sb.append("\r\n\r\n");
        sb.append(TimeUtil.getTime());
        sb.append("\r\n");

        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=imageUrl");
        sb.append("\r\n\r\n");
        sb.append(head);
        sb.append("\r\n\r\n--").append(boundary).append("--\r\n");

        String end = "\r\n--" + boundary + "--\r\n";

        String result = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            System.setProperty("http.keepAlive", "false");

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(sb.toString());
            out.flush();
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
                deleteUser();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteUser() {
        if (!isDeleteUser) {
            Toast.makeText(this, "请等待网络数据加载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordList.size() != 0 || fPrintList.size() != 0) {
            Toast.makeText(this, "请先删除所有指纹密码", Toast.LENGTH_SHORT).show();
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("pid", authId);
        String url;
        if ("A003".equals(sdlId.substring(0, 4))) {
            url = RequestUtils.BLE_DELETE_USER;
            if (adapter != null) {
                int id = adapter.getRefreshAuthUid();
                if (authUid > 0) {
                    params.put("authUid", authUid);
                } else if (id != -1 && id != 0) {
                    params.put("authUid", id);
                }
            }
            params.put("devId", sdlId);
        } else {
            params.put("sdlId", sdlId);
            url = RequestUtils.DELETE_USER;
        }
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(UserActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        ToastUtil.toastShort(getApplicationContext(), "连接异常，请检查网络情况！");
        return false;
    }

    class SelectDialog extends Dialog {

        SelectDialog(Context context, int themeResId) {
            super(context, themeResId);
            Window window = getWindow();
            window.setGravity(Gravity.BOTTOM);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_select_head);
            initView();
        }

        private void initView() {
            findViewById(R.id.take_photo_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto();
                }
            });

            findViewById(R.id.select_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                }
            });

            findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    /**
     * 拍照获取图片
     */
    private void takePhoto() {
        //执行拍照前，应该先判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
            // 获取 SD 卡根目录
            String saveDir = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
            SimpleDateFormat t = new SimpleDateFormat("yyyyMMdd_ssSSS");
            String filename = "IMG_" + (t.format(new Date())) + ".jpg";
            photoFile = new File(saveDir, filename);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(intent, PHOTO_REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(getApplicationContext(), "内存卡不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //头像修改结果
        //操作成功为-1，取消操作为0
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKE_PHOTO:
                    startPhotoZoom(Uri.fromFile(photoFile), imageCropSize);
                    break;
                case PHOTO_REQUEST_GALLERY:
                    if (data != null) {
                        startPhotoZoom(data.getData(), imageCropSize);
                    }
                    break;
                case PHOTO_REQUEST_CUT:
                    if (data != null)
                        getCropBitmap(data);
                    break;
            }
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 将进行剪裁后的图片显示到UI界面上
     *
     * @param picData
     */
    private void getCropBitmap(Intent picData) {
        Bundle bundle = picData.getExtras();
        if (bundle != null) {
            if (photo != null && !photo.isRecycled()) {
                photo.recycle();
                photo = null;
            }
            photo = bundle.getParcelable("data");
            binding.headCiv.setImageBitmap(photo);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (authId.equals("11111111111111111111111111111111")) {
                        synchronized (detail) {
                            String localUrl = detail.getHeadUrl();
                            if (localUrl == null) {
                                setBitmapToLocal("123", photo,
                                        "http://47.96.158.139:8080/file_server/deelock/image_add.json", null);
                            } else {
                                setBitmapToLocal("123", photo,
                                        "http://47.96.158.139:8080/file_server/deelock/image_update.json", localUrl);
                            }
                        }
                    } else {
                        if (TextUtils.isEmpty(headUrl)) {
                            setBitmapToLocal(sdlId + authId, photo, "http://47.96.158.139:8080/file_server/deelock/image_add.json", null);
                        } else {
                            setBitmapToLocal(sdlId + authId, photo, "http://47.96.158.139:8080/file_server/deelock/image_update.json", headUrl);
                        }
                    }
                }
            }).start();

//            showProgressDialog(this, "头像上传中…");
            //调起上传请求
//            upLoadData();
        }
    }

    /**
     * @param url1    本地文件名称
     * @param bitmap  头像
     * @param address
     * @param old
     */
    private void setBitmapToLocal(String url1, Bitmap bitmap, String address, String old) {
        try {

            File file = new File(CACHE_PATH, url1);

            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {// 如果文件夹不存在, 创建文件夹
                parentFile.mkdirs();
            }

            // 将图片保存在本地
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            String boundary = "----boundary";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data2 = baos.toByteArray();

            StringBuilder sb = new StringBuilder();

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=uid");
            sb.append("\r\n\r\n");
            sb.append(SPUtil.getUid(this));
            sb.append("\r\n");

            if (old != null) {
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=oldUrl");
                sb.append("\r\n\r\n");
                sb.append(old);
                sb.append("\r\n");
            }

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=token");
            sb.append("\r\n\r\n");
            sb.append(SPUtil.getToken(this));
            sb.append("\r\n");

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=timestamp");
            sb.append("\r\n\r\n");
            sb.append(TimeUtil.getTime());
            sb.append("\r\n");

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=file; filename=file.jpg");
//            sb.append(boundary).append("\r\n");
//            sb.append("Content-Type: image/jpeg");
            sb.append("\r\n\r\n");

            String end = "\r\n--" + boundary + "--\r\n";

            byte[] data = sb.toString().getBytes();

            String result = "";
            BufferedReader reader = null;
            try {
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(sb.toString());
                out.write(data2);
                out.writeBytes(end);
                out.flush();
                if (conn.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    result = reader.readLine();
                    BaseResponse response = new Gson().fromJson(result, BaseResponse.class);
                    if (response.code == 1) {
                        String content = response.content.toString();
                        if (content.indexOf("imgUrl") != -1) {
                            String u = content.substring(8, content.length() - 1);
                            updateUrl(u);
                        }
                    }
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUrl(final String url) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("pid", authId);
        params.put("headUrl", url);
        String address = RequestUtils.UPDATE_USER;
        if (authId.equals("11111111111111111111111111111111")) {
            params.put("timestamp", TimeUtil.getTime());
            params.put("pid", SPUtil.getUid(this));
            params.put("headUrl", url);
            address = RequestUtils.UPDATE_INFO;
        } else {
            params.put("timestamp", TimeUtil.getTime());
            params.put("uid", SPUtil.getUid(this));
            params.put("sdlId", sdlId);
            params.put("pid", authId);
            params.put("headUrl", url);
        }
        RequestUtils.request(address, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(UserActivity.this, "修改成功");
                        if (authId.equals("11111111111111111111111111111111")) {
                            detail.setHeadUrl(url);
                            Constants.headUrl = url;
                            SPUtil.setHeadUrl(UserActivity.this, url);
                        } else {
                            headUrl = url;
                        }
                        Glide.with(UserActivity.this).load(url).into(binding.headCiv);
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                    }
                }
        );
    }

    /**
     * 裁剪圖片
     *
     * @param uri
     * @param size
     */
    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dialog = new SelectDialog(UserActivity.this, R.style.dialog);
                    dialog.show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestData();
    }
}

package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityCenterBinding;
import com.deelock.wifilock.entity.UserDetail;
import com.deelock.wifilock.event.CenterEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.ServerFactory;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.deelock.wifilock.utils.LocalCacheUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class CenterActivity extends BaseActivity {

    private ActivityCenterBinding binding;

    public static final String CACHE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/local_cache";

    /**
     * 图片裁剪大小
     */
    private int imageCropSize = 100;
    /**
     * 获取到的图片路径
     */
    private File photoFile;
    private Bitmap photo;
    private Bitmap bitmap;

    SelectDialog dialog;

    private final int PHOTO_REQUEST_TAKE_PHOTO = 1;// 拍照
    private final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private final int PHOTO_REQUEST_CUT = 3;// 裁剪
    private final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 160;

    private UserDetail detail;

    private Uri uritempFile;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_center);
    }

    @Override
    protected void doBusiness() {
        binding.nickNameTv.setText(SPUtil.getNickName(this));
    }

    @Override
    protected void requestData() {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(CenterActivity.this));
        params.put("headUrl", SPUtil.getUid(CenterActivity.this));
        RequestUtils.request(RequestUtils.DETAIL_INFO, CenterActivity.this, params).enqueue(
                new ResponseCallback<BaseResponse>(CenterActivity.this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        detail = new Gson().fromJson(content, UserDetail.class);
                        Glide.with(CenterActivity.this).load(detail.getHeadUrl()).error(R.mipmap.user_head).into(binding.headCiv);
                    }
                }
        );
    }

    @Override
    protected void setEvent() {
        binding.setEvent(new CenterEvent() {
            @Override
            public void leftIcon() {
                finish();
            }

            @Override
            public void updateHead() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(CenterActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CenterActivity.this, new String[]{Manifest.permission.CAMERA},
                                REQUEST_CODE_ACCESS_COARSE_LOCATION);
                    } else {
                        dialog = new SelectDialog(CenterActivity.this, R.style.dialog);
                        dialog.show();
                    }
                } else {
                    dialog = new SelectDialog(CenterActivity.this, R.style.dialog);
                    dialog.show();
                }
            }

            @Override
            public void updateNickName() {
                final NickNameDialog dialog = new NickNameDialog(CenterActivity.this, R.style.dialog);
                dialog.setEvent(new NickNameDialog.Event() {
                    @Override
                    public void ensure(final String name) {
                        if (!isNetworkAvailable()) {
                            return;
                        }

                        Map params = new HashMap();
                        params.put("timestamp", TimeUtil.getTime());
                        params.put("pid", SPUtil.getUid(CenterActivity.this));
                        params.put("nickName", name);
                        RequestUtils.request(RequestUtils.UPDATE_INFO, CenterActivity.this, params).enqueue(
                                new ResponseCallback<BaseResponse>(CenterActivity.this) {
                                    @Override
                                    protected void onSuccess(int code, String content) {
                                        super.onSuccess(code, content);
                                        ToastUtil.toastShort(getApplicationContext(), "修改成功");
                                        binding.nickNameTv.setText(name);
                                        sendName(name);
                                        SPUtil.setNickName(CenterActivity.this, name);
                                    }

                                    @Override
                                    protected void onFailure(int code, String message) {
                                        super.onFailure(code, message);
                                        ToastUtil.toastShort(CenterActivity.this, message);
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

            @Override
            public void updatePassword() {
                openActivityForMoment(ResetPasswordActivity.class, null);
            }

            @Override
            public void updateGesture() {
                openActivityForMoment(VerifyPasswordActivity.class, null);
            }

            @Override
            public void logout() {
                if (!clickAble) {
                    return;
                }
                clickAble = false;
                logOut();
            }
        });
    }

    private void sendName(String name) {
        Intent intent = new Intent("com.deelock.wifilock.name");
        intent.putExtra("name", name);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
            Toast.makeText(this, "内存卡不存在", Toast.LENGTH_SHORT).show();
        }
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
                    synchronized (detail) {
                        if (detail.getHeadUrl() == null) {
                            setBitmapToLocal("123", photo,
                                    "http://47.96.158.139:8080/file_server/deelock/image_add.json", null);
                        } else {
                            setBitmapToLocal("123", photo,
                                    "http://47.96.158.139:8080/file_server/deelock/image_update.json", detail.getHeadUrl());
                        }
                    }
                }
            }).start();

//            showProgressDialog(this, "头像上传中…");
            //调起上传请求
//            upLoadData();
        }
    }

    private void setBitmapToLocal(String url1, Bitmap bitmap, String address, String old) {
        try {

            File file = new File(CACHE_PATH, url1);

            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {// 如果文件夹不存在, 创建文件夹
                parentFile.mkdirs();
            }

            if (file.exists()) {
                file.delete();
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
        params.put("pid", SPUtil.getUid(this));
        params.put("headUrl", url);
        RequestUtils.request(RequestUtils.UPDATE_INFO, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(CenterActivity.this, "修改成功");
                        detail.setHeadUrl(url);
                        Constants.headUrl = url;
                        SPUtil.setHeadUrl(CenterActivity.this, url);
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                    }
                }
        );
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
                    if (data != null) {
                        getCropBitmap(data);
                    }

                    break;
            }
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dialog = new SelectDialog(CenterActivity.this, R.style.dialog);
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

    private void logOut() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(this));
        params.put("token", SPUtil.getToken(this));
        RequestUtils.request(RequestUtils.LOGOUT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        BluetoothUtil.closeBluetooth();
                        BluetoothUtil.stopBluetooth();
                        BluetoothUtil.clearInfo();
                        SPUtil.logout(CenterActivity.this);
                        Constants.headUrl = null;
                        ToastUtil.toastShort(getApplicationContext(), "已成功退出");
                        Intent intent = new Intent(CenterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        clickAble = true;
                    }
                }
        );
    }
}

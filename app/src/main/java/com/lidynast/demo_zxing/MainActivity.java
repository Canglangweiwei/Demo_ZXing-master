package com.lidynast.demo_zxing;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lidynast.demo_zxing.rxbus.RxBus;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import rx.Observable;
import rx.functions.Action1;

/**
 * 二维码生成
 * 生成的二维码在 内存卡的Lidynast/pic/下
 *
 * @author LJC
 */
@SuppressWarnings("ALL")
public class MainActivity extends Activity {

    private final static String TAG = "Lidyanst_二维码生成";

    static String mPath = Environment.getExternalStorageDirectory() + "/lidynast/pic/";
    static String mFileName = java.lang.System.currentTimeMillis() + ".png";
    // 前景色
    private int FOREGROUND_COLOR = 0xff000000;
    // 背景色
    private int BACKGROUND_COLOR = 0xffffffff;

    // 界面组件
    private ImageView imgShow;
    private ImageView iconShow;
    private EditText et;
    private Button btn_next;

    private final String IMAGE_TYPE = "image/*";

    private final int IMAGE_CODE = 0; // 这里的IMAGE_CODE是自己任意定义的
    // 图片宽度的一般
    private static final int IMAGE_HALFWIDTH = 20;

    // 插入到二维码里面的图片对象
    private Bitmap mIcon;
    // 需要插图图片的大小 这里设定为40*40
    private int[] mPixels = new int[2 * IMAGE_HALFWIDTH * 2 * IMAGE_HALFWIDTH];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);
        imgShow = (ImageView) findViewById(R.id.img);
        iconShow = (ImageView) findViewById(R.id.icon);
        et = (EditText) findViewById(R.id.et);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, WeChatPayActivity.class));
            }
        });

        // 注册观察者
        registerObservable();
    }

    private Observable<String> observable;// 观察者

    /**
     * 注册观察者
     */
    private void registerObservable() {
        observable = RxBus.get().register("url", String.class);
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                if (TextUtils.isEmpty(s)) {
                    return;
                }
                et.setText(s);
            }
        });
    }

    /**
     * 按钮点击事件-设置前景色
     *
     * @param v
     */
    public void onQJS(View v) {
        ColorPickerDialog dialog = new ColorPickerDialog(this, "设置前景色",
                new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        FOREGROUND_COLOR = color;
                    }
                });
        dialog.show();
    }

    /**
     * 按钮点击事件-设置背景色
     *
     * @param v
     */
    public void onBJS(View v) {
        ColorPickerDialog dialog = new ColorPickerDialog(this, "设置背景色",
                new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        BACKGROUND_COLOR = color;
                    }
                });
        dialog.show();
    }

    /**
     * 按钮点击事件-切换中间图片
     *
     * @param v
     */
    public void onQHI(View v) {
        setImage();
    }

    /**
     * 按钮点击事件-生成二维码
     *
     * @param v
     */
    public void onSC(View v) {
        String content = et.getText().toString();
        if (null != content && !content.equals("")) {
            try {
                Bitmap bitmap = cretaeBitmap(new String(content.getBytes(), "UTF-8"), mIcon);
                // 将二维码写入内存卡
                writeBitmap(bitmap);
                imgShow.setImageBitmap(bitmap);
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(this, "出错！", Toast.LENGTH_SHORT).show();
            } catch (WriterException e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(this, "出错！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "请输入信息！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            // 此处的 RESULT_OK 是系统自定义得一个常量
            return;
        }
        ContentResolver resolver = getContentResolver();
        // 此处的用于判断接收的Activity是不是你想要的那个
        if (requestCode == IMAGE_CODE) {
            try {
                Uri originalUri = data.getData(); // 获得图片的uri
                mIcon = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                iconShow.setImageBitmap(mIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取用户选择的图片
     */
    private void setImage() {
        // 使用intent调用系统提供的相册功能，使用startActivityForResult是为了获取用户选择的图片
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType(IMAGE_TYPE);
        startActivityForResult(getAlbum, IMAGE_CODE);
    }

    /**
     * 生成二维码 中间插入小图片
     *
     * @param str 内容
     * @return Bitmap
     * @throws WriterException
     */
    public Bitmap cretaeBitmap(String str, Bitmap icon) throws WriterException {
        // 缩放一个40*40的图片
        icon = Untilly.zoomBitmap(icon, IMAGE_HALFWIDTH);
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, 480, 480, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                        && y > halfH - IMAGE_HALFWIDTH
                        && y < halfH + IMAGE_HALFWIDTH) {
                    pixels[y * width + x] = icon.getPixel(x - halfW
                            + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                } else {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = FOREGROUND_COLOR;
                    } else { // 无信息设置像素点为白色
                        pixels[y * width + x] = BACKGROUND_COLOR;
                    }
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 保存二维码
     */
    public static boolean writeBitmap(Bitmap b) {
        ByteArrayOutputStream by = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, by);
        byte[] stream = by.toByteArray();
        return Untilly.writeToSdcard(stream, mPath, mFileName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister("url", observable);
    }
}
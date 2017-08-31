package com.lidynast.demo_zxing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lidynast.demo_zxing.rxbus.RxBus;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信支付
 *
 * @author cheng
 */
public class WeChatPayActivity extends Activity {

    private TextView tv_result;
    private Button btn_result;

    /**
     * 二维码url
     */
    private String mUrl;
    /**
     * 时间戳
     */
    private long mTimestamp;
    /**
     * 随机字符串
     */
    private String mRandom;
    /**
     * 签名
     */
    private String sign;
    /**
     * 必须参数(这个建议从服务器上获取)
     * (appid----微信分配的公众账号ID,mch_id---微信支付分配的商户号,product_id---商户定义的商品id或者订单号,)
     */
    private String appid = "wxe3ab6428d32294f4";
    private String mch_id = "1389778302";
    private String product_id = "123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_we_chat_pay);
        initView();
        init();
    }

    private void initView() {
        tv_result = (TextView) findViewById(R.id.tv_result);
        btn_result = (Button) findViewById(R.id.btn_result);
        btn_result.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String result = getUrl();
                tv_result.setText(result);
                RxBus.get().post("url", result);
            }
        });
    }

    private void init() {
        TimeUtil mTimeUtil = new TimeUtil();
        // 时间戳--十位
        mTimestamp = mTimeUtil.getTimestamp();
        // 随机数
        mRandom = getRandomString();
        sign = createSign();
    }

    /**
     * 生成二维码url
     */
    private String getUrl() {
        // weixin：//wxpay/bizpayurl?sign=XXXXX&appid=XXXXX&mch_id=XXXXX&product_id=XXXXXX&time_stamp=XXXXXX&nonce_str=XXXXX
        mUrl = "weixin://wxpay/bizpayurl?" + "sign=" + sign + "&appid=" + appid
                + "&mch_id=" + mch_id + "&product_id=" + product_id
                + "&time_stamp=" + mTimestamp + "&nonce_str=" + mRandom;
        System.out.println("======mUrl=========" + mUrl);
        return mUrl;
    }

    /**
     * 生成随机数
     */
    private String getRandomString() {
        String randomNumber = "";
        // 生成纯数字随机数(自己任选一种)
        // int random1 = (int) (Math.random() * 1000000000);
        // randomNumber = Integer.toString(random1);

        // 生成数字和字母结合的随机字符串(自己任选一种)
        StringBuffer buffer = new StringBuffer(
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        int range = buffer.length();
        for (int i = 0; i < 32; i++) {
            sb.append(buffer.charAt(random.nextInt(range)));
        }
        randomNumber = sb.toString();
        return randomNumber;
    }

    private String createSign() {
        // 第一步 参数名ASCII码从小到大排序（字典序）
        SortedMap<String, String> packageParams = new TreeMap<>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", mRandom);
        packageParams.put("product_id", product_id);
        packageParams.put("time_stamp", mTimestamp + "");
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();// 字典序
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            // 为空不参与签名、参数名区分大小写
            if (null != v && !"".equals(v) && !"sign".equals(k)
                    && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }

        // 第二步
        // 拼接key，key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置(32位)
        sb.append("key=" + "wuyetuijian123456789012345678901");
        sign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();// MD5加密
        return sign;
    }
}

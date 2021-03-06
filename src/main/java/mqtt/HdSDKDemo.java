package mqtt;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.math.random.RandomAdaptor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mqtt.HdSDKDemo.*;

public class HdSDKDemo {

    /**
     * 需要配置的要素
     */
    public static final String PRODUCT_KEY = "db2f197fd13641eaa46e4ec484c890d3";
    public static final String DEVICE_KEY = "8b76424e41d74920bd7e5b92a56ee339";

    public static final String FUNCTION_INVOKE_TOPIC = "/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/function/invoke";
    public static final String SDK_UPGRADE_TOPIC = "/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/sdk/upgrade";

    private static MqttClient client = null;

    private HdSDKDemo() {
        // empty constructor
    }

    public static MqttClient getInstance() {
        String broker = "tcp://172.16.10.2:7231";
        if (null == client) {
            synchronized (HdSDKDemo.class) {
                if (null == client) {
                    MemoryPersistence persistence = new MemoryPersistence();
                    try {
                        client = new MqttClient(broker, DEVICE_KEY, persistence);
                        MqttConnectOptions connOpts = new MqttConnectOptions();
                        connOpts.setUserName("admin");
                        connOpts.setPassword("ok".toCharArray());
                        connOpts.setCleanSession(false);
                        connOpts.setKeepAliveInterval(25);
                        client.setCallback(new OnMessageCallback());
                        System.out.println("Connecting to broker: " + broker);
                        client.connect(connOpts);
                        client.subscribe(FUNCTION_INVOKE_TOPIC);
                        client.subscribe(SDK_UPGRADE_TOPIC);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Connected");
                }
            }
        }
        return client;
    }

    public static void main(String[] args) {
        final JSONObject content = new JSONObject();
        int qos = 0;

        try {
            MqttClient mqttClient = getInstance();
            // 心跳线程
            ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                    new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
            executorService.scheduleAtFixedRate(() -> {
                //发送心跳
                System.out.println("发送心跳...");
                try {
                    String sdkVersion = "1.0.1";
                    MqttMessage message = new MqttMessage(sdkVersion.getBytes());
                    mqttClient.publish("/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/heart", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }, 5, 15, TimeUnit.SECONDS);

            // 发送设备数据
            executorService.scheduleAtFixedRate(() -> {
                System.out.println("上报设备数据...");
                Random random = new Random();
                content.put("temperature",  (float)Math.random());
                content.put("speed", random.nextInt(10));
                content.put("engineSpeed", random.nextInt(10));

                //content.put("oilPressure",  random.nextInt(10));
                //content.put("engineOliTem", (int)(Math.random()*100)+1);
                MqttMessage message = new MqttMessage(content.toJSONString().getBytes());
                message.setQos(qos);
                System.out.println("Publishing message: " + content.toJSONString());
                try {
                    mqttClient.publish("/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/properties/report", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                System.out.println("Message published");
            }, 1, 50, TimeUnit.SECONDS);
        } catch (Exception me) {
            me.printStackTrace();
        }
    }
}

class OnMessageCallback implements MqttCallback {


    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("连接断开");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("接收消息主题:" + topic);
        System.out.println("接收消息Qos:" + message.getQos());
        String messageStr = new String(message.getPayload());
        System.out.println("接收消息内容:" + messageStr);
        JSONObject messageObj = JSONObject.parseObject(messageStr);
        MqttClient mqttClient = HdSDKDemo.getInstance();
        if (topic.equals(FUNCTION_INVOKE_TOPIC)) {
            //会话ID
            String dialogId = messageObj.get("dialogId").toString();
            JSONObject replyObj = new JSONObject();
            replyObj.put("dialogId", dialogId);
            replyObj.put("status", "success");
            MqttMessage replyMsg = new MqttMessage(replyObj.toJSONString().getBytes());
            mqttClient.publish("/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/function/invoke/reply", replyMsg);
        } else if (topic.equals(SDK_UPGRADE_TOPIC)) {
            System.out.println("收到设备升级指令");
            String url = messageObj.get("url").toString();
            String version = messageObj.get("version").toString();
            System.out.println("uprade, url:" + url + ", version:" + version);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("发送" + token.isComplete());
    }
}

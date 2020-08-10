package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mqtt.HdSDKDemo.DEVICE_KEY;
import static mqtt.HdSDKDemo.PRODUCT_KEY;

public class HdSDKDemo {

    //需要配置的三个要素
    public static final String PRODUCT_KEY = "ppp";
    public static final String DEVICE_KEY = "ddd";
    public static final String CLIENT_ID = "sjkfajkl3kjljkljklfaskljdf";

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
                        client = new MqttClient(broker, CLIENT_ID, persistence);
                        MqttConnectOptions connOpts = new MqttConnectOptions();
                        connOpts.setUserName("admin");
                        connOpts.setPassword("ok".toCharArray());
                        connOpts.setCleanSession(false);
                        connOpts.setKeepAliveInterval(10);
                        client.setCallback(new OnMessageCallback());
                        System.out.println("Connecting to broker: " + broker);
                        client.connect(connOpts);
                        client.subscribe("/+/+/function/invoke");
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
        String content = "{\"temperature\":\"38.2\"}";
        int qos = 2;

        try {
            MqttClient mqttClient = getInstance();
            System.out.println("Publishing message: " + content);
            // 消息发布所需参数
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            while (true) {
                mqttClient.publish("/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/properties/report", message);
                System.out.println("Message published");
                Thread.sleep(10000);
                int i = 0;
                if (i++ == 1000) {
                    break;
                }
            }
            System.out.println("Disconnected");
            mqttClient.disconnect();
            mqttClient.close();
        } catch (Exception me) {
            me.printStackTrace();
        }
    }
}

class OnMessageCallback implements MqttCallback {


    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("连接断开，可以做重连");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("接收消息主题:" + topic);
        System.out.println("接收消息Qos:" + message.getQos());
        System.out.println("接收消息内容:" + new String(message.getPayload()));
        MqttClient mqttClient = HdSDKDemo.getInstance();
        String invokeRegex = "/.+/.+/function/invoke";
        Pattern pattern = Pattern.compile(invokeRegex);
        Matcher matcher = pattern.matcher(topic);
        boolean isFind = matcher.find();
        if (isFind) {
            String reply = "reply";
            MqttMessage replyMsg = new MqttMessage(reply.getBytes());
            mqttClient.publish("/" + PRODUCT_KEY + "/" + DEVICE_KEY + "/function/invoke/reply", replyMsg);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}

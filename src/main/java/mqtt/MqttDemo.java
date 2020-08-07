package mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttDemo {

    //需要配置的三个要素
    public static final String PRODUCT_KEY = "ppp";
    public static final String DEVICE_KEY = "ddd";
    public static final String CLIENT_ID = "sjkfajkl3kjljkljklfaskljdf";

    private static MqttClient client = null;

    public static MqttClient getInstance() {
        String broker = "tcp://172.16.10.2:7231";
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Connected");
        }
        return client;
    }

    public static void main(String[] args) {
        String content = "{\"temperature\":\"38.2\"}";
        int qos = 2;

        try {
            MqttClient mqttClient = getInstance();
            System.out.println("Publishing message: " + content);
            // 订阅
            mqttClient.subscribe("/+/+/function/invoke");

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
            mqttClient.disconnect();
            System.out.println("Disconnected");
            mqttClient.close();
        } catch (Exception me) {
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}

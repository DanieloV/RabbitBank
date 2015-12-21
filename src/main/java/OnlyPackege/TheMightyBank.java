/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OnlyPackege;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Daniel
 */
public class TheMightyBank {

    private final static String EXCHANGE_NAME = "local.bank";

    public static void main(String[] args) throws Exception {

        JSONParser jsonParster = new JSONParser();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel recvChannel = connection.createChannel();

        recvChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String queueName = recvChannel.queueDeclare().getQueue();
        recvChannel.queueBind(queueName, EXCHANGE_NAME, "");
        
        QueueingConsumer consumer = new QueueingConsumer(recvChannel);
        recvChannel.basicConsume(queueName, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String responseQueue = delivery.getProperties().getReplyTo();

            Channel sendChannel = connection.createChannel();
            sendChannel.queueDeclare(responseQueue, false, false, false, null);
            
            String message = new String(delivery.getBody());
            JSONObject recvObj = (JSONObject)jsonParster.parse(message);
       
            
            //to be deleted after testing
            System.out.println(" [x] Received '" + message + "'");
            
            JSONObject obj = new JSONObject();
            obj.put("ssn", recvObj.get("ssn"));
            obj.put("interestRate", Math.random() * 10);

            sendChannel.basicPublish("", responseQueue, null, obj.toJSONString().getBytes());
        }
    }
}

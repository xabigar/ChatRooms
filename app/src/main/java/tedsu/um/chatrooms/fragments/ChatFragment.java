package tedsu.um.chatrooms.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tedsu.um.chatrooms.MainActivity;
import tedsu.um.chatrooms.R;
import tedsu.um.chatrooms.messages.events.LocationChangeEvent;


public class ChatFragment extends Fragment {
    private BlockingDeque<String> queue;
    private ConnectionFactory factory;
    private String senderRoutingKey, receiverRoutingKey;
    private Handler incomingMessageHandler;
    private Thread senderThread, receiverThread;
    private OnFragmentInteractionListener mListener;
    private ScrollView scrollView;
    private LinearLayout lineas;
    private EditText inputText;
    private Button sendButton;
    private Connection connectionSender, connectionReceiver;
    private Channel chSender, chReceiver;
    private String queueName;



    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment_view = inflater.inflate(R.layout.fragment_chat, container, false);

        scrollView = fragment_view.findViewById(R.id.scroll);
        lineas = fragment_view.findViewById(R.id.lineas);
        inputText = fragment_view.findViewById(R.id.editText);
        sendButton = fragment_view.findViewById(R.id.sendButton);
        senderRoutingKey = "informatica.1.dibulibu";
        receiverRoutingKey = "informatica.*.*";
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = inputText.getText();
                String newText = text.toString();
                String trim = rightTrim(newText);
                if(!trim.isEmpty()) {
                    addLineInScroll("Send: " + inputText.getText(), senderRoutingKey);
                    sendMessage(inputText.getText().toString());
                    inputText.setText("");
                }
            }
        });
        queue = new LinkedBlockingDeque<>();

        factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(false);
        try {
            factory.setHost("155.54.204.34");
            factory.setUsername("test");
            factory.setPassword("tedsu2019");
            factory.setPort(5672);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("sender","[excepcion] uri");
        }

        senderThread = new Thread(() -> {

            while(true) {
                try {
                    connectionSender = factory.newConnection();
                    chSender = connectionSender.createChannel();
                    chSender.confirmSelect();
                    chSender.exchangeDeclare("chatService", BuiltinExchangeType.TOPIC,true);
                    while (true) {
                        String message = queue.takeFirst();
                        try{
                            chSender.basicPublish("chatService", senderRoutingKey, null, message.getBytes());
                            Log.d("sender", "[s] " + message);
                            chSender.waitForConfirmsOrDie();
                        } catch (Exception e){
                            Log.d("sender","[f] " + message);
                            queue.putFirst(message);
                            throw e;
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.d("sender", "Connection broken: " + e.toString());
                    try {
                        Thread.sleep(5000); //sleep and then try again
                    } catch (InterruptedException e1) {
                        break;
                    }
                }
            }
        });

        senderThread.start();

        /*incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                String routing = msg.getData().getString("routing");
                addLineInScroll("Receive: " + message, routing);
            }
        };
        subscribe(incomingMessageHandler);*/

        return fragment_view;
    }

    @Subscribe (threadMode=ThreadMode.MAIN)
    public void retriveRoutingKeys (LocationChangeEvent event) throws IOException {
        if (!senderRoutingKey.equals(event.getS())) {
            senderRoutingKey = event.getS();
        }
        /*if (!receiverRoutingKey.equals(event.getR())) {
            String oldReceiverRoutingKey = receiverRoutingKey;
            receiverRoutingKey = event.getR();
            Log.d("receiver", receiverRoutingKey);
            try {
                chReceiver.queueUnbind(queueName, "chatService", oldReceiverRoutingKey);
                chReceiver.close();
                chReceiver = connectionReceiver.createChannel();
                chReceiver.basicQos(1);
                queueName = chReceiver.queueDeclare().getQueue();
                chReceiver.queueBind(queueName, "chatService", receiverRoutingKey);
            }catch(Exception e){
                e.printStackTrace();
            }
        }*/
    }

    private void addLineInScroll (String line, String routingKey) {
        TextView newTextView = new TextView(getActivity());
        newTextView.setText("["+routingKey+"] "+line);
        lineas.addView(newTextView);


        //scrollView.smoothScrollTo(0, scrollView.getHeight());

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100L);

    }

    public boolean sendMessage(String message){
        try{
            queue.putLast(message);
            // Toast.makeText(getActivity(), senderRoutingKey, Toast.LENGTH_SHORT).show();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /*private void subscribe(final Handler handler)  {

        receiverThread = new Thread(() -> {

            connectionReceiver = null;
            try {
                connectionReceiver = factory.newConnection();
                chReceiver = connectionReceiver.createChannel();
                chReceiver.basicQos(1);
                queueName = chReceiver.queueDeclare().getQueue();
                chReceiver.queueBind(queueName, "chatService", receiverRoutingKey);
                DefaultConsumer consumer = new DefaultConsumer(chReceiver) {
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body) {
                        String routingKey = envelope.getRoutingKey();
                        String contentType = properties.getContentType();
                        long deliveryTag = envelope.getDeliveryTag();

                        String message = new String(body);
                        Log.d("receiver", "[r] " + message);
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", message);
                        bundle.putString("routing", routingKey);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }


                };
                String consumerTag = chReceiver.basicConsume(queueName, true, consumer);
                Log.d("receiver", "Consumer tag: "+consumerTag);
            } catch (Exception e) {
                Log.d("receiver", "exception while subscribing:" + e.toString());
            }
        });
        receiverThread.start();
    }*/

    public static String rightTrim(String str) {
        return str.replaceAll("\\s+$","");
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

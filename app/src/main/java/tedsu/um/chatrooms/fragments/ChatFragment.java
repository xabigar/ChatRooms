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
import java.util.Calendar;
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
        //Default keys if there not exits any location.
        senderRoutingKey = "*.*.*";
        receiverRoutingKey = "*.*.*";
        //Event when the button is clicked, it sends a message.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = inputText.getText();
                String newText = text.toString();
                //Remove blank spaces from the right
                String trim = rightTrim(newText);
                // Check if the text is not empty
                if(!trim.isEmpty()) {
                    addLineInScroll("Send: " + inputText.getText(), senderRoutingKey, "");
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

        //Event which recives the incoming messages
        incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                String routing = msg.getData().getString("routing");
                String time = msg.getData().getString("time");
                addLineInScroll("Receive: " + message, routing, time);
            }
        };
        subscribe(incomingMessageHandler);

        return fragment_view;
    }

    //When the location is changed this method is call and receives the news routingKeys.
    @Subscribe (threadMode=ThreadMode.MAIN)
    public void retriveRoutingKeys (LocationChangeEvent event) throws IOException {
        //If the new sender routing key is different, the old one is changed
        if (!senderRoutingKey.equals(event.getSenderRoutingKey())) {
            senderRoutingKey = event.getSenderRoutingKey();
        }
        //If the new receiver routing key is different, the old one is changed and the binding is modified with the new routingKey
        if (!receiverRoutingKey.equals(event.getReceiverRoutingKey())) {
            String oldReceiverRoutingKey = receiverRoutingKey;
            receiverRoutingKey = event.getReceiverRoutingKey();
           try {
                // The old queue binding is removed
                chReceiver.queueUnbind(queueName, "chatService", oldReceiverRoutingKey);
                // Set the new queue binding
                chReceiver.queueBind(queueName, "chatService", receiverRoutingKey);
           }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // Prints the messages in the Scroll
    private void addLineInScroll (String line, String routingKey, String time) {
        TextView newTextView = new TextView(getActivity());
        newTextView.setText("["+routingKey+" "+ time +"] "+line);
        lineas.addView(newTextView);

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100L);

    }

    // Puts the message in the queue to be published
    public boolean sendMessage(String message){
        try{
            queue.putLast(getTime()+"."+message);
            // Toast.makeText(getActivity(), senderRoutingKey, Toast.LENGTH_SHORT).show();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //It creates the connection between the broker and the consumer
    private void subscribe(final Handler handler)  {
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
                        int idx = message.indexOf('.');
                        String time = message.substring(0, idx);
                        String messagePart = message.substring(idx + 1, message.length());
                        bundle.putString("msg", messagePart);
                        bundle.putString("routing", routingKey);
                        bundle.putString("time", time);
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
    }

    // Giving a string removes the blank spaces from the right
    public static String rightTrim(String str) {
        return str.replaceAll("\\s+$","");
    }

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        int hour24hrs = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return  hour24hrs + ":" + minutes +":"+ seconds;
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

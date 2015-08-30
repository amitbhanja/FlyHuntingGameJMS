package com.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;



public class Consumer extends JFrame implements ActionListener {
	
	JButton startButton;
	JLabel userNameLabel;
	JTextField userNameText;
	JPanel panel;
	//public static LinkedList<GameClient> clients;
	public static int counter = 0;
	static MessageProducer producer;
	static MessageConsumer consumer;
	public static String userName;
	public static Session session;
	public static Topic topic;
	public static String username;
	public static boolean c = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	public Consumer(){

		
		setTitle("Hunt the fly");
		setSize(500,300);
		setLayout(new FlowLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBackground(Color.WHITE);
		
		userNameLabel = new JLabel();
		userNameLabel.setText("Player Name");
		userNameText = new JTextField(15);
		
		String curr_dir = System.getProperty("user.dir");
		System.out.println(curr_dir);
		
		JLabel flyImageJLabel = new JLabel(new ImageIcon(curr_dir.concat("\\fly.jpg")));
		getContentPane().add(flyImageJLabel);
		
		startButton = new JButton("Login");
		
		setVisible(true);
		//shetLocationRelativeTo(null);
		
		panel = new JPanel(new GridLayout(3,1));
		panel.add(userNameLabel);
		panel.add(userNameText);
		panel.add(startButton);
		add(panel,BorderLayout.CENTER);
		startButton.addActionListener(this);
	
	}
	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) throws JMSException {
		// TODO Auto-generated method stub
		System.out.println(url);
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        if(args.length != 0)
        	connection.setClientID(args[0]);
        //connection.setClientID(url);
        connection.start();
         session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);
         topic = session.createTopic("Game");
        consumer = session.createDurableSubscriber(topic, args[0]);
        producer = session.createProducer(topic);
        
        MessageListener listner = new MessageListener(){
            public void onMessage(Message message){
            	System.out.println("Object Received");
                if (message instanceof ObjectMessage) {
                	System.out.println("Object Message Received");
                	
				    GameClient gameclient = null;
					try {
						gameclient = (GameClient) ((ObjectMessage) message).getObject();
					} catch (JMSException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(gameclient.username.contains("_logout"))
					{
						String playerName = null;
						GameClient.receiveFlyHunted(gameclient.username, 0);
						playerName = gameclient.username.substring(0, gameclient.username.length() - 7);
						for(int i = 0;i < GameClient.player.size();i++)
						{
							if(playerName.equals(GameClient.player.get(i).playername))
							{
								GameClient.player.remove(i);
							}
						}
					}
				    if(gameclient.killed == true)
				    {
				    	//String player = null;
				    	if(gameclient.username.equals(Consumer.username) == false)
				    	{
				    		
				    	System.out.printf("level %d counter %d",GameClient.level,GameClient.counter);
				    	int j =0 ;
				    	for(;j < GameClient.player.size();j++)
				    	{
				    		if(gameclient.username.equals(GameClient.player.get(j).playername) == true)
				    			break;
				    	}
				    	GameClient.receiveFlyHunted(gameclient.username, ++GameClient.player.get(j).points);
				    	for(int i = 0;i < GameClient.clients.size();i++)
				    	{
				    		if(((gameclient.x == GameClient.clients.get(i).x) && (gameclient.y == GameClient.clients.get(i).y)))
				    		{
				    			System.out.printf("Removing index %d",i);
				    			GameClient.gamePanel.remove(GameClient.clients.get(i).flyImageJLabel);
				    			GameClient.clients.remove(i);
				    			GameClient.counter++;
				    			break;
				    		}
				    	}
				    	if((GameClient.counter % (2*GameClient.level) == 0) && (GameClient.counter != 0))
				    	{
				    		for(int i = 0; i < GameClient.counter/2 +1;i++)
							{
								int x = new Random().nextInt(Math.abs((500)+new Random().nextInt(100)));
								int y = new Random().nextInt(Math.abs((600)+new Random().nextInt(100)));
								new GameClient(username,x,y);
								try {
									producer.send(session.createObjectMessage(GameClient.clients.get(GameClient.clients.size()-1)));
								} catch (JMSException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
				    		for(int z = 0;z < GameClient.player.size();z++)
				    		{
				    			GameClient.player.get(z).points = 0;
				    			GameClient.receiveFlyHunted(GameClient.player.get(z).playername, 0);
				    		}
				    		GameClient.counter = 0;
				    		if(GameClient.levelhaschanged == false)
				    		{
				    			GameClient.levelhaschanged = true;
				    			GameClient.level++;
				    		}
				    	}
				    }
				    }
				    else 
				    {
				    	boolean present = false;
				    	boolean player_present = false;
				    	System.out.printf("username %s", gameclient.username);
				    	for(int i = 0;i < GameClient.clients.size();i++)
				    		if((gameclient.username.equals(GameClient.clients.get(i).username) == true) && (gameclient.x == (GameClient.clients.get(i).x)) && (gameclient.y == (GameClient.clients.get(i).y)))
				    			present = true;
				    	if(present == false)
				    	{
				    		System.out.println("Hello I am here");
				    		new GameClient(gameclient.username,gameclient.x,gameclient.y);
				    		
				    	}
				    	for(int i = 0;i < GameClient.player.size();i++)
				    		if(gameclient.username.equals(GameClient.player.get(i).playername))
				    			player_present = true;
				    	if(player_present == false)
				    			GameClient.Player(session, producer,gameclient.username);
				    }
                }
            }
        };
        consumer.setMessageListener(listner);
        new Consumer();
        
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

		 userName = userNameText.getText();
		int x = new Random().nextInt(Math.abs((500)+new Random().nextInt(100)));
		int y = new Random().nextInt(Math.abs((600)+new Random().nextInt(100)));
		if (!userName.isEmpty()) {
			System.out.println("called: Game console");
			setVisible(false);
			username = userNameText.getText();
			GameClient.createAndShowGUI();
			new GameClient(userNameText.getText(), x, y);
			GameClient.Player(session, producer, userNameText.getText());
			
			try {
				System.out.println("Object sent");
				producer.send(session.createObjectMessage(GameClient.clients.get(GameClient.clients.size() - 1)));
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}


}

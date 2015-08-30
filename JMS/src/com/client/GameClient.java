package com.client;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class GameClient implements Serializable {

	/**
	 * 
	 */
	public static JFrame frame;
	public static JTable table1;
	public static JPanel gamePanel = null; /*instructionPanel,*/ 
	public static JPanel scoresPanel;
	private static JTable table ;
	private static final long serialVersionUID = 1L;
	int x =	0;
	int y = 0;
	String username;
	JLabel flyImageJLabel;
	protected boolean killed = false;
	public static Map<String, Integer> data = new HashMap<String, Integer>();
	public static TableModel model;
	public static LinkedList<GameClient> clients = new LinkedList<GameClient>();
	public static LinkedList<Player> player = new LinkedList<Player>();
	public static String userName;
	public static int player_points;
	public static MessageProducer producer;
	public static Session session;
	public static int counter = 0;
	public static int level = 1;
	public static boolean levelhaschanged = false;
	//public static int number_flies = 0;
	
	
	public GameClient(String userName,int x, int y){
		System.out.printf("NEw Game Client added");
		
		int i = 0;
		this.username = userName;
		this.x = x;
		this.y = y;
		//this.points = 0;
		this.killed = false;
		clients.add(this);
		i = clients.size();
		System.out.printf("size of clients is %d", clients.size());
		if(gamePanel != null)
			this.FlyImageProcess(i-1);
		levelhaschanged = false;
		if((GameClient.level == 1) && (GameClient.gamePanel == null))
			Consumer.c = true;
		if((Consumer.c == true) && (GameClient.gamePanel != null))
		{
			System.out.println("HEllo 2");
			GameClient.clients.get(0).FlyImageProcess(0);
			Consumer.c = false;
		}
	}
	public GameClient() {
		// TODO Auto-generated constructor stub
	}
	public static void Player(Session session1,MessageProducer producer1,String username){
		producer = producer1;
		userName = username;
		session = session1;
		player.add(new Player(username,0));
		receiveFlyHunted(username,0);
	}

	
	
	public synchronized static void receiveFlyHunted(String playerName, int newPoints) {
		// TODO Auto-generated method stub
		System.out.println("STARTED: receiveFlyHunted");		
		System.out.printf("Player : %s, points : %d", playerName, newPoints);
		System.out.println();
		
		if(playerName.contains("_logout"))
		{
			System.out.println("Removing");
			playerName = playerName.substring(0, playerName.length() - 7);
			System.out.println("Player Name to be removed = "+playerName);
			data.remove(playerName);
		}
		else
		{
			data.put(playerName, newPoints);
		}
		
		data = sortHashMapByValues(data);
		System.out.println("Table is added up");
		model = toTableModel(data);
		table.setModel(model);
		table.setFillsViewportHeight(true);
		
		System.out.println("ENDED: receiveFlyHunted");
	}

	
	public void FlyImageProcess(final int i){
		String curr_dir = System.getProperty("user.dir");
		System.out.println(curr_dir);
		this.flyImageJLabel = new JLabel(new ImageIcon(curr_dir.concat("\\fly.jpg")));
		final GameClient client_temp = new GameClient();
		client_temp.username = GameClient.clients.get(i).username;
		client_temp.x = GameClient.clients.get(i).x;
		client_temp.y = GameClient.clients.get(i).y;
		client_temp.flyImageJLabel = GameClient.clients.get(i).flyImageJLabel;
		
		this.flyImageJLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				
					for(int j = 0;j < player.size();j++)
					{
						if(Consumer.username.equals(player.get(j).playername) == true)
							receiveFlyHunted(Consumer.username,++player.get(j).points);
					}
						client_temp.killed = true;
						try {
							System.out.println("Object sent");
							client_temp.username = Consumer.username;
							producer.send(session.createObjectMessage(client_temp));
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						client_temp.flyImageJLabel.removeMouseListener(this);
						gamePanel.remove(flyImageJLabel);
						for(int k = 0;k < clients.size();k++)
						{
							if(client_temp.flyImageJLabel.equals(GameClient.clients.get(k).flyImageJLabel))
							{
								System.out.printf("Mouse Clicked Removing %d",k);
								GameClient.clients.remove(k);
							}
						}		
					
				
				System.out.println("Hello ");
				counter++;
				
				if(counter % (2*level) == 0)
				{
					for(int i = 0; i < counter/2 +1;i++)
					{
						int x = new Random().nextInt(Math.abs((500)+new Random().nextInt(100)));
						int y = new Random().nextInt(Math.abs((600)+new Random().nextInt(100)));
						new GameClient(username,x,y);
						try {
							producer.send(session.createObjectMessage(clients.get(clients.size()-1)));
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
					counter = 0;
					if(levelhaschanged == false)
					{
						levelhaschanged = true;
						level++;
					}
				}
				
				Toolkit.getDefaultToolkit().beep();
				SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
					@Override
					protected Boolean doInBackground() throws Exception {

						//gameServer.huntFly(userName);
						
						publish(x);
						publish(y);
						return true;
					}
					protected void done() {
						try {
							boolean status = get();
							System.out.println("Status = "+status);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
					@Override
					protected void process(List<Integer> l) {
						
						int x = l.get(0);
						int y = l.get(1);
						
						System.out.println("Obtained x="+x+" y="+y);
					}
				};
				worker.execute();
			}
        });
		
		gamePanel.add(flyImageJLabel);
		flyImageJLabel.setBounds(x, y, 70, 70);
		
		
	} // end FlyImageProcess
	
	
public static void addComponentsToPane(final Container pane) {
		
		System.out.println("STARTED: addComponentsToPane");
		
		String curr_dir = System.getProperty("user.dir");
		System.out.println(curr_dir);
		
		gamePanel = new JPanel() {

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				for(int i= 0;i < GameClient.clients.size();i++)
					GameClient.clients.get(i).flyImageJLabel.setBounds(GameClient.clients.get(i).x, GameClient.clients.get(i).y, 70, 70);
			}
		};
		
		pane.add(gamePanel);
       
		table = new JTable();
        table.setFillsViewportHeight(true);
		gamePanel.add(table);
		
		/*
		 * Logout Button Implementation
		 */
		JButton logoutButton = new JButton("logout");
		gamePanel.add(logoutButton);
		
		logoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Logout Button clicked"); 
				frame.setVisible(false);
				for(int i = 0;i < GameClient.clients.size();i++)
					GameClient.clients.remove(i);
				for(int i = 0;i < GameClient.player.size();i++)
					GameClient.player.remove(i);
				GameClient logout = new GameClient();
				logout.username = Consumer.username.concat("_logout");
				try {
					producer.send(session.createObjectMessage(logout));
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				new Consumer();
				}
		});
		
		System.out.println("ENDED: addComponentsToPane");
		
	}
	
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
   public static void createAndShowGUI() {
    	System.out.println("STARTED: createAndShowUI");
    	
    	frame = new JFrame("Fly Hunting Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addComponentsToPane(frame.getContentPane());
        frame.pack();

        Insets insets = frame.getInsets();
        frame.setSize(new Dimension(insets.left + insets.right + 1800,
                insets.top + insets.bottom + 600));
        
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        System.out.println("ENDED: createAndShowUI");
    	
    }
   
	public static LinkedHashMap<String, Integer> sortHashMapByValues(Map<String, Integer> map) {
		   List<String> mapKeys = new ArrayList<String>(map.keySet());
		   List<Integer> mapValues = new ArrayList<Integer>(map.values());
		   Collections.sort(mapValues);
		   Collections.sort(mapKeys);
		
		   LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		
		   Iterator<Integer> valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator<String> keyIt = mapKeys.iterator();
		
		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = map.get(key).toString();
		           String comp2 = val.toString();
		
		           if (comp1.equals(comp2)){
		               map.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((String)key, (Integer)val);
		               break;
		           }
		       }
		   }
		   return sortedMap;
		}
	public static TableModel toTableModel(Map<String, Integer> map) {
		DefaultTableModel model = new DefaultTableModel (
		new Object[] { "Key", "Value" }, 0
		);
		for (Iterator<Entry<String, Integer>> it = map.entrySet().iterator(); it.hasNext();) {
		Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)it.next();
		model.addRow(new Object[] { entry.getKey(), entry.getValue() });
		}
		return model;
	}
	
}

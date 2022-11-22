import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * FriendGUI
 * derates the GUI for the popup for the user to view the friend list, accept friends request, and send friend requests
 *
 *
 *
 * @author Hunter Watson, Kaz Wu, Keegan Carter, Gonger Wang, Rahul Goyalsection
 * @version 5/3/2021
 *
 */
public class FriendGUI extends JComponent implements Runnable{
    public static ObjectInputStream reader;
    public static ObjectOutputStream writer;

    public static User friend;
    public static User friendReceived;
    public static User user;
    public static User userReceived;

    public static String username;

    JButton addNewFriend = new JButton("Add New Friend");
    JButton seeCurrentFriends = new JButton("See your current friend list");
    JButton seeRequestStatus = new JButton("See your request status");
    JButton home = new JButton("Home");

    JButton rescinded = new JButton("Rescinded X");
    JButton confirmed = new JButton("Confirmed √");

    JFrame frame;
    JComboBox<String> comboBox = new JComboBox<>();
    JComboBox<String> comboBoxTwo = new JComboBox<>();
    JTextArea jta;
    JScrollPane jsp;
    JPanel jp;

    String chosenUser;
    String chosenUserTwo;

    Socket socket;

    public void run() {
        frame = new JFrame("Friend Request Page");
        Container container = frame.getContentPane();
        frame.setSize(500,600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        addNewFriend.setBounds(120,100,250,50);
        container.add(addNewFriend);

        seeCurrentFriends.setBounds(120,200,250,50);
        container.add(seeCurrentFriends);

        seeRequestStatus.setBounds(120,300,250,50);
        container.add(seeRequestStatus);

        home.setBounds(120,520,250,50);
        container.add(home);

        jta = new JTextArea("\n",20,20);
        jta.setEditable(false);
        jta.setLineWrap(true);
        jta.setForeground(Color.BLACK);
        jta.setBackground(Color.WHITE);
        jta.setBounds(500,20,400,400);
        jsp = new JScrollPane(jta);
        jsp.setBounds(50,20,400,400);
        jsp.setVisible(false);
        container.add(jsp);

        rescinded.setBounds(100,350,150,50);
        container.add(rescinded);
        rescinded.setVisible(false);

        confirmed.setBounds(300,350,150,50);
        container.add(confirmed);
        confirmed.setVisible(false);

        JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
        line.setBounds(120, 580, 250, 20);
        container.add(line);
        line.setVisible(false);
        //--------------------------------------------------------------------------------------------

        try {
            socket = new Socket("localhost", 4242);

            User u = new User(username);
            u.setPhase(0);

            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.writeObject(u);
            writer.flush();

            reader = new ObjectInputStream(socket.getInputStream());
            userReceived = (User) reader.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        addNewFriend.addActionListener(e -> {
            String newFriendName;
            while (true) {
                newFriendName =
                        JOptionPane.showInputDialog(null, "What is his/her user name?",
                                "Friend Request Page", JOptionPane.QUESTION_MESSAGE);

                friend = new User(newFriendName);
                friend.setPhase(0);
                try {
                    writer.writeObject(friend);
                    writer.flush();
                    friendReceived = (User) reader.readObject();
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }

                if (friendReceived.getUsername() != null
                        && (userReceived.getFriendsList() == null || !userReceived.getFriendsList().contains(newFriendName))
                        && (userReceived.getFriendsList() == null || !userReceived.getFriendrequests().contains(newFriendName))
                        && (newFriendName != null && !newFriendName.equals(""))) {

                    userReceived.getFriendrequests().add(newFriendName);
                    friendReceived.getIncomingrequst().add(userReceived.getUsername());

                    JOptionPane.showMessageDialog(null, "Sent the Friend Request!",
                            "Friend Request Page", JOptionPane.PLAIN_MESSAGE);
                    break;
                } else if (newFriendName == null) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Username!",
                            "Friend Request Page", JOptionPane.ERROR_MESSAGE);
                }
            }
            userReceived.setPhase(1);
            friendReceived.setPhase(1);
            try {
                writer.writeObject(userReceived);
                writer.flush();
                userReceived = (User) reader.readObject();
                writer.writeObject(friendReceived);
                writer.flush();
                friendReceived = (User) reader.readObject();
            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        });

        seeCurrentFriends.addActionListener(e -> {
            addNewFriend.setVisible(false);
            seeRequestStatus.setVisible(false);
            seeCurrentFriends.setVisible(false);
            if (jp != null) {jp.setVisible(false);}
            jsp.setVisible(true);

            jta.selectAll();
            jta.replaceSelection("\nHere is your friends:\n-------------------\n");

            if (userReceived.getFriendsList() == null || userReceived.getFriendsList().isEmpty()) {
                jta.append("NO Friends Available!");
            } else {
                for (var i = 0; i < userReceived.getFriendsList().size(); i++) {
                    jta.append(userReceived.getFriendsList().get(i) + '\n');
                }
            }
        });

        seeRequestStatus.addActionListener(e -> {
            addNewFriend.setVisible(false);
            seeRequestStatus.setVisible(false);
            seeCurrentFriends.setVisible(false);
            jsp.setVisible(false);

            rescinded.setVisible(true);
            confirmed.setVisible(true);

            comboBox = new JComboBox<>();
            comboBox.addItem("Sent requests below:");

            comboBoxTwo = new JComboBox<>();
            comboBoxTwo.addItem("Received requests below:");

            jp = new JPanel(new BorderLayout());

            if (userReceived.getFriendsList() != null) {
                for (var i = 0; i < userReceived.getFriendrequests().size(); i++) {
                    comboBox.addItem(userReceived.getFriendrequests().get(i));
                }
            }
            if (userReceived.getIncomingrequst() != null) {
                for (var i = 0; i < userReceived.getIncomingrequst().size(); i++) {
                    comboBoxTwo.addItem(userReceived.getIncomingrequst().get(i));
                }
            }

            jp.add(comboBox, BorderLayout.EAST);
            jp.add(comboBoxTwo, BorderLayout.WEST);
            jp.setVisible(true);
            container.add(jp, BorderLayout.NORTH);

            confirmed.addActionListener(a -> {
                chosenUser = String.valueOf(comboBox.getSelectedItem());
                chosenUserTwo = String.valueOf(comboBoxTwo.getSelectedItem());


                if (!chosenUser.equals("Sent requests below:")) {
                    try {
                        writer.writeObject(new User(chosenUser));
                        writer.flush();
                        friendReceived = (User) reader.readObject();
                    } catch (IOException | ClassNotFoundException ioException) {
                        ioException.printStackTrace();
                    }
                    userReceived.getFriendrequests().remove(chosenUser);
                    friendReceived.getIncomingrequst().add(userReceived.getUsername());

                } else if (!chosenUserTwo.equals("Received requests below:")) {

                    try {
                        writer.writeObject(new User(chosenUserTwo));
                        writer.flush();
                        friendReceived = (User) reader.readObject();
                    } catch (IOException | ClassNotFoundException ioException) {
                        ioException.printStackTrace();
                    }

                    userReceived.getFriendsList().add(chosenUserTwo);
                    userReceived.getIncomingrequst().remove(chosenUserTwo);

                    friendReceived.getFriendsList().add(userReceived.getUsername());
                    friendReceived.getFriendrequests().remove(userReceived.getUsername());
                }

                if (!chosenUser.equals("Sent requests below:") || !chosenUserTwo.equals("Received requests below:")) {
                    JOptionPane.showMessageDialog(null,
                            "Confirmed this friend request, now please return to home page", "Confirmed Successfully!",
                            JOptionPane.PLAIN_MESSAGE);
                }

                userReceived.setPhase(1);
                friendReceived.setPhase(1);

                try {
                    writer.writeObject(userReceived);
                    writer.flush();
                    userReceived = (User) reader.readObject();
                    writer.writeObject(friendReceived);
                    writer.flush();
                    friendReceived = (User) reader.readObject();
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            });

            rescinded.addActionListener(b -> {
                chosenUser = String.valueOf(comboBox.getSelectedItem());
                chosenUserTwo = String.valueOf(comboBoxTwo.getSelectedItem());

                if (!chosenUser.equals("Sent requests below:")) {
                    try {
                        writer.writeObject(new User(chosenUser));
                        writer.flush();
                        friendReceived = (User) reader.readObject();
                    } catch (IOException | ClassNotFoundException ioException) {
                        ioException.printStackTrace();
                    }

                    userReceived.getFriendrequests().remove(chosenUser);
                } else if (!chosenUserTwo.equals("Received requests below:")) {

                    try {
                        writer.writeObject(new User(chosenUserTwo));
                        writer.flush();
                        friendReceived = (User) reader.readObject();
                    } catch (IOException | ClassNotFoundException ioException) {
                        ioException.printStackTrace();
                    }

                    userReceived.getIncomingrequst().remove(chosenUserTwo);
                    friendReceived.getFriendrequests().remove(userReceived.getUsername());
                }

                if (!chosenUser.equals("Sent requests below:") || !chosenUserTwo.equals("Received requests below:")) {
                    JOptionPane.showMessageDialog(null,
                            "Rescinded this friend request, now please return to home page", "Rescinded Successfully!",
                            JOptionPane.PLAIN_MESSAGE);
                }

                userReceived.setPhase(1);
                friendReceived.setPhase(1);

                try {
                    writer.writeObject(userReceived);
                    writer.flush();
                    userReceived = (User) reader.readObject();
                    writer.writeObject(friendReceived);
                    writer.flush();
                    friendReceived = (User) reader.readObject();
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            });

        });

        home.addActionListener(e -> {
            addNewFriend.setVisible(true);
            seeCurrentFriends.setVisible(true);
            seeRequestStatus.setVisible(true);
            jsp.setVisible(false);
            rescinded.setVisible(false);
            confirmed.setVisible(false);
            if (jp != null) {
                jp.setVisible(false);
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

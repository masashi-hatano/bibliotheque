import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.TableCellRenderer;

public class Main {

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());

        login();
    }

    public static void login() {
        JFrame f = new JFrame("Login");
        String[] typesUtilisateur = {"Utilisateur", "Admin"};
        JComboBox comboBox1 = new JComboBox(typesUtilisateur);
        comboBox1.setBounds(25, 15, 90, 25);

        JLabel l1, l2;
        l1 = new JLabel("Username");
        l1.setBounds(35, 50, 100, 30);

        l2 = new JLabel("Password");
        l2.setBounds(35, 85, 100, 30);

        JTextField F_user = new JTextField();
        F_user.setBounds(125, 50, 200, 30);

        JPasswordField F_pass = new JPasswordField();
        F_pass.setBounds(125, 85, 200, 30);

        JButton login_but = new JButton("Login");
        login_but.setBounds(160, 125, 80, 25);
        login_but.addActionListener(e -> {
            Integer mode = comboBox1.getSelectedIndex();
            String username = F_user.getText();
            String password = F_pass.getText();

            if (username.equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter username");
            } else if (password.equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter password");
            } else if (mode.equals(1)) {
                Connection connection = connect();
                try {
                    Statement stmt = connection.createStatement();
                    String sql = ("SELECT * FROM gestionnaire WHERE username='" + username + "' AND password='" + password + "'");
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        f.dispose();
                        gestion_menu();
                        connection.close();
                    } else {
                        JOptionPane.showMessageDialog(null, "Your Username/Password is wrong !");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Connection connection = connect();
                try {
                    Statement stmt = connection.createStatement();
                    String sql = ("SELECT * FROM utilisateur WHERE username='" + username + "' AND password='" + password + "'");
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        f.dispose();
                        Integer userID = rs.getInt("utilisateurID");
                        user_menu(userID);
                        connection.close();
                    } else {
                        JOptionPane.showMessageDialog(null, "Your Username/Password is wrong !");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        f.add(comboBox1);
        f.add(F_user);
        f.add(F_pass);
        f.add(login_but);
        f.add(l1);
        f.add(l2);

        f.setSize(400, 210);
        f.setLayout(null);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void user_menu(Integer userID) {
        JFrame f = new JFrame("User Functions");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l;
        l = new JLabel("USER ID : " + userID + "");
        l.setBounds(10, 10, 150, 30);

        JButton view_but = new JButton("Available Books");
        view_but.setBounds(70, 150, 120, 40);
        view_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Connection connection = connect();
                try {
                    JFrame f_viewbooks = new JFrame("All available books");
                    Statement stmt = connection.createStatement();
                    String sql = "SELECT livre.ISBN,livre.titre,auteur,date_publication,editeur,editionyear,COUNT(livre.ISBN) " +
                                 "FROM livre " +
                                 "INNER JOIN edition ON livre.ISBN = edition.ISBN " +
                                 "INNER JOIN oeuvre ON livre.titre = oeuvre.titre " +
                                 "INNER JOIN auteur ON oeuvre.auteurID1=auteur.auteurID " +
                                 "WHERE utilisateurID is NULL " +
                                 "GROUP BY livre.ISBN " +
                                 "ORDER BY livre.ISBN ASC ";
                    ResultSet rs = stmt.executeQuery(sql);
                    int line = 0;
                    Object[][] data = new Object[100][8];
                    String[] columns = {"ISBN", "titre", "auteur", "date of first publication", "editeur", "editionyear", "quantity", "Emprunt"};
                    while (rs.next()) {
                        data[line][0] = rs.getString("ISBN");
                        data[line][1] = rs.getString("titre");
                        data[line][2] = rs.getString("auteur");
                        data[line][3] = rs.getString("date_publication");
                        data[line][4] = rs.getString("editeur");
                        data[line][5] = rs.getInt("editionyear");
                        data[line][6] = rs.getInt("COUNT(livre.ISBN)");
                        data[line][7] = "Borrow : " + rs.getString("ISBN") + "";
                        line += 1;
                    }

                    String sql1 = "SELECT livre.ISBN,livre.titre,auteur,auteurID2,date_publication,editeur,editionyear,COUNT(livre.ISBN) " +
                            "FROM livre " +
                            "INNER JOIN edition ON livre.ISBN = edition.ISBN " +
                            "INNER JOIN oeuvre ON livre.titre = oeuvre.titre " +
                            "INNER JOIN auteur ON oeuvre.auteurID2=auteur.auteurID " +
                            "WHERE utilisateurID is NULL " +
                            "GROUP BY livre.ISBN";
                    ResultSet rs1 = stmt.executeQuery(sql1);
                    int line1 = 0;
                    Object[][] data1 = new Object[100][8];
                    while (rs1.next()) {
                        data1[line1][2] = rs1.getString("auteur");
                        if(rs1.getInt("auteurID2")==100){}
                        else{
                            data[line1][2] +="," + data1[line1][2];
                        }
                        line1+=1;
                    }
                    JTable table = new JTable(data, columns);
                    JScrollPane scroll = new JScrollPane(table);

                    f_viewbooks.setSize(900, 500);
                    f_viewbooks.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f_viewbooks.add(scroll, BorderLayout.CENTER);
                    f_viewbooks.setVisible(true);
                    f_viewbooks.setLocationRelativeTo(null);
                    connection.close();

                    table.getColumn("Emprunt").setCellRenderer(new ButtonRenderer());
                    table.getColumn("Emprunt").setCellEditor(new ButtonBorrow(new JCheckBox()));
                    table.setPreferredScrollableViewportSize(table.getPreferredSize());
                    table.getColumnModel().getColumn(0).setPreferredWidth(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton my_book = new JButton("My Books");
        my_book.setBounds(260, 150, 120, 40);
        my_book.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Connection connection = connect();
                try {
                    JFrame f_mybooks = new JFrame("My books");
                    Statement stmt = connection.createStatement();
                    String sql = "SELECT livreID, livre.ISBN, livre.titre, auteur, date_publication, editeur, editionyear " +
                            "FROM livre " +
                            "INNER JOIN edition ON livre.ISBN=edition.ISBN " +
                            "INNER JOIN oeuvre ON livre.titre=oeuvre.titre " +
                            "INNER JOIN auteur ON oeuvre.auteurID1=auteur.auteurID " +
                            "WHERE utilisateurID=" + userID + " " +
                            "ORDER BY livreID ASC";
                    ResultSet rs = stmt.executeQuery(sql);
                    int line = 0;
                    Object[][] data = new Object[10][8];
                    String[] columns = {"livreID", "ISBN", "titre", "auteur", "date of fist publication", "editeur", "editionyear", "Return"};
                    while (rs.next()) {
                        data[line][0] = rs.getInt("livreID");
                        data[line][1] = rs.getString("ISBN");
                        data[line][2] = rs.getString("titre");
                        data[line][3] = rs.getString("auteur");
                        data[line][4] = rs.getString("date_publication");
                        data[line][5] = rs.getString("editeur");
                        data[line][6] = rs.getInt("editionyear");
                        data[line][7] = "Return : " + rs.getString("ISBN") + "";
                        line += 1;
                    }

                    String sql1 = "SELECT livreID, ISBN, livre.titre, utilisateurID, auteur, auteurID2 " +
                            "FROM livre " +
                            "INNER JOIN oeuvre ON livre.titre=oeuvre.titre " +
                            "INNER JOIN auteur ON oeuvre.auteurID2=auteur.auteurID " +
                            "ORDER BY livreID ASC";
                    ResultSet rs1 = stmt.executeQuery(sql1);
                    int line1 = 0;
                    Object[][] data1 = new Object[10][8];
                    while (rs1.next()) {
                        if(rs1.getInt("utilisateurID")==userID) {
                            data1[line1][3] = rs1.getString("auteur");
                            if (rs1.getInt("auteurID2") == 100) {
                            } else {
                                data[line1][3] += "," + data1[line1][3];
                            }
                            line1 += 1;
                        }
                    }

                    JTable table = new JTable(data, columns);
                    JScrollPane scroll = new JScrollPane(table);

                    f_mybooks.setSize(900, 500);
                    f_mybooks.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f_mybooks.add(scroll, BorderLayout.CENTER);
                    f_mybooks.setVisible(true);
                    f_mybooks.setLocationRelativeTo(null);
                    connection.close();

                    table.getColumn("Return").setCellRenderer(new ButtonRenderer());
                    table.getColumn("Return").setCellEditor(new ButtonReturn(new JCheckBox()));
                    table.setPreferredScrollableViewportSize(table.getPreferredSize());
                    table.getColumnModel().getColumn(0).setPreferredWidth(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        JButton history = new JButton("History");
        history.setBounds(450, 150, 120, 40);
        history.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Connection connection = connect();
                try {
                    JFrame f_history = new JFrame("History");
                    Statement stmt = connection.createStatement();
                    String sql = "SELECT date_column, history.livreID, ISBN, titre, purpose " +
                            "FROM history " +
                            "INNER JOIN livre ON history.livreID=livre.livreID " +
                            "WHERE history.utilisateurID='" + userID + "'";
                    ResultSet rs = stmt.executeQuery(sql);
                    int line = 0;
                    Object[][] data = new Object[100][5];
                    String[] columns = {"Date", "livreID", "ISBN", "titre", "Purpose"};
                    while (rs.next()) {
                        data[line][0] = rs.getString("date_column");
                        data[line][1] = rs.getInt("livreID");
                        data[line][2] = rs.getString("ISBN");
                        data[line][3] = rs.getString("titre");
                        data[line][4] = rs.getString("purpose");
                        if(rs.getInt("livreID")==0){
                            data[line][1] = "----------";
                            data[line][2] = "----------";
                            data[line][3] = "----------";
                        }
                        line += 1;
                    }

                    JTable table = new JTable(data, columns);
                    JScrollPane scroll = new JScrollPane(table);

                    f_history.setSize(900, 500);
                    f_history.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f_history.add(scroll, BorderLayout.CENTER);
                    f_history.setVisible(true);
                    f_history.setLocationRelativeTo(null);
                    connection.close();

                    table.setPreferredScrollableViewportSize(table.getPreferredSize());
                    table.getColumnModel().getColumn(0).setPreferredWidth(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        });

        f.add(my_book);
        f.add(view_but);
        f.add(history);
        f.add(l);
        f.setSize(640, 400);
        f.setLayout(null);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
    }

    public static void gestion_menu() {
        JFrame f = new JFrame("Admin Functions");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton books = new JButton("Book");
        books.setBounds(70, 100, 120, 40);
        books.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_books = new JFrame("Book");
                f_books.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JButton all_books = new JButton("All Books");
                all_books.setBounds(70, 100, 120, 40);
                all_books.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_viewbooks = new JFrame("All books");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT livreID, livre.ISBN,livre.titre,auteur,date_publication,editeur,editionyear, utilisateurID " +
                                    "FROM livre " +
                                    "INNER JOIN edition ON livre.ISBN = edition.ISBN " +
                                    "INNER JOIN oeuvre ON livre.titre = oeuvre.titre " +
                                    "INNER JOIN auteur ON oeuvre.auteurID1=auteur.auteurID " +
                                    "ORDER BY livreID ASC ";
                            ResultSet rs = stmt.executeQuery(sql);
                            int line = 0;
                            Object[][] data = new Object[100][8];
                            String[] columns = {"livreID", "ISBN", "titre", "auteur", "date of first publication", "editeur", "editionyear", "Borrowed By"};
                            while (rs.next()) {
                                data[line][0] = rs.getString("livreID");
                                data[line][1] = rs.getString("ISBN");
                                data[line][2] = rs.getString("titre");
                                data[line][3] = rs.getString("auteur");
                                data[line][4] = rs.getString("date_publication");
                                data[line][5] = rs.getString("editeur");
                                data[line][6] = rs.getInt("editionyear");
                                data[line][7] = rs.getString("utilisateurID");
                                line += 1;
                            }

                            String sql1 = "SELECT livreID, livre.ISBN,livre.titre,auteur,auteurID2 " +
                                    "FROM livre " +
                                    "INNER JOIN edition ON livre.ISBN = edition.ISBN " +
                                    "INNER JOIN oeuvre ON livre.titre = oeuvre.titre " +
                                    "INNER JOIN auteur ON oeuvre.auteurID2=auteur.auteurID " +
                                    "ORDER BY livreID ASC ";
                            ResultSet rs1 = stmt.executeQuery(sql1);
                            int line1 = 0;
                            Object[][] data1 = new Object[100][8];
                            while (rs1.next()) {
                                data1[line1][3] = rs1.getString("auteur");
                                if(rs1.getInt("auteurID2")==100){}
                                else{
                                    data[line1][3] +="," + data1[line1][3];
                                }
                                line1+=1;
                            }
                            JTable table = new JTable(data, columns);
                            JScrollPane scroll = new JScrollPane(table);

                            f_viewbooks.setSize(900, 500);
                            f_viewbooks.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f_viewbooks.add(scroll, BorderLayout.CENTER);
                            f_viewbooks.setVisible(true);
                            f_viewbooks.setLocationRelativeTo(null);
                            connection.close();

                            table.setPreferredScrollableViewportSize(table.getPreferredSize());
                            table.getColumnModel().getColumn(0).setPreferredWidth(100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton add_book = new JButton("Add Book");
                add_book.setBounds(220, 100, 120, 40);
                add_book.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame f_addbook = new JFrame("Add Book");
                        f_addbook.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        JLabel l1, l2, l3, l4, l5, l6, l7, l8, l9;
                        l1 = new JLabel("*livreID");
                        l1.setBounds(10, 50, 100, 30);

                        l2 = new JLabel("*ISBN");
                        l2.setBounds(60, 50, 100, 30);

                        l3 = new JLabel("*titre");
                        l3.setBounds(100, 50, 100, 30);

                        l4 = new JLabel("*auteurID1");
                        l4.setBounds(150, 50, 100, 30);

                        l5 = new JLabel("auteurID2");
                        l5.setBounds(220, 50, 100, 30);

                        l6 = new JLabel("*date of first pub");
                        l6.setBounds(290, 50, 100, 30);

                        l7 = new JLabel("*editeur");
                        l7.setBounds(390, 50, 100, 30);

                        l8 = new JLabel("*edition year");
                        l8.setBounds(440,50,100,30);

                        l9 = new JLabel("*oeuvreID");
                        l9.setBounds(520,50,100,30);

                        JTextField F_livreID = new JTextField();
                        F_livreID.setBounds(5, 80, 50, 30);

                        JTextField F_ISBN = new JTextField();
                        F_ISBN.setBounds(55, 80, 40, 30);

                        JTextField F_titre = new JTextField();
                        F_titre.setBounds(95,80,50,30);

                        JTextField F_auteurID1 = new JTextField();
                        F_auteurID1.setBounds(145, 80, 70, 30);

                        JTextField F_auteurID2 = new JTextField();
                        F_auteurID2.setBounds(215, 80, 70, 30);

                        JTextField F_date = new JTextField();
                        F_date.setBounds(285, 80, 100, 30);

                        JTextField F_editeur = new JTextField();
                        F_editeur.setBounds(385, 80, 50, 30);

                        JTextField F_editionyear = new JTextField();
                        F_editionyear.setBounds(435, 80, 80, 30);

                        JTextField F_oeuvreID = new JTextField();
                        F_oeuvreID.setBounds(515, 80, 70, 30);

                        JButton add_but = new JButton("Add");
                        add_but.setBounds(260,140,80,25);
                        add_but.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String livreID = F_livreID.getText();
                                String ISBN = F_ISBN.getText();
                                String titre = F_titre.getText();
                                String auteurID1 = F_auteurID1.getText();
                                String auteurID2 = F_auteurID2.getText();
                                String date = F_date.getText();
                                String editeur = F_editeur.getText();
                                String editionyear = F_editionyear.getText();
                                String oeuvreID = F_oeuvreID.getText();

                                if(livreID.equals("")||ISBN.equals("")||titre.equals("")||auteurID1.equals("")||date.equals("")||editeur.equals("")||editionyear.equals("")||oeuvreID.equals("")){
                                    JOptionPane.showMessageDialog(null, "Please fill all * blanc");
                                }
                                else{
                                    Connection connection = connect();
                                    try {
                                        Statement stmt = connection.createStatement();
                                        String sql = ("SELECT livreID FROM livre WHERE livreID='" + livreID + "'");
                                        ResultSet rs = stmt.executeQuery(sql);
                                        if (rs.next()) {
                                            JOptionPane.showMessageDialog(null, "This livreID is already used");
                                        }
                                        else {
                                            String sql1 = "REPLACE INTO livre(livreID,titre,ISBN) VALUES(?,?,?)";
                                            String sql2 = "REPLACE INTO edition(editeur, editionyear) VALUES(?,?)";
                                            String sql3 = "REPLACE INTO oeuvre(oeuvreID, titre, date_publication, auteurID1, auteurID2) VALUES (?,?,?,?,?)";
                                            PreparedStatement ps1 = connection.prepareStatement(sql1);
                                            PreparedStatement ps2 = connection.prepareStatement(sql2);
                                            PreparedStatement ps3 = connection.prepareStatement(sql3);

                                            ps1.setString(1, livreID);
                                            ps1.setString(2,titre);
                                            ps1.setString(3,ISBN);
                                            ps1.executeUpdate();

                                            ps2.setString(1,editeur);
                                            ps2.setString(2,editionyear);
                                            ps2.executeUpdate();

                                            ps3.setString(1,oeuvreID);
                                            ps3.setString(2,titre);
                                            ps3.setString(3,date);
                                            ps3.setString(4,auteurID1);
                                            ps3.setString(5,auteurID2);
                                            ps3.executeUpdate();

                                            connection.close();
                                            JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });

                        f_addbook.add(l1);
                        f_addbook.add(l2);
                        f_addbook.add(l3);
                        f_addbook.add(l4);
                        f_addbook.add(l5);
                        f_addbook.add(l6);
                        f_addbook.add(l7);
                        f_addbook.add(l8);
                        f_addbook.add(l9);

                        f_addbook.add(F_livreID);
                        f_addbook.add(F_ISBN);
                        f_addbook.add(F_titre);
                        f_addbook.add(F_auteurID1);
                        f_addbook.add(F_auteurID2);
                        f_addbook.add(F_date);
                        f_addbook.add(F_editeur);
                        f_addbook.add(F_editionyear);
                        f_addbook.add(F_oeuvreID);

                        f_addbook.add(add_but);

                        f_addbook.setSize(600, 230);
                        f_addbook.setLayout(null);
                        f_addbook.setVisible(true);
                        f_addbook.setLocationRelativeTo(null);
                        f_addbook.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    }
                });

                JButton modify_book = new JButton("Modify Book");
                modify_book.setBounds(370, 100, 120, 40);

                f_books.add(all_books);
                f_books.add(add_book);
                f_books.add(modify_book);

                f_books.setSize(550, 300);
                f_books.setLayout(null);
                f_books.setVisible(true);
                f_books.setLocationRelativeTo(null);
            }
        });

        JButton users = new JButton("User");
        users.setBounds(260, 100, 120, 40);
        users.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_users = new JFrame("User");
                f_users.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JButton all_users = new JButton("All Users");
                all_users.setBounds(70, 100, 120, 40);
                all_users.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_viewusers = new JFrame("All Users");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM utilisateur " +
                                    "ORDER BY utilisateurID ASC";
                            ResultSet rs = stmt.executeQuery(sql);
                            int line = 0;
                            Object[][] data = new Object[100][7];
                            String[] columns = {"utilisateurID", "Nom", "Prenom", "e-mail", "tel", "username", "password"};
                            while (rs.next()) {
                                data[line][0] = rs.getString("utilisateurID");
                                data[line][1] = rs.getString("nom");
                                data[line][2] = rs.getString("prenom");
                                data[line][3] = rs.getString("email");
                                data[line][4] = rs.getString("tel");
                                data[line][5] = rs.getString("username");
                                data[line][6] = rs.getString("password");
                                line += 1;
                            }

                            JTable table = new JTable(data, columns);
                            JScrollPane scroll = new JScrollPane(table);

                            f_viewusers.setSize(900, 500);
                            f_viewusers.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f_viewusers.add(scroll, BorderLayout.CENTER);
                            f_viewusers.setVisible(true);
                            f_viewusers.setLocationRelativeTo(null);
                            connection.close();

                            table.setPreferredScrollableViewportSize(table.getPreferredSize());
                            table.getColumnModel().getColumn(0).setPreferredWidth(100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton add_user = new JButton("Add User");
                add_user.setBounds(220, 100, 120, 40);
                add_user.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame f_adduser = new JFrame("Add User");
                        f_adduser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        JLabel l1, l2, l3, l4, l5, l6, l7;
                        l1 = new JLabel("*Nom");
                        l1.setBounds(10, 50, 100, 30);

                        l2 = new JLabel("*Prenom");
                        l2.setBounds(60, 50, 100, 30);

                        l3 = new JLabel("email");
                        l3.setBounds(115, 50, 100, 30);

                        l4 = new JLabel("tel");
                        l4.setBounds(190, 50, 100, 30);

                        l5 = new JLabel("*Username");
                        l5.setBounds(240, 50, 100, 30);

                        l6 = new JLabel("*Password");
                        l6.setBounds(310, 50, 100, 30);

                        l7 = new JLabel("*UserID");
                        l7.setBounds(380,50,100,30);

                        JTextField F_nom = new JTextField();
                        F_nom.setBounds(5, 80, 50, 30);

                        JTextField F_prenom = new JTextField();
                        F_prenom.setBounds(55, 80, 55, 30);

                        JTextField F_email = new JTextField();
                        F_email.setBounds(110, 80, 80, 30);

                        JTextField F_tel = new JTextField();
                        F_tel.setBounds(190, 80, 50, 30);

                        JTextField F_username = new JTextField();
                        F_username.setBounds(240, 80, 70, 30);

                        JTextField F_password = new JTextField();
                        F_password.setBounds(310, 80, 70, 30);

                        JTextField F_userID = new JTextField();
                        F_userID.setBounds(380, 80, 70, 30);

                        JButton add_but = new JButton("Add");
                        add_but.setBounds(195,140,80,25);
                        add_but.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String nom = F_nom.getText();
                                String prenom = F_prenom.getText();
                                String email = F_email.getText();
                                String tel = F_tel.getText();
                                String username = F_username.getText();
                                String password = F_password.getText();
                                String userID = F_userID.getText();

                                if(nom.equals("")||prenom.equals("")||username.equals("")||password.equals("")||userID.equals("")){
                                    JOptionPane.showMessageDialog(null, "Please fill all * blanc");
                                }
                                else{
                                    Connection connection = connect();
                                    try {
                                        Statement stmt = connection.createStatement();
                                        String sql = ("SELECT * FROM utilisateur WHERE username='" + username + "'");
                                        ResultSet rs = stmt.executeQuery(sql);
                                        if (rs.next()) {
                                            JOptionPane.showMessageDialog(null, "This username is already used");
                                        }
                                        else {
                                            String sql1 = "REPLACE INTO utilisateur(nom, prenom, email, tel, username, password, utilisateurID) VALUES(?,?,?,?,?,?,?)";
                                            PreparedStatement ps = connection.prepareStatement(sql1);
                                            ps.setString(1, nom);
                                            ps.setString(2, prenom);
                                            ps.setString(3, email);
                                            ps.setString(4, tel);
                                            ps.setString(5, username);
                                            ps.setString(6, password);
                                            ps.setString(7,userID);

                                            ps.executeUpdate();
                                            connection.close();
                                            JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });

                        f_adduser.add(l1);
                        f_adduser.add(l2);
                        f_adduser.add(l3);
                        f_adduser.add(l4);
                        f_adduser.add(l5);
                        f_adduser.add(l6);
                        f_adduser.add(l7);

                        f_adduser.add(F_nom);
                        f_adduser.add(F_prenom);
                        f_adduser.add(F_email);
                        f_adduser.add(F_tel);
                        f_adduser.add(F_username);
                        f_adduser.add(F_password);
                        f_adduser.add(F_userID);

                        f_adduser.add(add_but);

                        f_adduser.setSize(470, 230);
                        f_adduser.setLayout(null);
                        f_adduser.setVisible(true);
                        f_adduser.setLocationRelativeTo(null);
                        f_adduser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    }
                });

                JButton modify_user = new JButton("Modify User");
                modify_user.setBounds(370, 100, 120, 40);

                f_users.add(all_users);
                f_users.add(add_user);
                f_users.add(modify_user);

                f_users.setSize(550, 300);
                f_users.setLayout(null);
                f_users.setVisible(true);
                f_users.setLocationRelativeTo(null);
            }
        });

        JButton authors = new JButton("Author");
        authors.setBounds(450, 100, 120, 40);
        authors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_authors = new JFrame("Author");
                f_authors.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JButton all_authors = new JButton("All Authors");
                all_authors.setBounds(70, 100, 120, 40);
                all_authors.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_viewauthors = new JFrame("All Authors");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM auteur " +
                                    "ORDER BY auteurID ASC";
                            ResultSet rs = stmt.executeQuery(sql);
                            int line = 0;
                            Object[][] data = new Object[100][3];
                            String[] columns = {"auteurID","auteur","anniversaire"};
                            while (rs.next()) {
                                if(rs.getInt("auteurID")!=100) {
                                    data[line][0] = rs.getInt("auteurID");
                                    data[line][1] = rs.getString("auteur");
                                    data[line][2] = rs.getString("anniversaire");
                                }
                                line += 1;
                            }

                            JTable table = new JTable(data, columns);
                            JScrollPane scroll = new JScrollPane(table);

                            f_viewauthors.setSize(900, 500);
                            f_viewauthors.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f_viewauthors.add(scroll, BorderLayout.CENTER);
                            f_viewauthors.setVisible(true);
                            f_viewauthors.setLocationRelativeTo(null);
                            connection.close();

                            table.setPreferredScrollableViewportSize(table.getPreferredSize());
                            table.getColumnModel().getColumn(0).setPreferredWidth(100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton add_author = new JButton("Add Author");
                add_author.setBounds(220, 100, 120, 40);
                add_author.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame f_addauthor = new JFrame("Add Admin");
                        f_addauthor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        JLabel l1, l2, l3;
                        l1 = new JLabel("*ID");
                        l1.setBounds(10, 20, 100, 30);

                        l2 = new JLabel("*auteur");
                        l2.setBounds(60, 20, 100, 30);

                        l3 = new JLabel("anniversaire");
                        l3.setBounds(115, 20, 100, 30);

                        JTextField F_auteurID = new JTextField();
                        F_auteurID.setBounds(5, 50, 50, 30);

                        JTextField F_auteur = new JTextField();
                        F_auteur.setBounds(55, 50, 55, 30);

                        JTextField F_anniv = new JTextField();
                        F_anniv.setBounds(110, 50, 80, 30);

                        JButton add_but = new JButton("Add");
                        add_but.setBounds(60,90,80,25);
                        add_but.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String auteurID = F_auteurID.getText();
                                String auteur = F_auteur.getText();
                                String anniv = F_anniv.getText();

                                if(auteurID.equals("")||auteur.equals("")){
                                    JOptionPane.showMessageDialog(null, "Please fill all * blanc");
                                }
                                else{
                                    Connection connection = connect();
                                    try {
                                        Statement stmt = connection.createStatement();
                                        String sql = ("SELECT * FROM auteur WHERE auteurID='" + auteurID + "'");
                                        ResultSet rs = stmt.executeQuery(sql);
                                        if (rs.next()) {
                                            JOptionPane.showMessageDialog(null, "This auteurID is already used");
                                        }
                                        else {
                                            String sql1 = "REPLACE INTO auteur(auteurID,auteur,anniversaire) VALUES(?,?,?)";
                                            PreparedStatement ps = connection.prepareStatement(sql1);
                                            ps.setString(1, auteurID);
                                            ps.setString(2, auteur);
                                            ps.setString(3, anniv);

                                            ps.executeUpdate();
                                            connection.close();
                                            JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });

                        f_addauthor.add(l1);
                        f_addauthor.add(l2);
                        f_addauthor.add(l3);

                        f_addauthor.add(F_auteurID);
                        f_addauthor.add(F_auteur);
                        f_addauthor.add(F_anniv);

                        f_addauthor.add(add_but);

                        f_addauthor.setSize(210, 180);
                        f_addauthor.setLayout(null);
                        f_addauthor.setVisible(true);
                        f_addauthor.setLocationRelativeTo(null);
                        f_addauthor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    }
                });

                JButton modify_author = new JButton("Modify Author");
                modify_author.setBounds(370, 100, 120, 40);

                f_authors.add(all_authors);
                f_authors.add(add_author);
                f_authors.add(modify_author);

                f_authors.setSize(550, 300);
                f_authors.setLayout(null);
                f_authors.setVisible(true);
                f_authors.setLocationRelativeTo(null);
            }
        });

        JButton admins = new JButton("Admin");
        admins.setBounds(70, 200, 120, 40);
        admins.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_admin = new JFrame("Admin");
                f_admin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JButton all_admin = new JButton("All Admins");
                all_admin.setBounds(70, 100, 120, 40);
                all_admin.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_viewauthors = new JFrame("All Admins");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM gestionnaire ";
                            ResultSet rs = stmt.executeQuery(sql);
                            int line = 0;
                            Object[][] data = new Object[100][6];
                            String[] columns = {"nom","prenom","email","tel","username","password"};
                            while (rs.next()) {
                                data[line][0] = rs.getString("nom");
                                data[line][1] = rs.getString("prenom");
                                data[line][2] = rs.getString("email");
                                data[line][3] = rs.getString("tel");
                                data[line][4] = rs.getString("username");
                                data[line][5] = rs.getString("password");
                                line += 1;
                            }

                            JTable table = new JTable(data, columns);
                            JScrollPane scroll = new JScrollPane(table);

                            f_viewauthors.setSize(900, 500);
                            f_viewauthors.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f_viewauthors.add(scroll, BorderLayout.CENTER);
                            f_viewauthors.setVisible(true);
                            f_viewauthors.setLocationRelativeTo(null);
                            connection.close();

                            table.setPreferredScrollableViewportSize(table.getPreferredSize());
                            table.getColumnModel().getColumn(0).setPreferredWidth(100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton add_admin = new JButton("Add Admin");
                add_admin.setBounds(220, 100, 120, 40);
                add_admin.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame f_addadmin = new JFrame("Add Admin");
                        f_addadmin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        JLabel l1, l2, l3, l4, l5, l6;
                        l1 = new JLabel("*Nom");
                        l1.setBounds(10, 50, 100, 30);

                        l2 = new JLabel("*Prenom");
                        l2.setBounds(60, 50, 100, 30);

                        l3 = new JLabel("email");
                        l3.setBounds(115, 50, 100, 30);

                        l4 = new JLabel("tel");
                        l4.setBounds(190, 50, 100, 30);

                        l5 = new JLabel("*Username");
                        l5.setBounds(240, 50, 100, 30);

                        l6 = new JLabel("*Password");
                        l6.setBounds(310, 50, 100, 30);

                        JTextField F_nom = new JTextField();
                        F_nom.setBounds(5, 80, 50, 30);

                        JTextField F_prenom = new JTextField();
                        F_prenom.setBounds(55, 80, 55, 30);

                        JTextField F_email = new JTextField();
                        F_email.setBounds(110, 80, 80, 30);

                        JTextField F_tel = new JTextField();
                        F_tel.setBounds(190, 80, 50, 30);

                        JTextField F_username = new JTextField();
                        F_username.setBounds(240, 80, 70, 30);

                        JTextField F_password = new JTextField();
                        F_password.setBounds(310, 80, 70, 30);

                        JButton add_but = new JButton("Add");
                        add_but.setBounds(160,140,80,25);
                        add_but.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String nom = F_nom.getText();
                                String prenom = F_prenom.getText();
                                String email = F_email.getText();
                                String tel = F_tel.getText();
                                String username = F_username.getText();
                                String password = F_password.getText();

                                if(nom.equals("")||prenom.equals("")||username.equals("")||password.equals("")){
                                    JOptionPane.showMessageDialog(null, "Please fill all * blanc");
                                }
                                else{
                                    Connection connection = connect();
                                    try {
                                        Statement stmt = connection.createStatement();
                                        String sql = ("SELECT * FROM gestionnaire WHERE username='" + username + "'");
                                        ResultSet rs = stmt.executeQuery(sql);
                                        if (rs.next()) {
                                            JOptionPane.showMessageDialog(null, "This username is already used");
                                        }
                                        else {
                                            String sql1 = "REPLACE INTO gestionnaire(nom, prenom, email, tel, username, password) VALUES(?,?,?,?,?,?)";
                                            PreparedStatement ps = connection.prepareStatement(sql1);
                                            ps.setString(1, nom);
                                            ps.setString(2, prenom);
                                            ps.setString(3, email);
                                            ps.setString(4, tel);
                                            ps.setString(5, username);
                                            ps.setString(6, password);

                                            ps.executeUpdate();
                                            connection.close();
                                            JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });

                        f_addadmin.add(l1);
                        f_addadmin.add(l2);
                        f_addadmin.add(l3);
                        f_addadmin.add(l4);
                        f_addadmin.add(l5);
                        f_addadmin.add(l6);

                        f_addadmin.add(F_nom);
                        f_addadmin.add(F_prenom);
                        f_addadmin.add(F_email);
                        f_addadmin.add(F_tel);
                        f_addadmin.add(F_username);
                        f_addadmin.add(F_password);

                        f_addadmin.add(add_but);

                        f_addadmin.setSize(400, 230);
                        f_addadmin.setLayout(null);
                        f_addadmin.setVisible(true);
                        f_addadmin.setLocationRelativeTo(null);
                        f_addadmin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    }
                });

                JButton modify_admin = new JButton("Modify Admin");
                modify_admin.setBounds(370, 100, 120, 40);

                f_admin.add(all_admin);
                f_admin.add(add_admin);
                f_admin.add(modify_admin);

                f_admin.setSize(550, 300);
                f_admin.setLayout(null);
                f_admin.setVisible(true);
                f_admin.setLocationRelativeTo(null);
            }
        });

        JButton category = new JButton("Category");
        category.setBounds(260, 200, 120, 40);
        category.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_category = new JFrame("Category");
                f_category.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JLabel l1, l2, l3, l4;
                l1 = new JLabel("Maximum number of books at a time");
                l1.setBounds(15, 20, 200, 30);

                l2 = new JLabel("Lending-period");
                l2.setBounds(15, 75, 200, 30);

                String[] max_number = {"5","8","10"};
                String[] period_number = {"2","3","4","5","6"};
                String[] max_number_book = {max_number[0]+" books", max_number[1]+" books", max_number[2]+" books"};
                String[] period = {period_number[0]+" weeks", period_number[1]+" weeks", period_number[2]+" weeks", period_number[3]+" weeks", period_number[4]+" weeks"};

                JComboBox comboBox1 = new JComboBox(max_number_book);
                comboBox1.setBounds(10, 45, 100, 25);

                JComboBox comboBox2 = new JComboBox(period);
                comboBox2.setBounds(10, 100, 100, 25);

                JButton ok_but = new JButton("OK");
                ok_but.setBounds(110,140,80,25);
                ok_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Integer max_num = comboBox1.getSelectedIndex();
                        Integer period = comboBox2.getSelectedIndex();

                        Connection connection = connect();
                        try {
                            String sql = "REPLACE INTO category(max_number, period_number, categoryID) VALUES(?,?,?)";
                            PreparedStatement ps = connection.prepareStatement(sql);
                            int i=0;
                            int j=0;
                            while(i<3) {
                                if(max_num==i) {
                                    ps.setInt(1, Integer.parseInt(max_number[i]));
                                }
                                i+=1;
                            }
                            while(j<5) {
                                if(period==j) {
                                    ps.setInt(2, Integer.parseInt(period_number[j]));
                                }
                                j+=1;
                            }
                            ps.setInt(3,1);

                            ps.executeUpdate();
                            connection.close();
                            JOptionPane.showMessageDialog(null, "Your modification is well registered");
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                Connection connection = connect();
                try {
                    Statement stmt = connection.createStatement();
                    String sql = "SELECT * " +
                            "FROM category ";
                    ResultSet rs = stmt.executeQuery(sql);
                    if(rs.next()){
                        l3 = new JLabel("Currently number : "+ rs.getInt("max_number") +" books");
                        l3.setBounds(125,45,200,30);
                        l4 = new JLabel("Currently number : "+ rs.getInt("period_number") +" weeks");
                        l4.setBounds(125,100,200,30);

                        f_category.add(l3);
                        f_category.add(l4);
                    }
                    else{
                        System.out.println("l'affichage est echoue !");
                    }
                    connection.close();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                f_category.add(l1);
                f_category.add(l2);

                f_category.add(comboBox1);
                f_category.add(comboBox2);

                f_category.add(ok_but);

                f_category.setSize(300, 230);
                f_category.setLayout(null);
                f_category.setVisible(true);
                f_category.setLocationRelativeTo(null);
                f_category.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });

        JButton red_list = new JButton("Red List");
        red_list.setBounds(450, 200, 120, 40);
        red_list.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f_redlist = new JFrame("Red List");
                f_redlist.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JButton red_list = new JButton("Red List");
                red_list.setBounds(70, 100, 120, 40);
                red_list.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_viewredlist = new JFrame("Red List");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM utilisateur " +
                                    "ORDER BY utilisateurID ASC";
                            ResultSet rs = stmt.executeQuery(sql);
                            int line = 0;
                            Object[][] data = new Object[100][7];
                            String[] columns = {"utilisateurID", "Nom", "Prenom", "e-mail", "tel", "username", "password"};
                            while (rs.next()) {
                                if(rs.getInt("redlist")==1) {
                                    data[line][0] = rs.getString("utilisateurID");
                                    data[line][1] = rs.getString("nom");
                                    data[line][2] = rs.getString("prenom");
                                    data[line][3] = rs.getString("email");
                                    data[line][4] = rs.getString("tel");
                                    data[line][5] = rs.getString("username");
                                    data[line][6] = rs.getString("password");
                                    line += 1;
                                }
                            }

                            JTable table = new JTable(data, columns);
                            JScrollPane scroll = new JScrollPane(table);

                            f_viewredlist.setSize(900, 500);
                            f_viewredlist.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f_viewredlist.add(scroll, BorderLayout.CENTER);
                            f_viewredlist.setVisible(true);
                            f_viewredlist.setLocationRelativeTo(null);
                            connection.close();

                            table.setPreferredScrollableViewportSize(table.getPreferredSize());
                            table.getColumnModel().getColumn(0).setPreferredWidth(100);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton add_list = new JButton("Add to Red List");
                add_list.setBounds(220, 100, 120, 40);
                add_list.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_addlist = new JFrame("Add User to Red List");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM utilisateur " +
                                    "ORDER BY utilisateurID ASC";
                            ResultSet rs = stmt.executeQuery(sql);
                            Object[][] data = new Object[100][7];
                            String[] user = new String[100];
                            int i=0;
                            final int[] n = {1};
                            while (rs.next()) {
                                if(rs.getInt("redlist")==0) {
                                    data[i][0] = rs.getString("utilisateurID");
                                    data[i][1] = rs.getString("nom");
                                    data[i][2] = rs.getString("prenom");
                                    data[i][3] = rs.getString("email");
                                    data[i][4] = rs.getString("tel");
                                    data[i][5] = rs.getString("username");
                                    data[i][6] = rs.getString("password");
                                    user[i] = " | "+data[i][0]+" | "+data[i][1]+" | "+data[i][2]+" | "+data[i][3]+" | "+data[i][4]+" | ";
                                    i+=1;
                                }
                                n[0]+=1;
                            }

                            JComboBox comboBox1 = new JComboBox(user);
                            comboBox1.setBounds(30, 85, 400, 25);

                            JLabel l;
                            l = new JLabel("select the user that you want to ban");
                            l.setBounds(35, 50, 300, 30);

                            JButton add_but = new JButton("Add");
                            add_but.setBounds(195,140,80,25);
                            add_but.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    int selected = comboBox1.getSelectedIndex();
                                    Connection connection = connect();
                                    try {
                                        String sql1 = "REPLACE INTO utilisateur(utilisateurID, nom, prenom, email, tel, username, password, redlist) VALUES(?,?,?,?,?,?,?,?)";
                                        PreparedStatement ps = connection.prepareStatement(sql1);
                                        while(n[0] >0) {
                                            if(selected==n[0]-1) {
                                                ps.setString(1, (String) data[n[0] - 1][0]);
                                                ps.setString(2, (String) data[n[0] - 1][1]);
                                                ps.setString(3, (String) data[n[0] - 1][2]);
                                                ps.setString(4, (String) data[n[0] - 1][3]);
                                                ps.setString(5, (String) data[n[0] - 1][4]);
                                                ps.setString(6, (String) data[n[0] - 1][5]);
                                                ps.setString(7, (String) data[n[0] - 1][6]);
                                                ps.setInt(8, 1);
                                                ps.executeUpdate();

                                                String sql2 = "REPLACE INTO history(date_column, livreID, utilisateurID, purpose) VALUES(?,?,?,?)";
                                                PreparedStatement ps1 = connection.prepareStatement(sql2);
                                                Date date = new Date();
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                                ps1.setString(1, format.format(date));
                                                ps1.setInt(2, 0);
                                                ps1.setString(3, (String) data[n[0] - 1][0]);
                                                ps1.setString(4, "Banned");
                                                ps1.executeUpdate();

                                                JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                            }
                                            n[0] -=1;
                                        }
                                        connection.close();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });

                            f_addlist.add(comboBox1);
                            f_addlist.add(l);
                            f_addlist.add(add_but);

                            f_addlist.setSize(470, 230);
                            f_addlist.setLayout(null);
                            f_addlist.setVisible(true);
                            f_addlist.setLocationRelativeTo(null);
                            f_addlist.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton remove_list = new JButton("Remove from Red List");
                remove_list.setBounds(370, 100, 120, 40);
                remove_list.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = connect();
                        try {
                            JFrame f_remove= new JFrame("Remove User from Red List");
                            Statement stmt = connection.createStatement();
                            String sql = "SELECT * " +
                                    "FROM utilisateur " +
                                    "ORDER BY utilisateurID ASC" ;
                            ResultSet rs = stmt.executeQuery(sql);
                            Object[][] data = new Object[100][7];
                            String[] user = new String[100];
                            int i=0;
                            final int[] n = {1};
                            while (rs.next()) {
                                if(rs.getInt("redlist")==1) {
                                    data[i][0] = rs.getString("utilisateurID");
                                    data[i][1] = rs.getString("nom");
                                    data[i][2] = rs.getString("prenom");
                                    data[i][3] = rs.getString("email");
                                    data[i][4] = rs.getString("tel");
                                    data[i][5] = rs.getString("username");
                                    data[i][6] = rs.getString("password");
                                    user[i] = " | " + (String) data[i][0] + " | " + (String) data[i][1] + " | " + (String) data[i][2] + " | " + (String) data[i][3] + " | " + (String) data[i][4] + " | ";
                                    i += 1;
                                }
                                n[0]+=1;
                            }

                            JComboBox comboBox1 = new JComboBox(user);
                            comboBox1.setBounds(30, 85, 400, 25);

                            JLabel l;
                            l = new JLabel("select the user that you want to lift the ban");
                            l.setBounds(35, 50, 400, 30);

                            JButton remove_but = new JButton("Remove");
                            remove_but.setBounds(195,140,80,25);
                            remove_but.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    int selected = comboBox1.getSelectedIndex();
                                    Connection connection = connect();
                                    try {
                                        String sql1 = "REPLACE INTO utilisateur(utilisateurID, nom, prenom, email, tel, username, password, redlist) VALUES(?,?,?,?,?,?,?,?)";
                                        PreparedStatement ps = connection.prepareStatement(sql1);
                                        while(n[0] >0) {
                                            if(selected== n[0]-1) {
                                                ps.setString(1, (String) data[n[0]-1][0]);
                                                ps.setString(2, (String) data[n[0]-1][1]);
                                                ps.setString(3, (String) data[n[0]-1][2]);
                                                ps.setString(4, (String) data[n[0]-1][3]);
                                                ps.setString(5, (String) data[n[0]-1][4]);
                                                ps.setString(6, (String) data[n[0]-1][5]);
                                                ps.setString(7, (String) data[n[0]-1][6]);
                                                ps.setInt(8, 0);

                                                ps.executeUpdate();
                                                JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                            }
                                            n[0] -=1;
                                        }
                                        connection.close();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });

                            f_remove.add(comboBox1);
                            f_remove.add(l);
                            f_remove.add(remove_but);

                            f_remove.setSize(470, 230);
                            f_remove.setLayout(null);
                            f_remove.setVisible(true);
                            f_remove.setLocationRelativeTo(null);
                            f_remove.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            connection.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                f_redlist.add(red_list);
                f_redlist.add(add_list);
                f_redlist.add(remove_list);

                f_redlist.setSize(550, 300);
                f_redlist.setLayout(null);
                f_redlist.setVisible(true);
                f_redlist.setLocationRelativeTo(null);
            }
        });

        f.add(books);
        f.add(users);
        f.add(authors);
        f.add(admins);
        f.add(category);
        f.add(red_list);
        f.setSize(640, 420);
        f.setLayout(null);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
    }

    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Loaded driver");
            Connection con = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\hatan\\Bibliotheque ver.11.08\\src\\bibliotheque.db");
            System.out.println("Connection to SQLite has been establised.");
            return con;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static class ButtonBorrow extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonBorrow(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                    JFrame f_borrow = new JFrame("Verification");
                    f_borrow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    JLabel l1, l2;
                    l1 = new JLabel("Please enter your USER ID :");
                    l1.setBounds(40, 15, 200, 30);
                    l2 = new JLabel("Please enter the ISBN :");
                    l2.setBounds(67, 50, 200, 30);

                    JTextField F_userID = new JTextField();
                    F_userID.setBounds(200, 15, 65, 30);
                    JTextField F_ISBN = new JTextField();
                    F_ISBN.setBounds(200, 50, 65, 30);

                    JButton ok_but = new JButton("OK");
                    ok_but.setBounds(160, 100, 80, 25);
                    ok_but.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String ISBN = F_ISBN.getText();
                            String userID = F_userID.getText();

                            if (ISBN.equals("")) {
                                JOptionPane.showMessageDialog(null, "Please enter the ISBN");
                            } else if (userID.equals("")) {
                                JOptionPane.showMessageDialog(null, "Please enter your USER ID");
                            } else {
                                Connection connection = connect();
                                try {
                                    Statement stmt = connection.createStatement();
                                    Statement stmt1 = connection.createStatement();
                                    Statement stmt2 = connection.createStatement();
                                    String sql = "SELECT * " +
                                                 "FROM livre " +
                                                 "WHERE utilisateurID is null " +
                                                 "AND ISBN='"+ISBN+"'" ;
                                    String sql1 = "SELECT redlist, COUNT(livre.utilisateurID)" +
                                                  "FROM livre " +
                                            "INNER JOIN utilisateur ON utilisateur.utilisateurID=livre.utilisateurID " +
                                                  "WHERE livre.utilisateurID='"+userID+"'";
                                    String sql2 = "SELECT * FROM category ";
                                    ResultSet rs = stmt.executeQuery(sql);
                                    ResultSet rs1 = stmt1.executeQuery(sql1);
                                    ResultSet rs2 = stmt2.executeQuery(sql2);

                                    if (rs.next()) {
                                        if(rs1.getInt("redlist")==0) {
                                            if (rs2.getInt("max_number") <= rs1.getInt("COUNT(livre.utilisateurID)")){
                                                JOptionPane.showMessageDialog(null, "You can't borrow any more books !");
                                                connection.close();
                                            }
                                            else{
                                                int livreID = rs.getInt("livreID");
                                                String titre = rs.getString("titre");
                                                String sql4 = "REPLACE INTO livre(livreID,titre,ISBN,utilisateurID) VALUES(?,?,?,?)";
                                                PreparedStatement ps = connection.prepareStatement(sql4);
                                                ps.setInt(1, livreID);
                                                ps.setString(2, titre);
                                                ps.setString(3, ISBN);
                                                ps.setInt(4, Integer.parseInt(userID));
                                                ps.executeUpdate();

                                                String sql5 = "REPLACE INTO history(date_column, livreID, utilisateurID, purpose) VALUES(?,?,?,?)";
                                                PreparedStatement ps1 = connection.prepareStatement(sql5);
                                                Date date = new Date();
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                                ps1.setString(1, format.format(date));
                                                ps1.setInt(2, livreID);
                                                ps1.setString(3, userID);
                                                ps1.setString(4, "Borrow");
                                                ps1.executeUpdate();

                                                connection.close();
                                                JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                            }
                                        }
                                        else{
                                            JOptionPane.showMessageDialog(null,"You are forbidden to borrow books !");
                                            connection.close();
                                        }
                                    }
                                    else {
                                        JOptionPane.showMessageDialog(null, "Verify USER ID / ISBN !");
                                        connection.close();
                                    }
                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });

                    f_borrow.add(F_userID);
                    f_borrow.add(F_ISBN);
                    f_borrow.add(ok_but);
                    f_borrow.add(l1);
                    f_borrow.add(l2);

                    f_borrow.setSize(400, 180);
                    f_borrow.setLayout(null);
                    f_borrow.setVisible(true);
                    f_borrow.setLocationRelativeTo(null);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static class ButtonReturn extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonReturn(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                JFrame f_return = new JFrame("Verification");
                f_return.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JLabel l1, l2;
                l1 = new JLabel("Please enter your USER ID :");
                l1.setBounds(40, 15, 200, 30);
                l2 = new JLabel("Please enter the ISBN :");
                l2.setBounds(67, 50, 200, 30);

                JTextField F_userID = new JTextField();
                F_userID.setBounds(200, 15, 65, 30);
                JTextField F_ISBN = new JTextField();
                F_ISBN.setBounds(200, 50, 65, 30);

                JButton ok_but = new JButton("OK");
                ok_but.setBounds(160, 100, 80, 25);
                ok_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String ISBN = F_ISBN.getText();
                        String userID = F_userID.getText();

                        if (ISBN.equals("")) {
                            JOptionPane.showMessageDialog(null, "Please enter the ISBN");
                        }
                        else if (userID.equals("")) {
                            JOptionPane.showMessageDialog(null, "Please enter your USER ID");
                        }
                        else {
                            Connection connection = connect();
                            try {
                                Statement stmt = connection.createStatement();
                                String sql = "SELECT * " +
                                             "FROM livre " +
                                             "ORDER BY livreID ASC" ;
                                ResultSet rs = stmt.executeQuery(sql);
                                while(rs.next()) {
                                    if (rs.getString("utilisateurID") != null) {
                                        if (rs.getString("utilisateurID").equals(userID)) {
                                            if (rs.getString("ISBN").equals(ISBN)) {
                                                int livreID = rs.getInt("livreID");
                                                String titre = rs.getString("titre");
                                                String sql1 = "REPLACE INTO livre(livreID,titre,ISBN,utilisateurID) VALUES(?,?,?,?)";
                                                PreparedStatement ps = connection.prepareStatement(sql1);
                                                ps.setInt(1, livreID);
                                                ps.setString(2, titre);
                                                ps.setString(3, ISBN);
                                                ps.setNull(4, Integer.parseInt(userID));
                                                ps.executeUpdate();

                                                String sql2 = "REPLACE INTO history(date_column, livreID, utilisateurID, purpose) VALUES(?,?,?,?)";
                                                PreparedStatement ps1 = connection.prepareStatement(sql2);
                                                Date date = new Date();
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                                ps1.setString(1, format.format(date));
                                                ps1.setInt(2, livreID);
                                                ps1.setString(3, userID);
                                                ps1.setString(4, "Return");
                                                ps1.executeUpdate();

                                                connection.close();
                                                JOptionPane.showMessageDialog(null, "Your modification is well registered");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                f_return.add(F_userID);
                f_return.add(F_ISBN);
                f_return.add(ok_but);
                f_return.add(l1);
                f_return.add(l2);

                f_return.setSize(400, 180);
                f_return.setLayout(null);
                f_return.setVisible(true);
                f_return.setLocationRelativeTo(null);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            }
            else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
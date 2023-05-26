/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package group1database;

import group1database.view.celladapter.TableActionCellEditor;
import group1database.view.celladapter.TableActionCellRender;
import group1database.view.celladapter.TableActionEvent;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author JuliusDev
 */
public class Dashboard extends javax.swing.JFrame implements OnSavedEvent {

    /**
     * Creates new form Dashboard
     *
     * @param Username
     * @param pwd
     * @param Role
     */
    public Dashboard(String Username, String pwd, String Role) {
        initComponents();
        this.setLocationRelativeTo(null);

        txtUsername.setText(Username + " | " + Role);

        txtUsername.setIcon(scaleImage((ImageIcon) txtUsername.getIcon(), 50, 50));
        txtExit.setIcon(scaleImage((ImageIcon) txtExit.getIcon(), 30, 30));
        txtLogout.setIcon(scaleImage((ImageIcon) txtLogout.getIcon(), 30, 30));

        if ("USER".equals(Role)){
             txtAddRecord.hide();
        }
        txtAddRecord.addMouseListener(hoverListener(txtAddRecord));
        txtExitWindow.addMouseListener(hoverListener(txtExitWindow));
        txtExit.addMouseListener(hoverListener(txtExit));
        txtLogout.addMouseListener(hoverListener(txtLogout));

        refreshTable("");
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onSaved() {
        refreshTable("");
    }

    private void refreshTable(String arg) {
        DefaultTableModel dtm = (new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID", "Full Name", "Course & Year", "Address", "Birthday", "Contact", "Action"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, true
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        int recordcount = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:group1.db")) {
                con.setAutoCommit(false);
                System.out.println("Opened Database Successfully!");

                try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM STUDINFO " + arg)) {

                    while (rs.next()) {
                        int StudID = rs.getInt("StudID");
                        String LastName = rs.getString("LastName");
                        String FirstName = rs.getString("FirstName");
                        String MiddleName = rs.getString("MiddleName");
                        String Course = rs.getString("Course");
                        int Year = rs.getInt("Year");
                        String Address = rs.getString("Address");
                        String Birthday = rs.getString("Birthday");
                        String Contact = rs.getString("Contact");
                        dtm.addRow(new Object[]{StudID, LastName + ", " + FirstName + ", " + MiddleName, Course + "-" + Year, Address, Birthday, Contact});
                        recordcount++;
                    }
                    txtTotalRecord.setText("Total Record: " + recordcount);
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
            System.exit(0);
        }

        TableActionEvent event = new TableActionEvent() {

            @Override
            public void onEdit(int row) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }

                DefaultTableModel model = (DefaultTableModel) table.getModel();

                String fullname[] = model.getValueAt(row, 1).toString().split(",");
                String course[] = model.getValueAt(row, 2).toString().split("-");
                String arg[] = {
                    model.getValueAt(row, 0).toString(),
                    fullname[0].trim(),
                    fullname[1].trim(),
                    fullname[2].trim(),
                    course[0].trim(),
                    course[1].trim(),
                    model.getValueAt(row, 3).toString(),
                    model.getValueAt(row, 4).toString(),
                    model.getValueAt(row, 5).toString()
                };

                AddEditRecord aer = new AddEditRecord(true, Dashboard.this, arg);
                aer.showAER();
            }

            @Override
            public void onDelete(int row) {

                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }

                DefaultTableModel model = (DefaultTableModel) table.getModel();

                JFrame frame = new JFrame("Logout");

                if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete item named: " + model.getValueAt(row, 1) + "?", "Logout",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                    try {
                        Class.forName("org.sqlite.JDBC");
                        try (Connection con = DriverManager.getConnection("jdbc:sqlite:group1.db")) {
                            String update = "DELETE FROM STUDINFO where StudID=" + model.getValueAt(row, 0) + "";
                            PreparedStatement pst = con.prepareStatement(update);

                            pst.execute();
                        }
                        JOptionPane.showMessageDialog(null, "Deleted Successfully!");
                    } catch (HeadlessException | ClassNotFoundException | SQLException e) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                    model.removeRow(row);

                }

            }

            @Override
            public void onView(int row) {
                System.out.println("View row : " + row);
            }
        };

        table.setModel(dtm);
        table.getColumnModel().getColumn(6).setCellRenderer(new TableActionCellRender());
        table.getColumnModel().getColumn(6).setCellEditor(new TableActionCellEditor(event));

    }

    private ImageIcon scaleImage(ImageIcon icon, int w, int h) {
        int nw = icon.getIconWidth();
        int nh = icon.getIconHeight();

        if (icon.getIconWidth() > w) {
            nw = w;
            nh = (nw * icon.getIconHeight()) / icon.getIconWidth();
        }

        if (nh > h) {
            nh = h;
            nw = (icon.getIconWidth() * nh) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(nw, nh, Image.SCALE_DEFAULT));
    }

    private MouseAdapter hoverListener(JLabel label) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (label == txtExitWindow || label == txtExit) {
                    JFrame frame = new JFrame("Exit");
                    if (JOptionPane.showConfirmDialog(frame, "Do you want to Exit?", "Exit",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                        System.exit(0);
                    }
                } else if (label == txtLogout) {
                    JFrame frame = new JFrame("Logout");
                    if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout?", "Logout",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                        Dashboard.this.setVisible(false);
                        Login login = new Login();
                        login.show();
                    }
                } else if (label == txtAddRecord) {
                    AddEditRecord aer = new AddEditRecord(false, Dashboard.this, null);
                    aer.showAER();
                }

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(Color.CYAN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(Color.LIGHT_GRAY);
            }
        };
    }

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        txtExitWindow = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        txtTotalRecord = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtProjectTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        navigationPanel = new javax.swing.JPanel();
        txtUsername = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtLogout = new javax.swing.JLabel();
        txtExit = new javax.swing.JLabel();
        txtAddRecord = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        contentPanel.setBackground(new java.awt.Color(44, 62, 80));

        txtExitWindow.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        txtExitWindow.setForeground(new java.awt.Color(255, 255, 255));
        txtExitWindow.setText("X");

        jPanel1.setPreferredSize(new java.awt.Dimension(500, 102));

        txtTotalRecord.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        txtTotalRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/all.png"))); // NOI18N
        txtTotalRecord.setText("Total Record:");
        txtTotalRecord.setIconTextGap(10);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtTotalRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(txtTotalRecord)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setPreferredSize(new java.awt.Dimension(500, 102));

        txtProjectTitle.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        txtProjectTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/teamwork.png"))); // NOI18N
        txtProjectTitle.setText("Group 1 - Database");
        txtProjectTitle.setIconTextGap(10);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(txtProjectTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(txtProjectTitle)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Full Name", "Course & Year", "Address", "Birthday", "Contact", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setRowHeight(40);
        jScrollPane1.setViewportView(table);

        txtSearch.setBackground(new java.awt.Color(44, 62, 80));
        txtSearch.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        txtSearch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search / Find :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 24))); // NOI18N
        txtSearch.setPreferredSize(new java.awt.Dimension(300, 80));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(contentPanelLayout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(txtExitWindow)
                .addGap(17, 17, 17))
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contentPanelLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(contentPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(txtExitWindow)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                .addContainerGap())
        );

        navigationPanel.setBackground(new java.awt.Color(248, 148, 6));

        txtUsername.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        txtUsername.setForeground(new java.awt.Color(0, 0, 0));
        txtUsername.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/user.png"))); // NOI18N
        txtUsername.setIconTextGap(20);

        txtLogout.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        txtLogout.setForeground(new java.awt.Color(0, 0, 0));
        txtLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/logout.png"))); // NOI18N
        txtLogout.setText("Logout");
        txtLogout.setIconTextGap(20);

        txtExit.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        txtExit.setForeground(new java.awt.Color(0, 0, 0));
        txtExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/exit.png"))); // NOI18N
        txtExit.setText("Exit");
        txtExit.setIconTextGap(20);

        txtAddRecord.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        txtAddRecord.setForeground(new java.awt.Color(0, 0, 0));
        txtAddRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/group1database/icon/add.png"))); // NOI18N
        txtAddRecord.setText("Add Record");
        txtAddRecord.setIconTextGap(20);

        javax.swing.GroupLayout navigationPanelLayout = new javax.swing.GroupLayout(navigationPanel);
        navigationPanel.setLayout(navigationPanelLayout);
        navigationPanelLayout.setHorizontalGroup(
            navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, navigationPanelLayout.createSequentialGroup()
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, navigationPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(navigationPanelLayout.createSequentialGroup()
                                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtExit, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtAddRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, navigationPanelLayout.createSequentialGroup()
                        .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        navigationPanelLayout.setVerticalGroup(
            navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigationPanelLayout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(txtUsername)
                .addGap(33, 33, 33)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtAddRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtExit, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(navigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(navigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            refreshTable(" WHERE FirstName LIKE '%"+txtSearch.getText()+"%' OR "+"LastName LIKE '%"+txtSearch.getText()+"%' OR "+"MiddleName LIKE '%"+txtSearch.getText()+"%' OR "
            +"Course LIKE '%"+txtSearch.getText()+"%' ");
        }
    }//GEN-LAST:event_txtSearchKeyPressed

    public static void showDash(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {
            new Dashboard(args[0], args[1], args[2]).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel navigationPanel;
    private javax.swing.JTable table;
    private javax.swing.JLabel txtAddRecord;
    private javax.swing.JLabel txtExit;
    private javax.swing.JLabel txtExitWindow;
    private javax.swing.JLabel txtLogout;
    private javax.swing.JLabel txtProjectTitle;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JLabel txtTotalRecord;
    private javax.swing.JLabel txtUsername;
    // End of variables declaration//GEN-END:variables

}

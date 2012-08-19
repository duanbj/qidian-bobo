/*  Copyright (c) 2012 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.qidianbobo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Main extends JFrame {
    private static final String CANCEL_TEXT = "停止发布起点更新";
    private static final long serialVersionUID = 7011088969424843486L;
    protected final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);
    private final static String CFG_FILE = System.getProperty("user.home") + File.separator + "qidian-bobo.cfg";
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(final String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (final ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (final InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (final IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (final UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frm;
                try {
                    frm = new Main();
                    frm.setResizable(false);
                    frm.setVisible(true);
                } catch (final IOException e) {
                    System.err.println("程序错误。请重新安装本软件。");
                }
            }
        });
    }

    private final PublisherManager publisher;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnAdd;

    private JButton btnRemove;

    JButton btnStart;

    JCheckBox chkQQ;

    JCheckBox chkSina;

    JCheckBox chkSohu;

    JCheckBox chkTwitter;

    private JScrollPane spNames;
    private JPanel pnlCategories;
    private CategoryButton btnCatXuan;
    private CategoryButton btnCatQi;
    private CategoryButton btnCatWu;
    private CategoryButton btnCatXian;
    private CategoryButton btnCatDu;
    private CategoryButton btnCatLi;
    private CategoryButton btnCatJun;
    private CategoryButton btnCatYou;
    private CategoryButton btnCatJing;
    private CategoryButton btnCatKe;
    private CategoryButton btnCatLing;
    private CategoryButton btnCatTong;
    private CategoryButton btnCatTu;
    private CategoryButton btnCatWen;
    private CategoryButton btnCatNv;
    private JLabel lblName;
    private JLabel lblPass;
    private JLabel lblUser;
    JList lstNames;
    private JPanel pnlConf;
    private JPanel pnlList;
    private JTextField tfName;
    JPasswordField tfPass;
    JTextField tfUser;

    public Main() throws IOException {
        setIconImage(ImageIO.read(getClass().getResource("/bobo.png")));
        setTitle("起点小说更新发布器");
        initComponents();
        setLocation((Main.SCREEN_SIZE.width - getWidth()) / 2, (Main.SCREEN_SIZE.height - getHeight()) / 2);

        this.publisher = new PublisherManager(this);
        this.lstNames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                checkSelection();
            }

        });
        this.tfName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(final DocumentEvent e) {
                checkName();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                checkName();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                checkName();
            }
        });
        try {
            final Properties props = new Properties();
            props.load(new FileReader(Main.CFG_FILE));

            String val = props.getProperty("user");
            if (Helper.isNotEmptyOrNull(val)) {
                this.tfUser.setText(val);
            }
            val = props.getProperty("pass");
            if (Helper.isNotEmptyOrNull(val)) {
                this.tfPass.setText(val);
            }
            val = props.getProperty("time");
            if (Helper.isNotEmptyOrNull(val)) {
                try {
                    this.publisher.setLastUpdateTime(Long.parseLong(val));
                } catch (final Exception e) {
                    // silent
                }
            }
            val = props.getProperty("chapter");
            if (Helper.isNotEmptyOrNull(val)) {
                try {
                    this.publisher.setLastChapterName(val);
                } catch (final Exception e) {
                    // silent
                }
            }
            val = props.getProperty("books");
            if (Helper.isNotEmptyOrNull(val)) {
                final StringTokenizer st = new StringTokenizer(val, "; ");
                while (st.hasMoreTokens()) {
                    final String book = st.nextToken();
                    if (!book.trim().isEmpty()) {
                        ((DefaultListModel) this.lstNames.getModel()).addElement(book);
                    }
                }
            }
            val = props.getProperty("qq");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.chkQQ.setSelected(true);
            } else {
                this.chkQQ.setSelected(false);
            }
            val = props.getProperty("sina");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.chkSina.setSelected(true);
            } else {
                this.chkSina.setSelected(false);
            }
            val = props.getProperty("sohu");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.chkSohu.setSelected(true);
            } else {
                this.chkSohu.setSelected(false);
            }
            val = props.getProperty("twitter");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.chkTwitter.setSelected(true);
            } else {
                this.chkTwitter.setSelected(false);
            }
            val = props.getProperty("catDu");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatDu.setSelected(true);
            } else {
                this.btnCatDu.setSelected(false);
            }
            val = props.getProperty("catJing");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatJing.setSelected(true);
            } else {
                this.btnCatJing.setSelected(false);
            }
            val = props.getProperty("catJun");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatJun.setSelected(true);
            } else {
                this.btnCatJun.setSelected(false);
            }
            val = props.getProperty("catKe");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatKe.setSelected(true);
            } else {
                this.btnCatKe.setSelected(false);
            }
            val = props.getProperty("catLi");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatLi.setSelected(true);
            } else {
                this.btnCatLi.setSelected(false);
            }
            val = props.getProperty("catLing");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatLing.setSelected(true);
            } else {
                this.btnCatLing.setSelected(false);
            }
            val = props.getProperty("catNv");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatNv.setSelected(true);
            } else {
                this.btnCatNv.setSelected(false);
            }
            val = props.getProperty("catQi");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatQi.setSelected(true);
            } else {
                this.btnCatQi.setSelected(false);
            }
            val = props.getProperty("catTong");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatTong.setSelected(true);
            } else {
                this.btnCatTong.setSelected(false);
            }
            val = props.getProperty("catTu");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatTu.setSelected(true);
            } else {
                this.btnCatTu.setSelected(false);
            }
            val = props.getProperty("catWen");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatWen.setSelected(true);
            } else {
                this.btnCatWen.setSelected(false);
            }
            val = props.getProperty("catWu");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatWu.setSelected(true);
            } else {
                this.btnCatWu.setSelected(false);
            }
            val = props.getProperty("catXian");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatXian.setSelected(true);
            } else {
                this.btnCatXian.setSelected(false);
            }
            val = props.getProperty("catXuan");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatXuan.setSelected(true);
            } else {
                this.btnCatXuan.setSelected(false);
            }
            val = props.getProperty("catYou");
            if (Helper.isNotEmptyOrNull(val) && val.equalsIgnoreCase("checked")) {
                this.btnCatYou.setSelected(true);
            } else {
                this.btnCatYou.setSelected(false);
            }

            System.out.println("成功读取设置文件'" + Main.CFG_FILE + "'。");
        } catch (final Throwable t) {
            // ignore
        }
        reset();
    }

    private void btnAddActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_btnAddActionPerformed
        if (!this.tfName.getText().isEmpty()) {
            final ListModel model = this.lstNames.getModel();
            ((DefaultListModel) model).addElement(this.tfName.getText());
            this.tfName.setText("");
        }
        checkManual();
        checkConfig();
    }// GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_btnRemoveActionPerformed

        final int[] selected = this.lstNames.getSelectedIndices();
        this.lstNames.getSelectionModel().clearSelection();
        final DefaultListModel model = (DefaultListModel) (this.lstNames.getModel());
        if ((selected != null) && (selected.length > 0)) {
            for (int i = selected.length - 1; i >= 0; i--) {
                model.remove(selected[i]);
            }
        }
        checkManual();
        checkConfig();
    }// GEN-LAST:event_btnRemoveActionPerformed

    private void btnStartActionPerformed(final ActionEvent evt) {// GEN-FIRST:event_btnStartActionPerformed
        this.btnStart.setEnabled(false);
        try {
            if (!stopPublishers()) {
                disableUI();
                pack();
                Main.EXECUTOR_SERVICE.execute(new Runnable() {
                    @Override
                    public void run() {
                        Main.this.publisher.start();
                    }
                });
            }
        } finally {
            this.btnStart.setEnabled(true);
        }
    }// GEN-LAST:event_btnStartActionPerformed

    private void disableUI() {
        this.tfUser.setEnabled(false);
        this.tfPass.setEnabled(false);
        // this.tfName.setEnabled(false);
        // this.lstNames.setEnabled(false);
        this.chkQQ.setEnabled(false);
        this.chkSina.setEnabled(false);
        this.chkSohu.setEnabled(false);
        this.chkTwitter.setEnabled(false);
        this.btnStart.setText(Main.CANCEL_TEXT);
    }

    public void cancel() {
        reset();
    }

    private void checkManual() {
        if (this.lstNames.getModel().getSize() == 0) {
            this.btnCatXuan.setEnabled(true);
            this.btnCatQi.setEnabled(true);
            this.btnCatWu.setEnabled(true);
            this.btnCatXian.setEnabled(true);
            this.btnCatDu.setEnabled(true);
            this.btnCatLi.setEnabled(true);
            this.btnCatJun.setEnabled(true);
            this.btnCatYou.setEnabled(true);
            this.btnCatJing.setEnabled(true);
            this.btnCatKe.setEnabled(true);
            this.btnCatLing.setEnabled(true);
            this.btnCatTong.setEnabled(true);
            this.btnCatTu.setEnabled(true);
            this.btnCatWen.setEnabled(true);
            this.btnCatNv.setEnabled(true);
            this.lstNames.setEnabled(false);
            this.lstNames.setBackground(Color.LIGHT_GRAY);
            this.lstNames.setOpaque(true);
        } else {
            this.btnCatXuan.setEnabled(false);
            this.btnCatQi.setEnabled(false);
            this.btnCatWu.setEnabled(false);
            this.btnCatXian.setEnabled(false);
            this.btnCatDu.setEnabled(false);
            this.btnCatLi.setEnabled(false);
            this.btnCatJun.setEnabled(false);
            this.btnCatYou.setEnabled(false);
            this.btnCatJing.setEnabled(false);
            this.btnCatKe.setEnabled(false);
            this.btnCatLing.setEnabled(false);
            this.btnCatTong.setEnabled(false);
            this.btnCatTu.setEnabled(false);
            this.btnCatWen.setEnabled(false);
            this.btnCatNv.setEnabled(false);
            this.lstNames.setEnabled(true);
            this.lstNames.setBackground(this.lblName.getBackground());
            this.lstNames.setOpaque(false);
        }
    }

    private boolean checkName() {
        final String text = this.tfName.getText();
        boolean okay = false;
        if (text.length() > 0) {
            okay = true;
            final DefaultListModel model = (DefaultListModel) (this.lstNames.getModel());
            final int size = model.getSize();
            for (int i = 0; i < size; i++) {
                if (text.equals(model.getElementAt(i))) {
                    okay = false;
                    break;
                }
            }
        }
        if (okay) {
            this.btnAdd.setEnabled(true);
            return true;
        } else {
            this.btnAdd.setEnabled(false);
            return false;
        }
    }

    // End of variables declaration//GEN-END:variables

    private void checkSelection() {

        if (Main.this.lstNames.getSelectedIndices().length == 0) {
            Main.this.btnRemove.setEnabled(false);
        } else {
            Main.this.btnRemove.setEnabled(true);
        }

    }

    private void formWindowClosing(final WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
        try {
            stopPublishers();
            save();
        } catch (final Throwable t) {
            t.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * 
     * @return true if publishers could be stopped
     */
    private boolean stopPublishers() {
        if (Main.CANCEL_TEXT.equals(this.btnStart.getText())) {
            this.publisher.cancel();
            reset();
            return true;
        } else {
            return false;
        }
    }

    public BlockingDeque<String> getNames() {
        final BlockingDeque<String> names = new LinkedBlockingDeque<String>();
        final DefaultListModel model = ((DefaultListModel) this.lstNames.getModel());
        final int size = model.getSize();
        for (int i = 0; i < size; i++) {
            names.add((String) model.get(i));
        }
        return names;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        this.pnlConf = new JPanel();
        this.lblUser = new JLabel();
        this.tfUser = new JTextField();
        this.lblPass = new JLabel();
        this.tfPass = new JPasswordField();
        this.chkTwitter = new JCheckBox();
        this.chkQQ = new JCheckBox();
        this.chkSina = new JCheckBox();
        this.chkSohu = new JCheckBox();
        this.pnlList = new JPanel();
        this.lblName = new JLabel();
        this.tfName = new JTextField();
        this.spNames = new JScrollPane();
        this.lstNames = new JList();
        this.btnAdd = new JButton();
        this.btnRemove = new JButton();
        this.btnCatXuan = new CategoryButton("玄", "玄幻", "发布所有玄幻小说更新");
        this.btnCatQi = new CategoryButton("奇", "奇幻", "发布所有奇幻小说更新");
        this.btnCatWu = new CategoryButton("武", "武侠", "发布所有武侠小说更新");
        this.btnCatXian = new CategoryButton("仙", "仙侠", "发布所有仙侠小说更新");
        this.btnCatDu = new CategoryButton("都", "都市", "发布所有都市小说更新");
        this.btnCatLi = new CategoryButton("历", "历史", "发布所有历史小说更新");
        this.btnCatJun = new CategoryButton("军", "军事", "发布所有军事小说更新");
        this.btnCatYou = new CategoryButton("游", "游戏", "发布所有游戏小说更新");
        this.btnCatJing = new CategoryButton("竞", "竞技", "发布所有竞技小说更新");
        this.btnCatKe = new CategoryButton("科", "科幻", "发布所有科幻小说更新");
        this.btnCatLing = new CategoryButton("灵", "灵异", "发布所有灵异小说更新");
        this.btnCatTong = new CategoryButton("同", "同人", "发布所有同人小说更新");
        this.btnCatTu = new CategoryButton("图", "图文", "发布所有图文小说更新");
        this.btnCatWen = new CategoryButton("文", "文学", "发布所有文学小说更新");
        this.btnCatNv = new CategoryButton("女", "女生", "发布所有女生小说更新");

        this.btnStart = new JButton();
        this.pnlCategories = new JPanel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        this.pnlConf.setBorder(BorderFactory.createEtchedBorder());

        this.lblUser.setLabelFor(this.tfUser);
        this.lblUser.setText("用户：");
        this.lblUser.setToolTipText("用户名或email地址");

        this.tfUser.setText("");

        this.lblPass.setLabelFor(this.tfPass);
        this.lblPass.setText("密码：");

        this.tfPass.setText("");

        this.chkTwitter.setText("Twitter");
        this.chkTwitter.setSelected(true);

        this.chkQQ.setText("QQ微博");
        this.chkQQ.setSelected(true);
        this.chkQQ.setEnabled(false);

        this.chkSina.setText("新浪微博");
        this.chkSina.setSelected(true);
        this.chkSina.setEnabled(false);

        this.chkSohu.setText("搜狐微博");
        this.chkSohu.setSelected(true);
        this.chkSohu.setEnabled(false);

        ActionListener checkCfgActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Main.CANCEL_TEXT.equals(Main.this.btnStart.getText())) {
                    checkConfig();
                }
            }
        };
        this.chkQQ.addActionListener(checkCfgActionListener);
        this.chkSina.addActionListener(checkCfgActionListener);
        this.chkSohu.addActionListener(checkCfgActionListener);
        this.chkTwitter.addActionListener(checkCfgActionListener);
        this.btnCatXuan.addActionListener(checkCfgActionListener);
        this.btnCatQi.addActionListener(checkCfgActionListener);
        this.btnCatWu.addActionListener(checkCfgActionListener);
        this.btnCatXian.addActionListener(checkCfgActionListener);
        this.btnCatDu.addActionListener(checkCfgActionListener);
        this.btnCatLi.addActionListener(checkCfgActionListener);
        this.btnCatJun.addActionListener(checkCfgActionListener);
        this.btnCatYou.addActionListener(checkCfgActionListener);
        this.btnCatJing.addActionListener(checkCfgActionListener);
        this.btnCatKe.addActionListener(checkCfgActionListener);
        this.btnCatLing.addActionListener(checkCfgActionListener);
        this.btnCatTong.addActionListener(checkCfgActionListener);
        this.btnCatTu.addActionListener(checkCfgActionListener);
        this.btnCatWen.addActionListener(checkCfgActionListener);
        this.btnCatNv.addActionListener(checkCfgActionListener);
        this.lstNames.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                checkConfig();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                checkConfig();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                checkConfig();
            }

        });

        final GroupLayout pnlConfLayout = new GroupLayout(this.pnlConf);
        this.pnlConf.setLayout(pnlConfLayout);
        pnlConfLayout
                .setHorizontalGroup(pnlConfLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                pnlConfLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                pnlConfLayout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                pnlConfLayout
                                                                        .createSequentialGroup()
                                                                        .addComponent(this.lblUser)
                                                                        .addPreferredGap(
                                                                                LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(this.tfUser))
                                                        .addGroup(
                                                                pnlConfLayout.createSequentialGroup()
                                                                        .addComponent(this.lblPass).addGap(10, 10, 10)
                                                                        .addComponent(this.tfPass))
                                                        .addGroup(
                                                                pnlConfLayout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                pnlConfLayout
                                                                                        .createParallelGroup(
                                                                                                GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                pnlConfLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                this.chkSina)
                                                                                                        .addPreferredGap(
                                                                                                                LayoutStyle.ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                this.chkTwitter))
                                                                                        .addGroup(
                                                                                                pnlConfLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                this.chkSohu)
                                                                                                        .addPreferredGap(
                                                                                                                LayoutStyle.ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                this.chkQQ)))
                                                                        .addGap(0, 0, Short.MAX_VALUE)))
                                        .addContainerGap()));
        pnlConfLayout.setVerticalGroup(pnlConfLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                pnlConfLayout
                        .createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(
                                pnlConfLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.lblUser)
                                        .addComponent(this.tfUser, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                pnlConfLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.tfPass, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.lblPass))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                pnlConfLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.chkSohu).addComponent(this.chkQQ))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                pnlConfLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.chkSina).addComponent(this.chkTwitter))));

        this.pnlList.setBorder(BorderFactory.createEtchedBorder());

        this.lblName.setText("小说");
        this.lblName.setToolTipText("小说名或小说ID");

        this.tfName.setText("");
        this.tfName.setToolTipText("小说名或小说ID");

        this.lstNames.setModel(new DefaultListModel());
        this.spNames.setViewportView(this.lstNames);

        this.btnAdd.setText("添");
        this.btnAdd.setToolTipText("添加小说");
        this.btnAdd.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        this.btnAdd.setEnabled(false);
        this.btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        this.btnRemove.setText("删");
        this.btnRemove.setToolTipText("删除选中的小说");
        this.btnRemove.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        this.btnRemove.setEnabled(false);
        this.btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        GridLayout lytCategories = new GridLayout(0, 2);
        this.pnlCategories.setLayout(lytCategories);
        this.pnlCategories.add(this.btnCatXuan);
        this.pnlCategories.add(this.btnCatQi);
        this.pnlCategories.add(this.btnCatWu);
        this.pnlCategories.add(this.btnCatXian);
        this.pnlCategories.add(this.btnCatDu);
        this.pnlCategories.add(this.btnCatLi);
        this.pnlCategories.add(this.btnCatJun);
        this.pnlCategories.add(this.btnCatYou);
        this.pnlCategories.add(this.btnCatJing);
        this.pnlCategories.add(this.btnCatKe);
        this.pnlCategories.add(this.btnCatLing);
        this.pnlCategories.add(this.btnCatTong);
        this.pnlCategories.add(this.btnCatTu);
        this.pnlCategories.add(this.btnCatWen);
        this.pnlCategories.add(this.btnCatNv);

        final GroupLayout pnlListLayout = new GroupLayout(this.pnlList);
        this.pnlList.setLayout(pnlListLayout);
        pnlListLayout.setHorizontalGroup(pnlListLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                pnlListLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                pnlListLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                                        pnlListLayout
                                                .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(this.lblName, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(this.btnAdd, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(this.btnRemove, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(this.pnlCategories, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                pnlListLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.tfName)
                                        .addComponent(this.spNames, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap()));
        pnlListLayout.setVerticalGroup(pnlListLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                pnlListLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                pnlListLayout
                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.lblName)
                                        .addComponent(this.tfName, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                pnlListLayout
                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.spNames, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                        .addGroup(
                                                pnlListLayout.createSequentialGroup().addComponent(this.btnAdd)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(this.btnRemove)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(this.pnlCategories)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        this.btnStart.setText("发布起点更新微博");
        this.btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.pnlConf, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(this.pnlList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(this.btnStart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addComponent(this.pnlConf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.pnlList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.btnStart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void reset() {
        setStatus(this.btnStart, Status.NORMAL);
        setStatus(this.chkQQ, Status.NORMAL);
        setStatus(this.chkTwitter, Status.NORMAL);
        setStatus(this.chkSina, Status.NORMAL);
        setStatus(this.chkSohu, Status.NORMAL);
        this.btnStart.setText("发布起点更新微博");
        this.tfUser.setEnabled(true);
        this.tfPass.setEnabled(true);
        this.tfName.setEnabled(true);
        this.lstNames.setEnabled(true);
        this.chkQQ.setEnabled(true);
        this.chkTwitter.setEnabled(true);
        this.chkSina.setEnabled(true);
        this.chkSohu.setEnabled(true);

        checkName();
        checkManual();
        checkSelection();
        checkConfig();
    }

    public void save() {
        final Properties props = new Properties();
        props.put("user", this.tfUser.getText());
        props.put("pass", this.tfPass.getText());
        final StringBuffer sb = new StringBuffer();
        final DefaultListModel model = ((DefaultListModel) this.lstNames.getModel());
        final int size = model.getSize();
        for (int i = 0; i < size; i++) {
            sb.append(model.get(i)).append("; ");
        }
        props.put("books", sb.toString());
        if (this.chkQQ.isSelected()) {
            props.put("qq", "checked");
        }
        if (this.chkSina.isSelected()) {
            props.put("sina", "checked");
        }
        if (this.chkSohu.isSelected()) {
            props.put("sohu", "checked");
        }
        if (this.chkTwitter.isSelected()) {
            props.put("twitter", "checked");
        }
        if (this.btnCatDu.isSelected()) {
            props.put("catDu", "checked");
        }
        if (this.btnCatJing.isSelected()) {
            props.put("catJing", "checked");
        }
        if (this.btnCatJun.isSelected()) {
            props.put("catJun", "checked");
        }
        if (this.btnCatKe.isSelected()) {
            props.put("catKe", "checked");
        }
        if (this.btnCatLi.isSelected()) {
            props.put("catLi", "checked");
        }
        if (this.btnCatLing.isSelected()) {
            props.put("catLing", "checked");
        }
        if (this.btnCatNv.isSelected()) {
            props.put("catNv", "checked");
        }
        if (this.btnCatQi.isSelected()) {
            props.put("catQi", "checked");
        }
        if (this.btnCatTong.isSelected()) {
            props.put("catTong", "checked");
        }
        if (this.btnCatTu.isSelected()) {
            props.put("catTu", "checked");
        }
        if (this.btnCatWen.isSelected()) {
            props.put("catWen", "checked");
        }
        if (this.btnCatWu.isSelected()) {
            props.put("catWu", "checked");
        }
        if (this.btnCatXian.isSelected()) {
            props.put("catXian", "checked");
        }
        if (this.btnCatXuan.isSelected()) {
            props.put("catXuan", "checked");
        }
        if (this.btnCatYou.isSelected()) {
            props.put("catYou", "checked");
        }
        if (this.publisher.getLastUpdateTime() != -1) {
            props.put("time", String.valueOf(this.publisher.getLastUpdateTime()));
            props.put("chapter", String.valueOf(this.publisher.getLastChapterName()));
        }

        try {
            final FileWriter writer = new FileWriter(Main.CFG_FILE, false);
            props.store(writer, null);
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void checkConfig() {
        if ((this.chkQQ.isSelected() || this.chkTwitter.isSelected() || this.chkSina.isSelected() || this.chkSohu
                .isSelected())
                && (!getNames().isEmpty() || (this.btnCatDu.isSelected() || this.btnCatJing.isSelected()
                        || this.btnCatJun.isSelected() || this.btnCatKe.isSelected() || this.btnCatLi.isSelected()
                        || this.btnCatLing.isSelected() || this.btnCatNv.isSelected() || this.btnCatQi.isSelected()
                        || this.btnCatTong.isSelected() || this.btnCatTu.isSelected() || this.btnCatWen.isSelected()
                        || this.btnCatWu.isSelected() || this.btnCatXian.isSelected() || this.btnCatXuan.isSelected() || this.btnCatYou
                            .isSelected()))) {
            this.btnStart.setEnabled(true);
        } else {
            this.btnStart.setEnabled(false);
        }
    }

    public boolean isCategorySelected(String categoryName) {
        if (this.btnCatDu.isSelected() && categoryName.contains(this.btnCatDu.getKey())) {
            return true;
        } else if (this.btnCatJing.isSelected() && categoryName.contains(this.btnCatJing.getKey())) {
            return true;
        } else if (this.btnCatJun.isSelected() && categoryName.contains(this.btnCatJun.getKey())) {
            return true;
        } else if (this.btnCatKe.isSelected() && categoryName.contains(this.btnCatKe.getKey())) {
            return true;
        } else if (this.btnCatLi.isSelected() && categoryName.contains(this.btnCatLi.getKey())) {
            return true;
        } else if (this.btnCatLing.isSelected() && categoryName.contains(this.btnCatLing.getKey())) {
            return true;
        } else if (this.btnCatNv.isSelected() && categoryName.contains(this.btnCatNv.getKey())) {
            return true;
        } else if (this.btnCatQi.isSelected() && categoryName.contains(this.btnCatQi.getKey())) {
            return true;
        } else if (this.btnCatTong.isSelected() && categoryName.contains(this.btnCatTong.getKey())) {
            return true;
        } else if (this.btnCatTu.isSelected() && categoryName.contains(this.btnCatTu.getKey())) {
            return true;
        } else if (this.btnCatWen.isSelected() && categoryName.contains(this.btnCatWen.getKey())) {
            return true;
        } else if (this.btnCatWu.isSelected() && categoryName.contains(this.btnCatWu.getKey())) {
            return true;
        } else if (this.btnCatXian.isSelected() && categoryName.contains(this.btnCatXian.getKey())) {
            return true;
        } else if (this.btnCatXuan.isSelected() && categoryName.contains(this.btnCatXuan.getKey())) {
            return true;
        } else if (this.btnCatYou.isSelected() && categoryName.contains(this.btnCatYou.getKey())) {
            return true;
        }
        return false;
    }

    public void setStatus(final JComponent cmp, final Status status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case PREPARING:
                        cmp.setBackground(Color.YELLOW);
                        break;
                    case FAILED:
                        cmp.setBackground(Color.RED);
                        break;
                    case PUBLISHING:
                        cmp.setBackground(Color.ORANGE);
                        break;
                    case WAITING:
                        cmp.setBackground(Color.GREEN);
                        break;
                    case AUTHENTICATING:
                        cmp.setBackground(Color.BLUE);
                        break;
                    case NORMAL:
                    default:
                        cmp.setBackground(Main.this.pnlConf.getBackground());
                }
            }
        });
    }
}

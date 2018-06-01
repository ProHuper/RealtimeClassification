import org.apache.commons.io.output.FileWriterWithEncoding;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class MainWindow extends JFrame{

    private JPanel panel1;
    private JTextArea resultMessage;
//    private JButton resButton;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JButton buttonRT;
    private JTextField inputDate;
//    private JButton buttonInsret;
    private JButton buttonSpark;
    private JButton buttonStop;
    private JButton buttonSZ;
    private JButton buttonSK;
    private JScrollPane messagePane;
    private JButton buttonScrapy;
//    private ResDialog resDialog;
    private String hintText = "输入格式:%Y%m%d";
    private Thread threadConsumer;
    private Thread threadProducer;
    private boolean started = true;
//    private ArrayList<String> resList = new ArrayList<>();


    public MainWindow() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        resDialog = new ResDialog(this, "结果统计对话框");
//        buttonInsret.addActionListener(e -> {
//            resDialog.setModal(true);
//            resDialog.setVisible(true);
//        });

        buttonSZ.addActionListener(e -> {
            Thread thread = new Thread(() -> {
                execCommand(Commands.DELETE);
                execCommand(Commands.START_ZOOKEEPER);
            });
            thread.start();
        });

        buttonSK.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(null, "开启kafka前请确认开启zookeeper", "是否继续", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION){
                Thread thread = new Thread(() -> execCommand(Commands.START_KAFKA));
                thread.start();
            }
        });

        buttonRT.addActionListener(e -> {
            String model = (String) comboBox2.getSelectedItem();
            int option = JOptionPane.showConfirmDialog(null, "确认重新训练", "是否继续", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION){
                Thread thread = new Thread(() -> execCommand("python", "E:\\finalwork\\scripts\\train.py", model));
                thread.start();
            }
        });

        buttonSpark.addActionListener(e -> {
            started = true;
            String model = (String) comboBox2.getSelectedItem();
            threadConsumer = new Thread(() -> execCommand("python", "E:\\finalwork\\scripts\\streaming_process.py", model));
            threadConsumer.start();
        });

        buttonScrapy.addActionListener(e -> {
            String date = inputDate.getText();
            int option = JOptionPane.showConfirmDialog(null, "开始爬取前请确认已开启Spark", "是否继续", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION){
//                threadProducer = new Thread(() -> execCommand(Commands.START_PRODUCER));
                threadProducer = new Thread(() -> {
                    writeDate(date);
                    execCommand(Commands.START_PRODUCER);
                });
                threadProducer.start();
            }
        });

        buttonStop.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(null, "是否确认停止？", "是否继续", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION){
                Thread thread = new Thread(() -> {
                    started = false;
                    threadConsumer.stop();
                    threadProducer.stop();
                    resultMessage.append("爬取停止......");
                    Command.KillProcess(Commands.FIND_ONE);
                    Command.KillProcess(Commands.FIND_TWO);
                });
                thread.start();
            }
        });

        inputDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String temp = inputDate.getText();
                if(temp.equals(inputDate.getText())) {
                    inputDate.setText("");
                    inputDate.setForeground(Color.BLACK);
                }
            }
        });

        inputDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String temp = inputDate.getText();
                if(temp.equals("")) {
                    inputDate.setForeground(Color.GRAY);
                    inputDate.setText(hintText);
                }
            }
        });
    }

    private void writeDate(String info){
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter("E:\\finalwork\\other_file\\date_file"));
            bf.write(info);
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        JFrame frame = new JFrame("实时网页分类系统控制平台");
        frame.setMinimumSize(new Dimension(700,500));
        frame.setContentPane(new MainWindow().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    public void execCommand(String ... commands){
        try {
            new ProcessExecutor().command(commands).redirectOutput(new LogOutputStream() {
                @Override
                protected void processLine(String line) {
//                    System.out.println(line);
                    if(started){
                        resultMessage.append(line);
                        resultMessage.append("\n");
//                        if(line.contains("--------------->")){
//                            resList.add(line);
//                        }
                        JScrollBar bar = messagePane.getVerticalScrollBar();
                        bar.setValue(bar.getMaximum());
                    }
                }
            }).destroyOnExit().execute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}

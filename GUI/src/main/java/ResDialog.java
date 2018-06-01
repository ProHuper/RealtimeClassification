import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

public class ResDialog extends JDialog implements ActionListener{
    private JList<String> list;
    private JTable jTable;
    Vector<String> columnNames = new Vector<>();
    Vector<Vector<String>> data = new Vector<>();

    ResDialog(JFrame f,String s){
        super(f,s);
        setSize(500,400);

        initTableData();
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);
        list = new JList<>();
        jTable = new JTable(data, columnNames){
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            public String getToolTipText(MouseEvent e) {
                int row = jTable.rowAtPoint(e.getPoint());
                int col= jTable.columnAtPoint(e.getPoint());
                String tip=null;
                if(row>-1 && col>-1){
                    Object value = jTable.getValueAt(row, col);
                    if(null!=value && !"".equals(value))
                        tip = value.toString();//悬浮显示单元格内容
                }
                return tip;
            }
        };

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JTextField.CENTER);
        jTable.setRowHeight(30);
        jTable.getColumn("分类结果").setCellRenderer(renderer);
        jTable.getColumnModel().getColumn(0).setMaxWidth(100);

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(jTable);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        jTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    int index = jTable.getSelectedRow();
                    DefaultTableModel model = (DefaultTableModel) jTable.getModel();
                    int option = JOptionPane.showConfirmDialog(null, "是否将此条数据加入训练数据", "是否继续", JOptionPane.YES_NO_OPTION);
                    if(option == JOptionPane.YES_OPTION){
                        model.removeRow(index);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void initTableData(){
        columnNames.add("分类结果");
        columnNames.add("详细信息");
        data.add(addRow("1", "dsggggggggggggggggggggggggggggggggggggggggggggg"));
        data.add(addRow("0", "dsggggggggggggggggggggggggggggggggggggggggggggg"));
        data.add(addRow("9", "dsggggggggggggggggggggggggggggggggggggggggggggg"));
        data.add(addRow("5", "dsggggggggggffffffffffffffffffffffgffffddddddddddddgggggggggggggggggggggggggggggggggg"));
        data.add(addRow("6", "dsggggggggggffffffffffffffffffffffggggggggggggggggggggggggggggggggggg"));
    }

    private Vector<String> addRow(String type, String info){
        Vector<String> temp = new Vector<>();
        temp.add(type);
        temp.add(info);
        return temp;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

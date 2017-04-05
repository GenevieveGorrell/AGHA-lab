
package gen.nlp.svd;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import gen.nlp.svd.Lab;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.TimerTask;
import java.util.Timer;

public class LabGui {
    JFrame frame = new JFrame("GHALab 0.2");
    Container contentPane = frame.getContentPane();
    SpringLayout layout = new SpringLayout();
    JTextArea commandField = new JTextArea(30, 50);
    JScrollPane scrollCommandPane = new JScrollPane(commandField,
						    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JTextArea displayField = new JTextArea(30, 20);
    JScrollPane scrollDisplayPane = new JScrollPane(displayField,
						    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    int curserpos=0;
    public LabThread l = null;
    public boolean runningProcess = false;
    public boolean requestStop = false;
    Lab lab = new Lab();


    /** Class constructor. */    
    public LabGui() {
    }

    class LabThread extends Thread {
        LabGui labgui;
        String input;
        
        public LabThread(LabGui labgui, String input){
            this.labgui = labgui;
            this.input = input;
        }
        
        public void run() {
            this.labgui.lab.parseInput(input);
            this.labgui.runningProcess = false;
            this.labgui.requestStop=false;
	    this.labgui.commandField.append("LAB> ");
	    this.labgui.curserpos=this.labgui.commandField.getText().length();
	    this.labgui.commandField.setCaretPosition(curserpos);
        }
    }
    
    public void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentPane.setLayout(layout);

	commandField.setEditable(true);
        contentPane.add(scrollCommandPane);

	displayField.setEditable(false);
        contentPane.add(scrollDisplayPane);

	layout.putConstraint(SpringLayout.WEST, scrollCommandPane, 5, SpringLayout.WEST, contentPane);
	layout.putConstraint(SpringLayout.WEST, scrollDisplayPane, 5, SpringLayout.EAST, scrollCommandPane);
	layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, scrollDisplayPane);
	layout.putConstraint(SpringLayout.SOUTH, scrollDisplayPane, -5, SpringLayout.SOUTH, contentPane);
	layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, scrollCommandPane);
	layout.putConstraint(SpringLayout.NORTH, scrollDisplayPane, -5, SpringLayout.NORTH, contentPane );
	layout.putConstraint(SpringLayout.NORTH, contentPane, 5, SpringLayout.NORTH, scrollCommandPane);

        frame.pack();
        frame.setVisible(true);
	commandField.getDocument().addDocumentListener(new MyDocumentListener());
    }

    class MyDocumentListener implements DocumentListener {
	public void insertUpdate(DocumentEvent e) {
	    Document doc = (Document)e.getDocument();
	    try {
		if(doc.getText(doc.getLength()-1, 1).equals("\n")){
		    go(doc.getText(curserpos, doc.getLength()-curserpos));
		}
	    } catch(Exception ex){
		System.out.println(ex);
	    }
	}
	public void removeUpdate(DocumentEvent e) {
	}
	public void changedUpdate(DocumentEvent e) {
	    //Plain text components don't fire these events
	}
    }

    public void go(String input){
	displayField.append(input);
        this.runningProcess = false;
	curserpos=this.commandField.getText().length();
	this.commandField.setCaretPosition(curserpos);
	if(this.runningProcess==true){
	    this.requestStop=true;
	} else if (!input.equals("exit") && !input.equals("quit") && !input.equals("q")) {
	    this.l = new LabThread(this, input);
	    this.runningProcess = true;
	    this.l.start();
	} else {
	    System.exit(0);
	}
    }
   
    public static void main(String[] args) {
	final LabGui myLabGui = new LabGui();

        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                myLabGui.createAndShowGUI();
            }
	    });*/

	myLabGui.createAndShowGUI();

	String welcomestring = (
			    "\n  Welcome to GHA Lab v0.2"+
			    "\n  G. Gorrell 2006"+
			    "\n"+
			    "\n  GHA Lab demonstrates singular value decomposition, eigen decomposition"+
			    "\n  and the Generalised Hebbian Algorithm. It allows textual corpora and sets"+
			    "\n  of vectors to be read in in various formats, manipulated and compared."+
			    "\n  Type \"help\" to get help\n\n");
	myLabGui.commandField.append(welcomestring);
	//myLabGui.go();
        myLabGui.commandField.append("LAB> ");
	myLabGui.curserpos=myLabGui.commandField.getText().length();
	myLabGui.commandField.setCaretPosition(myLabGui.curserpos);
    }
}

// QuestionPopup.java
// $Id: QuestionPopup.java,v 1.1 2010/06/15 12:20:38 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class QuestionPopup extends Dialog implements ActionListener {

    protected Button yesB = null;
    protected Button noB  = null;

    protected final static String defaultYesMsg = "Yes";
    protected final static String defaultNoMsg  = "No";
    protected final static String defaultTitle  = "Question";

    protected AnswerListener answerListener = null;

    public void registerAnswerListener (AnswerListener listener) {
	answerListener = listener;
    }

    /**
     * ActionListsner implementation - One of our button was fired.
     * @param evt The ActionEvent.
     */

    public void actionPerformed(ActionEvent evt) {
	if (answerListener != null) {
	    String command = evt.getActionCommand();
	    if (command.equals("yes"))
		answerListener.questionAnswered(this, answerListener.YES);
	    else if (command.equals("no"))
		answerListener.questionAnswered(this, answerListener.NO);
	}
    }

    public QuestionPopup(Frame parent, String question) {
	this(parent, defaultTitle, question, 
	     defaultYesMsg, defaultNoMsg, true);
    }

    public QuestionPopup(Frame parent, String question, boolean modal) {
	this(parent, defaultTitle, question, 
	     defaultYesMsg, defaultNoMsg, modal);
    }

    public QuestionPopup (Frame parent, String title, 
			  String question, boolean modal) 
    {
	this(parent, title, question, defaultYesMsg, defaultNoMsg, modal);
    }

    public QuestionPopup (Frame parent, String title, 
			  String question, String yes, String no,
			  boolean modal) 
    {
	super(parent, title, modal);
	Button yesB      = new Button(yes);
	yesB.setActionCommand("yes");
	yesB.addActionListener(this);
	Button noB       = new Button(no);
	noB.addActionListener(this);
	noB.setActionCommand("no");
	Label  questionL = new Label(question);

	Panel pq = new Panel();
	pq.add(questionL);

	BorderPanel pb = new BorderPanel(BorderPanel.IN, 2);
	pb.setLayout(new FlowLayout());
	pb.add(yesB);
	pb.add(noB);

	add(pq, "Center");
	add(pb, "South");
	pack();
    }

}

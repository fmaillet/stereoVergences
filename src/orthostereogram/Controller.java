/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthostereogram;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author Fred
 */
public class Controller extends JFrame implements ActionListener {
    
    JButton start ;
    
    public Controller () {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle ("orthoStéréogram (F. Maillet - v0.0.1)") ;
        setLayout(null);
        getContentPane().setBackground( Color.CYAN) ; 
               
        
        //Démarrer convergence
        start = new JButton ("Démarrer") ;
        start.setBounds(100, 30, 100, 30);
        start.addActionListener(this);
        getContentPane().add(start) ;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource() ;
        if (o == start) {
            DisplayStereogram stereo = new DisplayStereogram () ;
            stereo.setAppearence () ;
            stereo.setVisible(true);
        };
    }
}


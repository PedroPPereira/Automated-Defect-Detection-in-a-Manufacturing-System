/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import jade.core.Agent;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ConsoleAgent extends Agent{

    ConsoleFrame myFrame;
    
    @Override
    protected void setup() {
        this.myFrame = new ConsoleFrame(this);
        this.myFrame.setVisible(true);
    }
    
    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}

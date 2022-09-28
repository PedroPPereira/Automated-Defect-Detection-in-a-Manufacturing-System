/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Libraries;

import coppelia.CharWA;
import coppelia.IntW;
import coppelia.remoteApi;
import jade.core.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class SimTransportLibrary implements ITransport {

    remoteApi sim;
    int clientID;
    Agent myAgent;
    final long timeout = 60000;

    @Override
    public void init(Agent a) {
        this.myAgent = a;
        this.sim = new remoteApi();
        this.clientID = sim.simxStart("127.0.0.1", 20002, true, true, 5000, 5);
        if (this.clientID != -1) {
            System.out.println(this.myAgent.getAID().getLocalName() + " initialized communication with the simulation.");            
        }
    }

    @Override
    public String[] getSkills() {
        String[] skills = new String[1];
        skills[0] = Utilities.Constants.SK_MOVE;
        return skills;
    }

    @Override
    public boolean executeMove(String origin, String destination, String productID) {
        sim.simxSetStringSignal(clientID, "Move", new CharWA(productID + "#" + origin + "#" + destination), sim.simx_opmode_blocking);
        IntW opRes = new IntW(-1);
        long startTime = System.currentTimeMillis();
        while ((opRes.getValue() != 1) && (System.currentTimeMillis() - startTime < timeout)) {
            sim.simxGetIntegerSignal(clientID, "Move", opRes, sim.simx_opmode_blocking);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimResourceLibrary.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sim.simxClearIntegerSignal(clientID, "Move", sim.simx_opmode_blocking);
        if (opRes.getValue() == 1) {
            return true;
        }
        return false;
    }

}

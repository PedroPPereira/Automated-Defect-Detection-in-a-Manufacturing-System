/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Libraries;

import jade.core.Agent;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public interface ITransport {
    public void init(Agent myAgent);
    public String[] getSkills();
    public boolean executeMove(String origin, String destination, String ProductID);
}

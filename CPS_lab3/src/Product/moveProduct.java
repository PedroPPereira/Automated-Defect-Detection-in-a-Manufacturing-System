/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Product;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pmper
 */
public class moveProduct extends AchieveREInitiator {
        
        public moveProduct(Agent a, ACLMessage msg){
            super(a, msg);
        }
        
        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }
 
        @Override
        protected void handleRefuse(ACLMessage msg) {
            System.out.println(myAgent.getLocalName() + ": REFUSE message received");
            //resend request later (AVG not available)
            if(msg.getContent().equals("resend") ) {
                try { Thread.sleep(5000); } 
                catch (InterruptedException ex) {
                    Logger.getLogger(searchResource.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("[ERROR]" + myAgent.getLocalName() + ": re-sending REQUEST messages\n");
                ((ProductAgent)myAgent).addBehaviour(new moveProduct(((ProductAgent)myAgent), ((ProductAgent)myAgent).msgMP));
            }
            
        }
        
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            if(inform.getContent().equals("ERROR")) {
                System.out.println("[ERROR]" + myAgent.getLocalName() + ": MOVE skill failed, re-doing skill\n");
                //send msg to restart the MOVE skill
                ((ProductAgent)myAgent).addBehaviour(new moveProduct(((ProductAgent)myAgent), ((ProductAgent)myAgent).msgMP));
            }
            //update new product location
            else {
                ((ProductAgent)myAgent).location = inform.getContent();
                //send skill to resource choosen 
                ((ProductAgent)myAgent).msgES.setContent(((ProductAgent)myAgent).currentSkill);
            }
        }        
    }

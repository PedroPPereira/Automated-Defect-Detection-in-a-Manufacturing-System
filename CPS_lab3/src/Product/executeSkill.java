 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Product;

import jade.proto.AchieveREInitiator;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
/**
 *
 * @author pmper
 */
public class executeSkill extends AchieveREInitiator {
        
        public executeSkill(Agent a, ACLMessage msg){
            super(a, msg);
        }
        
        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }
               
        
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            if( inform.getContent().equals("NOK") ) {
                ((ProductAgent) myAgent).skillCounter -= 3;
            }
//            else if( !inform.getContent().equals("SUCCESS") ) {
//                System.out.println("[ERROR]" + myAgent.getLocalName() + ": SKILL failed, ending earlier\n");
//                //ending earlier the trajectory because the resource failed
//                ((ProductAgent) myAgent).failure = inform.getContent(); //contains resource name
//                //update skill counter to go directily to the last skill (going to the Sink by the Operator)
//                ((ProductAgent) myAgent).skillCounter = ((ProductAgent) myAgent).executionPlan.size()-2;
//            }
            //start new sequence, for a new skill
            ((ProductAgent) myAgent).startNewSequence();
        }        
        
        
    }

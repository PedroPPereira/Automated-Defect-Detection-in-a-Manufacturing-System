/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Product;

import Utilities.DFInteraction;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author pmper
 */

//------------------------------------------ContractNetInitiator------------------------------------------//
public class searchResource extends ContractNetInitiator {
        
        public searchResource(Agent a, ACLMessage msg){
            super(a, msg);
        }
        
        /***********************************************
        @desc: chooses best resource to do the skill
        @param: VECTOR of messages with all the resources responses
        @return: index of resoruce to ACCEPT
        ***********************************************/
        private int bestResourceAlgorithm(Vector resp) {
            int bestValue = -1; //will contain highest value recieved by the resource
            int bestIndex = -1; //index associated to the best resource
            int currValue = 0; 
            ACLMessage auxMsg; //current message  
            String[] contentRA;
            String loc; 
            int hour, rate;
            
            for(int i = 0; i <= resp.size() - 1; i++) {
                auxMsg = (ACLMessage)resp.get(i);
                //if the agent REFUSED, then continue (dont update)
                if(auxMsg.getPerformative() == ACLMessage.REFUSE) continue;
                //get msg and parameters
                contentRA = auxMsg.getContent().split("/"); 
                loc = contentRA[0]; //resource location
                hour = Integer.parseInt(contentRA[1]); //resource cost per hour
                rate = Integer.parseInt(contentRA[2]); //resource production rate
                System.out.println(">>" + loc);
                //get the highest value to decide best RA
                if ( !loc.equals(((ProductAgent)myAgent).location) ) {
                    currValue = hour + rate;
                }
                //advantage to the resource, if the product is already there
                else currValue = 100 + hour + rate;
                
                if(bestValue < currValue) {
                    bestValue = currValue;
                    bestIndex = i;
                }
            }
            return bestIndex;
        }
        
        /***********************************************
        @desc: waits to recieve all PROPOSALS and accepts one
        @param: VECTOR of messages with all the resources responses
        @return: ACCEPT or REJECT response to all the resources
        ***********************************************/
        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            ACLMessage auxMsg; //message recieved (PROPOSAL or REFUSE)            
            System.out.println(myAgent.getLocalName() + ": ALL PROPOSALS received");
            
            //see all the messages recieved and find best one
            int bestIndex = bestResourceAlgorithm(responses);
            
            //where all the agents REFUSED the CFP request, so he must send a new request until one RA accepts
            if(bestIndex == -1) {
                try { Thread.sleep(5000); } 
                catch (InterruptedException ex) {
                    Logger.getLogger(searchResource.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("[ERROR]" + myAgent.getLocalName() + ": re-sending CFP messages\n");
                //send CFC message again
                ((ProductAgent)myAgent).addBehaviour(new searchResource(((ProductAgent)myAgent), ((ProductAgent)myAgent).msgSR));
                return;
            }
            
            //update recourses to ACCEPT or REJECT proposals
            for(int i = 0; i <= responses.size() - 1; i++) {
                auxMsg = (ACLMessage)responses.get(i);
                ACLMessage reply = auxMsg.createReply();
                //accept only one and reject the others
                if(i == bestIndex) {
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                }
                else if(auxMsg.getPerformative() != ACLMessage.REFUSE) { 
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
                acceptances.add(reply); 
            }            
        }  
        
        
         /***********************************************
        @desc: response to ACCEPTED resource
        @param: INFORM message with resource location
        @return: 
        ***********************************************/
        @Override
        protected void handleInform(ACLMessage inform) {
            String locResource = inform.getContent(); //resource location 
            String locProduct = ((ProductAgent)myAgent).location; //product location
            
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            
            //(3)search DF for transportation capable of MOVING
            try {
                DFAgentDescription[] dfListTA = DFInteraction.SearchInDFByType(Utilities.Constants.DFSERVICE_TRANSPORT, myAgent);
                ((ProductAgent)myAgent).msgMP.addReceiver(dfListTA[0].getName());
            } 
            catch (FIPAException ex) {
                Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

            if( !locResource.equals(locProduct) ) {
                //send to AVG the current product location and resource location
                ((ProductAgent)myAgent).msgMP.setContent(locProduct + "/" + locResource + "/" + myAgent.getLocalName()); 
            }    
            //no need to transport the product if he is already in place
            else ((ProductAgent)myAgent).msgMP.setContent("skip"); 

            //add the RA that will be performing the skill
            ((ProductAgent) myAgent).msgES.addReceiver( inform.getSender() );

        }
    }

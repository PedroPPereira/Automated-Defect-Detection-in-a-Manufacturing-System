package Transport;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import Resource.ResourceAgent;
import Utilities.DFInteraction;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id; 
    ITransport myLib;
    String description;
    String[] associatedSkills; //contains the skills of the TA
    Map<String, String> occupiedRAs = new HashMap<String, String>(); //sees if the RA is occupied
    //added varibles
    String state;    //state: "STARTING", "WAITING", "CONNECTED", "PERFORMING" 
    String origin, destination; //location to go and send product
    String currentProduct;
    ArrayList<String> queue; //list of products waiting to be transported
    
    
    @Override
    protected void setup() {
        state = "STARTING"; //turning transport on
        //update resources status
        occupiedRAs.put("GlueStation1", "WAITING");
        occupiedRAs.put("GlueStation2", "WAITING");
        occupiedRAs.put("QualityControlStation1", "WAITING");
        occupiedRAs.put("QualityControlStation2", "WAITING");
        occupiedRAs.put("Operator", "WAITING");
        occupiedRAs.put("Source", "WAITING");
        
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];
        queue = new ArrayList<String>();
        
        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (ITransport) instance;
            System.out.println(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Transport Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));
        
        //Register in DF  pag.11)
        try {
            DFInteraction.RegisterInDF(this, getLocalName(), Utilities.Constants.DFSERVICE_TRANSPORT);
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Add responder behaviour (AchieveREResponder pag.7)
        this.addBehaviour(new responder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        state = "WAITING"; //waiting to perform a skill
    }

    
    @Override
    protected void takeDown() {
        super.takeDown();
    }
    
    
    
    //------------------------------------------AchieveREResponder------------------------------------------//
    //TODO: personalizar msg
    private class responder extends AchieveREResponder {
        
        public responder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }
        
        
        /***********************************************
        @desc: checks RESOURCE availability to the REQUEST made
        @param: REQUEST message from the INITIATOR (to see if the resource is available)
        @return: message with the availability of the RESOURCE (AGREE or WAIT)
        ***********************************************/
        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message");
            ACLMessage msg = request.createReply();
            
            //no need to transport the product if is already in place
            if(request.getContent().equals("skip") ) {
                msg.setContent("skip");
                msg.setPerformative(ACLMessage.REFUSE);
            }
            else {
                String[] locations = request.getContent().split("/"); 
                origin = locations[0];
                destination = locations[1];
                String product = locations[2];
                
                //refuse if there is already a product in the destination resource
//                if( occupiedRAs.get(destination).equals("OCCUPIED")) {
//                    msg.setContent("resend");
//                    msg.setPerformative(ACLMessage.REFUSE);
//                    //add to queue to wait for his turn
//                    //if (!queue.contains(product)) queue.add(product); 
//                }
//                else 
                if (state.equals("WAITING") ) {
                    //check if there is products waiting already in the queue
                    if(queue.size()==0) {
                        currentProduct = product;
                        msg.setPerformative(ACLMessage.AGREE);
                        state = "CONNECTED"; //stuck to a product now
                    }
                    //check if the product sending the REQUEST is the first queued (the one waiting the longest)
                    else if(queue.get(0) == product) {
                        msg.setPerformative(ACLMessage.AGREE);
                        state = "CONNECTED"; //stuck to a product now
                        currentProduct = product;
                        queue.remove(product); //remove product from the waiting list
                    }
                    //check if product already exists in the queue, but is not the first
                    else{ //its occupied, send request later (update queue w/ a new product)
                        msg.setContent("resend");
                        if (!queue.contains(product)) queue.add(product); //add to queue to wait for his turn
                        msg.setPerformative(ACLMessage.REFUSE);
                    }
                }
                else { //its occupied, send request later (update queue w/ a new product)
                    msg.setContent("resend");
                    if (!queue.contains(product)) queue.add(product); //add to queue to wait for his turn
                    msg.setPerformative(ACLMessage.REFUSE);
                }
                
            }
            System.out.println(">>QUEUE: " + Arrays.toString(queue.toArray()) );
            return msg;
        }

        /***********************************************
        @desc: executes SKILL requested 
        @param: REQUEST message from the INITIATOR 
        @return: message to inform about the SKILL executed
        ***********************************************/
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
            block(2000);
            ACLMessage msg = request.createReply();
            
            state = "PERFORMING";
            //the origin resource is free now from products
            occupiedRAs.replace(origin, "WAITING");
            
            //if (myLib.executeMove(origin, destination)) { //Normal run
            if (myLib.executeMove(origin, destination, currentProduct)) { // Simulation
                msg.setContent(destination);
                System.out.println(myAgent.getLocalName() + ": REQUEST success");
            }
            else msg.setContent("ERROR"); //something wrong happened, redo execute
            state = "CONNECTED"; 
            
            //the destination resource has a product there now
            occupiedRAs.replace(destination, "OCCUPIED");
            
            origin = "";
            destination = "";
            currentProduct = "";
            
            msg.setPerformative(ACLMessage.INFORM);
            state = "WAITING";
            return msg;
        }               
    }
}

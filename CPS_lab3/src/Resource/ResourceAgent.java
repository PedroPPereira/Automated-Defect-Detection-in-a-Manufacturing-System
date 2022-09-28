package Resource;

import jade.core.Agent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import Utilities.Constants;
import Utilities.DFInteraction;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.AchieveREResponder;
/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ResourceAgent extends Agent {

    String id;
    IResource myLib;
    String description;
    String[] associatedSkills;
    String location;
    //added varibles   
    String state;
    int hourCost = 1;
    int productionRate = 3;
    String performSkill;

    
    @Override
    protected void setup() {
        state = "STARTING";
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (IResource) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.location = (String) args[3];
        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));
        
        
        //Register in DF with the corresponding skills as services (pag.11)
        try {
            DFInteraction.RegisterInDF(this, associatedSkills, Utilities.Constants.DFSERVICE_RESOURCE);
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Add responder behaviour (AchieveREResponder pag.7 && ContractNetResponder pag.10)
        this.addBehaviour(new responderCN(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        this.addBehaviour(new responderARE(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        state = "WAITING";
    }

    
    @Override
    protected void takeDown() {
        super.takeDown(); 
    }
    
    
    
    //------------------------------------------ContractNetResponder------------------------------------------//
    //TODO: personalizar msg
    private class responderCN extends ContractNetResponder {
        
        public responderCN(Agent a, MessageTemplate mt) {
            super(a, mt);
        }
        
        
        /***********************************************
        @desc: sees if the RESOURCE is available to perform a skill
        @param: CFP message from the INITIATOR (to see if the resource is available) 
        @return: PROPOSAL if the current RESOURCE state/availability (ex. WAITING/14,15)
        ***********************************************/
        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            System.out.println(myAgent.getLocalName() + ": Processing CFP message");
            ACLMessage msg = cfp.createReply();
            
            if (state.equals("WAITING") ) {
                msg.setPerformative(ACLMessage.PROPOSE);
                msg.setContent( location + "/" + hourCost + "/" + productionRate); 
            }
            else {
                //occupied with another product
                msg.setPerformative(ACLMessage.REFUSE);
            }
            return msg;
        }

        /***********************************************
        @desc: if the PROPOSAL sent was successful confirms agreement
        @param: CFP message from the INITIATOR and RESPONSE to the proposal inicially made by the RESOURCE
        @return: if the PROPOSAL was accepted sends the RESOURCE location (ex. INFORM)
        ***********************************************/
        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(myAgent.getLocalName() + ": Preparing result of CFP");
            block(2000);
            ACLMessage msg = cfp.createReply();
            
            state = "CONNECTED"; //stuck to a product now
            msg.setContent(location); //send the resource location, so the product can arrive 
            msg.setPerformative(ACLMessage.INFORM);
            return msg;
        }        
    }
    
    
    
    //------------------------------------------AchieveREResponder------------------------------------------//
    private class responderARE extends AchieveREResponder {
        
        public responderARE(Agent a, MessageTemplate mt) {
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
            msg.setPerformative(ACLMessage.AGREE);
            performSkill = request.getContent();
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
            //execute specific skill
            Boolean boolExecution = myLib.executeSkill(performSkill);
            state = "CONNECTED"; 
            
            //deal with product with the quality check detects a error
            if(!boolExecution && performSkill.equalsIgnoreCase(Constants.SK_QUALITY_CHECK)) msg.setContent("NOK");
            else if(boolExecution) msg.setContent("SUCCESS");
            //resource failed to perform the skill, send name to store in product failure info
            else  msg.setContent(myAgent.getLocalName());
            
            System.out.println("!!! performSkill: " + performSkill);
            System.out.println("!!! boolExecution: " + boolExecution.toString());
            System.out.println("!!! msg.getContent: " + msg.getContent());
            
            performSkill = "";
            msg.setPerformative(ACLMessage.INFORM);
            state = "WAITING";
            return msg;
        }               
    }
}

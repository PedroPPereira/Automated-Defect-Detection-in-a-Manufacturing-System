package Product;

import Resource.ResourceAgent;
import Utilities.DFInteraction;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.AID;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>(); //list of skills to perform
    //added varibles
    String name;
    String description;
    String type;
    String shape;
    String dimensions;
    String supplier;
    String progress;
    String weight;
    String height;
    String unitPrice;
    //main added varibles
    String failure = "";
    String location = "Source";
    String currentSkill = ""; //current skill the product is trying to perform
    int skillCounter = -1; //skill counter
    ACLMessage msgSR = new ACLMessage(ACLMessage.CFP);     //message FOR searchResource
    ACLMessage msgMP = new ACLMessage(ACLMessage.REQUEST); //message FOR moveProduct
    ACLMessage msgES = new ACLMessage(ACLMessage.REQUEST); //message FOR executeSkill
    SequentialBehaviour sb; 
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);
        
        //Register in DF (could be used in a hypothetical solution, for example communication with a SCADA or other produts)
        try {
            DFInteraction.RegisterInDF(this, getLocalName(), Utilities.Constants.DFSERVICE_PRODUCT);
        } catch (FIPAException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        //start sequence to execute the first product
        startNewSequence();

    }

    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<String> getExecutionList(String productType) {
        switch(productType) {
            case "A": return Utilities.Constants.PROD_A;
            case "B": return Utilities.Constants.PROD_B;
            case "C": return Utilities.Constants.PROD_C;
        }
        return null;
    }
    
    
    /***********************************************
    @desc: starts a new sequence of tasks to perform the current skill
    @param: void
    @return: void
    ***********************************************/
    public void startNewSequence() {
        skillCounter++;
        //stop iterating when all the skills are done
        if(skillCounter > executionPlan.size()-1 ) return;
        
        //get current skill to be done
        currentSkill = executionPlan.get(skillCounter);
        System.out.println("\n--------------" + this.getLocalName() + "->" + currentSkill +  "---------------");
        System.out.println(getLocalName() + ": trying to execute ->" + currentSkill );
        
        sb = new SequentialBehaviour();
        //clean msg recievers
        msgSR.clearAllReceiver();
        msgMP.clearAllReceiver();
        msgES.clearAllReceiver();
        //set ontologies
        msgSR.setOntology(Utilities.Constants.ONTOLOGY_NEGOTIATE_RESOURCE);
        msgMP.setOntology(Utilities.Constants.ONTOLOGY_MOVE);
        msgES.setOntology(Utilities.Constants.ONTOLOGY_EXECUTE_SKILL);
        
        try {
            //(1) search DF for resources capable of performing the current skill
            DFAgentDescription[] dfListRA = DFInteraction.SearchInDFByName(currentSkill, this);
            //iterate every resource found to send a msg CFP and find best resource
            for (DFAgentDescription dfRA : dfListRA) {
                msgSR.addReceiver((AID)dfRA.getName());
            } 
        } catch (FIPAException ex) {
            Logger.getLogger(ProductAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        //send skill name as a message to all the resources that are capable of executing that skill
        msgSR.setContent(currentSkill); 
        //send skill to resource choosen in the future
        //msgES.setContent(currentSkill);
        
        //create product workflow -> Sequencial Behaviour (pag.13)
        //(2) negaciate execution with possible resoruces and accept one
        System.out.println(getLocalName() + " sending CFP messages\n");
        sb.addSubBehaviour(new searchResource(this, msgSR)); 
        //(4) request transportation and update the current location once the INFORM is recieved
        sb.addSubBehaviour(new moveProduct(this, msgMP));    
        //(5) request skill execution from the resource ACCEPTED
        sb.addSubBehaviour(new executeSkill(this, msgES));  
        this.addBehaviour(sb);    
        
    }
}

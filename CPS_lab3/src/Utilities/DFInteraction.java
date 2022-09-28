package Utilities;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class DFInteraction {

    //Registers the service with a given name and a type relative to myAgent 
    public static void RegisterInDF(Agent myAgent, String name, String type) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(myAgent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        DFService.register(myAgent, dfd);
    }

    //Registers multiple services with a given names and a type relative to myAgent 
    public static void RegisterInDF(Agent myAgent, String name[], String type) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(myAgent.getAID());
        for (String n : name) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            sd.setName(n);
            dfd.addServices(sd);
        }
        DFService.register(myAgent, dfd);
    }

    //Searches the DF for services with a given name
    //Returns: An array with the matching DFAgentDescription 
    public static DFAgentDescription[] SearchInDFByName(String name, Agent myAgent) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(name);
        dfd.addServices(sd);
        DFAgentDescription[] result = DFService.search(myAgent, dfd);
        return result;
    }

    //Searches the DF for services with a given type
    //Returns: An array with the matching DFAgentDescription 
    public static DFAgentDescription[] SearchInDFByType(String type, Agent myAgent) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        dfd.addServices(sd);
        DFAgentDescription[] result = DFService.search(myAgent, dfd);
        return result;
    }

}

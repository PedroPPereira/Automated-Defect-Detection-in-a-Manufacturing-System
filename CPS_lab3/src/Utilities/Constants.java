package Utilities;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class Constants {
//JADE Arguments to run on NETBEANS   
//-gui QualityControlStation1:Resource.ResourceAgent("QCS1","QCS1","TestLibrary","QCS1");QualityControlStation2:Resource.ResourceAgent("QCS2","QCS2","TestLibrary","QCS2");Operator:Resource.ResourceAgent("OP","OP","TestLibrary","OP");GlueStation2:Resource.ResourceAgent("GS2","GS2","TestLibrary","GS2");GlueStation1:Resource.ResourceAgent("GS1","GS1","TestLibrary","GS1");AVG:Transport.TransportAgent("AGV","AGV","TestTransportLibrary");GUI:Utilities.ConsoleAgent();
//
    
//JADE Arguments to run on Coppelia
//-gui QualityControlStation1:Resource.ResourceAgent("QCS1","QCS1","SimResourceLibrary","QCS1");QualityControlStation2:Resource.ResourceAgent("QCS2","QCS2","SimResourceLibrary","QCS2");Operator:Resource.ResourceAgent("OP","OP","SimResourceLibrary","OP");GlueStation2:Resource.ResourceAgent("GS2","GS2","SimResourceLibrary","GS2");GlueStation1:Resource.ResourceAgent("GS1","GS1","SimResourceLibrary","GS1");AVG:Transport.TransportAgent("AGV","AGV","SimTransportLibrary");GUI:Utilities.ConsoleAgent();
  
   
    //DF Services
    public static final String DFSERVICE_TRANSPORT = "dfservice_transport";
    public static final String DFSERVICE_PRODUCT = "dfservice_product";
    public static final String DFSERVICE_RESOURCE = "dfservice_resource";
    
    //JADE Ontologies
    public static final String ONTOLOGY_NEGOTIATE_RESOURCE = "ont_neg_res";
    public static final String ONTOLOGY_MOVE = "ont_move";
    public static final String ONTOLOGY_EXECUTE_SKILL = "ont_exec";
    
    //Skills
    public static final String SK_GLUE_TYPE_A = "sk_g_a";
    public static final String SK_GLUE_TYPE_B = "sk_g_b";
    public static final String SK_GLUE_TYPE_C = "sk_g_c";
    public static final String SK_PICK_UP = "sk_pick";
    public static final String SK_DROP = "sk_drop";
    public static final String SK_MOVE = "sk_move";
    public static final String SK_QUALITY_CHECK = "sk_q_c";
   
    //Product type execution lists
    public static final ArrayList<String> PROD_A = new ArrayList<>(Arrays.asList(
            SK_PICK_UP, SK_GLUE_TYPE_A, SK_GLUE_TYPE_B, SK_QUALITY_CHECK, SK_DROP));
    public static final ArrayList<String> PROD_B = new ArrayList<>(Arrays.asList(
            SK_PICK_UP, SK_GLUE_TYPE_A, SK_GLUE_TYPE_C, SK_QUALITY_CHECK, SK_DROP));
    public static final ArrayList<String> PROD_C = new ArrayList<>(Arrays.asList(
            SK_PICK_UP, SK_GLUE_TYPE_B, SK_GLUE_TYPE_C, SK_QUALITY_CHECK, SK_DROP));

    //Token
    public static final String TOKEN = "#TOKEN#";
}

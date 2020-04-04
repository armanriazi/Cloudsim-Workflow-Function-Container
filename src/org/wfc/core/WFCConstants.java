package org.wfc.core;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5670;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5675;

/**
 * In this class the specifications of the Cloudlets, Containers, VMs and Hosts are coded.
 * Regarding to the hosts, the powermodel of each type of the hosts are all included in this class.
 */
public  class WFCConstants {
  /**
     * The available virtual machine types along with the specs.
     */

    public static   int VM_TYPES = 4;
    public static   double[] VM_MIPS = new double[]{37274/ 10, 37274 / 10, 37274 / 10};
    public static   int[] VM_PES = new int[]{ 2, 4, 8};
    public static   float[] VM_RAM = new float[] {(float)1024, (float) 2048, (float) 4096};//**MB*
    public static   int[] VM_BW = new int[]{ 100000/10,100000/10,100000/10};
    //public static   int VM_BW = 1000000;
    public static   int VM_SIZE = 1000000;

    public static   int CONTAINER_TYPES = 5;
    public static   double[] CONTAINER_MIPS = new double[]{18637/ 10, 18637/ 10, 18637/ 10};
    public static   int[] CONTAINER_PES = new int[]{1, 1, 1};
    public static   float[] CONTAINER_RAM = new float[]{8 , 8, 8};
    public static   int[] CONTAINER_BW = new int[]{100,100,100};
    public static   int CONTAINER_SIZE = 450;

    public static   int HOST_TYPES = 3;
    public static   int[] HOST_MIPS = new int[]{37274, 37274, 37274};
    public static   int[] HOST_PES = new int[]{16,32,64};
    public static   int[] HOST_RAM = new int[]{8192,16384,32768};
    public static   int[] HOST_BW = new int[]{ 100000,100000,100000};
    //public static   int HOST_BW = 1000000;
    public static   int HOST_STORAGE = 10000000;
    public static   PowerModel[] HOST_POWER = new PowerModel[]{new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(), new PowerModelSpecPowerIbmX3550XeonX5670()};

//-----------------------Simulation
    
    public static  int OVERBOOKING_FACTOR= 80;
    public static  int JOB_LENGTH= 110;
    
    public static  boolean CAN_PRINT_SEQ_LOG = false;
    public static  boolean CAN_PRINT_SEQ_LOG_Just_Step = false;
    
    public static  boolean ENABLE_OUTPUT = false;
    public static  boolean FAILURE_FLAG = false;            
    public static  boolean RUN_AS_STATIC_RESOURCE = true;                
     public static  boolean POWER_MODE = false; 
    public static   double SIMULATION_LIMIT = 87400.0D;
    

//-----------------------Delay
    
    public static  double WFC_CONTAINER_STARTTUP_DELAY = 0.4;
    public static  double WFC_VM_STARTTUP_DELAY = 1;

//-----------------------Number
    
    public static   int WFC_NUMBER_SCHEDULER = 1;
    public static   int WFC_NUMBER_HOSTS = 1;
    public static   int WFC_NUMBER_VMS =1+2;
    public static   int WFC_NUMBER_USERS = 1;
    public static   int WFC_NUMBER_CONTAINER= 101;
    public static   int WFC_NUMBER_CLOUDLETS = 101;
    public static   int WFC_NUMBER_CLOUDLET_PES = 1;
    public static   int WFC_NUMBER_CONTAINER_PES = 1;
    public static   int WFC_CONTAINER_PES_NUMBER = 1;
    public static  int  WFC_NUMBER_VM_PES = 2+2; //number of cpus
    public static  int WFC_NUMBER_HOST_PES = 10+2; //number of cpus
//-----------------------Cloudlet
    
    public static   int CLOUDLET_LENGTH = 30;    
    public static  long CLOUDLET_FILESIZE= 300;
    public static  long CLOUDLET_OUTPUTSIZE= 300;

//-----------------------Container    
     
    public static   long  WFC_CONTAINER_SIZE = 100;
    public static   int   WFC_CONTAINER_RAM = 8; 
    public static   int   WFC_CONTAINER_MIPS = 10000;
    public static   long  WFC_CONTAINER_BW = 100;
    public static   double WFC_CONTAINER_RATIO = 1.0;
    public static   String WFC_CONTAINER_VMM = "Xen"; 
    public static   double WFC_CONTAINER_OVER_UTILIZATION_THRESHOLD = 0.80D ;
    public static   double WFC_CONTAINER_UNDER_UTILIZATION_THRESHOLD = 0.70D ;
    
//----------------------- VM    
    
    public static  long WFC_VM_SIZE = 10000; //image size (MB)
    public static  int  WFC_VM_RAM = 1024; //vm memory (MB)
    public static  int  WFC_VM_MIPS = 100000;
    public static  long WFC_VM_BW = 10000;
    public static  double WFC_VM_RATIO = 1.0;
    public static  String WFC_VM_VMM = "Xen"; //VMM name
    
//----------------------- HOST    
    
     public static  long WFC_HOST_STORAGE = 100000; 
    public static  long WFC_HOST_SIZE = 100000; //image size (MB)    
    public static  int WFC_HOST_MIPS = 100000;
    public static  long WFC_HOST_BW = 100000;
    public static  int WFC_HOST_RAM = 1024*3; //vm memory (MB)    
    public static  String WFC_HOST_VMM = "Xen"; //VMM name    
    public static  double WFC_HOST_RATIO = 1.0;
        
//----------------------- DataCenter_Characteristics    
    
    
    public static  String WFC_DC_ARCH = "x86";      // system architecture
    public static  String WFC_DC_OS = "Linux";          // operating system
    public static  String WFC_DC_VMM = "Xen";
    public static  double WFC_DC_TIME_ZONE = 10.0;         // time zone this resource located
    public static  double WFC_DC_COST = 3.0;              // the cost of using processing in this resource
    public static  double WFC_DC_COST_PER_MEM = 0.05;		// the cost of using memory in this resource
    public static  double WFC_DC_COST_PER_STORAGE = 0.1;	// the cost of using storage in this resource
    public static  double WFC_DC_COST_PER_BW = 0.1;	
    public static   int   WFC_DC_MAX_TRANSFER_RATE= 15;
    public static    double WFC_DC_SCHEDULING_INTERVAL = 0.1D;
        
//-----------------------The Addresses
    public WFCConstants() {
    }
 }

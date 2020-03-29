/**
 * Copyright 2019-2020 ArmanRiazi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cloudbus.cloudsim.wfc.examples;

import org.cloudbus.cloudsim.wfc.core.WFCDatacenter;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.resourceAllocators.*;
import org.cloudbus.cloudsim.container.containerProvisioners.*;
import org.cloudbus.cloudsim.container.containerVmProvisioners.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;
import org.workflowsim.CondorVM;
import org.workflowsim.Task;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.Job;
import org.workflowsim.WFCEngine;
import org.workflowsim.WFCPlanner;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.WFCReplicaCatalog;
import org.workflowsim.utils.Parameters.ClassType;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.schedulers.*;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.*;
import org.cloudbus.cloudsim.wfc.core.WFCConstants;
import org.cloudbus.cloudsim.util.Conversion;
import org.workflowsim.failure.FailureGenerator;
import org.workflowsim.failure.FailureMonitor;
import org.workflowsim.failure.FailureParameters;
import org.workflowsim.utils.DistributionGenerator;

/*
 * @author Arman Riazi
 * @since WFC Toolkit 1.0
 * @date March 29, 2020
 */
/*
ConstantsExamples.WFC_DC_SCHEDULING_INTERVAL+ 0.1D

On Vm (
    Allocation= PowerContainerVmAllocationPolicyMigrationAbstractHostSelection
    Scheduler = ContainerVmSchedulerTimeSharedOverSubscription    
    SelectionPolicy = PowerContainerVmSelectionPolicyMaximumUsage
    Pe = CotainerPeProvisionerSimple
    Overhead = 0
    ClusteringMethod.NONE
    SchedulingAlgorithm.MINMIN
    PlanningAlgorithm.INVALID
    FileSystem.LOCAL
)

On Host (
    Scheduler = ContainerVmSchedulerTimeSharedOverSubscription    
    SelectionPolicy = HostSelectionPolicyFirstFit
    Pe = PeProvisionerSimple 
)

On Container (
    Allocation = PowerContainerAllocationPolicySimple
    Scheduler = ContainerCloudletSchedulerDynamicWorkload 
    UtilizationModelFull
)
*/

public class WFCExample {
    
    private static String experimentName="WFCExampleStatic";
    private static  int num_user = 1;
    private static boolean trace_flag = false;  // mean trace events
    private static boolean failure_flag = false;   
    private static List<Container> containerList;       
    private static List<ContainerHost> hostList;    
    public static List<? extends ContainerVm> vmList;    
    
    public static void main(String[] args) {
        try {                                                
                        
            WFCConstants.CAN_PRINT_SEQ_LOG = false;
            WFCConstants.CAN_PRINT_SEQ_LOG_Just_Step = false;
            WFCConstants.ENABLE_OUTPUT = false;
            WFCConstants.FAILURE_FLAG = false;            
            WFCConstants.RUN_AS_STATIC_RESOURCE = true;     
            
            FailureParameters.FTCMonitor ftc_monitor = null;
            FailureParameters.FTCFailure ftc_failure = null;
            FailureParameters.FTCluteringAlgorithm ftc_method = null;
            DistributionGenerator[][] failureGenerators = null;
             
            Log.printLine("Starting " + experimentName + " ... ");
                        
            String daxPath = "./config/dax/Montage_" + (WFCConstants.WFC_NUMBER_CLOUDLETS - 1) + ".xml";
            
            File daxFile = new File(daxPath);
            if (!daxFile.exists()) {
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }
     
            if(failure_flag){
                /*
                *  Fault Tolerant Parameters
                */
               /**
                * MONITOR_JOB classifies failures based on the level of jobs;
                * MONITOR_VM classifies failures based on the vm id; MOINTOR_ALL
                * does not do any classification; MONITOR_NONE does not record any
                * failiure.
                */
                ftc_monitor = FailureParameters.FTCMonitor.MONITOR_ALL;
               /**
                * Similar to FTCMonitor, FTCFailure controls the way how we
                * generate failures.
                */
                ftc_failure = FailureParameters.FTCFailure.FAILURE_ALL;
               /**
                * In this example, we have no clustering and thus it is no need to
                * do Fault Tolerant Clustering. By default, WorkflowSim will just
                * rety all the failed task.
                */
                ftc_method = FailureParameters.FTCluteringAlgorithm.FTCLUSTERING_NOOP;
               /**
                * Task failure rate for each level
                *
                */
               failureGenerators = new DistributionGenerator[1][1];
               failureGenerators[0][0] = new DistributionGenerator(DistributionGenerator.DistributionFamily.WEIBULL,
                       100, 1.0, 30, 300, 0.78);
            }
            
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.MINMIN;//local
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;//global-stage
            WFCReplicaCatalog.FileSystem file_system = WFCReplicaCatalog.FileSystem.LOCAL;

            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);
   
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            if(failure_flag){
                FailureParameters.init(ftc_method, ftc_monitor, ftc_failure, failureGenerators);
            }
          
           Parameters.init(WFCConstants.WFC_NUMBER_VMS, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            WFCReplicaCatalog.init(file_system);

            FailureMonitor.init();
            FailureGenerator.init();
      
            
            WFCReplicaCatalog.init(file_system);
            
            Calendar calendar = Calendar.getInstance();            

            CloudSim.init(num_user, calendar, trace_flag);


            PowerContainerAllocationPolicy containerAllocationPolicy = new PowerContainerAllocationPolicySimple();
            PowerContainerVmSelectionPolicy vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();
            HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicyFirstFit();

            String logAddress = "~/Results";
                       
            hostList = new ArrayList<ContainerHost>();
            hostList = createHostList(WFCConstants.WFC_NUMBER_HOSTS);
            //cloudletList = new ArrayList<ContainerCloudlet>();
            containerList= new ArrayList<Container>();        
            //vmList = new ArrayList<ContainerVm>();
            
            ContainerVmAllocationPolicy vmAllocationPolicy = new
                    PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(hostList, vmSelectionPolicy,
                    hostSelectionPolicy, WFCConstants.WFC_CONTAINER_OVER_UTILIZATION_THRESHOLD, WFCConstants.WFC_CONTAINER_UNDER_UTILIZATION_THRESHOLD);        
            
            WFCDatacenter datacenter = (WFCDatacenter) createDatacenter("datacenter_0",
                        PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy,containerList,containerAllocationPolicy,
                        getExperimentName(experimentName, String.valueOf(WFCConstants.OVERBOOKING_FACTOR)),
                        WFCConstants.WFC_DC_SCHEDULING_INTERVAL, logAddress,
                        WFCConstants.WFC_VM_STARTTUP_DELAY,
                        WFCConstants.WFC_CONTAINER_STARTTUP_DELAY);
       
            WFCPlanner wfPlanner = new WFCPlanner("planner_0", 1);
                      
            WFCEngine wfEngine = wfPlanner.getWorkflowEngine();
            //vmList = createVmList(wfEngine.getSchedulerId(0), Parameters.getVmNum());                        
            //wfEngine.submitVmList(wfEngine.getVmList(), 0);                           
            wfEngine.bindSchedulerDatacenter(datacenter.getId(), 0);
            

            CloudSim.terminateSimulation(WFCConstants.SIMULATION_LIMIT);
            CloudSim.startSimulation();         
            CloudSim.stopSimulation();
            
            List<Job> outputList0 = wfEngine.getJobsReceivedList();
           
            printJobList(outputList0,datacenter);
            
            Log.printLine(experimentName + "finished!");
            //outputByRunnerAbs();
            
        } catch (Exception e) {                        
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            Log.printLine(e.getMessage());
            System.exit(0);
        }
    }

     public static WFCDatacenter createDatacenter(String name, Class<? extends WFCDatacenter> datacenterClass,
                                                       List<ContainerHost> hostList,
                                                       ContainerVmAllocationPolicy vmAllocationPolicy,
                                                       List<Container> containerList,
                                                       ContainerAllocationPolicy containerAllocationPolicy,                                                       
                                                       String experimentName, double schedulingInterval, String logAddress, double VMStartupDelay,
                                                       double ContainerStartupDelay) throws Exception {
       
        // 4. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        
        LinkedList<Storage> storageList =new LinkedList<Storage>();
        WFCDatacenter datacenter = null;

        // 5. Finally, we need to create a storage object.
        /**
         * The bandwidth within a data center in MB/s.
         */
        //int maxTransferRate = 15;// the number comes from the futuregrid site, you can specify your bw

        try {
            // Here we set the bandwidth to be 15MB/s
            HarddriveStorage s1 = new HarddriveStorage(name, 1e12);
            s1.setMaxTransferRate(WFCConstants.WFC_DC_MAX_TRANSFER_RATE);
            storageList.add(s1);           

            ContainerDatacenterCharacteristics characteristics = new              
                ContainerDatacenterCharacteristics(WFCConstants.WFC_DC_ARCH, WFCConstants.WFC_DC_OS, WFCConstants.WFC_DC_VMM,
                                                     hostList, WFCConstants.WFC_DC_TIME_ZONE, WFCConstants.WFC_DC_COST , WFCConstants.WFC_DC_COST_PER_MEM, 
                                                     WFCConstants.WFC_DC_COST_PER_STORAGE,WFCConstants.WFC_DC_COST_PER_BW);
            
            datacenter = new WFCDatacenter(name, 
                    characteristics, 
                    vmAllocationPolicy,
                    containerAllocationPolicy, 
                    storageList, 
                    schedulingInterval, 
                    experimentName, 
                    logAddress
                    );
                
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            Log.printLine(e.getMessage());
            System.exit(0);
        }
        return datacenter;
    }
    
       
    public static List<ContainerHost> createHostList(int hostsNumber) {
        
            ArrayList<ContainerHost> hostList = new ArrayList<ContainerHost>();
        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        
         try {
            for (int i = 1; i <= WFCConstants.WFC_NUMBER_HOSTS; i++) {
                ArrayList<ContainerVmPe> peList = new ArrayList<ContainerVmPe>();            
                // 3. Create PEs and add these into the list.
                //for a quad-core machine, a list of 4 PEs is required:
                for (int p = 0; p < WFCConstants.WFC_NUMBER_HOST_PES; p++) {
                  peList.add(new ContainerVmPe(p, new ContainerVmPeProvisionerSimple(WFCConstants.WFC_HOST_MIPS))); // need to store Pe id and MIPS Rating            
                }

                
                 hostList.add(new PowerContainerHostUtilizationHistory(IDs.pollId(ContainerHost.class) ,
                        new ContainerVmRamProvisionerSimple(WFCConstants.WFC_HOST_RAM),
                        new ContainerVmBwProvisionerSimple(WFCConstants.WFC_HOST_BW), WFCConstants.WFC_HOST_STORAGE , peList,
                        new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                         //new ContainerVmSchedulerTimeShared(peList),
                        WFCConstants.HOST_POWER[2]));
            }
          } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            Log.printLine(e.getMessage());
            System.exit(0);
        }
        return hostList;
    }
          
        private static String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isEmpty()) {
                if (i != 0) {
                    experimentName.append("_");
                }

                experimentName.append(args[i]);
            }
        }

        return experimentName.toString();
    }
      /**
    /**
     * Gets the maximum number of GB ever used by the application's heap.
     * @return the max heap utilization in GB
     * @see <a href="https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html">Java Garbage Collection Basics (for information about heap space)</a>
     */
    private static double getMaxHeapUtilizationGB() {
        final double memoryBytes =
            ManagementFactory.getMemoryPoolMXBeans()
                             .stream()
                             .filter(bean -> bean.getType() == MemoryType.HEAP)
                             .filter(bean -> bean.getName().contains("Eden Space") || bean.getName().contains("Survivor Space"))
                             .map(MemoryPoolMXBean::getPeakUsage)
                             .mapToDouble(MemoryUsage::getUsed)
                             .sum();

        return Conversion.bytesToGigaBytes(memoryBytes);
    }

                   
    public static List<Container> createContainerList(int brokerId, int containersNumber) {
        LinkedList<Container> list = new LinkedList<>();        
        //peList.add(new ContainerPe(0, new CotainerPeProvisionerSimple((double)mips * ratio)));         
        //create VMs
        try{
            Container[] containers = new Container[containersNumber];
            for (int i = 0; i < containersNumber; i++) {

                containers[i] = new PowerContainer(IDs.pollId(Container.class), brokerId, (double) WFCConstants.WFC_CONTAINER_MIPS ,
                        WFCConstants.WFC_CONTAINER_PES_NUMBER , WFCConstants.WFC_CONTAINER_RAM,
                        WFCConstants.WFC_CONTAINER_BW, WFCConstants.WFC_CONTAINER_SIZE, WFCConstants.WFC_CONTAINER_VMM,
                        //new ContainerCloudletSchedulerTimeShared(),WFCConstants.WFC_DC_SCHEDULING_INTERVAL);                    
                        new ContainerCloudletSchedulerDynamicWorkload(WFCConstants.WFC_CONTAINER_MIPS, WFCConstants.WFC_CONTAINER_PES_NUMBER),
                        WFCConstants.WFC_DC_SCHEDULING_INTERVAL);                    
                list.add(containers[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            Log.printLine(e.getMessage());
            System.exit(0);
        }
        return list;
       }
    
    
      public static List<ContainerVm> createVmList(int brokerId, int containerVmsNumber) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<ContainerVm> list = new LinkedList<>();
        ArrayList peList = new ArrayList();
       
        try{
            for (int p = 0; p < WFCConstants.WFC_NUMBER_VM_PES ; p++) {
              peList.add(new ContainerPe(p, new CotainerPeProvisionerSimple((double)WFCConstants.WFC_VM_MIPS * WFCConstants.WFC_VM_RATIO)));         
            }
           //create VMs
           ContainerVm[] vm = new ContainerVm[containerVmsNumber];

           for (int i = 0; i < containerVmsNumber; i++) {           
               vm[i] = new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId, WFCConstants.WFC_VM_MIPS, (float)WFCConstants.WFC_VM_RAM,
                        WFCConstants.WFC_VM_BW, WFCConstants.WFC_VM_SIZE,  WFCConstants.WFC_VM_VMM,
                       new ContainerSchedulerTimeSharedOverSubscription(peList),
                       //new ContainerSchedulerTimeSharedOverSubscription(peList),
                       new ContainerRamProvisionerSimple(WFCConstants.WFC_VM_RAM),
                       new ContainerBwProvisionerSimple(WFCConstants.WFC_VM_BW), peList,
                       WFCConstants.WFC_DC_SCHEDULING_INTERVAL);

                       /*new ContainerVm(IDs.pollId(ContainerVm.class), brokerId, (double) mips, (float) ram,
                       bw, size, "Xen", new ContainerSchedulerTimeShared(peList),
                       new ContainerRamProvisionerSimple(ram),
                       new ContainerBwProvisionerSimple(bw), peList);*/

                       //new ContainerVm(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
               list.add(vm[i]);
           }
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            Log.printLine(e.getMessage());
            System.exit(0);
        }
        return list;
    }
    /**
     * Prints the job objects
     *
     * @param list list of jobs
     */
    protected static void printJobList(List<Job> list, WFCDatacenter datacenter) {
        double maxHeapUtilizationGB = getMaxHeapUtilizationGB();
        String indent = "    ";        
        double cost = 0.0;
        double time = 0.0;
        double length= 0.0;
        int counter = 1;
        int success_counter = 0;
        int failed_counter = 0;

        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet Column=Task=>Length,WFType,Impact # Times of Task=>Actual,Exec,Finish.");//,CloudletOutputSize
        Log.printLine();
        Log.printLine(indent+"Row"+indent + "JOB ID"  +  indent + indent + "CLOUDLET" + indent + indent 
                + "STATUS" + indent
                + "Data CENTER ID" 
                //+ indent + indent + "HOST ID" 
                + indent + "VM ID" + indent + indent+ "CONTAINER ID" + indent + indent
                + "TIME" + indent +  indent +"START TIME" + indent + indent + "FINISH TIME" + indent + "DEPTH" + indent + indent + "Cost");
        
        DecimalFormat dft0 = new DecimalFormat("###.###");
        DecimalFormat dft = new DecimalFormat("####.###");
        
        for (Job job : list) {
            Log.print(String.format("%6d |",counter++)+indent + job.getCloudletId() + indent + indent);
            if (job.getClassType() == ClassType.STAGE_IN.value) {
                Log.print("STAGE-IN");
            }
            for (Task task : job.getTaskList()) {                
               
              Log.print(task.getCloudletId()+ " ,");
              Log.print(task.getCloudletLength()+ " ,");               
              Log.print(task.getType());                                            
              //Log.print(dft0.format(task.getImpact()));
              
              Log.print("\n"+"\t\t\t ("+dft0.format(task.getActualCPUTime())+ " ,");      
              Log.print("\n"+"\t\t\t"+dft0.format(task.getExecStartTime())+ " ,");      
              Log.print("\n"+"\t\t\t"+dft0.format(task.getTaskFinishTime())+ " )");      
             
            }
            Log.print(indent);
                
            cost += job.getProcessingCost();
            time += job.getActualCPUTime();
            length +=job.getCloudletLength();
            
            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("     SUCCESS");         
                success_counter++;
                //datacenter.getContainerAllocationPolicy().getContainerVm(job.getContainerId(), job.getUserId()).getHost().getId()
                Log.printLine(indent + indent +indent + job.getResourceId() 
                        //+ indent + indent  + indent + indent + datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getId()
                        + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + job.getContainerId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth()
                        + indent + indent + indent 
                        + dft.format(job.getProcessingCost()                       
                       
                        ));
                  //Log.printLine();                              
                  /*
                   Log.printLine(datacenter.getContainerAllocationPolicy().getContainerVm(job.getContainerId(), job.getUserId()).getAllocatedMipsForContainer(datacenter.getContainerList().get(job.getContainerId()-1)));
                  Log.printLine(datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getBw());
                  Log.printLine(datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getMaxAvailableMips());
                  Log.printLine(datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getContainerVmBwProvisioner().getUsedBw());
                  Log.printLine(datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getContainerVmRamProvisioner().getUsedVmRam());
                  Log.printLine(datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getContainerVmScheduler().getTotalAllocatedMipsForContainerVm(datacenter.getVmAllocationPolicy().get(job.getVmId(), job.getUserId()).);
                  */
       
            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
                Log.print("      FAILED");                
                failed_counter++;
                Log.printLine(indent + indent + job.getResourceId() 
                        + indent + indent  + indent + indent + datacenter.getVmAllocationPolicy().getHost(job.getVmId(), job.getUserId()).getId()
                        + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + job.getContainerId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth()
                        + indent + indent + indent + dft.format(job.getProcessingCost()
                        
                        ));
            }
        }    
        Log.printLine();
        Log.printLine("MinTimeBetweenEvents is " + dft.format(CloudSim.getMinTimeBetweenEvents()));
        Log.printLine("Used MaxHeapUtilization/GB is " + dft.format(maxHeapUtilizationGB));
        Log.printLine("----------------------------------------");
        Log.printLine("The total cost is " + dft.format(cost));
        Log.printLine("The total actual cpu time is " + dft.format(time));
        Log.printLine("The length cloudlets is " + dft.format(length));    
        Log.printLine("The total failed counter is " + dft.format(failed_counter));
        Log.printLine("The total success counter is " + dft.format(success_counter));
        
    }   
}



/*Other Comments*/
/*public static void handelNumberConstants(int numMontage){            
            int numCoefficient=1;
            
            switch (numMontage){
                    case 25:
                        WFCConstants.NUMBER_MONTAGE=25;
                        numCoefficient=1;
                        break;
                    case 50:
                        WFCConstants.NUMBER_MONTAGE=50;
                        numCoefficient=2;
                        break;
                    case 100:
                        WFCConstants.NUMBER_MONTAGE=100;
                        numCoefficient=3;
                        break;
                    case 1000:
                        WFCConstants.NUMBER_MONTAGE=1000;
                        numCoefficient=4;
                        break;
                    default : 
                        WFCConstants.NUMBER_MONTAGE=25;
            }
            WFCConstants.NUMBER_HOSTS = numCoefficient * 2;
            WFCConstants.NUMBER_VMS = numCoefficient * 7;            
            WFCConstants.NUMBER_CLOUDLETS=WFCConstants.NUMBER_MONTAGE;  
      }/*
   
        /**
     * Create the data center
     *
     * @param name
     * @param datacenterClass
     * @param hostList
     * @param vmAllocationPolicy
     * @param containerAllocationPolicy
     * @param experimentName
     * @param logAddress
     * @return
     * @throws Exception
     */

/*
    public static WFCDatacenter createDatacenter(String name, Class<? extends WFCDatacenter> datacenterClass,
                                                       List<ContainerHost> hostList,
                                                       ContainerVmAllocationPolicy vmAllocationPolicy,
                                                       List<Container> containerList,
                                                       ContainerAllocationPolicy containerAllocationPolicy,
                                                       LinkedList<Storage> storageList,
                                                       String experimentName, double schedulingInterval, String logAddress, double VMStartupDelay,
                                                       double ContainerStartupDelay) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0D;
        double costPerMem = 0.05;
        double costPerStorage = 0.1; //0.001D;
        double costPerBw = 0.1; //0.0D;
 
        ContainerDatacenterCharacteristics characteristics = new
                ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage,
                costPerBw);
        WFCDatacenter datacenter = new PowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
                containerAllocationPolicy, storageList, schedulingInterval, experimentName, logAddress,
                VMStartupDelay, ContainerStartupDelay);

        return datacenter;
    }

*/

    /**
     * Create the host list considering the specs listed in the {@link WFCConstants}.
     *
     * @param hostsNumber
     * @return
     */

/*
    public static List<ContainerHost> createHostList(int hostsNumber) {
        
        ArrayList<ContainerHost> hostList = new ArrayList<ContainerHost>();
        
        for (int i = 0; i < hostsNumber; ++i) {
            
            int hostType = i / (int) Math.ceil((double) hostsNumber / 3.0D);
            ArrayList<ContainerVmPe> peList = new ArrayList<ContainerVmPe>();
            
            for (int j = 0; j < WFCConstants.HOST_PES[hostType]; ++j) {
                peList.add(new ContainerVmPe(j,
                        new ContainerVmPeProvisionerSimple((double) WFCConstants.HOST_MIPS[hostType])));
            }

            hostList.add(new PowerContainerHostUtilizationHistory(IDs.pollId(ContainerHost.class)  ,
                    new ContainerVmRamProvisionerSimple(WFCConstants.HOST_RAM[hostType]),
                    new ContainerVmBwProvisionerSimple(WFCConstants.HOST_BW), WFCConstants.HOST_STORAGE , peList,
                    new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                    WFCConstants.HOST_POWER[hostType]));
        }

        return hostList;
    }


  /**
     * Create the Virtual machines and add them to the list
     *
     * @param brokerId
     * @param containerVmsNumber
     */
        /*
    public static ArrayList<ContainerVm> createVmList0(int brokerId, int containerVmsNumber) {
        ArrayList<ContainerVm> containerVms = new ArrayList<ContainerVm>();

        for (int i = 0; i < containerVmsNumber; ++i) {
            ArrayList<ContainerPe> peList = new ArrayList<ContainerPe>();
            int vmType = i / (int) Math.ceil((double) containerVmsNumber / 4.0D);
            for (int j = 0; j < WFCConstants.VM_PES[vmType]; ++j) {
                peList.add(new ContainerPe(j,
                        new CotainerPeProvisionerSimple((double) WFCConstants.VM_MIPS[vmType])));
            }
            containerVms.add(new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId,
                    (double) WFCConstants.VM_MIPS[vmType], (float) WFCConstants.VM_RAM[vmType],
                    WFCConstants.VM_BW, WFCConstants.VM_SIZE, "Xen",
                    new ContainerSchedulerTimeSharedOverSubscription(peList),
                    new ContainerRamProvisionerSimple(WFCConstants.VM_RAM[vmType]),
                    new ContainerBwProvisionerSimple(WFCConstants.VM_BW),
                    peList, WFCConstants.SCHEDULING_INTERVAL));
        }

        return containerVms;
    }
    
        public static List<Container> createContainerList0(int brokerId, int containersNumber) {
        ArrayList<Container> containers = new ArrayList<Container>();

        for (int i = 0; i < containersNumber; ++i) {
            int containerType = i / (int) Math.ceil((double) containersNumber / 3.0D);

            containers.add(new PowerContainer(IDs.pollId(Container.class), brokerId, (double) WFCConstants.CONTAINER_MIPS[containerType], WFCConstants.
                    CONTAINER_PES[containerType], WFCConstants.CONTAINER_RAM[containerType], WFCConstants.CONTAINER_BW, 0L, "Xen",
                    new ContainerCloudletSchedulerDynamicWorkload(WFCConstants.CONTAINER_MIPS[containerType], WFCConstants.CONTAINER_PES[containerType]), WFCConstants.SCHEDULING_INTERVAL));
        }

        return containers;
    }
*/
    
    


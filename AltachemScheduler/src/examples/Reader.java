package examples;

import model.Machine;
import model.Problem;
import solution.Item;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Reader {

    public static Problem read(String pathname){

        Problem problem = new Problem();

        try{
            File file = new File(pathname);
            Scanner scanner = new Scanner(file);

            //instance name
            String instanceName = scanner.nextLine().substring(15);

            //number of machines
            int numberOfMachines = Integer.parseInt(scanner.nextLine().substring(20));

            //number of different items
            int numberOfDifferentItems = Integer.parseInt(scanner.nextLine().substring(27));

            //number of days
            int numberOfDays = Integer.parseInt(scanner.nextLine().substring(16));

            //number of requests
            int numberOfrequests = Integer.parseInt(scanner.nextLine().substring(20));

            //number of blocks per day
            int numberOfBlocksPerDay = Integer.parseInt(scanner.nextLine().substring(26));

            //index of block e ()
            int indexOfBlockE = Integer.parseInt(scanner.nextLine().substring(18));

            //index of block l ()
            int indexOfBlockL = Integer.parseInt(scanner.nextLine().substring(18));

            //index of block s ()
            int indexOfBlockS = Integer.parseInt(scanner.nextLine().substring(18));

            //index of block o ()
            int indexOfBlockO = Integer.parseInt(scanner.nextLine().substring(18));

            //minimum consecutive days with night shift
            int minimumConsecutiveDaysWithNightShift = Integer.parseInt(scanner.nextLine().substring(40));

            //past consecutive days with night shift
            int pastConsecutiveDaysWithNightShift = Integer.parseInt(scanner.nextLine().substring(41));

            //cost of overstime per overtime
            double costOfOvertimePerBlock = Double.parseDouble(scanner.nextLine().substring(22));

            //cost of nightshift per night
            double costOfNightShift = Double.parseDouble(scanner.nextLine().substring(24));

            //cost of parallel task per parallel
            double costOfParallelDay = Double.parseDouble(scanner.nextLine().substring(27));

            //penalty per item under minimum level per stock
            double costPerItemUnderMinimumStock = Double.parseDouble(scanner.nextLine().substring(42));

            //Machines data [machineID last_itemID_produced(initial_setup) days_passed_without_maintenance max_days_without_maintenance maintenance_duration_in_blocks]
            String infoMachines = scanner.nextLine();
            Machine[] machines = new Machine[numberOfMachines];
            for (int i=0; i<numberOfMachines; i++){
                String[] machineData = scanner.nextLine().split(" ");
                machines[i] = new Machine();
                machines[i].setMachineId(Integer.parseInt(machineData[0]));
                machines[i].setLastItemIdProduced(Integer.parseInt(machineData[1]));
                machines[i].setDaysPastWithoutMaintenance(Integer.parseInt(machineData[2]));
                machines[i].setMaxDaysWithoutMaintenance(Integer.parseInt(machineData[3]));
                machines[i].setMaintenanceDurationInBlocks(Integer.parseInt(machineData[4]));
            }

            //Items data [itemID cost_per_item quantity_in_stock min_allowed_in_stock max_allowd_in_stock]
            String infoItems = scanner.nextLine();
            Item[] items = new Item[numberOfDifferentItems];
            for (int i=0; i<numberOfDifferentItems; i++){
                String[] itemData = scanner.nextLine().split(" ");
                items[i] = new Item();
                items[i].setItemId(Integer.parseInt(itemData[0]));
                items[i].setCostPerItem(Double.parseDouble(itemData[1]));
                items[i].setQuantityInStock(Integer.parseInt(itemData[2]));
                items[i].setMinAllowedInStock(Integer.parseInt(itemData[3]));
                items[i].setMaxAllowedInStock(Integer.parseInt(itemData[4]));
            }

            //Machine efficiency per item [itemID production_in_machine_0_per_block production_in_machine_1_per_block]
            String infoMachineEfficienties = scanner.nextLine();
            int[][] machineEfficienciesPerItem = new int[numberOfDifferentItems][numberOfMachines+1];
            for (int i=0; i<numberOfDifferentItems; i++){
                String[] machineEfficientiesData = scanner.nextLine().split(" ");
                for (int j=0; j<numberOfMachines+1; j++){
                    machineEfficienciesPerItem[i][j] = Integer.parseInt(machineEfficientiesData[j]);
                }
            }

            //Large setup description matrix [#items X #items]. [i,j] is 1 if changing production from item i to j is considered a large setup, 0 if considered small. If i == j there is no cost involved.
            String infoDescription = scanner.nextLine();
            boolean[][] isLargeChangeover = new boolean[numberOfDifferentItems][numberOfDifferentItems];
            for (int i=0; i<numberOfDifferentItems; i++){
                String[] isLargeChangeoverData = scanner.nextLine().split(" ");
                for (int j=0; j<numberOfDifferentItems; j++)
                    isLargeChangeover[i][j] = Integer.parseInt(isLargeChangeoverData[j]) != 0;
            }

            //Machine setup duration in blocks [#items X #items]. [i,j] represents the number of blocks it takes to change production from item i to item j
            String infoSetupDuration = scanner.nextLine();
            int[][] machineSetupDuration = new int[numberOfDifferentItems][numberOfDifferentItems];
            for (int i=0; i<numberOfDifferentItems; i++){
                String[] machineSetupDurationData = scanner.nextLine().split(" ");
                for (int j=0; j<numberOfDifferentItems; j++)
                    machineSetupDuration[i][j] = Integer.parseInt(machineSetupDurationData[j]);
            }

            //Shipping day matrix [#request X #shipping_day]. [i,j] is 1 if request i can be shipped on day j, 0 otherwise
            String infoShippingDays = scanner.nextLine();
            boolean[][] shippingDays = new boolean[numberOfrequests][numberOfDays];
            for (int i=0; i<numberOfrequests; i++){
                String[] shippingDaysData = scanner.nextLine().split(" ");
                for (int j=0; j<numberOfDays; j++)
                    shippingDays[i][j] = Integer.parseInt(shippingDaysData[j]) != 0;
            }

            //Requested items matrix [#request X #items]. position [i,j] represents: number of items j requested by request i
            String infoRequestedItems = scanner.nextLine();
            int[][] requestedItems = new int[numberOfrequests][numberOfDifferentItems];
            for (int i=0; i<numberOfrequests; i++){
                String[] requestedItemsData = scanner.nextLine().split(" ");
                for (int j=0; j<numberOfDifferentItems; j++)
                    requestedItems[i][j] = Integer.parseInt(requestedItemsData[j]);
            }

            //TODO: plaats alle info in Problem, ...

            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }

        return problem;

    }

}

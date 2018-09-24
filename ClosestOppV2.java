package summativePackage;
import java.awt.Color;
import becker.robots.*;

/**
 * Robot Summative Template Class - 2 different factors used (Health and Distance)
 * @author pratham thukral
 * @version june 16, 2017
 */
public class ClosestOppV2 extends FighterRobot{	
	//constant attributes for the robot
	private static final int ATTACK = 4;
	private static final int DEFENSE = 3;
	private static final int NUMMOVES = 3;

	//number of total players in the game
	private static final int NUMPLAYERS = BattleManager.NUM_PLAYERS;

	//PROPRORTIONALITY used to make "weighted" decisions
	private static final double HEALTHPERCENT = 0.15;
	private static final double DISTANCEPERCENT = 0.75;

	//stores the distances and healths of the various opposing robots
	private static int[][] dist = new int[NUMPLAYERS][2];
	private static int[][] health = new int[NUMPLAYERS][2];

	//global stored variable for the robot's health
	private static int healthGlobal;

	/**
	 * constructor class to create a fighter class
	 * @param c is for the city or arena 
	 * @param a is the initial avenue for the robot
	 * @param s is the initial street for the robot
	 * @param d is the initial direction, for all the initial direction is north
	 * @param id is the id of this particular robot
	 * @param health is the current amount of health for the robot
	 */
	public ClosestOppV2(City c, int y, int x, Direction d,int id, int health) {
		super(c, y, x, d, id, ATTACK, DEFENSE, NUMMOVES);
		healthGlobal = health;

		this.setLabel(healthGlobal);
	}

	public void setLabel(int health){
		//sets the label to show the id and health of the robot
		super.setLabel(super.getID() + ":" + healthGlobal);

		//if the robot is dead
		if(health==0){
			//change the color to black
			super.setColor(Color.BLACK);
		}
		else{
			//else leave it as red
			super.setColor(Color.RED);
		}
	}

	/**
	 * actually moves the robot to the requested location
	 * @param a stores the requested avenue value
	 * @param s stores the requested street value
	 */
	@Override
	public void goToLocation(int a, int s) {		
		//if the robot is to the right of the requested avenue
		if(super.getAvenue()>a){
			//face west
			this.faceDirection(Direction.WEST);
		}
		//if the robot is to the left of the requested avenue
		else if(super.getAvenue()<a){
			//face east
			this.faceDirection(Direction.EAST);
		}

		//keeps moving the robot until it has landed on the requested avenue
		while(super.getAvenue()!=a){
			super.move();
		}

		//if the robot is below the requested street
		if(super.getStreet()>s){
			//turn towards north
			this.faceDirection(Direction.NORTH);
		}
		//if the robot is above the requested street
		else if(super.getStreet()<s){
			//turn towards south
			this.faceDirection(Direction.SOUTH);
		}

		//keep moving the robot until it lands on the requested street
		while(super.getStreet()!=s){
			super.move();
		}

		//		y axis = street
		//		x axis = avenue
	}

	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		//adds dist, health, and id to 2 arrays
		for (int i = 0; i < NUMPLAYERS; i++) {
			dist[i][1] = data[i].getID();
			health[i][1] = data[i].getID();

			dist[i][0] = this.getDistance(data, i);
			health[i][0] = data[i].getHealth();
		}


		//sorts the two arrays in terms of the healths and distances
		insertionSort(dist);
		insertionSort(health);

		//scores + id
		double[][] scores = new double[NUMPLAYERS][2];

		//loops through each player and mergest the dist and health array into one score array
		for (int idCount = 0; idCount < NUMPLAYERS; idCount++) {			
			//initial variables that will be assinged values for the current robots attributes
			int healthVal = -1;
			int distVal = -1;
			double scoreVal;

			//the id is set to the for loop counter
			scores[idCount][1] = idCount;

			//loop through each element in the array of health until you find the idCount
			int idPickHealth=-1;
			//find the location of idCount in the separate array
			for (int i = 0; i < NUMPLAYERS; i++) {
				if(health[i][1]==idCount){
					//if it is found, break the loop
					idPickHealth = i;
					break;
				}
			}

			//loop through each element in the array of dist until you find the idCount
			int idPickDist=-1;
			//find the location of idCount in the separate array
			for (int i = 0; i < NUMPLAYERS; i++) {
				if(dist[i][1]==idCount){
					//if it is found, break the loop
					idPickDist = i;
					break;
				}
			}

			//health and distance values are updated to match the found indexes
			healthVal = health[idPickHealth][0];
			distVal = dist[idPickDist][0];

			//score value is calculated by 
			scoreVal = (HEALTHPERCENT*healthVal)+(DISTANCEPERCENT*distVal);

			//adds the scoreVal to the proper location in the scores array
			scores[idCount][0] = scoreVal;
		}
		//sorts the merged array of scores
		insertionSort(scores);

		//values will be assigned to match up with the target
		int aveSelection = 0;
		int strSelection = 0;
		int idSelection = 0;
		boolean validPick = false;
		//loop through each player and decide on the best robot to attack based on score and validity
		for (int i = 0; i < NUMPLAYERS; i++) {
			//idValue is used from the sorted scores array
			int idValue = (int) scores[i][1];

			//ave and str selection are set to the current robot's coordinates
			aveSelection = data[idValue].getAvenue();
			strSelection = data[idValue].getStreet();

			//if the selected robot can be attacked (ie. it is alive, not too far, etc.)
			if(idValue!=super.getID() && (this.getDistance(data,idValue)*5<=energy) && (data[idValue].getHealth()!=0) && (this.getDistance(data, idValue)<=NUMMOVES)){
				//the selected id is the current robot's id
				idSelection = idValue;
				//the pick is valid
				validPick = true;

				//break the loop and proceed to the next section
				break;
			}
			//if it cannot be attacked (ie. it is too far, or it is dead, etc.)
			else if(super.getID()!=idValue && data[idValue].getHealth()!=0 && (NUMMOVES*5)<=energy){
				//id is still set to the idValue
				idSelection = idValue;
				//the pick is not valid - so move to the next step but check to see the best choices 
				validPick = false;
				break;
			}
			//else leave the id as -1
			else{
				idSelection = -1;
				validPick = false;
			}

		}

		//request is currently set so the robot does nothing
		TurnRequest request = new TurnRequest(super.getAvenue(),super.getStreet(),-1,0);

		//if the requested target is not valid
		if(!validPick && idSelection!=-1){
			// if the attacked robot is too far
			if((Math.abs(super.getAvenue()-aveSelection)+Math.abs(super.getStreet()-strSelection))>NUMMOVES){
				request = (robotTooFar(data,idSelection));			
			}
			//while the id is not valid keep adding one, (moving down the list of scores)
			while(idSelection==0 || data[idSelection].getHealth()<=0){
				idSelection++;

				//if it reaches the end, start from the beginning again
				if(idSelection==NUMPLAYERS){
					idSelection =0;
				}
			}

		}
		//if energy is 0 - do not attack and do not move
		else if(idSelection==-1 || energy==0){
			request = new TurnRequest(super.getAvenue(),super.getStreet(),-1,0);
		}
		//else attack 3 times 
		else{
			request = new TurnRequest(aveSelection,strSelection,idSelection,3);
		}

		//send the request to the battlemanager to make sure it is valid
		return request;
	}


	/**
	 * if the robot is too far from the requested target
	 * @param data is the OppData Array of data 
	 * @param idSelection is the current selected id for attacking
	 * @return the best location to move to, preferably close to the robot that you want to attack
	 */
	private TurnRequest robotTooFar(OppData[] data,int idSelection){
		//ave and str distance is assigned the distance in the axis to the robot 
		int aveDistance = Math.abs(super.getAvenue()-data[idSelection].getAvenue());
		int strDistance = Math.abs(super.getStreet()-data[idSelection].getStreet());

		//ave and str selection are the target's coordinates
		int aveSelection = data[idSelection].getAvenue();
		int strSelection = data[idSelection].getStreet();

		//the request is default set to make sure the robot doesn't move or attack
		TurnRequest request= new TurnRequest(super.getAvenue(),super.getStreet(),-1,0);

		//if both distances are too far
		if(aveDistance>NUMMOVES && strDistance>NUMMOVES){
			//move NUMMOVES to the left
			if (aveSelection<super.getAvenue()){
				request = new TurnRequest(super.getAvenue()-NUMMOVES,super.getStreet(),-1,0);
			}
			//move NUMMOVES to the right
			else{
				request = new TurnRequest(super.getAvenue()+NUMMOVES,super.getStreet(),-1,0);
			}

		}
		//them added together is too large
		else if(strDistance<NUMMOVES && aveDistance<NUMMOVES && (strDistance+aveDistance)>NUMMOVES){
			//move to avenue but only part way to the street
			if (strSelection<super.getStreet()){
				request = new TurnRequest(aveSelection,super.getStreet()-(NUMMOVES-aveDistance),-1,0);
			}
			//move to avenue but only part way to the street
			else{
				request = new TurnRequest(aveSelection,super.getStreet()+(NUMMOVES-aveDistance),-1,0);
			}

		}
		//if the avenue is more than the number of moves but the street is close
		else if(aveDistance>=NUMMOVES && strDistance<NUMMOVES){
			//move to the street but only part way to the avenue
			if (aveSelection<super.getAvenue()){
				request = new TurnRequest(super.getAvenue()+strDistance-NUMMOVES,strSelection,-1,0);
			}
			//move to the street but only part way to the avenue
			else{
				request = new TurnRequest(super.getAvenue()-strDistance+NUMMOVES,strSelection,-1,0);
			}

		}
		//if the street is more but the avenue is less
		else if(strDistance>=NUMMOVES && aveDistance<NUMMOVES){
			//move to the avenue but only part way to the street
			if (strSelection<super.getStreet()){
				request = new TurnRequest(aveSelection,super.getStreet()+aveDistance-NUMMOVES,-1,0);
			}
			//move to the avenue but only part way to the street
			else{
				request = new TurnRequest(aveSelection,super.getStreet()-aveDistance+NUMMOVES,-1,0);
			}

		}

		//return the "closer" coordinates  
		return request;

	}


	private static void insertionSort(int[][] arr){
		//sorts the id array
		int j, key;
		int[] temp;
		for (int i = 1; i < NUMPLAYERS; i++) {
			key = arr[i][0];
			j=i-1;
			while(j>=0 && key<arr[j][0]){
				//inserts the number into the proper position if it is on the RHS of the "wall"
				temp = arr[j];
				arr[j] = (arr[j+1]);
				arr[j+1]= (temp);
				j--;
			}
		}

	}

	private static void insertionSort(double[][] arr){
		//sorts the id array
		int j;
		double key;
		double[] temp;
		for (int i = 1; i < NUMPLAYERS; i++) {
			key = arr[i][0];
			j=i-1;
			while(j>=0 && key<arr[j][0]){
				//inserts the number into the proper position if it is on the RHS of the "wall"
				temp = arr[j];
				arr[j] = (arr[j+1]);
				arr[j+1]= (temp);
				j--;
			}
		}


	}

	/**
	 * used to get the distance between two robots
	 * @param oppData is the formal parameter of the OppData Array 
	 * @param index is the index of the robot being analyzed
	 * @return the distance between the two robots as an integer
	 */
	private int getDistance(OppData[] oppData,int index) {
		//finds distance
		int ave = oppData[index].getAvenue();
		int st = oppData[index].getStreet();

		//absolute value of changes in x and y axes
		int distance = Math.abs(super.getAvenue()-ave)+Math.abs(super.getStreet()-st);

		//returns the distance between the robots
		return distance;
	}

	/**
	 *modifies the global health value
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		//lowers the global health value
		health[super.getID()][0] -= healthLost;

		//turns the robot black after checking the data
		this.setLabel(healthGlobal);
	}

	/**
	 * keeps turning until it faces the required direction
	 * @param d is the direction requested by the function
	 */
	private void faceDirection(Direction d){
		//while not facing right direction, keep turning
		while (super.getDirection()!=d){
			super.turnLeft();
		}
	}
}

package summativePackage;
import java.awt.Color;

import becker.robots.*;

/**
 * Robot Summative Template Class - 4 different factors used to make the best decision on who to to attack
 * @author pratham thukral
 * @version june 16, 2017
 */
public class ThukralFighterRobot extends FighterRobot{	
	//constant attributes for the robot
	private static final int ATTACK = 4;
	private static final int DEFENSE = 3;
	private static final int NUMMOVES = 3;

	//number of total players in the game
	private static final int NUMPLAYERS = BattleManager.NUM_PLAYERS;

	//PROPRORTIONALITY used to make "weighted" decisions
	private static final double HEALTHPERCENT = 0.15;
	private static final double DISTANCEPERCENT = 0.65;
	private static final double MAXDISTANCEPERCENT = 0.05;
	private static final double PREVFIGHTSPERCENT = 0.15;

	//stores the distances and healths of the various opposing robots
	private static int[][] dist = new int[NUMPLAYERS][2];
	private static int[][] health = new int[NUMPLAYERS][2];

	//used to make the OppData parameter global throughout the template class
	private static OppData[] data;

	//used to store the health of my robot
	private static int healthGlobal;

	//used to store the results of each round to make it more/less likely to attack the same robot
	private static int[] fightResults = new int[NUMPLAYERS];

	//creates an array used to store the additional pieces of information from the child-class of OppData
	private static ThukralOppData[] newData = new ThukralOppData[NUMPLAYERS];
	
	//used to tell if the game is still in its first round, or not
	private static int roundCount = 0;

	/**
	 * constructor class to create a fighter class
	 * @param c is for the city or arena 
	 * @param a is the initial avenue for the robot
	 * @param s is the initial street for the robot
	 * @param d is the initial direction, for all the initial direction is north
	 * @param id is the id of this particular robot
	 * @param health is the current amount of health for the robot
	 */
	public ThukralFighterRobot(City c, int y, int x, Direction d,int id, int health) {
		super(c, y, x, d, id, ATTACK, DEFENSE, NUMMOVES);

		//assigns the global health value
		healthGlobal = health;

		//sets label to black if the robot has no health leftover
		this.setLabel(healthGlobal);

		//initializes the array of fighting results to 0 (bc no rounds have taken place so far)
		for (int i = 0; i < NUMPLAYERS; i++) {
			fightResults[i] = 0;
		}
	}

	/**
	 * setLabel is used to set the colour of the robot to black if it has a health value of 0 
	 * @param health is the value of the health of the robot being checked
	 */
	public void setLabel(int health){
		//sets the label to show the id and health of the robot
		super.setLabel(super.getID() + ":" + healthGlobal);

		//if the robot is dead
		if(health==0){
			//change the color to black
			super.setColor(Color.BLACK);
		}
		else{
			//else leave it as white
			super.setColor(Color.WHITE);
		}
	}

	/**
	 * moves the robot to the requested location
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

		//	y axis = street
		//	x axis = avenue
	}


	/**
	 * takeTurn is used to decide (using various pieces of data) which robot to attack and where to move
	 * @param energy is the energy of the template class robot
	 * @param data1 is the formal parameter for the OppData array
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data1) {
		//ave and str selection are to be assigned the value of the target's coordinates (needs an initializing value)
		int aveSelection = 0;
		int strSelection =0 ;
		//idSelection is the target's id
		int idSelection = -1;
		//validpick is used to see if the decision to attack a robot is valid or not 
		boolean validPick = false;

		//will be used to store the scores of the robots and their id
		double[][] scores = new double[NUMPLAYERS][2];
		
		//updates the health of my robot
		healthGlobal = data1[super.getID()].getHealth();
		
		//makes the data from OppData global to the entire class
		data = data1;
		
		//if round is just starting, just set up the constructor
		for (int i = 0; i < NUMPLAYERS; i++) {
			if(roundCount == 0){
				//constructor
				newData[i]=new ThukralOppData(data1[i]);
			}
		}
		
		//after running, add 1 so it can never be the "first round" again
		roundCount++;

		//adds dist, health, id, and nummoves to the arrays
		for (int i = 0; i < NUMPLAYERS; i++) {
			//element at 1 will store the id
			dist[i][1] = data[i].getID();
			health[i][1] = data[i].getID();
			
			//element at 0 will store the actual data for that robot
			dist[i][0] = this.getDistance(data, i);
			health[i][0] = data[i].getHealth();
			
			//if it is not the first round - update the location of all robots
			if(roundCount!=0){
				newData[i].updateLocation(data[i].getAvenue(), data[i].getStreet(),i);
			}
		}

		//loop through each player and calculate a score for them, based of the different factors (Health,Distance,NumMoves,and Previous fights)
		for (int i = 0; i < NUMPLAYERS; i++) {
			//stores the actual value that the robot scores
			double scoreVal = 0;
			//stores the values for the distance and health of the robot
			int distVal = dist[i][0];
			int healthVal = health[i][0];

			//if the robot is 0 distance away and it is not the robot itself, make the value extremely low
			if(distVal==0 && healthVal>0 && super.getID()!=i){
				//make the value extremely low making it very appealing
				//also make it lower if the fight results are in favour of the attacking robot
				scoreVal = -1000-(fightResults[i]);
			}
			else if(healthVal==0){
				scoreVal = 10000;
			}
			//takes 4 pieces of information to make best decision: 
			/*
			 * health value of the opponent
			 * distance between my robot and the opponent
			 * maximum distance the opponent robot can travel
			 * previous fight results between the opponent and my robot
			 */
			else{
				scoreVal = (HEALTHPERCENT*healthVal)+(DISTANCEPERCENT*distVal)+(MAXDISTANCEPERCENT*newData[i].getMaxDist())+(PREVFIGHTSPERCENT*fightResults[i]);
			}

			//adds the id value and the scoreVal to the scores 2dimensional array
			scores[i][1] = i;
			scores[i][0] = scoreVal;
		}

		//sorts the scores from lowest (most appealing) to highest (least appealing)
		insertionSort(scores);
		
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
		//else attack the robot
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

	/**
	 * insertion sort used to organize the scores array
	 * @param arr is the inputted array, in this case it will be the scores
	 */
	private static void insertionSort(double[][] arr){
		int j;
		double key;
		double[] temp;

		//sorts the array
		for (int i = 1; i < NUMPLAYERS; i++) {
			key = arr[i][0];
			j=i-1;
			//while the current value is less than the next one, keep "shifting" it one space down
			while(j>=0 && key<arr[j][0]){
				//inserts the number into the proper position if it is on the RHS of the "wall"
				temp = arr[j];
				
				//switches
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
	 * battle result is used to monitor the global health value and the results of each fight
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		//subtracts the healthLost in the battle from the global health value
		healthGlobal -= healthLost;

		//if my robot lost more health than the opponent, then raise the value so it is less likely to attack it again
		if(oppID!=-1){
			//if my robot lost more than opposing, add 5 --> makes it less appealing
			if(healthLost>oppHealthLost){
				fightResults[oppID]+=5;
			}
			//if my robot lost less than opposing, subtract 5 --> makes it more appealing
			else{
				fightResults[oppID]-=5;
			}
		}
		
		//turns the robot black once checking the data
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

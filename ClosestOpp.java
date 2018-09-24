package summativePackage;
import java.awt.Color;

import becker.robots.*;

/**
 * Robot Summative Template Class - 2 different factors used (Health and Distance)
 * @author pratham thukral
 * @version june 16, 2017
 */
public class ClosestOpp extends FighterRobot{	
	//constant attributes for the robot
	private static final int ATTACK = 4;
	private static final int DEFENSE = 3;
	private static final int NUMMOVES = 3;

	//number of total players in the game
	private static final int NUMPLAYERS = BattleManager.NUM_PLAYERS;

	//stores the distances of the various opposing robots
	private static int[][] dist = new int[NUMPLAYERS][2];

	//stores the health of the robot as a global variable
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
	public ClosestOpp(City c, int y, int x, Direction d,int id, int health) {
		super(c, y, x, d, id, ATTACK, DEFENSE, NUMMOVES);

		//assigns the global health value
		healthGlobal = health;

		//sets label to black if the robot has no health leftover
		this.setLabel(healthGlobal);
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

	/**
	 * this method sorts the data to ensure the robot makes the most efficient decision on who to attack
	 * @param energy is the current robot's amount of energy
	 * @param data is an array of OppData containing information about the robots
	 */
	@Override
	public TurnRequest takeTurn(int energy, OppData[] data) {
		//adds dist, health, and id to 2 arrays
		for (int i = 0; i < NUMPLAYERS; i++) {
			dist[i][1] = data[i].getID();
			dist[i][0] = this.getDistance(data, i);
		}

		//sorts the merged array of scores
		insertionSort(dist);

		//will be assigned the values of the targetted robot
		int aveSelection = super.getAvenue();
		int strSelection = super.getStreet() ;
		int idSelection=-1;

		//loop through each player and decide on the best robot to attack based only on distance		
		for (int i = 0; i < NUMPLAYERS; i++) {
			//if the robot can actually move to the location AND the target is not dead AND the target is not itself
			if(dist[i][0]<=NUMMOVES && data[i].getHealth()!=0 && dist[i][1]!=super.getID()){
				//if all are true then note down the robot's id to attack
				idSelection = dist[i][1]; 
				break;
			}
			//else leave the id as -1 so the robot will not move
			else{
				idSelection = -1;
			}

		}

		//request is currently set so the robot does nothing
		TurnRequest request = new TurnRequest(super.getAvenue(),super.getStreet(),-1,0);
		//if energy is 0 - do not attack and do not move
		if(idSelection==-1 || energy==0){
			request = new TurnRequest(super.getAvenue(),super.getStreet(),-1,0);
		}
		//else - target the requested robot and use the coordinates in the TurnRequest
		else{
			aveSelection = data[idSelection].getAvenue();
			strSelection = data[idSelection].getStreet();

			//request the target robot's location and id
			request = new TurnRequest(aveSelection,strSelection,idSelection,3);
		}

		//send the request to the battlemanager to make sure it is valid
		return request;
	}

	/**
	 * sorts the array of health and distance from lowest to highest
	 * @param arr
	 */
	private void insertionSort(int[][] arr) {
		int j;
		int key;
		int[] temp;

		//sorts the array
		for (int i = 1; i < NUMPLAYERS; i++) {
			key = arr[i][0];
			j=i-1;
			//while the current value is less than the next one, keep "shifting" it one space down
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
	 * battle result is used to monitor the global health value and the results of each fight
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		//subtract health lost from the global variable
		healthGlobal-=healthLost;

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

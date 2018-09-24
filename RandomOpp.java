package summativePackage;
import java.awt.Color;
import java.util.Random;

import becker.robots.*;

/**
 * Robot Summative Template Class - 0 different factors used, selects robots at random
 * @author pratham thukral
 * @version june 16, 2017
 */
public class RandomOpp extends FighterRobot {
	//constant attributes for the robots
	private static final int ATTACK = 3;
	private static final int DEFENSE = 2;
	private static final int NUMMOVES = 5;

	//number of players in teh game
	private static final int NUMPLAYERS = BattleManager.NUM_PLAYERS;

	//health of current robot
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
	public RandomOpp(City c, int y, int x, Direction d,int id, int health) {
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
		//request is set to a default number, will be assigned later
		TurnRequest request=new TurnRequest(0,0,0,0);

		//randomly generate a number for which robot to attack
		Random rand = new Random();
		int opponentID = -1;
		int i = -1;

		while(true){
			//randomly generated number for who to attack
			opponentID=rand.nextInt(NUMPLAYERS);			
			
			//distance between both robots
			int dist = Math.abs(data[opponentID].getAvenue()-super.getAvenue())+Math.abs(data[opponentID].getStreet()-super.getStreet());
			
			//if the robot doesn't attack itself and it is not too far away
			if (data[opponentID].getID()!=super.getID() && dist<=NUMMOVES){
				break;
			}
			//else leave the id for the target as -1
			else if(i==20){
				opponentID=-1;
				break;
			}

			i++;
		}

		//if the robot has more than 10 energy, try to move to the location
		if(energy>=10 && opponentID!=-1){
			//find the coordinates of the random opponent
			int ave = data[opponentID].getAvenue();
			int street = data[opponentID].getStreet();
			
			//request to attack that robot 3 times
			request = new TurnRequest(ave, street, opponentID, 3);
		}
		//else stay at same location
		else{
			//request not to move and not to attack
			request = new TurnRequest(super.getAvenue(), super.getStreet(), -1, 0);
		}

		//request the location and id of the target
		return request;
	}

	/**
	 * battle result is used to monitor the global health value and the results of each fight
	 */
	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		//removes amount of health from the global variable
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

package summativePackage;

/**
 * ThukralOppData is used to monitor the maximum number of movements of each robot 
 * @author pratham thukral
 * @version june 19, 2017
 */
public class ThukralOppData extends OppData{
	//used to store the maximum distance of the particular robot
	private int maxDistance = 0;
	
	//stores values for cur and prev coordinates as well as the current distance moved
	private int curAve,curStr,curDistance, prevAve, prevStr;

	/**
	 * this is the constructor for OppData and additionally it sets the initial positions of the robot
	 * @param data is the parameter passed into the method to set up the constructor for OppData
	 */
	public ThukralOppData(OppData data) {
		super(data.getID(), data.getAvenue(), data.getStreet(), data.getHealth());
		
		//initializes the coordinates of the robot
		this.prevAve = data.getAvenue();
		this.prevStr = data.getStreet();
		
		this.curAve = data.getAvenue();
		this.curStr = data.getStreet();
	}

	/**
	 * simple query method that returns the maximum distance that the bot can travel
	 */
	public int getMaxDist(){
		return this.maxDistance;
	}

	/**
	 * updates the location of a particular robot and calcultates the maximum distance
	 * @param a is the new avenue the robot is at
	 * @param s is the new street the robot is at
	 * @param ID is the id of the particular robot
	 */
	public void updateLocation(int a, int s, int ID){		
		//refreshes the previous and current locations
		this.prevAve = curAve;
		this.prevStr = curStr;
		this.curAve = a;
		this.curStr = s;
		
		//calculates the current distance of the robot
		this.curDistance = Math.abs(this.prevAve-this.curAve) + Math.abs(this.prevStr-this.curStr);

		//if the current distance is more than or equal to the max distance  
		if(this.curDistance>=this.maxDistance){
			//then make the current distance the maximum distance
			this.maxDistance = this.curDistance;
		}
	}

}

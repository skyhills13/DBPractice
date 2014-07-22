
public class Ship {
	public int sid;
	public int uid;
	public int gid;
	public int atk;

	public void initialize(){
		this.atk = (int) Math.floor(Math.random() * 100); 
	}
}

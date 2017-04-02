/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.game.controls;
 
import my.game.network.messages.*;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.NetworkClient;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Peter
 */
public class NetworkEnableControl extends AbstractControl{
	//Any local variables should be encapsulated by getters/setters so they
	//appear in the SDK properties window and can be edited.
	//Right-click a local variable to encapsulate it with getters and setters.

	protected NetworkClient client;
	protected long id;
	protected String parentName;
	protected Spatial parent;
	//protected Node rootNode;
	protected Vector3f translation = new Vector3f();
	protected Vector3f velocity = new Vector3f();
	protected Vector3f rotation = new Vector3f();
	protected Vector3f oldWorldRotation = new Vector3f();
	protected Matrix3f matrix = new Matrix3f();
	protected Matrix3f endMatrix;
	protected Vector3f spin = new Vector3f();
	protected Vector3f worldSpin = new Vector3f();
	protected Quaternion q = new Quaternion();
	protected Vector3f oldRotation = new Vector3f();
	//protected String action = new String();
	//protected boolean isPressed = false;
	protected boolean velocityChanged = false;
	protected boolean spinChanged = false;
	protected float tpf = -1;
	//protected float degree = -1;
	//protected Quaternion rv = new Quaternion();
	//protected int frame = 0;
	
	
	NetworkEnableControl(){
		
	}
	
	NetworkEnableControl(NetworkClient client, long id){
		this.client = client;
		this.id = id;
	}
	

	@Override
	public void setSpatial(Spatial spatial){
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been added to a Spatial");
        }
		this.spatial = spatial;
		parent = this.spatial.getParent();
		this.spatial.setUserData("controlled", false);
		this.spatial.setUserData("setControlled", false);
		System.out.println("NEC.setSpatial()" + "(" + this.spatial.getName() + ")");
	}
	

	//do *******************************************************************************
	//if no client, set spatial, otherwise, doSync will get echo from server and set spatial
	public void doSetLocalTranslation(Vector3f translation){
		spatial.setLocalTranslation(translation);
	}
	
	
	public void doMove(Vector3f velocity){
		//System.out.println("NEC.doMove()" + "(" + spatial.getName() + ")");
		if (spatial.getUserData("controlled")) System.out.println("NEC.doMove() (" + spatial.getName() + "), velocity=" + velocity + ", tpf=" + tpf);
		spatial.move(velocity.x * tpf, velocity.y * tpf, velocity.z * tpf);
		translation = spatial.getLocalTranslation();
	}
	
	
	public void doSetLocalRotation(Quaternion q){
		spatial.setLocalRotation(q);
	}
	
	
	public void doSetLocalRotation(Matrix3f matrix){
		spatial.setLocalRotation(matrix);
	}
	

	public void doSetLocalRotation(Vector3f rotation){
		Quaternion q = new Quaternion().fromAngles(rotation.x, rotation.y, rotation.z);
		spatial.setLocalRotation(q);
	}

	public void doRotate(Vector3f spin){
		if (spatial.getUserData("controlled")) System.out.println("NEC.doRotate()" + "(" + spatial.getName() + ")" + ", spin=" + spin + ", tpf=" + tpf);
		spatial.rotate(spin.x * tpf, spin.y * tpf, spin.z * tpf);
		float[] rad = new float[3];
		spatial.getLocalRotation().toAngles(rad);
		rotation.set(rad[0], rad[1], rad[2]);
	}
	
	public void doSetVelocity(Vector3f velocity){
		this.velocity = velocity;
	}
/*
	public void doAddVelocity(Vector3f delta){
		if (client == null){
			this.velocity.addLocal(acc);
		}
		//else wait for network sync
	}
*/
	public void doSpin(Vector3f spin){//doSpin
		this.spin = spin;
	}
	
	//syncs
	public void sendTranslationSync(Vector3f translation){
		if (client == null)
			doSetLocalTranslation(translation);
		else
			client.send(new NetworkTranslationMessage(id, translation));
	}
	
	public void sendMoveSync(Vector3f velocity){
		//System.out.println("NEC.sendMoveSync()" + ", client=" + client);
		if (client == null)
			doMove(velocity);
		else
			client.send(new NetworkMoveMessage(id, velocity));
	}
	
	public void sendVelocitySync(Vector3f velocity){
		if (client == null)
			doSetVelocity(velocity);
		else
			client.send(new NetworkVelocityMessage(id, velocity));
	}
	
	public void sendRotationSync(Quaternion rotation){
		if (client == null)
			doSetLocalRotation(rotation);
		else
			client.send(new NetworkQuaternionMessage(id, rotation));
	}
	
	public void sendRotationSync(Matrix3f rotation){
		if (client == null)
			doSetLocalRotation(rotation);
		else
			client.send(new NetworkMatrixMessage(id, rotation));
	}
	
	public void sendRotationSync(Vector3f rotation){
		if (client == null)
			doSetLocalRotation(rotation);
		else
			client.send(new NetworkRotationMessage(id, rotation));
	}
	
	public void sendRotateSync(Vector3f spin){
		if (client == null)
			doRotate(spin);
		else
			client.send(new NetworkRotateMessage(id, spin));
	}
	
	public void sendSpinSync(Vector3f spin){
		if (client == null)
			doSpin(spin);
		else
			client.send(new NetworkSpinMessage(id, spin));
	}
	
	public void sendActionSync(String action, boolean isPressed){
		if (client == null)
			doPerformAction(action, isPressed);
		else
			client.send(new NetworkActionMessage(id, action, isPressed));
	}
	
	//actions
	//translation
	public Vector3f setLocalTranslation(Vector3f translation){
		Vector3f vel = spatial.getLocalTranslation().subtract(translation);
		sendTranslationSync(translation);
		return(vel);
	}
	
	public Vector3f getLocalTranslation(){
		return(spatial.getLocalTranslation());
	}

	public Vector3f getWorldTranslation(){
		return(spatial.getWorldTranslation());
	}

/*	
	public void move(Vector3f velocity, float tpf){
		doMove(velocity, tpf);
		sendTranslationSync();
	}
*/
	
	public void move(float tpf){
		//if (spatial.getName().equals("Ship 01")) System.out.println("NEC.move() (" + spatial.getName() + "), velocity=" + velocity + ", tpf=" + tpf);
		this.tpf = tpf;
		sendMoveSync(velocity);
	}
	
	//set velocity
	public void setVelocity(Vector3f velocity){
		sendVelocitySync(velocity);
	}

	public void addVelocity(Vector3f delta){
		velocity = velocity.add(delta);
		sendVelocitySync(velocity);
	}

	public Vector3f getVelocity(){
		return(velocity);
	}

	//rotation
	public void setLocalRotation(Quaternion q){//in autoLevel2
		sendRotationSync(q);
	}

	public void setLocalRotation(Matrix3f matrix){
		sendRotationSync(matrix);
	}

	public void setLocalRotation(Vector3f vector){
		Quaternion q = new Quaternion().fromAngles(vector.x, vector.y, vector.z);
		sendRotationSync(q);
	}

	public Quaternion getLocalRotationQuaternion(){
		return(spatial.getLocalRotation());
	}

	public Matrix3f getLocalRotationMatrix(){
		return(spatial.getLocalRotation().toRotationMatrix());
	}

	public Vector3f getLocalRotationVector(){
		float[] rad = new float[3];
		spatial.getLocalRotation().toAngles(rad);
		Vector3f tRot = new Vector3f(rad[0], rad[1], rad[2]);
		return(tRot);
	}

	public Quaternion getWorldRotationQuaternion(){
		return(spatial.getWorldRotation());
	}

	public Matrix3f getWorldRotationMatrix(){
		return(spatial.getWorldRotation().toRotationMatrix());
	}

	public Vector3f getWorldRotationVector(){
		//System.out.println("1velocity=" + this.velocity);
		float[] rad = new float[3];
		spatial.getWorldRotation().toAngles(rad);
		Vector3f tRot = new Vector3f(rad[0], rad[1], rad[2]);
		return(tRot);
	}

	public Vector3f getRotationVelocity(){
		return(spin);
	}

/*
	public Vector3f getRotationVelocity(Vector3f newRotation){
		Vector3f rotSpeed = oldRotation.subtract(newRotation);
		oldRotation = newRotation;
		return(rotSpeed);
	}
*/ /*
	public void setLocalRotation(Vector3f rotation){//in setData
		//System.out.println("rotation=" + rotation);
		sendRotationSync(rotation);
	}
*/
/*
	public void rotate(Vector3f spin){
		//System.out.println("NEC.rotate(" + spatial + ")" + ", spin=" + this.spin);
		doRotate(spin);
		//q = spatial.getLocalRotation();
		//float[] angles = new float[3];
		//q.toAngles(angles);
		//doSetRotation(angles[0], angles[1], angles[2]);
		sendRotationSync();
	}
*/
	
	public void rotate(float tpf){
		this.tpf = tpf;
		//if (spatial.getName().equals("Ship 01")) System.out.println("NEC.rotate()1 (" + spatial.getName() + "), (" + "rad[0]=" + rad[0]  + ", rad[1]=" + rad[1]  + ", rad[2]=" + rad[2] + ")" + ", spin=" + spin + ", tpf=" + tpf);
		sendRotateSync(spin);
	}

	//spin
	public void setSpin(Vector3f spin){
		sendSpinSync(spin);
	}

	
	public void setSpin(int i, float delta){
		spin.set(i, delta);
		sendSpinSync(spin);
	}

	
	public void addSpin(Vector3f delta){
		spin = spin.add(delta);
		sendSpinSync(spin);
	}

	
	public Vector3f getSpin(){
		return(spin);
	}

	public Vector3f getWorldSpin(){
		float[] rad = new float[3];
		spatial.getWorldRotation().toAngles(rad);
		Vector3f newWorldRotation = new Vector3f(rad[0], rad[1], rad[2]);
		worldSpin.set(newWorldRotation.subtract(oldWorldRotation));
		oldWorldRotation = newWorldRotation;
		
		//System.out.println("NEC.getWorldSpin() (" + spatial.getName() + "), (" + "rad[0]=" + rad[0]  + ", rad[1]=" + rad[1]  + ", rad[2]=" + rad[2] + ")" + ", worldSpin=" + worldSpin + ", tpf=" + tpf);
		return(worldSpin);
	}

	//performAction
	public void performAction(String action, boolean isPressed){
		sendPerformActionSync(action, isPressed);
	}
	
	public void sendPerformActionSync(String action, boolean isPressed){
        if (client == null)
			doPerformAction(action, isPressed);
		else
            client.send(new NetworkActionMessage(id, action, isPressed));
	}

	public void doPerformAction(String action, boolean isPressed){
		ControllableControl control = spatial.getControl(ControllableControl.class);
		if (action.equals("StopSpin")) control.stopSpin(action, isPressed);
	}
	
	//public void networkPerformAction(String action, boolean isPressed){
	//}

	
	@Override
	protected void controlUpdate(float tpf){
		//System.out.println("NetworkEnabledControl.controlUpdate():");
		this.tpf = tpf;
	}
	
	
	@Override
    public void update(float tpf) {
		//System.out.println("NetworkEnabledControl.update():" + ", enabled=" + enabled);
        if (!enabled) return;
        controlUpdate(tpf);
    }


	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		//Only needed for rendering-related operations,
		//not called when spatial is culled.
	}
	
	
}

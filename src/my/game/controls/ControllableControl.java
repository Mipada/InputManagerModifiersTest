/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.game.controls;

import com.jme3.audio.AudioNode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.NetworkClient;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Line;
//import com.jme3.scene.control.Control;
//import app.OrbitMaker;
//import my.game.states.SolarAppState;
import com.jme3.app.SimpleApplication;
import my.game.states.EnvironmentAppState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3d;

/**
 *
 * @author Peter
 */
public class ControllableControl extends MassControl {
	//Any local variables should be encapsulated by getters/setters so they
	//ar in the SDK properties window and can be edited.
	//Right-click a local variable to encapsulate it with getters and setters.

	static final int X = 0;//pitch
	static final int Y = 1;//yaw
	static final int Z = 2;//roll
	
	static final float PI = (float)Math.PI;
	static final float PI2 = (float)Math.PI * 2f;

	//JME stuff
	//protected SolarAppState solarAppState;
	protected Object model;
	protected boolean driveForward = false;
	protected boolean driveBackward = false;
	protected boolean thrusterForward = false;
	protected boolean thrusterBackward = false;
	protected boolean thrusterUp = false;
	protected boolean thrusterDown = false;
	protected boolean thrusterRight = false;
	protected boolean thrusterLeft = false;
	//rotate
	protected boolean pitchUp = false;
	protected boolean pitchDown = false;
	protected boolean yawRight = false;
	protected boolean yawLeft = false;
	protected boolean rollRight = false;
	protected boolean rollLeft = false;
	protected boolean stopSpin = false;
	protected boolean stop = false;

	//drive stuff
	protected int driveIndex = 0;//thrust level//s, m, l, xl, xxl
	protected float[] driveThrust = {1f, 1f, 340f, 40233f, 5995849f, 149896269f, 299792458f, 599584916f, 899377374f, 1199169835f, 1498962290f, 1798754748f};
	protected float[] driveBurnRate = {0.001f, 0.002f, 0.005f, 0.0010f};
	protected int translationThrustIndex = 0;//thrust level
	protected float[] translationThrust = {0.1f, 0.25f, 0.50f, 1.0f};
	protected int rotationThrustIndex = 0;//thrust level
	protected float[] rotationThrust = {0.0001f, 0.00020f, 0.00050f, 0.0010f};
	protected float[] thrusterBurnRate = {0.001f, 0.002f, 0.005f, 0.0010f};

	//extras
	protected boolean moving = false;
	protected boolean setControl = false;

	protected AudioNode[] sound = new AudioNode[16];//gotta check numbers below
	//activity stuff
	
	public ControllableControl(NetworkClient client, long id, String[] data, EnvironmentAppState environmentAppState, SimpleApplication app){
		super(client, id, data);
		this.environmentAppState = environmentAppState;
		this.app = app;
	}
	

	//@Override
	public void initialize(){
		//Quaternion q = new Quaternion();
		//init sound array
	}//initialize
	
	
	@Override
	public void setSpatial(Spatial spatial){
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been added to a Spatial");
        }
		if (spatial == null){
            throw new IllegalStateException("Cannot add null Spatial");
		}
		this.spatial = spatial;
		//setup
		for (int i = 0;i < 16;i++){
			sound[i] = environmentAppState.getEngineAudioNode(translationThrustIndex);	
		}
		setData(data);
		Quaternion q = spatial.getWorldRotation();
		endMatrix = q.toRotationMatrix();
	}

	
	//actions
	boolean echo = true;
	public void driveForward(String action, boolean isPressed){
		if (echo) System.out.println("CC.driveForward()" + ", action=" + action + ", isPressed=" + isPressed);
		if (driveForward == isPressed) return;
		driveForward = isPressed;
		if (driveForward){
			sound[0] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[0].play();
		}
		else
			sound[0].stop();
	}


	public void driveBackward(String action, boolean isPressed){
		if (echo) System.out.println("CC.driveBackward()" + ", action=" + action + ", isPressed=" + isPressed);
		driveBackward = isPressed;
		if (driveBackward){
			sound[1] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[1].play();
		}
		else{
			sound[1].stop();
		}
	}
	

	public void thrusterForward(String action, boolean isPressed){
		if (echo) System.out.println("CC.thrusterForward()" + ", action=" + action + ", isPressed=" + isPressed);
		if (thrusterForward == isPressed) return;
			thrusterForward = isPressed;
		if (thrusterForward){
			sound[2] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[2].play();
		}
		else{
			sound[2].stop();
		}
	}
	

	public void thrusterBackward(String action, boolean isPressed){
		if (echo) System.out.println("CC.thrusterBackward()" + ", action=" + action + ", isPressed=" + isPressed);
		if (thrusterBackward == isPressed) return;
		thrusterBackward = isPressed;
		if (thrusterBackward){
			//turn on back thruster
			sound[3] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[3].play();
		}
		else{
			sound[3].stop();
		}
	}
	

	public void thrusterUp(String action, boolean isPressed){
		if (echo) System.out.println("CC.thrusterUP()" + ", action=" + action + ", isPressed=" + isPressed);
		if (thrusterUp == isPressed) return;
		thrusterUp = isPressed;
		if (thrusterUp){
			//turn on bow/stern up thrusters
			sound[4] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[4].play();
		}
		else{
			sound[4].stop();
		}
	}


	public void thrusterDown(String action, boolean isPressed){
		if (echo) System.out.println("CC.thrusterDown(): action=" + action + ", isPressed=" + isPressed);
		if (thrusterDown == isPressed) return;
		thrusterDown = isPressed;
		if (thrusterDown){
			sound[5] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[5].play();
		}
		else{
			sound[5].stop();
		}
	}
	

	public void thrusterLeft(String action, boolean isPressed){
		System.out.println("CC.thrusterLeft()" + ", action=" + action + ", isPressed=" + isPressed);
		if (thrusterLeft == isPressed) return;
		thrusterLeft = isPressed;
		if (thrusterLeft){
			sound[6] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[6].play();
		}
		else{
			sound[6].stop();
		}
	}
	

	public void thrusterRight(String action, boolean isPressed){
		System.out.println("CC.thrusterRight()" + ", action=" + action + ", isPressed=" + isPressed);
		if (thrusterRight == isPressed) return;
		thrusterRight = isPressed;
		if (thrusterRight){
			sound[7] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[7].play();
		}
		else{
			sound[7].stop();
		}
	}
	

	//rotation
	public void pitchUp(String action, boolean isPressed){
		System.out.println("CC.pitchUp(): action=" + action + ", isPressed=" + isPressed);
		if (pitchUp == isPressed) return;
		pitchUp = isPressed;
		if (pitchUp){
			sound[ 8] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[ 8].play();
		}
		else{
			sound[ 8].stop();
		}
	}
	

	public void pitchDown(String action, boolean isPressed){
		System.out.println("CC.pitchDown(): action=" + action + ", isPressed=" + isPressed);
		if (pitchDown == isPressed) return;
		pitchDown = isPressed;
		if (pitchDown){
			sound[ 9] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[ 9].play();
		}
		else{
			sound[ 9].stop();
		}
	}
	

	public void yawLeft(String action, boolean isPressed){
		if (echo) System.out.println("CC.yawLeft()" + ", action=" + action + ", isPressed=" + isPressed);
		if (yawLeft == isPressed) return;
		yawLeft = isPressed;
		if (yawLeft){
			sound[10] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[10].play();
		}
		else{
			sound[10].stop();
		}
	}
	

	public void yawRight(String action, boolean isPressed){
		if (echo) System.out.println("CC.yawRight()" + ", action=" + action + ", isPressed=" + isPressed);
		if (yawRight == isPressed) return;
		yawRight = isPressed;
		if (yawRight){
			sound[11] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[11].play();
		}
		else{
			sound[11].stop();
		}
	}
	

	public void rollRight(String action, boolean isPressed){
		if (echo) System.out.println("CC.rollRight()" + ", action=" + action + ", isPressed=" + isPressed);
		if (rollRight == isPressed) return;
		rollRight = isPressed;
		if (rollRight){
			sound[12] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[12].play();
		}
		else{
			sound[12].stop();
		}
	}
	

	public void rollLeft(String action, boolean isPressed){
		if (echo) System.out.println("CC.rollLeft()" + ", action=" + action + ", isPressed=" + isPressed);
		if (rollLeft == isPressed) return;
		rollLeft = isPressed;
		if (rollLeft){
			sound[13] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[13].play();
		}
		else{
			sound[13].stop();
		}
	}
	

	public void stopSpin(String action, boolean isPressed){
		if (echo) System.out.println("CC.stopSpin()" + ", action=" + action + ", isPressed=" + isPressed);
		if (stopSpin == isPressed) return;
		stopSpin = isPressed;
		if (stopSpin){
			//setSpin(new Vector3f(0f, 0f, 0f));//stops in stopSpin at 628
			sound[14] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[14].play();
		}
		else{
			sound[14].stop();
		}
	}
	

	//setVelocity
	public void stop(String action, boolean isPressed){
		if (echo) System.out.println("CC.stop()" + ", action=" + action + ", isPressed=" + isPressed);
		if (echo) System.out.println("CC.stop()" + ", vel=" + velocity);
		if (stop == isPressed) return;
		stop = isPressed;
		if (stop){
			setVelocity(new Vector3f());
			sound[15] = environmentAppState.getEngineAudioNode(driveIndex);
			sound[15].play();
		}
		else{
			sound[15].stop();
		}
	}

	
	Matrix3f startMatrix = new Matrix3f();
	Quaternion startQ = new Quaternion();
	Quaternion endQ = new Quaternion();
	public void doAttach(){
		Vector3f targetLocation = new Vector3f();

		String name = spatial.getUserData("targetName");
		if (name == null) return;
		Node node = (Node)rootNode.getChild(name);
		Node child = (Node)app.getRootNode().getChild(spatial.getName());
		targetLocation = spatial.getUserData("targetLocation");
		System.out.println("CC.doAttach(): parent=" + child + " to " + node + ", at " + targetLocation);
		//lLoc = targetLocation;
		spatial.setLocalTranslation(targetLocation);
		if (name.equals("rootNode")){
			app.getRootNode().attachChild(child);
		}
		else{
			node.attachChild(child);
		}
		spatial.setUserData("attach", false);
	}//doAttach

	
	private void doAttachCamera(){//setup from set controlled
		//if (spatial.getUserData("controlled")) System.out.println("ControllableControl.doAttachCamera(): (" + spatial.getName() + ")");
		String name = spatial.getUserData("targetName");
		Node node = (Node)rootNode.getChild(name);
		//System.out.println("ControllableControl.doAttachCamera(): name=" + name);
		//((Node)rootNode.getChild("Camera 01")).setLocalRotation(q.fromAngles((float)Math.toRadians(90d), 0f, 0f));
		Quaternion q = new Quaternion();
		if (node != null){
			Node cameraNode = (Node)rootNode.getChild("CameraNode");
			if (spatial.getName().substring(0, 4).equals("Ship")){
				System.out.println("ControllableControl.doAttachCamera()1 attach to " + name);
				//cameraNode.setLocalTranslation(0f, 5f, -15f);
				//cameraNode.setLocalRotation(q.fromAngles(0f, 0f, 0f));
				//cameraNode.setLocalTranslation(0f, 25f, 0f);
				//cameraNode.setLocalRotation(q.fromAngles((float)Math.toRadians(90f), 0f, 0f));
			}
			else{//cameras
				System.out.println("ControllableControl.doAttachCamera()2 attach to " + name);
				cameraNode.setLocalTranslation(0f, 0f, 0f);
				cameraNode.setLocalRotation(q.fromAngles((float)Math.toRadians(0f), 0f, 0f));//camera has proper rotation
				//cameraNode.setLocalRotation(q.fromAngles((float)Math.toRadians(90d), 0f, 0f));
			}
			node.attachChild(cameraNode);
			spatial.setUserData("attachCamera", false);
		}
		else{
			System.out.println("ControllableControl.doAttachCamera(): Error: node is null");
		}
	}

 
	@Override
	protected void controlUpdate(float tpf) {
		//TODO: add code that controls Spatial,
		//e.g. spatial.rotate(tpf,tpf,tpf);
		boolean input = false;
		Vector3f inputVector = new Vector3f();
		Vector3f transThrust = new Vector3f();
		Vector3f spinThrust = new Vector3f();
		
		super.controlUpdate(tpf);
		//System.out.println("CC.controlUpdate(" + spatial.getName() + ")" + ", moveMode=" + moveMode);
		//move
		if (driveForward){
			//System.out.println("CC.processInput (" + spatial.getName() + ")");
			inputVector = spatial.getLocalRotation().getRotationColumn(Z).mult(driveThrust[driveIndex]);
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (driveBackward){
			inputVector = spatial.getLocalRotation().getRotationColumn(Z).mult(driveThrust[driveIndex]).negate();
			transThrust.add(new Vector3f(inputVector));
			input = true;
		}
		if (thrusterForward){
			inputVector = spatial.getLocalRotation().getRotationColumn(Z).mult(translationThrust[translationThrustIndex]);
			//println("CC.??()" + ", Z=" + inputVector);
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (thrusterBackward){
			inputVector = spatial.getLocalRotation().getRotationColumn(Z).mult(translationThrust[translationThrustIndex]).negate();
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (thrusterUp){
			inputVector = spatial.getLocalRotation().getRotationColumn(Y).mult(translationThrust[translationThrustIndex]);//get vector
			//println("CC.??()" + ", Y=" + inputVector);
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (thrusterDown){
			inputVector = spatial.getLocalRotation().getRotationColumn(Y).mult(translationThrust[translationThrustIndex]).negate();
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (thrusterLeft){
			inputVector = spatial.getLocalRotation().getRotationColumn(X).mult(translationThrust[translationThrustIndex]);
			//println("CC.??()" + ", X=" + inputVector);
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		if (thrusterRight){
			inputVector = spatial.getLocalRotation().getRotationColumn(X).mult(translationThrust[translationThrustIndex]).negate();
			transThrust = transThrust.add(inputVector);
			input = true;
		}
		//rotate
		if (pitchUp){
			inputVector = new Vector3f(rotationThrust[rotationThrustIndex], 0f, 0f);//why -thrust
			spinThrust = spinThrust.add(inputVector);
			//println("CC.pitchUp()" + ", =" + inputVector);
			//println();
			input = true;
		}
		if (pitchDown){
			inputVector = new Vector3f(-rotationThrust[rotationThrustIndex], 0f, 0f);
			spinThrust = spinThrust.add(inputVector);
			//println("CC.pitchDown()" + ", =" + inputVector);
			//println();
			input = true;
		}
		if (yawLeft){
			inputVector = new Vector3f(0f, rotationThrust[rotationThrustIndex], 0f);
			spinThrust = spinThrust.add(inputVector);
			input = true;
		}
		if (yawRight){
			inputVector = new Vector3f(0f, -rotationThrust[rotationThrustIndex], 0f);
			spinThrust = spinThrust.add(inputVector);
			input = true;
		}
		if (rollRight){
			inputVector = new Vector3f(0f, 0f, rotationThrust[rotationThrustIndex]);
			spinThrust = spinThrust.add(inputVector);
			//println("CC.rollRight()" + ", =" + inputVector);
			//println();
			input = true;
		}
		if (rollLeft){
			inputVector = new Vector3f(0f, 0f, -rotationThrust[rotationThrustIndex]);
			spinThrust = spinThrust.add(inputVector);
			//println("CC.rollLeft()" + ", =" + inputVector);
			//println();
			input = true;
		}
		if (stopSpin){
			spin.set(new Vector3f());//stop here or in stopSpin()
			input = true;
		}
		//if input
		if (input){
			if (transThrust.x != 0 || transThrust.y != 0 || transThrust.z != 0){
				addVelocity(transThrust);
				//System.out.println("CC.Update()" + ", velo=" + velocity);
			}
			if (spinThrust.x != 0 || spinThrust.y != 0 || spinThrust.z != 0){
				addSpin(spinThrust);
				//System.out.println("CC.Update(" + spatial.getName() + ")" + ", spin=" + spin);
			}
			float[] rad = new float[3];
			spatial.getLocalRotation().toAngles(rad);
		}

		if (!input){//slow to stop
			moving = false;
			if ((velocity.x != 0 || velocity.y != 0 || velocity.z != 0)){
				float transLimit = translationThrust[translationThrustIndex] * 7f;// + more sens, - less sens
				if (velocity.x != 0){
					if ((-transLimit < velocity.x) && (velocity.x < transLimit)){
						velocity.x = 0f;
						System.out.println("CC.processInput(): stop velocity.x");
					}
				}
				if (velocity.y != 0){
					if ((-transLimit < velocity.y) && (velocity.y < transLimit)){
						velocity.y = 0f;
						System.out.println("CC.processInput(): stop velocity.y");
					}
				}
				if (velocity.z != 0){
					if ((-transLimit < velocity.z) && (velocity.z < transLimit)){
						velocity.z = 0f;
						System.out.println("CC.processInput(): stop velocity.z");
					}
				}

				moving = true;
			}


			//ang vel
			if ((spin.x != 0 || spin.y != 0 || spin.z != 0)){
				float spinLimit = rotationThrust[rotationThrustIndex] * 6.5f;
				if (spin.x != 0){
					if (-spinLimit < spin.x && spin.x < spinLimit){//rads
						spin.x = 0f;
						if (echo) System.out.println("CC.processInput(): stop roll.x");
					}
				}
				if (spin.y != 0){
					if (-spinLimit < spin.y && spin.y < spinLimit){
						spin.y = 0f;
						if (echo) System.out.println("CC.processInput(): stop roll.y");
					}
				}
				if (spin.z != 0){
					if (-spinLimit < spin.z && spin.z < spinLimit){
						spin.z = 0f;
						if (echo) System.out.println("CC.processInput(): stop roll.z");
					}
				}

				moving = true;
			}//moving
		}//not control.ed, auto slow/stop
		//velocity check
		float[] rad = new float[3];
		spatial.getWorldRotation().toAngles(rad);//local
	}//controlUpdate
	


	@Override
	public void controlRender(RenderManager rm, ViewPort vp) {
		//Only needed for rendering-related operations,
		//not called when spatial is culled.
	}
	
	
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		long id = -1;
		ControllableControl control = new ControllableControl(client, id, data, environmentAppState, app);//moveMode, OrbitMaker
		//TODO: copy parameters to new Control
		return control;
	}
	
	
	public void println(){
		System.out.println();
	}
	
	public void println(String s){
		System.out.println(s);
	}
	
	
	@Override
	public void read(JmeImporter im) throws IOException {
		super.read(im);
		InputCapsule in = im.getCapsule(this);
		//TODO: load properties of this Control, e.g.
		//this.value = in.readFloat("name", defaultValue);
	}
	
	@Override
	public void write(JmeExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule out = ex.getCapsule(this);
		//TODO: save properties of this Control, e.g.
		//out.write(this.value, "name", defaultValue);
	}
}

/*
	private boolean isMass(Object obj){
		boolean mass = false;
		if (obj instanceof Node){
			MassControl massControl = ((Node)obj).getControl(MassControl.class);
			if (massControl != null){
				mass = true;
			}
		}
		return(mass);
	}

	
	

*/

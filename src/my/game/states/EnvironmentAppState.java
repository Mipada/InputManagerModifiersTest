/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.game.states;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.InputManager;
import com.jme3.app.state.AppStateManager;
 
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.NetworkClient;
import com.jme3.network.Server;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import my.game.controls.ControllableControl;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.FastMath;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import com.jme3.system.AppSettings;
import java.awt.event.AdjustmentEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import javax.swing.text.NumberFormatter;
import my.game.app.Main;
import my.game.controls.MassControl;
import my.game.controls.NetworkEnableControl;

/**
 *
 * @author Peter
 */
public class EnvironmentAppState extends AbstractAppState {
	public static final float PI = (float)Math.PI;
	public static final float PI2 = (float)Math.PI * 2;
	
	private SimpleApplication app;
	private Node controlled;
	private Geometry sourcePort;
	private Geometry targetPort;
	private Geometry torus;
	private Node newControlled;
	private boolean setControlled = false;
	private boolean moveCamera = false;
	private boolean moveControl = false;
	static private final String MAPPING_ROTATE = "Rotate";
	static private final Trigger TRIGGER_ROTATE = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
	//JME
	private AssetManager assetManager;
	private AppStateManager appStateManager;
	private InputManager inputManager;
	private InputManager IM;
    private Server server;
    private NetworkClient client;
	private Node rootNode;
	private CameraNode cameraNode;
	//scene
	private PointLight pointLight;
	private Node testBox;
	private Node starField;
	private Node ship1, ship2, camera;
	private boolean running = false;
	private Camera cam;
	DecimalFormat degFormat = new DecimalFormat("000.00");
	NumberFormatter degFormatter = new NumberFormatter(degFormat);
	
	

	
	public EnvironmentAppState(){
	}
	
	public EnvironmentAppState(AppStateManager appStateManager, Node rootNode, SimpleApplication app ){
		System.out.println("EAS.EAS()");
		this.app = app;
		this.appStateManager = appStateManager;
		this.assetManager = app.getAssetManager();
		this.inputManager = app.getInputManager();
		this.IM = inputManager;
		this.rootNode = app.getRootNode();
		cam = app.getCamera();
	}
	
	
	public Node getControlled(){
		return(controlled);
	}
	

	public void setControlled(Node node){
		System.out.println("EAS.setControlled" + " " + node.getName());
		newControlled = node;
		setControlled = true;
	}//setControlled

	
	public void moveCamera(Node node){//move camera
		//System.out.println("EAS.moveCamera(" + node.getName() + ")");
		if (node != null){
			newControlled = node;
			moveCamera = true;
		}
	}//moveCamera
	
	
	public void moveControl(Node node){//don't move camera
		System.out.println("EAS.moveControl");
		if (node != null){
			newControlled = node;
			moveControl = true;
		}
	}//moveControl
	
	
	public void unsetControlled(Node node){
		node.setUserData("controlled", false);
		//Iterator it = node
		//List list = node.getChildren();//only gets 1st generation children
		Iterator it = node.getChildren().iterator();
		while (it.hasNext()){
			Object obj = it.next();
			if (obj instanceof Node){
				//System.out.println("EAS.setUncontrolled(): list(" + i + ")=" + ((Node)obj).getName());
				Node child = (Node)obj;
				unsetControlled(child);
			}
		}
	}
	
	
	public void adjustmentValueChanged(AdjustmentEvent e){
	}

	
	
	private void initKeys(){
		inputManager.setEcho(true);
		//remove mappings
		//inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
		//inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_CAMERA_POS);
		//inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_MEMORY);
		//add system mappings
		inputManager.addMapping("Test", new KeyTrigger(KeyInput.KEY_NUMPAD9));//old interface
		inputManager.addMapping("LeftAlt", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_LMENU));
		inputManager.addMapping("RightAlt", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_RMENU));
		inputManager.addMapping("LeftControl", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_LCONTROL));
		inputManager.addMapping("RightControl", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_RCONTROL));
		inputManager.addMapping("LeftMeta", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_LMETA));
		inputManager.addMapping("RightMeta", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_RMETA));
		inputManager.addMapping("LeftShift", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addMapping("RightShift", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_RSHIFT));
		//add mappings
		inputManager.addMapping("ThrusterUp", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD8));
		inputManager.addMapping("PitchUp", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD2));//NUMPAD2-rotate
		inputManager.addMapping("ThrusterDown", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD2));//NUMPAD2-translate
		inputManager.addMapping("PitchDown", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD8));
		inputManager.addMapping("ThrusterLeft", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD4));
		inputManager.addMapping("YawLeft", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD4));
		inputManager.addMapping("ThrusterRight", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD6));
		inputManager.addMapping("YawRight", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD6));
		inputManager.addMapping("ThrusterForward", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD7));
		inputManager.addMapping("RollLeft", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD1));
		inputManager.addMapping("ThrusterBackward", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_NUMPAD1));
		inputManager.addMapping("RollRight", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_NUMPAD3));

		inputManager.addMapping("Stop", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_NUMPAD0));
		inputManager.addMapping("StopSpin", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_NUMPAD5));

		inputManager.addMapping("DriveForward", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("DriveBackward", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("YawRight", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("YawLeft", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_RIGHT));

		inputManager.addMapping("ThrusterForward2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("ThrusterBackward2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("ThrusterBackward2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_X));
		inputManager.addMapping("ThrusterUp2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("ThrusterDown2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("ThrusterRight2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("ThrusterLeft2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_D));
 
		inputManager.addMapping("Pause", IM.MASK_ANY, new KeyTrigger(KeyInput.KEY_P));
		inputManager.addMapping("Rotation", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_F));

		inputManager.addMapping("Port 1", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_1));
		inputManager.addMapping("Port 2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_2));
		inputManager.addMapping("Port 3", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_3));
		inputManager.addMapping("Port 4", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_4));
		inputManager.addMapping("Port 5", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_5));

		inputManager.addMapping("SetShip1", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_F1));
		inputManager.addMapping("SetShip2", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addMapping("SetShip3", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_F3));
		inputManager.addMapping("SetShip4", IM.MASK_NONE, new KeyTrigger(KeyInput.KEY_F4));
		inputManager.addMapping("ControlShip1", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_F1));
		inputManager.addMapping("ControlShip2", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addMapping("ControlShip3", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_F3));
		inputManager.addMapping("ControlShip4", IM.MASK_CONTROL, new KeyTrigger(KeyInput.KEY_F4));
		
		

		//add listener
		inputManager.addListener(actionListener, "Pause", "DriveForward", "DriveBackward", "ThrusterForward", "ThrusterBackward", "ThrusterUp", "ThrusterDown", "ThrusterRight", "ThrusterLeft", "PitchUp", "PitchDown", "YawLeft", "YawRight", "RollRight", "RollLeft", "StopSpin", "Stop", "Display", "Rotation", "VTOLUp", "VTOLDown", "Lights", "Gear", "AutoLevel", "AutoLevel2", "LeftAlt", "RightAlt", "LeftControl", "RightControl", "LeftShift", "RightShift", "LeftMeta", "RightMeta", "Test", "SetShip1", "SetShip2", "SetShip3", "SetShip4", "DockingRing1", "DockingRing2", "DockingRing3", "DockingRing4", "ControlShip1", "ControlShip2", "ControlShip3", "ControlShip4", "Port 1", "Port 2", "Port 3", "Port 4", "Port 5");
	}//initKeys

	
	
	private ActionListener actionListener = new ActionListener(){
		@Override
		public void onAction(String action, boolean isPressed, float tpf){
			System.out.println("EAS.onAction()1 (" + controlled.getName() + "), " + action + ", " + isPressed + ", " + tpf);
			//onAction(action, isPressed, IM.MASK_BOGUS, tpf);
		}
		
		@Override
		public void onAction(String action, boolean isPressed, int modifier, float tpf){
			if (running){//move
				System.out.println("EAS.onAction()2 (" + controlled.getName() + "), " + action + ", " + isPressed + ", modifier=" + inputManager.toBits(modifier) + ", " + tpf);
				//simple action
				if(action.equals("Stop")){//ignore modifier
					controlled.getControl(ControllableControl.class).stop(action, isPressed);
				}
				if(action.equals("StopSpin")){
					controlled.getControl(ControllableControl.class).stopSpin(action, isPressed);
				}
				if(action.equals("Pause") && isPressed){
					running = !running;//toggle
				}
				//branch
				if (modifier == inputManager.MASK_NONE){//no modifiers
					noMods(action, isPressed, modifier);
				}
				else if((modifier & IM.MASK_SHIFT) != 0){
					doShift(action, isPressed, modifier);
				}
				else if((modifier & IM.MASK_CONTROL) != 0){
					doControl(action, isPressed, modifier);
				}
				else if ((modifier & (IM.MASK_SHIFT | IM.MASK_CONTROL)) != 0){
					doShiftControl(action, isPressed, modifier);
				}
				else if ((modifier & (IM.MASK_ALT | IM.MASK_CONTROL)) != 0){
					doShiftControl(action, isPressed, modifier);
				}
				else if ((modifier & (IM.MASK_ALT)) != 0){
					doAlt(action, isPressed, modifier);
				}
				else{
					System.out.println("EAS.onAction(): bad modifier=" + modifier);
				}
			}//no mods
			else{
				//if(action.equals("Pause") && isPressed){
					System.out.println("Press P to unpause");
				//}
			}
			//System.out.println();
		};
	};
	
	//@Override
	private void noMods(String action, boolean isPressed, int modifier){
		//System.out.println("EnvironmentAppState.doNoMods()" + ", action=" + action + ", isPressed=" + isPressed + ", modifiers=" + modifiers + ", rotation=" + rotation);
		//drive

		//if (action.equals("Rotation") && isPressed){
		//	rotation = (rotation == false);
		//}
		//rotations
		if (action.equals("DriveForward")){
			controlled.getControl(ControllableControl.class).driveForward(action, isPressed);
		}
		if (action.equals("DriveBackward")){
			controlled.getControl(ControllableControl.class).driveBackward(action, isPressed);
		}
		//thrusters
		if(action.equals("ThrusterForward")){
			controlled.getControl(ControllableControl.class).thrusterForward(action, isPressed);
		}
		if(action.equals("ThrusterBackward")){
			controlled.getControl(ControllableControl.class).thrusterBackward(action, isPressed);
		}
		if (action.equals("ThrusterUp")){
			controlled.getControl(ControllableControl.class).thrusterUp(action, isPressed);
		}
		if (action.equals("ThrusterDown")){
			controlled.getControl(ControllableControl.class).thrusterDown(action, isPressed);
		}
		if (action.equals("ThrusterRight")){
			controlled.getControl(ControllableControl.class).thrusterRight(action, isPressed);
		}
		if (action.equals("ThrusterLeft")){
			controlled.getControl(ControllableControl.class).thrusterLeft(action, isPressed);
		}
		if (action.equals("SetShip1") && isPressed){
			Node node = (Node)rootNode.getChild("Ship1");
			setControlled(node);
		}
		if(action.equals("SetShip2") && isPressed){
			Node node = (Node)rootNode.getChild("Ship2");
			setControlled(node);
		}
		if(action.equals("SetShip3") && isPressed){
			Node node = (Node)rootNode.getChild("Ship3");
			setControlled(node);
		}
		if(action.equals("SetShip4") && isPressed){
			Node node = (Node)rootNode.getChild("Ship4");
			setControlled(node);
		}
		//set target
		if (action.equals("Port 1") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 1");
		}
		if(action.equals("Port 2") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 2");
		}
		if(action.equals("Port 3") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 3");
		}
		if(action.equals("Port 4") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 4");
		}
		if(action.equals("Port 5") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 5");
		}
		if(action.equals("Port 6") && isPressed){
			targetPort = (Geometry)rootNode.getChild("Port 6");
		}
	}//doNoMods
	
	
	private void doShift(String action, boolean isPressed, int modifiers){
	}//doShift
	
	
	private void doControl(String action, boolean isPressed, int modifier){
		System.out.println("SAS.doControl (" + controlled.getName() + ")" + ", action=" + action + ", isPressed=" + isPressed + ", modifier=" + modifier);
		//rotations
		if (action.equals("PitchDown")){
			controlled.getControl(ControllableControl.class).pitchDown(action, isPressed);
		}
		if (action.equals("PitchUp")){
			controlled.getControl(ControllableControl.class).pitchUp(action, isPressed);
		}
		if (action.equals("YawLeft")){
			controlled.getControl(ControllableControl.class).yawLeft(action, isPressed);
		}
		if (action.equals("YawRight")){
			controlled.getControl(ControllableControl.class).yawRight(action, isPressed);
		}
		if(action.equals("RollRight")){
			controlled.getControl(ControllableControl.class).rollRight(action, isPressed);
		}
		if(action.equals("RollLeft")){
			controlled.getControl(ControllableControl.class).rollLeft(action, isPressed);
		}
		//move control
		if (action.equals("ControlShip1") && isPressed){
			Node node = (Node)rootNode.getChild("Ship1");
			moveControl(node);
		}
		if(action.equals("ControlShip2") && isPressed){
			Node node = (Node)rootNode.getChild("Ship2");
			moveControl(node);
		}
		if(action.equals("ControlShip3") && isPressed){
			Node node = (Node)rootNode.getChild("Ship3");
			moveControl(node);
		}
		if(action.equals("ControlShip4") && isPressed){
			Node node = (Node)rootNode.getChild("Ship4");
			moveControl(node);
		}
		
	}//doControl
	

	private void doShiftControl(String action, boolean isPressed, int modifiers){
	}
	
	
	private void doAlt(String action, boolean isPressed, int modifier){
		System.out.println("SAS.doAlt()");
		
	}
	
	public void initMouse(){
		inputManager.addMapping(MAPPING_ROTATE, TRIGGER_ROTATE);
		inputManager.addListener(analogListener, new String[] {MAPPING_ROTATE});
		inputManager.changeModifier(MAPPING_ROTATE, inputManager.MASK_NONE, TRIGGER_ROTATE);
		
	}


	private AnalogListener analogListener = new AnalogListener(){
		@Override
		public void onAnalog(String action, float value, float tpf){
			onAnalog(action, value, 0, tpf);
		}

		@Override
		public void onAnalog(String action, float value, int modifier, float tpf){
			float intensity = value;
			if (running){
				if (action.equals(MAPPING_ROTATE)){
					//System.out.println("onAnalog-mapping_rotate");
					CollisionResults results = new CollisionResults();
					Vector2f click2d = inputManager.getCursorPosition();
					Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 0f);
					Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.getX(), click2d.getY()), 1f).subtractLocal(click3d);
					
					Ray ray = new Ray(click3d, dir);
					rootNode.collideWith(ray, results);
					if (results.size() > 0){
						Geometry target = results.getClosestCollision().getGeometry();
						System.out.println("target's parent=" + target.getParent().getName());
					}
					else{
						//nothing selected
					}
				}//end mapping rotate
			} else {
				System.out.println("Press P to unpause (analogListener)");
			}
		}
	};

	
	public void onAction(String action, boolean isPressed, float tpf){
		System.out.println("EAS.onAction(): NOT IM.MASK_LIMENTED");
	}
	
	
	//drive
	public AudioNode getEngineAudioNode(int i){
		AudioNode n = new AudioNode();
		//System.out.println("SAS.getEngineAudioNode(): i=" + i);
		
		if (i == 0)
			n = new AudioNode(assetManager, "Sounds/Ship/thruster small.wav");
		else if (i == 1)
			n = new AudioNode(assetManager, "Sounds/Ship/thruster medium.wav");
		else if (i == 2)
			n = new AudioNode(assetManager, "Sounds/Ship/thruster large.wav");
		//else if (i == 3)
		//	n = new AudioNode(assetManager, "Sounds/Ship/thruster XLarge.wav");
		//else if (i == 4)
		//	n = new AudioNode(assetManager, "Sounds/Ship/thruster XXL.wav");
		else{
			System.out.println("EAS.getEngineAudioNode - Error: bad index (" + i + ")");
			n = new AudioNode(assetManager, "Sounds/Ship/thruster large.wav");
		}
		return(n);
	}

	
	public void addStarField(int count){
		Sphere	sphere;
		//Geometry geometry;
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

		Node node;
		ColorRGBA color;
		String colorString;
		String[] colorData;

		//
		starField = new Node("starField");
		starField.setLocalTranslation(1, 1, 1);
		for (int i = 0;i < count;i++){
			
			color = new ColorRGBA(ColorRGBA.randomColor());

			int colorMode = 0;
			if (colorMode == 0){
				mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", color);
			}
			else if (colorMode == 1){
				mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
				mat.setBoolean("UseMaterialColors", true);
				mat.setColor("Diffuse", color);
				mat.setColor("Ambient", color);
				mat.setColor("Specular", new ColorRGBA(ColorRGBA.White));
				mat.setFloat("Shininess", 64f);
			}
			else{
				System.out.println("EnvironmentAppState.addStarField(): bad colorMode (" + colorMode + ")");
			}
			//geom
			float r = (float)Math.random() * 2;
			sphere = new Sphere(9, 9, r);//int zSamples, int radialSamples, int radius, boolean useEvenSlices, boolean interior
			Geometry geometry = new Geometry("Star" + i, sphere);
			float r2 = 5000f;
			Vector3f v;
			v = new Vector3f((float)Math.random() * r2, (float)Math.random() * r2, (float)Math.random() * r2);
			//v = new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random());
			//v.normalize();
			//v.mult(r2);//Float.MAX_VALUE * 0.9f
			//v.mult(v);//Float.MAX_VALUE * 0.9f
			geometry.setLocalTranslation(v);
			geometry.setMaterial(mat);
			starField.attachChild(geometry);	
			rootNode.attachChild(starField);
		}
		//((Node)rootNode.getChild("Milky Way")).attachChild(starField);//no milky way yet
	}//addStarField
	
	
	public Node addCameraNode(String name, Camera tempCam){
		cameraNode = new CameraNode(name, tempCam);
		
		//tempCam.setFrustumNear(0.01f);
		//tempCam.setFrustumFar(Float.MAX_VALUE * 0.5f);//Float.MAX_VALUE (1000, 5000)
		//tempCam.setFrustumFar(25000f);//Float.MAX_VALUE (1000, 5000)
		
		//System.out.println("EnvironmentAppState.setUpCamera() - FrustumNear=" + tempCam.getFrustumNear());
		//System.out.println("EnvironmentAppState.setUpCamera() - FrustumFar=" + tempCam.getFrustumFar());

		//cameraNode.setLocalTranslation(0f, 0f, 0f);//camTrans camLoc
		//camNode.setLocalRotation(q.fromAngles((float)Math.toRadians(90d), 0f, 0f));//camTrans camLoc
		cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
		long id = 3;
		ControllableControl control = new ControllableControl(client, id, controllableData[0], this, app);//test box
		cameraNode.addControl(control);
		//cameraNode.setControlDir(CameraControl.ControlDirection.CameraToSpatial);
		cameraNode.setEnabled(true);
		rootNode.attachChild(cameraNode);
		return(cameraNode);
	}//myCamNode
	

	private void addLight(){
		//add light
		pointLight = new PointLight();
		pointLight.setColor(ColorRGBA.White);
		pointLight.setRadius(Float.MAX_VALUE);//
		rootNode.addLight(pointLight);
	}
	
	
	public Node addShip1(){
		//add test box
        Geometry geo1, geo2, geo3;
		Cylinder cylinder1, cylinder2;
		Torus shape3;
		Material mat1, mat2, mat3;
		long id = -1;
		//sphere = new Sphere(32, 32, 1f);
		//Box box = new Box(1, 2.5f, 1);
        //Cylinder(int axisSamples, int radialSamples, float radius, float radius2, float height, boolean closed, boolean inverted)
		cylinder1 = new Cylinder(10, 12, 0.0f, 1.0f, 1.0f, true, false);
        geo1 = new Geometry("Commend Module", cylinder1);
		mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.White);
        geo1.setMaterial(mat1);
		geo1.setLocalRotation(new Quaternion().fromAngles(0f, 0f, 0f * FastMath.DEG_TO_RAD));
		
		cylinder2 = new Cylinder(10, 12, 1.0f, 1.0f, 3.0f, true, false);
		mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.White);
        geo2 = new Geometry("cone1", cylinder2);
        geo2.setMaterial(mat2);
		geo2.setLocalTranslation(0f, 0f, -2.25f);
		geo2.setLocalRotation(new Quaternion().fromAngles(0f, 0f, 0f * FastMath.DEG_TO_RAD));
		//port 1
		shape3 = new Torus(10, 12, 0.1f, 0.2f);
        geo3 = new Geometry("Port 1", shape3);
		mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Red);
        geo3.setMaterial(mat3);
		MassControl massCon3 = new MassControl();
		geo3.addControl(massCon3);
		geo3.setLocalTranslation(0f, 0f, 0.5f);//y=0.25
		geo3.setLocalRotation(new Quaternion().fromAngles(0f, 0f, 0f * FastMath.DEG_TO_RAD));
		//control.setSpin(new Vector3f(0.0f, 0.2f, 0.0f));//degrees
		//compile
		ship1 = new Node("Ship1");
		ship1.attachChild(geo1);
		ship1.attachChild(geo2);
		ship1.attachChild(geo3);
		id = -1;
		ControllableControl control = new ControllableControl(client, id, controllableData[0], this, app);//test box
		ship1.addControl(control);
		//ship1.setLocalTranslation(5.5f, 0.0f, 0.0f);
		//ship1.setLocalRotation(new Quaternion().fromAngles(0.0f, -90f * FastMath.DEG_TO_RAD, 0.0f));
		ship1.setLocalTranslation(0.0f, 10.0f, -40.0f);
		ship1.setLocalRotation(new Quaternion().fromAngles(0.0f * FastMath.DEG_TO_RAD, 0f * FastMath.DEG_TO_RAD, 0f));
		
		rootNode.attachChild(ship1);
		return(ship1);
	}//ship 1
	
	
	public Node addShip2(){
		Cylinder cylinder1;
		Torus shape2, shape3;
        Geometry geo1, geo2, geo3;
		Material mat1, mat2, mat3;
		//sphere = new Sphere(32, 32, 1f);
		//Box box = new Box(1, 2.5f, 1);
        //Cylinder(int axisSamples, int radialSamples, float radius, float radius2, float height, boolean closed, boolean inverted)
		cylinder1 = new Cylinder(10, 12, 2.0f, 2.0f, 10.0f, true, false);
        geo1 = new Geometry("cylinder2", cylinder1);
		mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.White);
        geo1.setMaterial(mat1);
		//control.setSpin(new Vector3f(0.0f, 0.2f, 0.0f));//degrees
		
		//port 2
		shape2 = new Torus(10, 12, 0.1f, 0.2f);
        targetPort = geo2 = new Geometry("Port 2", shape2);
		mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        geo2.setMaterial(mat2);
		MassControl massCon2 = new MassControl();
		geo2.addControl(massCon2);
		geo2.setLocalTranslation(0.0f, 0.0f, 5.0f);
		geo2.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, -90f * FastMath.DEG_TO_RAD, -90f * FastMath.DEG_TO_RAD));
		//port 3
		shape3 = new Torus(10, 12, 0.1f, 0.2f);
        geo3 = new Geometry("Port 3", shape3);
		mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Red);
        geo3.setMaterial(mat3);
		MassControl massCon3 = new MassControl();
		geo3.addControl(massCon3);
		geo3.setLocalTranslation(0.0f, 0.0f, -5.0f);
		//geo3.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, -90f * FastMath.DEG_TO_RAD, -90f * FastMath.DEG_TO_RAD));
		geo3.setLocalRotation(new Quaternion().fromAngles(0f * FastMath.DEG_TO_RAD, 0f * FastMath.DEG_TO_RAD, 0f * FastMath.DEG_TO_RAD));
		//control.setSpin(new Vector3f(0.0f, 0.2f, 0.0f));//degrees
		//stack
		ship2 = new Node("Ship2");
		ship2.attachChild(geo1);
		ship2.attachChild(geo2);
		ship2.attachChild(geo3);
		long id = 2;
		ControllableControl control = new ControllableControl(client, id, controllableData[0], this, app);//test box
		ship2.addControl(control);
		ship2.setLocalTranslation(0.0f, 0.0f, 0.0f);
		ship2.setLocalRotation(new Quaternion().fromAngles(0.0f, -90f * FastMath.DEG_TO_RAD, 0.0f));
		
		rootNode.attachChild(ship2);
		return(ship2);
	}//ship2
	
	
	public Node addShip3(){
        //Cylinder(int axisSamples, int radialSamples, float radius, float radius2, float height, boolean closed, boolean inverted)
		//Cylinder shape = new Cylinder(10, 12, 2.0f, 2.0f, 10.0f, true, false);;
		Sphere shape = new Sphere(32, 32, 1f);
        Geometry geo1;
		Material mat1;
		long id = -1;
		//Box box = new Box(1, 2.5f, 1);
        geo1 = new Geometry("shape3", shape);
		mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        geo1.setMaterial(mat1);
		//control.setSpin(new Vector3f(0.0f, 0.2f, 0.0f));//degrees
		
		camera = new Node("Ship3");
		camera.attachChild(geo1);
		id = 2;
		ControllableControl control = new ControllableControl(client, id, controllableData[0], this, app);//test box
		camera.addControl(control);
		camera.setLocalTranslation(0.0f, 10.0f, 40.0f);
		camera.setLocalRotation(new Quaternion().fromAngles(0.0f, 180f * FastMath.DEG_TO_RAD, 0.0f));
		
		rootNode.attachChild(camera);
		return(camera);
	}//ship3
	
	
	public Node addShip4(){
		//add test box
        //Geometry geo;
		//sphere = new Sphere(32, 32, 1f);
        //geom = new Geometry("test sphere", sphere);
		Node boxNode = new Node("boxNode");
		Torus shape2, shape3;
		Box box1;
        Geometry geo1, geo2, geo3;
		box1 = new Box(1, 2.5f, 1);//x, y, z
		geo1 = new Geometry("geoBox", box1);
		Material mat1, mat2, mat3;
		mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        geo1.setMaterial(mat1);
		//port4
		shape2 = new Torus(10, 12, 0.1f, 0.2f);
        geo2 = new Geometry("Port 4", shape2);
		mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        geo2.setMaterial(mat2);
		MassControl massCon2 = new MassControl();
		geo2.addControl(massCon2);
		geo2.setLocalTranslation(0.0f, -2.5f, 0.0f);
		geo2.setLocalRotation(new Quaternion().fromAngles(-90.0f * FastMath.DEG_TO_RAD, 0f, 0f));
		//massCon2.setSpin(new Vector3f(0.0f, 0.0f, 10.0f * FastMath.DEG_TO_RAD));//degrees, attached to box
		//port5
		shape3 = new Torus(10, 12, 0.1f, 0.2f);
        geo3 = new Geometry("Port 5", shape3);
		mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Red);
        geo3.setMaterial(mat3);
		MassControl massCon3 = new MassControl();
		geo3.addControl(massCon3);
		geo3.setLocalTranslation(0.0f, 2.5f, 0.0f);
		geo3.setLocalRotation(new Quaternion().fromAngles(-90f * FastMath.DEG_TO_RAD, 0.0f, 0.0f));
		//control.setSpin(new Vector3f(0.0f, 0.2f, 0.0f));//degrees
		
		boxNode.attachChild(geo1);
		boxNode.attachChild(geo2);
		//boxNode.setLocalRotation(new Quaternion().fromAngles(-0.1f, 0.0f, -0.1f));
		MassControl massCon = new MassControl();
		massCon.setSpin(new Vector3f(0.0f, 5f, 0.0f).mult(FastMath.DEG_TO_RAD));//rads
		boxNode.addControl(massCon);

		testBox = new Node("Ship4");
		testBox.attachChild(boxNode);
		testBox.attachChild(geo3);
		
		ControllableControl control;
		long id = -1;
		control = new ControllableControl(client, id, controllableData[0], this, app);//test box
		testBox.addControl(control);
		testBox.setLocalTranslation(0.0f, 200.0f, 0f);
		testBox.setLocalRotation(new Quaternion().fromAngles(0.0f, 10.0f * FastMath.DEG_TO_RAD, 0.0f));// * FastMath.DEG_TO_RAD
		rootNode.attachChild(testBox);
		return(testBox);
	}//test box
	
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		//TODO: initialize your AppState, e.g. attach spatials to rootNode
		//this is called on the OpenGL thread after the AppState has been attached
		System.out.println("SAS.initialize()");
		initScene();
		System.out.println("SAS.initialize() end");
	}//initialize

	
	private BitmapFont guiFont;
	private BitmapText statusText;
	private BitmapText locationText;
	private BitmapText rotationText;
	private BitmapText sourceLocationText;
	private BitmapText sourceRotationText;
	private BitmapText targetLocationText;
	private BitmapText targetRotationText;
	private BitmapText diffTransText;
	private BitmapText diffRotText;
	private BitmapText diffRotVelText;
	public void initScene(){
		System.out.println("EnvironmentAppState.initScene()");
		AppSettings appSettings = ((Main)app).getSettings();
		Node myGUINode = app.getGuiNode();
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		int style = 1;
		//guiFont.setStyle(style);

		statusText = new BitmapText(guiFont);
		statusText.setColor(ColorRGBA.Gray);
		locationText = new BitmapText(guiFont);
		locationText.setColor(ColorRGBA.Gray);
		rotationText = new BitmapText(guiFont);
		rotationText.setColor(ColorRGBA.Gray);
		sourceLocationText = new BitmapText(guiFont);
		sourceLocationText.setColor(ColorRGBA.Gray);
		sourceRotationText = new BitmapText(guiFont);
		sourceRotationText.setColor(ColorRGBA.Gray);
		targetLocationText = new BitmapText(guiFont);
		targetLocationText.setColor(ColorRGBA.Gray);
		targetRotationText = new BitmapText(guiFont);
		targetRotationText.setColor(ColorRGBA.Gray);
		diffTransText = new BitmapText(guiFont);
		diffTransText.setColor(ColorRGBA.Gray);
		diffRotText = new BitmapText(guiFont);
		diffRotText.setColor(ColorRGBA.Gray);
		diffRotVelText = new BitmapText(guiFont);
		diffRotVelText.setColor(ColorRGBA.Gray);

		statusText.move(0, statusText.getLineHeight() * 6, 0);//screen x, y, z

		locationText.move(0, locationText.getLineHeight() * 5, 0);//screen x, y, z
		sourceLocationText.move(0, sourceLocationText.getLineHeight() * 4, 0);//screen x, y, z
		targetLocationText.move(0, targetLocationText.getLineHeight() * 3, 0);//screen x, y, z
		diffTransText.move(0, diffTransText.getLineHeight() * 2, 0);//screen x, y, z

		rotationText.move(appSettings.getWidth()/2, rotationText.getLineHeight() * 5, 0);
		sourceRotationText.move(appSettings.getWidth()/2, sourceRotationText.getLineHeight() * 4, 0);
		targetRotationText.move(appSettings.getWidth()/2, targetRotationText.getLineHeight() * 3, 0);
		diffRotText.move(appSettings.getWidth()/2, diffRotText.getLineHeight() * 2, 0);
		diffRotVelText.move(appSettings.getWidth()/2, diffRotText.getLineHeight() * 1, 0);

		myGUINode.attachChild(statusText);

		myGUINode.attachChild(locationText);
		myGUINode.attachChild(sourceLocationText);
		myGUINode.attachChild(targetLocationText);
		myGUINode.attachChild(diffTransText);
		//
		myGUINode.attachChild(rotationText);
		myGUINode.attachChild(sourceRotationText);
		myGUINode.attachChild(targetRotationText);
		myGUINode.attachChild(diffRotText);
		myGUINode.attachChild(diffRotVelText);
		//
		addLight();
		addStarField(1000);//starField
		//controlled = addShip1();
		//controlled.attachChild(addCameraNode("Camera", app.getCamera()));
		addCameraNode("Camera", app.getCamera());
		addShip1();
		addShip2();
		addShip3();
		addShip4();
		Torus shape = new Torus(10, 12, 2f, 4f);
		torus = new Geometry("Torus", shape);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        torus.setMaterial(mat);
		MassControl massCon = new MassControl();
		torus.addControl(massCon);
		torus.setLocalTranslation(0.0f, 10f, 0.0f);
		//geo.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, 0.0f, 0.0f));
		//rootNode.attachChild(torus);
		System.out.println("EAS.initScene()" + ", got here1");
		newControlled = (Node)rootNode.getChild("Ship1");
		setControlled = true;
		doSetControlled((Node)rootNode.getChild("Ship1"));
		System.out.println("EAS.initScene()" + ", got here2");
		//sourcePort = (Geometry)rootNode.getChild("Port 1");
        targetPort = (Geometry)rootNode.getChild("Port 2");
		initKeys();
		initMouse();
		running = true;
		System.out.println();
	}//initScene

	
    public boolean isServer() {
        return server != null;
    }

	
    public void setServer(Server server) {
        this.server = server;
    }
	
	
	public String degFormat(double n){
		String s = new String("");
		
		try{
			s = degFormatter.valueToString(n);
		}
		catch(ParseException e){
			s = new String("EAS.degFormat(): string parse error");
		}
		return(s);
	}
	
	
	private void updateDisplay(float tpf){
		//update readout
		Vector3f sourceLoc = null;
		Vector3f targetLoc = null;
		float[] sourceRad = new float[3];
		float[] targetRad = new float[3];
		Vector3f sourceRot = new Vector3f();
		Vector3f targetRot = new Vector3f();

		if (sourcePort != torus){
			//loc
			sourceLoc = sourcePort.getWorldTranslation();
			sourceLocationText.setText(sourcePort.getName() + " " + degFormat(sourceLoc.x) + ", " + degFormat(sourceLoc.y) + ", " + degFormat(sourceLoc.y));
			//rot
			sourcePort.getWorldRotation().toAngles(sourceRad);
			sourceRot.set(sourceRad[0], sourceRad[1], sourceRad[2]);
			sourceRotationText.setText(sourcePort.getName() + " " + degFormat(sourceRot.x * FastMath.RAD_TO_DEG) + ", " + degFormat(sourceRot.y * FastMath.RAD_TO_DEG) + ", " + degFormat(sourceRot.z * FastMath.RAD_TO_DEG));
		}
		else {
			//System.out.println("EAS.update()" + " sourcePort = torus");
			//sourceLocationText.setText(sourcePort.getName() + " " + sourceXText + ", " + sourceYText + ", " + sourceZText);
			sourceLocationText.setText("This ship has no docking port");
			//targetLocationText.setText(targetPort.getName() + " " + targetXText + ", " + targetYText + ", " + targetZText);
			sourceRotationText.setText("");
			//targetRotationText.setText(targetPort.getName() + " " + targetXRotText + ", " + targetYRotText + ", " + targetZRotText);
			diffTransText.setText("");
			diffRotText.setText("");
			diffRotVelText.setText("");
			//return;

		}
		if (targetPort != torus){
			//loc
			targetLoc = targetPort.getWorldTranslation();
			targetLocationText.setText(targetPort.getName() + " " + degFormat(targetLoc.x) + ", " + degFormat(targetLoc.y) + ", " + degFormat(targetLoc.z));
			//rot
			targetPort.getWorldRotation().toAngles(targetRad);
			targetRot.set(targetRad[0], targetRad[1], targetRad[2]);
			//System.out.println("EAS.??()2" + ", targetPort=" + targetPort);
			//targetRot = targetRot.mult(FastMath.RAD_TO_DEG);
			//System.out.println("EAS.??()2" + ", targetRot=" + targetRot.x + ", " + targetRot.y + ", " + targetRot.z);
		
			targetRotationText.setText(targetPort.getName() + " " + degFormat(targetRot.x * FastMath.RAD_TO_DEG) + ", " + degFormat(targetRot.y * FastMath.RAD_TO_DEG) + ", " + degFormat(targetRot.z * FastMath.RAD_TO_DEG));
		}
		else {
			//System.out.println("EAS.update()" + " targetPort = torus");
			//sourceLocationText.setText(sourcePort.getName() + " " + sourceXText + ", " + sourceYText + ", " + sourceZText);
			//sourceLocationText.setText("No docking port available");
			//targetLocationText.setText(targetPort.getName() + " " + targetXText + ", " + targetYText + ", " + targetZText);
			targetLocationText.setText("No docking port available");
			//sourceRotationText.setText("");
			targetRotationText.setText("");
			diffTransText.setText("");
			diffRotText.setText("");
			diffRotVelText.setText("");
			//return;
			
		}
		
		//set display
		//header
		locationText.setText("Translation");
		rotationText.setText("Rotation");
		if ((sourceLoc != null) && (targetLoc != null)){
			//diff
			Vector3f range = targetLoc.subtract(sourceLoc);
			Vector3f targetAngleVel = targetPort.getControl(NetworkEnableControl.class).getWorldSpin();
			Vector3f sourceAngleVel = sourcePort.getControl(NetworkEnableControl.class).getWorldSpin();
			Vector3f angleVelocity = targetAngleVel.subtract(sourceAngleVel);
			angleVelocity = angleVelocity.mult(FastMath.RAD_TO_DEG);
			clipDeg(angleVelocity);

			Vector3f angle = targetRot.subtract(sourceRot);//.mult()
			angle = angle.mult(FastMath.RAD_TO_DEG);
			clipDeg(angle);
			//System.out.println("EAS.update(" + controlled.getName() + ")" + ", targetAngleVel=" + targetAngleVel + ", sourceAngleVel=" + sourceAngleVel);
			diffTransText.setText("Diff = " + " " + degFormat(range.x) + ", " + degFormat(range.y) + ", " + degFormat(range.z));
			diffRotText.setText("Diff = " + " " + degFormat(angle.x) + ", " + degFormat(angle.y) + ", " + degFormat(angle.z));
			diffRotVelText.setText("DRV= " + "(" + degFormat(angleVelocity.x) + ") (" + degFormat(angleVelocity.y) + ") (" + degFormat(angleVelocity.z) + ")");
			checkDocking(range, angleVelocity);
		}
	}
	
	
	@Override
	public void update(float tpf) {
		//TODO: implement behavior during runtime
		//System.out.println("EAS.update()");
		if (running){
			if (setControlled){
				doSetControlled(newControlled);
				setControlled = false;
			}
			if (moveCamera){
				doMoveCamera();
				moveCamera = false;
			}
			if (moveControl){
				controlled = newControlled;
				moveControl = false;
			}
		}
		
		updateDisplay(tpf);
		

	}
	
	
	private boolean isCloseEnough(Vector3f range){
		return((-0.25f < range.x && range.x < 0.25f) && (-0.25f < range.y && range.y < 0.25f) && (-0.25f < range.z && range.z < 0.25f));
	}
	
	
	private boolean isSpinningEnough(Vector3f angleVelocity){
		return((-0.1f < angleVelocity.x && angleVelocity.x < 0.1f) && (-0.1f < angleVelocity.y && angleVelocity.y < 0.1f) && (-0.1f < angleVelocity.z && angleVelocity.z < 0.1f));
	}
	
	private boolean isDocked(Vector3f range, Vector3f angleVelocity){
		if (isCloseEnough(range) && isSpinningEnough(angleVelocity)) return(true);
		else return(false);
	}
	
	
	private boolean checkDocking(Vector3f range, Vector3f angleVelocity){
		boolean close = isCloseEnough(range);
		boolean syncing = isSpinningEnough(angleVelocity);
		if (close && syncing){
			statusText.setText("Docked");
			return(true);
		}
		else if (close && !syncing){
			statusText.setText("Undocked: Adjust rotation velocity before final approach.");
			return(false);
		}
		else{// if (!close && !syncing)
			statusText.setText("Undocked");
			return(false);
		}
		//if (!docked)
			//System.out.println("EAS.isDocked()" + ", range=" + range + "(" + close + ")" + ", av=" + angleVelocity + " (" + syncing + ")");
	}

	
	private void clipRad(Vector3f v){
		if (v.x < -PI) v.x = v.x + PI2;
		else if (v.x > PI) v.x = v.x - PI2;
		if (v.y < -PI) v.y = v.y + PI2;
		else if (v.y > PI) v.y = v.y - PI2;
		if (v.z < -PI) v.z = v.z + PI2;
		else if (v.z > PI) v.z = v.z - PI2;
	}
	
	
	private void clipDeg(Vector3f v){
		if (v.x < -180f) v.x = v.x + 360f;
		else if (v.x > 180f) v.x = v.x - 360f;
		if (v.y < -180f) v.y = v.y + 360f;
		else if (v.y > 180) v.y = v.y - 360f;
		if (v.z < -180f) v.z = v.z + 360f;
		else if (v.z > 180f) v.z = v.z - 360f;
	}
	
	
	private void doSetControlled(Node tempControlled){
		System.out.println("EAS.doSetControlled() (" + newControlled.getName() + ")");
		if (tempControlled != null){
			controlled = tempControlled;
			controlled.attachChild(cameraNode);
			if (controlled.getName().equals("Ship1")){
				sourcePort = (Geometry)rootNode.getChild("Port 1");
				cameraNode.setLocalTranslation(0f, 0f, -0.5f);
				cameraNode.setLocalRotation(new Quaternion().fromAngles(0f, 0.0f, 0.0f));
			}
			else if (controlled.getName().equals("Ship2")){
				if (!sourcePort.getName().equals("Port 2"))
					sourcePort = (Geometry)rootNode.getChild("Port 2");
				else
					sourcePort = (Geometry)rootNode.getChild("Port 3");
				cameraNode.setLocalTranslation(0f, 0f, -0.5f);
				cameraNode.setLocalRotation(new Quaternion().fromAngles(0f, 0.0f, 0.0f));
			}
			else if (controlled.getName().equals("Ship3")){
				System.out.println("EAS.doSetControlled()1" + ", sourcePort=" + sourcePort);
				System.out.println("EAS.doSetControlled()2" + ", sourcePort name=" + sourcePort.getName());
				//sourcePort = torus;
				sourcePort = torus;//new Geometry("bogus", new Torus(10, 12, 0.25f, 0.5f))
				cameraNode.setLocalTranslation(0f, 0f, -0.5f);
				cameraNode.setLocalRotation(new Quaternion().fromAngles(0f, 0.0f, 0.0f));
			}
			else if (controlled.getName().equals("Ship4")){
				System.out.println("EAS.doSetControlled()3" + "controlled=" + controlled.getName() + ", sourcePort=" + sourcePort);
				System.out.println("EAS.doSetControlled()4" + ", controlled name=" + controlled.getName());
				if (!sourcePort.getName().equals("Port 4"))
					sourcePort = (Geometry)rootNode.getChild("Port 4");
				else
					sourcePort = (Geometry)rootNode.getChild("Port 5");
				cameraNode.setLocalTranslation(0f, 0f, 0.0f);
				cameraNode.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, 0.0f, 0.0f));
			}
			else{
				System.out.println("EAS.doSetControlled() bad control name (" + controlled.getName() + ")");
			}
		}
		assert sourcePort != null;
		assert sourcePort.getName() != null;
	}
	

	private void doMoveCamera(){
		newControlled.attachChild(cameraNode);
		if (newControlled.getName().equals("Ship1")){
			cameraNode.setLocalTranslation(0f, 0f, -0.5f);
			cameraNode.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, 0.0f, 0.0f));
		}
		else if (newControlled.getName().equals("Ship4")){
			cameraNode.setLocalTranslation(0f, 0f, -0.5f);
			cameraNode.setLocalRotation(new Quaternion().fromAngles(90f * FastMath.DEG_TO_RAD, 0.0f, 0.0f));
		}
		else{
			cameraNode.setLocalTranslation(0f, 0f, 0.0f);
			cameraNode.setLocalRotation(new Quaternion().fromAngles(0.0f, 0.0f, 0.0f));
		}
	}
	

	@Override
	public void cleanup() {
		super.cleanup();
		//TODO: clean up what you initialized in the initialize method,
		//e.g. remove all spatials from rootNode
		//this is called on the OpenGL thread after the AppState has been detached
	}
	

	String[][] controllableData = {
		{"10", "10"},
		{"10", "10"},
		{"10", "10"}
	};
	
	

}

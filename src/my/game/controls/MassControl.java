/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.game.controls; 

import com.jme3.app.SimpleApplication;
import my.game.states.EnvironmentAppState;
import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.network.NetworkClient;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Peter
 */
public class MassControl extends NetworkEnableControl {
	//Any local variables should be encapsulated by getters/setters so they
	//appear in the SDK properties window and can be edited.
	//Right-click a local variable to encapsulate it with getters and setters.
	protected SimpleApplication app;
	protected AssetManager assetManager;
	protected EnvironmentAppState environmentAppState;
	protected Node rootNode;
	//orbit data
	protected Vector3f orbit;// = new Vector3f();
	protected float degree = -1;
	protected Vector3f vector = new Vector3f();
	protected double zoom = 1d;
	private boolean initialized = false;
	private boolean thrusting = false;
	protected float revolution;
	protected float revolutionPercent;
	protected float spinPercent;
	//
	protected String[] data;
	protected float mass;
	protected float radius;
	
	public MassControl(){
		
	}
	
	public MassControl(NetworkClient client, long id, String[] data){
		super(client, id);
		//this.assetManager = app.getAssetManager();
		//this.rootNode = app.getRootNode();
		this.data = data;
	}
	
	@Override
	public void setSpatial(Spatial spatial){
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been added to a Spatial");
        }
		if (spatial == null) return;
		this.spatial = spatial;
		spatial.setLocalTranslation(translation);
		setData(data);
	}
	
	public void setData(String[] data){
	}//setData
	
	
	public float getMass(){
		return(mass);
	}

	
	public Vector3f getVector(){//from parent
		return(vector);
	}

	
	public float getDegree(){
		return(degree);
	}

	
	public float getRadius(){
		return(radius);
	}
	
	public boolean getMoving(){
		boolean moving;
		if (velocity.x != 0 || velocity.y != 0 || velocity.z != 0 || spin.x != 0 || spin.y != 0 || spin.z != 0){
			return(true);
		}
		else{
			return(false);
		}
		//return(moving);
	}
	
	
	//cascade move - moves itself and then moves it's children
	@Override
	public void move(float tpf){
		//if (spatial.getName().equals("Ship 01") || spatial.getName().equals("Ship 01")) 
			//System.out.println("MassControl.move(" + spatial.getName() + ")" + ", location=" + location + ", velocity=" + velocity + ", spin=" + spin);
		super.move(tpf);//update location
		super.rotate(tpf);
		//update degree and vector (rads)
		Vector3f d2 = parent.getWorldTranslation();
		Vector3f d1 = spatial.getWorldTranslation();
		double rad = Math.atan2(d2.z - d1.z, d2.x - d1.x);
		vector = getRadianVector(rad);
		degree = (float)rad * FastMath.RAD_TO_DEG;
		if      (degree <   0f) degree += 360f;
		else if (degree > 360f) degree -= 360f;
		//move children
		if (spatial instanceof Geometry) return;
		List list = ((Node)spatial).getChildren();
		for (int i = 0;i < list.size();i++){
			Object obj = list.get(i);
			if (obj instanceof Node){
				Node node = (Node)list.get(i);
				MassControl massControl = node.getControl(MassControl.class);
				if (massControl != null){// && node.getNumControls() > 0
					massControl.move(tpf);
				}
				else{
					//System.out.println("MassControl.move(): skipping " + node.getName());
				}
			}
		}
	}//move
	
	
	public Vector3f getCurrentRadianVector(){
		return(getRadianVector(Math.toRadians(degree)));
		
	}

	
	public Vector3f getRadianVector(double rad){
		Vector3f v = new Vector3f((float)-Math.cos(rad), (float)-Math.sin(rad),(float)-Math.sin(rad));
		return(v);
	}

	
	public Vector3f getRadianLocation(double rad){
		Vector3f v = getRadianVector(rad);
		Vector3f v2 = new Vector3f((float)v.x * orbit.x, (float)v.y * orbit.y, (float)v.z * orbit.z);//add distance
		return(v2);
	}
	
	
	@Override
	protected void controlUpdate(float tpf) {
		//TODO: add code that controls Spatial,
		//e.g. spatial.rotate(tpf,tpf,tpf);
		spatial.move(velocity.mult(tpf));
		spatial.rotate(spin.x, spin.y, spin.z);
	}

	
	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		//Only needed for rendering-related operations,
		//not called when spatial is culled.
	}

	
	public Control cloneForSpatial(Spatial spatial) {
		MassControl control = new MassControl(client, id, data);
		//TODO: copy parameters to new Control
		return control;
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

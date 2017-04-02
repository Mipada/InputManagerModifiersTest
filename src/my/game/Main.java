package my.game;

import my.game.states.EnvironmentAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import java.awt.Canvas;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
 
/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        //settings.setResolution(200 * 4, 200 * 3);//w, h
        GraphicsDevice device;
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        device.getDefaultConfiguration();
        settings.setFrameRate(60);
        Main app = new Main();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    EnvironmentAppState environmentAppState;
    @Override
    public void simpleInitApp() {
        JmeCanvasContext myContext;
        Canvas canvas;
        
        setDisplayStatView(false);
        setPauseOnLostFocus(true);
        flyCam.setEnabled(false);

        environmentAppState = new EnvironmentAppState(stateManager, rootNode, this);
        stateManager.attach(environmentAppState);

        
        //Box b = new Box(1, 1, 1);
       // Geometry geom = new Geometry("Box", b);

       // Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
       // mat.setColor("Color", ColorRGBA.Blue);
       // geom.setMaterial(mat);

       //rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    
    public AppSettings getSettings(){
        return settings;
    }
    
    
}

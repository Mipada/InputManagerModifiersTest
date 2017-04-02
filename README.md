# InputManagerModidiersTest
Small JME program to test input with modifiers
(See below for setting up the library JME-core.dir)

Use 8/2 for up/down translation.
Use 4/6 for left/right translation.
Use 7/1 for forward/backward translation.
Use Control 8/2 for down/up rotation (pitch).
use Control 4/6 for left/right rotation (yaw).
Use Control 1/3 for left/right rotation (roll). 
Use numpad 5 to stop rolling.
Use numpad 0 to stop velocity.  
Holding the key down for more that a short burst will cause momentum.  Slowing to near no momentum will cause autostop to stop translation/rotation.  
Use F1-F4 to choose a ship to control and move the camera.
Use Control F1 - F4 to control a ship but not move the camera.  You can control a ship that you are looking at.
Use 1 - 5 to select docking port to dock to.  The readout will help you locate a port and dock to it.  


To setup the library JME-core.dir
1) Make a new project, i.e. \Modifiers
2) Copy the sources (com, Common, Interface, Jme3tools) from C:\Program Files\jmonkeyplatform\jmonkeyplatform\libs\jMonkeyEngine3-sources.zip into your \Modifiers projects, src directory.  Put my InputManager.java into the com.jme3.input directory and then open Main.java and compile.  You'll have to modify all the classes that have onAction() in them.  The compiler will tell you what files to fix.  
3) Make a new folder in a JME\libs\JME3-core.dir.
Copy com, Common, Interface, Jme3tools into JME\libs\JME-core.dir.  Then in your other projects, refer to JME\libs\JME3-core.dir.  If you set the InputManager.java setEcho(true), it will print debug info and show you what it's thinking.    If you have menus and you hit the Alt key, you may have to hit Alt-Esc or Alt-Tab.  


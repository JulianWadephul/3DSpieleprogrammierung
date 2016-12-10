package mygame;

import view.PauseState;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.AssetLinkNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.texture.Texture;
import java.util.List;                           
import ctrl.BookManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Forest;
import model.Progman;
import view.GameFinishedState;
import view.GameOverState;
import view.MenuState;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CrossHatchFilter;
import model.Rain;

/**
 * Progman Version 1
 * @author Julian and Florian
 */
public class Main extends SimpleApplication{
    boolean isWalking;
    boolean isRunning;
    boolean isWalkingFast;
    boolean anyKeyPressed;
    boolean lightActivated = false;
    
    long startTime;
    float runFactor = 0.1f;
    float fullBattery = 200;
    float batteryStatus = fullBattery;
    float flashRadius = 20f;
    float outerRange = 50f;
    long fadetime = 5000;
    float fogDensity = 1.8f;
    
    final int MOVEMENTSPEED = 5;
    final int GRAVITY = 10;
    final int JUMPFACTOR = 50;
    final float WORLD_SIZE = 125.0f;
    
    
    
    
    
    Camera camera;
    Node cameraNode; // For the Flashlight
    Vector3f position;
    ColorRGBA color;
    FogFilter fog;
    
    // Figures and Textures
    BookManager bookManager;
    Progman progman;
    Spatial flash;
    SpotLight spot;
    Spatial scenefile;
    Spatial house;
    
    // Sounds and Audio
    private AudioNode audio_theme;
    private AudioNode audio_nature;
    private AudioNode audio_foodsteps;
    private AudioNode audio_breathing;
    private AudioNode audio_fast_breathing;
    private AudioNode audio_jump;
    private AudioNode audio_flash_on;
    private AudioNode audio_flash_off;
    private AudioNode audio_flash_empty;
    private AudioNode audio_item_collected;
    private AudioNode audio_progman;
    private AudioNode audio_progman2;
    private AudioNode audio_rain;
    private boolean gameOver = false;

    CrossHatchFilter filter;
    
    // Labels & Textfields
    BitmapText textField;
            
    // Stuff for Collision detection
    private Vector3f camDir = new Vector3f();
    private Vector3f walkDirection = new Vector3f(0,0,0);
    private Vector3f camLeft = new Vector3f();
    private boolean left = false, right = false, move = false, back = false;
       
    BulletAppState bulletAppState;
    CharacterControl player;
    RigidBodyControl physicsNode;
    RigidBodyControl progControl;
    RigidBodyControl flashControl;

    
    // Geometries
    Forest forest = null;
    Rain rain = null;
    
   // States
    PauseState pState;
    GameFinishedState gfState;
    GameOverState goState;
    MenuState mState;
            
    public static void main(String[] args) {
        //Hide some Information in Output
        Logger.getLogger("de.lessvoid").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme3").setLevel(Level.SEVERE);
        
        AppSettings aS = new AppSettings(true);
        aS.setSettingsDialogImage("Models/Images/progman.png");
        
        Main app = new Main();
        app.setSettings(aS);
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
        isRunning = true;
        isWalking = false;
        anyKeyPressed = false;
        camera = viewPort.getCamera();
        startTime = 0;
        
        // Physics and Collision
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        
        
        
        
        // Init functionalities
        Long time = System.currentTimeMillis();
        initListeners();
        System.out.println("0 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initAudio();
        
        System.out.println("1 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initPlayerPhysics();
        System.out.println("2 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initFlashlight();
        System.out.println("3 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initAmbientLight();
        System.out.println("4 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();

        // Init Geometries
        forest = new Forest(rootNode, assetManager,bulletAppState);
   
        System.out.println("5 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initGround(); //extrem aufwendig
        System.out.println("6 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initHouses();
        System.out.println("7 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        forest.initForest(); //extrem aufwendig
        System.out.println("8 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initSky();
        System.out.println("9 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initItems(); //relativ aufwendig
        System.out.println("10 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        bookManager.itemsCollected = 0;
        progman = new Progman(rootNode,assetManager, cam ,forest); //extrem aufwendig
        progman.setAudio_progman(audio_progman);
        progman.setAudio_progman2(audio_progman2);
        System.out.println("11 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        initHUD();
        System.out.println("12 "+(System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        
        createFog();
        makeFire();
        System.out.println("13 "+(System.currentTimeMillis()-time));
        
        
        setDisplayStatView(false);
        flyCam.setMoveSpeed(MOVEMENTSPEED);
        camera.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 1f, 100f); // Camera nur bis 100 meter
        showHUD("Finde die 8 Bücher bevor deine Zeit abläuft...");
        
        //create Filter
        FilterPostProcessor processor = (FilterPostProcessor) assetManager.loadAsset("Filters/noise.j3f");
        viewPort.addProcessor(processor);
        
        //set Filter params
        filter = processor.getFilter(CrossHatchFilter.class);
        progman.setFilter(filter);
        filter.setEnabled(true);

        filter.setColorInfluenceLine(0.8f);
        filter.setColorInfluencePaper(0.1f);
        filter.setFillValue(0.1f);
        filter.setLineDistance(1.0f);
        filter.setLineThickness(4.0f);
        
        makeItRain();
        
    }
    
    
    
    @Override
    public void simpleUpdate(float tpf) {
        position = cam.getLocation();
       
        // Updates
        gameOver = progman.updateProgman(position);
        updateFlashlight();
        updateItems();
        updateItemCollision(tpf);
        updatePhysics();
        if (lightActivated)
            updateBatteryStatus(tpf);
        rain.updateLogicalState(tpf);
        foodstepsCheck();
        isWalking = false; // Muss jedes Frame neu gesetzt werden
        fadeHUD(tpf, fadetime);
        super.simpleUpdate(tpf); // For Rain Node, updates all


    }
   
    @Override
    public void simpleRender(RenderManager rm) {
        // wird automatisch nach simple Update ausgeführt
    }
    
    
     
    // ________________________UPDATE METHODS_________________________
    
    
    
    public void updateItems(){

       for (int i = 0; i < bookManager.books.length; i++){
       bookManager.books[i].spatial.lookAt(new Vector3f(cam.getLocation().x, 0, cam.getLocation().z),new Vector3f(0,-((float) FastMath.PI),(float)(-Math.PI)));
       }


    }
    
    public void updateBatteryStatus(float tpf){
        
          if (batteryStatus > (0+tpf))
          batteryStatus = batteryStatus-tpf;
          else{
          }
    }
    
    public void updatePhysics(){
        camDir.set(cam.getDirection()).multLocal(runFactor);
        camLeft.set(cam.getLeft()).multLocal(runFactor);
        
        walkDirection.set(0, 0, 0);
        
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (move) {
            walkDirection.addLocal(camDir);
        }
        if (back) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }
    
    
    public void updateFlashlight(){
          Vector3f vectorDifference = new Vector3f(cam.getLocation().subtract(flash.getWorldTranslation()));
        flash.setLocalTranslation(vectorDifference.addLocal(flash.getLocalTranslation()));

        Quaternion worldDiff = new Quaternion(cam.getRotation().mult(flash.getWorldRotation().inverse()));
        flash.setLocalRotation(worldDiff.multLocal(flash.getLocalRotation()));

        // Move it to the bottom right of the screen
        flash.move(cam.getDirection().mult(3));
        flash.move(cam.getUp().mult(-1.5f)); // y Achse
        flash.move(cam.getLeft().mult(-1f)); // x Achse
        flash.rotate(3.4f, FastMath.PI, 0); // Rotation
        
        spot.setPosition(cam.getLocation());               
        spot.setDirection(cam.getDirection());
        
        
        
        // Intensity of light
        float t = batteryStatus/fullBattery;
               
        spot.setSpotRange(outerRange*t); 
        
        // Also: Make fog darker
        
       
    }
    
    public void updateItemCollision(float tpf){

               
        for (int i = 0; i < bookManager.books.length; i++){
        
            if(bookManager.books[i].spatial.getUserData("status").equals(false)){
                float distance = bookManager.books[i].spatial.getLocalTranslation().distance(position);
                bookManager.findNextBook(position);
                bookManager.minItemDistance = distance;
                
                if (distance < 3){
                showHUD("Du hast ein Buch über " + bookManager.books[i].name + " gefunden. " +
                        "Drücke B um es aufzunehmen.");
                }
            }

     
        }

    }
    
    
    
    // ___________________LISTENERS____________________
    private AnalogListener analogListener = new AnalogListener(){
        public void onAnalog(String name, float value, float tpf) {
            // Wird überschrieben falls er rennt
                audio_foodsteps.setPitch(1f);
                audio_foodsteps.setReverbEnabled(false);
            
               if (name.equals("Move") && isRunning == true){
                   runFactor = 0.1f;
                   isWalking = true;
                   audio_foodsteps.play();
                   audio_breathing.play();
               }
               if (name.equals("Left") && isRunning == true){ 
                   runFactor = 0.05f;
                   isWalking = true;
                   audio_foodsteps.play();
               }
               if (name.equals("Back") && isRunning == true){
                   runFactor = 0.1f;
                   isWalking = true;
                   audio_foodsteps.play();
               }
               if (name.equals("Right") && isRunning == true){
                   runFactor = 0.05f;
                   isWalking = true;
                   audio_foodsteps.play();
               } 
               if (name.equals("Run") && isRunning == true){
                   runFactor = 0.2f; // Double the speed
                   isWalkingFast = true;
                   
                   audio_breathing.stop();
                   audio_fast_breathing.play();
                   
                   audio_foodsteps.setPitch(2.0f);
                   audio_foodsteps.setReverbEnabled(true);
                   audio_foodsteps.play();  
               }
               
        }   
    };
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean isPressed, float tpf) {
            if(name.equals("Pause") && isPressed){
                
                
                
                        pState = new PauseState(flyCam, stateManager, assetManager, inputManager, audioRenderer, guiViewPort);
                        isRunning = false; // Continue or Pause game
                        stateManager.attach(pState);
                
                
                
              }
            if(name.equals("Move") && isPressed == false){
                audio_fast_breathing.stop();
                audio_foodsteps.stop();
                audio_breathing.stop();
            } 
            if(name.equals("Jump") && isPressed == true){
                audio_fast_breathing.stop();
                audio_foodsteps.stop();
                audio_breathing.stop();
                player.jump();
                audio_jump.play();
            } 
            if(name.equals("Light") && isPressed == true){
                if(!lightActivated){
                    if(batteryStatus > 0){
                        audio_flash_on.play();
                        rootNode.addLight(spot);
                        rootNode.attachChild(flash);
                        lightActivated = true;
                    }else{
                        audio_flash_empty.play();
                    }
                
                }
                
                else{
                audio_flash_off.play();
                rootNode.removeLight(spot);
                rootNode.detachChild(flash);
                lightActivated = false;
                }
            } 
            
            // Collision detection
             if (name.equals("Left")) {
              left = isPressed;
            } else if (name.equals("Right")) {
              right= isPressed;
            } else if (name.equals("Move")) {
              move = isPressed;
            } else if (name.equals("Back")) {
              back = isPressed;
            } 
             
             
             // Item collection
             
             if (name.equals("Item") && !isPressed) {
                 bookManager.findNextBook(position);
                 int index = bookManager.minItemIndex;

             if (bookManager.minItemDistance < 3 && bookManager.books[index].spatial.getUserData("status").equals(false)){                 
                 bookManager.detachChild(bookManager.books[index].spatial);
                 bookManager.itemsCollected++;
                 bookManager.books[index].spatial.setUserData("status", true);
                 audio_item_collected.play();
                 showHUD();
                 }
             }
        }
        
    };
    
    
    
    // _____________ FUNCTIONAL METHODS ____________________
    
   
    public void foodstepsCheck(){
        if (isWalking == false){
            audio_foodsteps.stop();
        }   
    }

    public void showHUD(){
        startTime = System.currentTimeMillis();
        if(bookManager.itemsCollected < bookManager.getBookCount()){
        textField.setText("You have collected " + bookManager.itemsCollected + "/" + bookManager.getBookCount() + " items.");
        } else if (bookManager.itemsCollected == bookManager.getBookCount()){
        textField.setText("You have collected all items. You can work for the AIFB now.");
        }
        textField.setAlignment(BitmapFont.Align.Center);
        guiNode.attachChild(textField);
    }
    
    public void showHUD(String text){
        startTime = System.currentTimeMillis();
        textField.setText("" + text);
        guiNode.attachChild(textField);
    }
   
    public void fadeHUD(float tpf, float fadeTime){
         if (startTime == 0)
             return;
         long time = System.currentTimeMillis();
         float t = ((float) (time - startTime))/fadeTime;
         if(t > 1){
             startTime = 0;
             return;
         }
         float colorValue = 1-t;
         color.a = colorValue;
         textField.setColor(color);
     }         
     
    
       
     // _________________INIT METHODS_______________________
    
    
    protected void initGround() {
        scenefile = assetManager.loadModel("Models/Scenes/world.j3o");
        
        final float worldSize = 125f;
        
        
        
        Box box = new Box(2.5f,3.0f,0.25f);
        Material mat_brick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap",assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        
        Spatial[] wall_north = new Geometry[51];
        for(int i = 0; i < 51; i++)
        {
            wall_north[i] = new Geometry("wall_north", box );
            wall_north[i].setMaterial(mat_brick);
            
            wall_north[i].rotate(0,(float)( Math.PI/2.0), 0);
            wall_north[i].setLocalTranslation(126.5f,0,i*5.0f-126.0f);
            rootNode.attachChild(wall_north[i]);
        }
        
        Spatial[] wall_south = new Geometry[51];
        for(int i = 0; i < 51; i++)
        {
            wall_south[i] = new Geometry("wall_south", box );
            wall_south[i].setMaterial(mat_brick);
            
            wall_south[i].rotate(0,(float)( Math.PI/2.0), 0);
            wall_south[i].setLocalTranslation(-126.5f,0,i*5.0f-126.0f);
            rootNode.attachChild(wall_south[i]);
        }
        
        Spatial[] wall_east = new Geometry[51];
        for(int i = 0; i < 51; i++)
        {
            wall_east[i] = new Geometry("wall_east", box );
           
            wall_east[i].setMaterial(mat_brick);
            
            
            wall_east[i].setLocalTranslation(i*5.0f-126.0f,0,126.5f);
            rootNode.attachChild(wall_east[i]);
        }
        
        Spatial[] wall_west = new Geometry[51];
        for(int i = 0; i < 51; i++)
        {
            wall_west[i] = new Geometry("wall_west", box );
            
            wall_west[i].setMaterial(mat_brick);
            
            
            wall_west[i].setLocalTranslation(i*5.0f-126.0f,0,-126.5f);
            rootNode.attachChild(wall_west[i]);
        }
        Box box1 = new Box(126f,2.5f,1f);
        Spatial north = new Geometry("wall_north", box1 );
        Spatial south = new Geometry("wall_south", box1 );
        Spatial west = new Geometry("wall_west", box1 );
        Spatial east = new Geometry("wall_east", box1 );
        
        
        
        north.rotate(0,(float)( Math.PI/2.0), 0);
        south.rotate(0,(float)( Math.PI/2.0), 0);
        
        north.setLocalTranslation(126.5f,0,0);
        south.setLocalTranslation(-126.5f,0,0);
        west.setLocalTranslation(0,0,-126.5f);
        east.setLocalTranslation(0,0,126.5f);
        
        CollisionShape southShape = CollisionShapeFactory.createMeshShape((Geometry) south);
        RigidBodyControl southControl = new RigidBodyControl(southShape, 0);
        south.addControl(southControl);
        bulletAppState.getPhysicsSpace().add(south);
        
        CollisionShape northShape = CollisionShapeFactory.createMeshShape((Geometry) north);
        RigidBodyControl northControl = new RigidBodyControl(northShape, 0);
        north.addControl(northControl);
        bulletAppState.getPhysicsSpace().add(north);
        
        CollisionShape westShape = CollisionShapeFactory.createMeshShape((Geometry) west);
        RigidBodyControl westControl = new RigidBodyControl(westShape, 0);
        west.addControl(westControl);
        bulletAppState.getPhysicsSpace().add(west);
        
        CollisionShape eastShape = CollisionShapeFactory.createMeshShape((Geometry) east);
        RigidBodyControl eastControl = new RigidBodyControl(eastShape, 0);
        east.addControl(eastControl);
        bulletAppState.getPhysicsSpace().add(east);
        
        
        //Add Models in scenefile to forest object List
        Node n = (Node) scenefile;
        List<Spatial> liste = n.getChildren();
        for(Spatial s : liste)
        {
            if(s instanceof AssetLinkNode)
                forest.addObject(s.getWorldBound());
        }
        
        
        rootNode.attachChild(scenefile);
        
        CollisionShape groundShape = CollisionShapeFactory.createMeshShape((Node) scenefile);
        
        RigidBodyControl groundControl = new RigidBodyControl(groundShape, 0);
        bulletAppState.getPhysicsSpace().add(groundControl);
    
        rootNode.attachChild(scenefile);
  }   
    
    public void initAudio(){
        // Background audio
       audio_theme = new AudioNode(assetManager, "Sounds/horror_theme_01.wav", true); 
       audio_theme.setPositional(false);
       audio_theme.setLooping(false);
       audio_theme.setVolume(0.2f);
       
       rootNode.attachChild(audio_theme);
       audio_theme.play();
       
       // Sound FX  
       audio_foodsteps = new AudioNode(assetManager, "Sounds/sound_fx_foodsteps1.wav", false);
       audio_foodsteps.setPositional(false);
       audio_foodsteps.setLooping(true);
       audio_foodsteps.setVolume(0.3f);
       rootNode.attachChild(audio_foodsteps);
       
       audio_breathing = new AudioNode(assetManager, "Sounds/soundFX/breathing.wav", false);
       audio_breathing.setPositional(false);
       audio_breathing.setLooping(true);
       audio_breathing.setVolume(0.3f);
       rootNode.attachChild(audio_breathing);
       
       audio_fast_breathing = new AudioNode(assetManager, "Sounds/soundFX/fast_breath.wav", false);
       audio_fast_breathing.setPositional(false);
       audio_fast_breathing.setLooping(true);
       audio_fast_breathing.setVolume(0.04f);
       rootNode.attachChild(audio_fast_breathing);
       
       audio_jump = new AudioNode(assetManager, "Sounds/soundFX/sigh.wav", false);
       audio_jump.setPositional(false);
       audio_jump.setLooping(false);
       audio_jump.setVolume(0.2f);
       rootNode.attachChild(audio_jump);
       
       audio_flash_on = new AudioNode(assetManager, "Sounds/soundFX/flash_on.wav", false);
       audio_flash_on.setPositional(false);
       audio_flash_on.setLooping(false);
       audio_flash_on.setVolume(0.2f);
       rootNode.attachChild(audio_flash_on);
       
       audio_flash_off = new AudioNode(assetManager, "Sounds/soundFX/flash_off.wav", false);
       audio_flash_off.setPositional(false);
       audio_flash_off.setLooping(false);
       audio_flash_off.setVolume(0.2f);
       rootNode.attachChild(audio_flash_off);
       
       audio_nature = new AudioNode(assetManager, "Sounds/soundFX/thunder2.wav", false);
       audio_nature.setPositional(false);
       audio_nature.setLooping(true);
       audio_nature.setVolume(0.06f);
       rootNode.attachChild(audio_nature);
       audio_nature.play();
       
       audio_flash_empty = new AudioNode(assetManager, "Sounds/soundFX/flashEmpty.wav", false);
       audio_flash_empty.setPositional(false);
       audio_flash_empty.setLooping(false);
       audio_flash_empty.setVolume(0.1f);
       rootNode.attachChild(audio_flash_empty);
       
       audio_item_collected = new AudioNode(assetManager, "Sounds/soundFX/item_collected.wav", false);
       audio_item_collected.setPositional(false);
       audio_item_collected.setLooping(false);
       audio_item_collected.setVolume(0.1f);
       rootNode.attachChild(audio_item_collected);
 
       audio_progman = new AudioNode(assetManager, "Sounds/soundFX/progman_sound.wav", false);
       audio_progman.setPositional(false);
       audio_progman.setLooping(true);
       audio_progman.setVolume(0.1f);
       rootNode.attachChild(audio_progman);
       
       audio_progman2 = new AudioNode(assetManager, "Sounds/soundFX/progman_sound2.wav", false);
       audio_progman2.setPositional(false);
       audio_progman2.setLooping(true);
       audio_progman2.setVolume(0.1f);
       rootNode.attachChild(audio_progman2);
       
       audio_rain = new AudioNode(assetManager, "Sounds/rain.wav", false);
       audio_rain.setPositional(false);
       audio_rain.setLooping(true);
       audio_rain.setVolume(0.03f);
       rootNode.attachChild(audio_rain);
       audio_rain.play();
    }
      
    public void initSky(){
        Texture west = assetManager.loadTexture("Models/sky/purplenebula_bk.jpg");
        Texture east = assetManager.loadTexture("Models/sky/purplenebula_dn.jpg");
        Texture north = assetManager.loadTexture("Models/sky/purplenebula_ft.jpg");
        Texture south = assetManager.loadTexture("Models/sky/purplenebula_lf.jpg");
        Texture up = assetManager.loadTexture("Models/sky/purplenebula_rt.jpg");
        Texture down = assetManager.loadTexture("Models/sky/purplenebula_up.jpg");
        
        Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
        rootNode.attachChild(sky);  
    }
     
    public void initHouses(){
       house = assetManager.loadModel("Models/Houses/small_house.j3o");
       house.scale(7.0f);
       house.setLocalTranslation(new Vector3f(10,0,10));
       
       
       CollisionShape houseShape = CollisionShapeFactory.createMeshShape((Node) house);
       RigidBodyControl houseControl = new RigidBodyControl(houseShape, 0);
       house.addControl(houseControl);
       bulletAppState.getPhysicsSpace().add(house);
       houseControl.setPhysicsLocation(new Vector3f(10, 0, 10));
       
       forest.addObject(house.getWorldBound());
       rootNode.attachChild(house);

    }
    
    public void initItems(){
        bookManager = new BookManager(assetManager);
        rootNode.attachChild(bookManager);
    }
    
    public void initHUD(){
        guiNode.setQueueBucket(Bucket.Gui);
        textField = new BitmapText(guiFont, false);          
        textField.setSize(2*guiFont.getCharSet().getRenderedSize()); 
        color = new ColorRGBA(ColorRGBA.White);
        textField.setColor(color);                             // font color
        textField.setText("");             // the text
        //textField.setLocalTranslation(100, settings.getHeight()/2, 0); // position
        textField.setBox(new Rectangle(settings.getWidth()/2-settings.getWidth()/4, settings.getHeight()*3/4, settings.getWidth()/2, settings.getHeight()/2));
        textField.setLineWrapMode(LineWrapMode.Word);
    }
    
    public void initListeners(){
        inputManager.addMapping("Move", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Light", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("Run", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Item", new KeyTrigger(KeyInput.KEY_B));

        inputManager.addListener(analogListener, "Move");
        inputManager.addListener(analogListener, "Left");
        inputManager.addListener(analogListener, "Back");
        inputManager.addListener(analogListener, "Right");
        inputManager.addListener(analogListener, "Run");

        inputManager.addListener(actionListener, "Pause");
        inputManager.addListener(actionListener, "Move");
        inputManager.addListener(actionListener, "Left");
        inputManager.addListener(actionListener, "Back");
        inputManager.addListener(actionListener, "Right");
        inputManager.addListener(actionListener, "Jump");
        inputManager.addListener(actionListener, "Light");
        inputManager.addListener(actionListener, "Item");
        

    }
    
    public void initPlayerPhysics(){
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 2f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setPhysicsLocation(new Vector3f(0, 2, 0));
        bulletAppState.getPhysicsSpace().add(player);
        player.setGravity(20);

    }
   
    public void initFlashlight()
    {      
        flash = assetManager.loadModel("Models/Flashlight/flashlight.j3o");
        flash.scale(2f);
        

        // Cone Light
        
        spot = new SpotLight();
        
        spot.setSpotRange(outerRange);                           // distance
        spot.setSpotInnerAngle(10f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot.setSpotOuterAngle(20f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(new ColorRGBA((float) 1, 1, (float) 1, 1));         // light color
        spot.setPosition(flash.getLocalTranslation());               // shine from camera loc
        spot.setDirection(cam.getDirection());             // shine forward from camera loc
        
    }
    
    public void initAmbientLight(){
        AmbientLight al = new AmbientLight();
        AmbientLight al2 = new AmbientLight();
        al.setColor(ColorRGBA.Black.mult(0.1f));
        al2.setColor(ColorRGBA.Blue.mult(0.1f));


        rootNode.addLight(al);
        rootNode.addLight(al2);
    }
    
    public void createFog(){
        // Verwendung von exponentiellem Verhalten:
        // f = e^(-d*b) mit d = distance, b = attenuation
        // final color = (1.0 -  f) * fogColor + f * light Color
        // Rangebased technique -> Vertex to Camera
        // Vertex -> Dreiecke, Fragment -> Pixelweise
        // Vertexshader berechnet position und übergibt sie weiter an fragemnt shader
        /*
         * uniform -> User defined variables (global)
         * attribute -> Per vertex variables (position e.g)
         * varying -> Vertex shader to fragment shader variables
         */
       /** Add fog to a scene */
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        fog=new FogFilter();
       // fog.setFogColor(new ColorRGBA((float) 80/255,(float) 0, (float) 100/255,1f));
        fog.setFogColor(new ColorRGBA(0.3f,0.3f,0.3f,1));
        fog.setFogDistance(100);
        fog.setFogDensity(fogDensity);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);

    }
    
    public void makeFire(){
         /** Uses Texture from jme3-test-data library! */
        ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect.setMaterial(fireMat);
        fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
        fireEffect.setEndColor( new ColorRGBA(1f, 0f, 0f, 1f) );   // red
        fireEffect.setStartColor( new ColorRGBA(1f, 1f, 0f, 0.5f) ); // yellow
        fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect.setStartSize(0.6f);
        fireEffect.setEndSize(0.1f);
        fireEffect.setGravity(0f,0f,0f);
        fireEffect.setLowLife(0.5f);
        fireEffect.setHighLife(3f);
        fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
        fireEffect.setLocalTranslation(new Vector3f(90.729515f, 0.0f, 14.6222f));
        rootNode.attachChild(fireEffect);
    }
    
    public void makeItRain(){
        rain = new Rain(assetManager,cam,2); // Last param is the weather intensity
        rootNode.attachChild(rain);
    }
 
}


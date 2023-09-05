/*
 * Compton.java
 * Simple Animated Simulation of Compton Scattering
 * Code: Jan Humble
 *
 *
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Compton extends Applet implements Runnable {

    public static double PLANCK = 4.14;  // 10 ^ -18 eVs
    public static double COMPTONWAVELENGTH = 0.02426;  // � 
    public static double LIGHTSPEED = 2.998;  // 10 ^ 8 m/s
    
    
    public static Font font1 = new Font("Helvetica", Font.BOLD, 14);
    public static Font font2 = new Font("Helvetica", Font.PLAIN, 10);
    public static Font font3 = new Font("Helvetica", Font.BOLD, 12);
    
    boolean toggleEmission = false;
    

    int time = 0;
    double deltaT = 5;
    double theta, phi;
    
    int dY, dX;           // Applet size parameters

    Panel headerPanel;
    ComptonAnimCanvas animCanvas;
    InfoPanel infoPanel;
    // EmitPanel emitPanel;
   
    Thread animThread;    // Thread animation process
    
    // private Button emit_button;


    
    // Compton particles

    Photon photon;
    Electron electron;

  
    public void init(){
	
	// Get the drawing area of the applet 
	dY=getSize().height; dX=getSize().width;
	
	double photonenergy = 
	    (Double.valueOf(getParameter("photonenergy"))).doubleValue();
	
	photon = new Photon(photonenergy, 90, 150);
	electron = new Electron(0.0, dX/2, 150);

       	theta = 45.0; 
	phi = 0.0;

       	

	// Set up a title
	
	headerPanel = new Panel();
	headerPanel.setBackground(Color.white);
	Label title = new Label("Efeito Compton");
	title.setFont(Compton.font1);
	headerPanel.add(title);
       
    
	// Prepare animation area

       	animCanvas = new ComptonAnimCanvas(this);
	animCanvas.setSize(dX, 200);

	// Prepare an information display canvas
	
	infoPanel = new InfoPanel(this);	
	infoPanel.setSize(300, 70);
	
     

	// set up the layout

	GridBagLayout layout = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(layout);
	
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	layout.setConstraints(headerPanel, gbc);
	add(headerPanel);
	

	gbc.weighty = 1.0;
	layout.setConstraints(animCanvas, gbc);	
	add(animCanvas);
		
	InputPanel inputPanel = new InputPanel(this);
	gbc.weighty = 0.0;
	gbc.gridwidth = GridBagConstraints.RELATIVE;
	layout.setConstraints(inputPanel, gbc);
	add(inputPanel);
	
	gbc.weightx = 0.0;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	layout.setConstraints(infoPanel, gbc);
	add(infoPanel);

       
	
    }
    
    
    
    public void start() { 
	if (toggleEmission && animThread == null){ 
	    animThread = new Thread(this); 
	    animThread.start(); 
	} 
    } 
    
    
    public void stop() { 
	animThread = null;
	toggleEmission = false;
    } 
    
    
    public void run() { 
	
	    
	while (time < 100 && animThread != null) {    
	    
	    // Use sleep to pause between movements 
	    try { 
		
		    Thread.sleep((int)(50)); 
		    
	    } catch (InterruptedException e) {}
	    
	    
	    // Collision detection
	    
		if (photon.currentX == electron.currentX &&
		    photon.currentY == electron.currentY)
		    {
			collision(photon, electron);
			infoPanel.repaint();
		    }
		
		photon.move(deltaT);
		electron.move(deltaT);
		
		animCanvas.repaint(); 
		time++;
		
	} 
	
	stop();
		
	// Reset the process
	// reset();
    }
    
    
    
    public void reset() {
	// Reset the process
	
	time = 0;
	electron.reset();
	photon.reset();	

	animCanvas.repaint();
	infoPanel.repaint(); 
	
    }
    


    /* collision:                                         */
    /* Calculates new parameters after Compton Collision. */
    /* Can be overloaded to simulate collisions with      */
    /* different kinds of particles.                      */                 

    
    public void collision(Photon photon, Electron electron)
    {
	
	double prefreq = photon.frequency;
	double prewave = photon.wavelength;
	double postfreq, postwave;


	postwave = Math.abs
	    (prewave + COMPTONWAVELENGTH * (1 - Math.cos(theta)));
	postfreq = LIGHTSPEED / postwave;
	
	// Calculate the corresponding Compton scat. angle
	
	phi = Math.atan 
	    ( postfreq * Math.sin(theta) / 
	      (prefreq  - postfreq * Math.cos(theta)));
	
    
			
	// set new direction and velocities for the particles
	
	electron.setPath(phi, photon.vel);
	photon.setPath(-theta, photon.vel);
	
	// set new particle values
	
	electron.energy = Compton.PLANCK * (prefreq - postfreq);
	
	photon.wavelength = postwave;
	photon.frequency = postfreq;
	photon.energy = postfreq * Compton.PLANCK;
	photon.tailLength = (int) (photon.tailLength * postwave / prewave);

	
    }

    public static String num2string (double num)
    {
	String s =Double.toString(num);
	
	return s.substring(0, Math.min(6, s.length()));
    }



}




class ComptonAnimCanvas extends Canvas {
    
    Image offscreen;      // Background graphics buffer
    
    int dX, dY;
    
    Compton comptonSim;
       
 
    ComptonAnimCanvas (Compton c) {
	
	this.comptonSim = c;
	setBackground(Color.white);
	

    }
    
    public void update (Graphics g){
	
	Dimension d = this.getSize();
	
	// Update using double buffering
	
	
	if (offscreen == null)
	    offscreen = this.createImage(d.width, d.height);
	
	// Draw to background buffer
	g = offscreen.getGraphics();
	paint(g);
	g.dispose();
	
	    // draw from background buffer to screen buffer
	g = this.getGraphics();
	g.drawImage(offscreen, 0, 0, this);
	
	
    }

    
    public void paint(Graphics g) {
	
	dX = getSize().width;
	dY = getSize().height;
	
	Electron e = comptonSim.electron;
	Photon p = comptonSim.photon;
	
	// Clear screen
 
	g.setColor(getBackground());
	g.fillRect(0,0, dX, dY);
	
	
	// draw axis with origin on the electron

	g.setColor(Color.lightGray);
	g.drawLine(0, (int) e.startY, dX, (int) e.startY);
	g.drawLine((int) e.startX, 0, (int) e.startX, dY);
		
	g.setColor(Color.black);
	g.setFont(Compton.font2);
	g.drawString("time = " + Integer.toString(comptonSim.time), 20, 20);
	
	// Draw particles
	p.draw(g);
	e.draw(g);
	


     }
}


class InputPanel extends Panel implements ActionListener {

    Button emitButton;
    TextField textEnergy;
    TextField textTheta;

    Compton comptonSim;
    
    InputPanel(Compton c) {
	
	this.comptonSim = c;

	this.setLayout(new GridLayout(5,1));

	this.setBackground( Color.white);
	setFont(Compton.font2);
	emitButton = new Button("EMITIR");
	emitButton.addActionListener(this);
       	
	add(emitButton); 
	
	// setFont(Compton.font2);
	textEnergy = 
	    new TextField(Double.toString(comptonSim.photon.energy), 6);
	textTheta = 
	    new TextField(Double.toString(comptonSim.theta), 6);
       

	
	add(new Label("Energia Cin�tica do F�ton (keV)"));
	add(textEnergy); 
	add(new Label("�ngulo dispers�o F�ton (deg)"));
	add(textTheta); 
	
	
    } 
    
    public void actionPerformed(ActionEvent ev) {

	
	double e = (Double.valueOf(textEnergy.getText())).doubleValue();
	double t = Math.PI / 180.0 * 
	    (Double.valueOf(textTheta.getText())).doubleValue();
     
	comptonSim.reset();
	comptonSim.photon.setEnergy(e);
	comptonSim.theta = t;
	comptonSim.infoPanel.repaint();
	comptonSim.toggleEmission = true;	
	comptonSim.start();

    }
    
    
}



class InfoPanel extends Canvas {
   
    Compton comptonSim;

    InfoPanel (Compton c) {
	
	this.comptonSim = c;

	setBackground(Color.white);
    }
    
    public void paint(Graphics g) {

	Electron e = comptonSim.electron;
	Photon p = comptonSim.photon;
      
	// Draw photon info
	
	g.setColor(Color.blue);
	g.setFont(Compton.font3);
	g.drawString("F�ton", 20, 12);
	   
	g.setFont(Compton.font2);
	g.drawString("Energia = " +
		     Compton.num2string(p.energy) + " keV", 
		     20, 28);
	g.drawString("freq��ncia = " +
		     Compton.num2string(p.frequency) + " EHz", 
		     20, 42);
	g.drawString("compr. de onda = " +
		     Compton.num2string(p.wavelength) + " �", 
		     20, 56);
	g.drawString("theta = " + 
		     Compton.num2string(comptonSim.theta * 180/Math.PI), 
		     20, 70);


	// Draw electron info

	g.setColor(Color.red);
	g.setFont(Compton.font3);
	g.drawString("El�tron", 170, 12);
	
	g.setFont(Compton.font2);
	g.drawString("Energia Cin�tica = " + 
		     Compton.num2string(e.energy) + " keV", 
		     170, 28);
	g.drawString("phi = " + 
		     Compton.num2string(comptonSim.phi * 180/Math.PI), 
		     170, 42);	
	
	/* g.drawString("tail = " + 
	   Compton.num2string(p.tailLength), 
	   170, 56);
	   
	*/
    }
    
    
}





/* ------------------------------------------------ */
/* Base Particle class                              */
/*                                                  */
/* ------------------------------------------------ */

abstract class Particle {
    
    Color color;
    
    double startX, startY;
    double currentX, currentY;
    double startVel, vel, velX, velY;

    double startAngle, angle;

    double energy, startenergy;

    Particle(double energy, int startX, int startY, 
	     double startAngle, double startVel, 
	     Color color)
    {
	this.startX = startX;
	this.startY = startY;
	this.currentX = startX;	
	this.currentY = startY;
	
	this.startAngle = startAngle;
	this.startVel = startVel;
	
	this.color = color;	
	
	this.startenergy = energy;
	setEnergy(energy);
	
	setPath(startAngle, startVel);
	
    }   

    public void reset() {
	
	currentX = startX;
	currentY = startY;
	
	energy = startenergy;
	setPath(startAngle, startVel);
	
	
    }
    
    
    public void setPath(double newAngle, double vel)
    {
	this.angle = newAngle;
	this.vel = vel;
	velX = vel*Math.cos(angle);
	velY = vel*Math.sin(angle);
    }
    
    
    public void move(double deltaT) {
	
	currentX = currentX + velX*deltaT;
	currentY = currentY - velY*deltaT;
    }


    public void setEnergy (double energy)
    {
	this.energy = energy; 
    }
    
    abstract void draw(Graphics g);
    
    
}




/* ------------------------------------------------ */
/* Electron class: Inherits Particle                */
/*                                                  */
/* ------------------------------------------------ */

class Electron extends Particle {
   
    final double restenergy = 511; // keV

    int drawDiameter;   // define diameter to draw on screen

   
    // Constructor

    Electron(double energy, int startX, int startY) {
	
	super(energy, startX, startY, 0, 0, Color.red);
	
	this.drawDiameter = 15;
      
    }
    
    public void draw(Graphics g) {
	
	g.setColor(color);
	g.fillOval((int) currentX - drawDiameter/2, 
		   (int) currentY - drawDiameter/2, 
		   drawDiameter, drawDiameter);
	
    }
}




/* ------------------------------------------------ */
/* Photon class: Inherits Particle                  */
/*                                                  */
/* ------------------------------------------------ */


class Photon extends Particle {

    double frequency, wavelength;

    
    final int TAIL = 60;
    int drawAmplitud, tailLength;  // photon graphical parameters
  

    // Constructor

    Photon (double energy, int startX, int startY) {
	
	super(energy, startX, startY, 0.0, 1.0, Color.blue);
	
	setEnergy(energy);
	
	
	this.drawAmplitud = 5;
	this.tailLength = TAIL;
	

    }
    
    public void setEnergy (double energy) {
	

	
	this.energy = energy;
	this.frequency = energy / Compton.PLANCK;
	this.wavelength = Compton.LIGHTSPEED / frequency;
       

	
    }
    
    public void reset() {
	super.reset();
	
	this.frequency = energy / Compton.PLANCK;
	this.wavelength = Compton.LIGHTSPEED / frequency;
	this.tailLength = TAIL;
    }
    
    
    public void draw(Graphics g)
    {
		
	g.setColor(color);
	
	double x1, x2, y1, y2, x1r, x2r, y1r, y2r;
	
	
	for (int x = - tailLength; x < 0; x++)
	    {	
		// Calculate animated sinus tail
 
		y1 = Math.sin((currentX + x)*8*Math.PI/tailLength) * drawAmplitud;
		y2 = Math.sin((currentX+x+1)*8*Math.PI/tailLength) * drawAmplitud;
		

		// Rotate tail according to photon path angle

		x1r = currentX + 
		    Math.cos(angle)*x - Math.sin(-angle)*y1; 
		y1r = Math.sin(-angle)*x + Math.cos(angle)*y1 + currentY;
		
		x2r = currentX +
		    Math.cos(angle)*(x+1) - Math.sin(-angle)*y2; 
		y2r = Math.sin(-angle)*(x+1) + Math.cos(angle)*y2 + currentY;
		
		g.drawLine((int) x1r, (int) y1r, (int) x2r, (int) y2r);
	
		// g.fillOval(currentX - 5, currentY - 5, 10, 10);
	
	    }
    }
}

    

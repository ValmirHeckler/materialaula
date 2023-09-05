// Modification history
// May 31,1997 fixed bug when object located at focus point 
// May  22,1997 add  paraxial ray option for mirror
// Feb 21,1997 major modification, almost rewritten the whole code
// try without frame => very slow for netscape
// Nov. 3, 1996 impletemt double buffering
//-----------------------------------------------------
// written by Fu-Kwun Hwang
// I hope that you enjoy this applet
// Suggestions? E-mail to  hwang@phy03.phy.ntnu.edu.tw
//-----------------------------------------------------
import java.awt.*;

public class thinLens extends java.applet.Applet{
	String buttonText="start";
	String windowTitle="Thin Lens demonstration by Fu-Kwun Hwang(1996)";
	int windowWidth = 600;
	int windowHeight = 350;
	LensWindow m;
	int windowCount=0;
	Color bgColor=new Color(0xC8,0xDF,0xD0);
	
	String rts,STR[]={"Reset","p","q","f","m","lens","mirror","Paraxial"};
	public void init() {
		setBackground(bgColor);
		for(int i=0;i<STR.length;i++){
			if((rts=getParameter(STR[i]))!=null)
				STR[i]=new String(rts);
		}
		String str;
		// get parameters
		if((str=getParameter("buttonText"))!=null)
				buttonText=str;
		if((str=getParameter("windowTitle"))!=null)
				windowTitle=str;
		if((str=getParameter("windowWidth"))!=null)
			windowWidth=Integer.parseInt(str);
		if((str=getParameter("windowHeight"))!=null)
			windowHeight=Integer.parseInt(str);
		if((str=getParameter("autoStart"))!=null){
			m = new LensWindow(windowTitle, true,STR);
			go();
		}
		add(new Button(buttonText));
	}

	private void go(){
		m.resize(windowWidth,windowHeight);
		m.show();
		m.start();
	}

	public boolean action(Event e,Object arg) {
		if (e.target instanceof Button &&
					((String)arg).equals(buttonText)) {
			m = new LensWindow(String.valueOf(++windowCount)+":"+
				windowTitle, true,STR);
		}
		go();
		return true;
	}
	
	//allow the applet to also run as an application.
	public static void main(String args[]) {
		new thinLens().begin();
	}

	private void begin() {
		m = new LensWindow(windowTitle, false,STR);
		go();
	}
}
 
class LensWindow extends Frame {
	boolean is_applet;
	boolean is_lens=true;
	int yOffset=30;
	String STR[];
	LensWindow(String title, boolean isapp,String[] s){
		super(title);
		is_applet=isapp;
		setBackground(Color.lightGray);
		STR=s;
		init();
	}
	public void start(){
		reset();
		repaint();
//		lMirror.hide();
//		cThin.hide();
	}

	public boolean handleEvent(Event e) {
		if (e.id == Event.WINDOW_DESTROY) {
			hide();
			removeAll();
			dispose();
			if(!is_applet) System.exit(0);
		}   
		return super.handleEvent(e);
	}

	TextField textP,textQ,textF,textM;//,textR;
//	Label lMirror;
	Checkbox cThin;
	Button type;
	Dimension area;
	int xc,yc;
	double scale=10.;
	void init(){
		Panel p=new Panel();
//		p.add(textR=new TextField(" X , Y",6));
		p.add(new Label(STR[1]));
		p.add(textP=new TextField("10.",2));
		p.add(new Label(STR[2]));
		p.add(textQ=new TextField("10.",2));
		p.add(new Label(STR[3]));
		p.add(type=new Button("+"));
		p.add(textF=new TextField("5.",2));
		p.add(new Label(STR[4]));
		p.add(textM=new TextField("1.",2));
		Choice c;
		p.add(c=new Choice());
		c.addItem(STR[5]);
		c.addItem(STR[6]);
		p.add(new Button(STR[0]));
//		p.add(lMirror=new Label());
		p.add(cThin=new Checkbox(STR[7]));
		cThin.setState(true);
		cThin.show();
		thinMirror=cThin.getState();
		add("North",p);
		show();
	}
	boolean thinMirror=true;
	public boolean action(Event ev, Object arg) {
		if (ev.target instanceof Button) {
			String label = (String)arg;
			if(label.equals(STR[0]))reset();
			else{
				if(label.equals("+"))	type.setLabel("-");
				else if(label.equals("-"))	type.setLabel("+");
				lf*=-1.;
				repaint();
			}
		}else if( ev.target instanceof TextField){
			String label = (String)arg;
			double value;
			value=Double.valueOf(label).doubleValue();
			if(ev.target==textP)
				textInput(1,value);
			else if(ev.target==textQ)
				textInput(2,value);
			else if(ev.target==textF)
				textInput(3,value);
			else if(ev.target==textM)
				textInput(4,value);
				repaint();
		}else if(ev.target instanceof Choice){
			String label = (String)arg;
			if(label.equals(STR[5])){
				is_lens=true;
//				lMirror.hide();
//				cThin.hide();
			}else{
				is_lens=false;
//				lMirror.show();
				cThin.show();
				show();
			}
			lshow();
			repaint();
		}else if(ev.target instanceof Checkbox){
			thinMirror=cThin.getState();
			lshow();
			repaint();
		}
		return true;
	}

	public void reset() {
		area=size();
		area.height-=yOffset;
		xc=area.width/2;
		yc=area.height/2;
		double ff=10.*scale;
		type.setLabel("+");
		textF.setText(Double.toString(ff));
		linit(xc,yc,(int)(0.7*yc),ff);
		xc-=(int)(2.*lf); // initial object position
		yc=yc*3/4;
		repaint();
	}

	boolean objectmove=false,rightClick=false,moveLine=false;
	int xp,yp;// vertical reference line
	public boolean mouseDown(Event e, int x, int y){
		y-=yOffset;
		if(Math.abs(x-xp)<5)moveLine=true;
		else if(!lmouseDown(x,y)){
			if(Math.abs(x-xc)<5 ){
				xc=x;
				yc=y;
				repaint();
				objectmove=true;
			}
		}
		if(e.modifiers==Event.META_MASK)//"Right Click, ";
				rightClick=true;
		return true;
	}
	
	public boolean mouseDrag(Event e, int x, int y){
		y-=yOffset;
		if(working)return true;
		if(lmouseDrag(x,y)){
			if(rightClick)xc=lxc-ox;// also move objects
			repaint(); // change lens
		}else if(objectmove){ // move object
			if(!is_lens && x>lxc || lh<Math.abs(y-lyc) )return true;
			xc=x;
			yc=y;
			if(rightClick)lxc=xc+side*ox;// also move lens
			writeXY(x,y);
			repaint();
		}else if (moveLine){
			xp=x;
			yp=y;
			writeXY(x,y);
			repaint();
		}
		return true;
	}	

	public boolean mouseUp(Event e, int x, int y){
		y-=yOffset;
		lmouseUp(x,y);
		objectmove=false;
		rightClick=false;
		moveLine=false;
		repaint();
		return true;
	}

	public boolean mouseMove(Event  evt, int  x, int  y){
		y-=yOffset;
		if(working)return true;
		if(!objectmove)writeXY(x,y);
		repaint();
		return true;
	}
	String msg=" X , Y";
	private void writeXY(int x, int y){
		//textR.setText(
			msg=
			Double.toString((int)(10.*(lxc-x)/scale)/10.)+
			","+
			Double.toString((int)(10.*(lyc-y)/scale)/10.);
	}

	public boolean mouseExit(Event  evt, int  x, int  y){
		y-=yOffset;
//		textR.setText(" X , Y");
		msg=" X , Y";
		repaint();
		return true;
	}

	public void textInput(int type,double value){
		switch (type) {
		case 1: // P
			xc=lxc-(int)(value*scale);
			break;
		case 2: // Q
			xc=lxc-(int)(1./(1./lf-1./(scale*value)));
			break;
		case 3: // F
			lf=value*scale;
			break;
		case 4: // M
			xc=lxc-(int)(Math.abs((1.-1./value)*lf));
			break;
		}
	}

	Dimension offDimension;
	Image offImage;
	Graphics g;

	public void paint(Graphics gs){
		update(gs);
	}
	private static boolean working=false;
	public void update(Graphics gs){
		working=true;
		area=size();
		area.height-=yOffset;
		if ( (g == null) // if resize window
				|| (area.width != offDimension.width)
				|| (area.height != offDimension.height) ) {
			offDimension = area;
			offImage = createImage(area.width, area.height);
			g = offImage.getGraphics();
			xp=area.width-50;
			reset();
		}
		//Erase the previous image.
		g.setColor(getBackground());
		g.fillRect(0, 0, area.width, area.height);
		g.setColor(Color.black);
		g.drawString(msg,5,15+yOffset);
		g.drawLine(0,lyc,area.width,lyc);
		drawGrid(g);
		drawRay(g);
		lshow();
		g.setColor(Color.white);
		g.drawLine(xp,30,xp,area.height-30);
		if(moveLine)g.drawLine(xp-15,yp,xp+15,yp);
		gs.drawImage(offImage, 0, yOffset, this);
		working=false;
	}

	private void drawGrid(Graphics g){
		g.setColor(Color.gray);
		int i,j;
		for(i=lxc,j=0;i>0;j++,i=lxc-(int)(j*scale))
			g.drawLine(i,lyc-2,i,lyc+2);
		for(i=lxc,j=0;i<area.width;j++,i=lxc+(int)(j*scale))
			g.drawLine(i,lyc-2,i,lyc+2);
		//g.setColor(Color.black);
	}

	private void writeText(TextField obj,double value){
		value=(double)((int)(10.*value/scale))/10.;
		//formated output, keep same digits outputs after .
		obj.setText(Double.toString(value));
	}

	int ox,oy,side;		// object position
	double ix,iy;			// image position
	double magnify;
	boolean inrange,normal;
	public void drawRay(Graphics g){
		if(lxc>xc){
			ox=lxc-xc;
			side=1;
		}else{
			ox=xc-lxc;
			side=-1;
		}
		oy=lyc-yc;
		if(ox==lf){// located at focus point
			normal=false;
			textQ.setText("Inf");
		}else {
			normal=true;
			ix=1./(1./lf-1./ox);
			writeText(textQ,ix);
		}
		magnify=-ix/ox;
		// write P,Q,F
		writeText(textP,Math.abs(lxc-xc));
		writeText(textF,Math.abs(lf));
		textM.setText(d2String(magnify));
		int x1,y1,x2,y2; // ray paths
		x1=lxc-side*ox;
		y1=lyc-oy;
		if(normal)iy=magnify*oy;
		else{// May 31,1997
			double mag2=-100.*magnify/Math.abs(magnify);
			iy=-mag2*oy;
			ix=mag2*ox;
		}
		y2=lyc-(int)iy;
		//draw object
		g.setColor(Color.blue);
		drawit(g,x1,lyc,oy);
		if(lh>Math.abs(iy))inrange=true;
		else inrange=false;
		if(is_lens){// lens
			x2=lxc+side*(int)ix;
			g.drawLine(x1,y1,lxc,y1);		//1-1 平行入射光
			if(magnify<0){
				g.drawLine(lxc,y1,x2,y2);	//1-2
				g.drawLine(x1,y1,x2,y2);		//2		穿過透鏡中心
				if(inrange){
					g.drawLine(x1,y1,lxc,y2);	//3-1	穿過焦點入射光
					g.drawLine(lxc,y2,x2,y2);	//3-2	平行主軸
				}
				g.setColor(Color.blue);
			}else{
				if(lf>0){		// converging lens
					g.drawLine(lxc,y1,lxc+2*side*(int)lf,lyc+oy);	//1-2
					g.drawLine(x1,y1,lxc-side*(int)ix,lyc+(int)iy);	//2-1
					if(inrange)	{
						g.drawLine(x1,y1,lxc,lyc-(int)iy);							//3-1
						g.drawLine(lxc,y2,lxc+2*side*(int)lf,y2);			//3-2
					}
					g.setColor(Color.green);
					g.drawLine(x1,y1,x2,y2);								//2-2
					g.drawLine(lxc,y1,x2,y2);							//1-3
					if(inrange)	g.drawLine(lxc,y2,x2,y2);							//3-3
				}else{				// diverging lens
					g.drawLine(lxc,y1,lxc-2*side*(int)lf,lyc-3*oy); //1-2
					g.drawLine(x1,y1,lxc+side*ox,lyc+(int)oy);	//2
					if(inrange){
						g.drawLine(x1,y1,lxc,y2);										//3-1
						g.drawLine(lxc,y2,lxc-2*side*(int)lf,y2);	//3-2
					}
					g.setColor(Color.green);
					g.drawLine(lxc,y1,lxc+side*(int)lf,lyc);	//1-3
					if(inrange)	g.drawLine(lxc,y2,lxc+side*(int)ix,y2);			//3-3
				}
			}
		}else{// mirror
			double angle=Math.asin(oy/lr);
			double dw=0.,dw2;
			int xx,xx2,yy;
			x2=lxc-(int)ix;
			if(!thinMirror)dw=lr*(1.-Math.cos(angle));
			xx=lxc-(int)dw;
			angle*=2.;
			if(lf>0){// concave mirror 凹面鏡
				boolean within;
				g.drawLine(x1,y1,lxc,lyc); //2-1 物 通過 鏡面中心
				yy=lyc-(int)(lr*oy/(lr-ox));
				double mm=iy/(-ix+lr),xt=0.;
				if(!thinMirror) xt=lr*(1./Math.sqrt(1.+mm*mm)-1.);
				if( (within=(Math.abs(mm*(xt+lr))<lh))&& !thinMirror)
					g.drawLine(x1,y1,lxc+(int)xt,yy=lyc-(int)(mm*(xt+lr)));//4-1垂直入射鏡面
				xx2=lxc+(int)(iy*(lf-ox)/oy-lf);
				if(oy>0)side=1;else side=-1;
				if(oy*iy>0){//虛像
					if(!thinMirror)g.drawLine(x1,y1,lxc-(int)lr,lyc);//4-2 垂直入射
					else g.drawLine(lxc-(int)(2.*lf),lyc,lxc,yy=lyc-(int)(iy*2.*lf/(2.*lf-ix)));
					g.drawLine(lxc,lyc,lxc-2*ox,lyc+2*oy);//2-2 鏡心反射
					g.setColor(Color.green);
					g.drawLine(lxc,lyc,x2,y2);//2-3 
					g.drawLine(lxc+(int)xt,yy,x2,y2);//2-1
					g.setColor(Color.blue);
					g.drawLine(x1,y1,xx,y1);//1-1 平行入射光
					if(!thinMirror)g.drawLine(xx,y1,lxc-(int)lr,lyc-oy+(int)(Math.tan(angle)*(lr-dw)));//1-2
					g.setColor(Color.green);
					if(!thinMirror){
						g.drawLine(xx,y1,lxc-(int)ix,lyc-oy-(int)(Math.tan(angle)*(-ix+dw)));//1-3
						g.setColor(Color.yellow);
						g.drawLine(lxc-(int)lf,lyc,x2,y2);
					}else{
						g.drawLine(lxc,y1,x2,y2);
						g.drawLine(lxc,yy,x2,y2);
						g.setColor(Color.blue);
						g.drawLine(lxc,y1,lxc-(int)lf,lyc);
					}
				}else{//實像
					g.drawLine(lxc,lyc,x2,y2);//2-2 鏡面中心--像
					if(within&&!thinMirror)g.drawLine(x1,y1,x2,y2);//4-2
					g.drawLine(x1,y1,xx,y1);//1-1 平行入射光
					if(!thinMirror){
						g.drawLine(xx,y1,lxc-(int)ix,lyc-oy+side*(int)(Math.abs(Math.tan(angle))*(ix-dw)));//1-2
						g.setColor(Color.yellow);
					}
					g.drawLine(xx,y1,lxc,yy=lyc+(int)(lf*iy/(ix-lf)));
					g.drawLine(x2,y2,lxc,yy);
					if(inrange){
						g.drawLine(x1,y1,lxc,yy=lyc+(int)(lf*oy/(ox-lf)));//3-1
						g.drawLine(x2,y2,lxc,yy);//3-2
					}
				}
				if(!thinMirror){
					g.setColor(Color.white);
					g.drawLine(lxc-(int)lr,lyc,xx,y1);
				}
			}else{// convex mirror
				dw2=lr*(1.-Math.cos(Math.asin(oy/(lr+ox))));
				yy=lyc+(int)((2*lf+dw2/2.)*oy/(ox-2.*lf));
				g.drawLine(x1,y1,xx=lxc+(int)dw2,yy);//2垂直射向鏡面
				g.drawLine(x1,y1,lxc,lyc);//3 通過鏡面中心
				g.drawLine(lxc-2*ox,lyc+2*oy,lxc,lyc);//3-1
				g.setColor(Color.green);
				if(!thinMirror)g.drawLine(lxc+(int)dw2,yy,lxc+(int)lr,lyc);//2-1
				else g.drawLine(xx,yy,lxc-(int)(2*lf),lyc);
				g.drawLine(lxc,lyc,x2,y2);//3-2
				g.setColor(Color.blue);
				g.drawLine(x1,y1,xx=lxc+(int)(dw),y1);//1-1 平行入射光
				if(!thinMirror)g.drawLine(lxc+(int)(dw+lf),y1+(int)(lf*Math.tan(angle)),xx,y1);
				else g.drawLine(lxc,(int)y1,lxc+(int)(2*lf),y1-2*oy);
				if(!thinMirror){
					g.setColor(Color.white);
					g.drawLine(lxc+(int)(2*dw-lr),2*y1-lyc,lxc-(int)(2*lf),lyc);//通過焦點
				}
				g.setColor(Color.green);
				if(!thinMirror){
					g.drawLine(xx,y1,lxc+(int)(dw-lf),y1-(int)(lf*Math.tan(angle)));
					g.setColor(Color.yellow);
				}
				g.drawLine(lxc,y1,lxc-(int)lf,lyc);
			}
		}
		if(oy*iy>0)g.setColor(Color.green);
		else g.setColor(Color.darkGray);
		drawit(g,x2,lyc,(int)iy); // draw image
	}
	String d2String(double d){
		float d2=(float)((int)(100.*d)/100.);
		String str=String.valueOf(d2);
		if(str.indexOf(".")==-1)str+=".0";
		return str;
	}

	private void drawit(Graphics g,int x,int y,int height){
		// for object and image 
		int x1,y1,width=2,sign=1;
		x1=x-width/2;
		if(height<0){ 
			sign=-1;
			y1=y;
		}else y1=y-height;
		g.fillRect(x1,y1,width,sign*height);
		y1=y-height;
		width=2*width;
		sign*=2;
		// draw hats
		g.drawLine(x,y1,x+width,y1+sign*width);
		g.drawLine(x,y1,x-width,y1+sign*width);
	}
	int lxc,lyc,lh;	// (xc,yc):center position, h:height of lens
	double lr,lf;		// focusLength*2;
	int lwidth;		// half/quater width of lens

	public void linit(int x, int y,int hi,double fi){
		lxc=x; lyc=y;
		lr=Math.abs(2.*fi);
		lf=fi;
		if(hi<10)lh=10; // minimum height of lens
		else if(hi>lr) lh=(int)lr-10;
			else lh=hi;
//		if(!is_lens && lf<0)type.setLabel("-");
//		else type.setLabel("+");
	}

	boolean moving=false;
	boolean sizing=false;
	boolean lmouseDown(int x,int y){
		if(Math.abs(x-lxc+ww)<lwidth){
			if(Math.abs(y-lyc)<lh/2.)moving=true; // move lens
				if((Math.abs(y-lyc-lh)<lh/2.) || (Math.abs(y-lyc+lh)<lh/2.))
					sizing=true; // change lens size/shape
				else sizing=false;
			return true;
		}else if(!is_lens && x>lxc && (x-lxc)<lwidth && Math.abs(y-lyc)<lwidth){
			return moving=true;
		}
		return false;		
	}

	boolean lmouseUp(int x,int y){
		sizing=false;// back to normal
		moving=false;
		return true;
	}

	boolean lmouseDrag(int x,int y){
		boolean done = true;
		if(sizing){ // change lens size/shape
			double dx,dy;
			double sign=1.;
			if(is_lens){
				dx=Math.abs(x-lxc);
			//	if(lf<0)sign=-1;
			}else dx=lxc-x;
			if(type.getLabel().compareTo("-")==0)sign=-1.;
			dy=Math.abs(y-lyc);
			linit(lxc,lyc,(int)dy,sign*Math.abs(dy*dy/dx+dx)/4.);
		}else if(moving)lmove(x,lyc); // move lens
		else done=false;
		return done;
	}

	void lmove(int x, int y){
		if(!is_lens && x<xc)return;
		lxc=x;
		lyc=y;
		show();
	}
 	int X[],Y[],cnt=0;
	int x0,y0,ww,hh;
	void lshow(){
		if(!sizing &&( is_lens || thinMirror) ) lr=(3.+lh*lh/3.)/4;
		//else if (!sizing && thinMirror)lr=(3.+lh*lh/3.)/4;
		else lr=Math.abs(2.*lf);
		// 3. is the half width of lens
		if(lr<lh)lh=(int)(0.95*lr);
		double angle=Math.asin((double)lh/lr);
		lwidth=(int)(lr*(1.-Math.cos(angle)));
		g.setColor(Color.red);
		if(is_lens && lf>0)if(sizing)g.drawOval(lxc-lwidth,lyc-lh,2*lwidth,2*lh);
		else g.fillOval(lxc-lwidth,lyc-lh,2*lwidth,2*lh);
		else{
			double x0,y0;
			double c=Math.PI-angle,dc;
			int ncnt=100;//(int)(lr*c/15.)+1;
			int ncnt2=2*ncnt;
			x0=lxc+lwidth/2+lr;
			y0=lyc;
			if(ncnt>cnt){
				X=new int[ncnt2];
				Y=new int[ncnt2];
				cnt=ncnt;
			}
			//g.drawString(String.valueOf(ncnt),100,100);
			ncnt2-=1;
			dc=2.*angle/(ncnt-1);
			if(is_lens){
				for(int i=0;i<ncnt;i++){
					X[i]=(int)(x0+lr*Math.cos(c));
					Y[ncnt2-i]=Y[i]=(int)(y0+lr*Math.sin(c));
					X[ncnt2-i]=(int)(2*lxc)-X[i];
					c+=dc;
				}
			}else{// mirror
				if(lf>0){// concave mirror
					ww=lwidth;
					x0=lxc-lr+1;
					for(int i=0;i<ncnt;i++){
						X[i]=(int)(x0-lr*Math.cos(c));
						Y[ncnt2-i]=Y[i]=(int)(y0+lr*Math.sin(c));
						X[ncnt2-i]=X[i]+lwidth;
						c+=dc;
					}
				}else{ //convex mirror
					ww=-lwidth;
					x0=lxc+lr;
					for(int i=0;i<ncnt;i++){
						X[i]=(int)(x0+lr*Math.cos(c));
						Y[ncnt2-i]=Y[i]=(int)(y0+lr*Math.sin(c));
						X[ncnt2-i]=X[i]+lwidth;
						c+=dc;
					}
				}
			}
			X[ncnt2]=X[0];
			ncnt2++;
			if(!is_lens){
				g.setColor(Color.black);
				if(thinMirror){if(sizing)g.drawPolygon(X,Y,ncnt2);
				else g.fillPolygon(X,Y,ncnt2);
				}else g.drawPolygon(X,Y,ncnt2);
			}else	if(sizing)g.drawPolygon(X,Y,ncnt2);
			else g.fillPolygon(X,Y,ncnt2);
		}
		g.setColor(Color.red);
		g.drawLine(lxc,lyc-5,lxc,lyc+5);
		// draw at +/- f, +/- 2f, 
		int delta=(int)lf,xa,ya=lyc-5,yb=lyc+5;
		g.drawLine(xa=lxc-delta,ya,xa,yb);
		delta=(int)(2.*lf);
		g.drawLine(xa=lxc-delta,ya,xa,yb);
		if(is_lens){
			g.drawLine(xa=lxc+delta,ya,xa,yb);
			delta=(int)(lf);
			g.drawLine(xa=lxc+delta,ya,xa,yb);
		}		
		if(is_lens){
			if(!sizing&&inrange){
				g.setColor(Color.black);
				//if(side>0.)ww=lwidth;//  draw ray path in lens approximately
				//else ww=-lwidth;
				ww=lwidth;
				g.drawLine(lxc-side*ww*3/4,lyc-oy,lxc+side*ww,lyc+(int)(oy*(ww-lf)/lf));
				g.drawLine(lxc-side*ww,lyc-(int)(iy*(-ww+lf)/lf),lxc+side*ww*3/4,lyc-(int)(iy));
			}
			ww=0;// for lens
		}
	}
}

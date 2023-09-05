import java.applet.*;
import java.awt.*;

public class colorBox extends Applet{
	final int total_boxs = 3;
	final int width = 100;
	boolean additive = true;
	boolean first = true;
	Graphics g = getGraphics();
	Coordinate coordinate[] = new Coordinate[total_boxs];
	int new_x, new_y;
	int select_box = 0;
	Rect r1, r2, r3, i12, i13, i23, i123;
	Color Yellow = new Color(255,255,0);
	Color Magenta = new Color(255,0,255);
	Color Cyan = new Color(0,255,255);
	Color Red;
	Color Green;
	Color Blue;
	Color Black = new Color(0,0,0);
	
	public void init(){
		g = getGraphics();
		init_coordinates();
		first = false;
		r1 = new Rect(coordinate[0].x, coordinate[0].y, coordinate[0].x+width, coordinate[0].y+width);
		r2 = new Rect(coordinate[1].x, coordinate[1].y, coordinate[1].x+width, coordinate[1].y+width);
		r3 = new Rect(coordinate[2].x, coordinate[2].y, coordinate[2].x+width, coordinate[2].y+width);
		i12 = r2.intersection(r1);       			      // Invoke Rect methods
		i13 = r3.intersection(r1);
		i23 = r2.intersection(r3);
		i123 = r1.intersection(i23);
	}
	
	void init_coordinates(){
		coordinate[0] = new Coordinate(10,10);
		coordinate[1] = new Coordinate(140,10);
		coordinate[2] = new Coordinate(270,10);
	}
	
	public boolean mouseDown(Event evt, int x, int y){
		for (int i = 0; i < total_boxs; i++){
			if (x > coordinate[i].x &&
			   x < coordinate[i].x + width &&
			   y > coordinate[i].y &&
			   y < coordinate[i].y + width){
				select_box = i + 1;
			}
		}
		return true;
	}
	
	public boolean mouseDrag(Event evt, int x, int y){
		if (select_box != 0){
			new_x = x - width/2;
			new_y = y - width/2;
			
			if (new_x < 0){new_x = 0;}
			if (new_y < 0){new_y = 0;}
			
			move_box(select_box - 1);
		}
		return true;
	}
	
	public boolean mouseUp(Event evt, int x, int y){
		select_box = 0;
		return true;
	}
	
	void move_box(int index){
		int old_x = coordinate[index].x;
		int old_y = coordinate[index].y;
		int dx = new_x - old_x;
		int dy = new_y - old_y;
		int end = Math.max(Math.abs(dx), Math.abs(dy));
		
		for (int i = 0; i < end; i++){
			int x = old_x + dx * i / end;
			int y = old_y + dy * i / end;
			
			Graphics g2;
			g2 = g.create();
			g2.clipRect(coordinate[index].x, coordinate[index].y, width, width);
			
			coordinate[index].x = x;
			coordinate[index].y = y;
			
			r1 = new Rect(coordinate[0].x, coordinate[0].y, coordinate[0].x+width, coordinate[0].y+width);
			r2 = new Rect(coordinate[1].x, coordinate[1].y, coordinate[1].x+width, coordinate[1].y+width);
			r3 = new Rect(coordinate[2].x, coordinate[2].y, coordinate[2].x+width, coordinate[2].y+width);
			i12 = r2.intersection(r1);       			      // Invoke Rect methods
			i13 = r3.intersection(r1);
			i23 = r2.intersection(r3);
			i123 = r1.intersection(i23);
			
			repaint();
		}
	}	
	
	public void paint(Graphics g){
		if (first){init(); first = false;}
		if (additive){
			setBackground(Color.black);
			g.setColor(Color.red);
			g.fillRect(r1.x1,r1.y1,r1.x2-r1.x1,r1.y2-r1.y1);
			g.setColor(Color.green);
			g.fillRect(r2.x1,r2.y1,r2.x2-r2.x1,r2.y2-r2.y1);
			g.setColor(Color.blue);
			g.fillRect(r3.x1,r3.y1,r3.x2-r3.x1,r3.y2-r3.y1);
			g.setColor(Color.yellow);
			g.fillRect(i12.x1,i12.y1,i12.x2-i12.x1,i12.y2-i12.y1);
			g.setColor(Color.magenta);
			g.fillRect(i13.x1,i13.y1,i13.x2-i13.x1,i13.y2-i13.y1);
			g.setColor(Color.cyan);
			g.fillRect(i23.x1,i23.y1,i23.x2-i23.x1,i23.y2-i23.y1);
			g.setColor(Color.white);
			g.fillRect(i123.x1,i123.y1,i123.x2-i123.x1,i123.y2-i123.y1);
		}
		else if(!additive){
			setBackground(Color.white);
			g.setColor(Yellow);
			g.fillRect(r1.x1,r1.y1,r1.x2-r1.x1,r1.y2-r1.y1);
			g.setColor(Cyan);
			g.fillRect(r2.x1,r2.y1,r2.x2-r2.x1,r2.y2-r2.y1);
			g.setColor(Magenta);
			g.fillRect(r3.x1,r3.y1,r3.x2-r3.x1,r3.y2-r3.y1);
			Green = new Color(Math.min(Yellow.getRed(), Cyan.getRed()), 255, Math.min(Yellow.getBlue(), Cyan.getBlue()));
			g.setColor(Green);
			g.fillRect(i12.x1,i12.y1,i12.x2-i12.x1,i12.y2-i12.y1);
			Red = new Color(255, Math.min(Yellow.getGreen(), Magenta.getGreen()), Math.min(Yellow.getBlue(), Magenta.getBlue()));
			g.setColor(Red);
			g.fillRect(i13.x1,i13.y1,i13.x2-i13.x1,i13.y2-i13.y1);
			Blue = new Color(Math.min(Magenta.getRed(), Cyan.getRed()), Math.min(Cyan.getGreen(), Magenta.getGreen()), 255);
			g.setColor(Blue);
			g.fillRect(i23.x1,i23.y1,i23.x2-i23.x1,i23.y2-i23.y1);
			
			Black = new Color(
			Math.min(Yellow.getRed(), Math.min(Magenta.getRed(), Cyan.getRed())), Math.min(Yellow.getGreen(), Math.min(Cyan.getGreen(), Magenta.getGreen())), Math.min(Cyan.getBlue(), Math.min(Yellow.getBlue(), Magenta.getBlue()))
			);
			
			g.setColor(Black);
			g.fillRect(i123.x1,i123.y1,i123.x2-i123.x1,i123.y2-i123.y1);
		}
	}	
}	
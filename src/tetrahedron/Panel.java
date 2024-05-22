package tetrahedron;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Panel {

	public static void main(String[] args) {
		
        int x[] = {100, 100, 100,  100, 100, 100,  -100,100,-100,  -100, 100, -100};
        int y[] = {-100, -100, 100,  100, -100, -100};
        
        List<Triangulo> tris = new ArrayList<>();
		
     // Creación de triángulos iniciales
        tris.add(new Triangulo( new Vertice(x[0], x[1], x[2]),
				new Vertice(y[0], y[1], y[2]),
				new Vertice(-100, 100, -100),
				Color.WHITE));

		tris.add(new Triangulo( new Vertice(x[0], x[1], x[2]),
				new Vertice(y[0], y[1], y[2]),
				new Vertice(100, -100, -100),
				Color.RED));
		
		tris.add(new Triangulo( new Vertice(x[6], x[7], x[8]),
				new Vertice(y[3], y[4], y[5]),
				new Vertice(100, 100, 100),
				Color.GREEN));
		
		tris.add(new Triangulo( new Vertice(x[6], x[7], x[8]),
				new Vertice(y[3], y[4], y[5]),
				new Vertice(-100, -100, 100),
				Color.BLUE));
		
//		tris.add(new Triangulo( new Vertice(100, 100, 100),
//				new Vertice(-100, -100, -100),
//				new Vertice(-100, -100, 100),
//				Color.BLUE));

        
		// Ventana
        JFrame frame = new JFrame();
        Container plane = frame.getContentPane();
        plane.setLayout(new BorderLayout());
       
        // Display panel
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.translate(getWidth() / 2, getHeight() / 2);

                // Matriz de transformacion para la rotacion en el eje y -> xz
                double heading = Math.toRadians(x[0]);
                Matrix3 headingTransform = new Matrix3(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                
                // Matriz de transformacion para la rotacion en el eje x -> yz
        		double pitch = Math.toRadians(y[0]);
                Matrix3 pitchTransform = new Matrix3(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                
                
        		Matrix3 transform = headingTransform.multiply(pitchTransform);
               
                g2.setColor(Color.WHITE);
                for (Triangulo t : tris) {
                    Vertice v1 = transform.transform(t.v1);
                    Vertice v2 = transform.transform(t.v2);
                    Vertice v3 = transform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }
                
            }
        };
        
        renderPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double yi = 180.0 / renderPanel.getHeight();
                double xi = 180.0 / renderPanel.getWidth();
                x[0] = (int) (e.getX() * xi);
                y[0] = -(int) (e.getY() * yi);
                renderPanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
        
        plane.add(renderPanel, BorderLayout.CENTER);
        
        frame.setSize(600, 600);
        frame.setVisible(true);
        

        
        
        //----------------------------------
	

       
        
        
	}

}

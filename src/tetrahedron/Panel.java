package tetrahedron;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Panel {

    private static BufferedImage img;
	
    static boolean sameSide(Vertice A, Vertice B, Vertice C, Vertice p){
    	Vertice V1V2 = new Vertice(B.x - A.x,B.y - A.y,B.z - A.z);
    	Vertice V1V3 = new Vertice(C.x - A.x,C.y - A.y,C.z - A.z);
    	Vertice V1P = new Vertice(p.x - A.x,p.y - A.y,p.z - A.z);

        // Si el producto cruzado del vector V1V2 y el vector V1V3 es el mismo que el del vector V1V2 y el vector V1p, están en el mismo lado.
        // Solo queda juzgar la direccion de z
        double V1V2CrossV1V3 = V1V2.x * V1V3.y - V1V3.x * V1V2.y;
        double V1V2CrossP = V1V2.x * V1P.y - V1P.x * V1V2.y;

        return V1V2CrossV1V3 * V1V2CrossP >= 0;
    }
	
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
		

        
		// Ventana
        JFrame frame = new JFrame();
        Container plane = frame.getContentPane();
        plane.setLayout(new BorderLayout());
       
        // Display panel
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
            	
//                if (img == null) {
//                    img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
//                }
                img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                //g2.translate(getWidth() / 2, getHeight() / 2);

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
                
                // z-buffer -> maneja la profundidad/distancia de los pixeles en pantalla
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
	             
	            for (int q = 0; q < zBuffer.length; q++) {
	                zBuffer[q] = Double.NEGATIVE_INFINITY;
	            }
               
               
               g2.setColor(Color.WHITE);                
               for (Triangulo t : tris) {
                	Vertice v1 = transform.transform(t.v1);
                	Vertice v2 = transform.transform(t.v2);
                	Vertice v3 = transform.transform(t.v3);

                    v1.x += getWidth() / 2.0;
                    v1.y += getHeight() / 2.0;
                    v2.x += getWidth() / 2.0;
                    v2.y += getHeight() / 2.0;
                    v3.x += getWidth() / 2.0;
                    v3.y += getHeight() / 2.0;
                    
//                    Path2D path = new Path2D.Double();
//                    path.moveTo(v1.x, v1.y);
//                    path.lineTo(v2.x, v2.y);
//                    path.lineTo(v3.x, v3.y);
//                    path.closePath();
//                    g2.draw(path);
                    
                    // Calcula el rango a ser procesado
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1,
                            Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                            Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    
                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                        	Vertice p = new Vertice(x,y,0);
                            
                            boolean V1 = sameSide(v1,v2,v3,p);
                            boolean V2 = sameSide(v2,v3,v1,p);
                            boolean V3 = sameSide(v3,v1,v2,p);
                            if (V3 && V2 && V1) 
                            {
                            	double depth = v1.z + v2.z + v3.z;
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) 
                                {
	                            	//System.out.println("Setting pixel at (" + x + ", " + y + ")");
	                                img.setRGB(x, y, t.color.getRGB());
	                                zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                g2.drawImage(img, 0, 0, null);
 
                
            }
        };
        
        renderPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double yi = 360.0 / renderPanel.getHeight();
                double xi = 360.0 / renderPanel.getWidth();
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
        
       
        
        
	}

}

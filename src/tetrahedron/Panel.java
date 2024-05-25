package tetrahedron;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class Panel {

    private static BufferedImage img;
    
    static List<Triangulo> tris = new ArrayList<>();
	
	public static void main(String[] args) {
		
        int x[] = {100, 100, 100,  100, 100, 100,  -100,100,-100,  -100, 100, -100};
        int y[] = {-100, -100, 100,  100, -100, -100};
        

		
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
		

        for (int i = 0; i < 6; i++) {
            tris = inflar(tris);
        }
        
		// Ventana
        JFrame frame = new JFrame();
        Container plane = frame.getContentPane();
        plane.setLayout(new BorderLayout());
        
        // Display panel
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
            	
                img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
               

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
                    
                    
                    // Se aplican las sombras
                    Vertice ab = new Vertice(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertice ac = new Vertice(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
                    // Normal vector
                    Vertice norm = new Vertice(
                         ab.y * ac.z - ab.z * ac.y,
                         ab.z * ac.x - ab.x * ac.z,
                         ab.x * ac.y - ab.y * ac.x
                    );
                    // Se normaliza el vector
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;
                    
                    // coseno entre el vector normal del triangulo y la direccion de la luz
                    double angleCos = Math.abs(norm.z);
                    
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
	                            	
	                                img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
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
	
	// Comprueba que el Verice P esté en el mismo lado que los otros 3 (que esté dentro del Triangulo)
    static boolean sameSide(Vertice A, Vertice B, Vertice C, Vertice p)
    {
    	Vertice V1V2 = new Vertice(B.x - A.x,B.y - A.y,B.z - A.z);
    	Vertice V1V3 = new Vertice(C.x - A.x,C.y - A.y,C.z - A.z);
    	Vertice V1P = new Vertice(p.x - A.x,p.y - A.y,p.z - A.z);

        // Si el producto cruzado del vector V1V2 y el vector V1V3 es el mismo que el del vector V1V2 y el vector V1p, están en el mismo lado.
        // Solo queda juzgar la direccion de z
        double V1V2CrossV1V3 = V1V2.x * V1V3.y - V1V3.x * V1V2.y;
        double V1V2CrossP = V1V2.x * V1P.y - V1P.x * V1V2.y;

        return V1V2CrossV1V3 * V1V2CrossP >= 0;
    }
    
    // reduce la intensidad del color en funcion a la distancia
    public static Color getShade(Color color, double shade) 
    {
    		
        double redLinear = Math.pow(color.getRed(), 2.2) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.2) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.2) * shade;

        int r = (int) Math.pow(redLinear, 1 / 2.2);
        int g = (int) Math.pow(greenLinear, 1 / 2.2);
        int b = (int) Math.pow(blueLinear, 1 / 2.2);

        return new Color(r, g, b);
 
//        int r = (int) (color.getRed() * shade);
//        int g = (int) (color.getGreen() * shade);
//        int b = (int) (color.getBlue() * shade);
//        return new Color(r, g, b);
    }
	
    // 
    public static List<Triangulo> inflar(List<Triangulo> tris) {
        List<Triangulo> innerTris = new ArrayList<>();
        
        for (Triangulo t : tris) 
        {
            Vertice m1 = new Vertice((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
            Vertice m2 = new Vertice((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
            Vertice m3 = new Vertice((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);
            innerTris.add(new Triangulo(t.v1, m1, m3, t.color));
            innerTris.add(new Triangulo(t.v2, m1, m2, t.color));
            innerTris.add(new Triangulo(t.v3, m2, m3, t.color));
            innerTris.add(new Triangulo(m1, m2, m3, t.color));
        }
        for (Triangulo t : innerTris) {
            for (Vertice v : new Vertice[] { t.v1, t.v2, t.v3 }) 
            {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return innerTris;
    }
}

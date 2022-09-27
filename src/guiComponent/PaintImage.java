package guiComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**在版面繪圖*/
public class PaintImage extends JComponent {
	private static final long serialVersionUID = 7103977782033673814L;
	private BufferedImage img;
    private int w, h, defaultW, defaultH;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dim = getPreferredSize();
        g.drawImage(img, 0, 0, dim.width, dim.height, this);
    }
    
    public PaintImage() {
    	addMouseListener(imageZoom);
    	addMouseWheelListener(imageZoom);
    }
    
    public void update(String fileName) throws IOException {
    	update(new File(fileName));
    }
    
    public void update(File file) throws IOException {
        update(ImageIO.read(file));
    }
    
    public void update(BufferedImage img) {
    	this.img = img;
    	w = defaultW = img.getWidth();
        h = defaultH = img.getHeight();
        setDefault();
	}
    
    private void setDefault() {
    	w = defaultW;
    	h = defaultH;
    	setPreferredSize(new Dimension(defaultW, defaultH));
    	revalidate();
        repaint();
    }

    private void setZoom(double scale) {
    	if(img == null) {
    		return;
    	}
		w = (int) (scale * 50 + w);
		w = (w < defaultW) ? defaultW : w;
		h = (int) (scale * 50 + h);
		h = (h < defaultH) ? defaultH : h;
        setPreferredSize(new Dimension(w, h));
        revalidate();
        repaint();
    }
    
    private MouseAdapter imageZoom = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				setDefault();	//重設圖片大小
			}
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {	//對著圖片區域使用滑鼠滾輪時
			setZoom(e.getWheelRotation());
		}	
	};
}
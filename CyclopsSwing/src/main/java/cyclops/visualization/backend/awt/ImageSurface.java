package cyclops.visualization.backend.awt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import cyclops.visualization.SaveableSurface;
import cyclops.visualization.Surface;
import cyclops.visualization.SurfaceType;




class ImageSurface extends AbstractGraphicsSurface implements SaveableSurface 
{

	private BufferedImage image;
	
	public ImageSurface(BufferedImage i)
	{
		super(i.createGraphics());
		image = i;
	}

	public void write(OutputStream out) throws IOException
	{
		graphics.finalize();
		ImageIO.write(image, "PNG", out);
		
		
	}

	public Surface getNewContextForSurface()
	{
		return new ImageSurface(image);
	}

	public boolean isVectorSurface() {
		return false;
	}

	@Override
	public SurfaceType getSurfaceType() {
		return SurfaceType.RASTER;
	}

}

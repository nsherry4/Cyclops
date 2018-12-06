package cyclops.visualization.drawing.map.painters;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import cyclops.GridPerspective;
import cyclops.ISpectrum;
import cyclops.Pair;
import cyclops.ReadOnlySpectrum;
import cyclops.Spectrum;
import cyclops.SpectrumCalculations;
import cyclops.visualization.Buffer;
import cyclops.visualization.drawing.DrawingRequest;
import cyclops.visualization.drawing.ViewTransform;
import cyclops.visualization.drawing.painters.PainterData;
import cyclops.visualization.palette.PaletteColour;
import cyclops.visualization.palette.palettes.SingleColourPalette;

/**
 * 
 * This class implements the drawing of a map using block pixel filling
 * 
 * @author Nathaniel Sherry, 2009
 */

public class RasterColorMapPainter extends MapPainter
{
	
	private List<PaletteColour> 	pixels;
	protected Buffer 				buffer;


	public RasterColorMapPainter()
	{
		super(new SingleColourPalette(new PaletteColour(0, 0, 0, 0)));
	}


	public void setPixels(List<PaletteColour> pixels)
	{
		this.pixels = pixels;
	}
	
	@Override
	public void drawMap(PainterData p, float cellSize, float rawCellSize)
	{

		p.context.save();

			List<PaletteColour> data = transformListDataForMap(p.dr, pixels);
	
			if (p.dr.drawToVectorSurface) {
				drawAsScalar(p, data, cellSize);
				buffer = null;
			} else {
				
				if (buffer == null) {
					buffer = drawAsRaster(p, data, cellSize, p.dr.dataHeight * p.dr.dataWidth);
				}
				p.context.compose(buffer, 0, 0, cellSize);
				
			}

		p.context.restore();

	}


	private Buffer drawAsRaster(PainterData p, final List<PaletteColour> data, float cellSize, final int maximumIndex)
	{

		final Buffer b = p.context.getImageBuffer(p.dr.dataWidth, p.dr.dataHeight);

		final PaletteColour transparent = new PaletteColour(0x00000000);
		
		IntStream.range(0, data.size()).parallel().forEach(ordinal -> {		
			if (maximumIndex > ordinal) {
				PaletteColour c = data.get(ordinal);
				if (c == null) c = transparent;
				b.setPixelValue(ordinal, c);
			}
		});

		p.context.compose(b, 0, 0, cellSize);
		
		return b;
	}


	private void drawAsScalar(PainterData p, List<PaletteColour> data, float cellSize)
	{

		p.context.save();

		p.context.rectAt(0, 0, p.plotSize.x, p.plotSize.y);
		p.context.clip();
		
		// draw the map
		for (int y = 0; y < p.dr.dataHeight; y++) {
			for (int x = 0; x < p.dr.dataWidth; x++) {



				int index = y * p.dr.dataWidth + x;
				p.context.rectAt(x * cellSize, y * cellSize, cellSize + 1, cellSize + 1);
				p.context.setSource(data.get(index));
				p.context.fill();

				
			}
		}
		
		p.context.restore();
	}

	
	protected <T> List<T> transformListDataForMap(DrawingRequest dr, List<T> list)
	{
		List<T> flip = new ArrayList<>(list);
		
		//vertical orientation flip
		if (!dr.screenOrientation) {
			/*
			 * the screenOrientation setting puts the origin (0,0) in the top left rather
			 * than in the bottom left. BUT, having the origin in the top left is the
			 * cyclops-native format. So when flipY is set, we don't have to do anything.
			 * It's when it's not set that we have to flip it...
			 */
			
			//We have to accommodate interpolated and uninterpolated data here
			GridPerspective<Float> grid;
			int height = 0;
			if (list.size() == dr.dataHeight * dr.dataWidth) {
				grid = new GridPerspective<>(dr.dataWidth, dr.dataHeight, 0f);
				height = dr.dataHeight;
			} else if (list.size() == dr.uninterpolatedWidth * dr.uninterpolatedHeight) {
				grid = new GridPerspective<>(dr.uninterpolatedWidth, dr.uninterpolatedHeight, 0f);
				height = dr.uninterpolatedHeight;
			} else {
				throw new IllegalArgumentException("List has wrong dimensions");
			}
			
			for (int i = 0; i < flip.size(); i++) {
				Pair<Integer, Integer> xy = grid.getXYFromIndex(i);
				int x = xy.first;
				int y = xy.second;
				y = (height-1) - y;
				int index = grid.getIndexFromXY(x, y);
				flip.set(index, list.get(i));
			}
		}		
		
		return flip;
	}
	
	
	
	public void clearBuffer()
	{
		buffer = null;
	}


	@Override
	public boolean isBufferingPainter()
	{
		return true;
	}
	
}

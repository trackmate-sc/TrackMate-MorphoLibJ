/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.morpholibj;

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.detection.DetectionUtils;
import fiji.plugin.trackmate.detection.LabelImageDetector;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.watershed.Watershed;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class MorphoLibJDetector< T extends RealType< T > & NativeType< T > > implements SpotDetector< T >
{
	private final static String BASE_ERROR_MESSAGE = "MorphoLibJDetector: ";

	protected final Interval interval;

	protected List< Spot > spots = new ArrayList<>();

	protected String baseErrorMessage;

	protected String errorMessage;

	protected long processingTime;

	private final ImgPlus< T > img;

	private final double tolerance;

	private final Connectivity connectivity;

	private final boolean simplify;

	public MorphoLibJDetector(
			final ImgPlus< T > img,
			final Interval interval,
			final double tolerance,
			final Connectivity connectivity,
			final boolean simplify )
	{
		this.img = img;
		this.tolerance = tolerance;
		this.connectivity = connectivity;
		this.interval = DetectionUtils.squeeze( interval );
		this.simplify = simplify;
		this.baseErrorMessage = BASE_ERROR_MESSAGE;
	}

	@Override
	public boolean checkInput()
	{
		if ( null == img )
		{
			errorMessage = baseErrorMessage + "Image is null.";
			return false;
		}
		return true;
	}

	@Override
	public boolean process()
	{
		final boolean dams = false; // Do we care about dams?
		final long start = System.currentTimeMillis();

		/*
		 * We have to duplicate the image because MorphoLibJ does not like
		 * virtual stacks.
		 */
		final ImagePlus tmp = ImageJFunctions.wrap( img, "tmpwrapped" );
		final ImagePlus tp = new Duplicator().run( tmp );

		final int conn = connectivity.getConnectivity();
		final ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( tp.getImageStack(), tolerance, conn );
		final ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( tp.getImageStack(), regionalMinima, conn );
		final ImageStack labeledMinima = BinaryImages.componentsLabeling( regionalMinima, conn, 32 );
		final ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, conn, dams );

		final ImagePlus resultImage = new ImagePlus( "watershed", resultStack );
		final Img< T > labelImage = ImageJFunctions.wrap( resultImage );
		final double[] calibration = TMUtils.getSpatialCalibration( img );

		final LabelImageDetector< T > lbldetector = new LabelImageDetector<>( labelImage, interval, calibration, simplify );
		if ( !lbldetector.checkInput() || !lbldetector.process() )
		{
			errorMessage = baseErrorMessage + lbldetector.getErrorMessage();
			return false;
		}
		spots = lbldetector.getResult();

		final long end = System.currentTimeMillis();
		this.processingTime = end - start;
		return true;
	}

	@Override
	public List< Spot > getResult()
	{
		return spots;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}
}

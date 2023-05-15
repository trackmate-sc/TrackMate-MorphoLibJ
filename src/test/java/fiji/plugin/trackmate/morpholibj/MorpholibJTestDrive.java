/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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

import java.util.List;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.LabelImageDetector;
import fiji.plugin.trackmate.features.FeatureUtils;
import fiji.plugin.trackmate.features.spot.Spot2DShapeAnalyzerFactory;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.IJ;
import ij.ImageJ;
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

public class MorpholibJTestDrive
{

	public static < T extends RealType< T > & NativeType< T > > void main( final String[] args )
	{
		final String targetImagePath = "samples/Cont1-1.tif";

		ImageJ.main( args );
		final ImagePlus imp = IJ.openImage( targetImagePath );
		imp.show();

		@SuppressWarnings( "unchecked" )
		final ImgPlus< T > allChannels = TMUtils.rawWraps( imp );
		final long targetChannel = 0;
		final long targetFrame = 0;
		final ImgPlus< T > input = TMUtils.hyperSlice( allChannels, targetChannel, targetFrame );

		final Interval interval = input;
		// Intervals.createMinSize( 464, 82, 325, 233 );
		
		// Params.
		final double tolerance = 30;
		final int conn = 26; // 6 or 26
		final boolean dams = false;
		final boolean simplify = false;

		/*
		 * Execute segmentation.
		 */

		final long start0 = System.currentTimeMillis();

		final long end0 = System.currentTimeMillis();
		System.out.println( String.format( "First run took %.2f seconds to run.", ( end0 - start0 ) / 1000. ) );

		/*
		 * We have to duplicate the image because MorphoLibJ does not like
		 * virtual stacks.
		 */
		final ImagePlus tmp = ImageJFunctions.wrap( input, "frame=" + targetFrame + "_channel=" + targetChannel );
		final ImagePlus tp = new Duplicator().run( tmp );
		
		final ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( tp.getImageStack(), tolerance, conn );
		final ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( tp.getImageStack(), regionalMinima, conn );
		final ImageStack labeledMinima = BinaryImages.componentsLabeling( regionalMinima, conn, 32 );
		final ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, conn, dams );

		final ImagePlus resultImage = new ImagePlus( "watershed", resultStack );
		final Img< T > labelImage = ImageJFunctions.wrap( resultImage );
		final double[] calibration = TMUtils.getSpatialCalibration( allChannels );

		final LabelImageDetector< T > lbldetector = new LabelImageDetector<>( labelImage, interval, calibration, simplify );
		if ( !lbldetector.checkInput() || !lbldetector.process() )
		{
			System.err.println( lbldetector.getErrorMessage() );
			return;
		}
		final List< Spot > spots0 = lbldetector.getResult();
		
		/*
		 * Display results.
		 */

		final SpotCollection spots = new SpotCollection();
		spots.put( 0, spots0 );
		spots.setVisible( true );
		System.out.println( spots.toString() );

		final Model model = new Model();
		model.setSpots( spots, false );

		final Settings settings = new Settings( imp );
		settings.addSpotAnalyzerFactory( new Spot2DShapeAnalyzerFactory<>() );

		final TrackMate trackmate = new TrackMate( model, settings );
		trackmate.computeSpotFeatures( false );

		final DisplaySettings ds = DisplaySettingsIO.readUserDefault();
		final double[] autoMinMax = FeatureUtils.autoMinMax( model, TrackMateObject.SPOTS, Spot2DShapeAnalyzerFactory.AREA );
		ds.setSpotColorBy( TrackMateObject.SPOTS, Spot2DShapeAnalyzerFactory.AREA );
		ds.setSpotMinMax( autoMinMax[ 0 ], autoMinMax[ 1 ] );
		final HyperStackDisplayer displayer = new HyperStackDisplayer( model, new SelectionModel( model ), imp, ds );
		displayer.render();
	}
}

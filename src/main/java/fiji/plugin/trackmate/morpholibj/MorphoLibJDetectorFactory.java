/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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

import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotDetectorFactory.class, priority = Priority.LOW - 5. )
public class MorphoLibJDetectorFactory< T extends RealType< T > & NativeType< T > > implements SpotDetectorFactory< T >
{
	/**
	 * The key to the parameter that stores the tolerance value to use in
	 * morphological segmentation. Accepted values are doubles.
	 */
	public static final String KEY_TOLERANCE = "TOLERANCE";

	public static final Double DEFAULT_TOLERANCE = Double.valueOf( 30. );

	/**
	 * The key to the parameter that stores the connectivity to use in
	 * morphological segmentation. Accepted values are integers, 6 (for straight
	 * connectivity) or 26 (for diagonal connectivity).
	 */
	public static final String KEY_CONNECTIVITY = "CONNECTIVITY";

	public static final Integer DEFAULT_CONNECTIVITY = Connectivity.DIAGONAL.getConnectivity();

	/** A string key identifying this factory. */
	public static final String DETECTOR_KEY = "MORPHOLIBJ_DETECTOR";

	/** The pretty name of the target detector. */
	public static final String NAME = "MorphoLibJ detector";

	/** An html information text. */
	public static final String INFO_TEXT = "<html>"
			+ "This detector relies on the 'Morphological segmentation' plugin in the MorphoLibJ library "
			+ "to detect objects. For it to work, you must have the 'IJPB-plugins' update site activated "
			+ "in Fiji. "
			+ "<p>"
			+ "It segment objects that are delineated by their contour, such as cells stained "
			+ "for their membrane. "
			+ "It works for 2D and 3D images, but return contours only for 2D images."
			+ "<p>"
			+ "If you use this detector for your work, please "
			+ "also cite the MorphoLibJ paper: <a href=\"https://doi.org/10.1093/bioinformatics/btw413\">Legland, D.; Arganda-Carreras, I. & Andrey, P. (2016), "
			+ "'MorphoLibJ: integrated library and plugins for mathematical morphology with ImageJ', "
			+ "Bioinformatics (Oxford Univ Press) 32(22): 3532-3534.</a> "
			+ "</html>";

	public static final String DOC_URL = "https://imagej.net/plugins/trackmate/trackmate-morpholibj";

	public static final ImageIcon ICON = new ImageIcon( GuiUtils.getResource( "images/TrackMateMorphoLibJ-logo-64px.png", MorphoLibJDetectorFactory.class ) );

	@Override
	public SpotDetector< T > getDetector( final ImgPlus< T > img, final Map< String, Object > settings, final Interval interval, final int frame )
	{
		final int channel = ( Integer ) settings.get( KEY_TARGET_CHANNEL ) - 1;
		final ImgPlus< T > input = TMUtils.hyperSlice( img, channel, frame );

		final double tolerance = ( double ) settings.get( KEY_TOLERANCE );
		final int conn = ( Integer ) settings.get( KEY_CONNECTIVITY );
		final boolean simplify = ( boolean ) settings.get( KEY_SIMPLIFY_CONTOURS );
		final MorphoLibJDetector< T > detector = new MorphoLibJDetector<>(
				input,
				interval,
				tolerance,
				Connectivity.valueFor( conn ),
				simplify );
		return detector;
	}

	@Override
	public ConfigurationPanel getDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		return new MorphoLibJDetectorConfigurationPanel( settings, model );
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > settings = new HashMap<>();
		settings.put( KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL );
		settings.put( KEY_TOLERANCE, DEFAULT_TOLERANCE );
		settings.put( KEY_CONNECTIVITY, DEFAULT_CONNECTIVITY );
		settings.put( KEY_SIMPLIFY_CONTOURS, Boolean.FALSE );
		return settings;
	}

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return ICON;
	}

	@Override
	public String getUrl()
	{
		return DOC_URL;
	}

	@Override
	public String getKey()
	{
		return DETECTOR_KEY;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean has2Dsegmentation()
	{
		return true;
	}
}

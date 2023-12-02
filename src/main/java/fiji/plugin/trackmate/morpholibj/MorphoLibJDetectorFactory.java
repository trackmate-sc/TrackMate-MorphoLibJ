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

import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SMOOTHING_SCALE;
import static fiji.plugin.trackmate.io.IOUtils.readBooleanAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readIntegerAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeTargetChannel;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkOptionalParameter;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetector;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotDetectorFactory.class )
public class MorphoLibJDetectorFactory< T extends RealType< T > & NativeType< T > > implements SpotDetectorFactory< T >
{

	/*
	 * CONSTANTS
	 */

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

	/**
	 * The key to the parameter that determines whether the largest object found
	 * by the morphological segmentation will be removed. This is particularly
	 * useful when the input image contains a large background which
	 * segmentation is not useful.
	 */
	public static final String KEY_REMOVE_LARGEST_OBJECT = "REMOVE_LARGEST_OBJECT";

	public static final Boolean DEFAULT_REMOVE_LARGEST_OBJECT = Boolean.FALSE;

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
			+ "It works for 2D and 3D images."
			+ "<p>"
			+ "If you use this detector for your work, please "
			+ "also cite the MorphoLibJ paper: <a href=\"https://doi.org/10.1093/bioinformatics/btw413\">Legland, D.; Arganda-Carreras, I. & Andrey, P. (2016), "
			+ "'MorphoLibJ: integrated library and plugins for mathematical morphology with ImageJ', "
			+ "Bioinformatics (Oxford Univ Press) 32(22): 3532-3534.</a> "
			+ "<p>"
			+ "Documentation for this module "
			+ "<a href=\"https://imagej.net/plugins/trackmate/trackmate-morpholibj\">on the ImageJ Wiki</a>."
			+ "<p>"
			+ "</html>";

	/*
	 * FIELDS
	 */

	/** The image to operate on. Multiple frames, single channel. */
	protected ImgPlus< T > img;

	protected Map< String, Object > settings;

	protected String errorMessage;

	/*
	 * METHODS
	 */

	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		final int channel = ( Integer ) settings.get( KEY_TARGET_CHANNEL ) - 1;
		final ImgPlus< T > input = TMUtils.hyperSlice( img, channel, frame );

		final double tolerance = ( double ) settings.get( KEY_TOLERANCE );
		final int conn = ( Integer ) settings.get( KEY_CONNECTIVITY );
		final boolean remogeLargestObject = ( boolean ) settings.get( KEY_REMOVE_LARGEST_OBJECT );
		final boolean simplify = ( boolean ) settings.get( KEY_SIMPLIFY_CONTOURS );
		final Object smoothingObj = settings.get( KEY_SMOOTHING_SCALE );
		final double smoothingScale = ( smoothingObj == null )
				? -1.
				: ( ( Number ) smoothingObj ).doubleValue();

		final MorphoLibJDetector< T > detector = new MorphoLibJDetector<>(
				input,
				interval,
				tolerance,
				Connectivity.valueFor( conn ),
				remogeLargestObject,
				simplify,
				smoothingScale );
		return detector;
	}

	@Override
	public boolean setTarget( final ImgPlus< T > img, final Map< String, Object > settings )
	{
		this.img = img;
		this.settings = settings;
		return checkSettings( settings );
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = writeTargetChannel( settings, element, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_TOLERANCE, Double.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_CONNECTIVITY, Integer.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_REMOVE_LARGEST_OBJECT, Boolean.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_SIMPLIFY_CONTOURS, Boolean.class, errorHolder );
		ok = ok && writeAttribute( settings, element, KEY_SMOOTHING_SCALE, Double.class, errorHolder );

		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		settings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true;
		ok = ok && readIntegerAttribute( element, settings, KEY_TARGET_CHANNEL, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_TOLERANCE, errorHolder );
		ok = ok && readIntegerAttribute( element, settings, KEY_CONNECTIVITY, errorHolder );
		ok = ok && readBooleanAttribute( element, settings, KEY_REMOVE_LARGEST_OBJECT, errorHolder );
		ok = ok && readBooleanAttribute( element, settings, KEY_SIMPLIFY_CONTOURS, errorHolder );
		ok = ok && readDoubleAttribute( element, settings, KEY_SMOOTHING_SCALE, errorHolder );

		if ( !ok )
		{
			errorMessage = errorHolder.toString();
			return false;
		}
		return checkSettings( settings );
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
		settings.put( KEY_REMOVE_LARGEST_OBJECT, DEFAULT_REMOVE_LARGEST_OBJECT );
		settings.put( KEY_SIMPLIFY_CONTOURS, Boolean.FALSE );
		settings.put( KEY_SMOOTHING_SCALE, -1. );
		return settings;
	}

	@Override
	public boolean checkSettings( final Map< String, Object > settings )
	{
		boolean ok = true;
		final StringBuilder errorHolder = new StringBuilder();
		ok = ok & checkParameter( settings, KEY_TARGET_CHANNEL, Integer.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_TOLERANCE, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_CONNECTIVITY, Integer.class, errorHolder );
		ok = ok & checkOptionalParameter( settings, KEY_REMOVE_LARGEST_OBJECT, Boolean.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_SIMPLIFY_CONTOURS, Boolean.class, errorHolder );
		ok = ok & checkOptionalParameter( settings, KEY_SMOOTHING_SCALE, Double.class, errorHolder );
		final List< String > mandatoryKeys = new ArrayList<>();
		mandatoryKeys.add( KEY_TARGET_CHANNEL );
		mandatoryKeys.add( KEY_TOLERANCE );
		mandatoryKeys.add( KEY_CONNECTIVITY );
		mandatoryKeys.add( KEY_SIMPLIFY_CONTOURS );
		final List< String > optionalKeys = Arrays.asList( KEY_SMOOTHING_SCALE, KEY_REMOVE_LARGEST_OBJECT );
		ok = ok & checkMapKeys( settings, mandatoryKeys, optionalKeys, errorHolder );
		if ( !ok )
			errorMessage = errorHolder.toString();

		return ok;
	}

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
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

	@Override
	public boolean has3Dsegmentation()
	{
		return true;
	}

	@Override
	public MorphoLibJDetectorFactory< T > copy()
	{
		return new MorphoLibJDetectorFactory<>();
	}
}

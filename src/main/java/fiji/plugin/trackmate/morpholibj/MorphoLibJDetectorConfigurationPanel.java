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

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS;
import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.PREVIEW_ICON;
import static fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory.KEY_CONNECTIVITY;
import static fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory.KEY_TOLERANCE;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import org.scijava.prefs.PrefService;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.DetectionUtils;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.JLabelLogger;
import fiji.plugin.trackmate.util.TMUtils;

public class MorphoLibJDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final NumberFormat THRESHOLD_FORMAT = new DecimalFormat( "#.##" );

	protected static final ImageIcon ICON = new ImageIcon( getResource( "images/TrackMateMorphoLibJ-logo-100px.png" ) );

	private static final String TITLE = MorphoLibJDetectorFactory.NAME;

	private final JSlider sliderChannel;

	private final JFormattedTextField ftfTolerance;

	protected final PrefService prefService;

	private final JCheckBox chkboxSimplify;

	private final JComboBox< Connectivity > cmbboxConnectivity;

	public MorphoLibJDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		this.prefService = TMUtils.getContext().getService( PrefService.class );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 144, 0, 32 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 84, 0, 27, 0, 0, 0, 0, 37, 23 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblSettingsForDetector = new JLabel( "Settings for detector:" );
		lblSettingsForDetector.setFont( FONT );
		final GridBagConstraints gbcLblSettingsForDetector = new GridBagConstraints();
		gbcLblSettingsForDetector.gridwidth = 3;
		gbcLblSettingsForDetector.insets = new Insets( 5, 5, 5, 0 );
		gbcLblSettingsForDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSettingsForDetector.gridx = 0;
		gbcLblSettingsForDetector.gridy = 0;
		add( lblSettingsForDetector, gbcLblSettingsForDetector );

		final JLabel lblDetector = new JLabel( TITLE, ICON, JLabel.RIGHT );
		lblDetector.setFont( BIG_FONT );
		lblDetector.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblDetector = new GridBagConstraints();
		gbcLblDetector.gridwidth = 3;
		gbcLblDetector.insets = new Insets( 5, 5, 5, 0 );
		gbcLblDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDetector.gridx = 0;
		gbcLblDetector.gridy = 1;
		add( lblDetector, gbcLblDetector );

		/*
		 * Help text.
		 */
		final JLabel lblHelptext = new JLabel( MorphoLibJDetectorFactory.INFO_TEXT
				.replace( "<br>", "" )
				.replace( "<p>", "<p align=\"justify\">" )
				.replace( "<html>", "<html><p align=\"justify\">" ) );
		lblHelptext.setFont( FONT.deriveFont( Font.ITALIC ) );
		final GridBagConstraints gbcLblHelptext = new GridBagConstraints();
		gbcLblHelptext.anchor = GridBagConstraints.NORTH;
		gbcLblHelptext.fill = GridBagConstraints.HORIZONTAL;
		gbcLblHelptext.gridwidth = 3;
		gbcLblHelptext.insets = new Insets( 5, 10, 5, 10 );
		gbcLblHelptext.gridx = 0;
		gbcLblHelptext.gridy = 2;
		add( lblHelptext, gbcLblHelptext );

		/*
		 * Channel selector.
		 */

		final JLabel lblSegmentInChannel = new JLabel( "Segment in channel:" );
		lblSegmentInChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentInChannel = new GridBagConstraints();
		gbcLblSegmentInChannel.anchor = GridBagConstraints.EAST;
		gbcLblSegmentInChannel.insets = new Insets( 5, 5, 5, 5 );
		gbcLblSegmentInChannel.gridx = 0;
		gbcLblSegmentInChannel.gridy = 3;
		add( lblSegmentInChannel, gbcLblSegmentInChannel );

		sliderChannel = new JSlider();
		final GridBagConstraints gbcSliderChannel = new GridBagConstraints();
		gbcSliderChannel.fill = GridBagConstraints.HORIZONTAL;
		gbcSliderChannel.insets = new Insets( 5, 5, 5, 5 );
		gbcSliderChannel.gridx = 1;
		gbcSliderChannel.gridy = 3;
		add( sliderChannel, gbcSliderChannel );

		final JLabel labelChannel = new JLabel( "1" );
		labelChannel.setHorizontalAlignment( SwingConstants.CENTER );
		labelChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLabelChannel = new GridBagConstraints();
		gbcLabelChannel.insets = new Insets( 5, 5, 5, 0 );
		gbcLabelChannel.gridx = 2;
		gbcLabelChannel.gridy = 3;
		add( labelChannel, gbcLabelChannel );

		sliderChannel.addChangeListener( l -> labelChannel.setText( "" + sliderChannel.getValue() ) );

		/*
		 * Tolerance
		 */

		final JLabel lblTolerance = new JLabel( "Tolerance:" );
		lblTolerance.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblTolerance = new GridBagConstraints();
		gbcLblTolerance.anchor = GridBagConstraints.EAST;
		gbcLblTolerance.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTolerance.gridx = 0;
		gbcLblTolerance.gridy = 4;
		add( lblTolerance, gbcLblTolerance );

		ftfTolerance = new JFormattedTextField( THRESHOLD_FORMAT );
		ftfTolerance.setFont( SMALL_FONT );
		ftfTolerance.setMinimumSize( new Dimension( 60, 20 ) );
		ftfTolerance.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcTolerance = new GridBagConstraints();
		gbcTolerance.gridwidth = 2;
		gbcTolerance.fill = GridBagConstraints.HORIZONTAL;
		gbcTolerance.insets = new Insets( 5, 5, 5, 5 );
		gbcTolerance.gridx = 1;
		gbcTolerance.gridy = 4;
		add( ftfTolerance, gbcTolerance );

		/*
		 * Connectivity.
		 */

		final JLabel lblConnectivity = new JLabel( "Connectivity:" );
		lblConnectivity.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbcLblConnectivity = new GridBagConstraints();
		gbcLblConnectivity.anchor = GridBagConstraints.EAST;
		gbcLblConnectivity.insets = new Insets( 0, 0, 5, 5 );
		gbcLblConnectivity.gridx = 0;
		gbcLblConnectivity.gridy = 5;
		add( lblConnectivity, gbcLblConnectivity );

		this.cmbboxConnectivity = new JComboBox<>( new Vector<>( Arrays.asList( Connectivity.values() ) ) );
		( ( JLabel ) cmbboxConnectivity.getRenderer() ).setHorizontalAlignment( SwingConstants.CENTER );
		cmbboxConnectivity.setFont( SMALL_FONT );
		final GridBagConstraints gbcCmbboxConnectivity = new GridBagConstraints();
		gbcCmbboxConnectivity.gridwidth = 2;
		gbcCmbboxConnectivity.insets = new Insets( 0, 0, 5, 5 );
		gbcCmbboxConnectivity.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxConnectivity.gridx = 1;
		gbcCmbboxConnectivity.gridy = 5;
		add( cmbboxConnectivity, gbcCmbboxConnectivity );

		/*
		 * Simplify.
		 */

		final JLabel lblSimplify = new JLabel( "Simplify contours:" );
		lblSimplify.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSimplify = new GridBagConstraints();
		gbcLblSimplify.anchor = GridBagConstraints.EAST;
		gbcLblSimplify.insets = new Insets( 0, 0, 5, 5 );
		gbcLblSimplify.gridx = 0;
		gbcLblSimplify.gridy = 6;
		add( lblSimplify, gbcLblSimplify );

		this.chkboxSimplify = new JCheckBox();
		final GridBagConstraints gbcChkboxSimplify = new GridBagConstraints();
		gbcChkboxSimplify.anchor = GridBagConstraints.WEST;
		gbcChkboxSimplify.insets = new Insets( 0, 0, 5, 5 );
		gbcChkboxSimplify.gridx = 1;
		gbcChkboxSimplify.gridy = 6;
		add( chkboxSimplify, gbcChkboxSimplify );

		/*
		 * Logger.
		 */

		final JLabelLogger labelLogger = new JLabelLogger();
		final GridBagConstraints gbcLabelLogger = new GridBagConstraints();
		gbcLabelLogger.gridwidth = 3;
		gbcLabelLogger.gridx = 0;
		gbcLabelLogger.gridy = 10;
		add( labelLogger, gbcLabelLogger );

		/*
		 * Preview.
		 */

		final JButton btnPreview = new JButton( "Preview", PREVIEW_ICON );
		btnPreview.setFont( FONT );
		final GridBagConstraints gbcBtnPreview = new GridBagConstraints();
		gbcBtnPreview.gridwidth = 1;
		gbcBtnPreview.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnPreview.insets = new Insets( 5, 5, 5, 0 );
		gbcBtnPreview.gridx = 2;
		gbcBtnPreview.gridy = 9;
		add( btnPreview, gbcBtnPreview );

		/*
		 * Listeners and specificities.
		 */

		btnPreview.addActionListener( e -> DetectionUtils.preview(
				model,
				settings,
				getDetectorFactory(),
				getSettings(),
				settings.imp.getFrame() - 1,
				labelLogger.getLogger(),
				b -> btnPreview.setEnabled( b ) ) );

		/*
		 * Deal with channels: the slider and channel labels are only visible if
		 * we find more than one channel.
		 */
		if ( null != settings.imp )
		{
			final int n_channels = settings.imp.getNChannels();
			sliderChannel.setMaximum( n_channels );
			sliderChannel.setMinimum( 1 );
			sliderChannel.setValue( settings.imp.getChannel() );

			if ( n_channels <= 1 )
			{
				labelChannel.setVisible( false );
				lblSegmentInChannel.setVisible( false );
				sliderChannel.setVisible( false );
			}
			else
			{
				labelChannel.setVisible( true );
				lblSegmentInChannel.setVisible( true );
				sliderChannel.setVisible( true );
			}
		}
	}

	private SpotDetectorFactoryBase< ? > getDetectorFactory()
	{
		return new MorphoLibJDetectorFactory<>();
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final HashMap< String, Object > settings = new HashMap<>( 4 );

		final int targetChannel = sliderChannel.getValue();
		settings.put( KEY_TARGET_CHANNEL, targetChannel );

		final double tolerance = ( ( Number ) ftfTolerance.getValue() ).doubleValue();
		settings.put( KEY_TOLERANCE, tolerance );

		final Connectivity connectivity = ( Connectivity ) cmbboxConnectivity.getSelectedItem();
		settings.put( KEY_CONNECTIVITY, connectivity.getConnectivity() );

		final boolean simplify = chkboxSimplify.isSelected();
		settings.put( KEY_SIMPLIFY_CONTOURS, simplify );

		return settings;
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		sliderChannel.setValue( ( Integer ) settings.get( KEY_TARGET_CHANNEL ) );
		ftfTolerance.setValue( settings.get( KEY_TOLERANCE ) );
		cmbboxConnectivity.setSelectedItem( Connectivity.valueFor( ( int ) settings.get( KEY_CONNECTIVITY ) ) );
		chkboxSimplify.setSelected( ( boolean ) settings.get( KEY_SIMPLIFY_CONTOURS ) );
	}

	@Override
	public void clean()
	{}

	protected static URL getResource( final String name )
	{
		return MorphoLibJDetectorFactory.class.getClassLoader().getResource( name );
	}
}

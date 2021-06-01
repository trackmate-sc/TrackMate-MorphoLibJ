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

public enum Connectivity
{
	STRAIGHT( 6, "straight" ),
	DIAGONAL( 26, "diagonal" );

	private final int connectivity3d;

	private final String name;

	Connectivity( final int connectivity3D, final String name )
	{
		connectivity3d = connectivity3D;
		this.name = name;
	}

	public int getConnectivity()
	{
		return connectivity3d;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public static Connectivity valueFor( final int conn )
	{
		if ( conn == 6 )
			return STRAIGHT;
		else
			return DIAGONAL;
	}
}

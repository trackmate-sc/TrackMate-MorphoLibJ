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

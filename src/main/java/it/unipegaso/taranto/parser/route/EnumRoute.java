package it.unipegaso.taranto.parser.route;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public enum EnumRoute {
	STRING(new RouteString()),
	IMAGE(new RouteImage()),
	PDF(new RoutePDF()),
	HTML(new RouteHTML());
	
	private IRoute route;
	
	EnumRoute(IRoute route) {
		this.route = route;
	}
	
	public static void unloadAll() {
		for (EnumRoute r : values()) {
			r.unload();
		}
	}
	
	public String parse(Object obj) throws Exception {
		System.out.println("Parsing through " + name());
		return route.parse(obj);
	}
	
	private void unload() {
		route.unload();
	}
}

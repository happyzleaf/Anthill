package it.unipegaso.taranto.parser.route;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public interface IRoute<T> {
	String parse(T obj) throws Exception;
	
	default void unload() {}
}

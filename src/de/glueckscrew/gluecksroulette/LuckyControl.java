package de.glueckscrew.gluecksroulette;

/**
 * Diese Klasse stellt die zentrale Verwaltungskomponente des Projekts da
 * Zudem beinhaltet sie die Main-Funktion
 *
 * @author Sebastian Schmitt
 */

public class LuckyControl {

	/**
	 * private to make sure we get only called from {@link #main(String[]))
	 */
	private LuckyControl() {

	}

	public static void main(String[] args) {
		new LuckyControl();
	}
}

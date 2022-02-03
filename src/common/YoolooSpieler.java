// History of Change
// vernr    |date  | who | lineno | what
//  V0.106  |200107| cic |    -   | add history of change

package common;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import common.YoolooKartenspiel.Kartenfarbe;
import utils.YoolooLogger;

public class YoolooSpieler implements Serializable {

	private static final long serialVersionUID = 376078630788146549L;
	private String name;
	private Kartenfarbe spielfarbe;
	private int clientHandlerId = -1;
	private int punkte;
	private YoolooKarte[] aktuelleSortierung;
	ArrayList<YoolooSpieler> spielerliste = new ArrayList<YoolooSpieler>();
	private int highscore;

	public YoolooSpieler(String name, int maxKartenWert) {
		this.name = name;
		this.punkte = 0;
		this.spielfarbe = null;
		this.aktuelleSortierung = new YoolooKarte[maxKartenWert];
	}
	public void sortierungCheat() {
		YoolooKarte[] neueSortierung = new YoolooKarte[this.aktuelleSortierung.length];
		for (int i = 0; i < neueSortierung.length; i++) {
			neueSortierung[i] = new YoolooKarte(Kartenfarbe.Blau, i + 1);
		}
		neueSortierung[2] = new YoolooKarte(Kartenfarbe.Gelb, 10);
		aktuelleSortierung = neueSortierung;
	}

	// Sortierung wird zufuellig ermittelt
	public void sortierungFestlegenBots() {
		YoolooKarte[] neueSortierung = new YoolooKarte[this.aktuelleSortierung.length];
		for (int i = 0; i < neueSortierung.length; i++) {
			int neuerIndex = (int) (Math.random() * neueSortierung.length);
			while (neueSortierung[neuerIndex] != null) {
				neuerIndex = (int) (Math.random() * neueSortierung.length);
			}
			neueSortierung[neuerIndex] = aktuelleSortierung[i];
			// System.out.println(i+ ". neuerIndex: "+neuerIndex);
		}
		aktuelleSortierung = neueSortierung;
	}

	public void sortierungFestlegen(int userInput) {
		List<YoolooKarte> min = new ArrayList<>();
		List<YoolooKarte> mid = new ArrayList<>();
		List<YoolooKarte> max = new ArrayList<>();
		// hier füllen wir die listen
		for (int i = 0; i < this.aktuelleSortierung.length; i++) {
			if (i < 3) {
				min.add(new YoolooKarte(Kartenfarbe.Blau, i + 1));
			} else if (i < 7){
				mid.add(new YoolooKarte(Kartenfarbe.Blau, i + 1));
			} else {
				max.add(new YoolooKarte(Kartenfarbe.Blau, i + 1));
			}
		}

		switch(userInput) {
			case 1: aktuelleSortierung = getShuffleList(mid, max, min);
				break;
			case 2: aktuelleSortierung = getShuffleList(min, max, mid);
				break;
			case 3: aktuelleSortierung = getShuffleList(min, mid, max);
				break;
			case 4: sortierungCheat();
				break;
		}

	}
	public YoolooKarte[] getShuffleList(List<YoolooKarte> list1, List<YoolooKarte> list2, List<YoolooKarte> list3) {
		List<YoolooKarte> ret = new ArrayList<>();
		//Die Listen werte werden gemischt
		Collections.shuffle(list1);
		Collections.shuffle(list2);
		Collections.shuffle(list3);

		//Zusammenfügen der Listen
		ret.addAll(list1);
		ret.addAll(list2);
		ret.addAll(list3);

		int count = 0;
		//umwandlung in Array, weil das Programm es so braucht
		YoolooKarte[] result = new YoolooKarte[ret.size()];
		for (YoolooKarte card : ret) {
			result[count] = card;
			count++;
		}
		return result;
	}

	public int erhaeltPunkte(int neuePunkte) {
		YoolooLogger.info(name + " hat " + punkte + " P - erhaelt " + neuePunkte + " P - neue Summe: ");
		String message = name + " hat " + punkte + " P - erhaelt " + neuePunkte + " P - neue Summe: ";
		this.punkte = this.punkte + neuePunkte;
		YoolooLogger.info(message + this.punkte);
		return this.punkte;
	}

	@Override
	public String toString() {
		return "YoolooSpieler [name=" + name + ", spielfarbe=" + spielfarbe + ", puntke=" + punkte
				+ ", altuelleSortierung=" + Arrays.toString(aktuelleSortierung) + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Kartenfarbe getSpielfarbe() {
		return spielfarbe;
	}

	public void setSpielfarbe(Kartenfarbe spielfarbe) {
		this.spielfarbe = spielfarbe;
	}

	public int getClientHandlerId() {
		return clientHandlerId;
	}

	public void setClientHandlerId(int clientHandlerId) {
		this.clientHandlerId = clientHandlerId;
	}

	public int getPunkte() {
		return punkte;
	}

	public void setPunkte(int puntke) {
		this.punkte = puntke;
	}

	public YoolooKarte[] getAktuelleSortierung() {
		return aktuelleSortierung;
	}

	public void setAktuelleSortierung(YoolooKarte[] aktuelleSortierung) {
		this.aktuelleSortierung = aktuelleSortierung;
	}

	public void stichAuswerten(YoolooStich stich) {
		YoolooLogger.info(stich.toString());

	}

	public int getHighscore() {
		return this.highscore;
	}

	public void setHighscore(int highscore) {
		this.highscore = highscore;
	}
}

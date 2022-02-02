// History of Change
// vernr    |date  | who | lineno | what
//  V0.106  |200107| cic |    -   | add  start_Client() SERVERMESSAGE_CHANGE_STATE 

package client;

import common.*;
import messages.ClientMessage;
import messages.ClientMessage.ClientMessageType;
import messages.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class YoolooClient {

	private String serverHostname = "";
	private int serverPort = 44137;
	private Socket serverSocket = null;
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	public static int userInput = 0;
	private ClientState clientState = ClientState.CLIENTSTATE_NULL;

	private LoginMessage newLogin = null;
	private YoolooSpieler meinSpieler;
	private YoolooStich[] spielVerlauf = null;

	public YoolooClient() {
		super();
	}

	public YoolooClient(String serverHostname, int serverPort) {
		super();
		this.serverPort = serverPort;
		this.serverHostname = serverHostname;
		clientState = ClientState.CLIENTSTATE_NULL;
	}

	/**
	 * Client arbeitet statusorientiert als Kommandoempfuenger in einer Schleife.
	 * Diese terminiert wenn das Spiel oder die Verbindung beendet wird.
	 */
	public void startClient() {

		try {
			clientState = ClientState.CLIENTSTATE_CONNECT;
			verbindeZumServer();

			while (clientState != ClientState.CLIENTSTATE_DISCONNECTED && ois != null && oos != null) {
				// 1. Schritt Kommado empfangen
				ServerMessage kommandoMessage = empfangeKommando();
				if (kommandoMessage.getServerMessageType().equals(ServerMessage.ServerMessageType.CHEATER_DETECTED)) {
					if (kommandoMessage.getMessage() != null) System.out.println("Die folgenden Spieler haben gecheatet: " + kommandoMessage.getMessage());
					else System.out.println("Alle Spieler haben fair gespielt!");
				} else System.out.println("[id-x]ClientStatus: " + clientState + "] " + kommandoMessage.toString());
				// 2. Schritt ClientState ggfs aktualisieren (fuer alle neuen Kommandos)
				ClientState newClientState = kommandoMessage.getNextClientState();
				if (newClientState != null) {
					clientState = newClientState;
				}
				// 3. Schritt Kommandospezifisch reagieren
				switch (kommandoMessage.getServerMessageType()) {
				case SERVERMESSAGE_SENDLOGIN:
					// Server fordert Useridentifikation an
					// Falls User local noch nicht bekannt wird er bestimmt
					if (newLogin == null || clientState == ClientState.CLIENTSTATE_LOGIN) {
						// TODO Klasse LoginMessage erweiteren um Interaktives ermitteln des
						newLogin = eingabeSpielerDatenFuerLogin();
					}
					// Client meldet den Spieler an den Server
					oos.writeObject(newLogin);
					System.out.println("[id-x]ClientStatus: " + clientState + "] : LoginMessage fuer  " + newLogin.getSpielerName()
							+ " an server gesendet warte auf Spielerdaten");
					empfangeSpieler();
					// ausgabeKartenSet();
					break;
				case SERVERMESSAGE_SORT_CARD_SET:
					// sortieren Karten
					meinSpieler.sortierungFestlegen(spielerSortierungFestlegen());
					ausgabeKartenSet();
					// ggfs. Spielverlauf löschen
					spielVerlauf = new YoolooStich[YoolooKartenspiel.maxKartenWert];
					ClientMessage message = new ClientMessage(ClientMessageType.ClientMessage_OK,
							"Kartensortierung ist erfolgt!");
					oos.writeObject(message);
					break;
				case SERVERMESSAGE_SEND_CARD:
					spieleStich(kommandoMessage.getParamInt());
					break;
				case SERVERMESSAGE_RESULT_SET:
					System.out.println("[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState
							+ "] : Ergebnis ausgeben ");
					String ergebnis = empfangeErgebnis();
					System.out.println(ergebnis.toString());
					break;
					           // basic version: wechsel zu ClientState Disconnected thread beenden
				case SERVERMESSAGE_CHANGE_STATE:
				break ;

				default:
					break;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verbindung zum Server aufbauen, wenn Server nicht antwortet nach ein Sekunde
	 * nochmals versuchen
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	// TODO Abbruch nach x Minuten einrichten
	private void verbindeZumServer() throws UnknownHostException, IOException {
		while (serverSocket == null) {
			try {
				serverSocket = new Socket(serverHostname, serverPort);
			} catch (ConnectException e) {
				System.out.println("Server antwortet nicht - ggfs. neu starten");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}
		System.out.println("[Client] Serversocket eingerichtet: " + serverSocket.toString());
		// Kommunikationskanuele einrichten
		ois = new ObjectInputStream(serverSocket.getInputStream());
		oos = new ObjectOutputStream(serverSocket.getOutputStream());
	}

	private void spieleStich(int stichNummer) throws IOException {
		System.out.println("[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState
				+ "] : Spiele Karte " + stichNummer);
		spieleKarteAus(stichNummer);
		YoolooStich iStich = empfangeStich();
		spielVerlauf[stichNummer] = iStich;
		System.out.println("[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState
				+ "] : Empfange Stich " + iStich);
		if (iStich.getSpielerNummer() == meinSpieler.getClientHandlerId()) {
			System.out.print(
					"[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState + "] : Gewonnen - ");
			meinSpieler.erhaeltPunkte(iStich.getStichNummer() + 1);
		}

	}

	private void spieleKarteAus(int i) throws IOException {
		oos.writeObject(meinSpieler.getAktuelleSortierung()[i]);
	}

	// Methoden fuer Datenempfang vom Server / ClientHandler
	private ServerMessage empfangeKommando() {
		ServerMessage kommando = null;
		boolean failed = false;
		try {
			kommando = (ServerMessage) ois.readObject();
		} catch (ClassNotFoundException e) {
			failed = true;
			e.printStackTrace();
		} catch (IOException e) {
			failed = true;
			e.printStackTrace();
		}
		if (failed)
			kommando = null;
		return kommando;
	}

	private void empfangeSpieler() {
		try {
			meinSpieler = (YoolooSpieler) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	private YoolooStich empfangeStich() {
		try {
			return (YoolooStich) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String empfangeErgebnis() {
		try {
			return (String) ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private LoginMessage eingabeSpielerDatenFuerLogin() {
		LoginMessage loginMessage = new LoginMessage();

		System.out.println("Usernamen eingeben: ");
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine();

		loginMessage.setSpielerName(username);

		return loginMessage;
	}

	public void ausgabeKartenSet() {
		// Ausgabe Kartenset
		System.out.println("[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState
				+ "] : Uebermittelte Kartensortierung beim Login ");
		for (int i = 0; i < meinSpieler.getAktuelleSortierung().length; i++) {
			System.out.println("[id-" + meinSpieler.getClientHandlerId() + "]ClientStatus: " + clientState
					+ "] : Karte " + (i + 1) + ":" + meinSpieler.getAktuelleSortierung()[i]);
		}

	}

	public enum ClientState {
		CLIENTSTATE_NULL, // Status nicht definiert
		CLIENTSTATE_CONNECT, // Verbindung zum Server wird aufgebaut
		CLIENTSTATE_LOGIN, // Anmeldung am Client Informationen des Users sammeln
		CLIENTSTATE_RECEIVE_CARDS, // Anmeldung am Server
		CLIENTSTATE_SORT_CARDS, // Anmeldung am Server
		CLIENTSTATE_REGISTER, // t.b.d.
		CLIENTSTATE_PLAY_SINGLE_GAME, // Spielmodus einfaches Spiel
		CLIENTSTATE_DISCONNECT, // Verbindung soll getrennt werden
		CLIENTSTATE_DISCONNECTED // Vebindung wurde getrennt
	};


	public int spielerSortierungFestlegen() {
		System.out.println("Wähle Optionen und notiere die Nummer: ");
		System.out.println("1. mid, max, min");
		System.out.println("2. min, max, mid");
		System.out.println("3. min, mid, max");
		System.out.println("4. Cheat mode");
		Scanner scan = new Scanner(System.in);
		userInput = scan.nextInt();
		return userInput;

	}

}

package app.wolfware;

import app.wolfware.Package.Attribute;
import app.wolfware.Package.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public class Zusi3Schnittstelle extends Thread {
	
	//Zusi-Fahrsimulator
	private String mVersionZusi = "error";
	private String mVerbindungsinfoZusi = "error";
	
	//Mein Programm
	private Socket mSocket;
	private String mClientName;
	private String mIp;
	private int mPort;
	private String mVersion;
	
	boolean reqFuehrerstandsbedienung = false;
	boolean reqProgrammdaten = false;
	boolean mDebugOutput = false;
	
	private boolean mRun = true;
	private int mCountRetry = 0;
	
	private ZusiDataManager mManager;
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param clientName
	 */
	public Zusi3Schnittstelle(String ip, int port, String clientName) {
		mClientName = clientName;
		mIp = ip;
		mPort = port;
		mVersion = Value.VERSION;
		mManager = new ZusiDataManager();
	}
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param clientName
	 */
	public Zusi3Schnittstelle(String ip, String port, String clientName) {
		mClientName = clientName;
		mIp = ip;
		try {
			mPort = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			mPort = Value.STANDART_SERVER_PORT;
		}
		mVersion = Value.VERSION;
		mManager = new ZusiDataManager();
	}
	
	/**
	 * Startet den Thread und baut eine Verbindung zum Server auf
	 */
	public void connect() {
		start();
	}
	
	/**
	 * Trennt die Verbindung
	 */
	public void close() {
		try {
			mRun = false;
			//mManager = null;
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Klasse als Listener anmelden
	 * @param listener
	 */
	public void register(Object listener) {
		mManager.addListener(listener);
	}
	
	/**
	 * Angemeldete Klasse abmelden
	 * @param listener
	 */
	public void unregister(Object listener) {
		mManager.removeListener(listener);
	}
	
	/**
	 * Legt fest, welche Daten von Zusi gefordert werden sollen
	 * @param request
	 */
	public void requestFuehrerstandsanzeigen(int request) {
		mManager.requestFuehrerstandsanzeigen(request);
	}
	
	/**
	 * Legt fest, welche Daten von Zusi gefordert werden sollen
	 * Kürzerer Methodenname
	 * @param request
	 */
	public void reqFstAnz(int request) {
		mManager.requestFuehrerstandsanzeigen(request);
	}
	
	public void requestFuehrerstandsbedienung(boolean value) {
		reqFuehrerstandsbedienung = value;
	}
	
	public void requestProgrammdaten(boolean value) {
		reqProgrammdaten = value;
	}
	
	/**
	 * Legt fest, ob die Zusi3Schnittstelle Ausgaben in die Konsole schreiben soll
	 * Standartmäßig = false
	 * @param output
	 */
	public void setDebugOutput(boolean output) {
		mDebugOutput = output;
	}
	
	/**
	 * Gibt die Version von Zusi zurück
	 * @return
	 */
	public String getZusiVersion() {
		return mVersionZusi;
	}
	
	public String getVersion() {
		return Value.VERSION;
	}
	
	
	/**
	 * Gibt Informationen zur Verbindung zur�ck
	 * @return
	 */
	public String getVerbindungsinfo() {
		return mVerbindungsinfoZusi;
	}
	
	@Deprecated
	public void zeitraffer(boolean value) {
		
	}
	
	@Deprecated
	public void pause(boolean value) {
		
	}
	
	private BufferedInputStream stream;
	
	@Override
	public void run() {
		System.out.println("Zusi3Schnitstelle-Version: " + Value.VERSION);
		while(mRun) {
			try {
				mSocket = new Socket();
				mSocket.setReuseAddress(true);
				mSocket.connect(new InetSocketAddress(mIp, mPort), 500);
				stream = new BufferedInputStream(mSocket.getInputStream());
				if (!mSocket.isClosed()) {
					try {
						HELLO();
					} catch (Exception ignored) {
						
					}
					
					try {
						ACK_HELLO();
					} catch (Exception ignored) {
						
					}
					
					try {
						NEEDED_DATA();
					} catch (Exception ignored) {
						
					}
					
					try {
						ACK_NEEDED_DATA();
					} catch (Exception ignored) {
						
					}
					//BufferedInputStream stream = new BufferedInputStream(mSocket.getInputStream());
					mManager.progressEvent(0x00, 1, -1);
					mCountRetry = 0;
					if (mDebugOutput) {
						System.out.println("Zusi3Schnittstelle: Verbunden");
					}
					
					while(mRun) {
						try {
							byte[] header = new byte[4];
							stream.read(header, 0, 4);
							byte[] ID = new byte[2];
							stream.read(ID, 0, 2);
							if (compare(ID, new byte[] {0x00, 0x00})) {
								if (mDebugOutput) {
									System.err.println("Zusi3Schnittstelle: Verbindung verloren");
								}
								mManager.progressEvent(0x00, 2, -1);
								break;
							}
						    Node node = streamToNodes(stream, ID);
						    //System.out.println(node.toString());
						    mManager.processData(node);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (SocketTimeoutException ste) {
				
			} catch (SocketException e1) {
				
			} catch (IOException e1) {
				
			}
			if (mRun) {
				try {
					mSocket.close();
					mSocket = null;
				} catch (IOException e1) {
					
				}
				try { Thread.sleep(2500); } catch (InterruptedException e) { }
				mCountRetry++;
				if (mDebugOutput) {
					System.out.println("Zusi3Schnittstelle: Verbindungsaufbau (" + mCountRetry + ")");
				}
				mManager.progressEvent(0x00, 3, mCountRetry);
			}
		}
		mManager.progressEvent(0x00, 0, -1);
		mManager = null;
	}
	
	/**
	 * Befehl 00 01 � HELLO (Client -> Zusi)
	 * Mit dem HELLO-Befehl melden sich die einzelnen Clients bei Zusi an.
	 * 
	 * @throws Exception
	 */
	private void HELLO() throws Exception {
		Node verbindungsaufbau = new Node(1);
			Node befehl_HELLO = new Node(1);
				Attribute protokoll_Version = new Attribute(1, 2);
				Attribute client_Typs = new Attribute(2, 2); 
				Attribute identifikation = new Attribute(3, mClientName);
				Attribute versionsnummer = new Attribute(4, mVersion);
			befehl_HELLO.addAttribute(protokoll_Version);
			befehl_HELLO.addAttribute(client_Typs);
			befehl_HELLO.addAttribute(identifikation);
			befehl_HELLO.addAttribute(versionsnummer);
		verbindungsaufbau.addNode(befehl_HELLO);
		
		mSocket.getOutputStream().write(verbindungsaufbau.get());  
	}
	
	/**
	 * Befehl 00 02 � ACK_HELLO (Zusi -> Client)
	 * Zusi teilt mit, ob der Client akzeptiert wird.
	 * 
	 * @throws Exception
	 */
	private void ACK_HELLO() throws Exception {
		Node verbindungsaufbau = getNodes();
		/*System.out.println("L�nge: " + verbindungsaufbau.get().length);
		for (byte b : verbindungsaufbau.get()) {
			System.out.print(b + ", ");
		}*/
		//System.out.println("");
		if (verbindungsaufbau.getIDAsInt() == 0x01) {
			Node befehl_ACK_HELLO = verbindungsaufbau.getNodeByID(0x02);
			if (befehl_ACK_HELLO != null) {
				int client_Aktzeptiert = befehl_ACK_HELLO.getAttributeByID(3).getDATAAsInt();
				String zusi_version = befehl_ACK_HELLO.getAttributeByID(1).getDATAAsString();
				String zusi_verbindungsinfo = befehl_ACK_HELLO.getAttributeByID(2).getDATAAsString();
				mManager.progressEvent(0x10, zusi_version, zusi_verbindungsinfo, client_Aktzeptiert == 0 ? true : false);
				mVersionZusi = zusi_version;
				mVerbindungsinfoZusi = zusi_verbindungsinfo;
			}
		}
	}
	
	/**
	 * Befehl 00 03 - NEEDED_DATA (Client -> Zusi)
	 * Mit dem NEEDED_DATA-Befehl teilt der Client Zusi mit, welche Daten er ben�tigt.
	 * Es werden die ID-Nummern gem�� Zusi-F�hrerstand-Datenformat benutzt.
	 * 
	 * @throws Exception
	 */
	private void NEEDED_DATA() throws Exception {
		Node client_Anwendung = new Node(0x02);
			Node befehl_NEEDED_DATA = new Node(0x03);
				Node untergruppe_Fuehrerstandsanzeigen = new Node(0x0A);
				for (int fuehrerstandsanzeigenID : mManager.getRequestedFuehrerstandsanzeigen()) {
					Attribute attribute = new Attribute(1, fuehrerstandsanzeigenID);
					untergruppe_Fuehrerstandsanzeigen.addAttribute(attribute);
				}
			befehl_NEEDED_DATA.addNode(untergruppe_Fuehrerstandsanzeigen);
			if (reqFuehrerstandsbedienung) {
				Node untergruppe_Fuehrerstandsbedienung = new Node (0x0B);
				befehl_NEEDED_DATA.addNode(untergruppe_Fuehrerstandsbedienung);
			}
			if (reqProgrammdaten) {
				Node untergruppe_Programmdaten = new Node (0x0C);
				for (int i = 1 ; i <= 4 ; i++) { //<- 3
					Attribute attribute = new Attribute(1, i);
					untergruppe_Programmdaten.addAttribute(attribute);
				}
			befehl_NEEDED_DATA.addNode(untergruppe_Programmdaten);
			}	
		client_Anwendung.addNode(befehl_NEEDED_DATA);
		//System.out.println(client_Anwendung.toString());
		mSocket.getOutputStream().write(client_Anwendung.get());
	}
	
	/**
	 * Befehl 00 04 � ACK_NEEDED_DATA (Zusi -> Client)
	 * Entscheidung, ob die ben�tigten Daten akzeptiert werden.
	 * Der Befehl wird von Zusi nach dem NEEDED_DATA-Befehl an den Client gesendet.
	 * 
	 * @throws Exception
	 */
	private void ACK_NEEDED_DATA() throws Exception {
		Node Client_Anwendung_02 = getNodes();
		if (Client_Anwendung_02.getIDAsInt() == 2) {
			Node befehl_ACK_NEEDED_DATA = Client_Anwendung_02.getNodeByID(0x04);
			if (befehl_ACK_NEEDED_DATA != null) {
				int befehl_Aktzeptiert = befehl_ACK_NEEDED_DATA.getAttributeByID(1).getDATAAsInt();
				if (befehl_Aktzeptiert == 0) {
					mManager.progressEvent(0x11, true);
				} else {
					mManager.progressEvent(0x11, false);
				}
			}
		}
	}
	
	/**
	 * 
	 * @return node
	 */
	private Node getNodes() {
	    try {
		    //BufferedInputStream stream = new BufferedInputStream(mSocket.getInputStream());
		    byte[] header = new byte[4];
			stream.read(header, 0, 4);
			byte[] ID = new byte[2];
			stream.read(ID, 0, 2);
		    Node node = streamToNodes(stream, ID);
		    return node;
	    } catch (Exception e) {
	    	//e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * 
	 * @param stream
	 * @param ID
	 * @return
	 */
	private Node streamToNodes(BufferedInputStream stream, byte[] ID) {
		Node rootNode = new Node(ID);
		try {
			while (true) {
				byte[] header = new byte[4];
				stream.read(header, 0, 4);
				if (compare(header, Value.STANDART_KNOTEN_ANFANG)) {
					byte[] lID = new byte[2];
					stream.read(lID, 0, 2);
					Node subnode = streamToNodes(stream, lID);
					rootNode.addNode(subnode);
				} else if (compare(header, Value.STANDART_KNOTEN_ENDE)) {
					return rootNode;
				} else {
					byte[] lID = new byte[2];
					stream.read(lID, 0, 2);
					int length = ((header[0] & 0xFF) | (header[1] & 0xFF) << 8 | (header[2] & 0xFF) << 16 | (header[3] & 0xFF) << 24);
					//System.out.println("L�nge: " + length);
					if (length-2 > 0) { // new
						byte[] DATA = new byte[length-2];
						stream.read(DATA, 0, length-2);
						Attribute attr = new Attribute(lID, DATA);
						rootNode.addAttribute(attr);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return null;
		return rootNode;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean compare(byte[] a, byte[] b) {
		int aLength = a.length;
		int bLength = b.length;
		int endPos = -1;
		if (aLength > bLength) {
			endPos = bLength;
		} else if (bLength > aLength) {
			endPos = aLength;
		} else {
			endPos = aLength;
		}
		for (int i = 0 ; i < endPos ; i++) {
			if (a[i] == b[i]) {
				
			} else {
				return false;
			}
		}
		return true;
	}
}

package app.wolfware;

import app.wolfware.Exceptions.ZusiEventException;
import app.wolfware.Interfaces.ZusiData;
import app.wolfware.Interfaces.ZusiEvent;
import app.wolfware.Package.Attribute;
import app.wolfware.Package.Node;
import app.wolfware.Values.FstAnz;
import app.wolfware.Values.Fuehrerstandsanzeigen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class ZusiDataManager extends Fuehrerstandsanzeigen {

	private List<Integer> mRequestListFuehrerstandsanzeigen = new ArrayList<Integer>();

	private List<Object> mListeners = new ArrayList<Object>();
	
	public void addListener(Object listener) {
		mListeners.add(listener);
	}
	
	public void removeListener(Object listener) {
		mListeners.remove(listener);
	}
	
	public List<Object> getListeners() {
		return mListeners;
	}

	public void requestFuehrerstandsanzeigen(int request) {
		mRequestListFuehrerstandsanzeigen.add(request);
		
	}
	
	public List<Integer> getRequestedFuehrerstandsanzeigen() {
		return mRequestListFuehrerstandsanzeigen;
	}
	
	

	public void processData(Node nodes) {
		if (nodes == null) {
			return;
		}
		if (nodes.getIDAsInt() == 0x02) {
			for (Node itemNode : nodes.getNodes()) {
				if (itemNode.getIDAsInt() == 0x0A) {
					processFuehrerstandsanzeigen(itemNode);
				} else if (itemNode.getIDAsInt() == 0x0B) {
					processTastatur(itemNode);
				} else if (itemNode.getIDAsInt() == 0x0C) {
					processZugData(itemNode);
				}
			}
		}
	}
	
	private void processZugData(Node nodes) {
		for (Attribute attribute : nodes.getAttribute()) {
			if (attribute.getIDAsInt() == 0x01) {
				progressEvent(0xC1, attribute.getDATAAsString());
			} else if (attribute.getIDAsInt() == 0x02) {
				progressEvent(0xC2, attribute.getDATAAsString());
			} else if (attribute.getIDAsInt() == 0x03) {
				progressEvent(0xC3);
			} else if (attribute.getIDAsInt() == 0x04) {
				progressEvent(0xC4, attribute.getDATAAsString());
			}
		}
	}
	
	private void processTastatur(Node nodes) {
		//TODO
		for (Node itemNode : nodes.getNodes()) {
			if (itemNode.getIDAsInt() == 0x01) {
				for (Attribute attribute : itemNode.getAttribute()) {
					if (attribute.getIDAsInt() == 0x02) {
						if (attribute.getDATAAsInt() == 0x33) {
							progressData(0x50, true);
						} else if (attribute.getDATAAsInt() == 0x34) {
							progressData(0x50, false);
						} else if (attribute.getDATAAsInt() == 0x35) {
							progressData(0x51, true);
						} else if (attribute.getDATAAsInt() == 0x36) {
							progressData(0x51, false);
						} else if (attribute.getDATAAsInt() == 0x37) {
							progressData(0x52, true);
						} else if (attribute.getDATAAsInt() == 0x38) {
							progressData(0x52, false);
						} else if (attribute.getDATAAsInt() == 0x2F) {
							progressData(0x53, true);
						} else if (attribute.getDATAAsInt() == 0x30) {
							progressData(0x53, false);
						} else if (attribute.getDATAAsInt() == 0x45) {
							progressData(0x69, true);
						} else if (attribute.getDATAAsInt() == 0x46) {
							progressData(0x69, false);
						}
					}
				}
			}
		}
	}
	
	private void processFuehrerstandsanzeigen(Node nodes) {
		for (Node itemNode : nodes.getNodes()) {
			int id = itemNode.getIDAsInt();
			if (id == Status_Sifa) {
				processSifa(itemNode);
			} else if (id == Status_Zugbeeinflussung) {
				processZugsicherung(itemNode);
			} else if (id == Status_Tueren) {
				
			}
		}
		for (Attribute attribute : nodes.getAttribute()) {
			switch (attribute.getIDAsInt()) {
			case keine_Funktion:
				break;
			case Geschwindigkeit:
				progressData(Geschwindigkeit, Math.round(attribute.getDATAAsFloat() * 3.6F));
				break;
			case Druck_Hauptluftleitung:
				progressData(Druck_Hauptluftleitung, Math.round(attribute.getDATAAsFloat()*100) / 100.0);
				//progressData(Druck_Hauptluftleitung, attribute.getDATAAsFloat());
				break;
			case Druck_Bremszylinder:
				progressData(Druck_Bremszylinder, Math.round(attribute.getDATAAsFloat()*100) / 100.0);
				//progressData(Druck_Bremszylinder, attribute.getDATAAsFloat());
				break;
			case Druck_Hauptluftbehaelter:
				progressData(Druck_Hauptluftbehaelter, Math.round(attribute.getDATAAsFloat()*100) / 100.0);
				//progressData(Druck_Hauptluftbehaelter, attribute.getDATAAsFloat());
				break;
			case Luftpresser_laeuft:
				progressData(Luftpresser_laeuft, attribute.getDATAAsBoolean());
				break;
			case Luftstrom_Fvb:
				progressData(Luftstrom_Fvb, attribute.getDATAAsInt());
				break;
			case Luftstrom_Zbv:
				progressData(Luftstrom_Zbv, attribute.getDATAAsInt());
				break;
			case Luefter_an:
				progressData(Luefter_an, attribute.getDATAAsBoolean());
				break;
			case Zugkraft_gesamt: {
				float n = attribute.getDATAAsFloat();
				if (n > 0.0F) {
					progressData(Zugkraft_gesamt, n / 2000.0F);
				} else {
					progressData(Zugkraft_gesamt, n * 0.002F);
				}
				break;
			}
			case Zugkraft_pro_Achse:{
				float n = attribute.getDATAAsFloat();
				if (n > 0.0F) {
					progressData(Zugkraft_pro_Achse, n / 2000.0F);
				} else {
					progressData(Zugkraft_pro_Achse, n * 0.002F);
				}
				break;
			}
			case Zugkraft_Soll_gesamt:{
				float n = attribute.getDATAAsFloat();
				if (n > 0.0F) {
					progressData(Zugkraft_Soll_gesamt, n / 2000.0F);
				} else {
					progressData(Zugkraft_Soll_gesamt, n * 0.002F);
				}
				break;
			}
			case Zugkraft_Soll_pro_Achse:{
				float n = attribute.getDATAAsFloat();
				if (n > 0.0F) {
					progressData(Zugkraft_Soll_pro_Achse, n / 2000.0F);
				} else {
					progressData(Zugkraft_Soll_pro_Achse, n * 0.002F);
				}
				break;
			}
			case Oberstrom:
				progressData(Oberstrom, (int) attribute.getDATAAsFloat());
				break;
			case Fahrleitungsspannung:
				progressData(Fahrleitungsspannung, (int) attribute.getDATAAsFloat());
				break;
			case Motordrehzahl:
				progressData(Motordrehzahl, attribute.getDATAAsInt()); //****
				break;
			case Uhrzeit_Stunde:
				progressData(Uhrzeit_Stunde, attribute.getDATAAsInt()); //****
				break;
			//TODO
				//---------------------
			case Streckenhoechstgeschwindigkeit:
				progressData(Streckenhoechstgeschwindigkeit, Math.round(attribute.getDATAAsFloat() * 3.6F));
			case Kilometrierung_Zugspitze:
				progressData(Kilometrierung_Zugspitze, Math.round(attribute.getDATAAsFloat()*100) / 100.0);
				//progressData(Kilometrierung_Zugspitze, new DecimalFormat("0.000").format(attribute.getDATAAsFloat()));
				//progressData(Kilometrierung_Zugspitze, attribute.getDATAAsFloat());
				break;
			case Aussenhelligkeit:
				double d = Math.pow(10, 2);
				d = Math.round(attribute.getDATAAsFloat() * d) / d;
				d = d * 100;
				d *= 2;
				progressData(Aussenhelligkeit, (int) d);
				break;
			default:
				break;
			}
		}
	}
	
	private void processSifa(Node nodes) {
		String bauart = null;
		boolean lm = false;
		int hupe = 0;
		boolean hauptschalter = false;
		boolean stoerstschalter = false;
		boolean luftabsperrhahn = false;
		for (Attribute attribute : nodes.getAttribute()) {
			int id = attribute.getIDAsInt();
			if (id == 0x01) {
				bauart = attribute.getDATAAsString();
			} else if (id == 0x02) {
				lm = attribute.getDATAAsBoolean();
			} else if (id == 0x03) {
				hupe = attribute.getDATAAsInt();
			} else if (id == 0x04) {
				hauptschalter = attribute.getDATAAsBoolean();
			} else if (id == 0x05) {
				stoerstschalter = attribute.getDATAAsBoolean();
			} else if (id == 0x06) {
				luftabsperrhahn = attribute.getDATAAsBoolean();
			}
		}
		progressData(Status_Sifa, bauart, lm, hupe, hauptschalter, stoerstschalter, luftabsperrhahn);
	}
	
	private void processZugsicherung(Node nodes) {
		//TODO
		for (Node itemNode : nodes.getNodes()) {
			if (itemNode.getIDAsInt() == 0x03) {
				for (Attribute attribute : itemNode.getAttribute()) {
					if (attribute.getIDAsInt() == 0x03) {
						progressData(0x60, attribute.getDATAAsInt());
					} else if (attribute.getIDAsInt() == 0x05) { //1000Hz
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					} else if (attribute.getIDAsInt() == 0x06) { //55
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					} else if (attribute.getIDAsInt() == 0x07) { //70
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					} else if (attribute.getIDAsInt() == 0x08) { //85
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					} else if (attribute.getIDAsInt() == 0x0A) { //500
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					} else if (attribute.getIDAsInt() == 0x0b) { //Befehl
						progressData(FstAnz.Status_Zugbeeinflussung, attribute.getIDAsInt(), attribute.getDATAAsBoolean());
					}
				}
			}
		}
	}

	
	
	public void progressEvent(int value, Object... args)  {
		for (Object listener : mListeners) {
			Class<? extends Object> listenerClass = listener.getClass();
			for (Method method : listenerClass.getDeclaredMethods()) {
				if (method.getAnnotation(ZusiEvent.class) != null) {
					ZusiEvent event = method.getAnnotation(ZusiEvent.class);
					if (event.value() == value) {
						try {
							method.invoke(listener, args);
						} catch (IllegalArgumentException e) {
							try {
								throw new ZusiEventException("wrong number of arguments -> " + method.getName());
							} catch (ZusiEventException e1) {
								e1.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void progressData(int value, Object... args) {
		for (Object listener : mListeners) {
			Class<? extends Object> listenerClass = listener.getClass();
			for (Method method : listenerClass.getDeclaredMethods()) {
				if (method.getAnnotation(ZusiData.class) != null) {
					ZusiData event = method.getAnnotation(ZusiData.class);
					if (event.value() == value) {
						try {
							method.invoke(listener, args);
						} catch (IllegalArgumentException e) {
							try {
								throw new ZusiEventException("wrong number of arguments -> " + method.getName());
							} catch (ZusiEventException e1) {
								e1.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}	
}

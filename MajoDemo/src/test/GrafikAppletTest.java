package test;

import graphics.GrafikApplet;

import java.awt.Container;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.swing.JFrame;

/**
 * Prüft die Klasse {@link GrafikApplet}.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class GrafikAppletTest {

	/** Testdaten */
	private final static String USER_DATA =
		"USER[10]	43508\n"+
		"USER[12]	70147\n"+
		"USER[13]	3073\n"+
		"USER[14]	22963\n"+
		"VALUE[GH~1. Auftrag]	4945\n"+
		"VALUE[GH~1. Auftragsnummer]	205\n"+
		"VALUE[GH~1. Aufträge]	164857\n"+
		"VALUE[GH~1. Auskunft]	17992";
	
	/** Testdaten */
	private final static String SESSION_DATA =
	"10	2014-10-09 08:34:33 [1412836473000:2 Auskunft][1412836474000:2 Lieferanten][1412836476000:Zuname I][1412837454000:B+R-Nummer][1412837810000:B+R-Nummer]\n"+
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]\n"+
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]\n";
	
	/** Testdaten */
	private final static String MORE_SESSION_DATA = 
	"10	2014-10-10 12:32:39 [1412937159000:2 Auskunft][1412937160000:Kunden-Nr]\n"+
	"10	2014-10-10 12:33:39 [1412937219000:2 Auskunft]\n"+
	"10	2014-10-10 12:33:42 [1412937222000:2 Auskunft][1412937224000:4 Aufträge][1412944483000:Zuname I]\n"+
	"10	2014-10-10 12:33:45 [1412937225000:Kunden-Nr][1412937280000:1 Kunden][1412938301000:2 Auskunft]\n"+
	"10	2014-10-10 12:34:49 [1412937289000:2 Auskunft][1412937292000:B+R-Nummer]\n"+
	"10	2014-10-10 12:35:37 [1412937337000:1 Einzelauftrag][1412937338000:Kunden-Nr][1412937344000:B+R-Nummer][1412937472000:B+R-Nummer][1412937537000:2 Stornieren][1412938285000:1 Aufträge][1412938286000:1 Einzelauftrag][1412938288000:Zuname I][1412938303000:4 Aufträge][1412938304000:5 B+R-Kunde][1412938305000:Zuname I][1412938317000:Zuname I][1412938349000:1 Einzelauftrag][1412938350000:Kunden-Nr][1412938354000:1 B+R-Lagerware (Zentrallager)][1412938356000:B+R-Nummer][1412938413000:2 Stornieren][1412938414000:2 Auskunft][1412938415000:4 Aufträge][1412938416000:Kunden-Nr][1412938604000:1 Aufträge][1412938605000:1 Einzelauftrag][1412938611000:2 Sonderauftrag][1412938612000:4 Telefax][1412938678000:1 Drucken+Speichern][1412938700000:Kunden-Nr][1412938706000:B+R-Nummer][1412938728000:B+R-Nummer][1412942081000:1 Einzelauftrag][1412942082000:Kunden-Nr][1412942084000:Zuname I][1412942095000:1 B+R-Lagerware (Zentrallager)][1412942096000:4 Telefax][1412942097000:B+R-Nummer][1412942105000:Artikel-Bezeichnung][1412942130000:B+R-Nummer][1412942132000:1 Drucken+Speichern][1412942169000:2 Stornieren][1412942177000:1 Kunden][1412942179000:Ort][1412942220000:1 Kunden][1412942222000:1 Aufträge][1412942228000:1 B+R-Lagerware (Zentrallager)][1412942229000:4 Telefax][1412942230000:Artikel-Nr Lieferant][1412942237000:Artikel-Bezeichnung][1412942264000:B+R-Nummer][1412942265000:1 Drucken+Speichern]\n"+
	"10	2014-10-10 01:56:11 [1412942171000:2 Auskunft][1412942312000:1 Genau am][1412943503000:1 Aufträge][1412944482000:Kunden-Nr][1412944528000:1 B+R-Lagerware (Zentrallager)][1412944529000:4 Telefax][1412944547000:B+R-Nummer][1412944552000:1 Drucken+Speichern]\n"+
	"10	2014-10-10 02:37:28 [1412944648000:8 Textverarbeitung][1412944651000:Kunden-Nr][1412944652000:Zuname I][1412944668000:B+R-Nummer][1412944722000:2 Auftrags-Bestätigung]\n"+
	"10	2014-10-10 03:34:17 [1412948057000:2 Auskunft][1412948058000:3 Artikel][1412948059000:B+R-Nummer][1412948062000:3 Artikel][1412948063000:B+R-Nummer]\n"+
	"10	2014-10-10 03:35:31 [1412948131000:2 Auskunft][1412948133000:4 Aufträge][1412948134000:Zuname I]\n"+
	"10	2014-10-10 03:38:35 [1412948315000:2 Auskunft][1412948318000:B+R-Nummer][1412949090000:1 Aufträge]\n"+
	"10	2014-10-10 03:56:59 [1412949419000:2 Auskunft][1412949421000:4 Aufträge][1412949423000:Kunden-Nr][1412949426000:Zuname I]\n"+
	"10	2014-10-13 06:58:26 [1413176306000:2 Auskunft][1413176307000:Kunden-Nr][1413176308000:Zuname I][1413176450000:Zuname I][1413179178000:1 Aufträge][1413186424000:Kunden-Nr][1413186428000:1 B+R-Lagerware (Zentrallager)][1413186429000:B+R-Nummer][1413190282000:B+R-Nummer][1413190284000:1 Drucken+Speichern]\n"+
	"10	2014-10-13 07:46:26 [1413179186000:2 Auskunft][1413179187000:Kunden-Nr][1413179191000:Zuname I]\n"+
	"10	2014-10-13 07:49:42 [1413179382000:2 Auskunft][1413179384000:4 Aufträge][1413179386000:3 Rechnungs-Nummer][1413183434000:Kunden-Nr]\n"+
	"10	2014-10-13 07:52:02 [1413179522000:2 Auskunft][1413179524000:3 Artikel]\n"+
	"10	2014-10-13 07:52:22 [1413179542000:1 Kunden][1413179543000:Zuname I][1413184064000:Zuname I][1413184074000:Zuname I][1413188831000:B+R-Nummer]\n"+
	"10	2014-10-13 07:54:27 [1413179667000:2 Auskunft][1413179670000:Kunden-Nr][1413179671000:Zuname I]\n"+
	"10	2014-10-13 07:57:00 [1413179820000:2 Auskunft][1413179822000:4 Aufträge][1413179824000:Kunden-Nr][1413187679000:Kunden-Nr]\n"+
	"10	2014-10-13 07:58:08 [1413179888000:2 Auskunft][1413179890000:3 Artikel][1413179893000:B+R-Nummer][1413179894000:Artikel-Bezeichnung][1413179902000:Artikel-Bezeichnung][1413179905000:Artikel-Bezeichnung]\n"+
	"10	2014-10-13 07:58:47 [1413179927000:2 Auskunft][1413179930000:3 Artikel][1413179932000:Artikel-Bezeichnung][1413187730000:1 Genau am]\n"+
	"10	2014-10-13 08:06:58 [1413180418000:2 Auskunft][1413180420000:4 Aufträge][1413180421000:Kunden-Nr][1413180422000:Zuname I][1413186628000:4 Aufträge][1413186630000:5 B+R-Kunde][1413192762000:4 Aufträge]\n"+
	"10	2014-10-13 08:16:37 [1413180997000:2 Auskunft][1413180998000:3 Artikel][1413180999000:B+R-Nummer][1413181078000:2 Lieferanten][1413181080000:Liefer-Nr]\n"+
	"10	2014-10-13 08:25:34 [1413181534000:8 Textverarbeitung][1413181536000:Kunden-Nr][1413181541000:Zuname I][1413181613000:B+R-Nummer][1413181637000:B+R-Nummer][1413181719000:3 Angebot][1413181735000:3 Angebot][1413187640000:B+R-Nummer]\n"+
	"10	2014-10-13 08:29:39 [1413181779000:2 Auskunft][1413181780000:Kunden-Nr][1413182741000:1 Aufträge][1413182758000:1 Aktive Sonderpreise][1413182767000:B+R-Nummer][1413182779000:B+R-Nummer][1413183434000:1 Kunden][1413183436000:Zuname I][1413186374000:1 Einzelauftrag][1413192764000:5 B+R-Kunde][1413202206000:4 Aufträge][1413202208000:5 B+R-Kunde]\n"+
	"10	2014-10-13 09:07:43 [1413184063000:2 Auskunft][1413184064000:1 Kunden][1413186294000:Artikel-Nr Lieferant][1413186301000:Artikel-Nr Lieferant][1413186304000:Artikel-Nr Lieferant]\n"+
	"10	2014-10-13 09:43:59 [1413186239000:2 Auskunft][1413186241000:4 Aufträge][1413186242000:Kunden-Nr][1413186243000:Zuname I]\n"+
	"10	2014-10-13 09:44:51 [1413186291000:2 Auskunft][1413186293000:3 Artikel][1413186312000:B+R-Nummer][1413186374000:Kunden-Nr][1413187630000:Kunden-Nr][1413188985000:1 Aufträge][1413188990000:1 B+R-Lagerware (Zentrallager)][1413188993000:1 Telefon][1413189015000:B+R-Nummer][1413199047000:1 Drucken+Speichern][1413199059000:1 Genau am][1413202003000:B+R-Nummer]\n"+
	"10	2014-10-13 09:46:24 [1413186384000:2 Auskunft][1413186385000:B+R-Nummer][1413186424000:1 Einzelauftrag][1413186446000:B+R-Nummer][1413186449000:1 Drucken+Speichern][1413186493000:1 Aufträge][1413186516000:B+R-Nummer][1413186543000:B+R-Nummer][1413186547000:1 Drucken+Speichern][1413186571000:1 Genau am][1413188960000:3 Artikel][1413199039000:B+R-Nummer][1413202004000:Artikel-Nr Lieferant][1413205139000:B+R-Nummer]\n"+
	"10	2014-10-13 09:49:52 [1413186592000:2 Auskunft][1413186593000:4 Aufträge][1413186595000:Kunden-Nr][1413187663000:Kunden-Nr]\n"+
	"10	2014-10-13 09:50:26 [1413186626000:2 Auskunft][1413186630000:Kunden-Nr]\n"+
	"10	2014-10-13 09:59:23 [1413187163000:2 Auskunft][1413187164000:4 Aufträge][1413187167000:Kunden-Nr][1413187466000:1 Aufträge]\n"+
	"10	2014-10-13 10:04:27 [1413187467000:2 Auskunft][1413187468000:4 Aufträge][1413187469000:5 B+R-Kunde][1413187470000:Zuname I][1413187679000:1 Aufträge][1413201572000:B+R-Nummer]\n"+
	"10	2014-10-13 10:06:08 [1413187568000:2 Auskunft][1413187569000:B+R-Nummer][1413187629000:1 Einzelauftrag][1413187639000:1 B+R-Lagerware (Zentrallager)][1413187640000:4 Telefax]\n"+
	"10	2014-10-13 10:07:39 [1413187659000:2 Auskunft][1413187661000:4 Aufträge][1413187663000:5 B+R-Kunde][1413187679000:1 Einzelauftrag][1413187684000:1 B+R-Lagerware (Zentrallager)][1413187685000:B+R-Nummer][1413187695000:B+R-Nummer][1413187706000:B+R-Nummer][1413187708000:1 Drucken+Speichern]\n"+
	"10	2014-10-13 10:09:30 [1413187770000:E Sonderpreise][1413187777000:B+R-Nummer]";
	
	/** temporäres Verzeichnis für BurSync */
	public static final File TEMP_PATH = 
			new File(System.getProperty("java.io.tmpdir"), "menulog");
	
	/**
	 * Eine ausführbare Methode mit Testdaten.
	 * @param args nicht genutzt
	 * @throws IOException bei Fehlern mit den Dateien
	 */
	public static void main(final String[] args) throws IOException {
		final String[] x = createTestFiles();
		final JFrame frame = new JFrame("Nutzung des B+R Systems");
		final Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1400,1000));
		content.add(new GrafikApplet(x[0],x[1]));
		frame.pack();
		frame.setVisible(true);
	}
		
	
	private static String[] createTestFiles() throws IOException {
		final String[] x = new String[] {"",""};
		if (!TEMP_PATH.exists()) {
			TEMP_PATH.mkdir();
		}
		final File info = new File(TEMP_PATH, "input_info.txt");
		final File session = new File(TEMP_PATH, "user_session.txt");
		if (!info.exists()) {
			info.createNewFile();
		} else {
			info.delete();
			info.createNewFile();
		}
		if (!session.exists()) {
			session.createNewFile();
		} else {
			session.delete();
			session.createNewFile();
		}
		writeTextFile(info.getAbsolutePath(), USER_DATA, false);
		writeTextFile(session.getAbsolutePath(), MORE_SESSION_DATA, false);
		
		x[0] = info.getAbsolutePath();
		x[1] = session.getAbsolutePath();
		return x;
	}
	
	 public static void writeTextFile(final String path, final String text,
	    		final boolean append) throws IOException {
	    	
	    	if ((null != path) && (null != text)) {
	    		FileOutputStream out = null;
	    		OutputStreamWriter os = null;
	    		
	    		try {
		    		out = new FileOutputStream(path, append);
					os = new OutputStreamWriter(out);
					
					final BufferedReader in = 
							new BufferedReader(new StringReader(text));
					
					final String ls = System.getProperty("line.separator");
					
					String line;
					while((line = in.readLine()) != null) {
						os.write(line);
						os.write(ls);
					}
					os.flush();
	    		} finally {
	    			close(out);
	    			close(os);
	    		}
				
	    	}
	    }
	 
	 /**
	 * Schließt <tt>out</tt> ohne Fehlermeldung, wenn es nichts zu schließen 
	 * gibt oder dabei ein Ausnahmefehler geworfen wird.
	 * @param out
	 */
	public static void close(final OutputStream out) {
		if (null != out) {
			try {
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void close(final Writer writer) {
		if (null != writer) {
			try {
				writer.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Liefert TRUE wenn <tt>str</tt> nicht NULL ist und eine getrimmte Länger 
     * größer 0 hat.
     * @param str eine Zeichenkette
     * @return TRUE wenn Zeichenkette gesetzt ist
     */
    public static synchronized boolean isNotEmpty(final String str) {
		boolean result = false;
		if (null != str) {
			if (0 < str.trim().length()) {
				result = true;
			}
		}
		return result;
	}
    
    /**
     * Liefert TRUE wenn <tt>str</tt> NULL ist oder eine getrimmte Länger 
     * kleiner gleich 0 hat.
     * @param str eine Zeichenkette
     * @return TRUE wenn Zeichenkette nicht gesetzt ist
     */
    public static synchronized boolean isEmpty(final String str) {
    	return !isNotEmpty(str);
    }
}

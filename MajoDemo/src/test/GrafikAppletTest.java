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
	
	/** temporäres Verzeichnis für BurSync */
	public static final File TEMP_PATH = 
			new File(System.getProperty("java.io.tmpdir"), "menulog");
	
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
		writeTextFile(session.getAbsolutePath(), SESSION_DATA, false);
		
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

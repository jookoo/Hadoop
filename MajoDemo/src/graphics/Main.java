package graphics;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections15.Transformer;

public class Main {
	
	private static final String FILENAME_INFO = "C:\\input_info.txt";

	private static final String FILENAME_SESSION = "C:\\user_session.txt";
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		
		// Daten
		
		 // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        // Add some vertices and edges
		final InformationCreator creator = new InformationCreator();
		
		creator.digest(FILENAME_INFO);
		creator.digest(FILENAME_SESSION);
		
		// der Vertex muss Layer haben...
		final Set<Edge> set = creator.getWeightedEdges();
		final Set<Menu> map = creator.getSizes();
		
		// Grafik
		final Graphics graphic = new Graphics(map, set);
		// Layout<V, E>, VisualizationComponent<V,E>
		final Layout<Menu, Edge> layout = 
				new FRLayout<>(graphic.getForest());
		layout.setSize(new Dimension(600,600));
		final VisualizationViewer<Menu, Edge> vv = 
				new VisualizationViewer<>(layout);
		vv.setPreferredSize(new Dimension(800,800));
		
		
		  // Transformer maps the vertex number to a vertex property
        final Transformer<String,Paint> vertexColor = new Transformer<String, Paint>() {
            @Override
			public Paint transform(final String s) {
                if(s.equals("2 Auskunft")) {
					return Color.GREEN;
				}
                return Color.RED;
            }
        };
        
        
//        final Transformer<String,Shape> vertexSize =  creator.createVertexSizeTransformer();
//        
//        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
//        vv.getRenderContext().setVertexShapeTransformer(vertexSize);

        
        final Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
            @Override
			public Paint transform(final Edge e) {
            	final Color c;
            	if (5 > e.getConnectedVertecis().size()) {
            		c = Color.RED;
            	} else {
            		c = Color.GREEN;
            	}
                return c;
            }
        };

        final Transformer<Edge, Stroke> edgeStroke = new Transformer<Edge, Stroke>() {
            float dash[] = { 10.0f };
            @Override
			public Stroke transform(final Edge e) {
                return new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            }
        };

        vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
        
		
		// Show vertex and edge labels
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//		vv.getRenderContext().setEdgeLabelTransformer(new MyEdgeDisplay());
		
		// Create a graph mouse and add it to the visualization component
		final DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm); 
		final JFrame frame = new JFrame("Nutzung des B+R Systems");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1400,1000));
		final JTabbedPane tpane = new JTabbedPane();
		final JPanel mpane = new MpunktePanel(creator.getInfos());
		final JPanel wpane = new WorkerPanel(creator.getInfos());
		final JPanel gpane = new GfxPanel(vv);
		tpane.add("Menüpunkte", mpane);
		tpane.add("Mitarbeiter", wpane);
		tpane.add("Grafik", gpane);
		frame.getContentPane().add(tpane);
		frame.pack();
		frame.setVisible(true);   
	}
	
	private static class MpunktePanel extends JPanel {
		
		public MpunktePanel(final Set<InfoLine> infos) {
			setLayout(new BorderLayout(0,0));
			final JTable table = new JTable();
			table.setAutoCreateRowSorter(true);
			table.setDefaultRenderer(
					InfoLine.class, new InfoTableCellRenderer());
			final InfoTableModel model = new InfoTableModel();
			for (final InfoLine il: infos) {
				if (!il.isWorkerLine()) {
					model.add(il);
				}
			}
			table.setModel(model);
			final JScrollPane spane = new JScrollPane();
			spane.getViewport().add(table);
			add(spane, BorderLayout.CENTER);
		}
		
	}
	
	private static class WorkerPanel extends JPanel {
		
		public WorkerPanel(final Set<InfoLine> infos) {
			setLayout(new BorderLayout(0,0));
			final JTable table = new JTable();
			table.setAutoCreateRowSorter(true);
			table.setDefaultRenderer(
					InfoLine.class, new InfoTableCellRenderer());
			final InfoTableModel model = new InfoTableModel();
			for (final InfoLine il: infos) {
				if (il.isWorkerLine()) {
					model.add(il);
				}
			}
			table.setModel(model);
			final JScrollPane spane = new JScrollPane();
			spane.getViewport().add(table);
			add(spane, BorderLayout.CENTER);
		}
		
	}
	
	private static class GfxPanel extends JPanel {
		
		public GfxPanel(final VisualizationViewer<Menu, Edge> vv) {
			setLayout(new BorderLayout(0,0));
			add(vv, BorderLayout.CENTER);
		}
		
	}
	
	private static class InfoTableModel extends DefaultTableModel {
		
		/** die Spaltendefinition */
		private static final Object[][] COLUMNS = new Object[][] {
			{"Name", String.class, 300},
			{".", String.class, 300},
			{"Anzahl.", Integer.class, 300},
		};

		/** die Daten */
		private final List<InfoLine> data = new LinkedList<>();

		/** 
		 * Ein Konstruktor. 
		 */
		public InfoTableModel() {
		}

		public void add(final InfoLine m) {
			data.add(m);
		}

		/**
		 * Liefert die Anzahl der Datensätze.
		 * @return eine Zahl, niemals <code>null</code>
		 */
		public int getSize() {
			return (null == data ? 0 : data.size());
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(final int col) {
			return (String)COLUMNS[col][0];
		}

		@Override
		public Class<?> getColumnClass(final int col) {
			return (Class<?>)COLUMNS[col][1];
		}

		@Override
		public int getRowCount() {
			return (null == data ? 0 : data.size());
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			Object value = null;
			if (data.size() > row) {
				final InfoLine x = data.get(row);
				switch (col) {
				case 0:
					final String v = x.isWorkerLine() ? x.getUser() : x.getProgram();
					value = v;
					break;
				case 1:
					final String y = x.isWorkerLine() ? "" : x.getMenu();
					value = y;
					break;
				case 2:
					value = x.getCount();
					break;
				}
			}
			return value;
		}

		@Override
		public boolean isCellEditable(final int arg0, final int arg1) {
			return false;
		}
		
	}
	
	private static class MyEdgeDisplay<V> implements Transformer<V,String> {

	    /**
	     * Retunrs v.toString()
	     */
	    @Override
		public String transform(final V v) {
	        return "";
	    }

	 }
	
	//unused
	private static class InfoTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected, final boolean hasFocus, final int row,
				final int column) {
			final Component comp = super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			if (value instanceof InfoLine) {
				final InfoLine il = (InfoLine) value;
				final JLabel label = (JLabel) comp;
				if (il.isWorkerLine()) {
					label.setText(il.getUser());
				} else {
					label.setText(il.getProgram());
				}
			}
			
			return comp;
		}
		
	}
	
}

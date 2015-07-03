package graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;
import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;
import graphics.InformationCreator.Weights;

/**
 * Die Darstellung der Ergebnisse aus den MapReduce-Jobs zur Auswertung von
 * Menülog-Dateien.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class GrafikApplet extends JApplet {

	/** das Input-Info-Job-Ergebniss */
	private static final String FILENAME_INFO = "C:\\input_info.txt";

	/** das User-Session-Job-Ergebniss */
	private static final String FILENAME_SESSION = "C:\\user_session.txt";

	/** die Grafik */
	private final Forest<Menu,Edge> graph;

	/** die Darstellung der Grafik */
	private final VisualizationViewer<Menu,Edge> vv;

	/** die Ringe zur Visualisierung der Ebenen-Tiefe */
	private final VisualizationServer.Paintable rings;

	/** ein Baumlayout */
	private final TreeLayout<Menu,Edge> treeLayout;

	/** ein radiales Baumlayout */
	private final RadialTreeLayout<Menu,Edge> radialLayout;

	/**
	 * die Methode zur Ausführung
	 * @param args
	 */
	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Nutzung des B+R Systems");
		final Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1400,1000));
		content.add(new GrafikApplet());
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * ein parameterloser Konstruktor für den Aufruf durch das Applet
	 */
	public GrafikApplet() {
		this(FILENAME_INFO,FILENAME_SESSION);
	}
	
	/**
	 * Ein Konstruktor
	 */
	public GrafikApplet(final String path_info, final String path_session) {

		// ein Daten-Verarbeitungsinstanz
		final InformationCreator creator = new InformationCreator();

		// Eingabe der Dateien
		creator.digest(path_info);
		creator.digest(path_session);

		// Ausgabe der verarbeiteten Dateien
		final Set<Edge> set = creator.getWeightedEdges();
		final Set<Menu> map = creator.getSizedMenues();

		// Grafik
		final MyTreeBuilder graphic = new MyTreeBuilder(map, set);
		graph = graphic.getForest();
		treeLayout = new TreeLayout<Menu,Edge>(graph);
		radialLayout = new RadialTreeLayout<Menu,Edge>(graph);
		radialLayout.setSize(new Dimension(1000,1000));
		vv =  new VisualizationViewer<Menu,Edge>(treeLayout, new Dimension(1000,1000));
		rings = new Rings();
		final Container content = getContentPane();
		final JTabbedPane tpane = new JTabbedPane();
		final JPanel mpane = new MpunktePanel(creator.getInfos());
		final JPanel wpane = new WorkerPanel(creator.getInfos());
		final JPanel gpane = new GfxPanel(vv,rings,treeLayout,radialLayout);
		tpane.add("Menüpunkte", mpane);
		tpane.add("Mitarbeiter", wpane);
		tpane.add("Grafik", gpane);
		content.add(tpane);
	}

	/**
	 * Repräsentiert die Ringe die die Ebenentiefe des Menüpunkts wiederspiegeln.
	 */
	private class Rings implements VisualizationServer.Paintable {

		/** tiefe des Rings */
		private final Collection<Double> depths;

		/**
		 *  ein Konstruktor
		 */
		public Rings() {
			depths = getDepths();
		}

		private Collection<Double> getDepths() {
			final Set<Double> depths = new HashSet<Double>();
			final Map<Menu,PolarPoint> polarLocations = radialLayout.getPolarLocations();
			for(final Menu v : graph.getVertices()) {
				final PolarPoint pp = polarLocations.get(v);
				depths.add((null == pp ? 0D : pp.getRadius()));
			}
			return depths;
		}

		@Override
		public void paint(final Graphics g) {
			g.setColor(Color.lightGray);

			final Graphics2D g2d = (Graphics2D)g;
			final Point2D center = radialLayout.getCenter();

			final Ellipse2D ellipse = new Ellipse2D.Double();
			for(final double d : depths) {
				ellipse.setFrameFromDiagonal(center.getX()-d, center.getY()-d, 
						center.getX()+d, center.getY()+d);
				final Shape shape = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).transform(ellipse);
				g2d.draw(shape);
			}
		}

		@Override
		public boolean useTransform() {
			return true;
		}
	}

	/**
	 * Die anzeigende Komponente für die Tabelle mit Menüpunkten
	 */
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
			final JPanel soutp = new JPanel();
			soutp.add(new JLabel("#Menüpunkte "+ model.getSize()));
			add(soutp, BorderLayout.SOUTH);
		}

	}

	/**
	 * Die anzeigende Komponente für die Tabelle mit Mitarbeitern
	 */
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
			final JPanel soutp = new JPanel();
			soutp.add(new JLabel("#Mitarbeiter "+ model.getSize()));
			add(soutp, BorderLayout.SOUTH);
		}

	}

	/**
	 * Die anzeigende Komponente für die Grafik
	 */
	private static class GfxPanel extends JPanel {

		public GfxPanel(final VisualizationViewer<Menu, Edge> vv,
				final VisualizationServer.Paintable rings,
				final TreeLayout<Menu,Edge> treeLayout,
				final RadialTreeLayout<Menu,Edge> radialLayout) {
			setLayout(new BorderLayout(0,0));
			// Hintergrund
			vv.setBackground(Color.white);
			
			// Label
			vv.getRenderContext().setVertexLabelTransformer(new Transformer<Menu, String>() {
	            @Override
				public String transform(final Menu e) {
	                return (e.getName());
	            }
	        });
			// Darstellung
			final Transformer<Menu,Paint> vertexColor = new Transformer<Menu, Paint>() {
				@Override
				public Paint transform(final Menu m) {
					if(m.isStartpage()) {
						return Color.BLACK;
					} else {
						if (m.getWeight() < 100) {
							return Color.RED;
						} else {
							return Color.GREEN;
						}
					}
				}
			};
			// Größe der Vertices anhand der absoluten Anzahl der Nutzungen setzen
			final Transformer<Menu,Shape> vertexSize = new Transformer<Menu, Shape>(){
				@Override
				public Shape transform(final Menu m){
					final Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
					final Weights w = m.getStagedSize();
					return AffineTransform.getScaleInstance(w.getSize(), w.getSize()).createTransformedShape(circle);
				}
			};
			vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
			vv.getRenderContext().setVertexShapeTransformer(vertexSize);
			
			// Kanten anhand der prozentualen Nutzung vergrößern
			final Transformer<Edge, Stroke> edgeStroke = new Transformer<Edge, Stroke>() {
				@Override
				public Stroke transform(final Edge e) {
					return new BasicStroke(e.getThickness());
				}
			};
			// Kanten als gerade Linien
			vv.getRenderContext().setEdgeShapeTransformer(
					new EdgeShape.Line<Menu,Edge>());

			// Kanten anhand der absoluten Anzahl von Nutzungen einfärben
			final Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
				@Override
				public Paint transform(final Edge e) {
					final Color c;
					if (50 < e.getWeight()) {
						c = Color.GREEN;
					} else {
						c = Color.RED;
					}
					return c;
				}
			};
			vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
			vv.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
			// Tooltips
			vv.setVertexToolTipTransformer(new Transformer<Menu, String>(){
			    @Override
				public String transform(final Menu m) {
			        return String.format("Prozentualer Anteil: %.2f ",m.getSize());
			    }
			});
			vv.setEdgeToolTipTransformer(new Transformer<Edge,String>(){
			    @Override
				public String transform(final Edge e) {
			        return "Edge:"+e.getWeight();
			    }
			});
			vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.CYAN));

			// Panel mit Grafik aufbauen
			final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
			add(panel, BorderLayout.CENTER);

			// Scalierung des Graphen ermöglichen
			final ScalingControl scaler = new CrossoverScalingControl();

			// Der Button + 
			final JButton plus = new JButton("+");
			plus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					scaler.scale(vv, 1.1f, vv.getCenter());
				}
			});
			// Der Button -
			final JButton minus = new JButton("-");
			minus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					scaler.scale(vv, 1/1.1f, vv.getCenter());
				}
			});

			// Der Buutton um zwischen Tree-Layout und Radial-Tree-Layout zu wechseln
			final JToggleButton radial = new JToggleButton("Radial");
			radial.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {

						final LayoutTransition<Menu,Edge> lt =
								new LayoutTransition<Menu,Edge>(vv, treeLayout, radialLayout);
						final Animator animator = new Animator(lt);
						animator.start();
						vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
						vv.addPreRenderPaintable(rings);
					} else {
						final LayoutTransition<Menu,Edge> lt =
								new LayoutTransition<Menu,Edge>(vv, radialLayout, treeLayout);
						final Animator animator = new Animator(lt);
						animator.start();
						vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
						vv.removePreRenderPaintable(rings);
					}
					vv.repaint();
				}});

			// Mausverhalten instaziieren und festelegen
			final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
			vv.setGraphMouse(graphMouse);
			final JComboBox modeBox = graphMouse.getModeComboBox();
			modeBox.addItemListener(graphMouse.getModeListener());
			graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);  

			final JPanel scaleGrid = new JPanel(new GridLayout(1,0));
			scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
			final JPanel controls = new JPanel();
			scaleGrid.add(plus);
			scaleGrid.add(minus);
			controls.add(radial);
			controls.add(scaleGrid);
			controls.add(modeBox);
			add(controls, BorderLayout.SOUTH);
		}

	}

	/**
	 * Ein Tabellen-Model für die Anzeige von Info-Zeilen
	 */
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

		/**
		 * Fügt der Tabelle eine Zeile hinzu
		 * @param m eine {@link InfoLine}
		 */
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
					final String v = x.isWorkerLine() ? x.getUser() : x.getMenu();
					value = v;
					break;
				case 1:
					final String y = x.isWorkerLine() ? x.tranlatedName(): x.getProgram();
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

	/**
	 * Alternativer Renderer für InfoTable-Zellen. 
	 */
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

/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.nodes.graphrenderer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.knime.core.node.NodeLogger;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import chemaxon.marvin.beans.MViewPane;

import org.erlwood.knime.nodes.graphrenderer.GraphRendererNodeView.DoubleMatrixOnBufferedDataTable;

/**
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * 
 * Code retrieved and adapted from http://prefuse.org/gallery/graphview/GraphView.java
 */
@SuppressWarnings("all")
public class GraphView extends JPanel implements ActionListener {
	private static final NodeLogger LOG = NodeLogger.getLogger(GraphView.class);
	
	private int numnodes = -1;
	private static final String GRAPH = "graph";
	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";
	private Visualization vis = null;
	private Graph g = null;
	private ForceSimulator fsim = null;
	private Display display = null;
	private Random random = new Random();
	private ActionList animate = null;
	private ActionList draw = null;
	private boolean animRunning = true;
	private Map edgecolors = null, edgelengths = null, nodesmiles = null,
			nodeprops = null, nodecolors = null;
	private List nodeorder = null;
	private MViewPane molview = null, moltable = null;
	private DoubleMatrixOnBufferedDataTable emat = null;

	public GraphView(int nnodes, DoubleMatrixOnBufferedDataTable edgemat) {
		super();
		nodesmiles = new HashMap();
		nodecolors = new HashMap();
		nodeprops = new HashMap();
		edgecolors = new HashMap();
		edgelengths = new HashMap();
		nodeorder = new ArrayList();
		UILib.setPlatformLookAndFeel();
		numnodes = nnodes;
		emat = edgemat;
		demo();
		JComponent graphview = getJpanel(display);
		add(graphview);
	}

	private StringBufferInputStream graphInputStream() {
		String graphstr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		graphstr += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">";
		graphstr += "<graph edgedefault=\"undirected\">";
		graphstr += "<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>";
		graphstr += "<key id=\"color\" for=\"edge\" attr.name=\"color\" attr.type=\"integer\"/>";
		graphstr += "<node id=\"1\">";
		graphstr += "<data key=\"name\">Jeff</data>";
		graphstr += "</node>";
		graphstr += "<node id=\"2\">";
		graphstr += "<data key=\"name\">Ed</data>";
		graphstr += "</node>";
		graphstr += "<edge source=\"1\" target=\"2\"></edge>";
		graphstr += "</graph>";
		graphstr += "</graphml>";
		return new StringBufferInputStream(graphstr);
	}

	public void demo() {
		String label = "name";
		try {
			g = new GraphMLReader().readGraph(graphInputStream());
		} catch (Exception e) {
			LOG.debug("cant read graph " + e);
			return;
		}
		g.clear();

		// create a new, empty visualization for our data
		Node n = g.addNode();
		n.set("name", "dummy");
		if (vis == null) {
			vis = new Visualization();
		}
		VisualGraph vg = vis.addGraph(GRAPH, g);
		vis.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE);

		TupleSet focusGroup = vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i) {
					((VisualItem) rem[i]).setFixed(false);
				}
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				vis.run("draw");
			}
		});

		// set up the renderers
		LabelRenderer tr = new LabelRenderer(label) {
			protected String getText(VisualItem ve) {
				if (ve.get("name").equals("dummy")) {
					return "";
				}
				return super.getText(ve);
			}
		};
		tr.setRoundedCorner(8, 8);
		tr.setVerticalPadding(0);
		tr.setHorizontalPadding(0);
		EdgeRenderer er = new EdgeRenderer() {
			protected double getLineWidth(VisualItem ve) {
				EdgeItem e = (EdgeItem) ve;
				if (e.getSourceItem().get("name").equals("dummy")) {
					return 0;
				}
				if (e.getTargetItem().get("name").equals("dummy")) {
					return 0;
				}
				return 2;
			}
		};
		vis.setRendererFactory(new DefaultRendererFactory(tr, er));

		// -- set up the actions ----------------------------------------------

		int maxhops = 4, hops = 4;
		final GraphDistanceFilter filter = new GraphDistanceFilter(GRAPH, hops);

		draw = new ActionList();
		draw.add(filter);
		draw.add(new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.rgb(0,
				0, 0)));
		draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR, 0));
		draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0,
				0, 0)));
		draw.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib
				.gray(255)));
		draw.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib
				.gray(200)) {
			public int getColor(VisualItem ve) {
				EdgeItem e = (EdgeItem) ve;
				String ek = e.getSourceItem().get("name") + ":"
						+ e.getTargetItem().get("name");

				try {

					return e.getInt("color");
				} catch (Exception ex) {
					LOG.debug("Cant get edgecolors for " + ek);
					LOG.error(ex.getMessage(), ex);
				}
				return getDefaultColor();
			}
		});

		ColorAction fill = new ColorAction(NODES, VisualItem.FILLCOLOR,
				ColorLib.gray(200)) {
			public int getColor(VisualItem ve) {
				int oc = super.getColor(ve);
				if (oc != getDefaultColor())
					return oc;
				try {
					int retval = ((Integer) nodecolors.get(ve.get("name")))
							.intValue();
					return retval;
				} catch (Exception e) {
					if (!ve.get("name").equals("dummy")) {
						LOG.debug("getColor error ! " + ve.get("name"));
					}
				}
				return oc;
			}
		};
		fill.add("_fixed", ColorLib.gray(125)); 
		fill.add("_highlight", ColorLib.gray(255));
		draw.add(fill);
		draw.add(new RepaintAction());

		ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH) {
			protected float getSpringLength(EdgeItem e) {
				String ek = e.getSourceItem().get("name") + ":"
						+ e.getTargetItem().get("name");
				try {
					return ((Float) edgelengths.get(ek)).floatValue();
				} catch (Exception ex) {
					LOG.debug("Cant get edgelengths for " + ek);
				}
				return super.getSpringLength(e);
			}

			protected float getSpringCoefficient(EdgeItem e) {
				if (e.getSourceItem().get("name").equals("dummy")) {
					return -1e-10f;
				}
				if (e.getTargetItem().get("name").equals("dummy")) {
					return -1e-10f;
				}
				return -1.0f;
			}
		};
		fsim = fdl.getForceSimulator();

		fsim.getForces()[0].setParameter(0, -10.0f);

		animate = new ActionList(Activity.INFINITY);
		animate.add(fdl);
		animate.add(fill);
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		vis.putAction("draw", draw);
		vis.putAction("layout", animate);
		vis.runAfter("draw", "layout");

		// --------------------------------------------------------------------
		// STEP 4: set up a display to show the visualization

		display = new Display(vis);
		display.setSize(500, 500);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// main display controls

		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		display.addControlListener(new HoverActionControl("hi") {
			public void itemEntered(VisualItem ve, MouseEvent me) {
				if (ve.canGet("name", GRAPH.getClass())) {
					molview.setM(0, (String) nodesmiles.get(ve.get("name")));
					molview.setL(0, (String) ve.get("name") + ":::"
							+ (Double) nodeprops.get(ve.get("name")));
				}
			}
		});
		display.addControlListener(new FocusControl(1) {
			public void itemClicked(VisualItem ve, MouseEvent me) {
				if (ve.canGet("name", GRAPH.getClass())
						&& me.getModifiers() == me.CTRL_MASK + me.BUTTON1_MASK) {
					return;
				}
				if (ve.canGet("name", GRAPH.getClass())
						&& !ve.get("name").equals("dummy")) {
					onclick("" + ve.get("name"));
				}
				super.itemClicked(ve, me);
			}
		});

		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// position and fix the default focus node
		NodeItem focus = (NodeItem) vg.getNode(0);
		PrefuseLib.setX(focus, null, 250);
		PrefuseLib.setY(focus, null, 250);
		focusGroup.setTuple(focus);
	}

	void highlightMCSS(int cell, List inds, String smiles) {

		for (int k = 0; k < inds.size(); k++) {
			moltable.setAtomSetSeq(cell, ((Integer) inds.get(k)).intValue(), 2);
		}

	}

	void onclick(String nodename) {

		List nodesorted = new ArrayList();
		int nodeindex = nodeorder.indexOf(nodename);
		Object temp = null;
		for (int i = 0; i < nodeprops.size(); i++) {
			nodesorted.add(new Integer(i));
		}
		for (int i = 0; i < nodesorted.size(); i++) {
			for (int k = i + 1; k < nodesorted.size(); k++) {
				int ii = ((Integer) nodesorted.get(i)).intValue();
				int kk = ((Integer) nodesorted.get(k)).intValue();
				if (emat.get(nodeindex, kk) < emat.get(nodeindex, ii)) {
					temp = nodesorted.get(i);
					nodesorted.set(i, nodesorted.get(k));
					nodesorted.set(k, temp);
				}
			}
		}
		for (int i = 0; i < nodesorted.size(); i++) {
			int si = ((Integer) nodesorted.get(i)).intValue();
			String nn = (String) nodeorder.get(si);
			String sm = (String) nodesmiles.get(nn);

			moltable.setM(2 * i, (String) nodesmiles.get(nodename));
			moltable.setM(2 * i + 1, sm);
			highlightMCSS(2 * i, emat.getcorr(si, nodeindex),
					(String) nodesmiles.get(nodename));
			highlightMCSS(2 * i + 1, emat.getcorr(nodeindex, si), sm);
			moltable.setL(7 + 5 * i, nn);
			// sim
			moltable.setL(7 + 5 * i + 1,
					String.format("%6.3f", emat.get(nodeindex, si)));
			// prop
			moltable.setL(7 + 5 * i + 2,
					String.format("%6.3f", nodeprops.get(nn))); 
			moltable.setL(7 + 5 * i + 3, "111");
			double cliff = -999;
			try {
				cliff = Math.abs((Double) nodeprops.get(nodename)
						- (Double) nodeprops.get(nn))
						/ emat.get(nodeindex, si);

			} catch (Exception e) {
			}
			
			moltable.setL(7 + 5 * i + 3, "" + String.format("%6.3f", cliff));
			// table index
			moltable.setL(7 + 5 * i + 4, "" + i); 

		}

	}

	public JComponent getJpanel(Display ds) {
		if (ds == null) {
			return ds;
		}
		JPanel fpanel = new JPanel();
		if (fsim != null) {
			fpanel = new JForcePanel(fsim);
			fpanel.add(Box.createVerticalGlue());
		}

		JButton animtoggle = new JButton("Start/Stop Animation");
		animtoggle.addActionListener(this);
		molview = new MViewPane();
		molview.setParams("rows=1\ncols=1\nlayout=:2:1:M:1:0:1:1:L:0:0:1:1:c:n:1:1\nparam=:M:200:190:L:10b\n");
		JSplitPane menusplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				molview, animtoggle);
		molview.setM(0, "");
		molview.setL(0, "No molecule");

		JSplitPane vsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fpanel,
				menusplit);

		JSplitPane split = new JSplitPane();
		if (ds != null) {
			split.setLeftComponent(ds);
		}
		split.setRightComponent(vsplit);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(200);
		split.setDividerLocation(500);

		moltable = new MViewPane();
		// mol, mol, name, similarity, property, cliff, focus
		moltable.setParams("rows="
				+ (numnodes + 1)
				+ "\ncols=1\nlayout=:1:6:M:0:0:1:1:M:0:1:1:1:L:0:2:1:1:L:0:3:1:1:L:0:4:1:1:L:0:5:1:1:L:0:6:1:1\nparam=:M:100:100:M:100:100:L:10b:L:10b:L:10b:L:10b:L:10b\n");
		moltable.setParams("layoutH=:1:6:L:0:0:1:1:L:0:1:1:1:L:0:2:1:1:L:0:3:1:1:L:0:4:1:1:L:0:5:1:1:L:0:6:1:1\nparamH=:L:10:L:10:L:10b:L:10b:L:10b:L:10b:L:10b\n");
		moltable.setParams("scroll=1\nborder=2\nvisibleRows=5\n");
		moltable.setParams("atomNumbersVisible=false\n");
		String spacer = "        ";
		moltable.setL(0, spacer + "Focus MCSS" + spacer);
		moltable.setL(1, spacer + "Current MCSS" + spacer);
		moltable.setL(2, spacer + "Name" + spacer);
		moltable.setL(3, spacer + "Distance" + spacer);
		moltable.setL(4, spacer + "Property" + spacer);
		moltable.setL(5, spacer + "Cliff" + spacer);
		moltable.setL(6, spacer + "TableIndex" + spacer);
		for (int i = 0; i < numnodes; i++) {
			moltable.setM(2 * i, "C=C");
			moltable.setM(2 * i + 1, "N-C");
			moltable.setL(7 + 5 * i, i + " : " + numnodes);
		}
		moltable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent evt) {

			}
		});
		JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split,
				moltable);
		return split1;
	}

	public void toggleAnim() {
		actionPerformed(null);
	}

	public void actionPerformed(ActionEvent e) {
		if (animRunning) {
			haltAnim();
			animRunning = false;
		} else {
			startAnim();
			animRunning = true;
		}
	}

	public void haltAnim() {
		vis.removeAction("layout");
		vis.removeAction("draw");
		vis.putAction("draw", draw);

	}

	public void startAnim() {
		vis.removeAction("layout");
		vis.removeAction("draw");
		vis.putAction("draw", draw);
		vis.putAction("layout", animate);
		vis.runAfter("draw", "layout");
		vis.run("layout");
		vis.run("draw");
	}

	public void addNode(String name, int nc, String smiles, double prop) {
		if (getNodeFromName(name) != null)
			return;
		Node n = g.addNode();
		n.set("name", name);
		n.get("name");
		nodeorder.add(name);
		int red = 0;
		if (nc > 50) {
			red = (nc - 50) * 2;
		}
		int white = 100 - Math.abs(nc - 50) * 2;
		int blue = 0;
		if (nc < 50) {
			blue = Math.abs(50 - nc) * 2;
		}
		nodecolors.put(n.get("name"),
				new Integer(ColorLib.rgb(150 + red, 150 + blue, 150 + white)));
		nodesmiles.put(n.get("name"), smiles);
		nodeprops.put(n.get("name"), new Double(prop));

		vis.run("draw");

	}

	public void addEdge(String name1, String name2, int ec, double el) {
		Node n1 = getNodeFromName(name1);
		Node n2 = getNodeFromName(name2);
		if (!name1.equals("dummy") && !name2.equals("dummy")) {
			double wt = emat.get(
					((Integer) nodeorder.indexOf(name1)).intValue(),
					((Integer) nodeorder.indexOf(name2)).intValue());
		}
		if (n1 != null && n2 != null) {
			if (g.getEdge(n1, n2) == null) {
				g.addEdge(n1, n2);
				Integer col = new Integer(ColorLib.gray(200));
				if (ec < 0) {
					col = new Integer(ColorLib.gray(255));
				} else if (ec > 0) {
					col = new Integer(ColorLib.rgb(0, 1000, 0));
				}
				edgecolors.put(n1.get("name") + ":" + n2.get("name"), col);
				edgecolors.put(n2.get("name") + ":" + n1.get("name"), col);
				edgelengths.put(n1.get("name") + ":" + n2.get("name"),
						new Float(el));
				edgelengths.put(n2.get("name") + ":" + n1.get("name"),
						new Float(el));
				g.getEdge(n1, n2).setInt("color", col);
			}
		}
		vis.run("draw");
	}

	public Node getNodeFromName(String name) {
		Iterator gi = g.nodes();
		while (gi.hasNext()) {
			Node n = (Node) gi.next();

			if (name.equals(n.get("name"))) {

				return n;
			}
		}
		return null;
	}

} // end of class GraphView

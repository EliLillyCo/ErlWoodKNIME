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
package org.erlwood.knime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.erlwood.knime.utils.iotiming.IOTiming;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.Node;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.workflow.ConnectionContainer;
import org.knime.core.node.workflow.NativeNodeContainer;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.node.workflow.NodeStateChangeListener;
import org.knime.core.node.workflow.NodeStateEvent;
import org.knime.core.node.workflow.WorkflowEvent;
import org.knime.core.node.workflow.WorkflowListener;
import org.knime.core.node.workflow.WorkflowManager;

public class ErlwoodNodeStateChangeListener implements NodeStateChangeListener {
	private static final ErlwoodNodeStateChangeListener INSTANCE = new ErlwoodNodeStateChangeListener();
    private static final NodeLogger LOG = NodeLogger.getLogger(ErlwoodNodeStateChangeListener.class.getName());
    
    private final WorkflowManager                                             	workflowManager;
    private final Map<NodeID, ErlwoodNodeStateChangeListener>                  	listenerMap       = new LinkedHashMap<NodeID, ErlwoodNodeStateChangeListener>();
    private final NodeAddRemoveListener                                       	addRemoveListener = new NodeAddRemoveListener();
	private static final List<NodeExecutionListener> 							nodeExecutionListeners = new ArrayList<NodeExecutionListener>();

    private static final Map<WorkflowName, Map<NodeID, NodeExecutionDetails>> 	CURRENT_EXECUTIONS = new TreeMap<WorkflowName, Map<NodeID, NodeExecutionDetails>>();
    
    private static final Map<NodeContainer, Long>      							NODE_CONTAINER_START	=	new WeakHashMap<NodeContainer, Long>();
    

    public static ErlwoodNodeStateChangeListener getInstance() {
    	return INSTANCE;
    }
    
    private ErlwoodNodeStateChangeListener() {
		this(WorkflowManager.ROOT);		
	}

    private ErlwoodNodeStateChangeListener(final WorkflowManager wfm) {

        this.workflowManager = wfm;

        for (NodeContainer nc : workflowManager.getNodeContainers()) {
            nodeAdded(nc);
        }
        workflowManager.addListener(addRemoveListener);

    }

    public void unregister() {
        for (NodeContainer nc : WorkflowManager.ROOT.getNodeContainers()) {
            nodeRemoved(nc);
        }
        workflowManager.removeListener(addRemoveListener);
    }

    private void nodeAdded(final NodeContainer nc) {
        if (nc instanceof WorkflowManager) {
            WorkflowManager child = (WorkflowManager) nc;
            ErlwoodNodeStateChangeListener childListener = new ErlwoodNodeStateChangeListener(child);
            listenerMap.put(child.getID(), childListener);
        }
        nc.addNodeStateChangeListener(this);
    }

    private void nodeRemoved(final NodeContainer nc) {
        if (nc instanceof WorkflowManager) {
            WorkflowManager child = (WorkflowManager) nc;
            ErlwoodNodeStateChangeListener childListener = listenerMap.remove(child.getID());
            if (childListener != null) {
                childListener.unregister();
            }
        }

        CURRENT_EXECUTIONS.remove(new WorkflowName(nc.getParent().getID(), nc.getParent().getDisplayLabel()));

        nc.removeNodeStateChangeListener(this);
    }

    @Override
    public void stateChanged(NodeStateEvent state) {
        NodeContainer nc = workflowManager.getNodeContainer(state.getSource());

        if (nc instanceof NativeNodeContainer) {
        	NativeNodeContainer nnc = (NativeNodeContainer)nc;        	
        	        	
            WorkflowName wfn = new WorkflowName(nc.getParent().getID(), nc.getParent().getDisplayLabel());

            Map<NodeID, NodeExecutionDetails> nodeMap = CURRENT_EXECUTIONS.get(wfn);

            if (nc.getNodeContainerState().isExecutionInProgress()) {
            	if (nc.getState().equals(NodeContainer.State.PREEXECUTE)) {
            		NODE_CONTAINER_START.put(nc,  System.currentTimeMillis());            		
            	}            	
        		
                if (nodeMap == null) {
                    nodeMap = new TreeMap<NodeID, NodeExecutionDetails>();
                    CURRENT_EXECUTIONS.put(wfn, nodeMap);
                }

                NodeExecutionDetails ned = nodeMap.get(nc.getID());
                if (ned == null) {
                    ned = new NodeExecutionDetails(nc.getDisplayLabel());
                    nodeMap.put(nc.getID(), ned);
                }
                ned.setState(nc.getNodeContainerState().toString());
                ned.setTime(System.currentTimeMillis());
            } else {
                if (nodeMap != null) {
                    nodeMap.remove(nc.getID());
                    if (nodeMap.isEmpty()) {
                        CURRENT_EXECUTIONS.remove(wfn);
                    }
                }
                
                if (nc.getNodeContainerState().isExecuted()) {
                
                	long executionTime = 0;
                	Long startTime = NODE_CONTAINER_START.get(nc);
                	if (startTime != null) {
                		executionTime = System.currentTimeMillis() - startTime;
                		
						Node n = ((NativeNodeContainer) nc).getNode();
						NodeModel nm = n.getNodeModel();
						long ioTiming = 0;
						if (nm instanceof IOTiming) {
							ioTiming = ((IOTiming)nm).getTiming();
						}
						
					
						for (NodeExecutionListener l : nodeExecutionListeners) {
							l.nodeExecuted(n.getName(), nc.getID(), executionTime, ioTiming);
						}
						
                	}
                
                	int rowCount = -1;
                	
                    ConnectionContainer cc = nnc.getParent().getIncomingConnectionFor(nnc.getID(), 1);
                	if (cc != null) {
                		try {
		                	PortObject po = nnc.getParent().getNodeContainer(cc.getSource()).getOutPort(cc.getSourcePort()).getPortObject();
		                	
		                	if (po != null && po instanceof BufferedDataTable) {
		                		rowCount = ((BufferedDataTable)po).getRowCount();
		                	}
                		} catch(Exception ex) {
                			//	Do nothing
                		}
                	}

                } 
            }

        }

    }

    private final class NodeAddRemoveListener implements WorkflowListener {

        /** {@inheritDoc} */
        @Override
        public void workflowChanged(final WorkflowEvent event) {
            switch (event.getType()) {
            case NODE_ADDED:
                NodeContainer newNC = (NodeContainer) event.getNewValue();
                ErlwoodNodeStateChangeListener.this.nodeAdded(newNC);
                break;
            case NODE_REMOVED:
                NodeContainer oldNC = (NodeContainer) event.getOldValue();
                ErlwoodNodeStateChangeListener.this.nodeRemoved(oldNC);
                break;
            default:
                // ignore
            }
            
        }

    }

    private static final class WorkflowName implements Comparable<WorkflowName> {
        private final NodeID id;
        private final String name;

        private WorkflowName(NodeID id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public int compareTo(WorkflowName o) {
            return id.compareTo(o.id);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            return sb.toString();
        }
    }

    private static final class NodeExecutionDetails {
        private final String name;
        private String       state;
        private long         time;

        private NodeExecutionDetails(String name) {
            this.name = name;
        }

        public void setTime(long t) {
            time = t;
        }

        public void setState(String s) {
            state = s;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name + "\t" + state + "\t" + (System.currentTimeMillis() - time));
            return sb.toString();
        }
    }
       
    public interface NodeExecutionListener {

		void nodeExecuted(String name, NodeID id, long executionTime, long ioTiming);
    	
    }

	public void addNodeExecutionListener(NodeExecutionListener listener) {
		nodeExecutionListeners.add(listener);
	}

	public void removeNodeExecutionListener(NodeExecutionListener listener) {
		nodeExecutionListeners.remove(listener);
	}
}

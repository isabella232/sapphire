/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.diagram.def.ConnectionServiceType;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePostAddEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePreDeleteEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.NodeTemplateVisibilityEvent;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.internal.DiagramConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.internal.DiagramConnectionTemplate.DiagramConnectionTemplateListener;
import org.eclipse.sapphire.ui.diagram.internal.DiagramEmbeddedConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.internal.DiagramImplicitConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.internal.DiagramImplicitConnectionTemplate.DiagramImplicitConnectionTemplateListener;
import org.eclipse.sapphire.ui.diagram.internal.StandardImplicitConnectionPart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class StandardConnectionService extends ConnectionService 
{
    private SapphireDiagramEditorPagePart diagramPagePart;
    private DiagramEditorPageDef diagramPageDef;
    private List<IDiagramConnectionDef> connectionDefs;
    private Map<DiagramNodeTemplate, DiagramEmbeddedConnectionTemplate> embeddedConnectionTemplateMap;
    private List<DiagramConnectionTemplate> connectionTemplates;
    private List<DiagramImplicitConnectionTemplate> implicitConnectionTemplates;
    private ConnectionTemplateListener connTemplateListener;
    private ImplicitConnectionTemplateListener implicitConnTemplateListener;
    private Listener diagramNodeListener;
    private Listener diagramNodeTemplateListener;
    
    @Override
    protected void init()
    {
        this.diagramPagePart = context(SapphireDiagramEditorPagePart.class);
        this.diagramPageDef = (DiagramEditorPageDef)this.diagramPagePart.getPageDef();
        this.connectionDefs = this.diagramPageDef.getDiagramConnectionDefs();
        this.embeddedConnectionTemplateMap = new HashMap<DiagramNodeTemplate, DiagramEmbeddedConnectionTemplate>();
        this.connTemplateListener = new ConnectionTemplateListener();
        this.implicitConnTemplateListener = new ImplicitConnectionTemplateListener();
        
        // Need to initialize the embedded connections after all the diagram node templates are initialized.
        // For connections between "anonymous" nodes, we'd represent connections using node index based
        // mechanism.
        for (DiagramNodeTemplate nodeTemplate : this.diagramPagePart.getNodeTemplates())
        {
            nodeTemplate.initEmbeddedConnections();
            if (nodeTemplate.getEmbeddedConnectionTemplate() != null)
            {
                nodeTemplate.getEmbeddedConnectionTemplate().addTemplateListener(this.connTemplateListener);
                this.embeddedConnectionTemplateMap.put(nodeTemplate, nodeTemplate.getEmbeddedConnectionTemplate());
            }
        }
        
        // Initialize connection templates
        this.connectionTemplates = new ArrayList<DiagramConnectionTemplate>();
        ElementList<IDiagramExplicitConnectionBindingDef> connectionBindings = this.diagramPageDef.getDiagramConnectionBindingDefs();
        for (IDiagramExplicitConnectionBindingDef connBinding : connectionBindings)
        {
            IDiagramConnectionDef connDef = getDiagramConnectionDef(connBinding.getConnectionId().content());
            DiagramConnectionTemplate connectionTemplate = new DiagramConnectionTemplate(connBinding);
            connectionTemplate.init(this.diagramPagePart, this.diagramPagePart.getLocalModelElement(), 
                    connDef, Collections.<String,String>emptyMap());
            connectionTemplate.initialize();
            this.connectionTemplates.add(connectionTemplate);
            connectionTemplate.addTemplateListener(this.connTemplateListener);
        }

        // initialize implicit connections
        this.implicitConnectionTemplates = new ArrayList<DiagramImplicitConnectionTemplate>();
        ElementList<IDiagramImplicitConnectionBindingDef> implicitConnBindings = this.diagramPageDef.getImplicitConnectionBindingDefs();
        for (IDiagramImplicitConnectionBindingDef implicitConnBinding : implicitConnBindings)
        {
            IDiagramConnectionDef connDef = getDiagramConnectionDef(implicitConnBinding.getConnectionId().content());
            DiagramImplicitConnectionTemplate connectionTemplate = new DiagramImplicitConnectionTemplate(implicitConnBinding);
            connectionTemplate.init(this.diagramPagePart, this.diagramPagePart.getLocalModelElement(), 
                    connDef, Collections.<String,String>emptyMap());
            connectionTemplate.initialize();
            this.implicitConnectionTemplates.add(connectionTemplate);
            connectionTemplate.addTemplateListener(this.implicitConnTemplateListener);
        }
        
        // Listen to "node about to be deleted" event to remove the connection parent element for 1 x n
        // connection type
        this.diagramNodeListener = new FilteredListener<DiagramNodeEvent>() 
        {
            @Override
            protected void handleTypedEvent(DiagramNodeEvent event) 
            {
                if (event instanceof DiagramNodePreDeleteEvent)
                {
                    handleNodeAboutToBeDeleted(event.part());
                }
                else if (event instanceof DiagramNodePostAddEvent)
                {
                    refreshAttachedConnections(event.part());
                }
            }
        };
        this.diagramNodeTemplateListener = new FilteredListener<NodeTemplateVisibilityEvent>() 
        {
            @Override
            protected void handleTypedEvent(NodeTemplateVisibilityEvent event) 
            {
                if (event.getNodeTemplate().visible())
                {
                    showAllAttachedConnections(event.getNodeTemplate());
                }
                else
                {
                    hideAllAttachedConnections(event.getNodeTemplate());
                }
            }
        };
        this.diagramPagePart.attach(this.diagramNodeListener);
        this.diagramPagePart.attach(this.diagramNodeTemplateListener);
        
    }

    @Override
    public boolean valid(DiagramNodePart srcNode, DiagramNodePart targetNode, String connectionType) 
    {
        DiagramConnectionTemplate connectionTemplate = getConnectionTemplate(srcNode, connectionType);
        if (connectionTemplate != null)
        {
            return connectionTemplate.canCreateNewConnection(srcNode, targetNode);
        }
        return false;
    }

    @Override
    public DiagramConnectionPart connect(DiagramNodePart srcNode, DiagramNodePart targetNode, String connectionType) 
    {
        DiagramConnectionTemplate connectionTemplate = getConnectionTemplate(srcNode, connectionType);
        if (connectionTemplate != null)
        {
            DiagramConnectionPart connection = connectionTemplate.createNewDiagramConnection(srcNode, targetNode);
            return connection;
        }
        return null;
    }
        
    @Override
    public List<DiagramConnectionPart> list()
    {
        final ListFactory<DiagramConnectionPart> connections = ListFactory.start();
        
        for( DiagramConnectionTemplate template : getAllConnectionTemplates() )
        {
            for (DiagramConnectionPart connPart : template.getDiagramConnections(null))
            {
                connections.add(connPart);
            }
        }
        
        for (DiagramConnectionTemplate embeddedConnectionTemplate : this.embeddedConnectionTemplateMap.values())
        {
            for (DiagramConnectionPart connPart : embeddedConnectionTemplate.getDiagramConnections(null))
            {
                connections.add(connPart);
            }
        }
        for( DiagramImplicitConnectionTemplate template : this.implicitConnectionTemplates )
        {
            for (StandardImplicitConnectionPart connPart : template.getImplicitConnections())
            {
                connections.add( connPart );
            }
        }

        return connections.result();
    }
                    
    private void showAllAttachedConnections(DiagramNodeTemplate nodeTemplate)
    {
        if (nodeTemplate != null)
        {
            List<DiagramConnectionTemplate> connTemplates = getAllConnectionTemplates();
            for (DiagramConnectionTemplate connTemplate : connTemplates)
            {
                connTemplate.showAllConnectionParts(nodeTemplate);
            }
            DiagramEmbeddedConnectionTemplate embeddedConnTemplate = this.embeddedConnectionTemplateMap.get(nodeTemplate);
            if (embeddedConnTemplate != null)
            {
                embeddedConnTemplate.showAllConnectionParts(nodeTemplate);
            }
            
            refreshImplicitConnections();
        }
    }
        
    private void hideAllAttachedConnections(DiagramNodeTemplate nodeTemplate)
    {       
        if (nodeTemplate != null)
        {
            List<DiagramConnectionTemplate> connTemplates = getAllConnectionTemplates();
            for (DiagramConnectionTemplate connTemplate : connTemplates)
            {
                connTemplate.hideAllConnectionParts(nodeTemplate);
            }
            DiagramEmbeddedConnectionTemplate embeddedConnTemplate = this.embeddedConnectionTemplateMap.get(nodeTemplate);
            if (embeddedConnTemplate != null)
            {
                embeddedConnTemplate.hideAllConnectionParts(nodeTemplate);
            }
            
            refreshImplicitConnections();
        }
    }
    
    private List<DiagramConnectionTemplate> getAllConnectionTemplates()
    {
        return this.connectionTemplates;
    }
    
    private IDiagramConnectionDef getDiagramConnectionDef(String connId)
    {
        if (connId == null)
        {
            throw new IllegalArgumentException();
        }
        
        IDiagramConnectionDef connDef = null;
        for (IDiagramConnectionDef def : this.connectionDefs)
        {
            String id = def.getId().content();
            if (id != null && id.equalsIgnoreCase(connId))
            {
                connDef = def;
                break;
            }
        }
        return connDef;
    }
    
    private DiagramConnectionTemplate getConnectionTemplate(DiagramNodePart srcNode, String connectionType)
    {
        DiagramConnectionTemplate connectionTemplate = null;
        DiagramEmbeddedConnectionTemplate embeddedConnTemplate = 
                this.embeddedConnectionTemplateMap.get(srcNode.getDiagramNodeTemplate());
        if (embeddedConnTemplate != null && 
                embeddedConnTemplate.getConnectionTypeId().equalsIgnoreCase(connectionType))
        {
            connectionTemplate = embeddedConnTemplate;
        }
        else
        {
            for (DiagramConnectionTemplate connectionTemplate2 : getAllConnectionTemplates())
            {
                if (connectionTemplate2.getConnectionTypeId().equalsIgnoreCase(connectionType))
                {
                    connectionTemplate = connectionTemplate2;
                    break;
                }
            }
        }
        return connectionTemplate;
    }
    
    private void refreshImplicitConnections()
    {       
        for( DiagramImplicitConnectionTemplate template : this.implicitConnectionTemplates )
        {
            template.refreshImplicitConnections();
        }
	}
	
	private void handleNodeAboutToBeDeleted(DiagramNodePart nodePart)
	{
		Element nodeModel = nodePart.getLocalModelElement();
        // Check top level connections to see whether we need to remove the connection parent element
        for (DiagramConnectionTemplate connTemplate : getAllConnectionTemplates())
        {
            if (connTemplate.getConnectionType() == DiagramConnectionTemplate.ConnectionType.OneToMany)
            {
                Element connParentElement = connTemplate.getConnectionParentElement(nodeModel);
                if (connParentElement != null)
                {
                    ElementList<?> connParentList = (ElementList<?>) connParentElement.parent();
                    connParentList.remove(connParentElement);
                }
            }
        }
        
    }
    
    /**
     * In the case where the entire Sapphire model is reconstructed (revert source file in the source editor), 
     * connection properties may have triggered events before the node properties change events
     * are sent out. So those connection parts will be created before the endpoint node
     * parts are created. But those connection parts won't be displayed visually on diagram canvas
     * until those corresponding endpoint nodes are created on the canvas.
     * 
     * [Bug 376245] Revert action in StructuredTextEditor does not revert diagram nodes and connections
     * in SapphireDiagramEditor
     * 
     * @param nodePart
     */
    private void refreshAttachedConnections(DiagramNodePart nodePart)
    {
    	Element nodeElement = nodePart.getLocalModelElement();
    	for (DiagramConnectionPart connPart : list())
    	{
			 if (connPart.removable() && 
					 (connPart.getEndpoint1() == nodeElement || 
							 connPart.getEndpoint2() == nodeElement))
			 {
				 ConnectionAddEvent addEvent = new ConnectionAddEvent(connPart);
				 this.broadcast(addEvent);
			 }

        }
            
    }
            
	private final class ConnectionTemplateListener extends DiagramConnectionTemplateListener
	{
    	@Override
    	public void handleConnectionEndpointUpdate(final ConnectionEndpointsEvent event)
    	{
    	    StandardConnectionService.this.broadcast(event);
    	}
    	
        @Override
        public void handleConnectionAddEvent(final ConnectionAddEvent event)
        {
            StandardConnectionService.this.broadcast(event);
        }

        @Override
        public void handleConnectionDeleteEvent(final ConnectionDeleteEvent event)
        {
            StandardConnectionService.this.broadcast(event);
        }
        
	}
    
    private final class ImplicitConnectionTemplateListener extends DiagramImplicitConnectionTemplateListener
    {
        @Override
        public void handleConnectionAddEvent(final ConnectionAddEvent event)
        {
            StandardConnectionService.this.broadcast(event);
        }
    	
        @Override
        public void handleConnectionDeleteEvent(final ConnectionDeleteEvent event)
        {
            StandardConnectionService.this.broadcast(event);
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            ISapphirePart part = context.find(ISapphirePart.class);
            if (part instanceof SapphireDiagramEditorPagePart)
            {
                SapphireDiagramEditorPagePart diagramPagePart = (SapphireDiagramEditorPagePart)part;
                DiagramEditorPageDef pageDef = diagramPagePart.getPageDef();
                if (pageDef.getConnectionServiceType().content() == ConnectionServiceType.STANDARD)
                {
                    return true;
                }
            }
            return false;
        }
    }
        
}

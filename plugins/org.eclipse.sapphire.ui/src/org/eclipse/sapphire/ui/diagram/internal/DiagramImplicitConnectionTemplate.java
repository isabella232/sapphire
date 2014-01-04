/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [374154] IllegalStateException in ServiceContext when disposing diagram connection templates
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.diagram.ConnectionAddEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionDeleteEvent;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IModelElementTypeDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate.DiagramNodeTemplateListener;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class DiagramImplicitConnectionTemplate extends DiagramConnectionTemplate 
{
    public static abstract class DiagramImplicitConnectionTemplateListener
    {
        public void handleConnectionAddEvent(final ConnectionAddEvent event)
        {            
        }
        public void handleConnectionDeleteEvent(final ConnectionDeleteEvent event)
        {            
        }
    }
    
    private IDiagramImplicitConnectionBindingDef bindingDef;
    private IDiagramConnectionDef connectionDef;
    private SapphireDiagramEditorPagePart diagramEditor;
    private String propertyName;
//    private ModelPath allDescendentsPath;
    private ListProperty modelProperty;
    private List<Class<?>> modelElementTypes;
    private List<StandardImplicitConnectionPart> implicitConnections;
    private Listener modelPropertyListener;
    private Set<DiagramImplicitConnectionTemplateListener> templateListeners;
    private Map<Element, FunctionResult> listEntryFunctionMap;
        
    public DiagramImplicitConnectionTemplate(IDiagramImplicitConnectionBindingDef bindingDef)
    {
        this.bindingDef = bindingDef;
    }

    @Override
    public void init()
    {
        this.diagramEditor = (SapphireDiagramEditorPagePart)parent();
        this.listEntryFunctionMap = new IdentityHashMap<Element, FunctionResult>();
        this.connectionDef = (IDiagramConnectionDef)super.definition();
        this.propertyName = this.bindingDef.getProperty().content();
        this.modelProperty = (ListProperty)getModelElement().property(this.propertyName).definition();
        
        this.modelElementTypes = new ArrayList<Class<?>>();
        ElementList<IModelElementTypeDef> types = this.bindingDef.getModelElementTypes();
        for (IModelElementTypeDef typeDef : types)
        {
            this.modelElementTypes.add((Class<?>)typeDef.getType().resolve().artifact());
        }
        initImplicitConnectionParts();
        
        this.templateListeners = new CopyOnWriteArraySet<DiagramImplicitConnectionTemplateListener>();
        
        // Add model property listener
        this.modelPropertyListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                refreshImplicitConnections();
            }
        };
        addModelListener();
        
        // Add node template listener. When the xml file is editing in the source
        // tab or in external editor, we have no control over who receives model events
        // first. Sometimes the model listener here is notified before the model listener
        // in diagram node template is notified. In this case, dangling connection parts
        // are created before node parts are created. So when new node part is created, we 
        // need to notify connection template.
        List<DiagramNodeTemplate> nodeTemplates = 
        		this.diagramEditor.getNodeTemplates(this.modelProperty);
        if (!nodeTemplates.isEmpty())
        {
        	NodeTemplateListener nodeTemplateListener = new NodeTemplateListener();
        	for (DiagramNodeTemplate nodeTemplate : nodeTemplates)
        	{
        		nodeTemplate.addTemplateListener(nodeTemplateListener);
        	}
        }
    }
    
    public SapphireDiagramEditorPagePart getDiagramEditorPart()
    {
        return this.diagramEditor;
    }
    
    @Override
    public void addModelListener()
    {
        getModelElement().attach(this.modelPropertyListener, this.propertyName);
        // I don't think the following code is necessary since the condition expression
        // refreshes implicit connections. TODO more testing when the "Property instance construct"
        // change propogrates to OEPE
//        String temp = this.propertyName + "/*";
//        this.allDescendentsPath = new ModelPath(temp);
//        getModelElement().attach(this.modelPropertyListener, this.allDescendentsPath);
    }
    
    @Override
    public void removeModelListener()
    {
        getModelElement().detach(this.modelPropertyListener, this.propertyName);
//        getModelElement().detach(this.modelPropertyListener, this.allDescendentsPath);
    }
    
    public void refreshImplicitConnections()
    {
        List<Element> newFilteredList = getFilteredModelElementList();
        
        for (StandardImplicitConnectionPart connPart : this.implicitConnections)
        {
            notifyConnectionDelete(connPart);
            connPart.dispose();
        }
        this.implicitConnections.clear();
        for (int i = 0; i < newFilteredList.size() - 1; i++)
        {
        	DiagramNodePart srcNode = this.diagramEditor.getDiagramNodePart(newFilteredList.get(i));
        	DiagramNodePart targetNode = this.diagramEditor.getDiagramNodePart(newFilteredList.get(i+1));
        	if (srcNode != null && srcNode.getDiagramNodeTemplate().visible() &&
        			targetNode != null && targetNode.getDiagramNodeTemplate().visible())
        	{
	            StandardImplicitConnectionPart connPart = 
	                    createNewImplicitConnectionPart(newFilteredList.get(i), newFilteredList.get(i+1));
                this.implicitConnections.add(connPart);
                notifyConnectionAdd(connPart);
        	}
        }            
    }
    
    public List<StandardImplicitConnectionPart> getImplicitConnections()
    {
        return this.implicitConnections;
    }
    
    private void initImplicitConnectionParts()
    {
    	List<Element> newFilteredList = getFilteredModelElementList();
        
        this.implicitConnections = new ArrayList<StandardImplicitConnectionPart>();
        for (int i = 0; i < newFilteredList.size() - 1; i++)
        {
            StandardImplicitConnectionPart connPart = 
                    createNewImplicitConnectionPart(newFilteredList.get(i), newFilteredList.get(i+1));
            this.implicitConnections.add(connPart);
        }    
        
    }
    
    private List<Element> getFilteredModelElementList()
    {
        ElementList<?> list = getModelElement().property(this.modelProperty);
        List<Element> filteredList = new ArrayList<Element>();
        for( Element listEntryModelElement : list )
        {
            if (isRightEntry(listEntryModelElement))
            {
                filteredList.add(listEntryModelElement);
            }
        }
        return filteredList;
    }
        
    private StandardImplicitConnectionPart createNewImplicitConnectionPart(Element srcNodeModel, Element targetNodeModel)
    {
        StandardImplicitConnectionPart connPart = new StandardImplicitConnectionPart(srcNodeModel, targetNodeModel);
        connPart.init(this, srcNodeModel, this.connectionDef, Collections.<String,String>emptyMap());
        connPart.initialize();
        return connPart;
    }
    
    private boolean isRightEntry(Element entryModelElement)
    {
        boolean isRightType = true;
        if (this.modelElementTypes.size() > 0)
        {
            isRightType = false;
            for (Class<?> eleType : this.modelElementTypes)
            {
                if (eleType.isAssignableFrom(entryModelElement.getClass()))
                {
                    isRightType = true;
                    break;
                }
            }
        }
        if (isRightType && this.bindingDef.getCondition() != null)
        {
            isRightType = false;
            // apply the condition
            FunctionResult fr = this.listEntryFunctionMap.get(entryModelElement);
            if (fr == null)
            {
	            fr = initExpression
	            ( 
	                entryModelElement,
	                this.bindingDef.getCondition().content(), 
	                String.class,
	                null,
	                new Runnable()
	                {
	                    public void run()
	                    {
	                    	refreshImplicitConnections();
	                    }    
	                }
	            );
	            this.listEntryFunctionMap.put(entryModelElement, fr);
            }
            if (fr != null && ((String)fr.value()).equals("true"))
            {
                isRightType = true;                
            }
        }
        return isRightType;
    }
    
    public void addTemplateListener( final DiagramImplicitConnectionTemplateListener listener )
    {
        this.templateListeners.add( listener );
    }
    
    public void removeTemplateListener( final DiagramImplicitConnectionTemplateListener listener )
    {
        this.templateListeners.remove( listener );
    }    

    public void notifyConnectionAdd(StandardImplicitConnectionPart connPart)
    {
        for( DiagramImplicitConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionAddEvent(new ConnectionAddEvent(connPart));
        }        
    }

    public void notifyConnectionDelete(StandardImplicitConnectionPart connPart)
    {
        for( DiagramImplicitConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionDeleteEvent(new ConnectionDeleteEvent(connPart));
        }        
    }
    
    @Override
    public void dispose()
    {
        for( FunctionResult fr : this.listEntryFunctionMap.values() )
        {
            if( fr != null )
            {
                fr.dispose();
            }
        }

        List<StandardImplicitConnectionPart> connParts = getImplicitConnections();
        for (StandardImplicitConnectionPart connPart : connParts)
        {
        	connPart.dispose();
        }
    }
    
	private class NodeTemplateListener extends DiagramNodeTemplateListener
	{
        
        @Override
        public void handleNodeAdd(final DiagramNodePart nodePart)
        {
            refreshImplicitConnections();
        }

	}
    
}

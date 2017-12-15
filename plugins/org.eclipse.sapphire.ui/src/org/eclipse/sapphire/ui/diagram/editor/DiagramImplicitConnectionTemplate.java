/******************************************************************************
 * Copyright (c) 2013 Oracle
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

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IModelElementTypeDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate.DiagramNodeTemplateListener;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramImplicitConnectionTemplate extends DiagramConnectionTemplate 
{
    public static abstract class DiagramImplicitConnectionTemplateListener
    {
        public void handleConnectionAdd(final DiagramConnectionEvent event)
        {            
        }
        public void handleConnectionDelete(final DiagramConnectionEvent event)
        {            
        }
    }
    
    private IDiagramImplicitConnectionBindingDef bindingDef;
    private IDiagramConnectionDef connectionDef;
    protected IModelElement modelElement;
    private SapphireDiagramEditorPagePart diagramEditor;
    private String propertyName;
    private ModelPath allDescendentsPath;
    private ListProperty modelProperty;
    private List<Class<?>> modelElementTypes;
    private List<DiagramImplicitConnectionPart> implicitConnections;
    private Listener modelPropertyListener;
    private Set<DiagramImplicitConnectionTemplateListener> templateListeners;
    private Map<IModelElement, FunctionResult> listEntryFunctionMap;
    private DiagramNodeTemplate nodeTemplate;
    private NodeTemplateListener nodeTemplateListener;
        
    public DiagramImplicitConnectionTemplate(IDiagramImplicitConnectionBindingDef bindingDef)
    {
        this.bindingDef = bindingDef;
    }

    @Override
    public void init()
    {
        this.diagramEditor = (SapphireDiagramEditorPagePart)getParentPart();
        this.listEntryFunctionMap = new IdentityHashMap<IModelElement, FunctionResult>();
        this.modelElement = getModelElement();
        this.connectionDef = (IDiagramConnectionDef)super.definition();
        this.propertyName = this.bindingDef.getProperty().getContent();
        this.modelProperty = (ListProperty)ModelUtil.resolve(this.modelElement, this.propertyName);
        
        this.modelElementTypes = new ArrayList<Class<?>>();
        ModelElementList<IModelElementTypeDef> types = this.bindingDef.getModelElementTypes();
        for (IModelElementTypeDef typeDef : types)
        {
            this.modelElementTypes.add(typeDef.getType().resolve().artifact());
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
        this.nodeTemplate = 
        		this.diagramEditor.getNodeTemplate(this.modelProperty);
        if (this.nodeTemplate != null)
        {
        	this.nodeTemplateListener = new NodeTemplateListener();
        	this.nodeTemplate.addTemplateListener(this.nodeTemplateListener);
        }
    }
    
    public SapphireDiagramEditorPagePart getDiagramEditorPart()
    {
        return this.diagramEditor;
    }
    
    @Override
    public void addModelListener()
    {
        this.modelElement.attach(this.modelPropertyListener, this.propertyName);
        String temp = this.propertyName + "/*";
        this.allDescendentsPath = new ModelPath(temp);
        this.modelElement.attach(this.modelPropertyListener, this.allDescendentsPath);
    }
    
    @Override
    public void removeModelListener()
    {
        this.modelElement.detach(this.modelPropertyListener, this.propertyName);
        this.modelElement.detach(this.modelPropertyListener, this.allDescendentsPath);
    }
    
    public void refreshImplicitConnections()
    {
    	removeModelListener();
        List<IModelElement> newFilteredList = getFilteredModelElementList();
        
        for (DiagramImplicitConnectionPart connPart : this.implicitConnections)
        {
            notifyConnectionDelete(connPart);
            connPart.dispose();
        }
        this.implicitConnections.clear();
        for (int i = 0; i < newFilteredList.size() - 1; i++)
        {
            DiagramImplicitConnectionPart connPart = 
                    createNewImplicitConnectionPart(newFilteredList.get(i), newFilteredList.get(i+1));
            if (connPart.getEndpoint1() != null && connPart.getEndpoint2() != null &&
            		this.diagramEditor.getDiagramNodePart(connPart.getEndpoint1()) != null &&
            		this.diagramEditor.getDiagramNodePart(connPart.getEndpoint2()) != null)
            {
                this.implicitConnections.add(connPart);
                notifyConnectionAdd(connPart);
            }
        } 
        addModelListener();
    }
    
    public List<DiagramImplicitConnectionPart> getImplicitConnections()
    {
        return this.implicitConnections;
    }
    
    private void initImplicitConnectionParts()
    {
    	List<IModelElement> newFilteredList = getFilteredModelElementList();
        
        this.implicitConnections = new ArrayList<DiagramImplicitConnectionPart>();
        for (int i = 0; i < newFilteredList.size() - 1; i++)
        {
            DiagramImplicitConnectionPart connPart = 
                    createNewImplicitConnectionPart(newFilteredList.get(i), newFilteredList.get(i+1));
            this.implicitConnections.add(connPart);
        }    
        
    }
    
    private List<IModelElement> getFilteredModelElementList()
    {
        ModelElementList<?> list = this.modelElement.read(this.modelProperty);
        List<IModelElement> filteredList = new ArrayList<IModelElement>();
        for( IModelElement listEntryModelElement : list )
        {
            if (isRightEntry(listEntryModelElement))
            {
                filteredList.add(listEntryModelElement);
            }
        }
        return filteredList;
    }
        
    private DiagramImplicitConnectionPart createNewImplicitConnectionPart(IModelElement srcNodeModel, IModelElement targetNodeModel)
    {
        DiagramImplicitConnectionPart connPart = new DiagramImplicitConnectionPart(srcNodeModel, targetNodeModel);
        connPart.init(this, srcNodeModel, this.connectionDef, Collections.<String,String>emptyMap());
        return connPart;
    }
    
    private boolean isRightEntry(IModelElement entryModelElement)
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
	                this.bindingDef.getCondition().getContent(), 
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
    
    @Override
    public void render(SapphireRenderingContext context)
    {
        throw new UnsupportedOperationException();        
    }
    
    public void addTemplateListener( final DiagramImplicitConnectionTemplateListener listener )
    {
        this.templateListeners.add( listener );
    }
    
    public void removeTemplateListener( final DiagramImplicitConnectionTemplateListener listener )
    {
        this.templateListeners.remove( listener );
    }    

    public void notifyConnectionAdd(DiagramImplicitConnectionPart connPart)
    {
        for( DiagramImplicitConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionAdd(new DiagramConnectionEvent(connPart));
        }        
    }

    public void notifyConnectionDelete(DiagramImplicitConnectionPart connPart)
    {
        for( DiagramImplicitConnectionTemplateListener listener : this.templateListeners )
        {
            listener.handleConnectionDelete(new DiagramConnectionEvent(connPart));
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

        List<DiagramImplicitConnectionPart> connParts = getImplicitConnections();
        for (DiagramImplicitConnectionPart connPart : connParts)
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

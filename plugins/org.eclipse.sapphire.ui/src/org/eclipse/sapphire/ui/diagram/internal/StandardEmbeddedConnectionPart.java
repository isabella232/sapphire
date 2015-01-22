/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [341856] NPE when a diagram connection doesn't define a label
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [342775] Support EL in MasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.internal;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionEndpointBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class StandardEmbeddedConnectionPart extends StandardDiagramConnectionPart 
{
    private Element srcNodeModel;
    private Element endpointModel;
    private ModelPath endpointPath;
    private Listener referenceServiceListener;
    private ReferenceValue<?, ?> endpointReferenceValue;
    private IDiagramConnectionEndpointBindingDef endpointDef;
    
    public StandardEmbeddedConnectionPart(IDiagramExplicitConnectionBindingDef connBindingDef, Element srcNodeModel, ModelPath endpointPath)
    {
        this.bindingDef = connBindingDef;
        this.srcNodeModel = srcNodeModel;
        this.endpointPath = endpointPath;
    }
    
    @Override
    protected void init()
    {   
        initLabelId();
        
        this.endpointDef = this.bindingDef.getEndpoint2().content();
        this.endpointModel = resolveEndpoint(this.modelElement, this.endpointPath);
        // Add model property listener
        this.modelPropertyListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                handleModelPropertyChange( event );
            }
        };
        addModelListener(); 
        addReferenceServiceListeners();
    }
    
    @Override
    public Element getEndpoint1()
    {
        return this.srcNodeModel;
    }
    
    @Override
    public Element getEndpoint2()
    {
        return this.endpointModel;
    }
            
    @Override
    public void resetEndpoint1(DiagramNodePart newSrcNode)
    {
    }
    
    public DiagramNodePart getSourceNodePart()
    {
        SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)parent().parent().parent();
        return diagramPart.getDiagramNodePart(this.srcNodeModel);
    }
    
    public String getId()
    {
        StringBuffer buffer = new StringBuffer(getConnectionTypeId());
        buffer.append(CONNECTION_ID_SEPARATOR);
        String instanceId = getInstanceId();
        if (instanceId != null && instanceId.length() > 0)
        {
            buffer.append(getInstanceId());
            buffer.append(CONNECTION_ID_SEPARATOR);
        }
        Element srcNodeElement = getSourceNodePart().getLocalModelElement();
        List<StandardDiagramConnectionPart> connParts = getDiagramConnectionTemplate().getDiagramConnections(srcNodeElement);
        int index = connParts.indexOf(this);
        buffer.append(index);                
        return buffer.toString();            	
    }
        
    @Override
    public void addModelListener()
    {
        this.modelElement.attach(this.modelPropertyListener, 
                                    this.endpointDef.getProperty().content());
    }
    
    @Override
    public void addReferenceServiceListeners()
    {
    	this.endpointReferenceValue = resolveEndpointReferenceValue(modelElement, endpointPath);
    	if (endpointReferenceValue != null)
    	{
    		ReferenceService<?> refService = endpointReferenceValue.service(ReferenceService.class);
    		this.referenceServiceListener = new Listener()
			{
				@Override
				public void handle( Event event )
				{
					handleEndpointReferenceChange();
				}
			};
    		if (refService != null)
    		{
    			refService.attach(this.referenceServiceListener);
    		}
    	}
    }
           
    @Override
    public void removeModelListener()
    {
        this.modelElement.detach(this.modelPropertyListener, 
                                    this.endpointDef.getProperty().content());
    }

    @Override
    protected void handleModelPropertyChange(final PropertyEvent event)
    {
        final PropertyDef property = event.property().definition();
        if (property.name().equals(this.endpointDef.getProperty().content()))
        {
        	this.endpointModel = resolveEndpoint(this.modelElement, this.endpointPath);
            notifyConnectionEndpointUpdate();
        }                
    }    
    
    protected void handleEndpointReferenceChange()
    {
		Element newTargetModel = resolveEndpoint(this.modelElement, this.endpointPath);
		if (newTargetModel != this.endpointModel)
		{
			this.endpointModel = newTargetModel;
			notifyConnectionEndpointUpdate(); 
		}    		
    }
        
}

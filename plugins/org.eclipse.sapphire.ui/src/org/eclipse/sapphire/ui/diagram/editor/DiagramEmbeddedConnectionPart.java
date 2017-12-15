/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [341856] NPE when a diagram connection doesn't define a label
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [342775] Support EL in IMasterDetailsTreeNodeDef.ImagePath
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionEndpointBindingDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramExplicitConnectionBindingDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramEmbeddedConnectionPart extends DiagramConnectionPart 
{
    private IModelElement srcNodeModel;
    private IModelElement endpointModel;
    private Listener endpointModelListener;
    private ModelPath endpointPath;
    private FunctionResult endpointFunctionResult;
    private IDiagramConnectionEndpointBindingDef endpointDef;
    
    public DiagramEmbeddedConnectionPart(IDiagramExplicitConnectionBindingDef connBindingDef, IModelElement srcNodeModel, ModelPath endpointPath)
    {
        this.bindingDef = connBindingDef;
        this.srcNodeModel = srcNodeModel;
        this.endpointPath = endpointPath;
    }
    
    @Override
    protected void init()
    {   
        initLabelId();
        this.endpointModelListener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                handlEndpointModelPropertyChange( event );
            }
        };
        
        this.endpointDef = this.bindingDef.getEndpoint2().element();
        this.endpointModel = resolveEndpoint(this.modelElement, this.endpointPath);
        if (this.endpointModel != null)
        {
            this.endpointFunctionResult = initExpression
            (
                this.endpointModel, 
                this.endpointDef.getValue().getContent(),
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                    }
                }
            );
        }        
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
    }
    
    @Override
    public IModelElement getEndpoint1()
    {
        return this.srcNodeModel;
    }
    
    @Override
    public IModelElement getEndpoint2()
    {
        return this.endpointModel;
    }

    @Override
    protected void handlEndpointModelPropertyChange(final PropertyEvent event)
    {
    	if (this.endpointModel == null || event.element() == this.endpointModel)
    	{
    		IModelElement newTargetModel = resolveEndpoint(this.modelElement, this.endpointPath);
    		if (newTargetModel != this.endpointModel)
    		{
    			handleEndpointChange();
    			notifyConnectionEndpointUpdate(); 
    		}    		
    	}
    }
    
    @Override
    public void resetEndpoint1()
    {
    }
    
    @Override
    public void resetEndpoint2()
    {
        if (this.endpointFunctionResult != null)
        {
            String value = (String)this.endpointFunctionResult.value();
            if (value == null  || value.length() == 0)
            {
                SapphireDiagramEditorPagePart diagramPart = this.getDiagramConnectionTemplate().getDiagramEditor();
                DiagramNodePart nodePart = diagramPart.getDiagramNodePart(this.endpointModel);
                if (nodePart != null)
                {
                    value = IdUtil.computeNodeId(nodePart);
                }
            }            
            
            String property = this.endpointDef.getProperty().getContent();
            setModelProperty(this.modelElement, property, value);
        }        
    }
        
    @Override
    public void resetEndpoint1(DiagramNodePart newSrcNode)
    {
    }
    
    public DiagramNodePart getSourceNodePart()
    {
        SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)getParentPart().getParentPart().getParentPart();
        return diagramPart.getDiagramNodePart(this.srcNodeModel);
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.endpointFunctionResult != null)
        {
            this.endpointFunctionResult.dispose();
        }        
    }
    
    @Override
    public void addModelListener()
    {
        if (this.labelProperty != null)
        {
            this.modelElement.attach(this.modelPropertyListener, 
                                        this.labelProperty.getName());
        }
        this.modelElement.attach(this.modelPropertyListener, 
                                    this.endpointDef.getProperty().getContent());
        if (this.endpointModel != null)
        {
        	this.endpointModel.attach(this.endpointModelListener);
        }
    }
    
    @Override
    public void removeModelListener()
    {
        if (this.labelProperty != null)
        {
            this.modelElement.detach(this.modelPropertyListener, 
                                        this.labelProperty.getName());
        }
        this.modelElement.detach(this.modelPropertyListener, 
                                    this.endpointDef.getProperty().getContent());
        if (this.endpointModel != null)
        {
        	this.endpointModel.detach(this.endpointModelListener);
        }
    }

    @Override
    protected void handleModelPropertyChange(final PropertyEvent event)
    {
        final ModelProperty property = event.property();
        if (property.getName().equals(this.endpointDef.getProperty().getContent()))
        {
            handleEndpointChange();
            notifyConnectionEndpointUpdate();
        }                
    }    
    
    private void handleEndpointChange()
    {
        this.endpointModel = resolveEndpoint(this.modelElement, this.endpointPath);
        if (this.endpointFunctionResult != null)
        {
            this.endpointFunctionResult.dispose();
            this.endpointFunctionResult = null;
        }
        if (this.endpointModel != null)
        {            
            this.endpointFunctionResult = initExpression
            (
                this.endpointModel, 
                this.endpointDef.getValue().getContent(), 
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                    }
                }
            );
        }        
    }
    
}

/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 *    Konstantin Komissarchik - [342775] Support EL in IMasterDetailsTreeNodeDef.ImagePath
 *    Ling Hao - [44319] Image specification for diagram parts inconsistent with the rest of sdef 
 *    Konstantin Komissarchik - [378756] Convert ModelElementListener and ModelPropertyListener to common listener infrastructure
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ElementValidationEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.IPropertiesViewContributorPart;
import org.eclipse.sapphire.ui.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.def.IDiagramImageDecoratorDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramNodeProblemDecoratorDef;
import org.eclipse.sapphire.ui.diagram.def.ImagePlacement;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramNodePart 

    extends SapphirePart
    implements IPropertiesViewContributorPart
    
{
	private static final String DEFAULT_ACTION_ID = "Sapphire.Diagram.Node.Default";
	
	private DiagramNodeTemplate nodeTemplate;
	private IDiagramNodeDef definition;
	private IModelElement modelElement;
	private FunctionResult labelFunctionResult;
	private FunctionResult idFunctionResult;
	private FunctionResult imageDataFunctionResult;
	private List<FunctionResult> imageDecoratorFunctionResults;
	private List<FunctionResult> imageDecoratorDataFunctionResults;
	private ValueProperty labelProperty;
	private SapphireAction defaultAction;
	private SapphireActionHandler defaultActionHandler;
	private Listener modelPropertyListener;
	private FilteredListener<ElementValidationEvent> elementValidationListener;
	private PropertiesViewContributionManager propertiesViewContributionManager; 
	private DiagramNodeBounds nodeBounds = new DiagramNodeBounds();

	@Override
    protected void init()
    {
        super.init();
        this.nodeTemplate = (DiagramNodeTemplate)getParentPart();
        this.definition = (IDiagramNodeDef)super.definition;
        this.modelElement = getModelElement();
        this.labelFunctionResult = initExpression
        ( 
            this.modelElement,
            this.definition.getLabel().element().getText().getContent(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    refreshLabel();
                }
            }
        );
        this.labelProperty = FunctionUtil.getFunctionProperty(this.modelElement, this.labelFunctionResult);
        
        this.idFunctionResult = initExpression
        ( 
            this.modelElement,
            this.definition.getInstanceId().getContent(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                }
            }
        );
        
        if (this.definition.getImage().element() != null)
        {
            this.imageDataFunctionResult = initExpression
            ( 
                this.modelElement,
                this.definition.getImage().element().getImage().getContent(),
                ImageData.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        refreshImage();
                    }
                }
            );
        }

        // Image decorator functions
        
        ModelElementList<IDiagramImageDecoratorDef> imageDecorators = this.definition.getImageDecorators();
        this.imageDecoratorFunctionResults = new ArrayList<FunctionResult>();
        for (IDiagramImageDecoratorDef imageDecorator : imageDecorators)
        {
            FunctionResult imageResult = initExpression
            ( 
                this.modelElement,
                imageDecorator.getVisibleWhen().getContent(),
                String.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        refreshDecorator();
                    }
                }
            );
            this.imageDecoratorFunctionResults.add(imageResult);
        }
        
        this.imageDecoratorDataFunctionResults = new ArrayList<FunctionResult>();
        for (IDiagramImageDecoratorDef imageDecorator : imageDecorators)
        {
            FunctionResult imageResult = initExpression
            ( 
                this.modelElement,
                imageDecorator.getImage().getContent(),
                ImageData.class,
                null,
                new Runnable()
                {
                    public void run()
                    {
                        refreshDecorator();
                    }
                }
            );
            this.imageDecoratorDataFunctionResults.add(imageResult);
        }
        
        // Add model property listener. It listens to all the properties so that the
        // validation status change would trigger node update
        this.modelPropertyListener =  new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                notifyNodeUpdate();
            }
        };
        this.modelElement.attach(this.modelPropertyListener, "*");
        
        this.elementValidationListener = new FilteredListener<ElementValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( final ElementValidationEvent event )
            {
                notifyNodeUpdate();
            }
        };
        this.modelElement.attach(this.elementValidationListener);        
    }
    
    public DiagramNodeTemplate getDiagramNodeTemplate()
    {
        return this.nodeTemplate;
    }
    
    @Override
    public IModelElement getLocalModelElement()
    {
        return this.modelElement;
    }    
           
    public List<NodeImageDecorator> getImageDecorators()
    {
        List<NodeImageDecorator> imageDecorators = new ArrayList<NodeImageDecorator>();
        ModelElementList<IDiagramImageDecoratorDef> defs = this.definition.getImageDecorators();
        for (int i = 0; i < this.imageDecoratorFunctionResults.size(); i++)
        {
            FunctionResult result = this.imageDecoratorFunctionResults.get(i);
            if (result != null)
            {
                String show = (String)result.value();
                if (show != null && show.equals("true"))
                {
                    IDiagramImageDecoratorDef def = defs.get(i);
                	FunctionResult imageDataResult = this.imageDecoratorDataFunctionResults.get(i);
                	NodeImageDecorator nodeImageDecorator = new NodeImageDecorator((ImageData)imageDataResult.value(), def);
                    imageDecorators.add(nodeImageDecorator);
                }
            }
        }
        return imageDecorators;
    }
    
    @Override
    public void render(SapphireRenderingContext context)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getActionContexts()
    {
        Set<String> contextSet = new HashSet<String>();
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE);
        contextSet.add(SapphireActionSystem.CONTEXT_DIAGRAM_NODE_HIDDEN);
        return contextSet;    	
    }
        
    public SapphireActionHandler getDefaultActionHandler()
    {        
    	if (this.defaultAction == null)
    	{
            // Default Action handler
            this.defaultAction = getAction(DEFAULT_ACTION_ID);
            this.defaultActionHandler = this.defaultAction.getFirstActiveHandler();    		
    	}
        return this.defaultActionHandler;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.labelFunctionResult != null)
        {
            this.labelFunctionResult.dispose();
        }
        
        if (this.idFunctionResult != null)
        {
            this.idFunctionResult.dispose();
        }
        
        if (this.imageDataFunctionResult != null)
        {
            this.imageDataFunctionResult.dispose();
        }
    
        for (int i = 0; i < this.imageDecoratorFunctionResults.size(); i++)
        {
            FunctionResult result = this.imageDecoratorFunctionResults.get(i);
            if (result != null)
            {
                result.dispose();
            }
        }
        
        for (int i = 0; i < this.imageDecoratorDataFunctionResults.size(); i++)
        {
            FunctionResult result = this.imageDecoratorDataFunctionResults.get(i);
            if (result != null)
            {
                result.dispose();
            }
        }

        this.modelElement.detach(this.modelPropertyListener, "*");
        this.modelElement.detach(this.elementValidationListener);
    }
    
    public String getLabel()
    {
        String label = null;
        
        if( this.labelFunctionResult != null )
        {
            label = (String) this.labelFunctionResult.value();
        }
        
        if( label == null )
        {
            label = "#null#";
        }
        
        return label;
    }

    public void setLabel(String newValue)
    {
        if (this.labelProperty != null)
        {
            this.modelElement.write(this.labelProperty, newValue);
        }
    }
    
    public void refreshLabel()
    {
        notifyNodeUpdate();
    }
    
    public void refreshImage()
    {
        notifyNodeUpdate();
    }
    
    public void refreshDecorator()
    {
        notifyNodeUpdate();
    }

    public boolean canEditLabel()
    {
        return this.labelProperty != null;
    }
    
    public String getNodeTypeId()
    {
        return this.definition.getId().getContent();
    }
    
    public String getInstanceId()
    {
        String id = null;
        
        if( this.idFunctionResult != null )
        {
            id = (String) this.idFunctionResult.value();
        }
                
        return id;		
	}
	
	public boolean canResizeShape()
	{
		return this.definition.isResizable().getContent();
	}
	
	public DiagramNodeBounds getNodeBounds()
	{
		return new DiagramNodeBounds(this.nodeBounds);
	}
	
	public void setNodeBounds(int x, int y)
	{
		setNodeBounds(new DiagramNodeBounds(x, y, -1, -1, false, false));
	}
	
	public void setNodeBounds(int x, int y, boolean autoLayout, boolean defaultPosition)
	{
		setNodeBounds(new DiagramNodeBounds(x, y, -1, -1, autoLayout, defaultPosition));
	}
	
	public void setNodeBounds(Bounds bounds)
	{
		DiagramNodeBounds nodeBounds = new DiagramNodeBounds(bounds);
		setNodeBounds(nodeBounds);
	}
	
	public void setNodeBounds(DiagramNodeBounds bounds)
	{
		// TODO handle node resizing events
		if (!this.nodeBounds.equals(bounds))
		{
			this.nodeBounds.setX(bounds.getX());
			this.nodeBounds.setY(bounds.getY());
			this.nodeBounds.setAutoLayout(bounds.isAutoLayout());
			this.nodeBounds.setDefaultPosition(bounds.isDefaultPosition());
			notifyNodeMove();
		}			
	}

	public int getHorizontalSpacing()
	{
		if (this.definition.getHorizontalSpacing().getContent() != null)
		{
			return this.definition.getHorizontalSpacing().getContent();
		}
		return 0;
	}
	
	public int getVerticalSpacing()
	{
		if (this.definition.getVerticalSpacing().getContent() != null)
		{
			return this.definition.getVerticalSpacing().getContent();
		}
		return 0;
	}

    public ImageData getImage()
    {
        if( this.imageDataFunctionResult != null )
        {
        	return (ImageData) this.imageDataFunctionResult.value();
        }
        return null;        
    }
    
    public ImagePlacement getImagePlacement()
    {
        if (this.definition.getImage().element() != null)
        {
            return this.definition.getImage().element().getPlacement().getContent();
        }
        return null;
    }
    
    public int getImageWidth()
    {
        if (this.definition.getImage().element() != null)
        {
            if (this.definition.getImage().element().getWidth().getContent() != null)
            {
                this.definition.getImage().element().getWidth().getContent();
            }
        }
        return 0;
    }
    
    public int getImageHeight()
    {
        if (this.definition.getImage().element() != null)
        {
            if (this.definition.getImage().element().getHeight().getContent() != null)
            {
                this.definition.getImage().element().getHeight().getContent();
            }
        }
        return 0;
    }

    public int getLabelWidth()
    {
        if (this.definition.getLabel().element().getWidth().getContent() != null)
        {
            return this.definition.getLabel().element().getWidth().getContent();
        }
        return 0;
    }

	public int getLabelHeight()
	{
		if (this.definition.getLabel().element().getHeight().getContent() != null)
		{
			return this.definition.getLabel().element().getHeight().getContent();
		}
		return 0;
	}
	
	public IDiagramNodeProblemDecoratorDef getProblemIndicatorDef()
	{
		return this.definition.getProblemDecorator();
	}
					
	private void notifyNodeUpdate()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramNodeEvent nue = new DiagramNodeEvent(this);
				((SapphireDiagramPartListener)listener).handleNodeUpdateEvent(nue);
			}
		}
	}
	
	private void notifyNodeMove()
	{
		Set<SapphirePartListener> listeners = this.getListeners();
		for(SapphirePartListener listener : listeners)
		{
			if (listener instanceof SapphireDiagramPartListener)
			{
				DiagramNodeEvent ne = new DiagramNodeEvent(this);
				((SapphireDiagramPartListener)listener).handleNodeMoveEvent(ne);
			}
		}		
	}
	
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( this.propertiesViewContributionManager == null )
        {
            this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, getLocalModelElement() );
        }
        
        return this.propertiesViewContributionManager.getPropertiesViewContribution();
    }
    
    public final static class NodeImageDecorator {
    	
    	ImageData imageData;
    	IDiagramImageDecoratorDef imageDecoratorDef;
    	
    	public NodeImageDecorator(ImageData imageData, IDiagramImageDecoratorDef imageDecoratorDef) {
    		this.imageData = imageData;
    		this.imageDecoratorDef = imageDecoratorDef;
    	}
    	
    	public ImageData getImageData() {
    		return this.imageData;
    	}
    	
    	public IDiagramImageDecoratorDef getImageDecoratorDef() {
    		return this.imageDecoratorDef;
    	}
    	
    }
    
}

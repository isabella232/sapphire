/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerSize;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ValidationMarkerPart extends ShapePart 
{
	private ValidationMarkerDef markerDef;
    private Listener validationListener;
    private SapphirePart containerParent;

	@Override
    protected void init()
    {
        super.init();
        this.markerDef = (ValidationMarkerDef)super.definition;
        this.containerParent = getContainerParent();
        this.validationListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
            	broadcast(new ValidationMarkerContentEvent(ValidationMarkerPart.this));
            	broadcast(new ShapeUpdateEvent(ValidationMarkerPart.this));
            }
        };
        this.containerParent.attach(this.validationListener);
    }
	
	
    public ValidationMarkerSize getSize()
    {
    	return this.markerDef.getSize().content();
    }
            
    public Status content()
    {
    	return this.containerParent.validation();
    }

	public SapphirePart getContainerParent() 
	{
		if (this.containerParent == null)
		{
			// Go up in the Sapphire part hierarchy until a parent part with different model element
			// or until the diagram node part. The validation marker should indicate validation problems
			// associated with the corresponding model element.
			DiagramNodePart nodePart = this.nearest(DiagramNodePart.class);
			if (nodePart.getLocalModelElement() == getLocalModelElement())
			{
				this.containerParent = nodePart;
			}
			else
			{
				SapphirePart part = this;
				SapphirePart parentPart = (SapphirePart)parent();
				while (!(parentPart instanceof DiagramNodePart || parentPart.getLocalModelElement() != getLocalModelElement())) {
					part = parentPart;
					parentPart = (SapphirePart)parentPart.parent();
				}
				this.containerParent = part;				
			}
		}
		return this.containerParent;
	}

	@Override
	public void dispose() {
		if (this.validationListener != null) {
	        this.containerParent.detach(this.validationListener);
	        this.validationListener = null;
		}
		super.dispose();
	}
		
	@Override
    protected Function initVisibleWhenFunction()
    {
		final Function function = new Function()
        {
            @Override
            public String name()
            {
                return "ValidationMarkerVisibleWhen";
            }

			@Override
			public FunctionResult evaluate(FunctionContext context) 
			{				
		        return new FunctionResult( this, context )
		        {
					private Listener validationListener;
					
                    @Override
                    protected void init()
                    {
                        this.validationListener = new FilteredListener<ValidationMarkerContentEvent>()
                        {
                        	@Override
                            protected void handleTypedEvent( ValidationMarkerContentEvent event )
                            {
                        		refresh();
                            }
                        };
                    	
                        ValidationMarkerPart.this.attach(this.validationListener);
                    }
                    
		            @Override
		            protected Object evaluate()
		            {
		            	Status status = content(); //getLocalModelElement().validation();
		            	return !status.ok();
		            }

		            @Override
                    public void dispose()
                    {
                        super.dispose();
                        
                        ValidationMarkerPart.this.detach(this.validationListener);
                    }
		            
		        };
			}
        };
        function.init();
        return function;
    }	
    
	
}

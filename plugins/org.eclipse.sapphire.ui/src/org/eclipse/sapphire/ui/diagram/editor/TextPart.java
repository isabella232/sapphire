/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Extend Sapphire Diagram Framework to support SQL Schema diagram like editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.runOnDisplayThread;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.diagram.shape.def.FontDef;
import org.eclipse.sapphire.ui.diagram.shape.def.TextDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class TextPart extends ShapePart 
{
	private TextDef textDef;
	private Element modelElement;
	private Function textFunction;
	private FunctionResult functionResult;
	private Property property;
	private Listener propertyListener;
	
	@Override
    protected void init()
    {
        super.init();
        this.textDef = (TextDef)super.definition;
        this.modelElement = getModelElement();
        
        this.textFunction = this.textDef.getContent().content();
        this.functionResult = initExpression
        ( 
            this.modelElement,
            this.textFunction,
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast(new TextChangeEvent(TextPart.this));
                }
            }
        );
        this.property = FunctionUtil.getFunctionProperty(this.modelElement, this.functionResult);
        if (this.property != null)
        {
            this.propertyListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof PropertyValidationEvent )
                    {
                        runOnDisplayThread
                        (
                            new Runnable()
                            {
                                public void run()
                                {
                                    refreshValidation();
                                }
                            }
                        );
                    }
                }
            };
            
            this.property.attach(this.propertyListener);        
        }
        
        this.setEditable(!(this.textFunction instanceof Literal));
    }
	
    @Override
    public void dispose()
    {
        super.dispose();
        if (this.functionResult != null)
        {
            this.functionResult.dispose();
        }
        if (this.property != null)
        {
        	this.property.detach( this.propertyListener );
        }
    }
	
    @Override
    protected Status computeValidation()
    {
        if( this.property == null )
        {
            return Status.createOkStatus();
        }
        
    	return this.property.validation();
    }
    
    public String getContent()
    {
    	String value = null;
    	if (this.functionResult != null)
    	{
    		value = (String)this.functionResult.value();
    	}
    	return value;
    }
    
    public FunctionResult getContentFunction()
    {
    	return this.functionResult;
    }
        
    public Color getTextColor()
    {
    	return this.textDef.getColor().content();
    }

    public FontDef getFontDef() 
    {
    	return this.textDef.getFont();
    }
    
    public boolean truncatable()
    {
    	return this.textDef.isTruncatable().content();
    }
    
    public Property getTextProperty()
    {
    	return this.property;
    }
}

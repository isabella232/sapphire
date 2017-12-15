/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.def.ConditionalDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ConditionalPart extends SapphirePart
{
    private ConditionalDef def;
    private FunctionResult conditionFunctionResult;
    private List<SapphirePart> currentBranchContent;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.def = (ConditionalDef) this.definition;
        
        this.conditionFunctionResult = initExpression
        (
            getModelElement(),
            this.def.getCondition().getContent(), 
            Boolean.class,
            Literal.FALSE,
            new Runnable()
            {
                public void run()
                {
                    handleConditionChanged();
                }
            }
        );
        
        this.currentBranchContent = ListFactory.empty();
        
        handleConditionChanged( true );
    }
    
    private void handleConditionChanged()
    {
        handleConditionChanged( false );
    }
    
    private void handleConditionChanged( final boolean initializing )
    {
        for( SapphirePart part : this.currentBranchContent )
        {
            part.dispose();
        }
        
        final IModelElement element = getLocalModelElement();
        final boolean newConditionState = (Boolean) this.conditionFunctionResult.value();
        final ListFactory<SapphirePart> partsListFactory = ListFactory.start();
        
        final Listener childPartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        for( PartDef childPartDef : ( newConditionState ? this.def.getThenContent() : this.def.getElseContent() ) )
        {
            final SapphirePart part = create( this, element, childPartDef, this.params );
            part.attach( childPartListener );
            partsListFactory.add( part );
        }
        
        this.currentBranchContent = partsListFactory.result();
    
        refreshValidation();
        
        if( ! initializing )
        {
            broadcast( new PartChildrenEvent( ConditionalPart.this ) );
        }
    }
    
    public List<SapphirePart> getCurrentBranchContent()
    {
        return this.currentBranchContent;
    }
    
    public void render( final SapphireRenderingContext context )
    {
        for( SapphirePart child : this.currentBranchContent )
        {
            child.render( context );
        }
    }
    
    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : this.currentBranchContent )
        {
            factory.merge( child.validation() );
        }
        
        return factory.create();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphirePart child : this.currentBranchContent )
        {
            child.dispose();
        }
        
        if( this.conditionFunctionResult != null )
        {
            this.conditionFunctionResult.dispose();
        }
    }

}

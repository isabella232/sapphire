/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.ISapphireIfElseDirectiveDef;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class IfThenElsePart extends SapphirePart
{
    private ISapphireIfElseDirectiveDef def;
    private SapphireCondition condition;
    private List<SapphirePart> currentBranchContent;
    private List<SapphirePart> currentBranchContentReadOnly;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.def = (ISapphireIfElseDirectiveDef) this.definition;
        
        final Class<?> conditionClass;
        final Status conditionClassValidation = this.def.getConditionClass().validate();
        
        if( conditionClassValidation.severity() != Status.Severity.ERROR )
        {
            conditionClass = this.def.getConditionClass().resolve().artifact();
        }
        else
        {
            LoggingService.log( conditionClassValidation );
            conditionClass = null;
        }
        
        if( conditionClass != null )
        {
            final String conditionParameter = this.def.getConditionParameter().getText();
            this.condition = SapphireCondition.create( this, conditionClass, conditionParameter );
            
            if( this.condition != null )
            {
                this.condition.addListener
                (
                    new SapphireCondition.Listener()
                    {
                        @Override
                        public void handleConditionChanged()
                        {
                            IfThenElsePart.this.handleConditionChanged();
                        }
                    }
                );
            }
        }
        
        this.currentBranchContent = new ArrayList<SapphirePart>();
        this.currentBranchContentReadOnly = Collections.unmodifiableList( this.currentBranchContent );
        
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
        
        this.currentBranchContent.clear();
        
        if( this.condition != null )
        {
            final IModelElement element = getLocalModelElement();
            
            final SapphirePartListener childPartListener = new SapphirePartListener()
            {
                @Override
                public void handleValidateStateChange( final Status oldValidateState,
                                                       final Status newValidationState )
                {
                    updateValidationState();
                }
            };
        
            for( ISapphirePartDef childPartDef : ( this.condition.getConditionState() ? this.def.getThenContent() : this.def.getElseContent() ) )
            {
                final SapphirePart childPart = create( this, element, childPartDef, this.params );
                this.currentBranchContent.add( childPart );
                childPart.addListener( childPartListener );
            }
        }
        
        updateValidationState();
        
        if( ! initializing )
        {
            notifyStructureChangedEventListeners( new SapphirePartEvent( IfThenElsePart.this ) );
        }
    }
    
    public boolean getConditionState()
    {
        return ( this.condition != null ? this.condition.getConditionState() : false );
    }
    
    public List<SapphirePart> getCurrentBranchContent()
    {
        return this.currentBranchContentReadOnly;
    }
    
    public void render( final SapphireRenderingContext context )
    {
        for( SapphirePart child : this.currentBranchContent )
        {
            child.render( context );
        }
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : this.currentBranchContent )
        {
            factory.add( child.getValidationState() );
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
        
        if( this.condition != null )
        {
            this.condition.dispose();
        }
    }

}

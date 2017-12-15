/******************************************************************************
 * Copyright (c) 2011 Oracle
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

public final class SapphireIfElseDirective

    extends SapphirePart
    
{
    private ISapphireIfElseDirectiveDef def;
    private SapphireCondition condition;
    private List<SapphirePart> thenContent;
    private List<SapphirePart> thenContentReadOnly;
    private List<SapphirePart> elseContent;
    private List<SapphirePart> elseContentReadOnly;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement modelElement = getModelElement();
        
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
                            updateValidationState();
                            notifyStructureChangedEventListeners( new SapphirePartEvent( SapphireIfElseDirective.this ) );
                        }
                    }
                );
            }
        }
        
        this.thenContent = new ArrayList<SapphirePart>();
        this.thenContentReadOnly = Collections.unmodifiableList( this.thenContent );
        this.elseContent = new ArrayList<SapphirePart>();
        this.elseContentReadOnly = Collections.unmodifiableList( this.elseContent );

        final SapphirePartListener childPartListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final Status oldValidateState,
                                                   final Status newValidationState )
            {
                updateValidationState();
            }
        };
    
        for( ISapphirePartDef childPartDef : this.def.getThenContent() )
        {
            final SapphirePart childPart = create( this, modelElement, childPartDef, this.params );
            this.thenContent.add( childPart );
            childPart.addListener( childPartListener );
        }
        
        for( ISapphirePartDef childPartDef : this.def.getElseContent() )
        {
            final SapphirePart childPart = create( this, modelElement, childPartDef, this.params );
            this.elseContent.add( childPart );
            childPart.addListener( childPartListener );
        }
        
        updateValidationState();
    }
    
    public boolean getConditionState()
    {
        return ( this.condition != null ? this.condition.getConditionState() : false );
    }
    
    public List<SapphirePart> getThenContent()
    {
        return this.thenContentReadOnly;
    }
    
    public List<SapphirePart> getElseContent()
    {
        return this.elseContentReadOnly;
    }
    
    public void render( final SapphireRenderingContext context )
    {
        if( this.condition != null )
        {
            for( SapphirePart child : ( this.condition.getConditionState() == true ? this.thenContent : this.elseContent ) )
            {
                child.render( context );
            }
        }
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        if( this.condition != null )
        {
            for( SapphirePart child : ( this.condition.getConditionState() == true ? this.thenContent : this.elseContent ) )
            {
                factory.add( child.getValidationState() );
            }
        }
        
        return factory.create();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphirePart child : this.thenContent )
        {
            child.dispose();
        }

        for( SapphirePart child : this.elseContent )
        {
            child.dispose();
        }
        
        if( this.condition != null )
        {
            this.condition.dispose();
        }
    }

}

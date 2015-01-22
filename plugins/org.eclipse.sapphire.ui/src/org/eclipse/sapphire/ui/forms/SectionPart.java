/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import java.util.Collections;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.HelpSystem;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.SectionPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class SectionPart extends CompositePart
{
    private FunctionResult titleFunctionResult;
    private FunctionResult descriptionFunctionResult;
    private boolean folded;
    
    @Override
    protected final  void init()
    {
        super.init();
        
        final SectionDef def = definition();
        
        this.titleFunctionResult = initExpression
        (
            def.getLabel().content(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new TitleEvent() );
                }
            }
        );
        
        this.descriptionFunctionResult = initExpression
        (
            def.getDescription().content(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new DescriptionEvent() );
                }
            }
        );

        this.folded = def.getCollapsedInitially().content();
    }
    
    @Override
    public final SectionDef definition()
    {
        return (SectionDef) super.definition();
    }
    
    public final String title()
    {
        return (String) this.titleFunctionResult.value();
    }
    
    public final String description()
    {
        return (String) this.descriptionFunctionResult.value();
    }
    
    public final boolean folded()
    {
        return this.folded;
    }
    
    public final void fold()
    {
        if( ! this.folded )
        {
            this.folded = true;
            broadcast( new FoldingEvent() );
        }
    }
    
    public final void unfold()
    {
        if( this.folded )
        {
            this.folded = false;
            broadcast( new FoldingEvent() );
        }
    }
    
    @Override
    public final Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_SECTION );
    }

    @Override
    public final IContext getDocumentationContext()
    {
        final ISapphireDocumentation doc = this.definition.getDocumentation().content();
        
        if( doc != null )
        {
            ISapphireDocumentationDef docdef = null;
            
            if( doc instanceof ISapphireDocumentationDef )
            {
                docdef = (ISapphireDocumentationDef) doc;
            }
            else
            {
                docdef = ( (ISapphireDocumentationRef) doc ).resolve();
            }
            
            if( docdef != null )
            {
                return HelpSystem.getContext( docdef );
            }
        }
        
        return null;
    }
    
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new SectionPresentation( this, parent, composite );
    }
    
    public final class TitleEvent extends PartEvent
    {
        public TitleEvent()
        {
            super( SectionPart.this );
        }

        @Override
        public SectionPart part()
        {
            return (SectionPart) super.part();
        }
    }

    public final class DescriptionEvent extends PartEvent
    {
        public DescriptionEvent()
        {
            super( SectionPart.this );
        }

        @Override
        public SectionPart part()
        {
            return (SectionPart) super.part();
        }
    }
    
    public final class FoldingEvent extends PartEvent
    {
        public FoldingEvent()
        {
            super( SectionPart.this );
        }

        @Override
        public SectionPart part()
        {
            return (SectionPart) super.part();
        }
    }
    
}

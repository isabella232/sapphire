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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL_ABOVE;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.HtmlContent;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class HtmlPropertyEditorRenderer

    extends ValuePropertyEditorRenderer
    
{
    private Browser browser;
    
    public HtmlPropertyEditorRenderer( final SapphireRenderingContext context,
                                       final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();
        final ValueProperty property = (ValueProperty) part.getProperty();

        final boolean showLabelAbove = part.getRenderingHint( HINT_SHOW_LABEL_ABOVE, false );
        final boolean showLabelInline = part.getRenderingHint( HINT_SHOW_LABEL, ! showLabelAbove );
        Label label = null;
        
        final int baseIndent = part.getLeftMarginHint() + 9;
        
        if( showLabelInline || showLabelAbove )
        {
            label = new Label( parent, SWT.NONE );
            label.setText( property.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
            label.setLayoutData( gdhindent( gdhspan( gdvalign( gd(), SWT.TOP ), showLabelAbove ? 2 : 1 ), baseIndent ) );
            this.context.adapt( label );
        }

        setSpanBothColumns( ! showLabelInline );
        
        final Composite composite = createMainComposite( parent );
        composite.setLayout( glayout( 1, 9, 0, 0, 0 ) );

        this.browser = new Browser( composite, SWT.BORDER );
        this.browser.setLayoutData( gdfill() );
        
        addControl( this.browser );
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        
        String newHtmlContent = getPropertyValue().getText();
        
        if( newHtmlContent == null )
        {
            newHtmlContent = MiscUtil.EMPTY_STRING;
        }
        
        if( ! newHtmlContent.equals( this.browser.getText() ) )
        {
            this.browser.setText( newHtmlContent );
        }
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.browser.setFocus();
    }
    
    protected boolean canExpandVertically()
    {
        return true;
    }

    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            final ModelProperty property = propertyEditorDefinition.getProperty();
            
            if( property instanceof ValueProperty && property.hasAnnotation( HtmlContent.class ) )
            {
                return true;
            }
            
            return false;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new HtmlPropertyEditorRenderer( context, part );
        }
    }

}

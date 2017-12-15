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

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.util.SwtUtil.gd;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.glspacing;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;
import static org.eclipse.sapphire.ui.util.SwtUtil.valign;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.actions.ActionsRenderer;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.internal.EnhancedComposite;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractSlushBucketPropertyEditorRenderer

    extends DefaultListPropertyEditorRenderer
    
{
    public AbstractSlushBucketPropertyEditorRenderer( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();
        final ListProperty property = (ListProperty) part.getProperty();

        final int heightHint = part.getRenderingHint( ISapphirePartDef.HINT_HEIGHT, 10 ) * 15;
        final boolean showLabelInline = part.getRenderingHint( HINT_SHOW_LABEL, true );
        final int leftMargin = part.getLeftMarginHint();
        
        Label label = null;
        
        if( showLabelInline )
        {
            final String labelText = property.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true ) + ":";
            label = new Label( parent, SWT.NONE );
            label.setLayoutData( gdhindent( valign( gd(), SWT.TOP ), leftMargin + 9 ) );
            label.setText( labelText );
            this.context.adapt( label );
        }
        
        final Composite rootComposite = new EnhancedComposite( parent, SWT.NONE );
        rootComposite.setLayoutData( gdhindent( hspan( gdhhint( gdhfill(), heightHint ), showLabelInline ? 1 : 2 ), showLabelInline ? 0 : leftMargin ) );
        rootComposite.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        this.context.adapt( rootComposite );

        final Composite sourceTableComposite = new Composite( rootComposite, SWT.NONE );
        sourceTableComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 0 ) );
        this.context.adapt( sourceTableComposite );
        
        final Composite decoratorComposite = new Composite( sourceTableComposite, SWT.NONE );
        decoratorComposite.setLayoutData( valign( gd(), SWT.TOP ) );
        decoratorComposite.setLayout( glayout( 1, 0, 2, 0, 0 ) );
        this.context.adapt( decoratorComposite );
        
        final PropertyEditorAssistDecorator decorator 
            = new PropertyEditorAssistDecorator( part, this.context, decoratorComposite );
        
        decorator.getControl().setLayoutData( gd() );
        setDecorator( decorator );
        
        final Control sourceTable = createSourceControl( sourceTableComposite );
        sourceTable.setLayoutData( gdfill() );
        this.context.adapt( sourceTable );
        
        final Composite toolbarComposite = new Composite( sourceTableComposite, SWT.NONE );
        toolbarComposite.setLayoutData( gd() );
        toolbarComposite.setLayout( glayout( 1, 5, 5, 0, 0 ) );
        this.context.adapt( toolbarComposite );
        
        final ToolBar toolbar = new ToolBar( toolbarComposite, SWT.FLAT | SWT.VERTICAL );
        toolbar.setLayoutData( gd() );
        this.context.adapt( toolbar );
        
        final Composite tableComposite = new Composite( rootComposite, SWT.NONE );
        tableComposite.setLayout( glayout( 2, 0, 0 ) );
        this.context.adapt( tableComposite );
        
        decorator.addEditorControl( rootComposite );
        decorator.addEditorControl( sourceTableComposite );
        decorator.addEditorControl( decoratorComposite );
        decorator.addEditorControl( sourceTable );
        decorator.addEditorControl( toolbarComposite );
        decorator.addEditorControl( toolbar );
        
        final Table mainTable = (Table) super.createContents( tableComposite, true, true );

        final Action moveRightAction = createAddAction();
        moveRightAction.setLabel( Resources.moveRightActionLabel );
        moveRightAction.setImageDescriptor( SapphireImageCache.ACTION_MOVE_RIGHT );
        moveRightAction.setPart( part );
        
        final ActionGroup moveRightActionGroup = new ActionGroup();
        moveRightActionGroup.addAction( moveRightAction );
        
        final List<ActionGroup> moveRightActions = new ArrayList<ActionGroup>();
        moveRightActions.add( moveRightActionGroup );
        ActionsRenderer.fillToolBar( toolbar, moveRightActions );

        final List<Control> relatedControls = getRelatedControls( mainTable );
        
        relatedControls.add( sourceTable );
        relatedControls.add( toolbar );
        
        if( label != null )
        {
            relatedControls.add( label );
        }
        
        ( (AbstractBinding) mainTable.getData( DATA_BINDING ) ).updateTargetAttributes();
        
        addControl( sourceTable );
    }
    
    protected abstract Control createSourceControl( Composite parent );

    protected abstract Action createAddAction();
    
    @SuppressWarnings( "unchecked" )
    private static List<Control> getRelatedControls( final Control control )
    {
        return (List<Control>) control.getData( RELATED_CONTROLS );
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String moveRightActionLabel;
        
        static
        {
            initializeMessages( AbstractSlushBucketPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }

}

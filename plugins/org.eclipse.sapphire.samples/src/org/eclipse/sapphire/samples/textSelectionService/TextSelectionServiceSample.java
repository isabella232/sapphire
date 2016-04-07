/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation review and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.textSelectionService;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ExecutableElement;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.TextSelectionService;
import org.eclipse.sapphire.ui.TextSelectionService.Range;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.SapphireDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextSelectionServiceSample
{
    public interface RootElement extends Element
    {
        ElementType TYPE = new ElementType( TextSelectionServiceSample.RootElement.class );
        
        // *** Text ***
        
        ValueProperty PROP_TEXT = new ValueProperty( TYPE, "Text" );
        
        Value<String> getText();
        void setText( String value );
        
        // *** TextSelection ***
        
        ValueProperty PROP_TEXT_SELECTION = new ValueProperty( TYPE, "TextSelection" );
        
        Value<String> getTextSelection();
        void setTextSelection( String value );
    }
    
    public static final class PropertyEditorListener extends Listener
    {
        private TextSelectionService textSelectionService;
        private Listener textSelectionServiceListener;
        
        @Override
        
        public void handle( final Event event )
        {
            if( event instanceof SapphirePart.PartInitializationEvent )
            {
                final PropertyEditorPart part = (PropertyEditorPart) ( (SapphirePart.PartInitializationEvent) event ).part();
                final Property property = part.property();
                final Value<?> textSelectionProperty = (Value<?>) property.element().property( property.name() + "Selection" );
                
                this.textSelectionServiceListener = new Listener()
                {
                    @Override
                    
                    public void handle( final Event event )
                    {
                        textSelectionProperty.write( PropertyEditorListener.this.textSelectionService.selection().toString() );
                    }
                };
                
                this.textSelectionService = part.service( TextSelectionService.class );
                this.textSelectionService.attach( this.textSelectionServiceListener );
                this.textSelectionServiceListener.handle( null );
            }
            else if( event instanceof DisposeEvent )
            {
                this.textSelectionService.detach( this.textSelectionServiceListener );
            }
        }
    }
    
    public static final class OpenSampleHandler extends AbstractHandler
    {
        @Override
        
        public Object execute( final ExecutionEvent event )
        {
            final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );
            
            final RootElement element = RootElement.TYPE.instantiate();
            
            try
            {
                final SapphireDialog dialog = new SapphireDialog
                (
                    window.getShell(),
                    element,
                    DefinitionLoader.sdef( TextSelectionServiceSample.class ).dialog()
                );
                
                dialog.open();
            }
            finally
            {
                element.dispose();
            }
            
            return null;
        }
    }
    
    public interface InsertEnvironmentVariablesOp extends ExecutableElement
    {
        ElementType TYPE = new ElementType( TextSelectionServiceSample.InsertEnvironmentVariablesOp.class );
        
        // *** Context ***
        
        @Type( base = PropertyEditorPart.class )
        
        TransientProperty PROP_CONTEXT = new TransientProperty( TYPE, "Context" );
        
        Transient<PropertyEditorPart> getContext();
        void setContext( PropertyEditorPart value );

        // *** Variables ***
        
        interface Variable extends Element
        {
            ElementType TYPE = new ElementType( TextSelectionServiceSample.InsertEnvironmentVariablesOp.Variable.class );
            
            // *** Name ***
            
            @Required
            @Unique
            
            ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
            
            Value<String> getName();
            void setName( String value );
        }
        
        class VariablesPossibleValuesService extends PossibleValuesService
        {
            @Override
            
            protected void compute( final Set<String> values )
            {
                for( final String variable : System.getenv().keySet() )
                {
                    values.add( variable );
                }
            }
        }
        
        @Type( base = Variable.class )
        @Service( impl = VariablesPossibleValuesService.class )
        
        ListProperty PROP_VARIABLES = new ListProperty( TYPE, "Variables" );
        
        ElementList<Variable> getVariables();
        
        // *** execute ***
        
        class ExecuteMethodDelegate
        {
            public static Status execute( final InsertEnvironmentVariablesOp op, final ProgressMonitor monitor )
            {
                final PropertyEditorPart context = op.getContext().content();
                final Value<?> property = (Value<?>) context.property();
                final TextSelectionService textSelectionService = context.service( TextSelectionService.class );
                final TextSelectionService.Range initialSelection = textSelectionService.selection();
                final String initialText = property.text();
                
                final StringBuilder modifiedText = new StringBuilder();
                
                if( initialText != null )
                {
                    modifiedText.append( initialText.substring( 0, initialSelection.start() ) );
                }
                
                final StringBuilder variables = new StringBuilder();
                
                for( final InsertEnvironmentVariablesOp.Variable variable : op.getVariables() )
                {
                    variables.append( "${" );
                    variables.append( variable.getName().text() );
                    variables.append( '}' );
                }
                
                modifiedText.append( variables );
                
                if( initialText != null )
                {
                    modifiedText.append( initialText.substring( initialSelection.end() ) );
                }
                
                property.write( modifiedText.toString() );
                
                textSelectionService.select( initialSelection.start() + variables.length() );
                
                return Status.createOkStatus();
            }
        }
        
        @DelegateImplementation( ExecuteMethodDelegate.class )
        
        Status execute( ProgressMonitor monitor );
    }
    
    public static final class InsertEnvironmentVariablesActionHandler extends SapphireActionHandler
    {
        @Override
        
        protected final Object run( final Presentation context )
        {
            final InsertEnvironmentVariablesOp operation = InsertEnvironmentVariablesOp.TYPE.instantiate();
            
            try
            {
                operation.setContext( (PropertyEditorPart) context.part() );
                
                final SapphireDialog dialog = new SapphireDialog
                (
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    operation,
                    DefinitionLoader.sdef( TextSelectionServiceSample.class ).dialog( "InsertEnvironmentVariablesDialog" )
                );
                
                dialog.open();
            }
            finally
            {
                operation.dispose();
            }
            
            return null;
        }
    }
    
    private static abstract class MoveActionHandler extends SapphireActionHandler
    {
        private PropertyEditorPart part;
        private Value<?> property;
        private TextSelectionService textSelectionService;
        
        @Override
        
        public final void init( final SapphireAction action, final ActionHandlerDef def )
        {
            super.init( action, def );
            
            this.part = (PropertyEditorPart) action.getPart();
            this.property = (Value<?>) this.part.property();
            this.textSelectionService = this.part.service( TextSelectionService.class );

            refreshEnablement();
            
            final Listener textSelectionServiceListener = new Listener()
            {
                @Override
                
                public void handle( final Event event )
                {
                    refreshEnablement();
                }
            };
            
            this.textSelectionService.attach( textSelectionServiceListener );
            
            final Listener propertyContentListener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    refreshEnablement();
                }
            };
            
            this.property.attach( propertyContentListener );
            
            attach
            (
                new FilteredListener<DisposeEvent>()
                {
                    @Override
                    
                    protected void handleTypedEvent( final DisposeEvent event )
                    {
                        MoveActionHandler.this.textSelectionService.detach( textSelectionServiceListener );
                        MoveActionHandler.this.part.detach( propertyContentListener );
                    }
                }
            );
        }
        
        private void refreshEnablement()
        {
            refreshEnablement( text(), this.textSelectionService.selection() );
        }
        
        protected abstract void refreshEnablement( String text, TextSelectionService.Range selection );
        
        @Override
        
        protected final Object run( final Presentation context )
        {
            run( text(), this.textSelectionService );
            
            return null;
        }
        
        protected abstract void run( String text, TextSelectionService textSelectionService );
        
        private String text()
        {
            final String text = this.property.text();
            return ( text != null ? text : "" );
        }
    }

    public static final class MoveLeftActionHandler extends MoveActionHandler
    {
        @Override
        
        protected void refreshEnablement( final String text, final Range selection )
        {
            setEnabled( selection.end() > 0 );
        }

        @Override
        
        protected void run( final String text, final TextSelectionService textSelectionService )
        {
            final TextSelectionService.Range selection = textSelectionService.selection();
            
            if( selection.end() > 0 )
            {
                textSelectionService.select( max( 0, selection.start() - 1 ), selection.end() - 1 );
            }
        }
    }
    
    public static final class MoveRightActionHandler extends MoveActionHandler
    {
        @Override
        
        protected void refreshEnablement( final String text, final Range selection )
        {
            setEnabled( selection.start() < text.length() );
        }

        @Override
        
        protected void run( final String text, final TextSelectionService textSelectionService )
        {
            final TextSelectionService.Range selection = textSelectionService.selection();
            
            if( selection.start() < text.length() )
            {
                textSelectionService.select( selection.start() + 1, min( selection.end() + 1, text.length() ) );
            }
        }
    }
    
    
    
}

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

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.logError;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.def.ICompositeParam;
import org.eclipse.sapphire.ui.def.ISapphireActionLinkDef;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireCompositeRef;
import org.eclipse.sapphire.ui.def.ISapphireCustomPartDef;
import org.eclipse.sapphire.ui.def.ISapphireDialogDef;
import org.eclipse.sapphire.ui.def.ISapphireElementPropertyCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireIfElseDirectiveDef;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.def.ISapphirePageBookExtDef;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphirePartListenerDef;
import org.eclipse.sapphire.ui.def.ISapphirePropertyEditorDef;
import org.eclipse.sapphire.ui.def.ISapphireSeparatorDef;
import org.eclipse.sapphire.ui.def.ISapphireSpacerDef;
import org.eclipse.sapphire.ui.def.ISapphireStaticTextFieldDef;
import org.eclipse.sapphire.ui.def.ISapphireTabGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;
import org.eclipse.sapphire.ui.def.PageBookPartControlMethod;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePart

    implements ISapphirePart
    
{
    private ISapphirePart parent;
    private IModelElement modelElement;
    protected ISapphirePartDef definition;
    protected Map<String,String> params;
    private ModelElementListener modelElementListener;
    private IStatus validationState;
    private Set<SapphirePartListener> listeners;
    private SapphireImageCache imageCache;
    
    public final void init( final ISapphirePart parent,
                            final IModelElement modelElement,
                            final ISapphirePartDef definition,
                            final Map<String,String> params )
    {
        this.parent = parent;
        this.definition = definition;
        this.params = params;
        this.imageCache = ( this.parent == null ? new SapphireImageCache() : this.parent.getImageCache() );

        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.modelElement = modelElement;
        
        this.modelElementListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                handleModelElementChange( event );
            }
        };
        
        this.modelElement.addListener( this.modelElementListener );
        
        this.validationState = Status.OK_STATUS;
        this.listeners = null;
        
        for( ISapphirePartListenerDef listenerDefinition : definition.getListeners() )
        {
            final Class<?> listenerClass = listenerDefinition.getListenerClass().resolve();
            
            if( listenerClass != null )
            {
                Object listener = null;
                
                try
                {
                    listener = listenerClass.newInstance();
                }
                catch( Exception e )
                {
                    final String msg = NLS.bind( Resources.failedToInstantiate, listenerClass.getName() );
                    logError( msg, e );
                }
                
                if( listener != null )
                {
                    if( listener instanceof SapphirePartListener )
                    {
                        addListener( (SapphirePartListener) listener );
                    }
                    else
                    {
                        final String msg = NLS.bind( Resources.doesNotExtend, listenerClass.getName() );
                        logError( msg );
                    }
                }
            }
        }
        
        init();
        
        updateValidationState();
    }
    
    protected void init()
    {
        // The default implement doesn't do anything.
    }
    
    public abstract void render( final SapphireRenderingContext context );
    
    public ISapphirePartDef getDefinition()
    {
        return this.definition;
    }
    
    public final ISapphirePart getParentPart()
    {
        return this.parent;
    }
    
    @SuppressWarnings( "unchecked" )
    public final <T> T getNearestPart( final Class<T> partType )
    {
        if( partType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            if( this.parent != null )
            {
                return this.parent.getNearestPart( partType );
            }
            else
            {
                return null;
            }
        }
    }
    
    public final IModelElement getModelElement()
    {
        return this.modelElement;
    }
    
    public Action getAction( final String id )
    {
        if( this.parent != null )
        {
            return this.parent.getAction( id );
        }
        else
        {
            return null;
        }
    }
    
    public final IStatus getValidationState()
    {
        return this.validationState;
    }
    
    protected IStatus computeValidationState()
    {
        return Status.OK_STATUS;
    }
    
    public final void updateValidationState()
    {
        final IStatus newValidationState = computeValidationState();
        boolean updateNeeded = false;
        
        if( this.validationState != newValidationState )
        {
            if( this.validationState == null || newValidationState == null )
            {
                updateNeeded = true;
            }
            else
            {
                if( this.validationState.getSeverity() != newValidationState.getSeverity() ||
                    ! this.validationState.getMessage().equals( newValidationState.getMessage() ) )
                {
                    updateNeeded = true;
                }
            }
        }
        
        if( updateNeeded )
        {
            final IStatus oldValidationState = this.validationState;
            this.validationState = newValidationState;
            
            if( this.listeners != null )
            {
                for( SapphirePartListener listener : this.listeners )
                {
                    try
                    {
                        listener.handleValidateStateChange( oldValidationState, newValidationState );
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
                }
            }
        }
    }
    
    public boolean setFocus()
    {
        return false;
    }
    
    public boolean setFocus( final ModelPath path )
    {
        return false;
    }
    
    public final boolean setFocus( final String path )
    {
        return setFocus( new ModelPath( path ) );
    }
    
    protected final void notifyFocusRecievedEventListeners()
    {
        if( this.listeners != null )
        {
            final SapphirePartEvent event = new SapphirePartEvent( new SapphirePartContext( this ) );
            
            for( SapphirePartListener listener : this.listeners )
            {
                try
                {
                    listener.handleFocusReceivedEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }
    
    public String getHelpContextId()
    {
        return null;
    }
    
    public SapphireImageCache getImageCache()
    {
        return this.imageCache;
    }
    
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        // The default implement doesn't do anything.
    }
    
    public final void addListener( final SapphirePartListener listener )
    {
        if( this.listeners == null )
        {
            this.listeners = Collections.singleton( listener );
        }
        else
        {
            this.listeners = new HashSet<SapphirePartListener>( this.listeners );
            this.listeners.add( listener );
        }
    }
    
    public final void removeListener( final SapphirePartListener listener )
    {
        if( this.listeners != null )
        {
            if( this.listeners.contains( listener ) )
            {
                if( this.listeners.size() == 1 )
                {
                    this.listeners = null;
                }
                else
                {
                    this.listeners = new HashSet<SapphirePartListener>( this.listeners );
                    this.listeners.remove( listener );
                }
            }
        }
    }
    
    public final Set<SapphirePartListener> getListeners()
    {
        if( this.listeners == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return this.listeners;
        }
    }
    
    public final void notifyStructureChangedEventListeners( final SapphirePartEvent event )
    {
        if( this.listeners != null )
        {
            for( SapphirePartListener listener : this.listeners )
            {
                try
                {
                    listener.handleStructureChangedEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
        
        if( this.parent != null && this.parent instanceof SapphirePart )
        {
            ( (SapphirePart) this.parent ).notifyStructureChangedEventListeners( event );
        }
    }
    
    public final ModelProperty resolve( final String propertyName )
    {
        return resolve( this.modelElement, propertyName );
    }

    public final ModelProperty resolve( final IModelElement modelElement,
                                        String propertyName )
    {
        if( propertyName != null )
        {
            propertyName = propertyName.trim();
            
            if( propertyName.startsWith( "@{" ) && propertyName.endsWith( "}" ) )
            {
                propertyName = propertyName.substring( 2, propertyName.length() - 1 );
                propertyName = this.params.get( propertyName );
            }
            
            final ModelElementType type = modelElement.getModelElementType();
            final ModelProperty property = type.getProperty( propertyName );
            
            if( property == null )
            {
                throw new RuntimeException( "Could not find property " + propertyName + " in " + type.getQualifiedName() );
            }
        
            return property;
        }
        
        return null;
    }
    
    public void dispose()
    {
        this.modelElement.removeListener( this.modelElementListener );
        
        if( this.parent == null )
        {
            this.imageCache.dispose();
        }
    }
    
    public static final SapphirePart create( final SapphirePart parent,
                                             final IModelElement modelElement,
                                             final ISapphirePartDef definition,
                                             final Map<String,String> params )
    {
        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        SapphirePart part = null;
        Map<String,String> partParams = params;
        ISapphirePartDef def = definition;
        
        if( definition instanceof ISapphirePropertyEditorDef )
        {
            part = new SapphirePropertyEditor();
        }
        else if( definition instanceof ISapphireLabelDef )
        {
            part = new SapphireLabel();
        }
        else if( definition instanceof ISapphireSeparatorDef )
        {
            part = new SapphireSeparator();
        }
        else if( definition instanceof ISapphireSpacerDef )
        {
            part = new SapphireSpacer();
        }
        else if( definition instanceof ISapphireActionLinkDef )
        {
            part = new SapphireActionLink(); 
        }
        else if( definition instanceof ISapphireCustomPartDef )
        {
            final Class<?> customPartImplClass = ( (ISapphireCustomPartDef) definition ).getImplClass().resolve();
            
            if( customPartImplClass != null )
            {
                try
                {
                    part = (SapphirePart) customPartImplClass.newInstance();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
        else if( definition instanceof ISapphireStaticTextFieldDef )
        {
            part = new SapphireStaticTextField();
        }
        else if( definition instanceof ISapphireGroupDef )
        {
            part = new SapphireGroup();
        }
        else if( definition instanceof ISapphireWithDirectiveDef )
        {
            part = new SapphireWithDirective();
        }
        else if( definition instanceof ISapphirePageBookExtDef )
        {
            final ISapphirePageBookExtDef pageBookPartDef = (ISapphirePageBookExtDef) definition;
            
            if( pageBookPartDef.getControlMethod().getContent() == PageBookPartControlMethod.ENUM_VALUE )
            {
                part = new SapphireEnumControlledPageBook();
            }
            else
            {
                part = new SapphireListControlledPageBook();
            }
        }
        else if( definition instanceof ISapphireElementPropertyCompositeDef )
        {
            part = new SapphireElementPropertyComposite();
        }
        else if( definition instanceof ISapphireDialogDef )
        {
            part = new SapphireDialogPart();
        }
        else if( definition instanceof ISapphireWizardPageDef )
        {
            part = new SapphireWizardPagePart();
        }
        else if( definition instanceof ISapphireCompositeDef )
        {
            part = new SapphireComposite();
        }
        else if( definition instanceof ISapphireCompositeRef )
        {
            final ISapphireCompositeRef compositeRef = (ISapphireCompositeRef) definition;
            def = compositeRef.resolve();
            
            if( def != null )
            {
                partParams = new HashMap<String,String>( params );
                
                for( ICompositeParam param : compositeRef.getParams() )
                {
                    final String paramName = param.getName().getText();
                    final String paramValue = param.getValue().getText();
                    
                    if( paramName != null && paramValue != null )
                    {
                        partParams.put( paramName, paramValue );
                    }
                }
                
                part = new SapphireComposite();
            }
        }
        else if( definition instanceof ISapphireTabGroupDef )
        {
            part = new SapphireTabGroup();
        }
        else if( definition instanceof ISapphireIfElseDirectiveDef )
        {
            part  = new SapphireIfElseDirective();
        }
        
        if( part == null )
        {
            throw new IllegalStateException();
        }
        
        part.init( parent, modelElement, def, partParams );
        
        return part;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String failedToInstantiate;
        public static String doesNotExtend;
        
        static
        {
            initializeMessages( SapphirePart.class.getName(), Resources.class );
        }
    }
    
}

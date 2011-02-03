/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;
import org.eclipse.sapphire.ui.internal.SapphireActionManager;
import org.eclipse.sapphire.ui.internal.SapphireEditorContentOutline;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireEditor

    extends FormEditor
    implements ISapphirePart
    
{
    private static final String PREFS_LAST_ACTIVE_PAGE = "LastActivePage"; //$NON-NLS-1$
    private static final String PREFS_GLOBAL = "Global"; //$NON-NLS-1$
    private static final String PREFS_INSTANCE_BY_URI = "InstanceByUri"; //$NON-NLS-1$
    private static final String PREFS_INSTANCE_BY_EDITOR_INPUT_TYPE = "InstanceByEditorInputType"; //$NON-NLS-1$
    
    private final String pluginId;
    private IModelElement model;
    private IResourceChangeListener fileChangeListener;
    private final SapphireImageCache imageCache;
    private final Map<String,Object> pagesById;
    private SapphireEditorContentOutline outline;
    private final SapphireActionManager actionsManager;
    
    public SapphireEditor( final String pluginId )
    {
        this.pluginId = pluginId;
        this.imageCache = new SapphireImageCache();
        this.pagesById = new HashMap<String,Object>();
        this.outline = null;
        this.actionsManager = new SapphireActionManager( this, getActionContexts() );
    }
    
    public ISapphirePartDef getDefinition()
    {
        return null;
    }
    
    public final IModelElement getModelElement()
    {
        return this.model;
    }

    protected abstract IModelElement createModel();
    
    protected void adaptModel( final IModelElement model )
    {
        final CorruptedResourceExceptionInterceptor interceptor 
            = new CorruptedResourceExceptionInterceptorImpl( getEditorSite().getShell() );
        
        this.model.resource().setCorruptedResourceExceptionInterceptor( interceptor );
    }
    
    public final Preferences getGlobalPreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        final Preferences prefs = getPreferencesRoot( createIfNecessary );
        
        if( prefs != null && ( prefs.nodeExists( PREFS_GLOBAL ) || createIfNecessary ) )
        {
            return prefs.node( PREFS_GLOBAL );
        }
        
        return null;
    }
    
    public final Preferences getInstancePreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        final IEditorInput editorInput = getEditorInput();
        final String level1;
        final String level2;
        
        if( editorInput instanceof IURIEditorInput )
        {
            level1 = PREFS_INSTANCE_BY_URI;
            
            final URI uri = ( (IURIEditorInput) editorInput ).getURI();
            
            if( uri != null )
            {
                level2 = ( (IURIEditorInput) editorInput ).getURI().toString();
            }
            else
            {
                level2 = "$#%**invalid**%#$";
            }
        }
        else
        {
            level1 = PREFS_INSTANCE_BY_EDITOR_INPUT_TYPE;
            level2 = editorInput.getClass().getName();
        }
        
        Preferences prefs = getPreferencesRoot( createIfNecessary );
        
        if( prefs != null && ( prefs.nodeExists( level1 ) || createIfNecessary ) )
        {
            prefs = prefs.node( level1 );
            
            if( prefs.nodeExists( level2 ) || createIfNecessary )
            {
                return prefs.node( level2 );
            }
        }
        
        return null;
    }
    
    private final Preferences getPreferencesRoot( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        final InstanceScope scope = new InstanceScope();
        final Preferences prefs = scope.getNode( this.pluginId );
        final String editorId = getClass().getName();
        
        if( prefs.nodeExists( editorId ) || createIfNecessary )
        {
            return prefs.node( editorId );
        }
        
        return null;
    }

    public final String getLastActivePage()
    {
        String lastActivePage = getPageId( this.pages.get( 0 ) );
        
        try
        {
            final Preferences prefs = getInstancePreferences( false );
            
            if( prefs != null )
            {
                lastActivePage = prefs.get( PREFS_LAST_ACTIVE_PAGE, lastActivePage );
            }
        }
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
        
        return lastActivePage;
    }

    public final void setLastActivePage( final String pageId )
    {
        try
        {
            final Preferences prefs = getInstancePreferences( true );
            
            if( prefs != null )
            {
                prefs.put( PREFS_LAST_ACTIVE_PAGE, pageId );
                prefs.flush();
            }
        }
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }

    public IFile getFile()
    {
        final IEditorInput editorInput = getEditorInput();
        
        if( editorInput instanceof FileEditorInput )
        {
            return ( (FileEditorInput) editorInput ).getFile();
        }
        else
        {
            return null;
        }
    }

    public final IProject getProject()
    {
        final IFile ifile = getFile();
        return ( ifile == null ? null : ifile.getProject() );
    }
    
    public final void init( final IEditorSite site, 
                            final IEditorInput input )
    
        throws PartInitException
        
    {
        super.init( site, input );
        
        doSetInput( input );
    }
    
    protected final void setInput( final IEditorInput input ) 
    {
        doSetInput( input );
        super.setInput( input );
    }

    @Override
    protected final void setInputWithNotify( final IEditorInput input ) 
    {
        doSetInput( input );
        super.setInputWithNotify( input );
    }

    private void doSetInput( final IEditorInput input )
    {
        setPartName( input.getName() );
    }

    protected final void addPages() 
    {
        final IFile file = getFile();
        
        if( file.isAccessible() )
        {
            try 
            {
                createSourcePages();
                
                this.model = createModel();
                adaptModel( this.model );
                
                createDiagramPages();
                
                createFormPages();
    
                createFileChangeListener();
            }
            catch( PartInitException e ) 
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            final String lastActivePage = getLastActivePage();
            int page = 0;
            
            if( lastActivePage != null )
            {
                int count = getPageCount();
                for (int i = 0; i < count; i++) {
                    String title = getPageText(i);
                    if (lastActivePage.equals(title)) {
                        page = i;
                        break;
                    }
                }
            }
            
            setActivePage( page );
        }
        else
        {
            final Composite page = new Composite( getContainer(), SWT.NONE );
            page.setLayout( glayout( 1 ) );
            page.setBackground( getSite().getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

            final SapphireFormText message = new SapphireFormText( page, SWT.NONE );
            message.setLayoutData( gd() );
            message.setBackground( getSite().getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
            message.setText( Resources.resourceNotAccessible, false, false );
            
            addPage( page );
            setPageText( 0, Resources.errorPageTitle );
        }
    }
    
    protected abstract void createSourcePages() throws PartInitException;
    protected abstract void createFormPages() throws PartInitException;
    
    // default impl does nothing, subclass may override it to add diagram pages
    protected void createDiagramPages() throws PartInitException
    {
    	
    }
    
    protected final void setPageId( final Object page,
                                    final String id )
    {
        this.pagesById.put( id, page );
    }
    
    public final Object getPage()
    {
        final int pageIndex = getActivePage();
        
        if( pageIndex == -1 )
        {
            return null;
        }
        else
        {
            return this.pages.get( pageIndex );
        }
    }
    
    public final Object getPage( final String id )
    {
        Object page = this.pagesById.get( id );
        
        if( page == null )
        {
            for( Object p : this.pages )
            {
                if( p instanceof SapphireEditorFormPage && ( (SapphireEditorFormPage) p ).getId().equals( id ) )
                {
                    page = p;
                    break;
                }
            }
        }
        
        return page;
    }
    
    public final String getPageId( final Object page )
    {
        String retId = null;
        
        for( String id : this.pagesById.keySet() )
        {
            final Object p = this.pagesById.get( id );
            
            if( p == page )
            {
                retId = id;
                break;
            }
        }
        
        return retId;
    }
    
    public final void showPage( final String id )
    {
        final Object page = getPage( id );
        
        if( page != null )
        {
            showPage( page );
            final int index = this.pages.indexOf( page );
            setActivePage( index );
        }
    }
    
    public final void showPage( final Object page )
    {
        final int index = this.pages.indexOf( page );
        setActivePage( index );
    }

    @Override
    protected void pageChange( final int pageIndex )
    {
        super.pageChange( pageIndex );
        
        setLastActivePage( getPageText( pageIndex ) );
        
        if( this.outline != null && ! this.outline.isDisposed() )
        {
            this.outline.refresh();
        }
        
        final Object page = this.pages.get( pageIndex );
        
        if( page instanceof SapphireEditorFormPage )
        {
            ( (SapphireEditorFormPage) page ).setFocus();
        }
    }
    
    public void doSave( final IProgressMonitor monitor ) 
    {
        try
        {
            this.model.resource().save();
        }
        catch( ResourceStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }

    public void doSaveAs() 
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean isSaveAsAllowed() 
    {
        return false;
    }
    
    protected final void createFileChangeListener()
    {
        this.fileChangeListener = new IResourceChangeListener()
        {
            public void resourceChanged( final IResourceChangeEvent event )
            {
                handleFileChangedEvent( event );
            }
        };
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this.fileChangeListener, IResourceChangeEvent.POST_CHANGE );
    }

    protected final void handleFileChangedEvent( final IResourceChangeEvent event )
    {
        final IResourceDelta delta = event.getDelta();
        
        if( delta != null )
        {
            final IResourceDelta localDelta = delta.findMember( getFile().getFullPath() );
            
            if( localDelta != null )
            {
                PlatformUI.getWorkbench().getDisplay().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            if( localDelta.getKind() == IResourceDelta.REMOVED )
                            {
                                getSite().getPage().closeEditor( SapphireEditor.this, false );
                            }
                        }
                    }
                );
            }
        }
    }

    @Override
    public void dispose() 
    {
        super.dispose();
        
        if( this.fileChangeListener != null )
        {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener( this.fileChangeListener );
        }
        
        this.imageCache.dispose();
        this.actionsManager.dispose();
        this.model.dispose();
    }
    
    @Override
    @SuppressWarnings( "rawtypes" )
    
    public Object getAdapter( final Class adapter ) 
    {
        if( adapter == IContentOutlinePage.class )
        {
            if( this.outline == null || this.outline.isDisposed() )
            {
                this.outline = new SapphireEditorContentOutline( this );
            }
            
            return this.outline;
        }
        else
        {
            return super.getAdapter( adapter );
        }
    }
    
    public final IContentOutlinePage getContentOutlineForActivePage()
    {
        final int activePageIndex = getActivePage();
        final Object page = this.pages.get( activePageIndex );
        return getContentOutline( page );
    }
    
    public IContentOutlinePage getContentOutline( final Object page )
    {
        if( page instanceof MasterDetailsPage )
        {
            final MasterDetailsPage mdpage = (MasterDetailsPage) page;
            return mdpage.getContentOutlinePage();
        }
        
        return null;
    }
    
    // *********************
    // ISapphirePart Methods
    // *********************
    
    public ISapphirePart getParentPart()
    {
        return null;
    }
    
    @SuppressWarnings( "unchecked" )
    public <T> T getNearestPart( final Class<T> partType )
    {
        if( partType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }
    
    public Set<String> getActionContexts()
    {
        return Collections.emptySet();
    }
    
    public final String getMainActionContext()
    {
        return this.actionsManager.getMainActionContext();
    }
    
    public final SapphireActionGroup getActions()
    {
        return this.actionsManager.getActions();
    }
    
    public final SapphireActionGroup getActions( final String context )
    {
        return this.actionsManager.getActions( context );
    }

    public final SapphireAction getAction( final String id )
    {
        return this.actionsManager.getAction( id );
    }
    
    public IStatus getValidationState()
    {
        throw new UnsupportedOperationException();
    }
    
    public IContext getDocumentationContext()
    {
        return null;
    }

    public SapphireImageCache getImageCache()
    {
        return this.imageCache;
    }
    
    public void collectAllReferencedProperties( final Set<ModelProperty> collection )
    {
        throw new UnsupportedOperationException();
    }
    
    public void addListener( final SapphirePartListener listener )
    {
        throw new UnsupportedOperationException();
    }
    
    public void removeListener( final SapphirePartListener listener )
    {
        throw new UnsupportedOperationException();
    }
    
    private static final class Resources extends NLS
    {
        public static String resourceNotAccessible;
        public static String errorPageTitle;
        
        static
        {
            initializeMessages( SapphireEditor.class.getName(), Resources.class );
        }
    }
    
}

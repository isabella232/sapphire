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

package org.eclipse.sapphire.ui.swt.renderer.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.BasePathsProviderImpl;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileExtensions;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class RelativePathBrowseActionHandler 

    extends SapphireBrowseActionHandler
    
{
    public static final String ID = "Sapphire.Browse.Path.Relative";
    
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_EXTENSIONS = "extensions";
    public static final String PARAM_LEADING_SLASH = "leading-slash";
    
    private List<String> extensions;
    private FileSystemResourceType type;
    private boolean includeLeadingSlash;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        setLabel( Resources.label );
        addImage( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_OBJ_FILE ) );
        
        final ValueProperty property = getProperty();
        
        this.type = null;
        
        final String paramType = def.getParam( PARAM_TYPE );
        
        if( paramType != null )
        {
            if( paramType.equalsIgnoreCase( "file" ) )
            {
                this.type = FileSystemResourceType.FILE;
            }
            else if( paramType.equalsIgnoreCase( "folder" ) )
            {
                this.type = FileSystemResourceType.FOLDER;
            }
        }
        else
        {
            final ValidFileSystemResourceType validFileSystemResourceTypeAnnotation
                = property.getAnnotation( ValidFileSystemResourceType.class );
        
            if( validFileSystemResourceTypeAnnotation != null )
            {
                this.type = validFileSystemResourceTypeAnnotation.value();
            }
        }

        this.extensions = Collections.emptyList();
        
        final String paramExtensions = def.getParam( PARAM_EXTENSIONS );
        
        if( paramExtensions != null )
        {
            this.extensions = new ArrayList<String>();
            
            for( String extension : paramExtensions.split( "," ) )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    this.extensions.add( extension );
                }
            }
        }
        else
        {
            final ValidFileExtensions validFileExtensionsAnnotation = property.getAnnotation( ValidFileExtensions.class );
            
            if( validFileExtensionsAnnotation != null )
            {
                this.extensions = new ArrayList<String>();
                
                for( String extension : validFileExtensionsAnnotation.value() )
                {
                    extension = extension.trim();
                    
                    if( extension.length() > 0 )
                    {
                        this.extensions.add( extension );
                    }
                }
            }
        }

        final String paramLeadingSlash = def.getParam( PARAM_LEADING_SLASH );
        
        if( paramLeadingSlash != null )
        {
            this.includeLeadingSlash = Boolean.parseBoolean( paramLeadingSlash );
        }
        else
        {
            this.includeLeadingSlash = false;
        }
    }
    
    @Override
    protected String browse( final SapphireRenderingContext context )
    {
        final ModelProperty property = getProperty();
        
        final List<IPath> basePaths = getBasePaths();
        final List<IContainer> baseContainers = new ArrayList<IContainer>();
        
        for( IPath path : basePaths )
        {
            final IContainer baseContainer = getWorkspaceContainer( path.toFile() );
            
            if( baseContainer != null )
            {
                baseContainers.add( baseContainer );
            }
            else
            {
                break;
            }
        }
        
        final ITreeContentProvider contentProvider;
        final ILabelProvider labelProvider;
        final ViewerComparator viewerComparator;
        final Object input;
        
        if( basePaths.size() == baseContainers.size() )
        {
            // All paths are in the Eclipse Workspace. Use the available content and label
            // providers.
        
            contentProvider = new WorkspaceContentProvider( baseContainers );
            labelProvider = new WorkbenchLabelProvider();
            viewerComparator = new ResourceComparator();
            input = ResourcesPlugin.getWorkspace().getRoot();
        }
        else
        {
            // At least one of the roots is not in the Eclipse Workspace. Use custom file
            // system content and label providers.
            
            contentProvider = new FileSystemContentProvider( basePaths );
            labelProvider = new FileSystemLabelProvider( context );
            viewerComparator = new FileSystemNodeComparator();
            input = new Object();
        }
    
        final ElementTreeSelectionDialog dialog
            = new ElementTreeSelectionDialog( context.getShell(), labelProvider, contentProvider );
        
        dialog.setTitle( property.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        dialog.setMessage( createBrowseDialogMessage( property.getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
        dialog.setAllowMultiple( false );
        dialog.setHelpAvailable( false );
        dialog.setInput( input );
        dialog.setComparator( viewerComparator );
        
        if( this.type == FileSystemResourceType.FILE )
        {
            dialog.setValidator( new FileSelectionStatusValidator() );
        }
        else if( this.type == FileSystemResourceType.FOLDER )
        {
            dialog.addFilter( new ContainersOnlyViewerFilter() );
        }
        
        if( ! this.extensions.isEmpty() )
        {
            dialog.addFilter( new ExtensionBasedViewerFilter( this.extensions ) );
        }
        
        if( dialog.open() == Window.OK )
        {
            final IPath path;
            final Object firstResult = dialog.getFirstResult();
            
            if( firstResult instanceof IResource )
            {
                path = new Path( ( (IResource) firstResult ).getLocation().toString() );
            }
            else
            {
                path = new Path( ( (FileSystemNode) firstResult ).getFile().getPath() );
            }
            
            for( IPath basePath : basePaths )
            {
                if( basePath.isPrefixOf( path ) )
                {
                    String relpath = path.makeRelativeTo( basePath ).toPortableString();
                    
                    if( this.includeLeadingSlash )
                    {
                        relpath = "/" + relpath;
                    }
                    
                    return relpath;
                }
            }
        }
    
        return null;
    }
    
    protected List<IPath> getBasePaths()
    {
        final BasePathsProvider basePathsProviderAnnotation = getProperty().getAnnotation( BasePathsProvider.class );
        final Class<? extends BasePathsProviderImpl> basePathsProviderClass = basePathsProviderAnnotation.value();
        
        final BasePathsProviderImpl basePathsProvider;
        
        try
        {
            basePathsProvider = basePathsProviderClass.newInstance();
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
        
        return basePathsProvider.getBasePaths( getModelElement() );
    }
    
    private static String getFileExtension( final String fileName ) 
    {
        if( fileName == null ) 
        {
            return null;
        }
        
        int dotIndex = fileName.lastIndexOf( '.' );
        
        if( dotIndex < 0 )
        {
            return null;
        }
        
        return fileName.substring( dotIndex + 1 );
    }
    
    private static IContainer getWorkspaceContainer( final File f )
    {
        final IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
        final IContainer[] wsContainers = wsroot.findContainersForLocationURI( f.toURI() );
        
        if( wsContainers.length > 0 )
        {
            return wsContainers[ 0 ];
        }
        
        return null;
    }
    
    public static final class ContainersOnlyViewerFilter
        
        extends ViewerFilter
        
    {
        public boolean select( final Viewer viewer, 
                               final Object parent, 
                               final Object element ) 
        {
            return ( element instanceof IContainer || 
                     ( element instanceof FileSystemNode && ( (FileSystemNode) element ).getFile().isDirectory() ) ); 
        }
    }

    public static final class ExtensionBasedViewerFilter
    
        extends ViewerFilter
        
    {
        private final List<String> extensions;
        
        public ExtensionBasedViewerFilter( final List<String> extensions )
        {
            this.extensions = extensions;
        }

        public boolean select( final Viewer viewer, 
                               final Object parent, 
                               final Object element ) 
        {
            if( element instanceof IFile || 
                ( element instanceof FileSystemNode && ( (FileSystemNode) element ).getFile().isFile() ) ) 
            {
                final String extension;
                
                if( element instanceof IFile )
                {
                    extension = ( (IFile) element ).getFileExtension();
                }
                else
                {
                    extension = getFileExtension( ( (FileSystemNode) element ).getFile().getName() );
                }
                
                if( extension != null && extension.length() != 0 )
                {
                    for( String ext : this.extensions )
                    {
                        if( extension.equalsIgnoreCase( ext ) )
                        {
                            return true;
                        }
                    }
                }
                
                return false;
            } 
            else if( element instanceof IContainer ) 
            {
                if( element instanceof IProject && ! ( (IProject) element ).isOpen() )
                {
                    return false;
                }
                
                return true;
            }
            
            return true;
        }
    }

    private static final class FileSelectionStatusValidator
    
        implements ISelectionStatusValidator
        
    {
        private static final IStatus ERROR_STATUS 
            = new Status( IStatus.ERROR, SapphireUiFrameworkPlugin.PLUGIN_ID, MiscUtil.EMPTY_STRING );
        
        private static final IStatus OK_STATUS 
            = new Status( IStatus.OK, SapphireUiFrameworkPlugin.PLUGIN_ID, MiscUtil.EMPTY_STRING );
    
        public IStatus validate( final Object[] selection )
        {
            if( selection.length == 1 )
            {
                final Object sel = selection[ 0 ];
                
                if( sel instanceof IFile || 
                    ( sel instanceof FileSystemNode && ( (FileSystemNode) sel ).getFile().isFile() ) )
                {
                    return OK_STATUS;
                }
            }
            
            return ERROR_STATUS;
        }
    }

    private static final class WorkspaceContentProvider
    
        extends WorkbenchContentProvider
 
    {
        private final List<IContainer> roots;
        
        public WorkspaceContentProvider( final List<IContainer> roots )
        {
            this.roots = roots;
        }
        
        @Override
        public Object[] getElements( final Object element )
        {
            final List<IResource> elements = new ArrayList<IResource>();
            
            if( this.roots.size() == 1 )
            {
                final IContainer root = this.roots.get( 0 );
                
                try
                {
                    for( IResource child : root.members() )
                    {
                        if( child.isAccessible() )
                        {
                            elements.add( child );
                        }
                    }
                }
                catch( CoreException e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
            else
            {
                elements.addAll( this.roots );
            }

            return elements.toArray( new IResource[ elements.size() ] );
        }

        @Override
        public Object getParent( final Object element )
        {
            if( ( this.roots.contains( element ) ) || 
                ( this.roots.size() == 1 && this.roots.contains( ( (IResource) element ).getParent() ) ) )
            {
                return null;
            }
            else
            {
                return super.getParent( element );
            }
        }
    }
    
    private static final class FileSystemNode
    {
        private final File file;
        private final FileSystemNode parent;
        private Map<File,FileSystemNode> children; 
        
        public FileSystemNode( final File file,
                               final FileSystemNode parent )
        {
            this.file = file;
            this.parent = parent;
            this.children = Collections.emptyMap();
        }
        
        public File getFile()
        {
            return this.file;
        }
        
        public FileSystemNode getParent()
        {
            return this.parent;
        }
        
        public boolean hasChildren()
        {
            return this.file.isDirectory();
        }
        
        public FileSystemNode[] getChildren()
        {
            if( this.file.isDirectory() )
            {
                final File[] directoryListing = this.file.listFiles();
                
                if( directoryListing != null && directoryListing.length > 0 )
                {
                    final FileSystemNode[] result = new FileSystemNode[ directoryListing.length ];
                    final Map<File,FileSystemNode> newChildrenMap = new HashMap<File,FileSystemNode>();
                    
                    for( int i = 0, n = directoryListing.length; i < n; i++ )
                    {
                        final File f = directoryListing[ i ];
                        
                        FileSystemNode node = this.children.get( f );
                        
                        if( node == null )
                        {
                            node = new FileSystemNode( f, this );
                        }
                        
                        newChildrenMap.put( f, node );
                        result[ i ] = node;
                    }
                    
                    this.children = newChildrenMap;
                    
                    return result;
                }
            }

            return new FileSystemNode[ 0 ];
        }
    }
    
    private static final class FileSystemContentProvider
    
        implements ITreeContentProvider
    
    {
        private final FileSystemNode[] roots;
        
        public FileSystemContentProvider( final List<IPath> roots )
        {
            this.roots = new FileSystemNode[ roots.size() ];
            
            for( int i = 0, n = roots.size(); i < n; i++ )
            {
                this.roots[ i ] = new FileSystemNode( roots.get( i ).toFile(), null );
            }
        }
        
        public Object[] getElements( final Object element )
        {
            return this.roots;
        }
    
        public Object getParent( final Object element )
        {
            return ( (FileSystemNode) element ).getParent();
        }

        public Object[] getChildren( final Object element )
        {
            return ( (FileSystemNode) element ).getChildren();
        }

        public boolean hasChildren( Object element )
        {
            return ( (FileSystemNode) element ).hasChildren();
        }

        public void inputChanged( final Viewer viewer,
                                  final Object oldInput,
                                  final Object newInput )
        {
        }

        public void dispose()
        {
        }
    }
    
    private static final class FileSystemLabelProvider
    
        extends LabelProvider
        
    {
        private final SapphireRenderingContext context;
        
        public FileSystemLabelProvider( final SapphireRenderingContext context )
        {
            this.context = context;
        }
        
        @Override
        public String getText( final Object element )
        {
            return ( (FileSystemNode) element ).getFile().getName();
        }

        @Override
        public Image getImage( final Object element )
        {
            if( ( (FileSystemNode) element ).getFile().isDirectory() )
            {
                return this.context.getImageCache().getImage( SapphireImageCache.OBJECT_FOLDER );
            }
            else
            {
                return this.context.getImageCache().getImage( SapphireImageCache.OBJECT_FILE );
            }
        }
    }
    
    private static final class FileSystemNodeComparator 
        
        extends ViewerComparator 
        
    {
        @SuppressWarnings( "unchecked" )
        
        public int compare( final Viewer viewer, 
                            final Object obj1, 
                            final Object obj2 ) 
        {
            final File f1 = ( (FileSystemNode) obj1 ).getFile();
            final File f2 = ( (FileSystemNode) obj2 ).getFile();
            
            final boolean isFile1Directory = f1.isDirectory();
            final boolean isFile2Directory = f2.isDirectory();
            
            if( isFile1Directory == isFile2Directory )
            {
                return Policy.getComparator().compare( f1.getName(), f2.getName() );
            }
            else if( isFile1Directory )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
    
    public static final class ResourceComparator 
    
        extends ViewerComparator 
        
    {
        @SuppressWarnings( "unchecked" )
        
        public int compare( final Viewer viewer, 
                            final Object obj1, 
                            final Object obj2 ) 
        {
            final IResource r1 = (IResource) obj1;
            final IResource r2 = (IResource) obj2;
            
            final boolean isResource1Container = ( r1 instanceof IContainer );
            final boolean isResource2Container = ( r2 instanceof IContainer );
            
            if( isResource1Container == isResource2Container )
            {
                return Policy.getComparator().compare( r1.getName(), r2.getName() );
            }
            else if( isResource1Container )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( RelativePathBrowseActionHandler.class.getName(), Resources.class );
        }
    }

}
/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [348673] Problem with drop action handler API
 ******************************************************************************/

package org.eclipse.sapphire.samples.map.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.samples.map.IDestination;
import org.eclipse.sapphire.samples.map.IMap;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.SapphireDiagramActionHandler;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.graphiti.DiagramRenderingContext;

/**
 * Action handler for Sapphire.Drop action for the map editor. The implementation reads city names
 * from the dropped text file (one line per city name) and adds these cities to the map. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MapDropActionHandler extends SapphireDiagramActionHandler
{
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
    	DiagramRenderingContext diagramCtx = (DiagramRenderingContext)context;
    	SapphireDiagramEditorPagePart diagramPart = (SapphireDiagramEditorPagePart)diagramCtx.getPart();
    	
    	Object obj = diagramCtx.getObject();
    	
        if( obj instanceof IFile )
        {
            final List<String> cities = new ArrayList<String>();

            InputStream in = null;
            
            try
            {
                in = ( (IFile) obj ).getContents();
                final BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
                
                for( String line = br.readLine(); line != null; line = br.readLine() )
                {
                    if( line != null )
                    {
                        line = line.trim();
                        
                        if( line.length() > 0 )
                        {
                            cities.add( line );
                        }
                    }
                }
            }
            catch( CoreException e )
            {
                LoggingService.log( e );
            }
            catch( IOException e )
            {
                LoggingService.log( e );
            }
            finally
            {
                if( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch( IOException e ) {}
                }
            }
            
            if( ! cities.isEmpty() )
            {
                final IMap map = (IMap) getModelElement();
                final Point currentMousePos = diagramCtx.getCurrentMouseLocation();
                List<DiagramNodePart> cityParts = new ArrayList<DiagramNodePart>();
                int x = currentMousePos.getX();
                int y = currentMousePos.getY();
                for (String cityName : cities)
                {
	                final IDestination city = map.getDestinations().addNewElement();
	                city.setName( cityName );
	                DiagramNodePart cityPart = diagramPart.getDiagramNodePart(city);
	                if (cityPart != null)
	                {
	                	cityPart.setNodePosition(x, y);
	                	cityParts.add(cityPart);
	                	x += 50;
	                	y += 50;
	                }
                }
                return cityParts;
            }
        }
        return null;
    }

    @Override
    public boolean canExecute( final Object obj )
    {
        return true;
    }
    
}

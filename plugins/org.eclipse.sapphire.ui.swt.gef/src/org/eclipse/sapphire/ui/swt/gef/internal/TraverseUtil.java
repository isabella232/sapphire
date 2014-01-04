/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class TraverseUtil 
{
	public static TextPart getNextTextPartInSameNode(TextPart textPart)
	{
    	DiagramNodePart nodePart = textPart.nearest(DiagramNodePart.class);
    	List<TextPart> textParts = nodePart.getContainedTextParts();
    	int index = textParts.indexOf(textPart);
    	for (int i = index + 1; i < textParts.size(); i++)
    	{
    		TextPart nextSibling = textParts.get(i);
    		if (nextSibling.isEditable())
    		{
    			return nextSibling;
    		}                		
    	}
		return null;
	}
	
	public static List<DiagramNodePart> getSortedNodeParts(SapphireDiagramEditorPagePart diagramPagePart)
	{
		List<DiagramNodePart> nodeParts = new ArrayList<DiagramNodePart>();
		nodeParts.addAll(diagramPagePart.getNodes());
		Collections.sort(nodeParts, new Comparator<DiagramNodePart>() 
		{
			public int compare(DiagramNodePart node1, DiagramNodePart node2) 
			{
				Bounds bounds1 = node1.getNodeBounds();
				Bounds bounds2 = node2.getNodeBounds();
	        	if (bounds1.getY() < bounds2.getY())
	        	{
	        		return -1;
	        	}
	        	else if (bounds1.getY() == bounds2.getY())
	        	{
	        		return bounds1.getX() < bounds2.getX() ? -1 : 
	        			(bounds1.getX() == bounds2.getX() ? 0 : 1);
	        	}
	        	else
	        	{
	        		return 1;
	        	}
			}
		});
		
		return nodeParts;
	}
	
	public static TextPart getTextPartInNextNode(List<DiagramNodePart> sortedNodes, DiagramNodePart thisNode)
	{
		int index = sortedNodes.indexOf(thisNode);
		for (int i = index + 1; i < sortedNodes.size(); i++)
		{
			DiagramNodePart nextNode = sortedNodes.get(i);
			List<TextPart> textParts = nextNode.getContainedTextParts();
			for (TextPart textPart : textParts)
			{
				if (textPart.isEditable())
				{
					return textPart;
				}
			}
		}
		return null;
	}
}

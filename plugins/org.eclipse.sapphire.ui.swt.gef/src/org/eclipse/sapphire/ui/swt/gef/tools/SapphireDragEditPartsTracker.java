/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/
package org.eclipse.sapphire.ui.swt.gef.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.util.EditPartUtilities;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.swt.SWT;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireDragEditPartsTracker extends SapphireNodeDragEditPartsTracker {
	
	public SapphireDragEditPartsTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void performSelection() {
		if (hasSelectionOccurred())
			return;
		setFlag(FLAG_SELECTION_PERFORMED, true);
		EditPartViewer viewer = getCurrentViewer();
		List<EditPart> selectedObjects = viewer.getSelectedEditParts();

		if (getCurrentInput().isModKeyDown(SWT.MOD1)) {
			EditPart deselectPart = getDeselectPart(selectedObjects, getSourceEditPart());
			if (deselectPart != null) {
				viewer.deselect(deselectPart);
			} else {
				if (!isParentSelected(getSourceEditPart())) {
					viewer.appendSelection(getSourceEditPart());
					
					removeChildrenDuplicates(getSourceEditPart());
				}
			}
			viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
		} else if (getCurrentInput().isShiftKeyDown()) {
			EditPart fromEditPart = (EditPart)viewer.getProperty(LAST_EDIT_PART);
			if (sameNodeModel(fromEditPart, getSourceEditPart())) {
				viewer.deselectAll();
				
				EditPart toEditPart = getSourceEditPart();
				Rectangle fromRect = getRectangle(fromEditPart);
				Rectangle toRect = getRectangle(toEditPart);
				Rectangle rect = fromRect.getUnion(toRect);
				
				Collection editPartsToProcess = new HashSet();
				editPartsToProcess.addAll(EditPartUtilities.getAllChildren(getNodeEditPart(fromEditPart)));
				List<EditPart> selectedEditParts = new ArrayList<EditPart>();
				for (Iterator iterator = editPartsToProcess.iterator(); iterator.hasNext();) {
					GraphicalEditPart editPart = (GraphicalEditPart) iterator.next();
					if (editPart.isSelectable()	&& FigureUtilities.isNotFullyClipped(editPart.getFigure()) && isEditPartInRect(editPart, rect)) {
						selectedEditParts.add(editPart);
					}
				}
				filterEditParts(selectedEditParts, fromEditPart, getSourceEditPart());
				for (EditPart editPart : selectedEditParts) {
					viewer.appendSelection(editPart);
				}
			} else {
				viewer.appendSelection(getSourceEditPart());

				viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
			}
		}
		else {
			viewer.select(getSourceEditPart());

			viewer.setProperty(LAST_EDIT_PART, getSourceEditPart());
		}
	}
	
	@SuppressWarnings("rawtypes")
	private boolean isParentSelected(EditPart sourceEditPart) {
		final EditPartViewer viewer = getCurrentViewer();
		List selectedParts = viewer.getSelectedEditParts();
		EditPart parent = sourceEditPart.getParent();
		while (parent != null) {
			if (selectedParts.contains(parent)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	private void filterEditParts(List<EditPart> list, EditPart part1, EditPart part2) {
		removeParent(list, part1);
		removeParent(list, part2);
		
		List<EditPart> toRemove = new ArrayList<EditPart>(list.size());
		for (EditPart part : list) {
			if (hasParentInList(list, part)) {
				toRemove.add(part);
			}
		}
		list.removeAll(toRemove);
	}
	
	private void removeParent(List<EditPart> list, EditPart part) {
		EditPart parent = part.getParent();
		while (parent != null) {
			list.remove(parent);
			parent = parent.getParent();
		}
	}
	
	private boolean hasParentInList(List<EditPart> list, EditPart part) {
		EditPart parent = part.getParent();
		while (parent != null) {
			if (list.contains(parent)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}
	
	private boolean isEditPartInRect(EditPart editPart, Rectangle selectionRect) {
		boolean included = false;
		if (!(editPart instanceof ConnectionEditPart)) {
			Rectangle rect = getRectangle(editPart);
			included = selectionRect.intersects(rect);
		}
		return included;
	}
	
	private Rectangle getRectangle(EditPart editPart) {
		if (editPart instanceof GraphicalEditPart) {
			IFigure figure = ((GraphicalEditPart)editPart).getFigure();
			Rectangle r = figure.getBounds().getCopy();
			figure.translateToAbsolute(r);
			return r;
		}
		return null;
	}
	
	private boolean sameNodeModel(EditPart part1, EditPart part2) {
		if (part1 != null && part2 != null) {
			ShapeModel model1 = (ShapeModel)part1.getModel();
			ShapeModel model2 = (ShapeModel)part2.getModel();
			return model1.getNodeModel().equals(model2.getNodeModel());
		}
		return false;
	}
	
	private GraphicalEditPart getNodeEditPart(EditPart part) {
		DiagramNodeModel nodeModel = ((ShapeModel)part.getModel()).getNodeModel();
		EditPart parentEditPart = part;
		while (parentEditPart != null) {
			if (nodeModel.equals(parentEditPart.getModel())) {
				return (GraphicalEditPart)parentEditPart;
			}
			parentEditPart = parentEditPart.getParent();
		}
		return (GraphicalEditPart) getCurrentViewer().getRootEditPart();
	}
}

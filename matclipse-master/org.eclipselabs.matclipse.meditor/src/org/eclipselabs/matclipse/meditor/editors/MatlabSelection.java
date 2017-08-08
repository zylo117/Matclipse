/*******************************************************************************
 * Copyright (c) 2006, 2011 Institute of Theoretical and Computational Physics (ITPCP), 
 * Graz University of Technology.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         initial API and implementation
 * Last changed: 
 *     2008-01-23
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.actions.MatlabAction;


public class MatlabSelection {
    /** The document this is a part of */
    public IDocument doc;

    /** The line number of the first line */
    public int startLineIndex;

    /** The line number of the last line */
    public int endLineIndex;

    /** Length of selected text */
    public int selLength;

    /** The selected text */
    public String selection;

    /** End line delimiter */
    public String endLineDelim;

    /** Start line region */
    public IRegion startLine;

    /** End line region */
    public IRegion endLine;

    /** Original cursor line */
    public int cursorLine;

    /** Text editor */
    public ITextEditor textEditor;

    /** Cursor offset. */
    public int absoluteCursorOffset;

    public ITextSelection textSelection;

    
    /**
     * Default constructor for PySelection. Simply defaults all the values.
     */
    public MatlabSelection() {
        // Initialize values
        setDefaults();
    }

    
    /**
     * Alt constructor for PySelection. Takes in a text editor from Eclipse, and a boolean that is used to indicate how to handle an empty
     * selection.
     * 
     * @param textEditor The text editor operating in Eclipse
     * @param onEmptySelectAll If true, this indicates that on an empty selection, the whole document should be selected
     */
    public MatlabSelection(ITextEditor textEditor, boolean onEmptySelectAll) {
        // Initialize values
        setDefaults();

        try {
            // Grab the document
            this.doc = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            this.textEditor = textEditor;
            // Grab the selection
            ITextSelection selection = getITextSelection();
            this.textSelection = selection;

            this.absoluteCursorOffset = selection.getOffset();
            // Set data
            this.startLine = doc.getLineInformation(selection.getStartLine());
            this.endLine = doc.getLineInformation(selection.getEndLine());

            this.startLineIndex = selection.getStartLine();
            this.endLineIndex = selection.getEndLine();
            this.selLength = selection.getLength();

            this.cursorLine = selection.getEndLine();

            // Store selection information
            select(onEmptySelectAll);
        } catch (Exception e) {
            Activator.beep(e);
        }
    }

    
    public ITextSelection getITextSelection() {
        return (ITextSelection) textEditor.getSelectionProvider().getSelection();
    }

    
    /**
     * Alt constructor for PySelection. Takes in a document, starting line, ending line, and length of selection, as well as a boolean
     * indicating how to handle an empty selection.
     * 
     * @param doc Document to be affected
     * @param startLineIndex Line number for first line
     * @param endLineIndex Line number for last line
     * @param selLength Length of selected text
     * @param onEmptySelectAll If true, this indicates that on an empty selection, the whole document should be selected
     */
    public MatlabSelection(IDocument doc, int startLineIndex, int endLineIndex, int selLength, boolean onEmptySelectAll) {
        // Initialize values
        setDefaults();

        // Set data
        this.doc = doc;
        this.startLineIndex = startLineIndex;
        this.endLineIndex = endLineIndex;
        this.selLength = selLength;

        this.cursorLine = endLineIndex;

        // Store selection information
        select(onEmptySelectAll);
    }

    
    /**
     * Defaults all the values.
     */
    private void setDefaults() {
        doc = null;
        startLineIndex = 0;
        endLineIndex = 0;
        selLength = 0;
        selection = "";
        endLineDelim = "";
        startLine = null;
        endLine = null;
        cursorLine = 0;
    }

    
    /**
     * Make the full selection from the information in the class' data.
     * 
     * @param onEmptySelectAll If true, this indicates that on an empty selection, the whole document should be selected
     */
    private void select(boolean onEmptySelectAll) {
        try {
            // Get some line information
            int initialPos = 0;

            //special cases...first char of the editor, last char...
            if (endLineIndex < startLineIndex) {
                endLineIndex = startLineIndex;
            }

            // If anything is actually selected, we'll be modifying the selection only
            if (selLength > 0) {
                startLine = doc.getLineInformation(startLineIndex);
                endLine = doc.getLineInformation(endLineIndex);

                // Get offsets and lengths
                initialPos = startLine.getOffset();
                endLineDelim = MatlabAction.getDelimiter(doc, startLineIndex);

                // Grab the selected text into our string
                selection = doc.get(getITextSelection().getOffset(), getITextSelection().getLength());
            }
            // Otherwise we'll modify the whole document, if asked to
            else if (onEmptySelectAll) {
                startLineIndex = 0;
                endLineIndex = doc.getNumberOfLines() - 1;

                startLine = doc.getLineInformation(startLineIndex);
                endLine = doc.getLineInformation(endLineIndex);

                endLineDelim = MatlabAction.getDelimiter(doc, 0);

                // Grab the whole document
                selection = doc.get();
                selLength = selection.length();
            }
            // Grab the current line only
            else {
                startLine = doc.getLineInformation(startLineIndex);
                endLine = doc.getLineInformation(endLineIndex);

                selLength = startLine.getLength();

                // Get offsets and lengths
                initialPos = startLine.getOffset();
                endLineDelim = MatlabAction.getDelimiter(doc, startLineIndex);

                // Grab the selected text into our string
                selection = doc.get(initialPos, selLength);
            }
        } catch (Exception e) {
            Activator.beep(e);
        }
    }

    
    /**
     * In event of partial selection, used to select the full lines involved.
     */
    public void selectCompleteLines() {
        selLength = (endLine.getOffset() + endLine.getLength()) - startLine.getOffset();
    }

    
    /**
     * Gets line from document.
     * 
     * @param i Line number
     * @return String line in String form
     */
    public String getLine(int i) {
        return getLine(doc, i);
    }
    
    
    /**
     * Gets line from document.
     * 
     * @param i Line number
     * @return String line in String form
     */
    public static String getLine(IDocument doc, int i) {
        try {
            return doc.get(doc.getLineInformation(i).getOffset(), 
                    doc.getLineInformation(i).getLength());
        } catch (Exception e) {
            return "";
        }
    }

    
    /**
     * Gets cursor offset within a line.
     * 
     * @return int Offset to put cursor at
     */
    public int getCursorOffset() {
        try {
            return doc.getLineInformation(cursorLine).getOffset();
        } catch (Exception e) {
            return 0;
        }
    }

    
    /**
     * Deletes a line from the document
     * @param i
     */
    public void deleteLine(int i) {
        deleteLine(doc, i);
    }

    
    /**
     * Deletes a line from the document
     * @param i
     */
    public static void deleteLine(IDocument doc, int i) {
        try {
            int offset = doc.getLineInformation(i).getOffset();
            
            int length = -1;
            
            if(doc.getNumberOfLines() > i){
                int nextLineOffset = doc.getLineInformation(i+1).getOffset();
                length = nextLineOffset - offset;
            }else{
                length = doc.getLineInformation(i).getLength();
            }
            
            if(length > -1){
                doc.replace(offset, length, "");
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        } 
    }
    
    
    public void addLine(String contents, int afterLine){
        addLine(doc, endLineDelim, contents, afterLine);
    }
    
    
    public static void addLine(IDocument doc, String endLineDelim, 
            String contents, int afterLine){
        try {
            
            int offset = -1;
            if (doc.getNumberOfLines() > afterLine) {
                offset = doc.getLineInformation(afterLine+1).getOffset();
            } else {
                offset = doc.getLineInformation(afterLine).getOffset();
            }
            
            
            if (!contents.endsWith(endLineDelim)){
                contents += endLineDelim;
            }
            
            if(offset >= 0){
                doc.replace(offset, 0, contents);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        } 
    }
    
}

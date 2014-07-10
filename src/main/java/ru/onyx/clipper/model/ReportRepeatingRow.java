package ru.onyx.clipper.model;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.onyx.clipper.data.PropertyGetter;
import ru.onyx.clipper.utils.TableUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;

/**
 * User: Alex
 * Date: 15.03.12
 * Time: 18:12
 */
public class ReportRepeatingRow extends BaseReportObject {
    PdfPTable header;
    PdfPTable footer;
    PdfPTable finalLine;

    Map aggrMap;
    public ReportRepeatingRow(Node tableNode,HashMap<String ,ReportBaseFont> fonts,BaseReportObject pParent,PropertyGetter pGetter, Document _doc) throws ParseException, DocumentException, IOException {
      _fonts = fonts;
      parent = pParent;
      propertyGetter = pGetter;
      String nodeName;
      Load(tableNode);

      NodeList childsList = tableNode.getChildNodes();

        for(int h=0;h<childsList.getLength();h++) {
            nodeName = childsList.item(h).getNodeName();
            Node node = childsList.item(h);

            if(nodeName.equalsIgnoreCase("header")) {
                NodeList headerChildList = node.getChildNodes();
                for(int j=0;j<headerChildList.getLength();j++) {
                    nodeName = headerChildList.item(j).getNodeName();
                    if(nodeName.equalsIgnoreCase("table"))
                        try {
                            header = (new ReportTable(headerChildList.item(j), _fonts, this, propertyGetter)).getPdfObject();
                            TableUtils.setExactWidthFromPercentage(header, _doc);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            if(nodeName.equalsIgnoreCase("footer")) {
                NodeList footerChildList = node.getChildNodes();
                for(int j=0;j<footerChildList.getLength();j++) {
                    nodeName = footerChildList.item(j).getNodeName();
                    if(nodeName.equalsIgnoreCase("table"))
                        try {
                            ReportTable footerTable = new ReportTable(footerChildList.item(j), _fonts, this, propertyGetter);
                            footer = footerTable.getPdfObject();
                            TableUtils.setExactWidthFromPercentage(footer, _doc);
                            aggrMap = footerTable.getTableAggrProps();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            if(nodeName.equalsIgnoreCase("final")) {
                NodeList finalChildList = node.getChildNodes();
                for(int j=0;j<finalChildList.getLength();j++) {
                    nodeName = finalChildList.item(j).getNodeName();
                    if(nodeName.equalsIgnoreCase("table"))
                        try {
                            ReportTable finalTable = new ReportTable(finalChildList.item(j), _fonts, this, propertyGetter);
                            finalLine = finalTable.getPdfObject();
                            TableUtils.setExactWidthFromPercentage(finalLine, _doc);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }

         if(getPageName().length() > 0) {
         int n = pGetter.GetPageCount(getPageName());
         for(int y=0;y<n;y++) {
             for(int h=0;h<childsList.getLength();h++) {
                 nodeName = childsList.item(h).getNodeName();
                 Node node = childsList.item(h);

                 if(nodeName.equalsIgnoreCase("items")) {
                     NodeList cells = node.getChildNodes();
                     for(int i=0;i<cells.getLength();i++) {
                         nodeName=cells.item(i).getNodeName();
                         if (nodeName.equalsIgnoreCase("cell")) {
                             NamedNodeMap attrObj = cells.item(i).getAttributes();
                             String propName = parseAttribute(attrObj, "property", "");
                             String textCell = pGetter.GetProperty(String.format("%s[%s].%s", getPageName(), y, propName));
                             SetAttribute(attrObj, "customtext", textCell);

                             items.add(new ReportCell(cells.item(i), _fonts, this, pGetter));
                         }
                     }
                 }
             }
           }
         }
     }

    public List<Object> getPdfTable(float spaceLeft, Document _doc) throws DocumentException, ParseException, IOException {
        List<Object> repeatingRowObjects = new ArrayList<>();

        int lastUsedRow = 0;
        int totalCols = getColumns();
        int totalCells = items.size();
        int totalRows = totalCells/totalCols;
        int minFreeSpaceAfter = getMinFreeSpaceAfter();
        float curTableHeight = 0f;
        float cellHeight=0f;
        float headerHeight;
        float heightLeft;
        if( header!=null) {
            headerHeight = header.calculateHeights();
        } else headerHeight = 0.0f;
        float footerHeight;
        if(footer!=null) {
            footerHeight = footer.calculateHeights();
        } else footerHeight = 0.0f;
        float otherPageTblHeight = getRepRowOtherPageHeight();
        boolean firstTblAdded=false; // flag to determine that first page is done
        boolean onePaged=false;
        boolean docComplete=false;

        PdfPTable table = new PdfPTable(getColumns());
        setTableParams(table, _doc);
        TableUtils.setExactWidthFromPercentage(table, _doc);
        String[] cellsFormat = new String[totalCols];

        for(int colNum=0; colNum<totalCols; colNum++){
            if(items.get(colNum).getStringformat() == null){
                cellsFormat[colNum] = "";
            } else cellsFormat[colNum] = items.get(colNum).getStringformat();
        }

        for (int i=0; i<totalRows; i++) {
            //row iteration
            cellHeight = 0;
            for (int j = 0; j < totalCols; j++) {               //column iteration
                if (items.get(i * totalCols + j).getNumerator())
                    ((ReportCell) items.get(i * totalCols + j)).setText(String.valueOf(i + 1));
                PdfPCell obj = ((ReportCell) items.get(i * totalCols + j)).getPdfObject();
                table.addCell(obj);
            }


            TableUtils.setExactWidthFromPercentage(table, _doc);
            curTableHeight = table.getTotalHeight();

            cellHeight = table.getRow(table.getRows().size() - 1).getMaxRowHeightsWithoutCalculating();
            heightLeft = (totalRows-i)*cellHeight;
            if(getReplicateHeader().equals(Boolean.FALSE)) {
                headerHeight=0f;
            }
            if(getReplicateFooter().equals(Boolean.FALSE)) {
                footerHeight=0f;
            }

            if ((curTableHeight + headerHeight + footerHeight + cellHeight + table.getRows().size() * getBorderWidth() > spaceLeft
                    || spaceLeft - curTableHeight - headerHeight - footerHeight - cellHeight - table.getRows().size() * getBorderWidth() < minFreeSpaceAfter) && !firstTblAdded ) {
                // if one of the limits has been found while trying to add a first page
                firstTblAdded = true;
                if(i + 2 == totalRows) { //this means that cur. row is penultimate and the next one needs to be moved to a next page. The document is one-paged and is larger than limits.

                    drawTable(table, repeatingRowObjects, cellsFormat);

                    repeatingRowObjects.add(null);
                    table = new PdfPTable(getColumns()); //create new table
                    setTableParams(table, _doc);
                    curTableHeight=0;

                } else if (i + 1 == totalRows) { // else it fits exactly on one page
                    onePaged=true;
                    drawTable(table, repeatingRowObjects, cellsFormat);

                } else { //else it needs to be continued on a next page
                    if((totalRows-i)*cellHeight<minFreeSpaceAfter) {
                        drawTable(table, repeatingRowObjects, cellsFormat);
                        repeatingRowObjects.add(null);
                        table = new PdfPTable(getColumns()); //create new table
                        setTableParams(table, _doc);
                        curTableHeight = 0;
                    }
                }
            } else if (i + 1 == totalRows && !firstTblAdded) { //this means that cur. row is penultimate and the next one needs to be moved to a next page. The document is one-paged and is smaller than limits
                firstTblAdded = true;
                onePaged = true;
                drawTable(table, repeatingRowObjects, cellsFormat);

            } else if (curTableHeight + headerHeight + footerHeight + cellHeight + table.getRows().size() * getBorderWidth() > spaceLeft && firstTblAdded) {
                // if one of the limits has been found while trying to add NOT a first page
                if(i + 2 == totalRows) { //this means that cur. row is penultimate and the next one needs to be moved to a next page.
                    drawTable(table, repeatingRowObjects, cellsFormat);

                    repeatingRowObjects.add(null);

                    table = new PdfPTable(getColumns()); //create new table
                    setTableParams(table, _doc);
                    curTableHeight = 0;
                } else { //else it needs to be continued on a next page
                    drawTable(table, repeatingRowObjects, cellsFormat);
                    repeatingRowObjects.add(null);
                    table = new PdfPTable(getColumns()); //create new table
                    setTableParams(table, _doc);
                    curTableHeight = 0;
                    spaceLeft=otherPageTblHeight;
                }
            } else if(firstTblAdded && heightLeft>_doc.getPageSize().getHeight() -_doc.bottomMargin() - _doc.topMargin() - curTableHeight - table.getRows().size()*getBorderWidth() - headerHeight - footerHeight - minFreeSpaceAfter) {

                if(i+2 == totalRows &&
                        _doc.getPageSize().getHeight() -_doc.bottomMargin() - _doc.topMargin() - curTableHeight - table.getRows().size()*getBorderWidth() - headerHeight - footerHeight < minFreeSpaceAfter) {
                    drawTable(table, repeatingRowObjects, cellsFormat);
                    repeatingRowObjects.add(null);
                    table = new PdfPTable(getColumns()); //create new table
                    setTableParams(table, _doc);

                    curTableHeight = 0;
                }else if(i + 1 == totalRows) { //this means the time to add finalizer
                    drawTable(table, repeatingRowObjects, cellsFormat);
                    if(finalLine!=null) repeatingRowObjects.add(finalLine);
                }
            } else if(i + 1 == totalRows) { //this means the time to add finalizer
                drawTable(table, repeatingRowObjects, cellsFormat);
                if(finalLine!=null) repeatingRowObjects.add(finalLine);
            }



            /*if((curTableHeight+headerHeight+footerHeight+cellHeight+table.getRows().size()*getBorderWidth() > firstPageTblHeight && firstPageTblHeight > 0 && otherPageTblHeight > 0 || i+1==totalRows) && !firstTblAdded) {
                //if current table fits to a first page, then
                table.setHeaderRows(0);
                table.setComplete(true);
                lastUsedRow=i; //remember that row
                if(header!=null) repeatingRowObjects.add(header); //add header before table if there is one
                repeatingRowObjects.add(table); //add the table to the list

                if(footer!=null) {
                    PdfPTable tempFooter = makeAggrRow(table,cellsFormat);
                        if(getReplicateFooter().equals(Boolean.TRUE)) {
                        repeatingRowObjects.add(tempFooter);
                    }
                }

                firstTblAdded=true; //say that first page is done

                if(i+1==totalRows) {
                    docComplete=true; // This means that document fits on one page and there is no need to do any more actions
                    if(finalLine!=null) repeatingRowObjects.add(finalLine);
                    if(spaceLeft - curTableHeight - headerHeight < minFreeSpaceAfter){
                        repeatingRowObjects.add(null);
                    }
                } else {
                    repeatingRowObjects.add(null);
                    table = new PdfPTable(getColumns()); //create new table and format it
                    setTableParams(table, _doc);
                    curTableHeight=0;

                    if(getReplicateHeader().equals(Boolean.FALSE)) {
                        headerHeight=0f;
                    }
                    if(getReplicateFooter().equals(Boolean.FALSE)) {
                        footerHeight=0f;
                    }
                }
            }

            if(( (curTableHeight+headerHeight+footerHeight+cellHeight+table.getRows().size()*getBorderWidth() > otherPageTblHeight && firstPageTblHeight > 0 && otherPageTblHeight > 0) || i+1==totalRows) && firstTblAdded && !docComplete) {
            //if this is not the first page and table has enough size or ends
                table.setHeaderRows(0);
                table.setComplete(true);
                lastUsedRow=i; //remember the row

                if(header!=null && getReplicateHeader().equals(Boolean.TRUE)) {
                    repeatingRowObjects.add(header); //add header before table on the other pages if flag is done
                }
                repeatingRowObjects.add(table); //add the table to the list
                if(footer!=null) {
                    PdfPTable tempFooter = makeAggrRow(table,cellsFormat);
                    if (getReplicateFooter().equals(Boolean.TRUE)) {
                        repeatingRowObjects.add(tempFooter);
                    }
                }
                if(i+1!=totalRows) repeatingRowObjects.add(null);
                table = new PdfPTable(getColumns()); //create new table
                setTableParams(table, _doc);
            }

            if(i+1==totalRows) {
                docComplete = true;
                if(finalLine!=null) repeatingRowObjects.add(finalLine);
            }

            if(i+2==totalRows && (_doc.getPageSize().getHeight() -_doc.bottomMargin() - _doc.topMargin() - curTableHeight - table.getRows().size()*getBorderWidth() - headerHeight - footerHeight < minFreeSpaceAfter)) {
                table.setHeaderRows(0);
                table.setComplete(true);
                if(header!=null && getReplicateHeader().equals(Boolean.TRUE)) {
                    repeatingRowObjects.add(header); //add header before table on the other pages if flag is done
                }
                repeatingRowObjects.add(table); //add the table to the list
                if(footer!=null) {
                    PdfPTable tempFooter = makeAggrRow(table, cellsFormat);
                    if (getReplicateFooter().equals(Boolean.TRUE)) {
                        repeatingRowObjects.add(tempFooter);
                    }
                }
                repeatingRowObjects.add(null);
                table = new PdfPTable(getColumns()); //create new table
                setTableParams(table, _doc);
            }
        }*/
        }
        return repeatingRowObjects;
    }

    private void drawTable(PdfPTable table, List<Object> repeatingRowObjects, String[] cellsFormat) {
        table.setHeaderRows(0);
        table.setComplete(true);
        if(header!=null) repeatingRowObjects.add(header); //add header before table if there is one
        repeatingRowObjects.add(table); //add the table to the list
        if(footer!=null) {
            PdfPTable tempFooter = makeAggrRow(table,cellsFormat);
            if(getReplicateFooter().equals(Boolean.TRUE)) {
                repeatingRowObjects.add(tempFooter);
            }
        }
    }

    protected void setTableParams(PdfPTable table, Document _doc){
        if(getTotalWidth() >0) table.setTotalWidth(getTotalWidth());
        if(getWidthPercentage() >0) {
            table.setWidthPercentage(getWidthPercentage());
            TableUtils.setExactWidthFromPercentage(table, _doc);
        }
        if(getWidthCellsPercentage() != null) try {
            table.setWidths(getWidthCellsPercentage());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        table.setHorizontalAlignment(getHorizontalAlignment());
        table.setTotalWidth(PageSize.A4.getWidth() * table.getWidthPercentage() / 100);

        if(getSpacingAfter() >= 0) table.setSpacingAfter(getSpacingAfter());
        if(getSpacingBefore() >=0) table.setSpacingBefore(getSpacingBefore());
    }


    private PdfPTable makeAggrRow(PdfPTable table, String[] cellsFormat) {

        PdfPTable tempFooter = new PdfPTable(footer);
        PdfPCell[] tempCells = tempFooter.getRow(0).getCells(); //save footer cells to get access later
        PdfPCell[] tempTableCells = table.getRow(0).getCells();
        tempFooter.flushContent();
        float[] aggrRes = new float[50];
        String[] aggrType = new String[50];
        final int aggrRowsCount = table.getRows().size();
        final Iterator<Integer> cursor = aggrMap.keySet().iterator();
        int aggrResIndex=0;
        while (cursor.hasNext()) {
            final Integer key = cursor.next();
            if(aggrMap.get(key).toString().toLowerCase().equals("sum")) {
                for (int j = 0; j < aggrRowsCount; j++) {
                    PdfPCell[] cells = table.getRow(j).getCells();
                    if(cells[key-1].getPhrase().getContent().toString().length()==0) {
                        aggrType[aggrResIndex] = "int";
                        continue;
                    }
                    else if(cells[key-1].getPhrase().getContent().toString().contains("."))
                        aggrType[aggrResIndex]="float";
                    else {
                        aggrType[aggrResIndex] = "int";
                    }
                    aggrRes[aggrResIndex] += Float.parseFloat(cells[key - 1].getPhrase().getContent().toString());
                }
            }
            aggrResIndex++;
        }

        final Iterator<Integer> cursor2 = aggrMap.keySet().iterator();
        aggrResIndex=0;
        int k=0;
        while (cursor2.hasNext()) { //while aggr columns are needed to be added to footer
            final Integer key = cursor2.next(); //get next index of aggr column
            for (; k < footer.getNumberOfColumns(); k++) {
                if (tempCells[k] != null) { //if columns are not blank (colspan is not used)
                    if (k == key - 1) { //if current column is aggr column
                        if(aggrType[aggrResIndex].equals("float")) {
                            tempCells[k].setPhrase(new Phrase(String.valueOf(String.format(cellsFormat[k],aggrRes[aggrResIndex])), new Font(tempTableCells[k].getPhrase().getFont()))); //write aggr value with table's font
                        }
                        if(aggrType[aggrResIndex].equals("int")) {
                            tempCells[k].setPhrase(new Phrase(String.valueOf((int) aggrRes[aggrResIndex]), new Font(tempTableCells[k].getPhrase().getFont()))); //write aggr value with table's font
                        }
                        tempCells[k].setHorizontalAlignment(tempTableCells[k].getHorizontalAlignment());

                        break;
                    }
                }
            }
            aggrResIndex++;
        }

        for(k=0;k<footer.getNumberOfColumns(); k++) { //make a temp footer using previously saved cells with new info
            if(tempCells[k]!=null) tempFooter.addCell(tempCells[k]);
        }

        return tempFooter;
    }

    @Override
    public PdfPTable getPdfObject() throws DocumentException, ParseException, IOException {
        throw new NotImplementedException();
    }
}

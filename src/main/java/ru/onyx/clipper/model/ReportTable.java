package ru.onyx.clipper.model;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import org.w3c.dom.Node;
import ru.onyx.clipper.data.PropertyGetter;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;


/**
 * User: Alex
 * Date: 14.03.12
 * Time: 19:15
 */
public class ReportTable extends BaseReportObject {

    //private ArrayList<ReportCell> items = new ArrayList<ReportCell>();

    class DashedTable implements PdfPTableEvent {
        @Override
        public void tableLayout(PdfPTable table, float[][] widths,
                                float[] heights, int headerRows, int rowStart,
                                PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.saveState();
            canvas.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);
            canvas.setLineDash(new float[] {0.125f, 3.0f}, 5.0f);
            float llx = widths[0][0];
            float urx = widths[0][widths[0].length -1];
            for (int i = 0; i < heights.length; i++) {
                canvas.moveTo(llx, heights[i]);
                canvas.lineTo(urx, heights[i]);
            }
            for (int i = 0; i < widths.length; i++) {
                for (int j = 0; j < widths[i].length; j++) {
                    canvas.moveTo(widths[i][j], heights[i]);
                    canvas.lineTo(widths[i][j], heights[i+1]);
                }
            }
            canvas.stroke();
            canvas.restoreState();
        }
    }

    public ReportTable(Node node, HashMap<String, ReportBaseFont> fonts, BaseReportObject pParent, PropertyGetter pGetter) throws ParseException {
        _fonts = fonts;
        parent = pParent;
        propertyGetter = pGetter;
        Load(node);
        LoadItems(node, fonts, this, pGetter);
    }

    public PdfPTable getPdfObject() throws DocumentException, ParseException, IOException {

        PdfPTable table = new PdfPTable(this.columns);
        if(getTotalWidth() >0) table.setTotalWidth(getTotalWidth());
        if(getWidthPercentage() >0) table.setWidthPercentage(getWidthPercentage());

         if(getSpacingAfter() != -1) table.setSpacingAfter(getSpacingAfter());
         if(getSpacingBefore() != -1) table.setSpacingBefore(getSpacingBefore());

        table.setKeepTogether(getKeepTogether());
        table.setHorizontalAlignment(getHorizontalAlignment());

        if(getBorderStyle() != null && getBorderStyle().equals("dotted")) {
            table.setTableEvent(new DashedTable());
            table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        }

        if(getWidthCellsPercentage() != null) {
        if(getWidthCellsPercentage().length > 0) {
            float[] cellsPercs = new float[getWidthCellsPercentage().length];
            for(int y=0;y<getWidthCellsPercentage().length;y++) {
                cellsPercs[y] = getWidthCellsPercentage()[y];
            }
            table.setWidths(cellsPercs);
        }
        }


        for (BaseReportObject item : items) {
            /*if(item instanceof ReportIfCondition){

                for(BaseReportObject ifCondItems : item){

                }
            }*/

                PdfPCell obj = ((ReportCell) item).getPdfObject();
                table.addCell(obj);

        }
        table.setComplete(true);

        return table;
    }
}

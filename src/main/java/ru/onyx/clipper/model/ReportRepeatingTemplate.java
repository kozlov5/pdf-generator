package ru.onyx.clipper.model;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.onyx.clipper.data.PropertyGetter;
import ru.onyx.clipper.utils.ReportRegexUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by novikam on 31.07.14.
 */
public class ReportRepeatingTemplate extends BaseReportObject{


    public ReportRepeatingTemplate(Node tableNode,HashMap<String ,ReportBaseFont> fonts,BaseReportObject pParent,PropertyGetter pGetter, Report rep) throws ParseException, DocumentException, IOException{
        _fonts = fonts;
        propertyGetter = pGetter;
        String nodeName;
        Load(tableNode);
        NodeList childsList = tableNode.getChildNodes();
        int x = rep.getCurPage();
        setPageNumber(x);
        String originalPageNameRT = getPageNameRT();


        if(getPageNameRT().length()>0){
            if(pGetter.GetPageCount(getPageNameRT())!=0) {
                int n = pGetter.GetPageCount(getPageNameRT());
                for(int y=0;y<n;y++) {
					if(n > 1) {
						setPageNameRT(originalPageNameRT + "[" + y + "].");
					}else{
						setPageNameRT(originalPageNameRT + ".");
					}
                    for (int h = 0; h < childsList.getLength(); h++) {
                        nodeName = childsList.item(h).getNodeName();
                        Node node = childsList.item(h);
                        if(nodeName.equalsIgnoreCase("table")){
//                            items.add(new ReportTable(node,_fonts,this,pGetter));
                            itemsGPO.add(new ReportTable(node,_fonts,this,pGetter,null).getPdfObject());
                        }else if(nodeName.equalsIgnoreCase("paragraph")){
//                            items.add(new ReportParagraph(node,_fonts,this,pGetter));
                            itemsGPO.add(new ReportParagraph(node,_fonts,this,pGetter).getPdfObject());
                        }else if(nodeName.equalsIgnoreCase("repeatingrow")){
//                            items.add(new ReportRepeatingRow(node,_fonts,this,pGetter));
                            itemsGPO.add(new ReportRepeatingRow(node,_fonts,this,pGetter).getPdfObject());
                        }else if(nodeName.equalsIgnoreCase("newpage")){
                            x++;
                            setPageNumber(x);
                        }else if(nodeName.equalsIgnoreCase("ifcondition")){
                            NodeList ifStatementChildren = node.getChildNodes();
                            ReportConditionalStatements.parseIfStatement(ifStatementChildren, pGetter, "condition", "else", "paragraph", items, _fonts);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Element getPdfObject() throws DocumentException, ParseException, IOException {
        return null;
    }
}

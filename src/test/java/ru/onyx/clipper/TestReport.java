package ru.onyx.clipper;

import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import ru.onyx.clipper.data.PropertyGetter;
import ru.onyx.clipper.data.PropertyGetterFromJSONFileImpl;
import ru.onyx.clipper.data.PropertyGetterFromJSONStringImpl;
import ru.onyx.clipper.utils.ConversionUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Created by anton on 03.04.14.
 */
public class TestReport {

    private static String getFileContent(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        byte[] fileContent = new byte[(int) file.length()];

        fis.read(fileContent);
        fis.close();

        return new String(fileContent);
    }

    private byte[] getFileFontByteContent(String name) throws IOException {

        File file = new ClassPathResource("fonts/" + name + ".ttf").getFile();
        FileInputStream inStream = new FileInputStream(file);
        byte fileContent[] = new byte[(int) file.length()];
        inStream.read(fileContent);
        return fileContent;
    }

    @Test
    public void test1() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException, DocumentException {
        //String markup = getFileContent(new ClassPathResource("reports/report1/payment order.xml").getFile().getAbsolutePath());
        String markup = getFileContent(new ClassPathResource("reports/report1/financial statement.xml").getFile().getAbsolutePath());


        HashMap<String, byte[]> fontBodies = new HashMap<String, byte[]>();
        fontBodies.put("arial", getFileFontByteContent("arial"));
        fontBodies.put("times", getFileFontByteContent("times"));
        fontBodies.put("arialbi", getFileFontByteContent("arialbi"));
        fontBodies.put("ariali", getFileFontByteContent("ariali"));
        fontBodies.put("arialbd", getFileFontByteContent("arialbd"));
        fontBodies.put("wingding", getFileFontByteContent("wingding"));

        Path path = Paths.get(new ClassPathResource("reports/report1/financialstatementdata.xml").getFile().getAbsolutePath());
        String jsonString = ConversionUtils.ByteXMLtoJSON(Files.readAllBytes(path));

        //ConversionUtils.XMLtoJSON(new ClassPathResource("reports/report1/financialstatementdata.xml").getFile().getAbsolutePath(), new ClassPathResource("reports/report1/financialstatementdata.json").getFile().getAbsolutePath());


        //PropertyGetter getterTest = new PropertyGetterFromJSONFileImpl(new ClassPathResource("reports/report1/payment order.json").getFile().getPath());
        PropertyGetter getterTest = new PropertyGetterFromJSONStringImpl(jsonString);
        //PropertyGetter getterTest = new PropertyGetterFromJSONFileImpl(new ClassPathResource("reports/report1/invoice.json").getFile().getPath());
        Object rep = Reporting.CreateDocumentEx(markup, fontBodies, getterTest);
        //Reporting.writeDocument("/home/anton/docs/payment order.pdf", rep);
        Reporting.writeDocument("/home/anton/docs/financialstatement.pdf", rep);
    }

    @Test
    public void test2() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException, DocumentException {
        String markup = getFileContent(new ClassPathResource("reports/report1/invoice.xml").getFile().getAbsolutePath());
        //String markup = getFileContent(new ClassPathResource("reports/report1/financial statement.xml").getFile().getAbsolutePath());


        HashMap<String, byte[]> fontBodies = new HashMap<String, byte[]>();
        fontBodies.put("arial", getFileFontByteContent("arial"));
        fontBodies.put("times", getFileFontByteContent("times"));
        fontBodies.put("arialbi", getFileFontByteContent("arialbi"));
        fontBodies.put("ariali", getFileFontByteContent("ariali"));
        fontBodies.put("arialbd", getFileFontByteContent("arialbd"));
        fontBodies.put("wingding", getFileFontByteContent("wingding"));

        //ConversionUtils.XMLtoJSON(new ClassPathResource("reports/report1/financialstatementdata.xml").getFile().getAbsolutePath(), new ClassPathResource("reports/report1/financialstatementdata.json").getFile().getAbsolutePath());

        PropertyGetter getterTest = new PropertyGetterFromJSONFileImpl(new ClassPathResource("reports/report1/invoice.json").getFile().getPath());
        Object rep = Reporting.CreateDocumentEx(markup, fontBodies, getterTest);
        Reporting.writeDocument("/home/anton/docs/invoice.pdf", rep);
        //Reporting.writeDocument("/home/anton/docs/financialstatement.pdf", rep);
    }
}
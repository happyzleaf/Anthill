package it.unipegaso.taranto.parser.route;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteHTML implements IRoute<String> {
	private Tidy tidy;
	private File temp;
	
	RouteHTML() {
		tidy = new Tidy();
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setWraplen(Integer.MAX_VALUE);
		tidy.setPrintBodyOnly(true);
		tidy.setXmlOut(true);
		tidy.setSmartIndent(true);
		tidy.setShowErrors(0);
		
		temp = new File(System.getProperty("user.dir"), "anthill/database/temp/pdftohtml");
		temp.mkdirs();
	}
	
	@Override
	public String parse(String string) throws Exception {
		try {
			File pdf = new File(temp, System.currentTimeMillis() + ".pdf");
			//Tidy
			ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			tidy.parseDOM(inputStream, outputStream);
			String formatted = outputStream.toString("UTF-8");
			
			//iText
			OutputStream file = new FileOutputStream(pdf);
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, file);
			document.open();
			InputStream is = new ByteArrayInputStream(formatted.getBytes());
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
			document.close();
			file.close();
			String r = EnumRoute.PDF.parse(pdf);
			if (r != null) return r;
			return EnumRoute.PDF.parse(pdf);
		} catch (Exception ignored) {}
		return EnumRoute.STRING.parse(string.replaceAll("\\<[^>]*>", ""));
	}
}

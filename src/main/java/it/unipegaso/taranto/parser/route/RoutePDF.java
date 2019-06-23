package it.unipegaso.taranto.parser.route;

import java.io.File;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 *
 * "The bigger they are, the harder they fall"
 * I tried to force to render a pdf through the specific font "Inconsolata" because it's recognized far easier by the computer,
 * but I ran out of time and I just passed the file directly to the image parser.
 */
public class RoutePDF implements IRoute<File> {
	//private static File inconsolata = new File(System.getProperty("user.dir"), "anthill/util/Inconsolata.ttf");
	
	@Override
	public String parse(File file) throws Exception {
		/*RandomAccessFile raf = new RandomAccessFile(file, "r");
		org.apache.pdfbox.pdfparser.PDFParser parser = new org.apache.pdfbox.pdfparser.PDFParser(raf);
		parser.parse();
		PDDocument document = new PDDocument(parser.getDocument());*/
		//PDDocument document = PDDocument.load(file);
		
		/*PDPage page = document.getPage(0);
		PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.OVERWRITE, true);
		PDFTextStripper stripper = new FontChangingTextStripper(content, PDType0Font.load(document, inconsolata));
		stripper.processPage(page);
		content.close();
		document.save(file);*/
		
		/*PDFRenderer pdfRenderer = new PDFRenderer(document);
		ImageIOUtil.writeImage(pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB), (file.toString().contains(".") ? file.toString().substring(0, file.toString().lastIndexOf(".")) : file) + ".png", 300);
		*/
		//document.close();
		//raf.close();
		return EnumRoute.IMAGE.parse(file);
	}
	
	/*private static class FontChangingTextStripper extends PDFTextStripper {
		private static Field posFont;
		static {
			try {
				posFont = TextPosition.class.getDeclaredField("font");
				posFont.setAccessible(true);
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(posFont, posFont.getModifiers() & ~Modifier.FINAL);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		private PDPageContentStream canvas;
		private PDType0Font font;
		
		public FontChangingTextStripper(PDPageContentStream canvas, PDType0Font font) throws IOException {
			this.canvas = canvas;
			this.font = font;
			for (int i = 0; i < charactersByArticle.size(); i++) {
				List<TextPosition> c = charactersByArticle.get(i);
				for (int j = 0; j < c.size(); j++){
					processTextPosition(c.get(j));
				}
			}
		}
		
		@Override
		protected void processTextPosition(TextPosition text) {
			super.processTextPosition(setFont(text));
		}
		
		private TextPosition setFont(TextPosition pos) {
			try {
				posFont.set(pos, font);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return pos;
		}
	}*/
}

package uk.gov.hmcts.sptribs.document.pdf.converter;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ImageConverter implements FileToPDFConverter {

    @Override
    public List<String> accepts() {
        return List.of("bmp",
                "jpeg",
                "png",
                "tiff",
                "jpg"
        );
    }

    @Override
    public ConvertedPdf convert(byte[] fileContent, String fileName) throws IOException {
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage();

        document.addPage(page);

        final PDRectangle mediaBox = page.getMediaBox();
        final PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, fileContent, fileName);
        final PDPageContentStream contents = new PDPageContentStream(document, page);
        final Dimension originalSize = new Dimension(pdImage.getWidth(), pdImage.getHeight());
        final Dimension maxSize = new Dimension((int)mediaBox.getWidth(), (int)mediaBox.getHeight());
        final Dimension scaledImageSize = getScaledDimension(originalSize, maxSize);
        final float startX = (mediaBox.getWidth() - scaledImageSize.width) / 2;
        final float startY = (mediaBox.getHeight() - scaledImageSize.height) / 2;

        try {
            contents.drawImage(pdImage, startX, startY, scaledImageSize.width, scaledImageSize.height);
        } finally {
            contents.close();
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        document.save(byteStream);
        document.close();

        final String convertedFileName = fileName + ".pdf";

        return ConvertedPdf.builder()
            .fileContent(byteStream.toByteArray())
            .fileName(convertedFileName)
            .build();
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // first check if we need to scale width
        if (originalWidth > boundWidth) {
            //scale width to fit
            newWidth = boundWidth;
            //scale height to maintain aspect ratio
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        // then check if we need to scale even with the new height
        if (newHeight > boundHeight) {
            //scale height to fit instead
            newHeight = boundHeight;
            //scale width to maintain aspect ratio
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }
}

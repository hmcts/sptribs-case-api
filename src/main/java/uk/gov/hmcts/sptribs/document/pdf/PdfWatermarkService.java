package uk.gov.hmcts.sptribs.document.pdf;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.document.pdf.converter.ConvertedPdf;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfWatermarkService {

    private final PdfWatermarker pdfWatermarker;

    public Pair<List<ConvertedPdf>, Integer> addWatermarkToPdfs(List<ConvertedPdf> pdfs, int pageNumber) {
        for (ConvertedPdf pdf : pdfs) {
            try {
                String leftText = pdf.getOriginalDocument().getDocumentCategory().getCategory();
                var result = pdfWatermarker.shrinkAndWatermarkPdf(pdf.getFileContent(), leftText, pageNumber);
                pdf.setFileContent(result.getLeft());
                pageNumber = result.getRight();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Pair.of(pdfs, pageNumber);
    }
}

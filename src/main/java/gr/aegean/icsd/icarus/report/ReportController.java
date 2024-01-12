package gr.aegean.icsd.icarus.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v0/tests/{testId}/executions/{executionID}/reports")
public class ReportController {


    private final ReportService service;



    public ReportController(ReportService service) {
        this.service = service;
    }



    @GetMapping(produces = "application/pdf")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long testId, @PathVariable Long executionID) {

        Report pdfReport = service.getReport(testId, executionID);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Here you have to set the actual filename of your pdf
        String filename = pdfReport.getDocumentName();
        headers.setContentDispositionFormData(filename, filename);

        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfReport.getReportDocument(), headers, HttpStatus.OK);
        }


}

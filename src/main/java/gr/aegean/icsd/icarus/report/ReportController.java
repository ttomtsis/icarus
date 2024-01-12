package gr.aegean.icsd.icarus.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v0/tests/{testId}/executions")
public class ReportController {


    private final ReportService service;



    public ReportController(ReportService service) {
        this.service = service;
    }



    @GetMapping(value = "/reports/", produces = "application/pdf", params = "executionID")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long testId, @RequestParam Long executionID) {

        Report pdfReport = service.getReport(testId, executionID);

        HttpHeaders headers = createHeaders(pdfReport);

        return new ResponseEntity<>(pdfReport.getReportDocument(), headers, HttpStatus.OK);
        }


    @GetMapping(value = "/reports/", produces = "application/pdf", params = "deploymentId")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long testId, @RequestParam String deploymentId) {

        Report pdfReport = service.getReport(testId, deploymentId);

        HttpHeaders headers = createHeaders(pdfReport);

        return new ResponseEntity<>(pdfReport.getReportDocument(), headers, HttpStatus.OK);
    }



    private HttpHeaders createHeaders(Report pdfReport) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        String filename = pdfReport.getDocumentName();
        headers.setContentDispositionFormData(filename, filename);

        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return headers;
    }


}

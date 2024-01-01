package gr.aegean.icsd.icarus.report;

import org.springframework.stereotype.Service;


@Service
public class ReportService {


    public Report createReport() {
        return new Report();
    }

}

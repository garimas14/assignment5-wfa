package edu.srmist.outpass.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class OutpassWorkers {

    private static final Logger log = LoggerFactory.getLogger(OutpassWorkers.class);

    @JobWorker(type = "check-outpass")
    public Map<String, Object> checkOutpass(
            @Variable String outDate,
            @Variable String returnDate
    ) {
        LocalDate out;
        LocalDate back;
        try {
            out = LocalDate.parse(outDate);
            back = LocalDate.parse(returnDate);
        } catch (DateTimeParseException | NullPointerException ex) {
            return Map.of("valid", false, "nights", -1L, "approved", false,
                    "validationMessage", "Dates must use yyyy-MM-dd format.");
        }

        long nights = ChronoUnit.DAYS.between(out, back);
        boolean valid = nights >= 0;
        boolean autoApproved = valid && nights == 0;

        return Map.of(
                "valid", valid,
                "nights", nights,
                "approved", autoApproved,
                "validationMessage", valid ? "Request dates are valid." :
                        "Return date cannot be before the out date."
        );
    }

    @JobWorker(type = "issue-outpass")
    public Map<String, Object> issueOutpass(
            @Variable String regNo,
            @Variable String outDate,
            @Variable Boolean approved,
            @Variable String wardenRemarks
    ) {
        boolean isApproved = Boolean.TRUE.equals(approved);
        String status = isApproved ? "ISSUED" : "REJECTED";
        String passNumber = isApproved ? "OP-" + regNo + "-" + outDate : "";

        log.info("Out-pass decision: status={}, regNo={}, passNumber={}, remarks={}",
                status, regNo, passNumber,
                wardenRemarks == null || wardenRemarks.isBlank() ? "No remarks" : wardenRemarks);

        return Map.of(
                "passStatus", status,
                "passNumber", passNumber
        );
    }
}

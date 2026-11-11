package edu.srmist.outpass.api;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/outpass")
public class OutpassController {

    private final CamundaClient camundaClient;

    public OutpassController(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @PostMapping
    public Map<String, Object> applyForOutpass(@RequestBody OutpassRequest request) {
        if (request.studentName() == null || request.studentName().isBlank()
                || request.regNo() == null || request.regNo().isBlank()
                || request.hostelBlock() == null || request.hostelBlock().isBlank()
                || request.outDate() == null || request.returnDate() == null
                || request.parentPhone() == null || request.parentPhone().isBlank()
                || request.purpose() == null || request.purpose().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "All fields are required: studentName, regNo, hostelBlock, outDate, returnDate, parentPhone, purpose");
        }

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("studentName", request.studentName());
        variables.put("regNo", request.regNo());
        variables.put("hostelBlock", request.hostelBlock());
        variables.put("outDate", request.outDate());
        variables.put("returnDate", request.returnDate());
        variables.put("parentPhone", request.parentPhone());
        variables.put("purpose", request.purpose());

        ProcessInstanceEvent instance = camundaClient.newCreateInstanceCommand()
                .bpmnProcessId("hostel-outpass")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        return Map.of(
                "message", "Out-pass request submitted",
                "processInstanceKey", instance.getProcessInstanceKey()
        );
    }

    public record OutpassRequest(
            String studentName,
            String regNo,
            String hostelBlock,
            String outDate,
            String returnDate,
            String parentPhone,
            String purpose
    ) {}
}

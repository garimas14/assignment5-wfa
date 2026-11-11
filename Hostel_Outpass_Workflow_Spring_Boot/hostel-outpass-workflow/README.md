# Hostel Out-Pass Workflow — Camunda 8 SaaS + Spring Boot

This is a starter implementation of the assignment in the supplied lab sheet.

## What it does

1. `POST /outpass` starts the `hostel-outpass` process.
2. `check-outpass` calculates the number of nights and validates that return date is not before out date.
3. Invalid dates are rejected; a day-out request (0 nights) is auto-approved; overnight requests wait for Warden approval in Tasklist.
4. `issue-outpass` sets `passStatus` and creates a pass number for approved requests. The worker logs a simulated parent notification.

## Requirements

- JDK 17+
- Maven 3.9+
- Camunda 8.8 SaaS cluster
- Camunda API client with the Zeebe / Orchestration Cluster scope

## Configure credentials

Set these environment variables before starting the application:

- `CAMUNDA_CLIENT_ID`
- `CAMUNDA_CLIENT_SECRET`
- `CAMUNDA_CLUSTER_ID`
- `CAMUNDA_CLUSTER_REGION` (for example, the region shown in your Camunda Console)

Do not commit real credentials to GitHub or submit them in your project ZIP.

Windows PowerShell example (replace values with your own):

```powershell
$env:CAMUNDA_CLIENT_ID="your-client-id"
$env:CAMUNDA_CLIENT_SECRET="your-client-secret"
$env:CAMUNDA_CLUSTER_ID="your-cluster-id"
$env:CAMUNDA_CLUSTER_REGION="your-region"
mvn spring-boot:run
```

macOS/Linux example:

```bash
export CAMUNDA_CLIENT_ID="your-client-id"
export CAMUNDA_CLIENT_SECRET="your-client-secret"
export CAMUNDA_CLUSTER_ID="your-cluster-id"
export CAMUNDA_CLUSTER_REGION="your-region"
mvn spring-boot:run
```

The app deploys `hostel-outpass.bpmn` and `warden-approval.form` at startup.

## Test the REST endpoint

Dates must be `yyyy-MM-dd`. Use future dates for a normal demo.

### 1. Day out — auto-approved (0 nights)

```bash
curl -X POST http://localhost:8080/outpass \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Priya","regNo":"RA2411056010001","hostelBlock":"A Block","outDate":"2026-11-20","returnDate":"2026-11-20","parentPhone":"9876543210","purpose":"Personal work"}'
```

Expected final variables: `approved=true`, `passStatus=ISSUED`, and a generated `passNumber`.

### 2. Overnight — Warden approval (1+ nights)

```bash
curl -X POST http://localhost:8080/outpass \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Priya","regNo":"RA2411056010001","hostelBlock":"A Block","outDate":"2026-11-20","returnDate":"2026-11-22","parentPhone":"9876543210","purpose":"Family function"}'
```

Expected: instance waits at **Warden approval**. In Tasklist, open the task, assign it to yourself, tick **Approve this out-pass** and add remarks, then complete it. If the box is unticked, the request is rejected.

### 3. Invalid — return date before out date

```bash
curl -X POST http://localhost:8080/outpass \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Priya","regNo":"RA2411056010001","hostelBlock":"A Block","outDate":"2026-11-22","returnDate":"2026-11-20","parentPhone":"9876543210","purpose":"Invalid date test"}'
```

Expected final variables: `valid=false`, `approved=false`, `passStatus=REJECTED`.

## Important Camunda Modeler checks

- Process ID: `hostel-outpass`
- Service task type 1: `check-outpass`
- Service task type 2: `issue-outpass`
- Form ID: `warden-approval-form`
- Form field keys: `approved`, `wardenRemarks`

The BPMN includes three decision paths. Both the invalid and auto-approved paths use a short service task to call `issue-outpass`; the overnight path goes through the Warden user task and then calls the same worker.

## Submission checklist

- `src/main/resources/bpmn/hostel-outpass.bpmn`
- `src/main/resources/bpmn/warden-approval.form`
- Complete Spring Boot project ZIP
- Screenshots of one auto-approved, one Warden-approved, and one rejected instance in Operate
- Screenshot of the Warden task/form in Tasklist
- Half-page worker variable note in `docs/VARIABLES-NOTE.md`

## Notes

- Parent notification is simulated using an application log, as in the supplied lab exercise; no real SMS is sent.
- The sample dates are examples. Change them to future dates when you run the demo.
- Verify that the Camunda cluster and starter versions match before running.

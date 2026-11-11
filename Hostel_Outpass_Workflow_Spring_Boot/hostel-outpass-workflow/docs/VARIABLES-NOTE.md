# Worker variables — half-page submission note

## 1. `check-outpass` worker

**Reads:** `outDate`, `returnDate`.

**Writes:** `nights`, `valid`, `approved`, `validationMessage`.

The worker parses both dates using `yyyy-MM-dd` and calculates `nights` as the number of days between the out date and return date. The request is valid when the return date is the same as or later than the out date. If `nights` is zero, the worker sets `approved=true` for automatic day-out approval. If the date range is invalid, it sets `valid=false` and `approved=false`. Overnight requests remain valid but are routed to the Warden user task.

## 2. `issue-outpass` worker

**Reads:** `regNo`, `outDate`, `approved`, `wardenRemarks`.

**Writes:** `passStatus`, `passNumber`.

If `approved=true`, the worker sets `passStatus=ISSUED` and generates a pass number in the format `OP-<regNo>-<outDate>`. If `approved=false`, it sets `passStatus=REJECTED` and leaves the pass number empty. It logs the decision and Warden remarks as a stand-in for notifying the parent.

## 3. Warden form

The Warden form displays the request details and writes the decision to `approved` and the comment to `wardenRemarks`. These process variables are read by the `issue-outpass` worker after the user task is completed.

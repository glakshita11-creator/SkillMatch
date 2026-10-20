# SkillMatch End-to-End Live Feature Verification Script

$baseUrl = "http://localhost:8080"
$results = @()

function Record-Result($testName, $success, $details) {
    $script:results += [PSCustomObject]@{
        TestName = $testName
        Status   = if ($success) { "PASS" } else { "FAIL" }
        Details  = $details
    }
    $color = if ($success) { "Green" } else { "Red" }
    Write-Host "[$($script:results[-1].Status)] $testName - $details" -ForegroundColor $color
}

# 1. Public Endpoints
try {
    $r = Invoke-WebRequest -Uri "$baseUrl/index.html" -UseBasicParsing
    Record-Result "1.1 Landing Page" ($r.StatusCode -eq 200) "Status $($r.StatusCode)"
} catch { Record-Result "1.1 Landing Page" $false $_.Exception.Message }

try {
    $r = Invoke-RestMethod -Uri "$baseUrl/api/opportunities" -Method GET
    Record-Result "1.2 Public Opportunities API" ($r.success -eq $true -and $r.data.Count -gt 0) "Found $($r.data.Count) opportunities"
} catch { Record-Result "1.2 Public Opportunities API" $false $_.Exception.Message }

try {
    $r = Invoke-RestMethod -Uri "$baseUrl/api/opportunities/1" -Method GET
    Record-Result "1.3 Opportunity Details API" ($r.success -eq $true -and $r.data.id -eq 1) "Opportunity: $($r.data.title)"
} catch { Record-Result "1.3 Opportunity Details API" $false $_.Exception.Message }

# 2. Student Workflow
$studentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    # 2.1 Login
    $loginBody = @{ email = "arun.kumar@student.edu"; password = "Password@123" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -WebSession $studentSession
    Record-Result "2.1 Student Login" ($r.success -eq $true -and $r.data.role -eq "ROLE_STUDENT") "Logged in as $($r.data.fullName)"

    # Get CSRF Token
    $xsrfCookie = $studentSession.Cookies.GetCookies($baseUrl) | Where-Object { $_.Name -eq "XSRF-TOKEN" }
    $csrfToken = if ($xsrfCookie) { [System.Uri]::UnescapeDataString($xsrfCookie.Value) } else { "" }

    # 2.2 Student Dashboard
    $dash = Invoke-RestMethod -Uri "$baseUrl/api/students/me/dashboard" -Method GET -WebSession $studentSession
    Record-Result "2.2 Student Dashboard" ($dash.success -eq $true) "Total Applied: $($dash.data.totalApplications)"

    # 2.3 Student Profile
    $prof = Invoke-RestMethod -Uri "$baseUrl/api/students/me" -Method GET -WebSession $studentSession
    Record-Result "2.3 Student Profile" ($prof.success -eq $true -and $prof.data.department -ne $null) "Dept: $($prof.data.department), CGPA: $($prof.data.cgpa)"

    # 2.4 Eligibility Check on Opp 1
    $elig = Invoke-RestMethod -Uri "$baseUrl/api/opportunities/1/eligibility" -Method GET -WebSession $studentSession
    Record-Result "2.4 Criteria Evaluation" ($elig.success -eq $true) "Eligible: $($elig.data.eligible), Status: $($elig.data.status)"

    # 2.5 My Applications
    $apps = Invoke-RestMethod -Uri "$baseUrl/api/students/me/applications" -Method GET -WebSession $studentSession
    Record-Result "2.5 Student Applications" ($apps.success -eq $true) "Count: $($apps.data.Count)"

    # 2.6 Change Password
    $pwBody = @{ currentPassword = "Password@123"; newPassword = "Password@123" } | ConvertTo-Json
    try {
        $pwRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/change-password" -Method PUT -Body $pwBody -ContentType "application/json" -Headers @{ "X-XSRF-TOKEN" = $csrfToken } -WebSession $studentSession
        Record-Result "2.6 Password Validation" ($false) "Should reject same password"
    } catch {
        # Expected bad request: "New password cannot be the same as your current password."
        Record-Result "2.6 Password Validation" ($_.Exception.Response.StatusCode.value__ -eq 400) "Blocked identical new password (400 Bad Request)"
    }
} catch { Record-Result "2.x Student Flow Error" $false $_.Exception.Message }

# 3. Company Recruiter Workflow
$compSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    # 3.1 Login
    $loginBody = @{ email = "recruiter@techcorp.com"; password = "Password@123" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -WebSession $compSession
    Record-Result "3.1 Recruiter Login" ($r.success -eq $true -and $r.data.role -eq "ROLE_COMPANY") "Logged in as $($r.data.fullName)"

    $xsrfCookieComp = $compSession.Cookies.GetCookies($baseUrl) | Where-Object { $_.Name -eq "XSRF-TOKEN" }
    $csrfComp = if ($xsrfCookieComp) { [System.Uri]::UnescapeDataString($xsrfCookieComp.Value) } else { "" }

    # 3.2 Company Dashboard
    $compDash = Invoke-RestMethod -Uri "$baseUrl/api/companies/me/dashboard" -Method GET -WebSession $compSession
    Record-Result "3.2 Company Dashboard" ($compDash.success -eq $true) "Postings: $($compDash.data.totalOpportunities), Applicants: $($compDash.data.totalApplications)"

    # 3.3 Company Postings
    $compOpps = Invoke-RestMethod -Uri "$baseUrl/api/companies/me/opportunities" -Method GET -WebSession $compSession
    Record-Result "3.3 Company Postings" ($compOpps.success -eq $true) "Found $($compOpps.data.Count) company openings"

    # 3.4 Company Applicants
    $compApps = Invoke-RestMethod -Uri "$baseUrl/api/companies/me/applicants" -Method GET -WebSession $compSession
    Record-Result "3.4 Company Applicants" ($compApps.success -eq $true) "Found $($compApps.data.Count) applicants"

    # 3.5 Recruiter CSV Export
    $csvRes = Invoke-WebRequest -Uri "$baseUrl/api/companies/me/applicants/export-csv" -Method GET -WebSession $compSession -UseBasicParsing
    Record-Result "3.5 Recruiter CSV Export" ($csvRes.StatusCode -eq 200 -and $csvRes.Content.Contains("Application ID,Candidate Name,Email")) "CSV Export Size: $($csvRes.Content.Length) bytes"

    # 3.6 Recruiter Update Applicant Status & Schedule Interview
    if ($compApps.data.Count -gt 0) {
        $firstApp = $compApps.data[0]
        $statusBody = @{
            status = "INTERVIEW_SCHEDULED"
            recruiterNotes = "Selected for technical round 1 on campus."
            interviewDate = "2026-10-25T14:30:00"
        } | ConvertTo-Json
        $updateRes = Invoke-RestMethod -Uri "$baseUrl/api/applications/$($firstApp.id)/status" -Method PATCH -Body $statusBody -ContentType "application/json" -Headers @{ "X-XSRF-TOKEN" = $csrfComp } -WebSession $compSession
        Record-Result "3.6 Interview Scheduling & Status" ($updateRes.success -eq $true -and $updateRes.data.status -eq "INTERVIEW_SCHEDULED") "Updated app #$($firstApp.id) to INTERVIEW_SCHEDULED"
    }
} catch { Record-Result "3.x Company Flow Error" $false $_.Exception.Message }

# 4. Administrator Workflow
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    # 4.1 Login
    $loginBody = @{ email = "admin@skillmatch.edu"; password = "Admin@123" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -WebSession $adminSession
    Record-Result "4.1 Admin Login" ($r.success -eq $true -and $r.data.role -eq "ROLE_ADMIN") "Logged in as $($r.data.fullName)"

    # 4.2 Admin Dashboard
    $admDash = Invoke-RestMethod -Uri "$baseUrl/api/admin/dashboard" -Method GET -WebSession $adminSession
    Record-Result "4.2 Admin Dashboard" ($admDash.success -eq $true) "Students: $($admDash.data.totalStudents), Companies: $($admDash.data.totalCompanies), Opps: $($admDash.data.totalOpportunities)"

    # 4.3 Admin User Directory
    $admUsers = Invoke-RestMethod -Uri "$baseUrl/api/admin/users" -Method GET -WebSession $adminSession
    Record-Result "4.3 User Directory" ($admUsers.success -eq $true -and $admUsers.data.Count -gt 0) "Total Users: $($admUsers.data.Count)"

    # 4.4 Admin Opportunity Audit
    $admOpps = Invoke-RestMethod -Uri "$baseUrl/api/admin/opportunities" -Method GET -WebSession $adminSession
    Record-Result "4.4 Opportunity Audit" ($admOpps.success -eq $true -and $admOpps.data.Count -gt 0) "Total Audited: $($admOpps.data.Count)"

    # 4.5 Admin CSV Export
    $admCsv = Invoke-WebRequest -Uri "$baseUrl/api/admin/applications/export-csv" -Method GET -WebSession $adminSession -UseBasicParsing
    Record-Result "4.5 Admin CSV Export" ($admCsv.StatusCode -eq 200 -and $admCsv.Content.Contains("Application ID,Candidate Name,Email")) "Export Size: $($admCsv.Content.Length) bytes"
} catch { Record-Result "4.x Admin Flow Error" $false $_.Exception.Message }

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "TEST EXECUTION SUMMARY: $($results.Count) Tests Run" -ForegroundColor Cyan
$failedCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
if ($failedCount -eq 0) {
    Write-Host "ALL $results.Count FEATURES PASSED FLAWLESSLY!" -ForegroundColor Green
} else {
    Write-Host "$failedCount TESTS FAILED!" -ForegroundColor Red
}
Write-Host "=======================================================" -ForegroundColor Cyan

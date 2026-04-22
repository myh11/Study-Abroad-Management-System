[CmdletBinding()]
param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$MysqlExecutable = "mysql",
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbName = "admission_system",
    [string]$DbUser = "root",
    [string]$DbPassword = "mYh031005",
    [switch]$SkipDbAssertions
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Fail {
    param([string]$Message)
    throw $Message
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )
    if (-not $Condition) {
        Fail $Message
    }
}

function Assert-Equal {
    param(
        $Expected,
        $Actual,
        [string]$Message
    )
    if ($Expected -ne $Actual) {
        Fail "$Message Expected=[$Expected] Actual=[$Actual]"
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body,
        [string]$Token
    )
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    $params = @{
        Method      = $Method
        Uri         = "$ApiBaseUrl$Path"
        Headers     = $headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }
    return Invoke-RestMethod @params
}

function Upload-TranscriptFile {
    param(
        [string]$Token,
        [string]$FileLabel,
        [string]$Content
    )
    $tempFile = Join-Path $env:TEMP "$FileLabel.txt"
    Set-Content -LiteralPath $tempFile -Value $Content -Encoding UTF8
    try {
        $headers = @("Authorization: Bearer $Token")
        $raw = & curl.exe -s -X POST "$ApiBaseUrl/api/files/transcripts" -H $headers[0] -F "file=@$tempFile"
        if ($LASTEXITCODE -ne 0) {
            Fail "Failed to upload transcript file: $FileLabel"
        }
        $response = $raw | ConvertFrom-Json
        Assert-Equal 0 $response.code "Upload transcript API failed"
        return [long]$response.data.fileId
    }
    finally {
        Remove-Item -LiteralPath $tempFile -ErrorAction SilentlyContinue
    }
}

function Login {
    param(
        [string]$Username,
        [string]$Password
    )
    $response = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body @{
        username = $Username
        password = $Password
    }
    Assert-Equal 0 $response.code "Login failed for $Username"
    return $response.data.token
}

function Invoke-MySqlQuery {
    param([string]$Sql)
    if ($SkipDbAssertions) {
        return ""
    }
    $args = @(
        "-h", $DbHost,
        "-P", "$DbPort",
        "-u$DbUser",
        "-p$DbPassword",
        "-N",
        "-B",
        $DbName,
        "-e", $Sql
    )
    $output = & $MysqlExecutable @args
    if ($LASTEXITCODE -ne 0) {
        Fail "MySQL query failed: $Sql"
    }
    return ($output -join "`n").Trim()
}

function Get-ApplicationStatus {
    param([long]$ApplicationId)
    return Invoke-MySqlQuery "SELECT current_status FROM applications WHERE id = $ApplicationId;"
}

function Get-StatusHistoryCount {
    param([long]$ApplicationId)
    return [int](Invoke-MySqlQuery "SELECT COUNT(*) FROM application_status_histories WHERE application_id = $ApplicationId;")
}

function Get-AuditLogCount {
    param([string]$EntityType, [string]$EntityId)
    return [int](Invoke-MySqlQuery "SELECT COUNT(*) FROM audit_logs WHERE entity_type = '$EntityType' AND entity_id = '$EntityId';")
}

function Get-QuotaState {
    param(
        [long]$BatchId,
        [string]$SchoolCode,
        [string]$MajorCode
    )
    $schoolRow = Invoke-MySqlQuery "SELECT CONCAT(used_quota, ',', remaining_quota) FROM school_quotas WHERE batch_id = $BatchId AND school_code = '$SchoolCode';"
    $majorRow = Invoke-MySqlQuery "SELECT CONCAT(used_quota, ',', remaining_quota) FROM major_quotas WHERE batch_id = $BatchId AND major_code = '$MajorCode';"
    $schoolParts = $schoolRow.Split(",")
    $majorParts = $majorRow.Split(",")
    return @{
        schoolUsed      = [int]$schoolParts[0]
        schoolRemaining = [int]$schoolParts[1]
        majorUsed       = [int]$majorParts[0]
        majorRemaining  = [int]$majorParts[1]
    }
}

function Promote-Waitlist {
    param(
        [string]$Token,
        [long]$ApplicationId
    )
    $response = Invoke-Api -Method "POST" -Path "/api/waitlists/$ApplicationId/promote" -Token $Token -Body $null
    Assert-Equal 0 $response.code "Promote waitlist failed"
}

function New-DraftPayload {
    param(
        [long]$FileId,
        [long]$Index,
        [string]$SchoolCode,
        [string]$MajorCode
    )
    return @{
        student = @{
            name          = "Smoke Student $Index"
            gender        = "MALE"
            birthDate     = "2006-01-0$(([math]::Min($Index, 9)))"
            currentSchool = "Smoke High School"
            grade         = "G12"
            email         = "smoke$Index@example.com"
            phone         = "1380000$("{0:D4}" -f $Index)"
            idCardNo      = "SMOKE20260421$("{0:D4}" -f $Index)"
        }
        score = @{
            transcriptSchoolName = "Smoke High School"
            termStart            = "2025-09-01"
            termEnd              = "2026-01-20"
            chinese              = 84
            math                 = 92
            english              = 86
            physics              = 88
            chemistry            = 83
            history              = 80
            averageScore         = 86
            failedSubjectCount   = 0
            fileId               = $FileId
        }
        personalStatement = @{
            content = "Smoke test statement $Index for end-to-end admission workflow verification."
        }
        batchId          = 1
        targetSchoolCode = $SchoolCode
        targetMajorCode  = $MajorCode
    }
}

function Create-Draft {
    param(
        [string]$Token,
        [long]$FileId,
        [long]$Index,
        [string]$SchoolCode,
        [string]$MajorCode
    )
    $response = Invoke-Api -Method "POST" -Path "/api/applications" -Token $Token -Body (New-DraftPayload -FileId $FileId -Index $Index -SchoolCode $SchoolCode -MajorCode $MajorCode)
    Assert-Equal 0 $response.code "Create draft failed"
    return [long]$response.data.applicationId
}

function Submit-Application {
    param([string]$Token, [long]$ApplicationId)
    $response = Invoke-Api -Method "POST" -Path "/api/applications/$ApplicationId/submit" -Token $Token -Body $null
    Assert-Equal 0 $response.code "Submit application failed"
}

function Claim-DomesticReview {
    param([string]$Token, [long]$ApplicationId)
    $response = Invoke-Api -Method "POST" -Path "/api/domesticreviews/$ApplicationId/claim" -Token $Token -Body $null
    Assert-Equal 0 $response.code "Claim domestic review failed"
}

function Submit-DomesticReview {
    param(
        [string]$Token,
        [long]$ApplicationId,
        [string]$Result,
        [string]$Comment
    )
    $response = Invoke-Api -Method "POST" -Path "/api/domesticreviews/$ApplicationId/submit" -Token $Token -Body @{
        materialComplete     = $true
        identityMatched      = $true
        basicScorePassed     = $true
        authenticityRiskLevel = "B"
        standardizationPassed = $true
        result               = $Result
        comment              = $Comment
    }
    Assert-Equal 0 $response.code "Submit domestic review failed"
}

function Submit-Supplement {
    param(
        [string]$Token,
        [long]$ApplicationId,
        [long]$NewTranscriptFileId
    )
    $response = Invoke-Api -Method "POST" -Path "/api/applications/$ApplicationId/supplement" -Token $Token -Body @{
        newTranscriptFileId = $NewTranscriptFileId
        supplementNote      = "Smoke supplement upload"
    }
    Assert-Equal 0 $response.code "Submit supplement failed"
}

function Submit-SchoolReview {
    param(
        [string]$Token,
        [long]$ApplicationId,
        [string]$Result,
        [string]$SuggestedMajorCode
    )
    $body = @{
        review_result         = $Result
        school_threshold_passed = $true
        major_threshold_passed  = $true
        school_quota_passed     = $true
        major_quota_passed      = $true
        academic_score          = 88
        material_score          = 85
        matching_score          = 86
        total_score             = 259
        review_reason           = "Smoke verification"
    }
    if ($SuggestedMajorCode) {
        $body["suggested_major_code"] = $SuggestedMajorCode
    }
    $response = Invoke-Api -Method "POST" -Path "/api/school-reviews/$ApplicationId/submit" -Token $Token -Body $body
    Assert-Equal 0 $response.code "Submit school review failed"
}

function Confirm-Waitlist {
    param(
        [string]$Token,
        [long]$ApplicationId,
        [bool]$Accept
    )
    $response = Invoke-Api -Method "POST" -Path "/api/applications/$ApplicationId/waitlist-confirm" -Token $Token -Body @{
        accept = $Accept
    }
    Assert-Equal 0 $response.code "Waitlist confirm failed"
}

function Accept-Adjustment {
    param(
        [string]$Token,
        [long]$ApplicationId,
        [string]$TargetMajorCode
    )
    $response = Invoke-Api -Method "POST" -Path "/api/applications/$ApplicationId/accept-adjustment" -Token $Token -Body @{
        targetMajorCode = $TargetMajorCode
    }
    Assert-Equal 0 $response.code "Accept adjustment failed"
}

function Close-Application {
    param(
        [string]$Token,
        [long]$ApplicationId
    )
    $response = Invoke-Api -Method "POST" -Path "/api/applications/$ApplicationId/close" -Token $Token -Body $null
    Assert-Equal 0 $response.code "Close application failed"
}

function Get-ApplicationDetail {
    param([string]$Token, [long]$ApplicationId)
    $response = Invoke-Api -Method "GET" -Path "/api/applications/$ApplicationId" -Token $Token -Body $null
    Assert-Equal 0 $response.code "Get application detail failed"
    return $response.data
}

function Get-AdjustmentDraftId {
    param([long]$SourceApplicationId)
    return [long](Invoke-MySqlQuery "SELECT id FROM applications WHERE source_application_id = $SourceApplicationId ORDER BY id DESC LIMIT 1;")
}

function Find-AdjustmentDraftIdFromList {
    param(
        [string]$Token,
        [string]$TargetSchoolCode,
        [string]$TargetMajorCode
    )
    $response = Invoke-Api -Method "GET" -Path "/api/applications/my?page=1&pageSize=50" -Token $Token -Body $null
    Assert-Equal 0 $response.code "List my applications failed"
    $item = $response.data.list |
        Where-Object { $_.status -eq "DRAFT" -and $_.targetSchoolCode -eq $TargetSchoolCode -and $_.targetMajorCode -eq $TargetMajorCode } |
        Select-Object -First 1
    if (-not $item) {
        Fail "Adjustment draft not found from application list fallback."
    }
    return [long]$item.applicationId
}

Write-Step "Log in with smoke-test accounts"
$agentToken = Login -Username "agent01" -Password "agent123"
$domesticToken = Login -Username "domestic01" -Password "domestic123"
$schoolUsydToken = Login -Username "school_usyd_01" -Password "school123"
$schoolAnuToken = Login -Username "school_anu_01" -Password "school123"
$adminToken = Login -Username "admin" -Password "admin123"

$results = New-Object System.Collections.Generic.List[object]

Write-Step "Scenario 1: login -> create draft -> submit -> domestic supplement -> supplement upload -> pass"
$file1 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-1a" -Content "Initial transcript for supplement flow"
$app1 = Create-Draft -Token $agentToken -FileId $file1 -Index 1 -SchoolCode "USYD" -MajorCode "USYD_BIZ"
Submit-Application -Token $agentToken -ApplicationId $app1
Claim-DomesticReview -Token $domesticToken -ApplicationId $app1
Submit-DomesticReview -Token $domesticToken -ApplicationId $app1 -Result "SUPPLEMENT_REQUIRED" -Comment "Need clearer transcript"
if (-not $SkipDbAssertions) {
    Assert-Equal "DOMESTIC_SUPPLEMENT" (Get-ApplicationStatus -ApplicationId $app1) "Scenario 1 supplement status mismatch."
}
$file2 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-1b" -Content "Supplemented transcript for pass flow"
Submit-Supplement -Token $agentToken -ApplicationId $app1 -NewTranscriptFileId $file2
Submit-Application -Token $agentToken -ApplicationId $app1
Claim-DomesticReview -Token $domesticToken -ApplicationId $app1
Submit-DomesticReview -Token $domesticToken -ApplicationId $app1 -Result "PASS" -Comment "Domestic review passed"
$detail1 = Get-ApplicationDetail -Token $agentToken -ApplicationId $app1
Assert-Equal "SCHOOL_REVIEWING" $detail1.applicationInfo.currentStatus "Scenario 1 final status mismatch."
$results.Add([pscustomobject]@{
    Scenario = "supplement-pass"
    ApplicationId = $app1
    FinalStatus = $detail1.applicationInfo.currentStatus
    StatusHistoryCount = ($detail1.statusHistory | Measure-Object).Count
    AuditLogCount = ($detail1.auditLogs | Measure-Object).Count
}) | Out-Null

Write-Step "Scenario 2: school review waitlist -> promote -> agent confirms -> reserved"
$file3 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-2" -Content "Transcript for waitlist confirm flow"
$app2 = Create-Draft -Token $agentToken -FileId $file3 -Index 2 -SchoolCode "ANU" -MajorCode "ANU_IR"
if (-not $SkipDbAssertions) {
    $quotaBeforeWaitlist = Get-QuotaState -BatchId 1 -SchoolCode "ANU" -MajorCode "ANU_IR"
}
Submit-Application -Token $agentToken -ApplicationId $app2
Claim-DomesticReview -Token $domesticToken -ApplicationId $app2
Submit-DomesticReview -Token $domesticToken -ApplicationId $app2 -Result "PASS" -Comment "Ready for school review"
Submit-SchoolReview -Token $schoolAnuToken -ApplicationId $app2 -Result "WAITLIST" -SuggestedMajorCode ""
if (-not $SkipDbAssertions) {
    Assert-Equal "WAITLISTED" (Get-ApplicationStatus -ApplicationId $app2) "Scenario 2 waitlist status mismatch."
    Promote-Waitlist -Token $schoolAnuToken -ApplicationId $app2
    Assert-Equal "WAITLIST_PENDING_CONFIRM" (Get-ApplicationStatus -ApplicationId $app2) "Scenario 2 promotion failed."
}
Confirm-Waitlist -Token $agentToken -ApplicationId $app2 -Accept $true
$detail2 = Get-ApplicationDetail -Token $agentToken -ApplicationId $app2
Assert-Equal "RESERVED" $detail2.applicationInfo.currentStatus "Scenario 2 reserved status mismatch."
if (-not $SkipDbAssertions) {
    $quotaAfterWaitlist = Get-QuotaState -BatchId 1 -SchoolCode "ANU" -MajorCode "ANU_IR"
    Assert-Equal ($quotaBeforeWaitlist.schoolUsed + 1) $quotaAfterWaitlist.schoolUsed "Scenario 2 school quota used mismatch."
    Assert-Equal ($quotaBeforeWaitlist.majorUsed + 1) $quotaAfterWaitlist.majorUsed "Scenario 2 major quota used mismatch."
}
$results.Add([pscustomobject]@{
    Scenario = "waitlist-confirm"
    ApplicationId = $app2
    FinalStatus = $detail2.applicationInfo.currentStatus
    StatusHistoryCount = ($detail2.statusHistory | Measure-Object).Count
    AuditLogCount = ($detail2.auditLogs | Measure-Object).Count
}) | Out-Null

Write-Step "Scenario 3: school review reserve direct"
$file4 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-3" -Content "Transcript for direct reserve flow"
$app3 = Create-Draft -Token $agentToken -FileId $file4 -Index 3 -SchoolCode "USYD" -MajorCode "USYD_SE"
if (-not $SkipDbAssertions) {
    $quotaBeforeReserve = Get-QuotaState -BatchId 1 -SchoolCode "USYD" -MajorCode "USYD_SE"
}
Submit-Application -Token $agentToken -ApplicationId $app3
Claim-DomesticReview -Token $domesticToken -ApplicationId $app3
Submit-DomesticReview -Token $domesticToken -ApplicationId $app3 -Result "PASS" -Comment "Ready for direct reserve"
Submit-SchoolReview -Token $schoolUsydToken -ApplicationId $app3 -Result "RESERVE" -SuggestedMajorCode ""
$detail3 = Get-ApplicationDetail -Token $agentToken -ApplicationId $app3
Assert-Equal "RESERVED" $detail3.applicationInfo.currentStatus "Scenario 3 reserved status mismatch."
if (-not $SkipDbAssertions) {
    $quotaAfterReserve = Get-QuotaState -BatchId 1 -SchoolCode "USYD" -MajorCode "USYD_SE"
    Assert-Equal ($quotaBeforeReserve.schoolUsed + 1) $quotaAfterReserve.schoolUsed "Scenario 3 school quota used mismatch."
    Assert-Equal ($quotaBeforeReserve.majorUsed + 1) $quotaAfterReserve.majorUsed "Scenario 3 major quota used mismatch."
}
$results.Add([pscustomobject]@{
    Scenario = "direct-reserve"
    ApplicationId = $app3
    FinalStatus = $detail3.applicationInfo.currentStatus
    StatusHistoryCount = ($detail3.statusHistory | Measure-Object).Count
    AuditLogCount = ($detail3.auditLogs | Measure-Object).Count
}) | Out-Null

Write-Step "Scenario 4: school review adjustment -> new draft -> accept adjustment"
$file5 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-4" -Content "Transcript for adjustment flow"
$app4 = Create-Draft -Token $agentToken -FileId $file5 -Index 4 -SchoolCode "USYD" -MajorCode "USYD_SE"
Submit-Application -Token $agentToken -ApplicationId $app4
Claim-DomesticReview -Token $domesticToken -ApplicationId $app4
Submit-DomesticReview -Token $domesticToken -ApplicationId $app4 -Result "PASS" -Comment "Ready for adjustment"
Submit-SchoolReview -Token $schoolUsydToken -ApplicationId $app4 -Result "SUGGEST_ADJUSTMENT" -SuggestedMajorCode "USYD_BIZ"
if (-not $SkipDbAssertions) {
    Assert-Equal "CLOSED" (Get-ApplicationStatus -ApplicationId $app4) "Scenario 4 source application should be closed."
}
$adjustmentDraftId = if ($SkipDbAssertions) { Find-AdjustmentDraftIdFromList -Token $agentToken -TargetSchoolCode "USYD" -TargetMajorCode "USYD_BIZ" } else { Get-AdjustmentDraftId -SourceApplicationId $app4 }
Accept-Adjustment -Token $agentToken -ApplicationId $adjustmentDraftId -TargetMajorCode "USYD_BIZ"
$detail4 = Get-ApplicationDetail -Token $agentToken -ApplicationId $adjustmentDraftId
Assert-Equal "SUBMITTED" $detail4.applicationInfo.currentStatus "Scenario 4 adjustment draft submit mismatch."
$results.Add([pscustomobject]@{
    Scenario = "adjustment-accept"
    SourceApplicationId = $app4
    ApplicationId = $adjustmentDraftId
    FinalStatus = $detail4.applicationInfo.currentStatus
    StatusHistoryCount = ($detail4.statusHistory | Measure-Object).Count
    AuditLogCount = ($detail4.auditLogs | Measure-Object).Count
}) | Out-Null

Write-Step "Scenario 5: waitlist pending confirm -> admin close as timeout"
$file6 = Upload-TranscriptFile -Token $agentToken -FileLabel "smoke-transcript-5" -Content "Transcript for timeout close flow"
$app5 = Create-Draft -Token $agentToken -FileId $file6 -Index 5 -SchoolCode "ANU" -MajorCode "ANU_IR"
Submit-Application -Token $agentToken -ApplicationId $app5
Claim-DomesticReview -Token $domesticToken -ApplicationId $app5
Submit-DomesticReview -Token $domesticToken -ApplicationId $app5 -Result "PASS" -Comment "Ready for timeout close"
Submit-SchoolReview -Token $schoolAnuToken -ApplicationId $app5 -Result "WAITLIST" -SuggestedMajorCode ""
if (-not $SkipDbAssertions) {
    Promote-Waitlist -Token $schoolAnuToken -ApplicationId $app5
}
Close-Application -Token $adminToken -ApplicationId $app5
$detail5 = Get-ApplicationDetail -Token $adminToken -ApplicationId $app5
Assert-Equal "CLOSED" $detail5.applicationInfo.currentStatus "Scenario 5 close status mismatch."
$results.Add([pscustomobject]@{
    Scenario = "waitlist-timeout-close"
    ApplicationId = $app5
    FinalStatus = $detail5.applicationInfo.currentStatus
    StatusHistoryCount = ($detail5.statusHistory | Measure-Object).Count
    AuditLogCount = ($detail5.auditLogs | Measure-Object).Count
}) | Out-Null

Write-Step "Database assertions"
if (-not $SkipDbAssertions) {
    foreach ($item in $results) {
        if (-not $item.ApplicationId) { continue }
        $historyCount = Get-StatusHistoryCount -ApplicationId $item.ApplicationId
        Assert-True ($historyCount -ge 2) "Application $($item.ApplicationId) should have status history."
        $applicationAuditCount = Get-AuditLogCount -EntityType "APPLICATION" -EntityId "$($item.ApplicationId)"
        Assert-True ($applicationAuditCount -ge 1) "Application $($item.ApplicationId) should have audit logs."
    }
}

Write-Step "Smoke verification summary"
$results | Format-Table -AutoSize

Write-Host ""
Write-Host "Smoke verification completed." -ForegroundColor Green
if ($SkipDbAssertions) {
    Write-Host "DB assertions were skipped. HTTP flow only." -ForegroundColor Yellow
} else {
    Write-Host "DB assertions were included, and waitlist pending-confirm used the public promote API." -ForegroundColor Yellow
}

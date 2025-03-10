# Set the HOME directory to the directory of the script
$WORKING_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition

Write-Output $WORKING_DIR

# Set the FNM_DIR variable
$FNM_DIR = Join-Path -Path $WORKING_DIR -ChildPath ".fnm"

# Create the directory if it does not exist
if (-Not (Test-Path -Path $FNM_DIR)) {
    New-Item -ItemType Directory -Path $FNM_DIR | Out-Null
}

# Download and execute the FNM install script
$zipUrl = "https://github.com/Schniz/fnm/releases/download/v1.38.1/fnm-windows.zip"
$zipFilePath = "$FNM_DIR\installer.zip"
Invoke-WebRequest -Uri $zipUrl -OutFile $zipFilePath

$destinationPath = $FNM_DIR
Expand-Archive -Path $zipFilePath -DestinationPath $destinationPath -Force

# Load the FNM environment and install Node.js version 20.15.1
Invoke-Expression "$FNM_DIR\fnm.exe install 20.15.1 --fnm-dir $FNM_DIR"

Write-Output "Finished FNM installation"
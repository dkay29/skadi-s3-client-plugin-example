#!/usr/bin/env bash
set -euo pipefail

echo "=== Skadi S3 Client Plugin: Top-level S3 Listing ==="
echo

# Prompt for bucket / URI
read -r -p "Enter S3 bucket or URI (e.g. s3://my-bucket/prefix/): " S3_TARGET
if [[ -z "${S3_TARGET}" ]]; then
  echo "ERROR: S3 target is required"
  exit 1
fi

echo

# Prompt for AWS credentials
read -r -p "AWS Access Key ID: " AWS_ACCESS_KEY_ID
read -r -s -p "AWS Secret Access Key: " AWS_SECRET_ACCESS_KEY
echo
read -r -s -p "AWS Session Token (press Enter if not using STS): " AWS_SESSION_TOKEN
echo
read -r -p "AWS Region [us-east-1]: " AWS_REGION

AWS_REGION=${AWS_REGION:-us-east-1}

# Export env vars (only for this process + children)
export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY
export AWS_REGION

if [[ -n "${AWS_SESSION_TOKEN}" ]]; then
  export AWS_SESSION_TOKEN
fi

echo
echo "Using region: ${AWS_REGION}"
echo "Running S3 top-level listing..."
echo "--------------------------------------------------"

# Run the Maven exec
mvn -q -Dexec.args="${S3_TARGET}" exec:java

echo "--------------------------------------------------"
echo "Done."

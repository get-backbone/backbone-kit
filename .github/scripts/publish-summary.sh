#!/usr/bin/env bash
set -euo pipefail

# publish-summary.sh
# Generate GitHub Actions job summary for package publishing
# Reads version from github.event.client_payload (repository_dispatch event)

# ---- Configuration ----------------------------------------------------------

VERSION=$(jq -r '.client_payload.version' "$GITHUB_EVENT_PATH")
GITHUB_STEP_SUMMARY="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

# ---- Helpers -----------------------------------------------------------------

# ---- Main --------------------------------------------------------------------

cat >> "$GITHUB_STEP_SUMMARY" <<EOF
## 📦 Packages Published

**Version:** \`${VERSION}\` (tag: \`v${VERSION}\`)

Deployed to:
- Maven Central
- GitHub Packages
EOF

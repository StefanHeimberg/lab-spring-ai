#!/usr/bin/env bash
#
# load-models.sh
# Pre-Loads all models for usage
# using jq. Requires jq >= 1.6 (for `to_entries`).
#

set -euo pipefail

JSON_FILE="opencode.jsonc"
AUTH_FILE=~/.local/share/opencode/auth.json
PROVIDER="heimnet-ai"

if [[ ! -f "$JSON_FILE" ]]; then
  echo "Error: $JSON_FILE not found in $(pwd)" >&2
  exit 1
fi

if [[ ! -f "$AUTH_FILE" ]]; then
  echo "Error: $AUTH_FILE not found" >&2
  exit 1
fi

# read and validate the provider
BASE_URL=$(sed -re 's#^(([^"\n]*"[^"\n]*")*[^"\n]*)\/\/.*$#\1#' $JSON_FILE | jq -r ".provider.\"${PROVIDER}\".options.baseURL")
if [[ "$BASE_URL" == "null" || -z "$BASE_URL" ]]; then
  echo "Error: Provider '${PROVIDER}' not found in '$JSON_FILE'" >&2
  exit 1
fi

API_TOKEN=$(jq -r ".\"${PROVIDER}\".key" "$AUTH_FILE")
if [[ "$API_TOKEN" == "null" || -z "$API_TOKEN" ]]; then
  echo "Error: API Token for Provider '${PROVIDER}' not found in '$AUTH_FILE'. use '/connect' first" >&2
  exit 1
fi

# Define the function to call for each model
load_model() {
  local base_url="$1"
  local api_token="$2"
  local model="$3"
  echo "  -> loading model: base_url='${base_url}', model='${model}'"
  curl -sS -o /dev/null -X POST "${base_url}/chat/completions" \
    -H "Authorization: Bearer ${api_token}" \
    -H 'Content-Type: application/json' \
    -d "{
        \"model\": \"${model}\",
        \"messages\": [{
          \"role\": \"user\",
          \"content\": \".\"
        }],
        \"max_tokens\": 0
    }"
  echo "  -> done"
}

# Models iterieren & Funktion aufrufen
sed -re 's#^(([^"\n]*"[^"\n]*")*[^"\n]*)\/\/.*$#\1#' $JSON_FILE | jq -r ".provider.\"${PROVIDER}\".models | keys[]" | while IFS= read -r model; do
  load_model "$BASE_URL" "$API_TOKEN" "$model"
done

echo "fertig."
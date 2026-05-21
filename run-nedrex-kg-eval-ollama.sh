#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

MODELS=(
  "gpt-oss:20b"
  "nemotron3:33b"
  "qwen3.6:27b"
  "gemma4:e4b"
)

EVAL_RUNS="${CHATDREX_EVAL_RUNS:-${EVAL_RUNS:-1}}"
CONTINUE_ON_FAILURE="${CONTINUE_ON_FAILURE:-false}"
LOG_DIR="${LOG_DIR:-results/eval/logs}"

mkdir -p "$LOG_DIR"

OPTIONAL_PROPS=()
if [[ -n "${OLLAMA_BASE_URL:-}" ]]; then
  OPTIONAL_PROPS+=("-Dquarkus.langchain4j.openai.base-url=${OLLAMA_BASE_URL}")
fi
if [[ -n "${OLLAMA_API_KEY:-}" ]]; then
  OPTIONAL_PROPS+=("-Dquarkus.langchain4j.openai.api-key=${OLLAMA_API_KEY}")
fi

echo "Running NeDRex KG evaluation for ${#MODELS[@]} Ollama models"
echo "Runs per model: ${EVAL_RUNS}"
echo "Logs: ${LOG_DIR}"

for model in "${MODELS[@]}"; do
  safe_model="${model//[^A-Za-z0-9._-]/_}"
  log_file="${LOG_DIR}/nedrex-kg-eval-${safe_model}.log"

  echo
  echo "==> ${model}"
  echo "Log file: ${log_file}"

  set +e
  ./mvnw test \
    -Dtest=de.hamburg.university.tool.NeDRexKGEvaluationTest \
    -Dchatdrex.eval.providers=OLLAMA \
    -Dchatdrex.eval.runs="${EVAL_RUNS}" \
    "-Dquarkus.langchain4j.openai.chat-model.model-name=${model}" \
    "${OPTIONAL_PROPS[@]}" \
    "$@" 2>&1 | tee "$log_file"
  status="${PIPESTATUS[0]}"
  set -e

  if [[ "$status" -ne 0 ]]; then
    echo "Model ${model} failed with exit code ${status}"
    if [[ "$CONTINUE_ON_FAILURE" == "true" ]]; then
      continue
    fi
    exit "$status"
  fi
done

echo
echo "All NeDRex KG Ollama evaluations completed."

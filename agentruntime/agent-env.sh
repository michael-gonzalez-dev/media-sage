# Sourced by worker-entrypoint.sh. Do not run directly.
#
# The entrypoint holds more secrets than the agent needs: the Supabase service-role key (transcript
# upload), the human Jira token and the GitHub App private key (installation-token minting). The agent
# (`claude -p`) and every tool it runs would inherit them, so it is started with `env -i` plus the
# names below. Anything not listed is not visible to it.
#
# Deliberate: ANTHROPIC_* (model access), GH_TOKEN (gh/git, one-hour installation token) and
# JIRA_BOT_* (the worker scripts and skills write to Jira as the bot). Not covered: ~/.git-credentials
# still holds the GitHub token on disk, readable by the agent.
#
# Add a name here only when a worker script, skill or the toolchain demonstrably reads it.
AGENT_ENV_ALLOWLIST=(
  # shell and toolchain
  PATH HOME USER LOGNAME SHELL LANG LC_ALL TERM TZ TMPDIR
  JAVA_HOME JAVA_TOOL_OPTIONS GRADLE_OPTS GRADLE_USER_HOME ANDROID_HOME ANDROID_SDK_ROOT
  DISABLE_AUTOUPDATER PYTHONUNBUFFERED
  # model access
  ANTHROPIC_AUTH_TOKEN ANTHROPIC_BASE_URL ANTHROPIC_MODEL
  # credentials the agent's scripts use
  GH_TOKEN JIRA_BOT_EMAIL JIRA_BOT_API_TOKEN
  # job context
  JOB_TYPE JOB_ID JOB_TIMEOUT_SECONDS JOB_START_EPOCH TICKET_KEY JIRA_TICKET_KEY PR_NUMBER
  GITHUB_OWNER GITHUB_REPO WORKER_SKIP_UI_RENDER
)

# Fills AGENT_ENV with NAME=value for each allowlisted variable that is set, for `env -i "${AGENT_ENV[@]}"`.
build_agent_env() {
  AGENT_ENV=()
  local name
  for name in "${AGENT_ENV_ALLOWLIST[@]}"; do
    if [ -n "${!name+x}" ]; then
      AGENT_ENV+=("$name=${!name}")
    fi
  done
}

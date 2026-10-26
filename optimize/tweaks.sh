#!/usr/bin/env bash
# Speed and memory tweaks that are safe and reversible. Nothing is uninstalled or disabled here.
#   optimize/tweaks.sh apply     - animations at half length
#                                - Play Store kept from running in the background (it stays installed and opens
#                                  normally; on one set its idle CPU fell 31% -> 6%)
#                                - closed apps frozen while cached (no CPU in the background)
#                                - app caches trimmed, only when the free storage is low (under 1 GB or 10%): the
#                                  caches (thumbnails, app data to load) are useful while there is room
#   optimize/tweaks.sh restore   back to Android's defaults
# Not by default until it is measured again: CACHED_LIMIT=16 optimize/tweaks.sh apply also keeps at most 16 closed
# apps in memory (only lowered, never raised). It shows more free RAM, but Android 14 raised this limit on purpose
# (up to 30% fewer cold app starts, source.android.com/docs/core/perf/cached-apps-freezer), and frozen cached apps
# use no CPU anyway. Seen on a TCL Google TV (Android 14): 1024 at boot; the device_config value only takes effect
# after a restart and did not survive one there (null again), so `dumpsys activity settings` shows the boot value
# and `device_config get activity_manager max_cached_processes` the setting.
set -euo pipefail
cd "$(dirname "$0")/.."
PY="${PY:-optimize/venv/bin/python}"
CACHED="${CACHED_LIMIT:-}"
case "${1:?usage: optimize/tweaks.sh apply|restore}" in
  apply)   v=0.5; bucket=restricted; bg=deny ;;
  restore) v=1.0; bucket=active;     bg=allow ;;
  *) echo "usage: optimize/tweaks.sh apply|restore"; exit 1 ;;
esac
cmds=(
  "settings put global window_animation_scale $v"
  "settings put global transition_animation_scale $v"
  "settings put global animator_duration_scale $v"
  "am set-standby-bucket com.android.vending $bucket"
  "cmd appops set com.android.vending RUN_IN_BACKGROUND $bg"
  "cmd appops set com.android.vending RUN_ANY_IN_BACKGROUND $bg"
)
if [ "$1" = apply ]; then
  if [ -n "$CACHED" ]; then   # opt-in only (see above); never raise the limit: low-RAM sets may sit below it
    now=$("$PY" -I optimize/tv.py "dumpsys activity settings 2> /dev/null | grep -m1 max_cached_processes" | tr -dc '0-9')
    if [ -n "$now" ] && [ "$now" -gt "$CACHED" ]; then
      cmds+=("device_config put activity_manager max_cached_processes $CACHED")
      echo "Closed apps kept in memory: $now -> $CACHED"
    else
      echo "Closed apps kept in memory: ${now:-unknown} (left as is)"
    fi
  else
    cmds+=("device_config delete activity_manager max_cached_processes")   # Android's own limit (undoes an older run)
  fi
  cmds+=("settings put global cached_apps_freezer enabled")
  # trimming throws away every app's cache (thumbnails, things apps would load again): only worth it when storage is short
  space=$("$PY" -I optimize/tv.py "df -k /data 2> /dev/null | tail -n 1" | tr -d '\r' | awk '{print $2, $4}') || space=""
  read -r total avail <<< "$space" || true
  if [[ "${total:-}" =~ ^[0-9]+$ && "${avail:-}" =~ ^[0-9]+$ && "$total" -gt 0 ]]; then
    if [ "$avail" -lt 1048576 ] || [ $((avail * 10)) -lt "$total" ]; then
      cmds+=("pm trim-caches 999G")
      echo "Free storage is low ($((avail / 1024)) MB of $((total / 1024)) MB): app caches trimmed."
    else
      echo "Free storage is fine ($((avail / 1024)) MB of $((total / 1024)) MB): app caches left alone."
    fi
  else
    echo "Free storage unknown: app caches left alone."
  fi
else
  cmds+=("device_config delete activity_manager max_cached_processes"
         "settings put global cached_apps_freezer device_default")
fi
printf '%s\n' "${cmds[@]}" | "$PY" -I optimize/tv.py --batch > /dev/null
echo "Animation scale $v, Play Store background: $bg, cached-app freezer: $([ "$1" = apply ] && echo on || echo default)"

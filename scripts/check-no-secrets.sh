#!/usr/bin/env bash
set -euo pipefail

if git grep -nE 'AIza[0-9A-Za-z_-]{35}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' -- ':!docs/superpowers/plans/*'; then
  echo 'Potential committed secret detected.' >&2
  exit 1
fi

echo 'No committed key or private-key patterns detected.'

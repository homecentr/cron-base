#!/usr/bin/with-contenv sh

if [ "$CRON_SCHEDULE" == "" ]
then
  echo "The env. variable CRON_SCHEDULE is not set but is mandatory."
  exit 1
fi

USER_NAME=$(getent passwd "$PUID" | cut -d: -f1)
echo "$CRON_SCHEDULE /usr/sbin/cron-tick-execute" > /etc/crontabs/$USER_NAME
FROM homecentr/base:2.4.0-alpine

ENV CRON_SCHEDULE=""
ENV PUSH_GATEWAY_URL=""

# Copy s6 configuration and scripts
COPY ./fs/ /

RUN rm /etc/crontabs/root && \
    apk add --no-cache \
        # Required to push metrics to push gateway
        curl=7.67.0-r0 \
        # Required for UUID generation
        util-linux=2.34-r1 && \
    chmod a+x /usr/sbin/cron-tick-execute
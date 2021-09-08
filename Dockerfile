FROM homecentr/base:3.4.0-alpine

ENV CRON_SCHEDULE=""
ENV PUSH_GATEWAY_URL=""

# Copy s6 configuration and scripts
COPY ./fs/ /

RUN rm /etc/crontabs/root && \
    apk add --no-cache \
        # Required to push metrics to push gateway
        curl=7.78.0-r0 \
        # Required for UUID generation
        util-linux=2.37-r0 && \
    chmod a+x /usr/sbin/cron-tick-execute
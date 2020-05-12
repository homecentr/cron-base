FROM homecentr/base:2.1.0-alpine

ENV CRON_SCHEDULE=""
ENV PUSH_GATEWAY_URL=""

# Fail the image execution if the init scripts fail
ENV S6_BEHAVIOUR_IF_STAGE2_FAILS=2

RUN rm /etc/crontabs/root && \
    apk add --no-cache \
        # Required to push metrics to push gateway
        curl=7.67.0-r0 \
        # Required for UUID generation
        util-linux=2.34-r1

# Copy s6 configuration and scripts
COPY ./fs/ /
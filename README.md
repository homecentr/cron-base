[![Project status](https://badgen.net/badge/project%20status/stable%20%26%20actively%20maintaned?color=green)](https://github.com/homecentr/docker-cron-base/graphs/commit-activity) [![](https://badgen.net/github/label-issues/homecentr/docker-cron-base/bug?label=open%20bugs&color=green)](https://github.com/homecentr/docker-cron-base/labels/bug) [![](https://badgen.net/github/release/homecentr/docker-cron-base)](https://hub.docker.com/repository/docker/homecentr/cron-base)
[![](https://badgen.net/docker/pulls/homecentr/cron-base)](https://hub.docker.com/repository/docker/homecentr/cron-base) 
[![](https://badgen.net/docker/size/homecentr/cron-base)](https://hub.docker.com/repository/docker/homecentr/cron-base)

![CI/CD on master](https://github.com/homecentr/docker-cron-base/workflows/CI/CD%20on%20master/badge.svg)

# HomeCentr - cron-base
This docker image is used as base image for all homecentr images which require a cron scheduler but can also be used on its own. The image executes a mounted script at the specified schedule and reports the results into a Prometheus [push-gateway](https://github.com/prometheus/pushgateway) for easy monitoring and alerting.

## Usage

```yml
version: "3.7"
services:
  cron-base:
    build: .
    image: homecentr/cron-base
    restart: unless-stopped
    environment:
      CRON_SCHEDULE: "* * * * *" # Run script every minute
      PUSH_GATEWAY_URL: "http://push_gateway:9091/metrics/job/cron/label-name/label-value"
    volumes:
      - ./example/success:/cron # must contain cron-tick script
```

## Environment variables

| Name | Default value | Description |
|------|---------------|-------------|
| PUID | 7077 | UID of the user the cron-tick script should be running as.  |
| PGID | 7077 | GID of the group the cron-tick script should be running as. |
| CRON_SCHEDULE |  | [Cron expression](https://crontab.guru/) which defines when/how often the script will be executed. This variable is **mandatory**. |
| PUSH_GATEWAY_URL | | URL of the [push gateway](https://github.com/prometheus/pushgateway) job where the metrics should be reported. The reporting is skipped if the variable is not set. |

## Exposed ports

The image does not expose any ports.

## Volumes

| Container path | Description |
|-------------|----------------|
| /cron | Directory containing the script which should be executed, the script must be named `cron-tick`. |

## Security
The container is regularly scanned for vulnerabilities and updated. Further info can be found in the [Security tab](https://github.com/homecentr/docker-cron-base/security).

### Container user
The container supports privilege drop. Even though the container starts as root, it will use the permissions only to perform the initial set up. The cron-tick script is executed as UID/GID provided in the PUID and PGID environment variables.

:warning: Do not change the container user directly using the `user` Docker compose property or using the `--user` argument. This would break the privilege drop logic.
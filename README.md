# ipmailer

Emails you when your IP has changed.

## Usage

### Configuration

Before running, you need to set up the configuration, which are set via environment variables.

These can be passed on the command line or in a `.env` file.

| variable               | description                                                         | example                           |
|:-----------------------|:--------------------------------------------------------------------|:----------------------------------|
| IPMAILER_SMTP_HOST     | The SMTP host the email will be sent from                           | smtp.gmail.com                    |
| IPMAILER_SMTP_USER     | The SMTP user                                                       | example@example.com               |
| IPMAILER_SMTP_PASS     | The SMTP password                                                   | pA$$w0rd                          |
| IPMAILER_SMTP_PORT     | The SMTP port                                                       | 587                               |
| IPMAILER_SMTP_TLS      | Use TLS                                                             | true                              |
| IPMAILER_SMTP_FROM     | The "From" field                                                    | Bob Smith \<example@example.com\> |
| IPMAILER_SMTP_TO       | The receiving email to be notified of an IP change                  | somebody@example.com              |
| IPMAILER_POLL_INTERVAL | How often, in ms, to poll for an IP change (optional, default: 30s) | 30000                             |


### Development


```
$ lein run
```

### Production

#### Manually

```
$ java -jar ipmailer-standalone.jar 
```

#### systemd

Place `systemd/ipmailer.service` in `/etc/system/systemd/system/` directory and start/enable the service.

## Installation

Arch Linux AUR PKGBUILD coming soon. Otherwise see [Usage](#usage).
